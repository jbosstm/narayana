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
 * $Id: AccountResource.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.demo.jta.localbank;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * This is a very simple implementation of an <CODE>XAResource</CODE>. Effectively it only supports rollback, commit and
 * a comparison call to check if this resource is the same as another.
 *
 * As an account it supports the reading and writing of a balance under transactional control. An AccountResource is
 * created per transaction which uses an Account. A more sophisticated apporach would be to use a <i>pool</i> of
 * <CODE>AccountResource</CODE>s.
 */
public class AccountResource implements XAResource
{
    /**
     * The transaction timeout. This is ONLY used by the get and set operations required by an XAResource. This
     * implementation of XAResource does not support transaction timeout.
     */
    private int _timeout = 0;

    /**
     * This contains the last commited balance. It can then be used to check during prepare if the value was modified or
     * read only. This is useful for the transaction manager to know and should be tracked.
     */
    private Account _account;

    /**
     * This is the last known good balance of the account object. It is the last commited state of the balance.
     */
    private float _initial_balance;

    /**
     * This is the current working balance of the account object. It is the uncommited state of the balance.
     */
    private float _current_balance;

    /**
     * The account resource is used by an account to transactionally update the accounts underlying balance. The balance
     * of the account is not modified until the transaction commits or rolls back.
     *
     * @param account   The account to monitor.
     */
    public AccountResource(Account account)
    {
        // Assign the account locally
        _account = account;

        _initial_balance = account._balance;
        _current_balance = _initial_balance;
    }

    /**
     * Returns the current volatile state of the account balance. This is the uncommited value and is read from the
     * balance storage variable.
     *
     * @return      The current volatile balance of the account.
     */
    public float balance()
    {
        return _current_balance;
    }

    /**
     * Credit the volatile storage with the required amount of money.
     *
     * @param amount    The amount to credit the account with.
     */
    public void credit(float amount)
    {
        _current_balance += amount;
    }

    /**
     * Debit the volatile storage by the required amount of money.
     *
     * @param amount    The amount to debit the account by.
     */
    public void debit(float amount)
    {
        _current_balance -= amount;
    }

    /**
     * Commits the current volatile state of the account to the <CODE>Account</CODE object.
     *
     * @param id            The XA transaction ID that is being commited.
     * @param onePhase      Whether this is the only participant or not. For the trailmap this will only be false for
     *                      the money transfer case.
     * @throws XAException  Not thrown by this implementation.
     *
     * @see javax.transaction.xa.XAResource
     */
    public void commit(Xid id, boolean onePhase) throws XAException
    {
        // NOTE: If this is a one phase commit the resource should check that it has had prepare called on it.
        // If this resource has not had prepare called on it and commit is called with onePhase set to false, the XA
        // resource should raise an XAException. This is not shown here.
        // Also note that If the resource manager did not commit the transaction and the paramether onePhase is set to
        // true, the resource manager may throw one of the XA_RB* exceptions.
        if (onePhase)
            System.out.println("XA_COMMIT (ONE_PHASE)[]");
        else
            System.out.println("XA_COMMIT[]");

        // Update the accounts actual balance with the last volatile state of the balance
        _account._balance = _current_balance;

        // Remove this AccountResource from the Account
        disassociateThisWithAccount();
    }

    /**
     * This method does not fully implement the required behaviour of XAResource.end(). For example, it does not support
     * suspend and resume.
     *
     * @param xid           A global transaction identifier.
     * @param flags         Indicates either success, fail or suspend.
     * @throws XAException  Not thrown by this implementation.
     *
     * @see javax.transaction.xa.XAResource
     */
    public void end(Xid xid, int flags) throws XAException
    {
        System.out.println("XA_END[]");
    }

    /**
     * This method does not fully implement the required behaviour of XAResource.forget().
     *
     * @param xid           A global transaction identifier.
     * @throws XAException  Not thrown by this implementation.
     *
     * @see javax.transaction.xa.XAResource
     */
    public void forget(Xid xid) throws XAException
    {
        System.out.println("XA_FORGET[]");
    }

