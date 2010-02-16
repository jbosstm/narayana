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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: UserTwoPhaseTx.java,v 1.2 2005/03/10 15:37:17 nmcl Exp $
 */

package com.arjuna.mwlabs.wstx.model.as.twophase;

import com.arjuna.mw.wstx.logging.wstxLogger;

import com.arjuna.mw.wstx.UserTransaction;

import com.arjuna.mw.wstx.transaction.*;
import com.arjuna.mw.wstx.status.*;

import com.arjuna.mw.wstx.common.TxId;

import com.arjuna.mwlabs.wstx.transaction.*;

import com.arjuna.mwlabs.wstx.common.arjunacore.TxIdImple;

import com.arjuna.mw.wsas.status.*;

import com.arjuna.mw.wsas.activity.*;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;
import com.arjuna.mw.wsas.completionstatus.Success;
import com.arjuna.mw.wsas.completionstatus.Failure;
import com.arjuna.mw.wsas.completionstatus.FailureOnly;

import com.arjuna.mw.wscf.UserCoordinator;
import com.arjuna.mw.wscf.UserCoordinatorFactory;

import com.arjuna.mw.wscf.common.Qualifier;
import com.arjuna.mw.wscf.common.CoordinatorId;

import com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.CoordinatorIdImple;

import com.arjuna.mw.wscf.model.twophase.common.TwoPhaseResult;
import com.arjuna.mw.wscf.model.twophase.status.*;
import com.arjuna.mw.wscf.model.twophase.outcomes.*;

import com.arjuna.mw.wscf.model.as.coordinator.twophase.outcomes.*;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.InvalidTimeoutException;
import com.arjuna.mw.wsas.exceptions.InvalidActivityException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;
import com.arjuna.mw.wsas.exceptions.NoActivityException;
import com.arjuna.mw.wsas.exceptions.NoPermissionException;
import com.arjuna.mw.wsas.exceptions.ActiveChildException;

import com.arjuna.mw.wscf.exceptions.ProtocolNotRegisteredException;

import com.arjuna.mw.wstx.exceptions.InvalidTransactionException;
import com.arjuna.mw.wstx.exceptions.NoTransactionException;
import com.arjuna.mw.wstx.exceptions.HeuristicHazardException;
import com.arjuna.mw.wstx.exceptions.HeuristicMixedException;
import com.arjuna.mw.wstx.exceptions.HeuristicCommitException;
import com.arjuna.mw.wstx.exceptions.HeuristicRollbackException;
import com.arjuna.mw.wstx.exceptions.TransactionRolledBackException;
import com.arjuna.mw.wstx.exceptions.TransactionCommittedException;

/**
 * The user portion of the transaction API.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: UserTwoPhaseTx.java,v 1.2 2005/03/10 15:37:17 nmcl Exp $
 * @since 1.0.
 *
 * @message com.arjuna.mwlabs.wstx.model.as.twophase.UserTwoPhaseTx_2 [com.arjuna.mwlabs.wstx.model.as.twophase.UserTwoPhaseTx_2] - Transaction has active children.
 * @message com.arjuna.mwlabs.wstx.model.as.twophase.UserTwoPhaseTx_3 [com.arjuna.mwlabs.wstx.model.as.twophase.UserTwoPhaseTx_3] - Unknown end result!
 */

public class UserTwoPhaseTx implements UserTransaction
{

    public UserTwoPhaseTx ()
    {
	try
	{
	    /*
	     * TODO: currently relies on the fact that the default
	     * coordination protocol is two-phase. Needs to be explicit.
	     */

	    _theCoordinator = UserCoordinatorFactory.userCoordinator();
	}
	catch (SystemException ex)
	{
	    // TODO

	    ex.printStackTrace();
	    
	    _theCoordinator = null;
	}
	catch (ProtocolNotRegisteredException ex)
	{
	    ex.printStackTrace();
	    
	    _theCoordinator = null;
	}
    }
    
