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

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.state.InputObjectState;

import org.jboss.jbossts.star.util.LinkHolder;
import org.jboss.jbossts.star.provider.HttpResponseException;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.logging.Logger;

import java.net.HttpURLConnection;

/**
 * Log record for driving participants through 2PC and recoverery
 */
public class RESTRecord extends AbstractRecord
{
    protected final static Logger log = Logger.getLogger(RESTRecord.class);
    private String terminateUrl;
    private String participantUrl;

    private String coordinatorUrl;

    private String coordinatorID;
    private String status;
    private String txId;
    private boolean prepared;
    private String recoveryUrl;

    public RESTRecord() {}

    public RESTRecord(String coordinatorUrl, String participantUrl, String terminateUrl, String txId)
    {
        super(new Uid());

        if (log.isTraceEnabled())
            log.trace("RESTRecord(" + coordinatorUrl + ", " + participantUrl + ", " + terminateUrl + ", " + txId + ')');

        this.participantUrl = participantUrl;
        this.terminateUrl = terminateUrl;
        this.coordinatorUrl = coordinatorUrl;
        this.txId = txId;

        coordinatorID = get_uid().fileStringForm();
        status = "";
        recoveryUrl = "";
    }

    String getParticipant()
    {
        return terminateUrl;
    }

    public int typeIs()
    {
        return RecordType.USER_DEF_FIRST0;
    }

    public Object value()
    {
        return status;
    }

    public void setValue(Object o)
    {
    }

