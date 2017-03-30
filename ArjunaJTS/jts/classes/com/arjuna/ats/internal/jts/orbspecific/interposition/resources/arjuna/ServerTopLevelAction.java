/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Hewlett Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ServerTopLevelAction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna;

import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.UNKNOWN;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicRollback;
import org.omg.CosTransactions.Inactive;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.RecoveryCoordinator;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.Vote;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.interposition.resources.arjuna.Interposition;
import com.arjuna.ats.internal.jts.interposition.resources.arjuna.ServerResource;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;
import com.arjuna.ats.jts.exceptions.ExceptionCodes;
import com.arjuna.ats.jts.logging.jtsLogger;

/**
 * This looks like an atomic action, but is not actually derived from
 * BasicAction or Transaction. This is because of the way in which the
 * OTS creates and manipulates transactions.
 *
 * As with Transaction, we only create actions here, and do not associated
 * these contexts with any thread. We do the association later.
 *
 * If we were to do the creation via a suitably modified current interface
 * then the thread association would be done for us automatically, and we
 * would not have to call resume at all.
 *
 * This is a top-level action proxy.
 */

/*
 * NOTE: all calls to pushAction in this and related classes are to do
 * with AIT abstract records. Some of them rely on being able to call
 * BasicAction.Current to determine what the current transaction is and
 * if we do not do thread-to-transaction association here then this all
 * breaks. There is no other reason for this. So, what we need to do
 * is remove this dependency by fixing the AIT records. This dependency
 * has a knock-on effect on certain optimisations we can make (e.g.,
 * local interpositon - see ServerControl.)
 */

public class ServerTopLevelAction extends ServerResource implements org.omg.CosTransactions.ResourceOperations
{

    protected boolean _registered;

    public ServerTopLevelAction (ServerControl control)
    {
	super(control);

	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("ServerTopLevelAction::ServerTopLevelAction ( " + _theUid + " )");
    }

	_theResource = null;
	_resourceRef = getReference();

	/*
	 * Now attempt to do interposition registration.
	 */

	if (_resourceRef != null)
	{
	    /*
	     * Would like to be able to attach a thread filter
	     * to this object if process-filters aren't supported.
	     * However, currently this won't work as we can't have
	     * two different filter types working at the same
	     * time.
	     *
	     *		ATTACH_THREAD_FILTER_(_theResource);
	     */

	    Coordinator realCoordinator = _theControl.originalCoordinator();

	    if (!(_valid = registerResource(realCoordinator))) {

            /*
            * Failed to register. Valid is set, and the interposition
            * controller will now deal with this.
            */
        }

	    realCoordinator = null;
	}
	else
	    _valid = false;
        _registered = false;
    }

public Resource getReference ()
    {
	if ((_resourceRef == null) && _valid)
	{
	    if (_theControl != null)
	    {
		_theResource = new org.omg.CosTransactions.ResourcePOATie(this);

		ORBManager.getPOA().objectIsReady(_theResource);

		_resourceRef = org.omg.CosTransactions.ResourceHelper.narrow(ORBManager.getPOA().corbaReference(_theResource));
	    }
	    else
		_valid = false;
	}

	return _resourceRef;
    }

/*
 * Will only be called by the remote top-level transaction.
 */

