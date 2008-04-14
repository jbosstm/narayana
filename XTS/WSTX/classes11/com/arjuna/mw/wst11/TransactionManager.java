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
 * Copyright (C) 2003,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionManager.java,v 1.10.6.1 2005/11/22 10:36:06 kconner Exp $
 */

package com.arjuna.mw.wst11;

import com.arjuna.wst.*;

import com.arjuna.wsc.AlreadyRegisteredException;
import com.arjuna.mw.wst.TxContext;

/**
 * This is the interface that the core exposes in order to allow different
 * types of participants to be enrolled. The messaging layer continues to
 * work in terms of the registrar, but internally we map to one of these
 * methods.
 *
 * This could also be the interface that high-level users see (e.g., at the
 * application Web Service).
 *
 * As with UserTransaction a TransactionManager does not represent a specific
 * transaction, but rather is responsible for providing access to an implicit
 * per-thread transaction context.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TransactionManager.java,v 1.10.6.1 2005/11/22 10:36:06 kconner Exp $
 * @since XTS 1.0.
 */

public abstract class TransactionManager
{
    /**
     * The manager.
     */
    private static TransactionManager TRANSACTION_MANAGER ;

    /**
     * Get the transaction manager.
     * @return The transaction manager.
     */
    public static synchronized TransactionManager getTransactionManager()
    {
        return TRANSACTION_MANAGER ;
    }

    /**
     * Set the transaction manager.
     * @param manager The transaction manager.
     */
    public static synchronized void setTransactionManager(final TransactionManager manager)
    {
        TRANSACTION_MANAGER = manager ;
    }

    /**
     * Enlist the specified participant with current transaction such that it
     * will participate in the Volatile 2PC protocol; a unique identifier for
     * the participant is also required. If there is no transaction associated
     * with the invoking thread then the UnknownTransactionException exception
     * is thrown. If the coordinator already has a participant enrolled with
     * the same identifier, then AlreadyRegisteredException will be thrown. If
     * the transaction is not in a state where participants can be enrolled
     * (e.g., it is terminating) then WrongStateException will be thrown.
     */
    public abstract void enlistForVolatileTwoPhase(final Volatile2PCParticipant pzp, final String id)
        throws WrongStateException, UnknownTransactionException, AlreadyRegisteredException, SystemException;

    /**
     * Enlist the specified participant with current transaction such that it
     * will participate in the 2PC protocol; a unique identifier for the
     * participant is also required. If there is no transaction associated with
     * the invoking thread then the UnknownTransactionException exception is
     * thrown. If the coordinator already has a participant enrolled with the
     * same identifier, then AlreadyRegisteredException will be thrown. If the
     * transaction is not in a state where participants can be enrolled (e.g.,
     * it is terminating) then WrongStateException will be thrown.
     */
    public abstract void enlistForDurableTwoPhase(final Durable2PCParticipant tpp, final String id)
        throws WrongStateException, UnknownTransactionException, AlreadyRegisteredException, SystemException;

    public abstract int replay () throws SystemException;

    /**
     * The resume method can be used to (re-)associate a thread with a
     * transaction(s) via its TxContext. Prior to association, the thread is
     * disassociated with any transaction(s) with which it may be currently
     * associated. If the TxContext is null, then the thread is associated with
     * no transaction. The UnknownTransactionException exception is thrown if
     * the transaction that the TxContext refers to is invalid in the scope of
     * the invoking thread.
     */
    public abstract void resume(final TxContext txContext)
        throws UnknownTransactionException, SystemException;

    /**
     * A thread of control may require periods of non-transactionality so that
     * it may perform work that is not associated with a specific transaction.
     * In order to do this it is necessary to disassociate the thread from any
     * transactions. The suspend method accomplishes this, returning a
     * TxContext instance, which is a handle on the transaction. The thread is
     * then no longer associated with any transaction.
     */
    public abstract TxContext suspend()
        throws SystemException;

    /**
     * The currentTransaction method returns the TxContext for the current
     * transaction, or null if there is none. Unlike suspend, this method does
     * not disassociate the current thread from the transaction(s). This can
     * be used to enable multiple threads to execute within the scope of the
     * same transaction.
     */
    public abstract TxContext currentTransaction()
        throws SystemException;
}