    public int nestedAbort()
    {
        return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedCommit()
    {
        return TwoPhaseOutcome.FINISH_OK;
    }

    /*
    * Not sub-transaction aware.
    */
    public int nestedPrepare()
    {
        return TwoPhaseOutcome.PREPARE_OK; // do nothing
    }

	private void check_suspend(Fault f)
	{
        if (fault.equals(f))
		{
            try
            {
            	log.info(f + ": for 10 seconds");
                Thread.sleep(10000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
	}

	private void check_halt(Fault f)
	{
        if (fault.equals(f))
		{
            log.info(f + ": halt VM");
            Runtime.getRuntime().halt(1);
		}
	}

    private int statusToOutcome()
    {
        return statusToOutcome(status);
    }

    private int statusToOutcome(String status)
    {
        if (TxSupport.H_COMMIT.equals(status))
            return TwoPhaseOutcome.HEURISTIC_COMMIT;
        else if (TxSupport.H_ROLLBACK.equals(status))
            return TwoPhaseOutcome.HEURISTIC_ROLLBACK;
        else if (TxSupport.H_MIXED.equals(status))
            return TwoPhaseOutcome.HEURISTIC_MIXED;
        else if (TxSupport.H_HAZARD.equals(status))
            return TwoPhaseOutcome.HEURISTIC_HAZARD;
        else if (TxSupport.ABORT_ONLY.equals(status))
            return TwoPhaseOutcome.FINISH_OK;
        else if (TxSupport.ABORTED.equals(status))
            return TwoPhaseOutcome.FINISH_OK;
        else if (TxSupport.COMMITTED.equals(status))
            return TwoPhaseOutcome.FINISH_OK;
        else if (TxSupport.COMMITTED_ONE_PHASE.equals(status))
            return TwoPhaseOutcome.FINISH_OK;
        else if (TxSupport.PREPARED.equals(status))
            return TwoPhaseOutcome.PREPARE_OK;
        else if (TxSupport.READONLY.equals(status))
            return TwoPhaseOutcome.PREPARE_READONLY;

/*
        else if (TxSupport.PREPARING.equals(status))
            return TwoPhaseOutcome.PREPARE_NOTOK;
        else if (TxSupport.RUNNING.equals(status))
            return TwoPhaseOutcome.FINISH_ERROR;
        else if (TxSupport.UNKNOWN.equals(status))
            return TwoPhaseOutcome.FINISH_ERROR;
        else if (TxSupport.ABORTING.equals(status))
            return TwoPhaseOutcome.FINISH_ERROR;
        else if (TxSupport.COMMITTING.equals(status))
            return TwoPhaseOutcome.FINISH_ERROR;
*/
        else
            return TwoPhaseOutcome.FINISH_ERROR;
    }

    public boolean forgetHeuristic() {
        if (log.isTraceEnabled())
            log.trace("forgetting heuristic for " + terminateUrl);

        try {
            new TxSupport().httpRequest(new int[] {HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NO_CONTENT},
                    this.participantUrl, "DELETE", null, null, null);
            status = "";
        } catch (HttpResponseException e) {
            return false;
        }

        return super.forgetHeuristic();
    }

    public int topLevelPrepare()
    {
        if (log.isTraceEnabled())
            log.trace("prepare " + terminateUrl);

        check_halt(Fault.prepare_halt);
        check_suspend(Fault.prepare_suspend);

        if (fault.equals(Fault.h_hazard))
            return TwoPhaseOutcome.HEURISTIC_HAZARD;

        if (terminateUrl == null || txId == null)
            return TwoPhaseOutcome.PREPARE_READONLY;

        try
        {
            status = TxSupport.getStatus(
				new TxSupport().httpRequest(new int[] {HttpURLConnection.HTTP_OK},
					this.terminateUrl, "PUT", TxSupport.STATUS_MEDIA_TYPE,
					TxSupport.toStatusContent(TxSupport.PREPARED), null));

            prepared = true;

            int outcome = statusToOutcome();

            if (outcome != TwoPhaseOutcome.FINISH_ERROR)
                return outcome;
        }
        catch (HttpResponseException e)
        {
            if (checkFinishError(e.getActualResponse(), false)) {
                status = TxSupport.toStatusContent(TxSupport.PREPARED);
                return TwoPhaseOutcome.PREPARE_OK;
            } else {
                status = TxSupport.toStatusContent(TxSupport.ABORTED);
            }
        }

        return TwoPhaseOutcome.PREPARE_NOTOK;
    }

    public int topLevelAbort()
    {
        if (log.isTraceEnabled())
            log.debug("trace " + terminateUrl);

        check_halt(Fault.abort_halt);
        check_suspend(Fault.abort_suspend);

        if (terminateUrl == null || txId == null)
            return TwoPhaseOutcome.FINISH_ERROR;

        try {
            status = TxSupport.getStatus(new TxSupport().httpRequest(new int[] {HttpURLConnection.HTTP_OK}, this.terminateUrl, "PUT", TxSupport.STATUS_MEDIA_TYPE,
                    TxSupport.toStatusContent(TxSupport.ABORTED), null));
        } catch (HttpResponseException e) {

            if (checkFinishError(e.getActualResponse(), false))
                return TwoPhaseOutcome.FINISH_OK;
        }

        return statusToOutcome();
    }

    public int topLevelCommit()
    {
        if (log.isTraceEnabled())
            log.trace("commit " + terminateUrl);

        if (terminateUrl == null || txId == null)
            return TwoPhaseOutcome.PREPARE_READONLY;

        if (!prepared)
            return TwoPhaseOutcome.NOT_PREPARED;

        return doCommit(TxSupport.COMMITTED);
    }

    public int nestedOnePhaseCommit()
    {
        return TwoPhaseOutcome.FINISH_ERROR;
    }

    /**
     * For commit_one_phase we can do whatever we want since the transaction
     * outcome is whatever we want. Therefore, we do not need to save any
     * additional recoverable state, such as a reference to the transaction
     * coordinator, since it will not have an intentions list anyway.
     */
    public int topLevelOnePhaseCommit()
    {
		return doCommit(TxSupport.COMMITTED_ONE_PHASE);
    }

    private int doCommit(String nextState)
    {
		TxSupport txs = new TxSupport();

        check_halt(Fault.commit_halt);
        check_suspend(Fault.commit_suspend);

        if (txId == null)
            return TwoPhaseOutcome.FINISH_ERROR;

        try
        {
            if (log.isTraceEnabled())
                log.trace("committing " + this.terminateUrl);
  
            if (!TxSupport.isReadOnly(status)) {
                txs = new TxSupport();
				String body = txs.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
					this.terminateUrl, "PUT", TxSupport.STATUS_MEDIA_TYPE,
					TxSupport.toStatusContent(nextState), null);

                status = txs.getStatus(body);

            	if (log.isTraceEnabled())
                	log.trace("commit http status: " + txs.getStatus() + " RTS status: " + status);
            } else {
                status = TxSupport.COMMITTED;
			}

            if (log.isTraceEnabled())
                log.trace("COMMIT OK at terminateUrl: " + this.terminateUrl);
        }
        catch (HttpResponseException e)
        {
            if (log.isDebugEnabled())
                log.debug("commit exception: " + e + " HTTP code: " + e.getActualResponse() +
					" body: " + txs.getBody());

			// should result in the recovery system taking over
			if (e.getActualResponse() == HttpURLConnection.HTTP_UNAVAILABLE) {
                log.trace("Finishing with TwoPhaseOutcome.FINISH_ERROR");
 				return TwoPhaseOutcome.FINISH_ERROR;
			} else {
            	checkFinishError(e.getActualResponse(), true);
				status = txs.getBody();
			}
        }

        return statusToOutcome(status);
    }
    private boolean checkFinishError(int expected, boolean commit) throws HttpResponseException
    {
        if (expected == HttpURLConnection.HTTP_NOT_FOUND)
        {
            // the participant may have moved so check the coordinator terminateUrl
            if (hasParticipantMoved())
            {
                if (log.isDebugEnabled())
                    log.debug("participant has moved commit to new terminateUrl " + this.terminateUrl);

                try
                {
                    TxSupport.getStatus(new TxSupport().httpRequest(new int[] {HttpURLConnection.HTTP_OK}, this.terminateUrl, "PUT", TxSupport.STATUS_MEDIA_TYPE,
                        TxSupport.toStatusContent(commit ? TxSupport.COMMITTED : TxSupport.ABORTED), null));
                    if (log.isDebugEnabled())
                        log.debug("Finish OK at new terminateUrl: " + this.terminateUrl);

                    status = (commit ? TxSupport.COMMITTED : TxSupport.ABORTED);

                    return true;
                }
                catch (HttpResponseException e1)
                {
                    if (log.isTraceEnabled())
                        log.trace("Finish still failing at new URI: " + e1);

                    if (log.isInfoEnabled())
                        log.debug("participant " + this.terminateUrl + " commit error: " + e1.getMessage());
                }
            }
        }

        status = TxSupport.RUNNING;

        return false;
    }

    /**
     * A participant tells the coordinator if it changes its URL.
     * To see if this has happened perform a GET on the recovery terminateUrl which returns the
     * last known location of the participant.
     * @return true if the participant did move
     */
    private boolean hasParticipantMoved()
    {
        try
        {
            if (log.isTraceEnabled())
                log.trace("seeing if participant has moved: " + coordinatorID + " recoveryUrl: " + recoveryUrl);

            if (recoveryUrl.length() == 0)
                    return false;

            // get the latest participant terminateUrl by probing the recovery terminateUrl:
            String content = new TxSupport().httpRequest(new int[] {HttpURLConnection.HTTP_OK}, recoveryUrl, "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);
            String terminator = new LinkHolder(content).get(TxSupport.TERMINATOR_LINK);
            String participant = new LinkHolder(content).get(TxSupport.PARTICIPANT_LINK);

            if (terminator != null && !terminator.equals(this.terminateUrl))
            {
                // participant has moved so remember the new location
                this.participantUrl = participant;
                this.terminateUrl = terminator;

            	if (log.isTraceEnabled())
                	log.trace("... yes it has - new terminateUrl is " + terminator);

                return true;
            }
        }
        catch (HttpResponseException e)
        {
            if (log.isTraceEnabled())
                log.trace("participant has not moved: " + e);
        }

        return false;
    }

    public boolean save_state(OutputObjectState os, int t)
    {
        try
        {
            os.packString(txId);
            os.packBoolean(prepared);
            os.packString(participantUrl);
            os.packString(terminateUrl);
            os.packString(coordinatorUrl);
            os.packString(recoveryUrl);
            os.packString(coordinatorID);
            os.packString(status);

            return super.save_state(os, t);
        }
        catch (Exception e)
        {
            e.printStackTrace();

            return false;
        }
    }

    public boolean restore_state(InputObjectState os, int t)
    {
        try
        {
            txId = os.unpackString();
            prepared = os.unpackBoolean();
            participantUrl = os.unpackString();
            terminateUrl = os.unpackString();
            coordinatorUrl = os.unpackString();
            recoveryUrl = os.unpackString();
            coordinatorID = os.unpackString();
            status = os.unpackString();

            if (log.isInfoEnabled())
                log.info("restore_state " + terminateUrl);

            return super.restore_state(os, t);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public String type()
    {
        return RESTRecord.typeName();
    }

    public static String typeName()
    {
        return "/StateManager/AbstractRecord/RESTRecord";
    }

    public boolean doSave()
    {
        return true;
    }

    public void merge(AbstractRecord a)
    {
    }

    public void alter(AbstractRecord a)
    {
    }

    public boolean shouldAdd(AbstractRecord a)
    {
        return (a.typeIs() == typeIs());
    }

    public boolean shouldAlter(AbstractRecord a)
    {
        return false;
    }

    public boolean shouldMerge(AbstractRecord a)
    {
        return false;
    }

    public boolean shouldReplace(AbstractRecord a)
    {
        return false;
    }

    public void setRecoveryUrl(String recoveryUrl) {
        this.recoveryUrl = recoveryUrl;
    }

    // TODO remove fault injection code - use byteman instead
    enum Fault {
		abort_halt, abort_suspend, prepare_halt,
		prepare_suspend, commit_halt, commit_suspend,
		h_commit, h_rollback, h_hazard, h_mixed, none
	}
    Fault fault = Fault.none;

    public void setFault(String name)
    {
        for (Fault f : Fault.values())
        {
            if (f.name().equals(name))
            {
                log.trace("setFault: " + f + " terminateUrl: " + terminateUrl);

                fault = f;
                return;
            }
        }

        fault = Fault.none;
    }
}
