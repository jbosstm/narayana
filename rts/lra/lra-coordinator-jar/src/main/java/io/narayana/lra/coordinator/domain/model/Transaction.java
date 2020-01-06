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
import io.narayana.lra.RequestBuilder;
import io.narayana.lra.ResponseHolder;
import io.narayana.lra.logging.LRALogger;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;

import io.narayana.lra.coordinator.domain.service.LRAService;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static io.narayana.lra.LRAHttpClient.PARTICIPANT_TIMEOUT;

public class Transaction extends AtomicAction {
    private static final String LRA_TYPE = "/StateManager/BasicAction/TwoPhaseCoordinator/LRA";
    private final ScheduledExecutorService scheduler;
    private URI id;
    private URI parentId;
    private String clientId;
    private List<LRARecord> pending;
    private List<AfterLRAListener> afterLRAListeners;
    private LRAStatus status;
    private String responseData;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private ScheduledFuture<?> scheduledAbort;
    private boolean inFlight;
    private LRAService lraService;
    private String uid;

    public Transaction(LRAService lraService, String baseUrl, URI parentId, String clientId) throws URISyntaxException {
        super(new Uid());

        this.uid = get_uid().fileStringForm();
        this.lraService = lraService;
        this.id = new URI(String.format("%s/%s", baseUrl, get_uid().fileStringForm()));
        this.inFlight = true;
        this.parentId = parentId;
        this.clientId = clientId;
        this.finishTime = null;
        this.status = LRAStatus.Active;

        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public Transaction(LRAService lraService, Uid rcvUid) {
        super(rcvUid);

        this.uid = rcvUid.fileStringForm();
        this.lraService = lraService;
        this.inFlight = false;
        this.id = null;
        this.parentId = null;
        this.clientId = null;
        this.finishTime = null;
        this.status = LRAStatus.Active;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public LRAData getLRAData() {
        return new LRAData(id.toASCIIString(), clientId, status == null ? "" : status.name(),
                isClosed(), isCancelled(), isRecovering(),
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

            if (startTime == null) {
                os.packBoolean(false);
            } else {
                os.packBoolean(true);
                os.packLong(startTime.toInstant(ZoneOffset.UTC).toEpochMilli());
            }

            if (finishTime == null) {
                os.packBoolean(false);
            } else {
                os.packBoolean(true);
                os.packLong(finishTime.toInstant(ZoneOffset.UTC).toEpochMilli());
            }

            if (status == null) {
                os.packBoolean(false);
            } else {
                os.packBoolean(true);
                os.packString(status.name());
            }

            if (afterLRAListeners == null) {
                os.packInt(0);
            } else {
                os.packInt(afterLRAListeners.size());
                for (AfterLRAListener participant : afterLRAListeners) {
                    os.packString(participant.notificationURI.toASCIIString());
                    os.packString(participant.recoveryId.toASCIIString());
                }
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

        uid = get_uid().fileStringForm();

        try {
            String s = os.unpackString();
            id = s == null ? null : new URI(s);
            s = os.unpackString();
            parentId = s == null ? null : new URI(s);
            clientId = os.unpackString();
            startTime = os.unpackBoolean() ? LocalDateTime.ofInstant(Instant.ofEpochMilli(os.unpackLong()), ZoneOffset.UTC) : null;
            finishTime = os.unpackBoolean() ? LocalDateTime.ofInstant(Instant.ofEpochMilli(os.unpackLong()), ZoneOffset.UTC) : null;
            status = os.unpackBoolean() ? LRAStatus.valueOf(os.unpackString()) : null;

            int cnt = os.unpackInt();

            if (cnt == 0) {
                afterLRAListeners = null;
            } else {
                afterLRAListeners = new ArrayList<>();
                for (int i = 0; i < cnt; i++) {
                    afterLRAListeners.add(new AfterLRAListener(new URI(os.unpackString()), new URI(os.unpackString())));
                }
            }

            /*
             * If the time limit has already been reached then the difference between now and the scheduled
             * abort time will be negative. Since scheduling a task with a negative time will run it immediately
             * we must ensure that the setTimeLimit call is placed after the state has been fully re-hydrated.
             */
            if (finishTime != null) {
                long ttl = ChronoUnit.MILLIS.between(LocalDateTime.now(ZoneOffset.UTC), finishTime);

                if (ttl <= 0) {
                    if (LRALogger.logger.isDebugEnabled()) {
                        LRALogger.logger.debugf("Timer for LRA '%s' has expired since last reload", id);
                    }

                    status = LRAStatus.Cancelling;
                } else {
                    if (LRALogger.logger.isDebugEnabled()) {
                        LRALogger.logger.debugf("Restarting time for LRA '%s'", id);
                    }
                }

                setTimeLimit(ttl);
            }

            return true;
        } catch (IOException | URISyntaxException e) {
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

    public URI getId() {
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

    boolean isClosed() {
        return status != null && status.equals(LRAStatus.Closed);
    }

    boolean isCancelled() {
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

    public int end(boolean cancel) {
        ReentrantLock lock = null;

        try {
            lock = lraService.tryLockTransaction(getId());

            if (lock == null) {
                if (LRALogger.logger.isInfoEnabled()) {
                    LRALogger.logger.debugf("Transaction.endLRA Some other thread is finishing LRA %s",
                            getId().toASCIIString());
                }

                return status();
            }

            if (status == LRAStatus.Cancelling) {
                return doEnd(true);
            }

            return doEnd(cancel);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    private void updateAfterLRAListeners(RecordList list) {
        if (list == null || list.size() == 0) {
            return;
        }

        if (afterLRAListeners == null) {
            afterLRAListeners = new ArrayList<>();
        } else {
            afterLRAListeners.clear();
        }

        RecordListIterator i = new RecordListIterator(list);
        AbstractRecord r;

        while ((r = i.iterate()) != null) {
            if (r instanceof LRARecord) {
                URI endNotification = ((LRARecord) r).getEndNotificationUri();
                URI recoveryURI = ((LRARecord) r).getRecoveryURI();

                if (endNotification != null && recoveryURI != null) {
                    afterLRAListeners.add(new AfterLRAListener(endNotification, recoveryURI));
                }
            }
        }
    }

    // in this version close need to run as blocking code {@link Vertx().executeBlocking}
    private int doEnd(boolean cancel) {
        inFlight = false;
        int res = status();
        boolean nested = !isTopLevel();

        if (scheduledAbort != null) {
            scheduledAbort.cancel(false);
            scheduledAbort = null;
        }

        // nested compensators need to be remembered in case the enclosing LRA decides to cancel
        // also save the list so that we can retrieve any response data after committing compensators
        // if (nested)
        savePendingList();

        if ((res != ActionStatus.RUNNING) && (res != ActionStatus.ABORT_ONLY)) {
            if (nested && cancel) {
                /*
                 * Note that we do not hook into ActionType.NESTED because that would mean that after a
                 * nested txn is committed its participants are merged
                 * with the parent and they can then only be aborted if the parent aborts whereas in
                 * the LRA model nested LRAs can be cancelled whilst the enclosing LRA is closed
                 */

                // repopulate the pending list TODO it won't neccessarily be present during recovery
                pendingList = new RecordList();

                pending.forEach(r -> pendingList.putRear(r));

                updateAfterLRAListeners(pendingList);
                updateState(LRAStatus.Cancelling);

                super.phase2Abort(true);
//                res = super.Abort();

                res = status();

                status = toLRAStatus(status());
            }
        } else {
            if (cancel || status() == ActionStatus.ABORT_ONLY) {
                // compensators must be called in reverse order so reverse the pending list
                int sz = pendingList == null ? 0 : pendingList.size();

                for (int i = sz - 1; i > 0; i--) {
                    pendingList.putRear(pendingList.getFront());
                }

                updateAfterLRAListeners(pendingList);
                // tell each participant that the LRA canceled
                updateState(LRAStatus.Cancelling);

                // since the phase2Abort route skips prepare we need to make sure the heuristic list exists
                if (heuristicList == null) {
                    heuristicList = new RecordList();
                }

                super.phase2Abort(true); // this route to abort forces a log write on failures and heuristics
                res = super.status();
            } else {
                updateAfterLRAListeners(pendingList);
                // tell each participant that the LRA closed ok
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
            status = cancel ? LRAStatus.Cancelling : LRAStatus.Closing;
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

        finishTime = LocalDateTime.now(ZoneOffset.UTC);

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
                // FALLTHRU
            case ActionStatus.ABORT_ONLY:
                // FALLTHRU
            case ActionStatus.COMMITTING:
                return status == LRAStatus.Cancelling ? LRAStatus.Cancelling : LRAStatus.Closing;
            case ActionStatus.ABORTED:
                // FALLTHRU
            case ActionStatus.H_ROLLBACK:
                return status == LRAStatus.Cancelling ? LRAStatus.Cancelled : LRAStatus.Closed;
            case ActionStatus.COMMITTED:
                return status == LRAStatus.Cancelling ? LRAStatus.Cancelled : LRAStatus.Closed;
            default:
                return LRAStatus.Active;
        }
    }

    public LRARecord enlistParticipant(URI coordinatorUrl, String participantUrl, String recoveryUrlBase,
                                       long timeLimit, String compensatorData) throws UnsupportedEncodingException {
        LRARecord participant = findLRAParticipant(participantUrl, false);

        if (participant != null) {
            return participant; // must have already been enlisted
        }

        participant = enlistParticipant(coordinatorUrl, participantUrl, recoveryUrlBase, null,
                timeLimit, compensatorData);

        if (participant != null) {
            // need to remember that there is a new participant
            updateAfterLRAListeners(pendingList);
            deactivate(); // if it fails the superclass will have logged a warning
            savedIntentionList = true; // need this clean up if the LRA times out
        }

        return participant;
    }

    private LRARecord enlistParticipant(URI coordinatorUrl, String participantUrl, String recoveryUrlBase, String terminateUrl,
                                        long timeLimit, String compensatorData) throws UnsupportedEncodingException {
        LRARecord p = new LRARecord(lraService, coordinatorUrl.toASCIIString(), participantUrl, compensatorData);
        String pid = p.get_uid().fileStringForm();

        String txId = URLEncoder.encode(coordinatorUrl.toASCIIString(), "UTF-8");

        p.setRecoveryURI(recoveryUrlBase, txId, pid);

        if (add(p) != AddOutcome.AR_REJECTED) {
            setTimeLimit(timeLimit);

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
        if (pendingList == null) {
            savedIntentionList = true;
            return;
        } else if (pending != null) {
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
        LRARecord rec;

        try {
            URI recoveryUrl = new URI(LRARecord.cannonicalForm(participantUrl));

            rec = findLRAParticipantByRecoveryUrl(recoveryUrl, remove, pendingList, preparedList, heuristicList, failedList);

        } catch (URISyntaxException ignore) {
            String pUrl;
            try {
                pUrl = LRARecord.extractCompensator(id, participantUrl);
            } catch (URISyntaxException e) {
                return null;
            }
            rec = findLRAParticipant(pUrl, remove, pendingList, preparedList, heuristicList, failedList);
        }

        return rec;
    }

    private LRARecord findLRAParticipant(String participantUrl, boolean remove, RecordList...lists) {
        for (RecordList list : lists) {
            if (list != null) {
                RecordListIterator i = new RecordListIterator(list);
                AbstractRecord r;

                if (participantUrl.indexOf(',') != -1) {
                    try {
                        participantUrl = LRARecord.extractCompensator(id, participantUrl);
                    } catch (URISyntaxException e) {
                        continue;
                    }
                }

                while ((r = i.iterate()) != null) {
                    if (r instanceof LRARecord) {
                        LRARecord rr = (LRARecord) r;
                        // can't use == because this may be a recovery scenario
                        if (rr.getCompensator().equals(participantUrl) ||
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

    private LRARecord findLRAParticipantByRecoveryUrl(URI recoveryUrl, boolean remove, RecordList...lists) {
        for (RecordList list : lists) {
            if (list != null) {
                RecordListIterator i = new RecordListIterator(list);
                AbstractRecord r;

                while ((r = i.iterate()) != null) {
                    if (r instanceof LRARecord) {
                        LRARecord rr = (LRARecord) r;
                        // can't use == because this may be a recovery scenario
                        if (rr.getRecoveryURI().equals(recoveryUrl)) {
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

        startTime = LocalDateTime.now(ZoneOffset.UTC);

        setTimeLimit(timeLimit);

        deactivate();

        return res;
    }

    public int setTimeLimit(Long timeLimit) {
        if (timeLimit <= 0L) {
            return Response.Status.OK.getStatusCode();
        }

        return scheduleCancelation(this::abortLRA, timeLimit);
    }

    private int scheduleCancelation(Runnable runnable, Long timeLimit) {
        assert timeLimit > 0L;

        if (status() != ActionStatus.RUNNING) {
            if (LRALogger.logger.isDebugEnabled()) {
                LRALogger.logger.debugf("Ignoring timer because the action status is `%e'", status());
            }

            return Response.Status.PRECONDITION_FAILED.getStatusCode();
        }

        if (finishTime != null) {
            // check whether the new time limit is less than the current one
            LocalDateTime ft = LocalDateTime.now(ZoneOffset.UTC).plusNanos(timeLimit * 1000000);

            if (ft.isAfter(finishTime)) {
                if (LRALogger.logger.isDebugEnabled()) {
                    LRALogger.logger.debugf(
                            "Ignoring timer for LRA `%s' since there is already an earlier one", id);
                }

                // the existing timer finishes before the requested one so there is nothing to do
                return Response.Status.OK.getStatusCode();
            }

            // it is earlier so cancel the current timer
            finishTime = ft;

            if (scheduledAbort != null) {
                scheduledAbort.cancel(false);
            }
        } else {
            // if timeLimit is negative the abort will be scheduled immediately
            finishTime = LocalDateTime.now(ZoneOffset.UTC).plusNanos(timeLimit * 1000000);
        }

        scheduledAbort = scheduler.schedule(runnable, timeLimit, TimeUnit.MILLISECONDS);

        return Response.Status.OK.getStatusCode();
    }

    private void abortLRA() {
        ReentrantLock lock = tryLockTransaction();

        if (lock != null) {
            try {
                int actionStatus = status();

                scheduledAbort = null;

                if (actionStatus == ActionStatus.RUNNING || actionStatus == ActionStatus.ABORT_ONLY) {
                    if (LRALogger.logger.isDebugEnabled()) {
                        LRALogger.logger.debugf("Transaction.abortLRA cancelling LRA `%s", id);
                    }

                    status = LRAStatus.Cancelling;

                    CompletableFuture.supplyAsync(this::cancelLRA); // use a future to avoid hogging the ScheduledExecutorService
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private void getRecoveryCoordinatorUrls(Map<String, String> participants, RecordList list) {
        RecordListIterator iter = new RecordListIterator(list);
        AbstractRecord rec;

        while (((rec = iter.iterate()) != null)) {
            if (rec instanceof LRARecord) { //rec.typeIs() == LRARecord.getTypeId()) {
                LRARecord lraRecord = (LRARecord) rec;

                participants.put(lraRecord.getRecoveryURI().toASCIIString(), lraRecord.getParticipantPath());
            }
        }
    }

    public void getRecoveryCoordinatorUrls(Map<String, String> participants) {
        getRecoveryCoordinatorUrls(participants, pendingList);
        getRecoveryCoordinatorUrls(participants, preparedList);
    }

    public void updateRecoveryURI(String compensatorUri, String recoveryUri) {
        LRARecord lraRecord = findLRAParticipant(compensatorUri, false);

        if (lraRecord != null) {
            try {
                lraRecord.setRecoveryURI(recoveryUri);


                if (!deactivate()) {
                    if (LRALogger.logger.isInfoEnabled()) {
                        LRALogger.logger.infof("Could not save new recovery URL");
                    }
                }

            } catch (WebApplicationException e) {
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

    URI getParentId() {
        return parentId;
    }

    public boolean afterLRANotification() {
        boolean notifiedAll = true;

        if (afterLRAListeners != null) {
            try {
                Iterator<AfterLRAListener> listeners = afterLRAListeners.iterator();

                while (listeners.hasNext()) {
                    AfterLRAListener participant = listeners.next();
                    ResponseHolder response = null;

                    try {
                        response = new RequestBuilder(participant.notificationURI)
                                .request()
                                .header(LRA.LRA_HTTP_ENDED_CONTEXT_HEADER, id)
                                .header(LRA.LRA_HTTP_RECOVERY_HEADER, participant.recoveryId.toASCIIString())
                                .async(PARTICIPANT_TIMEOUT, TimeUnit.SECONDS)
                                .put(status.name(), MediaType.TEXT_PLAIN);

                        if (response.getStatus() == 200) {
                            listeners.remove();
                        } else {
                            notifiedAll = false;
                        }
                    } catch (WebApplicationException e) {
                        notifiedAll = false;

                        if (LRALogger.logger.isInfoEnabled()) {
                            LRALogger.logger.infof("Could not notify AfterLRA listener at %s (%s)", participant.notificationURI, e.getMessage());
                        }
                    }
                }
            } finally {
            }
        }

        return notifiedAll;
    }

    public String getUid() {
        return uid;
    }

    static class AfterLRAListener {
        public AfterLRAListener(URI notificationURI, URI recoveryId) {
            this.notificationURI = notificationURI;
            this.recoveryId = recoveryId;
        }

        final URI notificationURI;
        final URI recoveryId;
    }
}
