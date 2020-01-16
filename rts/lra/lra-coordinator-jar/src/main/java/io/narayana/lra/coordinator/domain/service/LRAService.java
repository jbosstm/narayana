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
import io.narayana.lra.LRAConstants;
import io.narayana.lra.coordinator.domain.model.LRAData;
import io.narayana.lra.logging.LRALogger;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;

import io.narayana.lra.coordinator.domain.model.LRARecord;
import io.narayana.lra.coordinator.internal.Implementations;
import io.narayana.lra.coordinator.internal.LRARecoveryModule;
import io.narayana.lra.coordinator.domain.model.LRAStatusHolder;
import io.narayana.lra.coordinator.domain.model.Transaction;

import org.eclipse.microprofile.lra.annotation.LRAStatus;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@ApplicationScoped
public class LRAService {
    private Map<URI, Transaction> lras = new ConcurrentHashMap<>();
    private Map<URI, Transaction> recoveringLRAs = new ConcurrentHashMap<>();
    private Map<URI, ReentrantLock> locks = new ConcurrentHashMap<>();

    private Map<String, String> participants = new ConcurrentHashMap<>();
    private LRARecoveryModule lraRecoveryModule;

    public Transaction getTransaction(URI lraId) throws NotFoundException {
        if (!lras.containsKey(lraId)) {
            String uid = getUid(lraId);

            // try comparing on uid since differnt URIs can map to the same resource
            // (eg localhost versus 127.0.0.1 versus :1 etc)
            for (Transaction lra : lras.values()) {
                if (uid.equals(lra.getUid())) {
                    return lra;
                }
            }

            if (!recoveringLRAs.containsKey(lraId)) {
                for (Transaction lra : recoveringLRAs.values()) {
                    if (uid.equals(lra.getUid())) {
                        return lra;
                    }
                }

                throw new NotFoundException(Response.status(404).entity("Invalid transaction id: " + lraId).build());
            }

            return recoveringLRAs.get(lraId);
        }

        return lras.get(lraId);
    }

    /*
     * extract the Uid of the LRA from the LRA id
     */
    private String getUid(URI lraId) {
        String path = lraId.getPath();

        return path.substring(path.lastIndexOf('/') + 1);
    }

    public LRAData getLRA(URI lraId) {
        Transaction lra = getTransaction(lraId);
        return lra.getLRAData();
    }

    public synchronized ReentrantLock lockTransaction(URI lraId) {
        ReentrantLock lock = locks.computeIfAbsent(lraId, k -> new ReentrantLock());

        lock.lock();

        return lock;
    }