    /**
     * Start a new transaction. If there is already a transaction associated
     * with the thread then it will be interposed (nesting is to come!)
     *
     * @exception WrongStateException Thrown if the any currently associated
     * transaction is in a state that does not allow a new transaction to be
     * enlisted.
     * @exception SystemException Thrown in any other situation.
     */

    public void begin () throws WrongStateException, SystemException
    {
	_theCoordinator.start();
    }

    /**
     * Commit the transaction. All participants that are still enlisted
     * with the transaction are committed.
     *
     * @exception InvalidTransactionException Thrown if the current transaction is not
     * known about by the transaction system.
     * @exception WrongStateException Thrown if the current transaction is not in a
     * state that allows commit to be called.
     * @exception HeuristicHazardException Thrown if the participants generated a hazard
     * heuristic.
     * @exception HeuristicMixedException Thrown if the participants generated a mixed heuristic.
     * @exception NoTransactionException Thrown if there is no transaction
     * associated with the invoking thread.
     * @exception SystemException Thrown if some other error occurred.
     */

    public void commit () throws InvalidTransactionException, WrongStateException, HeuristicHazardException, HeuristicMixedException, NoTransactionException, TransactionRolledBackException, SystemException
    {
	try
	{
	    Outcome result = _theCoordinator.end(Success.instance());
	 
	    parseOutcome(result, true);
	}
	catch (ActiveChildException ex)
	{
	    throw new InvalidTransactionException(wstxLogger.arjLoggerI18N.getString("com.arjuna.mwlabs.wstx.model.as.twophase.UserTwoPhaseTx_2"));
	}
	catch (TransactionCommittedException ex)
	{
	}
	catch (InvalidActivityException ex)
	{
	    throw new InvalidTransactionException();
	}
	catch (WrongStateException ex)
	{
	    throw ex;
	}
	catch (ProtocolViolationException ex)
	{
	    throw new HeuristicHazardException(ex.toString());
	}
	catch (NoActivityException ex)
	{
	    throw new NoTransactionException();
	}
	catch (NoPermissionException ex)
	{
	    throw new InvalidTransactionException(ex.toString());
	}
	catch (HeuristicHazardException ex)
	{
	    throw ex;
	}
    }

    /**
     * Abort the transaction. All participants that are still enlisted
     * with the transaction are rolled back.
     *
     * @exception InvalidTransactionException Thrown if the current transaction is not
     * known about by the transaction system.
     * @exception WrongStateException Thrown if the current transaction is not in a
     * state that allows cancel to be called.
     * @exception HeuristicHazardException Thrown if the participants generated a hazard
     * heuristic.
     * @exception HeuristicMixedException Thrown if the participants generated a mixed heuristic.
     * @exception NoTransactionException Thrown if there is no transaction
     * associated with the invoking thread.
     * @exception SystemException Thrown if some other error occurred.
     */

    public void rollback () throws InvalidTransactionException, WrongStateException, HeuristicHazardException, HeuristicMixedException, NoTransactionException, TransactionCommittedException, SystemException
    {
	try
	{
	    Outcome result = _theCoordinator.end(Failure.instance());

	    parseOutcome(result, false);
	}
	catch (ActiveChildException ex)
	{
	    throw new InvalidTransactionException(wstxLogger.arjLoggerI18N.getString("com.arjuna.mwlabs.wstx.model.as.twophase.UserTwoPhaseTx_2"));
	}
	catch (TransactionRolledBackException ex)
	{
	}
	catch (InvalidActivityException ex)
	{
	    throw new InvalidTransactionException();
	}
	catch (WrongStateException ex)
	{
	    throw ex;
	}
	catch (ProtocolViolationException ex)
	{
	    throw new HeuristicHazardException(ex.toString());
	}
	catch (NoActivityException ex)
	{
	    throw new NoTransactionException();
	}
	catch (NoPermissionException ex)
	{
	    throw new InvalidTransactionException(ex.toString());
	}
	catch (HeuristicHazardException ex)
	{
	    throw ex;
	}
    }

