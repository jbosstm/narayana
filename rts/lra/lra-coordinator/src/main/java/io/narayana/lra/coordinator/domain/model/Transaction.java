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
package io.narayana.lra.coordinator.domain.model;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.RecordList;
import com.arjuna.ats.arjuna.coordinator.RecordListIterator;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.narayana.lra.annotation.CompensatorStatus;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Transaction extends AtomicAction {
    private static final String LRA_TYPE = "/StateManager/BasicAction/TwoPhaseCoordinator/LRA"; //org.jboss.jbossts.star.resource.Transaction {
    private final ScheduledExecutorService scheduler;
    private URL id;
    private URL parentId; // TODO save_state and restore_state
    private String clientId;
    private List<LRARecord> pending;
    private CompensatorStatus status; // reuse commpensator states for the LRA
    private List<String> responseData;
    private LocalDateTime cancelOn; // TODO make sure this acted upon during restore_state()
    private ScheduledFuture<?> scheduledAbort;

    public Transaction(String baseUrl, URL parentId, String clientId) throws MalformedURLException {
        super(new Uid());

        this.id = new URL(String.format("%s/%s", baseUrl, get_uid().fileStringForm()));
        this.parentId = parentId;
        this.clientId = clientId;
        this.cancelOn = null;
        this.status = null; // means the LRA is active

        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public Transaction(Uid rcvUid) {
        super(rcvUid);

        this.id = null;
        this.parentId = null;
        this.clientId = null;
        this.cancelOn = null;
        this.status = null; // means the LRA is active
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public static String getType() {
        return LRA_TYPE;
    }

    public boolean save_state (OutputObjectState os, int ot) {
        boolean saved = super.save_state(os, ot);

        try {
            os.packString(id == null ? null : id.toString());
            os.packString(parentId == null ? null : parentId.toString());
            os.packString(clientId);
            if (cancelOn != null)
                os.packLong(cancelOn.toInstant(ZoneOffset.UTC).toEpochMilli());
            else
                os.packLong(0L);

            if (status == null) {
                os.packBoolean(false);
            } else {
                os.packBoolean(true);
                os.packString(status.name());
            }
        } catch (IOException e) {
            return false;
        }

        return saved;
    }


    public boolean restore_state (InputObjectState os, int ot) {
        boolean restored = super.restore_state(os, ot);

        try {
            String s = os.unpackString();
            id = s == null ? null : new URL(s);
            s = os.unpackString();
            parentId = s == null ? null : new URL(s);
            clientId = os.unpackString();
            long millis = os.unpackLong();
            cancelOn = millis == 0 ? null : LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC);
            status = os.unpackBoolean() ? CompensatorStatus.valueOf(os.unpackString()) : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return restored;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        if (!super.equals(o)) return false;

        Transaction that = (Transaction) o;

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getId().hashCode();
        return result;
    }

    public String type() {
		return LRA_TYPE;
	}

    public URL getId() {
        return id;
    }

    public String getClientId() {
        return clientId;
    }

    /**
     * return the current status of the LRA
     *
     * @return null if the LRA is still active (not closing, cancelling, closed or cancelled
     */
    public CompensatorStatus getLRAStatus() {
        return status;
    }

    public boolean isComplete() {
        return status != null && status.equals(CompensatorStatus.Completed);
    }

    public boolean isCompensated() {
        return status != null && status.equals(CompensatorStatus.Compensated);
    }

    public boolean isRecovering() {
        return status != null && status.equals(CompensatorStatus.Compensating);
    }

    private int closeLRA() {
        return end(false);
    }

    private int cancelLRA() {
        return end(true);
    }

    // in this version close need to run as blocking code {@link Vertx().executeBlocking}
    public int end(/*Vertx vertx,*/ boolean compensate) {
        int res = status();
        boolean nested = !isTopLevel();

        if (scheduledAbort != null) {
            scheduledAbort.cancel(false);
            scheduledAbort = null;
        }

        // nested compensators need to be remembered in case the enclosing LRA decides to compensate
        // also save the list so that we can retrieve any response data after committing compensators
        // if (nested)
        savePendingList();

        if ((res != ActionStatus.RUNNING) && (res != ActionStatus.ABORT_ONLY)) {
            if (nested && compensate) {
                /*
                 * Note that we do not hook into ActionType.NESTED because that would mean that after a
                 * nested txn is committed its participants are merged
                 * with the parent and they can then only be aborted if the parent aborts whereas in
                 * the LRA model nested LRAs can be compensated whilst the enclosing LRA is completed
                 */

                // repopulate the pending list TODO it won't neccessarily be present during recovery
                pendingList = new RecordList();

                pending.forEach(r -> pendingList.putRear(r));

                super.phase2Abort(true);
//                res = super.Abort();

                res = status();

                status = toLRAStatus(status());
            }
        } else {
            if (compensate || status() == ActionStatus.ABORT_ONLY) {
                status = CompensatorStatus.Compensating;

                // compensators must be called in reverse order so reverse the pending list
                int sz = pendingList == null ? 0 : pendingList.size();

                for (int i = sz - 1; i > 0; i--)
                    pendingList.putRear(pendingList.getFront());

                // tell each compensator that the lra canceled
                res = super.Abort(); // this route to abort forces a log write on failures and heuristics
            } else {
                status = CompensatorStatus.Completing;

                // tell each compensator that the lra completed ok
                res = super.End(true);
            }
        }

        // gather up any response data
        ObjectMapper mapper = new ObjectMapper();

        if (pending != null && pending.size() != 0) {
            responseData = pending.stream()
                    .map(LRARecord::getResponseData)
                    .map(s -> getCompensatorResponse(mapper, s)) // some compensators may be for nested LRAs so their response data will be an encoded array
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            if (!nested)
                pending.clear(); // TODO we will loose this data if we need recovery
        }

        if (getSize(heuristicList) != 0 || getSize(failedList) != 0)
            status = CompensatorStatus.Compensating;
        else if (getSize(pendingList) != 0 || getSize(preparedList) != 0)
            status = CompensatorStatus.Completing;
        else
            status = toLRAStatus(res);

        return res;
    }

    private int getSize(RecordList list) {
        return list == null ? 0 : list.size();
    }

    private Collection<String> getCompensatorResponse(ObjectMapper mapper, String data) {
        if (data !=null && data.startsWith("[")) {
            try {
                String[] ja = mapper.readValue(data, String[].class);
                // TODO should reccurse here since the encoded strings may themselves contain compensator output
                return Arrays.asList(ja);
            } catch (IOException e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        } else {
            return Collections.singletonList(data);
        }

    }

    private CompensatorStatus toLRAStatus(int atomicActionStatus) {
        switch (atomicActionStatus) {
            case ActionStatus.ABORTING:
                return CompensatorStatus.Compensating;
            case ActionStatus.ABORT_ONLY:
                return CompensatorStatus.Compensating;
            case ActionStatus.ABORTED:
                return CompensatorStatus.Compensated;
            case ActionStatus.COMMITTING:
                return CompensatorStatus.Completing;
            case ActionStatus.COMMITTED:
                return CompensatorStatus.Completed;
            case ActionStatus.H_ROLLBACK:
                return CompensatorStatus.Compensated;
            default:
                return null;
        }
    }

    public String enlistParticipant(URL coordinatorUrl, String participantUrl, String recoveryUrlBase,
                                    long timeLimit, byte[] compensatorData) {
        LRARecord participant = findLRAParticipant(participantUrl);

        if (participant != null)
            return participant.get_uid().fileStringForm(); // must have already been enlisted

        String coordinatorId = enlistParticipant(coordinatorUrl.toString(), participantUrl, recoveryUrlBase, null,
                timeLimit, compensatorData);

        if (coordinatorId != null && findLRAParticipant(participantUrl) != null) {
            // need to remember that there is a new participant
            deactivate(); // if it fails the superclass will have logged a warning
            savedIntentionList = true; // need this clean up if the LRA times out
        }

        return coordinatorId;
    }

    public String enlistParticipant(String coordinatorUrl, String participantUrl, String recoveryUrlBase, String terminateUrl,
                                    long timeLimit, byte[] compensatorData) {
        if (findLRAParticipant(participantUrl) != null)
            return null;    // already enlisted

        String txId = get_uid().fileStringForm();
        LRARecord p = new LRARecord(txId, coordinatorUrl, participantUrl, compensatorData);
        String coordinatorId = p.get_uid().fileStringForm();

        String recoveryUrl = recoveryUrlBase + txId + '/' + coordinatorId;

        if (add(p) != AddOutcome.AR_REJECTED)
            return recoveryUrl;

        p.setTimeLimit(scheduler, timeLimit);

        return null;
    }

    public Boolean isActive() {
        return status == null;
    }

    public boolean forgetParticipant(String participantUrl) {
        return pendingList == null || pendingList.size() == 0 || doForgetParticipant(participantUrl);
    }

    private boolean doForgetParticipant(String participantLinkUrl) {
        LRARecord pUrl = findLRAParticipant(participantLinkUrl);

        return pUrl == null || pendingList.remove(pUrl);
    }

    public void forgetAllParticipants() {
        if (pending != null)
            pending.forEach(LRARecord::forget);
    }

    private void savePendingList() {
        if (pendingList == null || pending != null)
            return;

        RecordListIterator i = new RecordListIterator(pendingList);
        AbstractRecord r;

        pending = new ArrayList<>();

        while ((r = i.iterate()) != null) {
            if (r instanceof LRARecord) {
                pending.add((LRARecord) r);
            }
        }
    }

    private LRARecord findLRAParticipant(String participantUrl) {
        if (pendingList != null) {

            RecordListIterator i = new RecordListIterator(pendingList);
            AbstractRecord r;

            if (participantUrl.indexOf(',') != -1)
                participantUrl = LRARecord.cannonicalForm(participantUrl);

            while ((r = i.iterate()) != null) {
                if (r instanceof LRARecord) {
                    LRARecord rr = (LRARecord) r;
                    // can't use == because this may be a recovery scenario
                    if (rr.getParticipantPath().equals(participantUrl))
                        return rr;
                }
            }
        }

        return null;
    }

    public boolean isTopLevel() {
        return parentId == null;
    }

    public List<String> getResponseData() {
        return responseData;
    }

    public BasicAction currentLRA() {
        return ThreadActionData.currentAction();
    }

    public int getHttpStatus() {
        switch (status()) {
            case ActionStatus.COMMITTED:
            case ActionStatus.ABORTED:
                return 200;
            default:
                return lraStatusToHttpStatus();
        }
    }

    private int lraStatusToHttpStatus() {
        if (status == null)
            return 202; // in progress

        switch (status) {
            case Completed:
            case Compensated:
                return 200;
            case Compensating:
            case Completing:
                return 202;
            case FailedToComplete:
            case FailedToCompensate:
                return 412; // probably not the correct code
            default:
                return 500;
        }
    }

    public int begin(Long timeLimit) {
        int res = super.begin(); // no timeout because the default timeunit (SECONDS) is too course

        setTimeLimit(timeLimit);

        return res;
    }

    // TODO should this trickle down to compensators or do we need a separate API for that
    public int setTimeLimit(Long timeLimit) {
        return scheduleCancelation(this::abortLRA, timeLimit);
    }

    private int scheduleCancelation(Runnable runnable, Long timeLimit) {
        if ((scheduledAbort != null && !scheduledAbort.cancel(false)) || status() != ActionStatus.RUNNING)
            return Response.Status.PRECONDITION_FAILED.getStatusCode();

        if (timeLimit > 0) {
            cancelOn = LocalDateTime.now().plusNanos(timeLimit * 1000000);

            scheduledAbort = scheduler.schedule(runnable, timeLimit, TimeUnit.MILLISECONDS);
        } else {
            cancelOn = null;

            scheduledAbort = null;
        }

        return Response.Status.OK.getStatusCode();
    }

    private void abortLRA() {
        int status = status();

        if (status == ActionStatus.RUNNING || status == ActionStatus.ABORT_ONLY) {
            if (tsLogger.logger.isDebugEnabled()) {
                tsLogger.logger.debugf("Transaction.abortLRA cancelling LRA %s", id);
            }

            CompletableFuture.supplyAsync(this::cancelLRA); // use a future to avoid hogging the ScheduledExecutorService
        }
    }
}
