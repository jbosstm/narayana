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

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.RecordList;
import com.arjuna.ats.arjuna.coordinator.RecordListIterator;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import io.narayana.lra.Current;
import io.narayana.lra.LRAData;
import io.narayana.lra.logging.LRALogger;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

import io.narayana.lra.coordinator.domain.service.LRAService;
import org.eclipse.microprofile.lra.annotation.LRAStatus;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class LongRunningAction extends BasicAction {
    private static final String LRA_TYPE = "/StateManager/BasicAction/LongRunningAction";
    private static final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(10);
    private URI id;
    private URI parentId;
    private String clientId;
    private List<LRAParticipantRecord> pending;
    private LRAStatus status;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private ScheduledFuture<?> scheduledAbort;
    private final LRAService lraService;
    LRAParentAbstractRecord par;

    public LongRunningAction(LRAService lraService, String baseUrl, LongRunningAction parent, String clientId) throws URISyntaxException {
        super(new Uid());

        this.lraService = lraService;

        if (parent != null) {
            this.id = Current.buildFullLRAUrl(String.format("%s/%s", baseUrl, get_uid().fileStringForm()), parent.getId());
            this.parentId = parent.getId();
        } else {
            this.id = new URI(String.format("%s/%s", baseUrl, get_uid().fileStringForm()));
        }

        this.clientId = clientId;
        this.finishTime = null;
        this.status = LRAStatus.Active;

        trace_progress("created");
    }

    public LongRunningAction(LRAService lraService, Uid rcvUid) {
        super(rcvUid);

        this.lraService = lraService;
        this.id = null;
        this.parentId = null;
        this.clientId = null;
        this.finishTime = null;
        this.status = LRAStatus.Active;

        trace_progress("created");
    }

    // used for MBean LRA listing, see com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowser
    public LongRunningAction(Uid rcvUid) {
        this(new LRAService(), rcvUid);
    }

    /**
     * Creating {@link LRAData} from the current {@link LongRunningAction} state.
     * The data are immutable and represents the current state of the LRA transaction.
     *
     * @return  immutable {@link LRAData} representing the current state of the LRA transaction
     */
    public LRAData getLRAData() {
        return new LRAData(id, clientId, status, isTopLevel(), isRecovering(),
                startTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
                finishTime == null ? 0L : finishTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
                getHttpStatus());
    }

    public boolean save_state(OutputObjectState os, int ot) {
        if (!super.save_state(os, ot)
                || !save_list(os, ot, pendingList)) { // other lists are maintained in BasicAction
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

            os.packString(status.name());
        } catch (IOException e) {
            LRALogger.i18nLogger.warn_saveState(e.getMessage());
            return false;
        } finally {
            trace_progress("saved");
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

                if (record instanceof LRAParticipantRecord) {
                    LRAParticipantRecord lraRecord = (LRAParticipantRecord) record;
                    lraRecord.setLRAService(lraService);
                    lraRecord.setLRA(this);
                }

            }
        } catch (IOException | NullPointerException e1) {
            LRALogger.i18nLogger.warn_coordinatorNorecordfound(Integer.toString(record_type), e1);

            return false;
        }

        return true;
    }

    public boolean restore_state(InputObjectState os, int ot) {

        if (!super.restore_state(os, ot)
                || !restore_list(os, ot, pendingList)) { // other lists are maintained in BasicAction
            return false;
        }

        // restore_state may have put failed records onto the prepared list so move them back again:
        for (AbstractRecord rec = preparedList.peekFront(); rec != null; rec = preparedList.peekNext(rec)) {
            if (rec instanceof LRAParticipantRecord) {
                LRAParticipantRecord p = (LRAParticipantRecord) rec;

                if (p.isFailed()) {
                    AbstractRecord r;

                    preparedList.remove(p);

                    // put it back on the failedList if it isn't already on it
                    for (r = failedList.peekFront(); r != null; r = failedList.peekNext(r)) {
                        if (p.equals(r)) {
                            break;
                        }
                    }

                    if (r == null) {
                        failedList.putFront(p);
                    }
                }
            }
        }

        try {
            String s = os.unpackString();
            id = s == null ? null : new URI(s);
            s = os.unpackString();
            parentId = s == null ? null : new URI(s);
            clientId = os.unpackString();
            startTime = os.unpackBoolean() ? LocalDateTime.ofInstant(Instant.ofEpochMilli(os.unpackLong()), ZoneOffset.UTC) : null;
            finishTime = os.unpackBoolean() ? LocalDateTime.ofInstant(Instant.ofEpochMilli(os.unpackLong()), ZoneOffset.UTC) : null;
            status = LRAStatus.valueOf(os.unpackString());

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

                    if (status == LRAStatus.Active) {
                        status = LRAStatus.Cancelling; // transition from Active to Cancelling
                    }
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
                LRALogger.logger.debugf(e, "Cannot restore state of object type '%s'", ot);
            }

            LRALogger.i18nLogger.warn_restoreState(e.getMessage());

            return false;
        } finally {
            trace_progress("restored");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof LongRunningAction)) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        LongRunningAction that = (LongRunningAction) o;

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getId().hashCode();
        return result;
    }

    public static String getType() {
        return LRA_TYPE;
    }

    public String type() {
        return getType();
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
     * Return the current status of the LRA.
     *
     * @return current LRA status; never returns <code>null</code>
     */
    public LRAStatus getLRAStatus() {
        return status;
    }

    public boolean isRecovering() {
        return status.equals(LRAStatus.Cancelling) || status.equals(LRAStatus.Closing);
    }

    public boolean isFailed() {
        return status.equals(LRAStatus.FailedToCancel) || status.equals(LRAStatus.FailedToClose);
    }

    public boolean isCancel() {
        switch (status) {
            case Cancelling: /* FALLTHRU */
            case Cancelled: /* FALLTHRU */
            case FailedToCancel:
                return true;
            default:
                return false;
        }
    }

    boolean isFinished() {
        switch (status) {
            case Closed:
                /* FALLTHRU */
            case Cancelled:
                /* FALLTHRU */
            case FailedToClose:
                /* FALLTHRU */
            case FailedToCancel:
                return true;
            default: // if the participant lists are empty then assume finished (nb lists are processed by removal)
                return getSize(pendingList) == 0 && getSize(preparedList) == 0 && getSize(heuristicList) == 0;
        }
    }

    protected ReentrantLock tryLockTransaction() {
        return lraService.tryLockTransaction(getId());
    }

    public int finishLRA(boolean cancel) {
        return finishLRA(cancel, null, null);
    }

    public int finishLRA(boolean cancel, String compensator, String userData) {
        ReentrantLock lock = null;

        // check whether the transaction should cancel due to a timeout:
        if (finishTime != null && !cancel && ChronoUnit.MILLIS.between(LocalDateTime.now(ZoneOffset.UTC), finishTime) <= 0) {
            cancel = true;
            trace_progress("finishing with cancel");
        } else {
            trace_progress("finishing");
        }

        try {
            lock = lraService.tryLockTransaction(getId());

            if (lock == null) {
                if (LRALogger.logger.isInfoEnabled()) {
                    LRALogger.logger.debugf("LongRunningAction.endLRA Some other thread is finishing LRA %s",
                            getId().toASCIIString());
                }

                return status();
            }

            if (userData != null && !userData.isEmpty() && compensator != null && !compensator.isEmpty()) {
                updateCompensatorUserData(compensator, userData);
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

    // in this version close need to run as blocking code {@link Vertx().executeBlocking}
    private int doEnd(boolean cancel) {
        int res = status();
        boolean nested = !isTopLevel();

        if (status == LRAStatus.Active) {
            updateState(cancel ? LRAStatus.Cancelling : LRAStatus.Closing);
        } else if (isFinished()) {
            trace_progress("finished");
            return res;
        }

        if (scheduledAbort != null) {
            scheduledAbort.cancel(false);
            scheduledAbort = null;
        }

        // nested compensators need to be remembered in case the enclosing LRA decides to cancel
        // also save the list so that we can retrieve any response data after committing compensators
        // if (nested)
        savePendingList();

        if ((res != ActionStatus.RUNNING) && (res != ActionStatus.ABORT_ONLY)) {
            if (nested) {
                if (cancel) {
                    /*
                     * Note that we do not hook into ActionType.NESTED because that would mean that after a
                     * nested txn is committed its participants are merged
                     * with the parent and they can then only be aborted if the parent aborts whereas in
                     * the LRA model nested LRAs can be cancelled whilst the enclosing LRA is closed
                     */

                    // repopulate the pending list TODO it won't neccessarily be present during recovery
                    if (pendingList == null) {
                        pendingList = new RecordList();
                    }
                    if (preparedList == null) {
                        preparedList = new RecordList();
                    }
                    if (failedList == null) {
                        failedList = new RecordList();
                    }

                    moveTo(pendingList, preparedList);
                    moveTo(heuristicList, preparedList);

                    updateState(LRAStatus.Cancelling);

                    // call commit since the abort route does not save the failed list
                    trace_progress("phase2Commit for nested cancel");
                    super.phase2Commit(true);

                    res = status();

                    updateState(toLRAStatus(status()));
                } else {
                    // forget calls for nested participants
                    if (forgetAllParticipants()) {
                        updateState(LRAStatus.Closed);
                    } else {
                        // some forget calls have not been received, we need to repeat them at the next recovery pass
                        trace_progress("H_HAZARD forget failed");
                        return ActionStatus.H_HAZARD;
                    }
                }
            }
        } else {
            if (cancel || status() == ActionStatus.ABORT_ONLY) {
                // compensators must be called in reverse order so reverse the pending list
                preparedList = new RecordList();
                failedList = new RecordList();
                moveTo(pendingList, preparedList);
                moveTo(heuristicList, preparedList);

                // tell each participant that the LRA canceled
                updateState(LRAStatus.Cancelling);

                // since the phase2Abort route skips prepare we need to make sure the heuristic list exists
                if (heuristicList == null) {
                    heuristicList = new RecordList();
                }

                // call commit since the abort route does not save the failed list
                trace_progress("doEnd with cancel");
                super.phase2Commit(true);
                res = super.status();
            } else {
                // tell each participant that the LRA closed ok
                updateState(LRAStatus.Closing);
                trace_progress("doEnd with close");
                res = super.End(true);
            }
        }

        // note that we can either run the post actions now or wait for a recovery pass
        // doing it here will provide more timely notifications
        runPostLRAActions();

        if (pending != null && pending.size() != 0) {
            if (!nested) {
                pending.clear(); // TODO we will loose this data if we need recovery
            }
        }

        if (getSize(heuristicList) != 0) {
            updateState(cancel ? LRAStatus.Cancelling : LRAStatus.Closing);
        } else if (getSize(failedList) != 0) {
            updateState(cancel ? LRAStatus.FailedToCancel : LRAStatus.FailedToClose);
        } else if (getSize(pendingList) != 0 || getSize(preparedList) != 0) {
            updateState(LRAStatus.Closing);
        } // otherwise status is (cancel ? LRAStatus.Cancelled : LRAStatus.Closed)

        if (isTopLevel()) {
            // note that we don't update the finish time for nested LRAs since their final state depends on the parent
            finishTime = LocalDateTime.now(ZoneOffset.UTC);
        }

        trace_progress("doEnd update finishTime");

        updateState(); // ensure the record is removed if it finished otherwise persisted the state

        if (!isRecovering()) {
            if (lraService != null) {
                lraService.finished(this, false);
            }
        }

        trace_progress("doEnd finished");

        return res;
    }

    protected void runPostLRAActions() {
        // if there are no more heuristics then update the status of the LRA
        if (isInEndState() && heuristicList != null && heuristicList.size() != 0) {
            if (preparedList == null) {
                preparedList = new RecordList();
            }
            moveTo(heuristicList, preparedList);
            checkParticipant(preparedList);

            trace_progress("runPostLRAActions");
            super.phase2Commit(true);
        }
    }

    protected boolean updateState(LRAStatus nextState) {
        if (status != nextState) {
            status = nextState; // we trust that nextState is reachable from the current one

            return (pendingList == null || pendingList.size() == 0) || deactivate();
        }

        return true;
    }

    protected void checkParticipant(RecordList participants) {
        RecordListIterator i = new RecordListIterator(participants);
        AbstractRecord r;

        while ((r = i.iterate()) != null) {
            if (r instanceof LRAParticipantRecord) {
                LRAParticipantRecord rec = (LRAParticipantRecord) r;

                rec.setLraService(getLraService());
                rec.setLRA(this);
            }
        }
    }

    protected void moveTo(RecordList fromList, RecordList toList) {
        AbstractRecord record;

        if (fromList != null) {
            while ((record = fromList.getFront()) != null) {
                toList.putFront(record);
            }
        }
    }

    private boolean allFinished(RecordList... lists) {
        for (RecordList list : lists) {
            if (list != null) {
                RecordListIterator i = new RecordListIterator(list);
                AbstractRecord r;

                while ((r = i.iterate()) != null) {
                    if (r instanceof LRAParticipantRecord) {
                        LRAParticipantRecord rec = (LRAParticipantRecord) r;
                        if (!rec.isFinished()) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    protected boolean isInEndState() {
        // update the status first by checking for heuristics and failed participants
        if (status == LRAStatus.Cancelling && allFinished(heuristicList, failedList)) {
            updateState((failedList == null || failedList.size() == 0) ? LRAStatus.Cancelled : LRAStatus.FailedToCancel);
        } else if (status == LRAStatus.Closing && allFinished(heuristicList, failedList)) {
            updateState((failedList == null || failedList.size() == 0) ? LRAStatus.Closed : LRAStatus.FailedToClose);
        }

        return isFinished();
    }

    private int getSize(RecordList list) {
        return list == null ? 0 : list.size();
    }

    protected LRAStatus toLRAStatus(int atomicActionStatus) {
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

    public LRAParticipantRecord enlistParticipant(URI coordinatorUrl, String participantUrl, String recoveryUrlBase,
                                                  long timeLimit, String compensatorData) throws UnsupportedEncodingException {
        LRAParticipantRecord participant = findLRAParticipant(participantUrl, false);

        if (participant != null) {
            participant.setCompensatorData(compensatorData);
            return participant; // must have already been enlisted
        }

        participant = enlistParticipant(coordinatorUrl, participantUrl, recoveryUrlBase, null,
                timeLimit, compensatorData);

        if (participant != null) {
            // need to remember that there is a new participant
            deactivate(); // if it fails the superclass will have logged a warning
            savedIntentionList = true; // need this clean up if the LRA times out
        }

        return participant;
    }

    private LRAParticipantRecord enlistParticipant(URI coordinatorUrl, String participantUrl, String recoveryUrlBase, String terminateUrl,
                                                   long timeLimit, String compensatorData) {
        LRAParticipantRecord p = new LRAParticipantRecord(this, lraService, participantUrl, compensatorData);
        String pid = p.get_uid().fileStringForm();

        String txId = URLEncoder.encode(coordinatorUrl.toASCIIString(), StandardCharsets.UTF_8);

        p.setRecoveryURI(recoveryUrlBase, txId, pid);

        if (add(p) != AddOutcome.AR_REJECTED) {
            setTimeLimit(timeLimit);

            trace_progress("enlisted " + p.getParticipantPath());

            return p;
        } else if (isRecovering() && p.getCompensator() == null && p.getEndNotificationUri() != null) {
            // the participant is an AfterLRA listener so manually add it to heuristic list
            heuristicList.putRear(p);
            updateState();

            trace_progress("enlisted listener " + p.getParticipantPath());

            return p;
        }

        return null;
    }

    private void updateCompensatorUserData(String compensator, String userData) {
        LRAParticipantRecord participant = findLRAParticipant(compensator, false);

        if (participant != null) {
            participant.setCompensatorData(userData);
        } else {
            LRALogger.i18nLogger.warn_unknownParticipant(compensator);
        }
    }

    public boolean forgetParticipant(String participantUrl) {
        return findLRAParticipant(participantUrl, true) != null;
    }

    public boolean forgetAllParticipants() {
        if (pending == null) {
            return true;
        }

        pending.removeIf(LRAParticipantRecord::forget);

        return pending.isEmpty();
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
            if (r instanceof LRAParticipantRecord) {
                pending.add((LRAParticipantRecord) r);
            }
        }
    }

    private LRAParticipantRecord findLRAParticipant(String participantUrl, boolean remove) {
        LRAParticipantRecord rec;

        try {
            URI recoveryUrl = new URI(LRAParticipantRecord.cannonicalForm(participantUrl));

            rec = findLRAParticipantByRecoveryUrl(recoveryUrl, remove, pendingList, preparedList, heuristicList, failedList);

        } catch (URISyntaxException ignore) {
            String pUrl;
            try {
                pUrl = LRAParticipantRecord.extractCompensator(id, participantUrl);
            } catch (URISyntaxException e) {
                return null;
            }
            rec = findLRAParticipant(pUrl, remove, pendingList, preparedList, heuristicList, failedList);
        }

        return rec;
    }

    private LRAParticipantRecord findLRAParticipant(String participantUrl, boolean remove, RecordList...lists) {
        for (RecordList list : lists) {
            if (list != null) {
                RecordListIterator i = new RecordListIterator(list);
                AbstractRecord r;

                if (participantUrl.indexOf(',') != -1) {
                    try {
                        participantUrl = LRAParticipantRecord.extractCompensator(id, participantUrl);
                    } catch (URISyntaxException e) {
                        continue;
                    }
                }

                while ((r = i.iterate()) != null) {
                    if (r instanceof LRAParticipantRecord) {
                        LRAParticipantRecord rr = (LRAParticipantRecord) r;
                        // can't use == because this may be a recovery scenario
                        if (participantUrl.equals(rr.getCompensator())) {
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

    private LRAParticipantRecord findLRAParticipantByRecoveryUrl(URI recoveryUrl, boolean remove, RecordList...lists) {
        for (RecordList list : lists) {
            if (list != null) {
                RecordListIterator i = new RecordListIterator(list);
                AbstractRecord r;

                while ((r = i.iterate()) != null) {
                    if (r instanceof LRAParticipantRecord) {
                        LRAParticipantRecord rr = (LRAParticipantRecord) r;
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
        if (status() != ActionStatus.CREATED) {
            trace_progress("begin: already running");
            return status(); // cannot begin an action twice
        }

        trace_progress("begin");

        int res = super.Begin(null);

        if (parentId != null) {
            // We want LRAs to be TopLevelActions (TLA).
            // Normally if we were doing transaction to thread association we would pop the parent,
            // however LRA does not use thread association (the context is passed explicitly,
            // the reason for this is that services do not have access to ArjunaCore and use
            // JAX-RS to propagate the context
            LongRunningAction localParent = lraService.lookupTransaction(parentId);

            if (localParent != null) {
                // this parent is in-VM
                par = new LRAParentAbstractRecord(localParent, this); // the new LRA we want parent to know about

                if (localParent.add(par) != AddOutcome.AR_ADDED) {
                    return ActionStatus.INVALID;
                }

                LRAChildAbstractRecord childAR = new LRAChildAbstractRecord(par);

                if (add(childAR) == AddOutcome.AR_REJECTED) {
                    return ActionStatus.INVALID;
                }
            }
        }

        startTime = LocalDateTime.now(ZoneOffset.UTC);

        setTimeLimit(timeLimit);

        trace_progress("begin, deactivating");

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

            trace_progress("scheduleCancellation: wrong state");
            return Response.Status.PRECONDITION_FAILED.getStatusCode();
        }

        if (finishTime != null) {
            // check whether the new time limit is less than the current one
            LocalDateTime ft = LocalDateTime.now(ZoneOffset.UTC).plusNanos(timeLimit * 1000000);

            if (ft.isAfter(finishTime)) {
                // the existing timer finishes before the requested one so there is nothing to do
                return Response.Status.OK.getStatusCode();
            }

            // it is earlier so cancel the current timer
            finishTime = ft;

            if (scheduledAbort != null) {
                trace_progress("scheduleCancellation: earlier than previous timer");
                scheduledAbort.cancel(false);
            }
        } else {
            // if timeLimit is negative the abort will be scheduled immediately
            finishTime = LocalDateTime.now(ZoneOffset.UTC).plusNanos(timeLimit * 1000000);
        }

        trace_progress("scheduleCancelation update finishTime");

        try {
            scheduledAbort = scheduler.schedule(runnable, timeLimit, TimeUnit.MILLISECONDS);
            trace_progress("scheduleCancellation accepted");
        } catch (RejectedExecutionException executionException) {
            // This exception does not need to be handled as the periodic recovery
            // will eventually discover that this LRA is eligible for cancellation.
            updateState(LRAStatus.Cancelling);
            trace_progress("scheduleCancellation rejected");
            LRALogger.logger.warnf(
                    "The LRA transaction with ID %s has not correctly scheduled the task to cancel itself." +
                            "A recovery cycle will eventually cancel this LRA.\n" +
                            "Exception message: %s",
                    this.getId(),
                    executionException.getMessage());
        }

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
                        LRALogger.logger.debugf("LongRunningAction.abortLRA cancelling LRA `%s", id);
                    }

                    updateState(LRAStatus.Cancelling);
                    trace_progress("scheduledAbort fired");
                    finishLRA(true);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private void getRecoveryCoordinatorUrls(Map<String, String> participants, RecordList list) {
        if (list != null) {
            RecordListIterator iter = new RecordListIterator(list);
            AbstractRecord rec;

            while (((rec = iter.iterate()) != null)) {
                if (rec instanceof LRAParticipantRecord) { //rec.typeIs() == LRARecord.getTypeId()) {
                    LRAParticipantRecord lraRecord = (LRAParticipantRecord) rec;

                    participants.put(lraRecord.getRecoveryURI().toASCIIString(), lraRecord.getParticipantPath());
                }
            }
        }
    }

    public void getRecoveryCoordinatorUrls(Map<String, String> participants) {
        getRecoveryCoordinatorUrls(participants, pendingList);
        getRecoveryCoordinatorUrls(participants, preparedList);
    }

    public void updateRecoveryURI(String compensatorUri, String recoveryUri) {
        LRAParticipantRecord lraRecord = findLRAParticipant(compensatorUri, false);

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

    public URI getParentId() {
        return parentId;
    }

    private boolean hasElements(RecordList list) {
        return list != null && list.size() != 0;
    }

    /**
     * Checks whether the LRA has finished and whether or not all of the post LRA actions are complete
     * @return true if all post LRA actions are complete
     */
    public boolean hasPendingActions() {
        // if it is not in an end state or has a heuristic hazzard
        return !isFinished() || hasElements(heuristicList);
    }

    void forget() {
        if (heuristicList != null && heuristicList.size() != 0) {
            RecordListIterator i = new RecordListIterator(heuristicList);
            AbstractRecord r;

            while ((r = i.iterate()) != null) {
                if (r instanceof LRAParticipantRecord) {
                    LRAParticipantRecord rec = (LRAParticipantRecord) r;

                    rec.forget();
                }
            }
        }

        trace_progress("forget okay");
    }

    private void trace_progress(String reason) {
        if (LRALogger.logger.isTraceEnabled()) {
            LRALogger.logger.tracef("%s: LRA id: %s (%s) parent: %s reason: %s state: %s created: %s ttl: %s",
                    LocalDateTime.now(ZoneOffset.UTC), // use the same time function as used for LRA timeouts
                    id,
                    clientId,
                    parentId == null ? "" : parentId,
                    reason,
                    status,
                    startTime,
                    finishTime);
        }
    }
}
