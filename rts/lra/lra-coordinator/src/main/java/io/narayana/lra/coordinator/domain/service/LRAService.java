/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package io.narayana.lra.coordinator.domain.service;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import io.narayana.lra.client.IllegalLRAStateException;
import io.narayana.lra.client.InvalidLRAId;
import io.narayana.lra.client.LRAClient;
import io.narayana.lra.coordinator.domain.model.LRARecord;
import io.narayana.lra.coordinator.internal.Implementations;
import io.narayana.lra.coordinator.internal.LRARecoveryModule;
import io.narayana.lra.coordinator.domain.model.LRAStatus;
import io.narayana.lra.coordinator.domain.model.Transaction;

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
    private Map<URL, Transaction> lras = new ConcurrentHashMap<>();
    private Map<URL, Transaction> recoveringLRAs = new ConcurrentHashMap<>();

    private static Map<String, String> participants = new ConcurrentHashMap<>();
    private static Boolean isTrace = Boolean.getBoolean("trace");
    private LRARecoveryModule lraRecoveryModule;

    @Inject
    LRAClient lraClient;

    public Transaction getTransaction(URL lraId) throws NotFoundException {
        if (!lras.containsKey(lraId)) {
            if (!recoveringLRAs.containsKey(lraId))
                throw new NotFoundException(Response.status(404).entity("Invalid transaction id: " + lraId).build());

            return recoveringLRAs.get(lraId);
        }

        return lras.get(lraId);
    }

    public List<LRAStatus> getAll() {
        List<LRAStatus> all = getAllActive();

        all.addAll(getAllRecovering());

        return all;
    }

    public List<LRAStatus> getAllActive() {
        return lras.values().stream().map(LRAStatus::new).collect(toList());
    }

    public List<LRAStatus> getAllRecovering() {
        return recoveringLRAs.values().stream().map(LRAStatus::new).collect(toList());
    }

    public void addTransaction(Transaction lra) {
        lras.put(lra.getId(), lra);
    }

    public void finished(Transaction transaction, boolean fromHierarchy) {
        if (transaction.isRecovering()) {
            recoveringLRAs.put(transaction.getId(), transaction);
        } else if (fromHierarchy || transaction.isTopLevel()) {
            // the LRA is top level or it's a nested LRA that was closed by a
            // parent LRA (ie when fromHierarchy is true) then it's okay to forget about the LRA

            remove(ActionStatus.stringForm(transaction.status()), transaction.getId());
        }
    }

    public void remove(String state, URL lraId) {
        lraTrace(lraId, "remove LRA");

        if (lras.containsKey(lraId)) {
            lras.get(lraId); // validate that the LRA exists

            lras.remove(lraId);
        }

        if (recoveringLRAs.containsKey(lraId)) {
            recoveringLRAs.remove(lraId);
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

        try {
            lra = new Transaction(baseUri, parentLRA, clientId);
        } catch (MalformedURLException e) {
            throw new InvalidLRAId(baseUri, "Invalid base uri", e);
        }

        if (lra.currentLRA() != null)
            System.out.printf("WARNING LRA %s is already associated");

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

        if (!transaction.isActive() && !transaction.isRecovering() && transaction.isTopLevel())
            throw new IllegalLRAStateException(lraId.toString(), "LRA is closing or closed", null);

        transaction.end(compensate);

        if (transaction.currentLRA() != null)
            System.out.printf("WARNING LRA %s ended but is still associated with %s%n",
                    lraId, transaction.currentLRA().get_uid().fileStringForm());

        finished(transaction, fromHierarchy);

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

    public synchronized int joinLRA(StringBuilder recoveryUrl, URL lra, long timeLimit,
                                    String compensatorUrl, String linkHeader, String recoveryUrlBase,
                                    String compensatorData) {
        if (lra ==  null)
            lraTrace(null, "Error missing LRA header in join request");

        lraTrace(lra, "join LRA");

        Transaction transaction = getTransaction(lra);

        if (timeLimit < 0)
            timeLimit = 0;

        if (!transaction.isActive())
            return Response.Status.PRECONDITION_FAILED.getStatusCode();

        String recoveryURL = transaction.enlistParticipant(lra,
                linkHeader != null ? linkHeader : compensatorUrl, recoveryUrlBase,
                timeLimit, compensatorData);

        if (recoveryURL == null) // probably already closing or cancelling
            return Response.Status.PRECONDITION_FAILED.getStatusCode();

        addCompensator(transaction, recoveryURL, compensatorUrl);

        recoveryUrl.append(recoveryURL);

        return Response.Status.OK.getStatusCode();
    }

    public boolean hasTransaction(String id) {
        return lras.containsKey(id);
    }

    private void lraTrace(URL lraId, String reason) {
        if (isTrace) {
            if (lras.containsKey(lraId)) {
                Transaction lra = lras.get(lraId);
                System.out.printf("%s (%s) in state %s: %s%n",
                        reason, lra.getClientId(), ActionStatus.stringForm(lra.status()), lra.getId());
            } else {
                System.out.printf("%s not found: %s%n", reason, lraId);
            }
        }
    }

    public int renewTimeLimit(URL lraId, Long timelimit) {
        Transaction lra = lras.get(lraId);

        if (lra == null)
            return Response.Status.PRECONDITION_FAILED.getStatusCode();

        return lra.setTimeLimit(timelimit);
    }

    // TODO why doesn't this get triggered after construction
//    @PostConstruct
    // TODO enableRecovery(@Observes @Initialized(ApplicationScoped.class) Object init)
    public void enableRecovery() {
        if (lraRecoveryModule == null) {
            lraRecoveryModule = new LRARecoveryModule();
            lraRecoveryModule.getRecoveringLRAs(recoveringLRAs);
            RecoveryManager.manager().addModule(lraRecoveryModule);
            Implementations.install();

/*   TODO         for (Transaction transaction : recoveringLRAs.values())
                for (LRARecord lraRecord : transaction.getRecords())
                    addCompensator(lraRecord.getRecoveryCoordinatorURL(), lraRecord.getCompensatorURL());*/
        }

        RecoveryManager.manager().scan();
    }

    //    @PreDestroy
    public void disableRecovery() {
        if (lraRecoveryModule != null) {
            Implementations.uninstall();
            RecoveryManager.manager().removeModule(lraRecoveryModule, false);
            lraRecoveryModule = null;
        }
    }
}
