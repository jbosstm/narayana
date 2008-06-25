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
 * $Id: TwoPhaseTxManager.java,v 1.2 2003/03/04 12:59:29 nmcl Exp $
 */

package com.arjuna.mwlabs.wstx11.model.as.twophase;

import com.arjuna.mw.wscf.model.as.CoordinatorManager;
import com.arjuna.mw.wscf11.model.as.CoordinatorManagerFactory;

import com.arjuna.mw.wscf.model.as.coordinator.twophase.common.*;

import com.arjuna.mw.wstx.resource.Participant;
import com.arjuna.mw.wstx.resource.Synchronization;

import com.arjuna.mw.wstx.TransactionManager;

import com.arjuna.mwlabs.wstx.model.as.twophase.resource.ParticipantAction;
import com.arjuna.mwlabs.wstx.model.as.twophase.resource.SynchronizationAction;

import com.arjuna.mw.wstx.exceptions.DuplicateParticipantException;
import com.arjuna.mw.wstx.exceptions.NoTransactionException;
import com.arjuna.mw.wstx.exceptions.InvalidTransactionException;
import com.arjuna.mw.wstx.exceptions.InvalidParticipantException;
import com.arjuna.mw.wstx.exceptions.DuplicateSynchronizationException;
import com.arjuna.mw.wstx.exceptions.InvalidSynchronizationException;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.NoActivityException;

/**
 * This is the service side component of the user interface. Since services
 * or some entity acting on their behalf (e.g., an interceptor) must enlist
 * participants with the transaction (atom or cohesion), this interface
 * primarily concentrates on those methods. However, a service may well
 * not be the final destination for a transaction, i.e., in order to perform
 * its work, the service may have to make other remote invocations on other
 * services and propagate the context, suspend it, etc. As such, all of the
 * UserTransaction methods are also available to the service.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TwoPhaseTxManager.java,v 1.2 2003/03/04 12:59:29 nmcl Exp $
 * @since 1.0.
 */

public class TwoPhaseTxManager implements TransactionManager
{

    public TwoPhaseTxManager()
    {
	try
	{
	    _coordinatorManager = CoordinatorManagerFactory.coordinatorManager();
	}
	catch (Exception ex)
	{
	    // TODO

	    ex.printStackTrace();

	    _coordinatorManager = null;
	}
    }

    /**
     * Enrol the specified participant in the current transaction.
     * It is illegal to call this method when no transaction is associated
     * with the thread.
     *
     * @param Participant participant The participant to enrol.
     * @exception com.arjuna.mw.wsas.exceptions.WrongStateException Thrown if the transaction is not in a state
     * whereby participants can be enrolled.
     * @exception com.arjuna.mw.wstx.exceptions.DuplicateParticipantException Thrown if the participant identifier
     * has already been associated with a participant.
     * @exception com.arjuna.mw.wstx.exceptions.NoTransactionException Thrown if there is no transaction
     * associated with the invoking thread.
     * @exception com.arjuna.mw.wstx.exceptions.InvalidTransactionException Thrown if the transaction associated with
     * the thread is invalid.
     * @exception com.arjuna.mw.wstx.exceptions.InvalidParticipantException Thrown if the participant reference
     * is invalid.
     * @exception com.arjuna.mw.wsas.exceptions.SystemException Thrown if any other error occurs.
     */

    public void enlist (Participant participant) throws WrongStateException, DuplicateParticipantException, NoTransactionException, InvalidTransactionException, InvalidParticipantException, SystemException
    {
	try
	{
	    _coordinatorManager.addParticipant(new ParticipantAction(participant), Priorities.PARTICIPANT, null);
	}
	catch (com.arjuna.mw.wscf.exceptions.DuplicateParticipantException ex)
	{
	    throw new DuplicateParticipantException();
	}
	catch (NoActivityException ex)
	{
	    throw new NoTransactionException();
	}
	catch (com.arjuna.mw.wscf.exceptions.InvalidParticipantException ex)
	{
	    throw new InvalidParticipantException();
	}
    }