public org.omg.CosTransactions.Vote prepare () throws HeuristicMixed, HeuristicHazard, SystemException
    {
	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("ServerTopLevelAction::prepare for " + _theUid);
    }

	if (_theControl == null)
	{
	    throw new INVALID_TRANSACTION(ExceptionCodes.SERVERAA_NO_CONTROL, CompletionStatus.COMPLETED_NO);
	}

	if (_theControl.isWrapper())
	{
	    destroyResource();  // won't necessarily get another invocation!
	    return Vote.VoteReadOnly;
	}

	ServerTransaction theTransaction = (ServerTransaction) _theControl.getImplHandle();

	//ThreadActionData.pushAction(theTransaction); // LockManager needs to know if there is a transaction

	int result = TwoPhaseOutcome.PREPARE_NOTOK;

	/*
	 * Transaction may have locally timed out and been rolled back.
	 */

	int s = theTransaction.status();

	if ((s == ActionStatus.RUNNING) || (s == ActionStatus.ABORT_ONLY))
	    result = theTransaction.doPrepare();
	else
	{
	    switch (s)
	    {
	    case ActionStatus.COMMITTING:
	    case ActionStatus.COMMITTED:
	    case ActionStatus.H_COMMIT:
		result = TwoPhaseOutcome.PREPARE_OK;
		break;
	    case ActionStatus.H_MIXED:
		result = TwoPhaseOutcome.HEURISTIC_MIXED;
		break;
	    case ActionStatus.H_HAZARD:
		result = TwoPhaseOutcome.HEURISTIC_HAZARD;
		break;
	    }
	}

	ThreadActionData.popAction();

	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("ServerTopLevelAction::prepare for " + _theUid + " : " + TwoPhaseOutcome.stringForm(result));
    }

	/*
	 * If prepare failed, then rollback now.
	 */

	if (result == TwoPhaseOutcome.PREPARE_NOTOK)
	{
	    try
	    {
		rollback();
	    }
	    catch (HeuristicCommit ex1)
	    {
		result = TwoPhaseOutcome.HEURISTIC_COMMIT;
	    }
	    catch (HeuristicMixed ex2)
	    {
		result = TwoPhaseOutcome.HEURISTIC_MIXED;
	    }
	    catch (HeuristicHazard ex3)
	    {
		result = TwoPhaseOutcome.HEURISTIC_HAZARD;
	    }
	    catch (SystemException ex4)
	    {
		result = TwoPhaseOutcome.HEURISTIC_HAZARD;
	    }
	}

	switch (result)
	{
	case TwoPhaseOutcome.INVALID_TRANSACTION:
	    throw new INVALID_TRANSACTION(ExceptionCodes.INVALID_ACTION, CompletionStatus.COMPLETED_NO);
	case TwoPhaseOutcome.PREPARE_OK:
	    return Vote.VoteCommit;
	case TwoPhaseOutcome.PREPARE_NOTOK:
	    destroyResource();  // won't necessarily get another invocation!

	    return Vote.VoteRollback;
	case TwoPhaseOutcome.PREPARE_READONLY:
	    destroyResource();  // won't necessarily get another invocation!

	    // what is we subsequently rollback?

	    return Vote.VoteReadOnly;
	case TwoPhaseOutcome.HEURISTIC_MIXED:
	    if (TxControl.getMaintainHeuristics())
		destroyResource();
	    throw new HeuristicMixed();  // will eventually get forget
	case TwoPhaseOutcome.HEURISTIC_HAZARD:
	default:
	    if (TxControl.getMaintainHeuristics())
		destroyResource();
	    throw new HeuristicHazard();
	}
    }

public void rollback () throws HeuristicCommit, HeuristicMixed, HeuristicHazard, SystemException
    {
	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("ServerTopLevelAction::rollback for " + _theUid);
    }

	if (_theControl == null)
	{
	    throw new INVALID_TRANSACTION(ExceptionCodes.SERVERAA_NO_CONTROL, CompletionStatus.COMPLETED_NO);
	}

	if (_theControl.isWrapper())
	{
	    destroyResource();
	    return;
	}

	ServerTransaction theTransaction = (ServerTransaction) _theControl.getImplHandle();

	ThreadActionData.pushAction(theTransaction); // LockManager needs to know if there is a transaction

	int actionStatus = theTransaction.status();

	if (actionStatus == ActionStatus.PREPARED)
	{
	    /*
	     * This will also call any after_completions on
	     * registered synchronizations.
	     */

	    actionStatus = theTransaction.doPhase2Abort();
	}
	else
	{
	    if ((actionStatus == ActionStatus.RUNNING) ||
                (actionStatus == ActionStatus.ABORT_ONLY))
	    {
		try
		{
		    /*
		     * Have to do this because of the way PI works
		     * with thread-context association.
		     */

		    if (!valid())
			theTransaction.doPhase2Abort();  // must rollback
		    else
			theTransaction.rollback();

		    actionStatus = ActionStatus.ABORTED;
		}
		catch (SystemException ex)
		{
		    actionStatus = ActionStatus.ABORTED;

		    throw ex;
		}
		finally
		{
		    destroyResource();
		}
	    }
	}

	ThreadActionData.popAction();

	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("ServerTopLevelAction::rollback for " + _theUid + " : " + ActionStatus.stringForm(actionStatus));
    }

	switch (actionStatus)
	{
	case ActionStatus.PREPARED:
	    throw new INVALID_TRANSACTION(ExceptionCodes.INVALID_ACTION, CompletionStatus.COMPLETED_NO);
	case ActionStatus.ABORTED:
	case ActionStatus.H_ROLLBACK:
	    destroyResource();
	    break;
	case ActionStatus.COMMITTED:
	case ActionStatus.H_COMMIT:
	    destroyResource();
	    throw new HeuristicCommit();
	case ActionStatus.H_MIXED:
	    if (TxControl.getMaintainHeuristics())
		destroyResource();
	    throw new HeuristicMixed();
	case ActionStatus.H_HAZARD:
	    if (TxControl.getMaintainHeuristics())
		destroyResource();
	    throw new HeuristicHazard();
	default:
	    destroyResource();
	    break;
	}
    }

