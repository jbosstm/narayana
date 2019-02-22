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
import io.narayana.lra.coordinator.domain.model.LRAData;
import io.narayana.lra.logging.LRALogger;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;

import io.narayana.lra.coordinator.domain.model.LRARecord;
import io.narayana.lra.coordinator.internal.Implementations;
import io.narayana.lra.coordinator.internal.LRARecoveryModule;
import io.narayana.lra.coordinator.domain.model.LRAStatusHolder;
import io.narayana.lra.coordinator.domain.model.Transaction;

import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.client.IllegalLRAStateException;
import org.eclipse.microprofile.lra.client.InvalidLRAIdException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@ApplicationScoped
public class LRAService {
    private Map<URL, Transaction> lras = new ConcurrentHashMap<>();
    private Map<URL, Transaction> recoveringLRAs = new ConcurrentHashMap<>();
    private Map<URL, ReentrantLock> locks = new ConcurrentHashMap<>();

    private static Map<String, String> participants = new ConcurrentHashMap<>();
    private LRARecoveryModule lraRecoveryModule;

    public Transaction getTransaction(URL lraId) throws NotFoundException {
        if (!lras.containsKey(lraId)) {
            if (!recoveringLRAs.containsKey(lraId)) {
                throw new NotFoundException(Response.status(404).entity("Invalid transaction id: " + lraId).build());
            }

            return recoveringLRAs.get(lraId);
        }

        return lras.get(lraId);
    }

    public LRAData getLRA(URL lraId) {
        Transaction lra = getTransaction(lraId);
        return lra.getLRAData();
    }

    public synchronized ReentrantLock lockTransaction(URL lraId) {
        ReentrantLock lock = locks.computeIfAbsent(lraId, k -> new ReentrantLock());

        lock.lock();

        return lock;
    }

    public synchronized ReentrantLock tryLockTransaction(URL lraId) {
        ReentrantLock lock = locks.computeIfAbsent(lraId, k -> new ReentrantLock());

        return lock.tryLock() ? lock : null;
    }

    public List<LRAStatusHolder> getAll(String state) {
        if (state == null || state.isEmpty()) {
            Set<LRAStatusHolder> all = getAllActive();

            all.addAll(getAllRecovering());

            return new ArrayList<>(all);
        }

        if (LRAStatus.Cancelling.name().equals(state)) {
            return recoveringLRAs.values().stream().map(LRAStatusHolder::new).filter(LRAStatusHolder::isCancelling).collect(toList());
        } else if (LRAStatus.Cancelled.name().equals(state)) {
            return recoveringLRAs.values().stream().map(LRAStatusHolder::new).filter(LRAStatusHolder::isCancelled).collect(toList());
        } else if (LRAStatus.FailedToCancel.name().equals(state)) {
            return recoveringLRAs.values().stream().map(LRAStatusHolder::new).filter(LRAStatusHolder::isFailedToCancel).collect(toList());
        } else if (LRAStatus.Closing.name().equals(state)) {
            return recoveringLRAs.values().stream().map(LRAStatusHolder::new).filter(LRAStatusHolder::isClosing).collect(toList());
        } else if (LRAStatus.Closed.name().equals(state)) {
            return recoveringLRAs.values().stream().map(LRAStatusHolder::new).filter(LRAStatusHolder::isClosed).collect(toList());
        } else if (LRAStatus.FailedToClose.name().equals(state)) {
            return recoveringLRAs.values().stream().map(LRAStatusHolder::new).filter(LRAStatusHolder::isFailedToClose).collect(toList());
        }

        return null;
    }

    private Set<LRAStatusHolder> getAllActive() {
        return lras.values().stream().map(LRAStatusHolder::new).collect(toSet());
    }

    public List<LRAStatusHolder> getAllRecovering(boolean scan) {
        if (scan) {
            RecoveryManager.manager().scan();
        }

        return recoveringLRAs.values().stream().map(LRAStatusHolder::new).collect(toList());
    }

    public List<LRAStatusHolder> getAllRecovering() {
        return getAllRecovering(false);
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

        if (locks.containsKey(lraId)) {
            locks.remove(lraId);
        }
    }

    public void updateRecoveryURL(URL lraId, String compensatorUrl, String recoveryURL, boolean persist) {
        assert recoveryURL != null;
        assert compensatorUrl != null;

        participants.put(recoveryURL, compensatorUrl);

        if (persist && lraId != null) {
            Transaction transaction = getTransaction(lraId);

            transaction.updateRecoveryURL(compensatorUrl, recoveryURL);
        }
    }

    public String getParticipant(String rcvCoordId) {
        return participants.get(rcvCoordId);
    }

