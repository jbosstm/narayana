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
import com.arjuna.ats.arjuna.coordinator.RecordType;
import io.narayana.lra.client.NarayanaLRAInfo;
import io.narayana.lra.logging.LRALogger;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;

import io.narayana.lra.coordinator.domain.service.LRAService;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.client.InvalidLRAIdException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Transaction extends AtomicAction {
    private static final String LRA_TYPE = "/StateManager/BasicAction/TwoPhaseCoordinator/LRA";
    private final ScheduledExecutorService scheduler;
    private URL id;
    private URL parentId;
    private String clientId;
    private List<LRARecord> pending;
    private LRAStatus status;
    private String responseData;
    private LocalDateTime startTime;
    private LocalDateTime finishTime; // TODO make sure this acted upon during restore_state()
    private ScheduledFuture<?> scheduledAbort;
    private boolean inFlight;
    private LRAService lraService;

    public Transaction(LRAService lraService, String baseUrl, URL parentId, String clientId) throws MalformedURLException {
        super(new Uid());

        this.lraService = lraService;
        this.id = new URL(String.format("%s/%s", baseUrl, get_uid().fileStringForm()));
        this.inFlight = true;
        this.parentId = parentId;
        this.clientId = clientId;
        this.finishTime = LocalDateTime.MAX;
        this.status = LRAStatus.Active;

        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public Transaction(LRAService lraService, Uid rcvUid) {
        super(rcvUid);

        this.lraService = lraService;
        this.inFlight = false;
        this.id = null;
        this.parentId = null;
        this.clientId = null;
        this.finishTime = LocalDateTime.MAX;
        this.status = LRAStatus.Active;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public NarayanaLRAInfo getLRAInfo() {
        return new NarayanaLRAInfo(id.toExternalForm(), clientId, status == null ? "" : status.name(),
                isComplete(), isCompensated(), isRecovering(),
                isActive(), isTopLevel(),
                startTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
                finishTime == null ? 0L : finishTime.toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    public static String getType() {
        return LRA_TYPE;
    }

    public boolean save_state(OutputObjectState os, int ot) {
        if (!super.save_state(os, ot)
                || !save_list(os, ot, pendingList)
                || !save_list(os, ot, preparedList)) {
            return false;
        }

        try {
            os.packString(id == null ? null : id.toString());
            os.packString(parentId == null ? null : parentId.toString());
            os.packString(clientId);
            os.packLong(startTime == null ? 0L : startTime.toInstant(ZoneOffset.UTC).toEpochMilli());
            os.packLong(finishTime == null ? 0L : finishTime.toInstant(ZoneOffset.UTC).toEpochMilli());

            if (status == null) {
                os.packBoolean(false);
            } else {
                os.packBoolean(true);
                os.packString(status.name());
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    private boolean save_list(OutputObjectState os, int ot, RecordList list) {
        if (list != null && list.size() > 0) {
            AbstractRecord first, temp;

            first = temp = list.getFront();

            while (temp != null) {
                list.putRear(temp);

                if (!temp.doSave()) {
                    return false;
                }

                try {
                    os.packInt(temp.typeIs());

                    if (!temp.save_state(os, ot)) {
                        return false;
                    }
                } catch (IOException e) {
                    return false;
                }

                temp = list.getFront();

                if (temp == first) {
                    list.putFront(temp);
                    temp = null;
                }
            }
        }

        try {
            os.packInt(RecordType.NONE_RECORD);
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    private boolean restore_list(InputObjectState os, int ot, RecordList list) {

        int record_type = RecordType.NONE_RECORD;

        try {
            while ((record_type = os.unpackInt()) != RecordType.NONE_RECORD) {
                AbstractRecord record = AbstractRecord.create(record_type);

                if (record == null || !record.restore_state(os, ot) || !list.insert(record)) {
                    return false;
                }

                if (record instanceof LRARecord) {
                    ((LRARecord) record).setLRAService(lraService);
                }

            }
        } catch (IOException | NullPointerException e1) {
            LRALogger.i18NLogger.warn_coordinatorNorecordfound(Integer.toString(record_type), e1);

            return false;
        }

        return true;
    }

    public boolean restore_state(InputObjectState os, int ot) {

        if (!super.restore_state(os, ot)
                || !restore_list(os, ot, pendingList)
                || !restore_list(os, ot, preparedList)) {
            return false;
        }

        try {
            String s = os.unpackString();
            id = s == null ? null : new URL(s);
            s = os.unpackString();
            parentId = s == null ? null : new URL(s);
            clientId = os.unpackString();
            long startMillis = os.unpackLong();
            startTime = startMillis == 0 ? null :
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(startMillis), ZoneOffset.UTC);
            long finishMillis = os.unpackLong();
            finishTime = finishMillis == 0 ? null :
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(finishMillis), ZoneOffset.UTC);
            status = os.unpackBoolean() ? LRAStatus.valueOf(os.unpackString()) : null;

            return true;
        } catch (IOException e) {
            if (LRALogger.logger.isDebugEnabled()) {
                LRALogger.logger.debugf(e, "Cannot restore state of objec type '%s'", ot);
            }

            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Transaction)) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

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

    protected LRAService getLraService() {
        return lraService;
    }

    /**
     * return the current status of the LRA
     *
     * @return null if the LRA is still active (not closing, cancelling, closed or cancelled
     */
    public LRAStatus getLRAStatus() {
        return status;
    }

    protected void setLRAStatus(int actionStatus) {
        status = toLRAStatus(actionStatus);
    }

    boolean isComplete() {
        return status != null && status.equals(LRAStatus.Closed);
    }

    boolean isCompensated() {
        return status != null && status.equals(LRAStatus.Cancelled);
    }

    public boolean isRecovering() {
        return status != null &&
                (status.equals(LRAStatus.Cancelling) || status.equals(LRAStatus.Closing));
    }

    private int cancelLRA() {
        return end(true);
    }

    protected ReentrantLock tryLockTransaction() {
        return lraService.tryLockTransaction(getId());
    }

    public int end(boolean compensate) {
        ReentrantLock lock = null;

        try {
            lock = lraService.tryLockTransaction(getId());

            if (lock == null) {
                if (LRALogger.logger.isInfoEnabled()) {
                    LRALogger.logger.debugf("Transaction.endLRA Some other thread is finishing LRA %s", getId().toExternalForm());
                }

                return status();
            }

            return doEnd(compensate);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    // in this version close need to run as blocking code {@link Vertx().executeBlocking}
    private int doEnd(boolean compensate) {
        inFlight = false;
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

                updateState(LRAStatus.Cancelling);

                super.phase2Abort(true);
//                res = super.Abort();

                res = status();

                status = toLRAStatus(status());
            }
        } else {
            if (compensate || status() == ActionStatus.ABORT_ONLY) {
                // compensators must be called in reverse order so reverse the pending list
                int sz = pendingList == null ? 0 : pendingList.size();

                for (int i = sz - 1; i > 0; i--) {
                    pendingList.putRear(pendingList.getFront());
                }

                // tell each participant that the LRA canceled
                updateState(LRAStatus.Cancelling);

                // since the phase2Abort route skips prepare we need to make sure the heuristic list exists
                if (heuristicList == null) {
                    heuristicList = new RecordList();
                }

                super.phase2Abort(true); // this route to abort forces a log write on failures and heuristics
                res = super.status();
            } else {
                // tell each participant that the LRA completed ok
                updateState(LRAStatus.Closing);
                res = super.End(true);
            }
        }

        updateState();

        if (pending != null && pending.size() != 0) {
            if (!nested) {
                pending.clear(); // TODO we will loose this data if we need recovery
            }
        }

        if (getSize(heuristicList) != 0 || getSize(failedList) != 0) {
            status = LRAStatus.Cancelling;
        } else if (getSize(pendingList) != 0 || getSize(preparedList) != 0) {
            status = LRAStatus.Closing;
        } else {
            status = toLRAStatus(res);
        }

        if (!isRecovering()) {
            if (lraService != null) {
                lraService.finished(this, false);
            }
        }

        responseData = isActive() ? null : status.name();

        finishTime = LocalDateTime.now();

        return res;
    }

    private boolean updateState(LRAStatus nextState) {
        status = nextState;

        return (pendingList == null || pendingList.size() == 0) || deactivate();
    }

    private int getSize(RecordList list) {
        return list == null ? 0 : list.size();
    }

    private LRAStatus toLRAStatus(int atomicActionStatus) {
        switch (atomicActionStatus) {
            case ActionStatus.ABORTING:
                return LRAStatus.Cancelling;
            case ActionStatus.ABORT_ONLY:
                return LRAStatus.Cancelling;
            case ActionStatus.ABORTED:
                return LRAStatus.Cancelled;
            case ActionStatus.COMMITTING:
                return LRAStatus.Closing;
            case ActionStatus.COMMITTED:
                return LRAStatus.Closed;
            case ActionStatus.H_ROLLBACK:
                return LRAStatus.Cancelled;
            default:
                return LRAStatus.Active;
        }
    }

    public LRARecord enlistParticipant(URL coordinatorUrl, String participantUrl, String recoveryUrlBase,
                                    long timeLimit, String compensatorData) throws UnsupportedEncodingException {
        LRARecord participant = findLRAParticipant(participantUrl, false);

        if (participant != null) {
            return participant; // must have already been enlisted
        }

        participant = enlistParticipant(coordinatorUrl, participantUrl, recoveryUrlBase, null,
                timeLimit, compensatorData);

        if (participant != null && findLRAParticipant(participantUrl, false) != null) {
            // need to remember that there is a new participant
            deactivate(); // if it fails the superclass will have logged a warning
            savedIntentionList = true; // need this clean up if the LRA times out
        }

        return participant;
    }

    public LRARecord enlistParticipant(URL coordinatorUrl, String participantUrl, String recoveryUrlBase, String terminateUrl,
                                       long timeLimit, String compensatorData) throws UnsupportedEncodingException {
        if (findLRAParticipant(participantUrl, false) != null) {
            return null;    // already enlisted
        }

        LRARecord p = new LRARecord(lraService, coordinatorUrl.toExternalForm(), participantUrl, compensatorData);
        String pid = p.get_uid().fileStringForm();

        String txId = URLEncoder.encode(coordinatorUrl.toExternalForm(), "UTF-8");

        p.setRecoveryURL(recoveryUrlBase, txId, pid);

        if (add(p) != AddOutcome.AR_REJECTED) {
            if (!p.setTimeLimit(scheduler, timeLimit, this)) {
                if (LRALogger.logger.isInfoEnabled()) {
                    LRALogger.logger.infof("Transaction.enlistParticipant unable to start timer for %s", participantUrl);
                }
            }

            return p;
        }

        return null;
    }

    public Boolean isActive() {
        return status == null || status == LRAStatus.Active;
    }

    public boolean forgetParticipant(String participantUrl) {
        return findLRAParticipant(participantUrl, true) != null;
    }

    public void forgetAllParticipants() {
        if (pending != null) {
            pending.forEach(LRARecord::forget);
        }
    }

    private void savePendingList() {
        if (pendingList == null || pending != null) {
            return;
        }

        RecordListIterator i = new RecordListIterator(pendingList);
        AbstractRecord r;

        pending = new ArrayList<>();

        while ((r = i.iterate()) != null) {
            if (r instanceof LRARecord) {
                pending.add((LRARecord) r);
            }
        }
    }

    private LRARecord findLRAParticipant(String participantUrl, boolean remove) {
        LRARecord rec = null;

        try {
            URL recoveryUrl = new URL(participantUrl);

            rec = findLRAParticipantByRecoveryUrl(recoveryUrl, remove, pendingList, preparedList, heuristicList, failedList);

        } catch (MalformedURLException ignore) {
            String pUrl = LRARecord.extractCompensator(id, participantUrl);
            rec = findLRAParticipant(pUrl, remove, pendingList, pendingList, preparedList, heuristicList, failedList);
        }

        return rec;
    }

    private LRARecord findLRAParticipant(String participantUrl, boolean remove, RecordList...lists) {
        for (RecordList list : lists) {
            if (list != null) {
                RecordListIterator i = new RecordListIterator(list);
                AbstractRecord r;

                if (participantUrl.indexOf(',') != -1) {
                    participantUrl = LRARecord.extractCompensator(id, participantUrl);
                }

                while ((r = i.iterate()) != null) {
                    if (r instanceof LRARecord) {
                        LRARecord rr = (LRARecord) r;
                        // can't use == because this may be a recovery scenario
                        if (rr.getParticipantPath().equals(participantUrl) ||
                                rr.getCompensator().equals(participantUrl)) {
                            if (remove) {
                                list.remove(rr);
                            }

                            return rr;
                        }
                    }
                }
            }
        }

        return null;
    }

    private LRARecord findLRAParticipantByRecoveryUrl(URL recoveryUrl, boolean remove, RecordList...lists) {
        for (RecordList list : lists) {
            if (list != null) {
                RecordListIterator i = new RecordListIterator(list);
                AbstractRecord r;

                while ((r = i.iterate()) != null) {
                    if (r instanceof LRARecord) {
                        LRARecord rr = (LRARecord) r;
                        // can't use == because this may be a recovery scenario
                        if (rr.getRecoveryCoordinatorURL().equals(recoveryUrl)) {
                            if (remove) {
                                list.remove(rr);
                            }

                            return rr;
                        }
                    }
                }
            }
        }

        return null;
    }

    public boolean isTopLevel() {
        return parentId == null;
    }

    public String getResponseData() {
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
        if (status == null || status == LRAStatus.Active) {
            return Status.NO_CONTENT.getStatusCode(); // in progress, 204
        }

        switch (status) {
            case Closed:
            case Cancelled:
                return Status.OK.getStatusCode(); // 200
            case Closing:
            case Cancelling:
                return Status.ACCEPTED.getStatusCode(); // 202
            case FailedToCancel:
            case FailedToClose:
                return Status.PRECONDITION_FAILED.getStatusCode(); // 412, probably not the correct code
            default:
                return Status.INTERNAL_SERVER_ERROR.getStatusCode(); // 500
        }
    }

    public int begin(Long timeLimit) {
        int res = super.begin(); // no timeout because the default timeunit (SECONDS) is too course

        startTime = LocalDateTime.now();

        setTimeLimit(timeLimit);

        return res;
    }

    // TODO should this trickle down to compensators or do we need a separate API for that
    public int setTimeLimit(Long timeLimit) {
        return scheduleCancelation(this::abortLRA, timeLimit);
    }

    private int scheduleCancelation(Runnable runnable, Long timeLimit) {
        if ((scheduledAbort != null && !scheduledAbort.cancel(false)) || status() != ActionStatus.RUNNING) {
            return Response.Status.PRECONDITION_FAILED.getStatusCode();
        }

        if (timeLimit > 0) {
            finishTime = LocalDateTime.now().plusNanos(timeLimit * 1000000);

            scheduledAbort = scheduler.schedule(runnable, timeLimit, TimeUnit.MILLISECONDS);
        } else {
            finishTime = null;

            scheduledAbort = null;
        }

        return Response.Status.OK.getStatusCode();
    }

    private void abortLRA() {
        int status = status();

        if (status == ActionStatus.RUNNING || status == ActionStatus.ABORT_ONLY) {
            if (LRALogger.logger.isDebugEnabled()) {
                LRALogger.logger.debugf("Transaction.abortLRA cancelling LRA %s", id);
            }

            CompletableFuture.supplyAsync(this::cancelLRA); // use a future to avoid hogging the ScheduledExecutorService
        }
    }

    private void getRecoveryCoordinatorUrls(Map<String, String> participants, RecordList list) {
        RecordListIterator iter = new RecordListIterator(list);
        AbstractRecord rec;

        while (((rec = iter.iterate()) != null)) {
            if (rec instanceof LRARecord) { //rec.typeIs() == LRARecord.getTypeId()) {
                LRARecord lraRecord = (LRARecord) rec;

                participants.put(lraRecord.getRecoveryCoordinatorURL().toExternalForm(), lraRecord.getParticipantPath());
            }
        }
    }

    public void getRecoveryCoordinatorUrls(Map<String, String> participants) {
        getRecoveryCoordinatorUrls(participants, pendingList);
        getRecoveryCoordinatorUrls(participants, preparedList);
    }

    public void updateRecoveryURL(String compensatorUrl, String recoveryURL) {
        LRARecord lraRecord = findLRAParticipant(compensatorUrl, false);

        if (lraRecord != null) {
            try {
                lraRecord.setRecoveryURL(recoveryURL);


                if (!deactivate()) {
                    if (LRALogger.logger.isInfoEnabled()) {
                        LRALogger.logger.infof("Could not save new recovery URL");
                    }
                }

            } catch (InvalidLRAIdException e) {
                if (LRALogger.logger.isInfoEnabled()) {
                    LRALogger.logger.infof("Could not save new recovery URL: %s", e.getMessage());
                }
            }
        }
    }

    public boolean isInFlight() {
        return inFlight;
    }

    void timedOut(LRARecord lraRecord) {
        // a participant has timed out so cancel the whole LRA
        ReentrantLock lock = lraService.tryLockTransaction(getId());

        if (lock != null) {
            try {
                if (isActive()) {
                    doEnd(true);
                } // else it is too late to cancel
            } finally {
                lock.unlock();
            }
        }  // else another thread finishing this LRA so it is too late to cancel
    }
}