public void commit () throws NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard, SystemException
    {
	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("ServerTopLevelAction::commit for " + _theUid);
    }

	if (_theControl == null)
	{
	    throw new INVALID_TRANSACTION(ExceptionCodes.SERVERAA_NO_CONTROL, CompletionStatus.COMPLETED_NO);
	}

	if (_theControl.isWrapper())
	{
	    destroyResource();
	    return;
	}

	ServerTransaction theTransaction = (ServerTransaction) _theControl.getImplHandle();

	ThreadActionData.pushAction(theTransaction); // LockManager needs to know if there is a transaction

	int actionStatus = theTransaction.status();
	boolean notPrepared = false;

	if (actionStatus == ActionStatus.PREPARED)
	{
	    /*
	     * This will also call any after_completions on
	     * registered synchronizations.
	     */

	    actionStatus = theTransaction.doPhase2Commit();
	}
	else
	{
	    if (actionStatus == ActionStatus.RUNNING)
	    {
		if (jtsLogger.logger.isTraceEnabled()) {
            jtsLogger.logger.trace("ServerTopLevelAction::commit for " + _theUid + " : NotPrepared");
        }

		notPrepared = true;
	    }
	}

	ThreadActionData.popAction();

	if (notPrepared)
	    throw new NotPrepared();

	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("ServerTopLevelAction::commit for " + _theUid + " : " + ActionStatus.stringForm(actionStatus));
    }

	switch (actionStatus)
	{
	case ActionStatus.PREPARED:
	    throw new INVALID_TRANSACTION(ExceptionCodes.SERVERAA_NO_CONTROL, CompletionStatus.COMPLETED_NO);
	case ActionStatus.COMMITTED:
	case ActionStatus.H_COMMIT:
	    destroyResource();
	    break;
	case ActionStatus.ABORTED:
	case ActionStatus.H_ROLLBACK:
	    if (TxControl.getMaintainHeuristics())
		destroyResource();
	    throw new HeuristicRollback();
	case ActionStatus.H_MIXED:
	    if (TxControl.getMaintainHeuristics())
		destroyResource();
	    throw new HeuristicMixed();
	case ActionStatus.H_HAZARD:
	    if (TxControl.getMaintainHeuristics())
		destroyResource();
	    throw new HeuristicHazard();
	default:
	    destroyResource();
	    break;
	}
    }

public void forget () throws SystemException
    {
	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("ServerTopLevelAction::forget for " + _theUid);
    }

	boolean forgot = true;

	if (_theControl != null)
	    forgot = _theControl.forgetHeuristics();

	destroyResource();  // causes the removal of the transaction state.

	if (!forgot)
	    throw new BAD_OPERATION();
    }

