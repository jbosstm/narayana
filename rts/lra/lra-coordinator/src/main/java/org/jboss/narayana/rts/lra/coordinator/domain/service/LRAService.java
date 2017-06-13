/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.narayana.rts.lra.coordinator.domain.service;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import org.jboss.narayana.rts.lra.client.IllegalLRAStateException;
import org.jboss.narayana.rts.lra.client.InvalidLRAId;
import org.jboss.narayana.rts.lra.client.LRAClient;
import org.jboss.narayana.rts.lra.coordinator.domain.model.LRAStatus;
import org.jboss.narayana.rts.lra.coordinator.domain.model.Transaction;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

@ApplicationScoped
public class LRAService {
    private Map<URL, Transaction> transactions = new ConcurrentHashMap<>();
    private Map<URL, Transaction> recoveringTransactions = new ConcurrentHashMap<>();

    private static Map<String, String> participants = new ConcurrentHashMap<>();
    private static Boolean isTrace = Boolean.getBoolean("trace");

    @Inject
    LRAClient lraClient;

    public Transaction getTransaction(URL lraId) throws NotFoundException {
        if (!transactions.containsKey(lraId))
            throw new NotFoundException(Response.status(404).entity("Invalid transaction id: " + lraId).build());

        return transactions.get(lraId);
    }

    public List<LRAStatus> getAll() {
        List<LRAStatus> all = getAllActive();

        all.addAll(getAllRecovering());

        return all;
    }

    public List<LRAStatus> getAllActive() {
        return transactions.values().stream().map(LRAStatus::new).collect(toList());
    }

    public List<LRAStatus> getAllRecovering() {
        return recoveringTransactions.values().stream().map(LRAStatus::new).collect(toList());
    }

    public void addTransaction(Transaction lra) {
        transactions.put(lra.getId(), lra);
    }

    public void finished(Transaction transaction, boolean fromHierarchy, boolean needsRecovery) {
        if (fromHierarchy || transaction.isTopLevel()) {
            remove(ActionStatus.stringForm(transaction.status()), transaction.getId());
        }

        if (needsRecovery)
            recoveringTransactions.put(transaction.getId(), transaction);
    }

    public void remove(String state, URL lraId) {
        lraTrace(lraId, "remove LRA");

        if (transactions.containsKey(lraId)) {
            Transaction lra = transactions.get(lraId);

//            if (lra.isTopLevel()) {
                transactions.remove(lraId);
                recoveringTransactions.remove(lraId);
//            }

            // TODO make sure we clean up nested LRAs when the top level LRA closes
        }
    }

    public void addCompensator(Transaction transaction, String coordinatorId, String compensatorUrl) {
        participants.put(coordinatorId, compensatorUrl);
    }

    public String getParticipant(String rcvCoordId) {
        return participants.get(rcvCoordId);
    }

    public synchronized URL startLRA(String baseUri, URL parentLRA, String clientId, Long timelimit) {
        Transaction lra;
//        long timeout;

        try {
            lra = new Transaction(baseUri, parentLRA, clientId);
        } catch (MalformedURLException e) {
            throw new InvalidLRAId(baseUri, "Invalid base uri", e);
        }

        if (lra.currentLRA() != null)
            System.out.printf("WARNING LRA %s is already associated");

        // AtomicAction uses seconds for timeouts
/*        if (timelimit <= 0)
            timeout = 0; // no timeout
        } else if (timelimit < 1000) {
            timeout = 1; // 1 second
        } else {
            long ltl = timelimit / 1000; // round down millis to the nearest second


            if (ltl > Integer.MAX_VALUE) {
                System.out.printf("WARNING LRA %s requested timeout is bigger than Integer.MAX_VALUE seconds, rounding down");
                ltl = Integer.MAX_VALUE;
            }

            timeout = (int) ltl;
        }*/


        int status = lra.begin(timelimit);

        if (status != ActionStatus.RUNNING) {
            lraTrace(lra.getId(), "failed to start LRA");

            lra.abort();

            throw new InternalServerErrorException("Could not start LRA: " + ActionStatus.stringForm(status));
        } else {
            try {
                addTransaction(lra);

                lraTrace(lra.getId(), "started LRA");

                return lra.getId();
            } finally {
                AtomicAction.suspend();
            }
        }
    }

    public LRAStatus endLRA(URL lraId, boolean compensate, boolean fromHierarchy) {
        lraTrace(lraId, "end LRA");

        Transaction transaction = getTransaction(lraId);

        if (!transaction.isActive() && transaction.isTopLevel())
            throw new IllegalLRAStateException(lraId.toString(), "LRA is closing or closed", null);

        int status = transaction.end(compensate);

        if (transaction.currentLRA() != null)
            System.out.printf("WARNING LRA %s ended but is still associated with %s%n",
                    lraId, transaction.currentLRA().get_uid().fileStringForm());

        finished(transaction, fromHierarchy, false); // TODO implement recovery

        if (transaction.isTopLevel()) {
            // forget any nested LRAs
            transaction.forgetAllParticipants(); // instruct compensators to clean up
        }

        return new LRAStatus(transaction);
    }

    public int leave(URL lraId, String compensatorUrl) {
        lraTrace(lraId, "leave LRA");

        Transaction transaction = getTransaction(lraId);

        if (!transaction.isActive())
            return Response.Status.PRECONDITION_FAILED.getStatusCode();

        try {
            if (!transaction.forgetParticipant(compensatorUrl))
                System.out.printf("WARNING forget %s failed%n", lraId);

            return Response.Status.OK.getStatusCode();
        } catch (Exception e) {
            return Response.Status.BAD_REQUEST.getStatusCode();
        }
    }

    // TODO return a jax-rs Response
    public synchronized int joinLRA(StringBuilder recoveryUrl, URL lra, long timeLimit, String compensatorUrl, String linkHeader, String recoveryUrlBase) {
        if (lra ==  null)
            lraTrace(null, "Error missing LRA header in join request");

        lraTrace(lra, "join LRA");

        Transaction transaction = getTransaction(lra);

        if (timeLimit < 0)
            timeLimit = 0; // TODO use it

        /*
         * TODO update the spec with:
         *   If the transaction is not TransactionActive then the implementation MUST return a 412 status code
         */
        if (!transaction.isActive())
            return Response.Status.PRECONDITION_FAILED.getStatusCode();

        String recoveryURL = transaction.enlistParticipant(lra,
                linkHeader != null ? linkHeader : compensatorUrl, recoveryUrlBase,
                timeLimit);

        if (recoveryURL == null) // probably already closing or cancelling
            return Response.Status.PRECONDITION_FAILED.getStatusCode();

        addCompensator(transaction, recoveryURL, compensatorUrl);

        recoveryUrl.append(recoveryURL);

        return Response.Status.OK.getStatusCode();
    }

    public boolean hasTransaction(String id) {
        return transactions.containsKey(id);
    }

    private void lraTrace(URL lraId, String reason) {
        if (isTrace) {
            if (transactions.containsKey(lraId)) {
                Transaction lra = transactions.get(lraId);
                System.out.printf("%s (%s) in state %s: %s%n",
                        reason, lra.getClientId(), ActionStatus.stringForm(lra.status()), lra.getId());
            } else {
                System.out.printf("%s not found: %s%n", reason, lraId);
            }
        }
    }

    public int renewTimeLimit(URL lraId, Long timelimit) {
        Transaction lra = transactions.get(lraId);

        if (lra == null)
            return Response.Status.PRECONDITION_FAILED.getStatusCode();

        return lra.setTimeLimit(timelimit);
    }
}
