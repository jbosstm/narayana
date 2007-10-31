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
 * $Id: UserTransaction.java,v 1.1 2002/11/25 11:00:51 nmcl Exp $
 */

package com.arjuna.mw.wstx;

import com.arjuna.mw.wsas.status.Status;

import com.arjuna.mw.wstx.transaction.*;

import com.arjuna.mw.wstx.common.TxId;

import com.arjuna.mw.wscf.common.Qualifier;

// TODO: obtain via configuration

import com.arjuna.mwlabs.wstx.common.arjunacore.TxIdImple;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.InvalidTimeoutException;

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
 * @version $Id: UserTransaction.java,v 1.1 2002/11/25 11:00:51 nmcl Exp $
 * @since 1.0.
 */

public interface UserTransaction
{

    /**
     * Start a new transaction. If there is already a transaction associated
     * with the thread then it will be nested.
     *
     * @exception WrongStateException Thrown if the any currently associated
     * transaction is in a state that does not allow a new transaction to be
     * enlisted.
     * @exception SystemException Thrown in any other situation.
     */

    public void begin () throws WrongStateException, SystemException;

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

    public void commit () throws InvalidTransactionException, WrongStateException, HeuristicHazardException, HeuristicMixedException, SystemException, NoTransactionException, TransactionRolledBackException;

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

    public void rollback () throws InvalidTransactionException, WrongStateException, HeuristicHazardException, HeuristicMixedException, SystemException, NoTransactionException, TransactionCommittedException;

    /**
     * @exception SystemException Thrown if any error occurs.
     *
     * @return the status of the current transaction. If there is no
     * transaction associated with the thread then NO_TRANSACTION
     * will be returned.
     */

    public Status status () throws SystemException;

    /**
     * What is the name of the current transaction? Use only for
     * debugging purposes.
     *
     * @exception NoTransactionException Thrown if there is no transaction
     * associated with the invoking thread.
     * @exception SystemException Thrown if any other error occurs.
     *
     * @return the name of the transaction.
     */

    public String transactionName () throws NoTransactionException, SystemException;
    
    /**
     * Suspend all transactions associated with this thread. The thread then
     * becomes associated with no transaction.
     *
     * @exception SystemException if any error occurs.
     *
     * @return a representation of the context associated with the thread,
     * or null if there is no context.
     */

    public TransactionHierarchy suspend () throws SystemException;
    
    /**
     * Associate this thread with the specified context. Any current
     * associations are lost.
     *
     * @param tx The context representation to associate (may be
     * null).
     *
     * @exception InvalidTransactionException Thrown if the context is invalid.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void resume (TransactionHierarchy tx) throws InvalidTransactionException, SystemException;

    /**
     * @return a representation of the context currently associated with
     * the invoking thread, or null if there is none.
     */

    public TransactionHierarchy currentTransaction () throws SystemException;

    /**
     * @exception NoTransactionException Thrown if there is no transaction
     * associated with the current thread.
     * @exception SystemException Thrown if any other error occurs.
     *
     * @return the qualifiers that are currently associated with the
     * transaction, or null if there are none.
     */

    public Qualifier[] qualifiers () throws NoTransactionException, SystemException;

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

    public void setRollbackOnly () throws NoTransactionException, WrongStateException, SystemException;

    /**
     * Get the timeout value currently associated with the transaction.
     *
     * @exception SystemException Thrown if any error occurs.
     *
     * @return the timeout value in seconds, or 0 if no application specified
     * timeout has been provided.
     */

    public int getTimeout () throws SystemException;

    /**
     * Set the timeout to be associated with all subsequently created
     * transactions. A default value of 0 is automatically associated with
     * each thread and this means that no application specified timeout is
     * set for activities.
     *
     * @param timeout The timeout (in seconds) to associate with all
     * subsequently created activities. This value must be 0 or greater.
     *
     * @exception InvalidTimeoutException Thrown if the timeout value provided
     * is negative, too large, or if timeouts are simply not supported by
     * the activity implementation.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void setTimeout (int timeout) throws InvalidTimeoutException, SystemException;

    /**
     * @exception NoTransactionException Thrown if there is no activity
     * associated with the invoking thread.
     * @exception SystemException Thrown if some other error occurred.
     *
     * @return the unique transaction id for the current transaction. This
     * may or may not be the same as the activity id.
     */

    public TxId identifier () throws NoTransactionException, SystemException;

}
