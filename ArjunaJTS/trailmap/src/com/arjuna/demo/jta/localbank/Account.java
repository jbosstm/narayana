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
 * Copyright (C) 2003, 2004
 * Arjuna Technologies Limited
 * Newcastle upon Tyne, UK
 *
 * $Id: Account.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.demo.jta.localbank;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

/**
 * The account object reflects a non-persistent transaction-aware representation of a bank account. As a bank account,
 * it has a balance associated with it. As a transaction-aware entity it has a trailmap defined XA resource associated
 * with it (a single XAResource is created for each transaction that uses the account).
 *
 * This object allows an account balance to be interrogated, increased or decreased, all under transactional control.
 */
public class Account
{
    /**
     * This is the current value of this bank account. It is initialized to zero.
     */
    float _balance = 0;

    /**
     * This is XA resource associated with this bank account for this particular transaction. This code is not
     * thread-safe as the tralmap is mono-threaded. A true implementation of an <CODE>Account</CODE> could store
     * the <CODE>AccountResource</CODE>'s in a hashtable indexed by <CODE>Transaction</CODE>.
     */
    AccountResource accountResource = null;

    /**
     * This operation will obtain information about the current balance of the bank account.
     *
     * @return                      The current balance of the bank account.
     *
     * @throws SystemException      If the current transaction cannot be obtained.
     * @throws RollbackException    If the account is tried to be used after its transaction is marked for rollback.
     */
    public float balance() throws SystemException, RollbackException
    {
        // Delegate this call to the XA resource
        return getXAResource().balance();
    }

    /**
     * Try to credit this account by the desired amount.
     *
     * @param value                 The amount to credit the account with.
     *
     * @throws SystemException      If the current transaction cannot be obtained.
     * @throws RollbackException    If the account is tried to be used after its transaction is marked for rollback.
     */
    public void credit(float value) throws SystemException, RollbackException
    {
        // Delegate this call to the XA resource
        getXAResource().credit(value);
    }

    /**
     * Try to debit this account by the desired amount.
     *
     * @param value                 The amount to debit the account by.
     *
     * @throws SystemException      If the current transaction cannot be obtained.
     * @throws RollbackException    If the account is tried to be used after its transaction is marked for rollback.
     */
    public void debit(float value) throws SystemException, RollbackException
    {
        // Delegate this call to the XA resource
        getXAResource().debit(value);
    }

    /**
     * Get access to the XA resource. The XA resource is the workhorse part of the account object. The tutorial provides
     * a sample XA-aware non-persistent object that can be used to manage an account transactionally.
     *
     * @return                      The JBoss Transactions product sample XA resource.
     *
     * @throws SystemException      If the current transaction cannot be obtained.
     * @throws RollbackException    If the account is tried to be used after its transaction is marked for rollback.
     */
    private AccountResource getXAResource() throws SystemException, RollbackException
    {
        // Obtain a reference to the transaction manager, this could also be done via JNDI.
        javax.transaction.TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();
        // Obtain a reference to the current transaction, this code assumes that the transaction has had begin() called
        // on it before reaching this point. begin() is called from the corresponding invocations to the account
        // operations in this class in the <CODE>BankClient</CODE> class
        Transaction currentTransaction = transactionManager.getTransaction();

        // If this account has not created an XA resource yet
        if (accountResource == null)
        {
            currentTransaction.enlistResource(accountResource = new AccountResource(this));
        }

        // currentTrans.delistResource( accountResource, XAResource.TMSUCCESS );
        return accountResource;
    }
}
