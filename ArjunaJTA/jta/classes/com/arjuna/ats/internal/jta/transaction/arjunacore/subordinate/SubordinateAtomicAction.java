/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2003,
 *
 * Hewlett-Packard Arjuna Labs, Newcastle upon Tyne, Tyne and Wear, UK.
 *
 * $Id: SubordinateAtomicAction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate;

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
		super();

		start();
	}

	public SubordinateAtomicAction (int timeout)
	{
		super();

		start();

		// if it has a non-negative timeout, add it to the reaper.

		if (timeout > AtomicAction.NO_TIMEOUT)
			TransactionReaper.transactionReaper(true).insert(this, timeout);
	}

	/**
	 * Commit the transaction, and have heuristic reporting. Heuristic reporting
	 * via the return code is enabled.
	 *
	 * @return <code>ActionStatus</code> indicating outcome.
	 */

	public int commit ()
	{
		return ActionStatus.INVALID;
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
	    }
	    else
	    {
	        status = ActionStatus.ABORTED;
	    }

	    afterCompletion(status);
	    
	    return status;
	}

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
	
	public boolean doAfterCompletion (int status)
	{
	    if (!_doneAfter)
	    {
	        /*
	         * We don't need to convert from JTA status to AC status because
	         * the interpretation of the status is left up to the
	         * Synchronization instance and not the transaction.
	         * 
	         * TODO this is a potential problem in the case where we
	         * allow mixtures of Synchronization types in the same transaction
	         * and they expect status values that conflict.
	         */
	        
	        _afterOutcome = super.afterCompletion(status);
	        
	        _doneAfter = true;
	    }
	    
	    return _afterOutcome;
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
    private boolean _doneAfter = false;
    private boolean _afterOutcome = false;
}
