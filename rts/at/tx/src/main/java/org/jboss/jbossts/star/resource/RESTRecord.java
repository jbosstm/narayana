/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2010
 * @author JBoss Inc.
 */
package org.jboss.jbossts.star.resource;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.jboss.jbossts.star.provider.HttpResponseException;
import org.jboss.jbossts.star.util.TxLinkNames;
import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.logging.Logger;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

/**
 * Log record for driving participants through 2PC and recovery
 */
public class RESTRecord extends AbstractRecord implements Comparable {
    protected static final Logger log = Logger.getLogger(RESTRecord.class);
    private String participantURI;

    // two phase aware participant completion URI
    private String terminateURI;

    // two phase unaware participant completion URIs
    String commitURI;
    String prepareURI;
    String rollbackURI;
    String commitOnePhaseURI;

    private String coordinatorURI;

    private String coordinatorID;

    private TxStatus status;
    private String txId;
    private boolean prepared;
    private String recoveryURI;
    private long age = System.currentTimeMillis();

    public RESTRecord() {
        status = TxStatus.TransactionStatusUnknown;
    }

    public RESTRecord(String txId, String coordinatorURI, String participantURI, String terminateURI) {
        super(new Uid());

        if (log.isTraceEnabled())
            log.tracef("RESTRecord(%s, %s, %s, %s)", coordinatorURI, participantURI, terminateURI, txId);

        this.participantURI = participantURI;
        this.terminateURI = this.prepareURI = this.commitURI = this.rollbackURI = this.commitOnePhaseURI = terminateURI;
        this.coordinatorURI = coordinatorURI;
        this.txId = txId;

        coordinatorID = get_uid().fileStringForm();
        status = TxStatus.TransactionActive;
        recoveryURI = "";
    }

    public RESTRecord(String txId, String coordinatorURI, String participantURI,
                      String commitURI, String prepareURI, String  rollbackURI, String commitOnePhaseURI) {
        this(txId, coordinatorURI, participantURI, null);

        if (log.isTraceEnabled())
            log.tracef("RESTRecord(%s, %s, %s, %s)", commitURI, prepareURI, rollbackURI, commitOnePhaseURI);

        this.commitURI = commitURI;
        this.prepareURI = prepareURI;
        this.rollbackURI = rollbackURI;
        this.commitOnePhaseURI = commitOnePhaseURI;
    }

    public String getCoordinatorURI() {
        return coordinatorURI;
    }

    public String getTxId() {
        return txId;
    }

    public String getRecoveryURI() {
        return recoveryURI;
    }

    protected String getParticipantURI() {
        return participantURI;
    }

    public int typeIs() {
        return RecordType.RESTAT_RECORD;
    }

    public Object value() {
        return status.name();
    }

    public String getStatus() {
        return status.name();
    }

    public long getAge() {
        return age;
    }

    public void setValue(Object o) {
    }

