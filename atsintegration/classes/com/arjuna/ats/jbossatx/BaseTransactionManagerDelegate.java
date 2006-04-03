/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
package com.arjuna.ats.jbossatx;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.tm.TransactionLocal;
import org.jboss.tm.TransactionLocalDelegate;

/**
 * Delegate for JBoss TransactionManager/TransactionLocalDelegate.
 * @author kevin
 */
public class BaseTransactionManagerDelegate implements TransactionManager, TransactionLocalDelegate
{
    /**
     * Delegate transaction manager.
     */
    private final TransactionManager transactionManager ;
    /**
     * Delegate transaction local delegate.
     */
    private final TransactionLocalDelegate transactionLocalDelegate ;
    
    /**
     * Construct the delegate using the specified transaction manager.
     * @param transactionManager The delegate transaction manager.
     */
    protected BaseTransactionManagerDelegate(final TransactionManager transactionManager)
    {
        this(transactionManager, new TransactionLocalDelegateImpl()) ;
    }
    
    /**
     * Construct the delegate using the specified transaction manager and transaction local delegate.
     * @param transactionManager The delegate transaction manager.
     * @param transactionLocalDelegate The delegate transaction local delegate.
     */
    protected BaseTransactionManagerDelegate(final TransactionManager transactionManager, final TransactionLocalDelegate transactionLocalDelegate)
    {
        this.transactionManager = transactionManager ;
        this.transactionLocalDelegate = transactionLocalDelegate ;
    }

    /**
     * Begin a transaction and associate it with the current thread.
     */
    public void begin()
        throws NotSupportedException, SystemException
    {
        transactionManager.begin() ;
    }

    /**
     * Commit the current transaction and disassociate from the thread.
     */
    public void commit()
        throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
        SecurityException, IllegalStateException, SystemException
    {
        transactionManager.commit() ;
    }

    /**
     * Get the transaction status.
     * @return the transaction status.
     */
    public int getStatus()
        throws SystemException
    {
        return transactionManager.getStatus() ;
    }

    /**
     * Get the transaction associated with the thread.
     * @return the transaction or null if none associated.
     */
    public Transaction getTransaction()
        throws SystemException
    {
        return transactionManager.getTransaction() ;
    }

    /**
     * Resume the specified transaction.
     * @param transaction The transaction to resume.
     */
    public void resume(final Transaction transaction)
        throws InvalidTransactionException, IllegalStateException, SystemException
    {
        transactionManager.resume(transaction) ;
    }

    /**
     * Rollback the current transaction and disassociate from the thread.
     */
    public void rollback()
        throws IllegalStateException, SecurityException, SystemException
    {
        transactionManager.rollback() ;
    }

    /**
     * Set rollback only on the current transaction.
     */
    public void setRollbackOnly()
        throws IllegalStateException, SystemException
    {
        transactionManager.setRollbackOnly() ;
    }

    /**
     * Set the transaction timeout on the current thread.
     * @param timeout The transaction timeout.
     */
    public void setTransactionTimeout(final int timeout)
        throws SystemException
    {
        transactionManager.setTransactionTimeout(timeout) ;
    }

    /**
     * Suspend the current transaction.
     * @return The suspended transaction.
     */
    public Transaction suspend()
        throws SystemException
    {
        return transactionManager.suspend() ;
    }

    /**
     * Does the specified transaction contain a value for the transaction local.
     * @param transactionLocal The associated transaction local.
     * @param transaction The associated transaction.
     * @return true if a value exists within the specified transaction, false otherwise.
     */
    public boolean containsValue(final TransactionLocal transactionLocal, final Transaction transaction)
    {
        return transactionLocalDelegate.containsValue(transactionLocal, transaction) ;
    }

    /**
     * Get value of the transaction local in the specified transaction.
     * @param transactionLocal The associated transaction local.
     * @param transaction The associated transaction.
     * @return The value of the transaction local.
     */
    public Object getValue(final TransactionLocal transactionLocal, final Transaction transaction)
    {
        return transactionLocalDelegate.getValue(transactionLocal, transaction) ;
    }

    /**
     * Store the value of the transaction local in the specified transaction.
     * @param transactionLocal The associated transaction local.
     * @param transaction The associated transaction.
     * @param value The value of the transaction local.
     */
    public void storeValue(final TransactionLocal transactionLocal, final Transaction transaction,
            final Object value)
    {
        transactionLocalDelegate.storeValue(transactionLocal, transaction, value) ;
    }
    
    /**
     * Lock the transaction local in the context of this transaction.
     * @throws IllegalStateException if the transaction is not active
     * @throws InterruptedException if the thread is interrupted
     */
    public void lock(final TransactionLocal local, final Transaction tx)
    		throws InterruptedException
	{
    		transactionLocalDelegate.lock(local, tx) ;
	}
    
    /**
     * Unlock the transaction local in the context of this transaction
     */
    public void unlock(final TransactionLocal local, final Transaction tx)
    {
		transactionLocalDelegate.unlock(local, tx) ;
    }
}