public void commit_one_phase () throws HeuristicHazard, SystemException
    {
	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("ServerTopLevelAction::commit_one_phase for " + _theUid);
    }

	if (_theControl == null)
	{
	    throw new INVALID_TRANSACTION(ExceptionCodes.SERVERAA_NO_CONTROL, CompletionStatus.COMPLETED_NO);
	}

	if (_theControl.isWrapper())
	{
	    destroyResource();
	    return;
	}

	ServerTransaction theTransaction = (ServerTransaction) _theControl.getImplHandle();

	if (theTransaction == null) {
        jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_notx("ServerTopLevelAction.commit_one_phase");

        throw new INVALID_TRANSACTION(ExceptionCodes.NO_TRANSACTION, CompletionStatus.COMPLETED_NO);
    }

	ThreadActionData.pushAction(theTransaction); // LockManager needs to know if there is a transaction

	try
	{
	    /*
	     * This will commit and do any before/after_completion calls
	     * on registered synchronizations.
	     */

	    theTransaction.doCommit(true);
	}
	catch (HeuristicHazard e1)
	{
	    /*
	     * Is a heuristic, then don't remove the
	     * transaction information.
	     */

	    ThreadActionData.popAction();

	    throw e1;
	}
	catch (TRANSACTION_ROLLEDBACK e4)
	{
	    ThreadActionData.popAction();

	    throw e4;
	}
	catch (INVALID_TRANSACTION e5)
	{
	    ThreadActionData.popAction();

	    throw e5;
	}
	catch (SystemException e6)
	{
	    ThreadActionData.popAction();

	    throw e6;
	}
	catch (Exception e7)
	{
	    ThreadActionData.popAction();

	    throw new UNKNOWN(e7.toString());
	}
	finally
	{
	    destroyResource();
	}

	ThreadActionData.popAction();

	destroyResource();
    }

public String type ()
    {
	return "/Resources/Arjuna/ServerTopLevelAction";
    }

protected ServerTopLevelAction ()
    {
	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("ServerTopLevelAction::ServerTopLevelAction ()");
    }

	_theResource = null;
	_resourceRef = null;
    }

protected synchronized void destroyResource ()
    {
	if (!_destroyed)
	{
	    try
	    {
		if (Interposition.destroy(get_uid()))
		    _destroyed = true;
	    }
	    catch (Exception e) {
            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_generror("ServerTopLevelAction.destroyResource", e);
        }

	    try
	    {
		if (_theResource != null)
		{
		    ORBManager.getPOA().shutdownObject(_theResource);
		    _theResource = null;
		}
	    }
	    catch (Exception e) {
            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_generror("ServerTopLevelAction.destroyResource", e);
        }
	}

	tidyup();
    }

protected boolean registerResource (Coordinator theCoordinator)
    {
	boolean result = false;

	if (theCoordinator != null)
	{
	    if (_registered)
		return true;

	    try
	    {
		/*
		 * Register resource and pass RecoveryCoordinator reference
		 * to the interposed transaction to save and restore.
		 */

		RecoveryCoordinator recoveryCoord = theCoordinator.register_resource(_resourceRef);

            _registered = true;

		if (!_theControl.isWrapper())
		{
		    ServerTransaction tx = (ServerTransaction) _theControl.getImplHandle();

		    if (tx != null)
		    {
			tx.setRecoveryCoordinator(recoveryCoord);

			result = true;
		    }
		    else {
			result = false;
            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_ipfailed("ServerTopLevelAction");
		    }
		}
		else
		    result = true;
	    }
	    catch (ClassCastException classCastException) {
            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_generror("ServerTopLevelAction.registerResource", classCastException);
        }
	    catch (Inactive ine) {
            jtsLogger.i18NLogger.warn_server_top_level_action_inactive();
            jtsLogger.i18NLogger.debug_orbspecific_interposition_resources_arjuna_generror("ServerTopLevelAction.registerResource", ine);
            transactionInactive = true;
        }
	    catch (TRANSACTION_ROLLEDBACK ex1) {
            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_generror("ServerTopLevelAction.registerResource", ex1);
        }
	    catch (INVALID_TRANSACTION ex2) {
            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_generror("ServerTopLevelAction.registerResource", ex2);
        }
	    catch (Exception e) {
            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_generror("ServerTopLevelAction.registerResource", e);
        }
	}
	else {
        jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_nocoord("ServerTopLevelAction.registerResource");
    }

	return result;
    }

public boolean isTransactionInactive() {
    return transactionInactive;
}

protected org.omg.CosTransactions.ResourcePOATie _theResource;
protected Resource                               _resourceRef;
private boolean transactionInactive;

}