    /**
     * @exception SystemException Thrown if any error occurs.
     *
     * @return the status of the current transaction. If there is no
     * transaction associated with the thread then NO_TRANSACTION
     * will be returned.
     */

    public Status status () throws SystemException
    {
	Status s = _theCoordinator.status();
	
	if (s instanceof Cancelling)
	    s = RollingBack.instance();
	else
	{
	    if (s instanceof CancelOnly)
		s = RollbackOnly.instance();
	    else
	    {
		if (s instanceof Confirming)
		    s = Committing.instance();
		else
		{
		    if (s instanceof Confirmed)
			s = Committed.instance();
		    else
		    {
			if (s instanceof Cancelled)
			    s = RolledBack.instance();
			else
			{
			    if (s instanceof NoActivity)
				s = NoTransaction.instance();
			}
		    }
		}
	    }
	}

	return s;
    }

    /**
     * What is the name of the current transaction? Use only for
     * debugging purposes.
     *
     * @exception NoTransactionException Thrown if there is no transaction
     * associated with the invoking thread.
     *
     * @return the name of the transaction.
     */

    public String transactionName () throws NoTransactionException, SystemException
    {
	try
	{
	    return _theCoordinator.activityName();
	}
	catch (NoActivityException ex)
	{
	    throw new NoTransactionException();
	}
    }
    
    /**
     * Suspend all transactions associated with this thread. The thread then
     * becomes associated with no transaction (atom or cohesion).
     *
     * @exception SystemException if any error occurs.
     *
     * @return a representation of the context associated with the thread,
     * or null if there is no context.
     */

    public TransactionHierarchy suspend () throws SystemException
    {
	ActivityHierarchy hier = _theCoordinator.suspend();
	
	if (hier != null)
	    return new TransactionHierarchyImple(hier);
	else
	    return null;
    }
    
    /**
     * Associate this thread with the specified context. Any current
     * associations are lost.
     *
     * @param TransactionHierarchy tx The context representation to associate (may be
     * null).
     *
     * @exception InvalidTransactionException Thrown if the context is invalid.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void resume (TransactionHierarchy tx) throws InvalidTransactionException, SystemException
    {
	ActivityHierarchy hier;

	if (tx == null)
	    hier = null;
	else
	{
	    if (tx instanceof TransactionHierarchyImple)
	    {
		hier = ((TransactionHierarchyImple) tx).activityHierarchy();
	    }
	    else
		throw new InvalidTransactionException();
	}

	try
	{
	    _theCoordinator.resume(hier);
	}
	catch (InvalidActivityException ex)
	{
	    throw new InvalidTransactionException();
	}
    }

    /**
     * @return a representation of the context currently associated with
     * the invoking thread, or null if there is none.
     */

    public TransactionHierarchy currentTransaction () throws SystemException
    {
	ActivityHierarchy hier = _theCoordinator.currentActivity();
	
	if (hier != null)
	    return new TransactionHierarchyImple(hier);
	else
	    return null;
    }

    /**
     * @exception NoTransactionException Thrown if there is no transaction
     * associated with the current thread.
     * @exception SystemException Thrown if any other error occurs.
     *
     * @return the qualifiers that are currently associated with the
     * transaction, or null if there are none.
     */

    public Qualifier[] qualifiers () throws NoTransactionException, SystemException
    {
	try
	{
	    return _theCoordinator.qualifiers();
	}
	catch (NoActivityException ex)
	{
	    throw new NoTransactionException();
	}
    }