    /**
     * Cause the specified participant to resign from the transaction.
     * CAUTION: use with *extreme* care since if invoked at the wrong time
     * it could lead to data corruption or state modifications that are
     * no longer under the control of the transaction. You *must* ensure
     * that no state changes have been made that should be controlled by
     * the specified participant.
     *
     * @param Participant participant The participant to resign.
     * @exception com.arjuna.mw.wstx.exceptions.InvalidTransactionException Thrown if the transaction associated with
     * the thread is invalid.
     * @exception com.arjuna.mw.wstx.exceptions.NoTransactionException Thrown if no transaction is associated
     * with the invoking thread.
     * @exception com.arjuna.mw.wstx.exceptions.InvalidParticipantException Thrown if the transaction does not know
     * about the specified participant or the parameter is invalid.
     * @exception com.arjuna.mw.wsas.exceptions.WrongStateException Thrown if the transaction is not in a state
     * that allows participants to resign.
     * @exception com.arjuna.mw.wsas.exceptions.SystemException Thrown if any other error occurs.
     */

    public void delist (Participant participant) throws InvalidTransactionException, NoTransactionException, InvalidParticipantException, WrongStateException, SystemException
    {
	// TODO: support it!

	throw new WrongStateException();
    }

    /**
     * Enlist a synchronization with the current transaction. Synchronizations
     * do not receive the two-phase commit messages but instead are invoked
     * prior to its start and after it has completed.
     *
     * @param Synchronization participant The synchronization to enroll.
     *
     * @exception com.arjuna.mw.wsas.exceptions.WrongStateException Thrown if the transaction state is such
     * that synchronizations cannot be enrolled.
     * @exception com.arjuna.mw.wstx.exceptions.NoTransactionException Thrown if there is no transaction
     * associated with the invoking thread.
     * @exception com.arjuna.mw.wstx.exceptions.InvalidTransactionException Thrown if the transaction is not
     * top-level.
     * @exception com.arjuna.mw.wstx.exceptions.InvalidSynchronizationException Thrown if the synchronization
     * reference is invalid.
     * @exception com.arjuna.mw.wstx.exceptions.DuplicateSynchronizationException Thrown if the synchronization
     * has already been registered with the transaction.
     * @exception com.arjuna.mw.wsas.exceptions.SystemException Thrown if any other error occurs.
     */

    public void addSynchronization (Synchronization participant) throws WrongStateException, NoTransactionException, InvalidTransactionException, InvalidSynchronizationException, DuplicateSynchronizationException, SystemException
    {
	try
	{
	    _coordinatorManager.addParticipant(new SynchronizationAction(participant), Priorities.SYNCHRONIZATION, null);
	}
	catch (com.arjuna.mw.wscf.exceptions.DuplicateParticipantException ex)
	{
	    throw new DuplicateSynchronizationException();
	}
	catch (NoActivityException ex)
	{
	    throw new NoTransactionException();
	}
	catch (com.arjuna.mw.wscf.exceptions.InvalidParticipantException ex)
	{
	    throw new InvalidSynchronizationException();
	}
    }

    /**
     * Remove the specified synchronization participant from the transaction.
     *
     * @param Synchronization participant The participant to remove.
     *
     * @exception com.arjuna.mw.wstx.exceptions.InvalidTransactionException Thrown if the transaction is not
     * top-level.
     * @exception com.arjuna.mw.wstx.exceptions.NoTransactionException Thrown if there is no transaction
     * associated with the current thread.
     * @exception com.arjuna.mw.wstx.exceptions.InvalidSynchronizationException Thrown if the transaction
     * does not know about the specified synchronization.
     * @exception com.arjuna.mw.wsas.exceptions.WrongStateException Thrown if the state of the transaction is
     * such that the synchronization cannot be removed.
     * @exception com.arjuna.mw.wsas.exceptions.SystemException Thrown if any other error occurs.
     */

    public void removeSynchronization (Synchronization participant) throws SystemException, InvalidTransactionException, NoTransactionException, InvalidSynchronizationException, WrongStateException
    {
	throw new WrongStateException();
    }

    private CoordinatorManager _coordinatorManager;

}