/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
 * $Id: TransactionManager.java,v 1.1 2002/11/25 11:00:51 nmcl Exp $
 */

package com.arjuna.mw.wstx;

import com.arjuna.mw.wstx.resource.Participant;
import com.arjuna.mw.wstx.resource.Synchronization;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;

import com.arjuna.mw.wstx.exceptions.DuplicateParticipantException;
import com.arjuna.mw.wstx.exceptions.NoTransactionException;
import com.arjuna.mw.wstx.exceptions.InvalidTransactionException;
import com.arjuna.mw.wstx.exceptions.InvalidParticipantException;
import com.arjuna.mw.wstx.exceptions.DuplicateSynchronizationException;
import com.arjuna.mw.wstx.exceptions.InvalidSynchronizationException;

/**
 * This is the service side component of the user interface. Since services
 * or some entity acting on their behalf (e.g., an interceptor) must enlist
 * participants with the transaction, this interface
 * primarily concentrates on those methods. However, a service may well
 * not be the final destination for a transaction, i.e., in order to perform
 * its work, the service may have to make other remote invocations on other
 * services and propagate the context, suspend it, etc. As such, all of the
 * UserTransaction methods are also available to the service.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TransactionManager.java,v 1.1 2002/11/25 11:00:51 nmcl Exp $
 * @since 1.0.
 */

public interface TransactionManager
{
    /**
     * Enrol the specified participant in the current transaction such that
     * it will be invoked during the two-phase commit protocol.
     * It is illegal to call this method when no transaction is associated
     * with the thread.
     *
     * @param participant The participant to enrol.
     *
     * @exception WrongStateException Thrown if the transaction is not in a state
     * whereby participants can be enrolled.
     * @exception DuplicateParticipantException Thrown if the participant
     * has already been associated with the transaction.
     * @exception NoTransactionException Thrown if there is no transaction
     * associated with the invoking thread.
     * @exception InvalidTransactionException Thrown if the transaction associated with
     * the thread is invalid.
     * @exception InvalidParticipantException Thrown if the participant reference
     * is invalid.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void enlist (Participant participant) throws WrongStateException, DuplicateParticipantException, NoTransactionException, InvalidTransactionException, InvalidParticipantException, SystemException;
    
    /**
     * Cause the specified participant to resign from the transaction.
     *
     * CAUTION: use with *extreme* care since if invoked at the wrong time
     * it could lead to data corruption or state modifications that are
     * no longer under the control of the transaction. You *must* ensure
     * that no state changes have been made that should be controlled by
     * the specified participant.
     *
     * @param participant The participant to resign.
     *
     * @exception InvalidTransactionException Thrown if the transaction associated with
     * the thread is invalid.
     * @exception NoTransactionException Thrown if no transaction is associated
     * with the invoking thread.
     * @exception InvalidParticipantException Thrown if the transaction does not know
     * about the specified participant or the parameter is invalid.
     * @exception WrongStateException Thrown if the transaction is not in a state
     * that allows participants to resign.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void delist (Participant participant) throws InvalidTransactionException, NoTransactionException, InvalidParticipantException, WrongStateException, SystemException;

    /**
     * Enlist a synchronization with the current transaction. Synchronizations
     * do not receive the two-phase commit messages but instead are invoked
     * prior to its start and after it has completed.
     *
     * @param participant The synchronization to enroll.
     *
     * @exception WrongStateException Thrown if the transaction state is such
     * that synchronizations cannot be enrolled.
     * @exception NoTransactionException Thrown if there is no transaction
     * associated with the invoking thread.
     * @exception InvalidTransactionException Thrown if the transaction is not
     * top-level.
     * @exception InvalidSynchronizationException Thrown if the synchronization
     * reference is invalid.
     * @exception DuplicateSynchronizationException Thrown if the synchronization
     * has already been registered with the transaction.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void addSynchronization (Synchronization participant) throws WrongStateException, NoTransactionException, InvalidTransactionException, InvalidSynchronizationException, DuplicateSynchronizationException, SystemException;

    /**
     * Remove the specified synchronization participant from the transaction.
     *
     * @param participant The participant to remove.
     *
     * @exception InvalidTransactionException Thrown if the transaction is not
     * top-level.
     * @exception NoTransactionException Thrown if there is no transaction
     * associated with the current thread.
     * @exception InvalidSynchronizationException Thrown if the transaction
     * does not know about the specified synchronization.
     * @exception WrongStateException Thrown if the state of the transaction is
     * such that the synchronization cannot be removed.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void removeSynchronization (Synchronization participant) throws InvalidTransactionException, NoTransactionException, InvalidSynchronizationException, WrongStateException, SystemException;

}