    /**
     * Set the state of the transaction such that the only possible outcome is
     * for it to rollback.
     *
     * @exception NoTransactionException Thrown if there is no transaction
     * associated with the current thread.
     * @exception WrongStateException Thrown if the state of the transaction is
     * such that it is not possible to put it into a rollback-only state, e.g.,
     * it is committing.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void setRollbackOnly () throws NoTransactionException, WrongStateException, SystemException
    {
	try
	{
	    _theCoordinator.setCompletionStatus(FailureOnly.instance());
	}
	catch (NoActivityException ex)
	{
	    throw new NoTransactionException();
	}
    }

    /**
     * Get the timeout value currently associated with transactions.
     *
     * @exception SystemException Thrown if any error occurs.
     *
     * @return the timeout value in seconds, or 0 if no application specified
     * timeout has been provided.
     */

    public int getTimeout () throws SystemException
    {
	// TODO

	return 0;
    }

    /**
     * Set the timeout to be associated with all subsequently created
     * activities. A default value of 0 is automatically associated with
     * each thread and this means that no application specified timeout is
     * set for activities.
     *
     * @param int timeout The timeout (in seconds) to associate with all
     * subsequently created activities. This value must be 0 or greater.
     *
     * @exception InvalidTimeoutException Thrown if the timeout value provided
     * is negative, too large, or if timeouts are simply not supported by
     * the activity implementation.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void setTimeout (int timeout) throws InvalidTimeoutException, SystemException
    {
	// TODO
    }

    /**
     * @exception NoTransactionException Thrown if there is no activity
     * associated with the invoking thread.
     * @exception SystemException Thrown if some other error occurred.
     *
     * @return the unique transaction id for the current transaction. This
     * may or may not be the same as the activity id.
     *
     * @message com.arjuna.mwlabs.wstx.model.as.twophase.UserTwoPhaseTx_1 [com.arjuna.mwlabs.wstx.model.as.twophase.UserTwoPhaseTx_1] - Unknown coordinator identifier type {0}
     */

    public TxId identifier () throws NoTransactionException, SystemException
    {
	try
	{
	    CoordinatorId coordId = _theCoordinator.identifier();

	    if (coordId instanceof CoordinatorIdImple)
		return new TxIdImple((CoordinatorIdImple) coordId);
	    else
	    {
		wstxLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wstx.model.as.twophase.UserTwoPhaseTx_1",
					      new Object[]{coordId});
	    
		return null;
	    }
	}
	catch (NoActivityException ex)
	{
	    throw new NoTransactionException();
	}
    }
    
    private final void parseOutcome (Outcome out, boolean commit) throws InvalidTransactionException, WrongStateException, HeuristicHazardException, HeuristicMixedException, TransactionRolledBackException, TransactionCommittedException
    {
	try
	{
	    if ((out.completedStatus().equals(Failure.instance()) ||
		 out.completedStatus().equals(FailureOnly.instance())) && commit)
	    {
		throw new TransactionRolledBackException();
	    }
	}
	catch (SystemException ex)
	{
	    throw new HeuristicHazardException();
	}
	
	if (out instanceof CoordinationOutcome)
	{
	    int res = ((CoordinationOutcome) out).result();

	    switch (res)
	    {
	    case TwoPhaseResult.CANCELLED:
	    case TwoPhaseResult.HEURISTIC_CANCEL:
		{
		    if (commit)
			throw new TransactionRolledBackException();
		}
		break;
	    case TwoPhaseResult.CONFIRMED:
	    case TwoPhaseResult.HEURISTIC_CONFIRM:
		{
		    if (!commit)
			throw new TransactionCommittedException();
		}
		break;
	    case TwoPhaseResult.HEURISTIC_MIXED:
		throw new HeuristicMixedException();
	    case TwoPhaseResult.HEURISTIC_HAZARD:
		throw new HeuristicHazardException();
	    case TwoPhaseResult.FINISH_OK:
		break;
	    case TwoPhaseResult.FINISH_ERROR:
	    default:
		throw new HeuristicHazardException();
	    }
	}
	else
	    throw new HeuristicHazardException(wstxLogger.arjLoggerI18N.getString("com.arjuna.mwlabs.wstx.model.as.twophase.UserTwoPhaseTx_3"));
    }
    
    private UserCoordinator _theCoordinator;
    
}
