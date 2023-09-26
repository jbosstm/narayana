/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate;

import jakarta.transaction.Status;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;

/**
 * A subordinate JTA transaction; used when importing another transaction
 * context.
 *
 * @author mcl
 */

public class SubordinateAtomicAction extends
		com.arjuna.ats.internal.jta.transaction.arjunacore.AtomicAction
{

	public SubordinateAtomicAction ()
	{
		this(AtomicAction.NO_TIMEOUT);
		subordinate = true;
	}

	public SubordinateAtomicAction (int timeout)
	{
		super();
		subordinate = true;

		start();

		_timeout = timeout;

		// if it has a non-negative timeout, add it to the reaper.

		if (timeout > AtomicAction.NO_TIMEOUT)
			TransactionReaper.transactionReaper().insert(this, timeout);
	}

	/**
	 * Commit the transaction, and have heuristic reporting. Heuristic reporting
	 * via the return code is enabled.
	 *
	 * @return <code>ActionStatus</code> indicating outcome.
	 */

	public int commit ()
	{
		return commit(true);
	}

	/**
	 * Commit the transaction. The report_heuristics parameter can be used to
	 * determine whether or not heuristic outcomes are reported.
	 *
	 * If the transaction has already terminated, or has not begun, then an
	 * appropriate error code will be returned.
	 *
	 * @return <code>ActionStatus</code> indicating outcome.
	 */

	public int commit (boolean report_heuristics)
	{
		return ActionStatus.INVALID;
	}

	/**
	 * Abort (rollback) the transaction.
	 *
	 * If the transaction has already terminated, or has not been begun, then an
	 * appropriate error code will be returned.
	 *
	 * @return <code>ActionStatus</code> indicating outcome.
	 */

	public int abort ()
	{
		return ActionStatus.INVALID;
	}

	/**
	 * The type of the class is used to locate the state of the transaction log
	 * in the object store.
	 *
	 * Overloads BasicAction.type()
	 *
	 * @return a string representation of the hierarchy of the class for storing
	 *         logs in the transaction object store.
	 */

	public String type ()
	{
		return "/StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction/SubordinateAtomicAction";
	}

	public int doPrepare ()
	{
        int status = super.status();
        
        // JBTM-927 it is possible this transaction has been aborted by the TransactionReaper
        if (status == ActionStatus.ABORTED) {
        	return TwoPhaseOutcome.PREPARE_NOTOK;
        }

        // In JTA spec, beforeCompletions are run on commit attempts only, not rollbacks.
        // We attempt to mimic that here, even though we are outside the scope of the spec.
        // note it's not perfect- async timeout/rollback means there is a race condition in which we
        // can still call beforeCompletion on rollbacks, but that's not too bad as skipping it is really
        // just an optimization anyhow.  JBTM-429
        if ( !(status == ActionStatus.ABORT_ONLY || status == ActionStatus.ABORTING) && doBeforeCompletion())
        {
            int outcome = super.prepare(true);
            if(outcome == TwoPhaseOutcome.PREPARE_READONLY) {
                // we won't get called again, so we need to clean up
                // and run the afterCompletions before returning.
                doCommit();
            }

            return outcome;
        }
		else
		{
			super.phase2Abort(true);
			super.afterCompletion(Status.STATUS_ROLLEDBACK);

			return TwoPhaseOutcome.PREPARE_NOTOK;
		}
	}

	public int doCommit ()
	{
		super.phase2Commit(true);

		int toReturn;

		switch (super.getHeuristicDecision())
		{
		case TwoPhaseOutcome.PREPARE_OK:
		case TwoPhaseOutcome.FINISH_OK:
		    if (super.failedList != null && super.failedList.size() > 0) {
		        return ActionStatus.COMMITTING;
		    }
			toReturn = super.status();
			break;
		case TwoPhaseOutcome.HEURISTIC_ROLLBACK:
			toReturn = ActionStatus.H_ROLLBACK;
			break;
		case TwoPhaseOutcome.HEURISTIC_COMMIT:
			toReturn = ActionStatus.H_COMMIT;
			break;
		case TwoPhaseOutcome.HEURISTIC_MIXED:
			toReturn = ActionStatus.H_MIXED;
			break;
		case TwoPhaseOutcome.HEURISTIC_HAZARD:
		default:
			toReturn = ActionStatus.H_HAZARD;
			break;
		}

		super.afterCompletion(toReturn);

		TransactionReaper.transactionReaper().remove(this);

		return toReturn;
	}

	public int doRollback ()
	{
		super.phase2Abort(true);

		int toReturn;

		switch (super.getHeuristicDecision())
		{
		case TwoPhaseOutcome.PREPARE_OK:
		case TwoPhaseOutcome.FINISH_OK:
			toReturn = super.status();
			break;
		case TwoPhaseOutcome.HEURISTIC_ROLLBACK:
			toReturn = ActionStatus.H_ROLLBACK;
			break;
		case TwoPhaseOutcome.HEURISTIC_COMMIT:
			toReturn = ActionStatus.H_COMMIT;
			break;
		case TwoPhaseOutcome.HEURISTIC_MIXED:
			toReturn = ActionStatus.H_MIXED;
			break;
		case TwoPhaseOutcome.HEURISTIC_HAZARD:
		default:
			toReturn = ActionStatus.H_HAZARD;
			break;
		}

		super.afterCompletion(toReturn);

		TransactionReaper.transactionReaper().remove(this);

		return toReturn;
	}

	public int doOnePhaseCommit ()
	{
	    int status = super.status();
	    
	    // In JTA spec, beforeCompletions are run on commit attempts only, not rollbacks.
	    // We attempt to mimic that here, even though we are outside the scope of the spec.
	    // note it's not perfect- async timeout/rollback means there is a race condition in which we
	    // can still call beforeCompletion on rollbacks, but that's not too bad as skipping it is really
	    // just an optimization anyhow. JBTM-429

	    if (status == ActionStatus.ABORT_ONLY || doBeforeCompletion())
	    {
	        status = super.End(true);

	        switch (super.getHeuristicDecision())
	        {
	        // commit was processed correctly but some of the resources failed to finish
	        // for 2PC this means no trouble as in case of a crash at this time
	        // there is recovery which works with prepared transaction
	        // for 1PC where there is nothing prepared and nothing stored at object store
	        // we need to inform parent about error by setting up heuristic state
	        case TwoPhaseOutcome.PREPARE_OK:
	        case TwoPhaseOutcome.FINISH_OK:
	            if (super.failedList != null && super.failedList.size() > 0) {
	                status = ActionStatus.H_COMMIT;
	            }
	        default:
	            break;
	        }
	    }
	    else
	    {
	        status = ActionStatus.ABORTED;
	    }

	    afterCompletion(status);

		TransactionReaper.transactionReaper().remove(this);
	    
	    return status;
	}

	/**
	 * @deprecated Only called via tests
	 */
	public void doForget ()
	{
		super.forgetHeuristics();
	}

	public boolean doBeforeCompletion ()
	{
	    // should not need synchronizing at this level
	    
	    if (!_doneBefore)
	    {
	        _beforeOutcome = super.beforeCompletion();
	        
	        _doneBefore = true;
	    }
	    
	    return _beforeOutcome;
	}
	
	/**
	 * For crash recovery purposes.
	 *
	 * @param actId the identifier to recover.
	 */

	protected SubordinateAtomicAction (Uid actId)
	{
		super(actId);
	}

	/**
	 * By default the BasicAction class only allows the termination of a
	 * transaction if it's the one currently associated with the thread. We
	 * override this here.
	 *
	 * @return <code>false</code> to indicate that this transaction can only
	 *         be terminated by the right thread.
	 */

	protected boolean checkForCurrent ()
	{
		return false;
	}

    public boolean activated ()
    {
    	return true;
    }
    
    /*
     * We have these here because it's possible that synchronizations aren't
     * called explicitly either side of commit/rollback due to JCA API not supporting
     * them directly. We do though and in which case it's possible that they
     * can be driven through two routes and we don't want to get into a mess
     * due to that.
     */
    
    private boolean _doneBefore = false;
    private boolean _beforeOutcome = false;
}