    public int nestedAbort() {
        return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedCommit() {
        return TwoPhaseOutcome.FINISH_OK;
    }

    /*
    * Not sub-transaction aware.
    */
    public int nestedPrepare() {
        return TwoPhaseOutcome.PREPARE_OK; // do nothing
    }

    private void check_suspend(Fault f) {
        if (fault.equals(f))
        {
            try {
                log.infof("%s: for 10 seconds", f);
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void check_halt(Fault f) {
        if (fault.equals(f))
        {
            log.infof("%s: halt VM", f);
            Runtime.getRuntime().halt(1);
        }
    }

    private int statusToOutcome() {
        return statusToOutcome(status);
    }

    private int statusToOutcome(TxStatus status) {
        try {
            if (!status.equals(TxStatus.TransactionStatusUnknown))
                return status.twoPhaseOutcome();
        } catch (IllegalArgumentException e) {
            if (log.isTraceEnabled())
                log.trace("Participant returned unknown status");
        }

        return TwoPhaseOutcome.FINISH_ERROR;
    }

    public boolean forgetHeuristic() {
        if (log.isTraceEnabled())
            log.tracef("forgetting heuristic for %s", participantURI);

        try {
            new TxSupport().httpRequest(new int[] {HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NO_CONTENT},
                    this.participantURI, "DELETE", null);
            status = TxStatus.TransactionStatusUnknown;
        } catch (HttpResponseException e) {
            return false;
        }

        return super.forgetHeuristic();
    }

    public int topLevelPrepare() {
        if (log.isTraceEnabled())
            log.tracef("prepare %s", prepareURI);

        check_halt(Fault.prepare_halt);
        check_suspend(Fault.prepare_suspend);

        if (fault.equals(Fault.h_hazard))
            return TwoPhaseOutcome.HEURISTIC_HAZARD;

        if (prepareURI == null || txId == null)
            return TwoPhaseOutcome.PREPARE_READONLY;

        try {
            String body = new TxSupport().httpRequest(new int[] {HttpURLConnection.HTTP_OK}, this.prepareURI, "PUT",
                    TxMediaType.TX_STATUS_MEDIA_TYPE, TxSupport.toStatusContent(TxStatus.TransactionPrepared.name()));

            if (body.isEmpty()) {
                status = TxStatus.TransactionPrepared;
            } else {
                status = TxStatus.fromStatus(TxSupport.getStatus(body));
            }

            prepared = true;
            int outcome = statusToOutcome();

            if (outcome != TwoPhaseOutcome.FINISH_ERROR)
                return outcome;
        } catch (HttpResponseException e) {
            if (checkFinishError(e.getActualResponse(), TxStatus.TransactionPrepared)) {
                status = TxStatus.TransactionPrepared;
                return TwoPhaseOutcome.PREPARE_OK;
            } else {
                status = TxStatus.TransactionRolledBack;
            }
        }

        return TwoPhaseOutcome.PREPARE_NOTOK;
    }

    public int topLevelAbort() {
        if (log.isTraceEnabled())
            log.debugf("trace %s", rollbackURI);

        check_halt(Fault.abort_halt);
        check_suspend(Fault.abort_suspend);

        if (rollbackURI == null || txId == null)
            return TwoPhaseOutcome.FINISH_ERROR;

        try {
            String body = new TxSupport().httpRequest(new int[] {HttpURLConnection.HTTP_OK}, this.rollbackURI, "PUT",
                    TxMediaType.TX_STATUS_MEDIA_TYPE, TxSupport.toStatusContent(TxStatus.TransactionRolledBack.name()));

            if (body.isEmpty()) {
                status = TxStatus.TransactionRolledBack;
            } else {
                status = TxStatus.fromStatus(TxSupport.getStatus(body));
            }
        } catch (HttpResponseException e) {

            if (checkFinishError(e.getActualResponse(), TxStatus.TransactionRolledBack))
                return TwoPhaseOutcome.FINISH_OK;
        }

        return statusToOutcome();
    }

    public int topLevelCommit() {
        if (log.isTraceEnabled())
            log.tracef("commit %s", commitURI);

        if (commitURI == null || txId == null)
            return TwoPhaseOutcome.PREPARE_READONLY;

        if (!prepared)
            return TwoPhaseOutcome.NOT_PREPARED;

        return doCommit(TxStatus.TransactionCommitted);
    }

    public int nestedOnePhaseCommit() {
        return TwoPhaseOutcome.FINISH_ERROR;
    }

    /**
     * For commit_one_phase we can do whatever we want since the transaction
     * outcome is whatever we want. Therefore, we do not need to save any
     * additional recoverable state, such as a reference to the transaction
     * coordinator, since it will not have an intentions list anyway.
     */
    public int topLevelOnePhaseCommit() {
        return doCommit(TxStatus.TransactionCommittedOnePhase);
    }

    private int doCommit(TxStatus nextState) {
        TxSupport txs = new TxSupport();

        check_halt(Fault.commit_halt);
        check_suspend(Fault.commit_suspend);

        if (txId == null)
            return TwoPhaseOutcome.FINISH_ERROR;

        // if unaware two phase commit participant is enlisted without support of 1PC then on phase uri is null
        String commitUri = (nextState == TxStatus.TransactionCommittedOnePhase && this.commitOnePhaseURI != null)
            ? this.commitOnePhaseURI : this.commitURI;

        try {
            if (log.isTraceEnabled())
                log.tracef("committing %s", commitUri);

            if (!TxStatus.TransactionReadOnly.equals(status)) {
                txs = new TxSupport();
                String body = txs.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, commitUri, "PUT",
                        TxMediaType.TX_STATUS_MEDIA_TYPE, TxSupport.toStatusContent(nextState.name()));

                if (body.isEmpty()) {
                    status = TxStatus.TransactionCommitted;
                } else {
                    status = TxStatus.fromStatus(TxSupport.getStatus(body));
                }

                if (log.isTraceEnabled())
                    log.tracef("commit http status: %s RTS status: %s", txs.getStatus(), status);
            } else {
                status = TxStatus.TransactionCommitted;
            }

            if (log.isTraceEnabled())
                log.tracef("COMMIT OK at commitURI: %s", commitUri);
        } catch (HttpResponseException e) {
            if (log.isDebugEnabled())
                log.debugf(e, "commit exception: HTTP code: %s body: %s", e.getActualResponse(), txs.getBody());

            // should result in the recovery system taking over
            if (e.getActualResponse() == HttpURLConnection.HTTP_UNAVAILABLE) {
                log.trace("Finishing with TwoPhaseOutcome.FINISH_ERROR");
                 return TwoPhaseOutcome.FINISH_ERROR;
            } else {
                checkFinishError(e.getActualResponse(), nextState);
                status = TxStatus.fromStatus(txs.getBody());
            }
        }

        return statusToOutcome(status);
    }

    private boolean checkFinishError(int expected, TxStatus nextState) throws HttpResponseException {
        if (expected == HttpURLConnection.HTTP_NOT_FOUND)
        {
            // the participant may have moved so check the coordinator participantURI
            if (hasParticipantMoved())
            {
                if (log.isDebugEnabled())
                    log.debugf("participant has moved commit to new participantURI %s", this.participantURI);

                String uri;

                if (nextState.isCommit()) {
                    uri = commitURI;
                } else if (nextState.isAbort()) {
                    uri = rollbackURI;
                } else if (nextState.isPrepare()) {
                    uri = prepareURI;
                } else if (nextState.isCommitOnePhase()) {
                    uri = commitOnePhaseURI;
                } else {
                    status = TxStatus.TransactionActive;

                    return false;
                }

                try {
                    TxSupport.getStatus(new TxSupport().httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                            uri, "PUT", TxMediaType.TX_STATUS_MEDIA_TYPE,
                            TxSupport.toStatusContent(nextState.name())));
                    if (log.isDebugEnabled())
                        log.debug("Finish OK at new participantURI: %s" + this.participantURI);

                    status = nextState;

                    return true;
                } catch (HttpResponseException e1) {
                    if (log.isTraceEnabled())
                        log.tracef(e1, "Finish still failing at new URI: ");

                    if (log.isInfoEnabled())
                        log.debugf("participant %s commit error: %s", this.participantURI, e1.getMessage());
                }
            }
        }

        status = TxStatus.TransactionActive;

        return false;
    }

    /**
     * A participant tells the coordinator if it changes its URL.
     * To see if this has happened perform a GET on the recovery participantURI which returns the
     * last known location of the participant.
     * @return true if the participant did move
     */
    private boolean hasParticipantMoved() {
        try {
            if (log.isTraceEnabled())
                log.tracef("seeing if participant has moved: %s  recoveryURI: %s", coordinatorID, recoveryURI);

            if (recoveryURI.length() == 0)
                    return false;

            // get the latest participant terminateURI (or URIs in the case of a Two Phase Unaware participant)
            // by probing the recovery URI:
            Map<String, String> links = new HashMap<String, String>();

            new TxSupport().httpRequest(new int[] {HttpURLConnection.HTTP_OK}, recoveryURI, "GET",
                    TxMediaType.PLAIN_MEDIA_TYPE, null, links);

            String terminateURI = links.get(TxLinkNames.PARTICIPANT_TERMINATOR);

            if (links.containsKey(TxLinkNames.PARTICIPANT_TERMINATOR)) {
                // participant has moved so remember the new location
                this.participantURI = links.get(TxLinkNames.PARTICIPANT_RESOURCE);
            }

            if (terminateURI == null) {
                // see if it is two phase unaware
                String commitURI = links.get(TxLinkNames.PARTICIPANT_COMMIT);
                String prepareURI = links.get(TxLinkNames.PARTICIPANT_PREPARE);
                String rollbackURI = links.get(TxLinkNames.PARTICIPANT_ROLLBACK);
                String commitOnePhaseURI = links.get(TxLinkNames.PARTICIPANT_COMMIT_ONE_PHASE);

                if (commitURI != null)
                    this.commitURI = commitURI;

                if (prepareURI != null)
                    this.prepareURI = prepareURI;

                if (rollbackURI != null)
                    this.rollbackURI = rollbackURI;

                if (commitOnePhaseURI != null)
                    this.commitOnePhaseURI = commitOnePhaseURI;

                if (log.isTraceEnabled())
                    log.tracef("... yes it has - new terminate URIs (commit, prepare, rollback and commit one phase)" +
                            " are %s %s %s %s",
                            commitURI != null ?  commitURI : "",
                            prepareURI != null ?  prepareURI : "",
                            rollbackURI != null ?  rollbackURI : "",
                            commitOnePhaseURI != null ?  commitOnePhaseURI : "");

                if (this.commitURI != null && this.prepareURI != null && this.rollbackURI != null)
                    return true;
            } else {
                // terminator has moved so remember the new location
                this.terminateURI = this.prepareURI = this.commitURI = this.rollbackURI = this.commitOnePhaseURI = terminateURI;

                if (log.isTraceEnabled())
                    log.tracef("... yes it has - new terminateURI is %s", terminateURI);

                return true;
            }
        } catch (HttpResponseException e) {
            if (log.isTraceEnabled())
                log.tracef(e, "participant has not moved: %s", e.getMessage());
        }

        return false;
    }

    public boolean save_state(OutputObjectState os, int t) {
        try {
            os.packString(txId);
            os.packBoolean(prepared);
            os.packString(participantURI);
            os.packString(coordinatorURI);
            os.packString(recoveryURI);
            os.packString(coordinatorID);
            os.packString(status.name());

            os.packString(terminateURI);
            os.packString(commitURI);
            os.packString(prepareURI);
            os.packString(rollbackURI);
            os.packString(commitOnePhaseURI);

            return super.save_state(os, t);
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
    }

    public boolean restore_state(InputObjectState os, int t) {
        try {
            txId = os.unpackString();
            prepared = os.unpackBoolean();
            participantURI = os.unpackString();
            coordinatorURI = os.unpackString();
            recoveryURI = os.unpackString();
            coordinatorID = os.unpackString();
            status = TxStatus.fromStatus(os.unpackString());

            terminateURI = os.unpackString();
            commitURI = os.unpackString();
            prepareURI = os.unpackString();
            rollbackURI = os.unpackString();
            commitOnePhaseURI = os.unpackString();

            if (commitURI == null) {
                prepareURI = commitURI = rollbackURI = commitOnePhaseURI = terminateURI;
            }

            if (log.isInfoEnabled())
                log.infof("restore_state %s", terminateURI);

            return super.restore_state(os, t);
        } catch (Exception e) {
            return false;
        }
    }

    public String type() {
        return RESTRecord.typeName();
    }

    public static String typeName() {
        return "/StateManager/AbstractRecord/RESTRecord";
    }

    public boolean doSave() {
        return true;
    }

    public void merge(AbstractRecord a) {
    }

    public void alter(AbstractRecord a) {
    }

    public boolean shouldAdd(AbstractRecord a) {
        return (a.typeIs() == typeIs());
    }

    public boolean shouldAlter(AbstractRecord a) {
        return false;
    }

    public boolean shouldMerge(AbstractRecord a) {
        return false;
    }

    public boolean shouldReplace(AbstractRecord a) {
        return false;
    }

    public void setRecoveryURI(String recoveryURI) {
        this.recoveryURI = recoveryURI;
    }

    // TODO remove fault injection code - use byteman instead
    enum Fault {
        abort_halt, abort_suspend, prepare_halt,
        prepare_suspend, commit_halt, commit_suspend,
        h_commit, h_rollback, h_hazard, h_mixed, none
    }
    Fault fault = Fault.none;

    public void setFault(String name) {
        for (Fault f : Fault.values())
        {
            if (f.name().equals(name))
            {
                log.tracef("setFault: %s participantURI: %s", f, participantURI);

                fault = f;
                return;
            }
        }

        fault = Fault.none;
    }

    @Override
    public int compareTo(Object o) {
        AbstractRecord other = (AbstractRecord) o;

        if (lessThan(other))
            return -1;

        if (greaterThan(other))
            return 1;

        return 0;
    }

    public String httpRequest(int[] expect, String url, String method, String mediaType, String content,
                              Map<String, String> linkHeaders, Map<String, String> reqHeaders) {
        return new TxSupport().httpRequest(expect, url, method, mediaType, content, linkHeaders, reqHeaders);
    }
}