    public synchronized URL startLRA(String baseUri, URL parentLRA, String clientId, Long timelimit) {
        Transaction lra;

        try {
            lra = new Transaction(this, baseUri, parentLRA, clientId);
        } catch (MalformedURLException e) {
            throw new InvalidLRAIdException(baseUri, "Invalid base uri", e);
        }

        if (lra.currentLRA() != null) {
            if (LRALogger.logger.isInfoEnabled()) {
                LRALogger.logger.infof("LRAServicve.startLRA LRA %s is already associated%n",
                        lra.currentLRA().get_uid().fileStringForm());
            }
        }

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

    public LRAStatusHolder endLRA(URL lraId, boolean compensate, boolean fromHierarchy) {
        lraTrace(lraId, "end LRA");

        Transaction transaction = getTransaction(lraId);

        if (!transaction.isActive() && !transaction.isRecovering() && transaction.isTopLevel()) {
            throw new IllegalLRAStateException(lraId.toString(), "LRA is closing or closed", "endLRA");
        }

        transaction.end(compensate);

        if (transaction.currentLRA() != null) {
            if (LRALogger.logger.isInfoEnabled()) {
                LRALogger.logger.infof("LRAServicve.endLRA LRA %s ended but is still associated with %s%n",
                        lraId, transaction.currentLRA().get_uid().fileStringForm());
            }
        }

        finished(transaction, fromHierarchy);

        if (transaction.isTopLevel()) {
            // forget any nested LRAs
            transaction.forgetAllParticipants(); // instruct compensators to clean up
        }

        return new LRAStatusHolder(transaction);
    }

    public int leave(URL lraId, String compensatorUrl) {
        lraTrace(lraId, "leave LRA");

        Transaction transaction = getTransaction(lraId);

        if (!transaction.isActive()) {
            return Response.Status.PRECONDITION_FAILED.getStatusCode();
        }

        try {
            if (!transaction.forgetParticipant(compensatorUrl)) {
                if (LRALogger.logger.isInfoEnabled()) {
                    LRALogger.logger.infof("LRAServicve.forget %s failed%n", lraId);
                }
            }

            return Response.Status.OK.getStatusCode();
        } catch (Exception e) {
            return Response.Status.BAD_REQUEST.getStatusCode();
        }
    }

    public synchronized int joinLRA(StringBuilder recoveryUrl, URL lra, long timeLimit,
                                    String compensatorUrl, String linkHeader, String recoveryUrlBase,
                                    String compensatorData) {
        if (lra ==  null) {
            lraTrace(null, "Error missing LRA header in join request");
        } else {
            lraTrace(lra, "join LRA");
        }

        Transaction transaction = getTransaction(lra);

        if (timeLimit < 0) {
            timeLimit = 0;
        }

        if (!transaction.isActive()) {
            return Response.Status.PRECONDITION_FAILED.getStatusCode();
        }

        LRARecord participant;

        try {
            participant = transaction.enlistParticipant(lra,
                    linkHeader != null ? linkHeader : compensatorUrl, recoveryUrlBase,
                    timeLimit, compensatorData);
        } catch (UnsupportedEncodingException e) {
            return Response.Status.PRECONDITION_FAILED.getStatusCode();
        }

        if (participant == null || participant.getRecoveryCoordinatorURL() == null) {
            // probably already closing or cancelling
            return Response.Status.PRECONDITION_FAILED.getStatusCode();
        }

        String recoveryURL = participant.getRecoveryCoordinatorURL().toExternalForm();

        updateRecoveryURL(lra, participant.getParticipantURL(), recoveryURL, false);

        recoveryUrl.append(recoveryURL);

        return Response.Status.OK.getStatusCode();
    }

    public boolean hasTransaction(URL id) {
        return lras.containsKey(id);
    }

    public boolean hasTransaction(String id) {
        try {
            return lras.containsKey(new URL(id));
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private void lraTrace(URL lraId, String reason) {
        if (LRALogger.logger.isTraceEnabled()) {
            if (lraId != null && lras.containsKey(lraId)) {
                Transaction lra = lras.get(lraId);
                LRALogger.logger.tracef("LRAService: '%s' (%s) in state %s: %s%n",
                        reason, lra.getClientId(), ActionStatus.stringForm(lra.status()), lra.getId());
            } else {
                LRALogger.logger.tracef("LRAService: '%s', not found: %s%n", reason, lraId);
            }
        }
    }

    public int renewTimeLimit(URL lraId, Long timelimit) {
        Transaction lra = lras.get(lraId);

        if (lra == null) {
            return Response.Status.PRECONDITION_FAILED.getStatusCode();
        }

        return lra.setTimeLimit(timelimit);
    }

    public boolean isLocal(URL lraId) {
        return hasTransaction(lraId);
    }

    /**
     * When the deployment is loaded register for recovery
     *
     * @param init a javax.servlet.ServletContext
     */
    void enableRecovery(@Observes @Initialized(ApplicationScoped.class) Object init) {
        assert lraRecoveryModule == null;

        if (LRALogger.logger.isDebugEnabled()) {
            LRALogger.logger.debugf("LRAServicve.enableRecovery%n");
        }

        lraRecoveryModule = new LRARecoveryModule(this);
        RecoveryManager.manager().addModule(lraRecoveryModule);
        Implementations.install();

        lraRecoveryModule.getRecoveringLRAs(recoveringLRAs);

        for (Transaction transaction : recoveringLRAs.values()) {
            transaction.getRecoveryCoordinatorUrls(participants);
        }
    }

    /**
     * When the deployment is unloaded unregister for recovery
     *
     * @param init a javax.servlet.ServletContext
     */
    void disableRecovery(@Observes @Destroyed(ApplicationScoped.class) Object init) {
        assert lraRecoveryModule != null;

        Implementations.uninstall();
        RecoveryManager.manager().removeModule(lraRecoveryModule, false);
        lraRecoveryModule = null;

        if (LRALogger.logger.isDebugEnabled()) {
            LRALogger.logger.debugf("LRAServicve.disableRecovery%n");
        }
    }
}