    public synchronized ReentrantLock tryLockTransaction(URI lraId) {
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

            if (!transaction.hasPendingActions()) {
                // this call is only required to clean up cached LRAs (JBTM-3250 will remove this cache).
                remove(ActionStatus.stringForm(transaction.status()), transaction.getId());
            }
        }
    }

    public void remove(String state, URI lraId) {
        lraTrace(lraId, "remove LRA");

        if (lras.containsKey(lraId)) {
            lras.get(lraId); // validate that the LRA exists

            lras.remove(lraId);
        }

        recoveringLRAs.remove(lraId);

        locks.remove(lraId);
    }

    public void updateRecoveryURI(URI lraId, String compensatorUrl, String recoveryURI, boolean persist) {
        assert recoveryURI != null;
        assert compensatorUrl != null;

        participants.put(recoveryURI, compensatorUrl);

        if (persist && lraId != null) {
            Transaction transaction = getTransaction(lraId);

            transaction.updateRecoveryURI(compensatorUrl, recoveryURI);
        }
    }

    public String getParticipant(String rcvCoordId) {
        return participants.get(rcvCoordId);
    }

    public synchronized URI startLRA(String baseUri, URI parentLRA, String clientId, Long timelimit) {
        Transaction lra;

        try {
            lra = new Transaction(this, baseUri, parentLRA, clientId);
        } catch (URISyntaxException e) {
            throw new WebApplicationException(e, Response.status(Response.Status.PRECONDITION_FAILED)
                    .entity(String.format("Invalid base URI: '%s'", baseUri)).build());
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

    public LRAStatusHolder endLRA(URI lraId, boolean compensate, boolean fromHierarchy) {
        lraTrace(lraId, "end LRA");

        Transaction transaction = getTransaction(lraId);

        if (!transaction.isActive() && !transaction.isRecovering() && transaction.isTopLevel()) {
            throw new WebApplicationException(Response.status(Response.Status.PRECONDITION_FAILED)
                    .entity(String.format("%s: LRA is closing or closed: endLRA", lraId)).build());
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

    public int leave(URI lraId, String compensatorUrl) {
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

    public synchronized int joinLRA(StringBuilder recoveryUrl, URI lra, long timeLimit,
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

        // the tx must be either Active (for participants with the @Compensate methods) or
        // Closing/Canceling (for the AfterLRA listeners)
        if (!transaction.isActive()) {
            // validate that the party wanting to join with this LRA is a listener only:
            if (linkHeader != null) {
                Pattern linkRelPattern = Pattern.compile("(\\w+)=\"([^\"]+)\"|([^\\s]+)");
                Matcher relMatcher = linkRelPattern.matcher(linkHeader);

                while (relMatcher.find()) {
                    String key = relMatcher.group(1);

                    if (key != null && key.equals("rel")) {
                        String rel = relMatcher.group(2) == null ? relMatcher.group(3) : relMatcher.group(2);

                        if (!LRAConstants.AFTER.equals(rel)) {
                            // participants are not allowed to join inactive LRAs
                            return Response.Status.PRECONDITION_FAILED.getStatusCode();
                        } else if (!transaction.isRecovering()) {
                            // listeners cannot be notified if the LRA has already ended
                            return Response.Status.PRECONDITION_FAILED.getStatusCode();
                        }
                    }
                }
            }
        }

        LRARecord participant;

        try {
            participant = transaction.enlistParticipant(lra,
                    linkHeader != null ? linkHeader : compensatorUrl, recoveryUrlBase,
                    timeLimit, compensatorData);
        } catch (UnsupportedEncodingException e) {
            return Response.Status.PRECONDITION_FAILED.getStatusCode();
        }

        if (participant == null || participant.getRecoveryURI() == null) {
            // probably already closing or cancelling
            return Response.Status.PRECONDITION_FAILED.getStatusCode();
        }

        String recoveryURI = participant.getRecoveryURI().toASCIIString();

        updateRecoveryURI(lra, participant.getParticipantURI(), recoveryURI, false);

        recoveryUrl.append(recoveryURI);

        return Response.Status.OK.getStatusCode();
    }

    public boolean hasTransaction(URI id) {
        return lras.containsKey(id);
    }

    public boolean hasTransaction(String id) {
        try {
            return lras.containsKey(new URI(id));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private void lraTrace(URI lraId, String reason) {
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

    public int renewTimeLimit(URI lraId, Long timelimit) {
        Transaction lra = lras.get(lraId);

        if (lra == null) {
            return Response.Status.PRECONDITION_FAILED.getStatusCode();
        }

        return lra.setTimeLimit(timelimit);
    }

    public boolean isLocal(URI lraId) {
        return hasTransaction(lraId);
    }

    /**
     * When the deployment is loaded register for recovery
     */
    @PostConstruct
    void enableRecovery() {
        assert lraRecoveryModule == null;

        if (LRALogger.logger.isDebugEnabled()) {
            LRALogger.logger.debugf("LRAServicve.enableRecovery");
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
     */
    @PreDestroy
    void disableRecovery() {
        if (lraRecoveryModule != null) {

            RecoveryManager.manager().removeModule(lraRecoveryModule, false);
            Implementations.uninstall();

            lraRecoveryModule = null;

            if (LRALogger.logger.isDebugEnabled()) {
                LRALogger.logger.debugf("LRAServicve.disableRecovery");
            }
        }
    }
}
