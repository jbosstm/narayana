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
 * Copyright (C) 2003,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: UserTransaction.java,v 1.8.4.1 2005/11/22 10:36:05 kconner Exp $
 */

package com.arjuna.mw.wst11;

import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;

/**
 * This is the interface that allows transactions to be started and terminated.
 * The messaging layer converts the Commit, Rollback and Notify messages into
 * calls on this.
 * Importantly, a UserTransaction does not represent a specific transaction,
 * but rather is responsible for providing access to an implicit per-thread
 * transaction context; it is similar to the UserTransaction in the JTA
 * specification. Therefore, all of the UserTransaction methods implicitly act
 * on the current thread of control.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: UserTransaction.java,v 1.8.4.1 2005/11/22 10:36:05 kconner Exp $
 * @since XTS 1.0.
 */

public abstract class UserTransaction
{
    /**
     * The transaction.
     */
    private static UserTransaction USER_TRANSACTION ;

    /**
     * Get the user transaction.
     * @return the user transaction.
     */
    public static synchronized UserTransaction getUserTransaction()
    {
        return USER_TRANSACTION ;
    }

    /**
     * Set the user transaction.
     * @param userTransaction The user transaction.
     */
    public static synchronized void setUserTransaction(final UserTransaction userTransaction)
    {
        USER_TRANSACTION = userTransaction ;
    }

    /**
     * Start a new transaction. If one is already associated with this thread
     * then the WrongStateException will be thrown. Upon success, this
     * operation associates the newly created transaction with the current
     * thread.
     */
    public abstract void begin()
        throws WrongStateException, SystemException;

    /**
     * Start a new transaction with the specified timeout as its lifetime.
     * If one is already associated with this thread then the
     * WrongStateException will be thrown.
     */
    public abstract void begin(final int timeout)
        throws WrongStateException, SystemException;

    /**
     * Start a new subordinate transaction. If an AT transaction is not currently associated with
     * this thread then the WrongStateException will be thrown. Upon success, this
     * operation associates the newly created subordinate transaction with the current
     * thread.
     */
    public abstract void beginSubordinate()
        throws WrongStateException, SystemException;

    /**
     * Start a new transaction with the specified timeout as its lifetime.
     * If an AT transaction is not currently associated with
     * this thread then the WrongStateException will be thrown.
     */
    public abstract void beginSubordinate(final int timeout)
        throws WrongStateException, SystemException;

    /**
     * The transaction is committed by the commit method. This will execute
     * the PhaseZero, 2PC and OutcomeNotification protocols prior to returning.
     * If there is no transaction associated with the invoking thread then
     * WrongStateException is thrown. If the coordinator is not aware of the
     * current transaction UnknownTransactionException is thrown. If the transaction
     * ultimately rolls back then the TransactionRolledBackException is thrown.
     * If any other error occurs a SystemException is thrown. When complete, this
     * operation disassociates the transaction from the current thread such that
     * it becomes associated with no transaction.
     */
    public abstract void commit()
        throws TransactionRolledBackException, UnknownTransactionException, SecurityException, SystemException, WrongStateException;

    /**
     * The rollback operation will terminate the transaction and return
     * normally if it succeeded, while throwing an appropriate exception if it
     * didn't. If there is no transaction associated with the invoking thread
     * then WrongStateException is thrown. If the coordinator is not aware of the
     * current transaction UnknownTransactionException is thrown. If any other error
     * occurs a SystemException is thrown. When complete, this operation disassociates
     * the transaction from the current thread such that it becomes associated with no
     * transaction.
     */
    public abstract void rollback()
        throws UnknownTransactionException, SecurityException, SystemException, WrongStateException;

    public abstract String transactionIdentifier ();
}