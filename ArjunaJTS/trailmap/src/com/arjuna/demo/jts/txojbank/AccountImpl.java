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
 * $Id: AccountImpl.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.demo.jts.txojbank;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;

/**
 * This account representation is provided as part of the JBoss Transactions product trailmap. It illustrates the
 * creation of a Transactional Object 4 Java (TXOJ). As a TXOJ, this class extends LockManager which provides it with
 * the standard TXOJ capabilities save_state and restore_state. This allows the object to participate in transactions
 * seamlessly with the JBoss transaction service.
 *
 * The account provides standard banking operations as with all examples in the trailmap. The source code for these
 * operations indicates how the programmer can mediate their access to the state of the object without the need of
 * an external resource.
 */
public class AccountImpl extends LockManager implements AccountOperations
{
    /**
     * The current balance.
     */
    private float _balance = 0;

    /**
     * The name of the bank account.
     */
    private String _name;

    /**
     * Create a new zero balance bank account.
     *
     * @param name  The name of the bank account.
     */
    public AccountImpl(String name)
    {
        // Set the type of this LockManager object, ANDPERSISTENT indicates that this object can participate in
        // transactions and is persisted to disk. Other types include RECOVERABLE (if the object can only particpate in
        // transactions) NEITHER (if the object can neither participate in transactions, nor be transactionally recovered)
        // of UNKNOWN_TYPE (if this object is initialized invalid - to be handled later)
        super(ObjectType.ANDPERSISTENT);
        // Initialize the name of the account
        _name = name;
    }

    /**
     * Recreate an account by its UID.
     *
     * @param uid       The account to recover.
     */
    public AccountImpl(Uid uid)
    {
        super(uid);
    }

    /**
     * All implementations of <CODE>LockManager</CODE> should provide a finalize which invokes the super method terminate.
     */
    public void finalize()
    {
        super.terminate();
    }


    /**
     * The name of the account.
     *
     * @return  The name of the account.
     */
    protected String getName()
    {
        return _name;
    }

    /**
     * Obtain the balance of this account under transactional control.
     *
     * @return  The balance according to this transaction.
     */
    public float balance()
    {
        float result = 0;
        // Set a read lock
        if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED)
        {
            result = _balance;
        }
        else
        {
            //mark the transaction to rollback
        }
        return result;
    }

    /**
     * Credit this account within the scope of this transaction.
     *
     * @param value The amount to credit the account by.
     */
    public void credit(float value)
    {
        // Set a write lock
        if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
        {
            _balance += value;
        }
        else
        {
            //mark the transaction to rollback
        }
    }

    /**
     * Debit the account within the scope of this transaction.
     *
     * @param value The amount to debit the account by.
     */
    public void debit(float value)
    {
        // Set a write lock on the account
        if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
        {
            _balance -= value;
        }
        else
        {
            //mark the transaction to rollback
        }
    }

    /**
     * Save the current state of this object. Save state should not perform any business logic.
     *
     * @param os            The stream to write the output state onto.
     * @param ObjectType    The type of the object to super with.
     *
     * @return              A boolean indicating the success of the pack operations.
     */
    public boolean save_state(OutputObjectState os, int ObjectType)
    {
        // All implementations MUST first call this
        if (!super.save_state(os, ObjectType))
            return false;

        try
        {
            os.packString(_name);
            os.packFloat(_balance);

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Restore the object to the state indicated. Restore state should not perform any business logic.
     *
     * @param os            The stream to read the previous state from.
     * @param ObjectType    The type of the object to super with.
     *
     * @return              A boolean indicating the success of the unpack operations.
     */
    public boolean restore_state(InputObjectState os, int ObjectType)
    {
        // All implementations MUST first call this
        if (!super.restore_state(os, ObjectType))
            return false;

        try
        {
            _name = os.unpackString();
            _balance = os.unpackFloat();

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * The type of this <CODE>LockManager</CODE>. This is used by Arjuna to determine the objectstore location to save
     * the objects state within.
     *
     * @return  The type of the lock manager.
     */
    public String type()
    {
        return "/StateManager/LockManager/BankingAccounts";
    }
}