    /**
     * This returns the transaction timeout. The timeout is ONLY used by the get and set operations required of an
     * XAResource and does not support the timing out of transactions.
     *
     * @return              The configured transaction timeout.
     * @throws XAException  Not thrown by this implementation.
     *
     * @see javax.transaction.xa.XAResource
     */
    public int getTransactionTimeout() throws XAException
    {
        return _timeout;
    }

    /**
     * Is this the same resource manager. This implementation does a simple pointer comparison, though more complex
     * implementations are feasible.
     *
     * @param xaResource    The resource to compare.
     * @return              True, if the resources are identical.
     *
     * @throws XAException  Not thrown by this implementation.
     *
     * @see javax.transaction.xa.XAResource
     */
    public boolean isSameRM(XAResource xaResource) throws XAException
    {
        return xaResource.equals(this);
    }

    /**
     * This is called to indicate to the resource that the transaction manager is ready to commit the transaction. A
     * full implementation of this would persist the result of the prepare call.
     *
     * @param xid           The transaction about to be completed.
     * @return              A flag indicating whether the updates so far are read only or have resulted in a volatile
     *                      change in the data ready to be committed.
     * @throws XAException  Not thrown by this implementation.
     *
     * @see javax.transaction.xa.XAResource
     */
    public int prepare(Xid xid) throws XAException
    {
        System.out.println("XA_PREPARE[]");
        if (_initial_balance == _current_balance)
        {
            // No change
            disassociateThisWithAccount();
            System.out.println("XA_PREPARE - READ_ONLY");
            return XA_RDONLY;
        }
        if (_current_balance < 0)
        {
            // A negative balance is not allowed
            disassociateThisWithAccount();
            throw new XAException(XAException.XA_RBINTEGRITY);
        }
        return XA_OK;
    }

    /**
     * This method does not fulfil the contract of XAResource.recover(). A full implementation should return a list of
     * all the transaction IDs that have been prepared and not yet completed.
     *
     * @param flag          Indicates when the scan should start/end/start and end/continue.
     * @return              The list of XIDS in prepared state as seen by this XAResource.
     *
     * @throws XAException  Not thrown by this implementation.
     *
     * @see javax.transaction.xa.XAResource
     */
    public Xid[] recover(int flag) throws XAException
    {
        System.out.println("XA_RECOVER[]");
        return null;
    }

    /**
     * This method does nothing as the account has not been modified and the resource is not pooled so no
     * reinitialization is required.
     *
     * @param xid           The transaction ID that the rollback is performed under.
     * @throws XAException  Not thrown by this implementation.
     *
     * @see javax.transaction.xa.XAResource
     */
    public void rollback(Xid xid) throws XAException
    {
        System.out.println("XA_ROLLBACK[]");
        disassociateThisWithAccount();
    }

    /**
     * This returns the transaction timeout. The timeout is ONLY used by the get and set operations required of an
     * XAResource and does not support the timing out of transactions.
     *
     * @param seconds   The timeout to set (in seconds).
     * @return          True, if the transaction timeout was set OK.
     *
     * @throws XAException  Not thrown by this implementation.
     *
     * @see javax.transaction.xa.XAResource
     */
    public boolean setTransactionTimeout(int seconds) throws XAException
    {
        _timeout = seconds;
        return true;
    }

    /**
     * This method does not fully implement the contract of XAResource.start(). For example, it does not support
     * suspend and resume.
     *
     * @param xid           The transaction that is starting.
     * @param flags         May either be TMJOIN (to join with an existing TX) TMRESUME (if the transaction was
     *                      previously suspended) or TMNOFLAGS (if it is just starting fresh).
     *
     * @throws XAException  Not thrown by this implementation.
     *
     * @see javax.transaction.xa.XAResource
     */
    public void start(Xid xid, int flags) throws XAException
    {
        System.out.println("XA_START[]");
    }

    /**
     * This method breaks the link with the <CODE>Account</CODE> object. This implementation of XA Resource does not
     * use pooling so when this call returns the <CODE>AccountResource</CODE> is totally dereferenced and is ready
     * for garbage collection.
     */
    private void disassociateThisWithAccount()
    {
        _account.accountResource = null;
    }
}
