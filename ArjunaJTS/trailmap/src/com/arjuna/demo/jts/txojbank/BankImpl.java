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
 * $Id: BankImpl.java 2342 2006-03-30 13:06:17Z  $
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
import com.arjuna.orbportability.OA;

import java.util.Hashtable;


/**
 *
 * The object is also available remotely under transactional control ensuring that no partial accounts are commited to
 * the underlying datastore (for this example, the file system).
 */
public class BankImpl extends LockManager implements BankOperations
{
    /**
     * The maximum number of accounts that this bank can hold.
     */
    private static final int ACCOUNT_SIZE = 10;

    /**
     * The accounts at this bank.
     */
    private Hashtable _accounts = new Hashtable(ACCOUNT_SIZE);

    /**
     * The uids of all the accounts.
     */
    private String[] accounts = new String[ACCOUNT_SIZE];

    /**
     * The current number of accounts that this bank holds.
     */
    private int currentNumberOfAccounts = 0;

    /**
     * The object adapter to use to obtain the IORs of the bank account objects.
     */
    private OA _oa;

    /**
     * Create a new bank with the object adapter to use to create account object references.
     *
     * @param oa        The object adapter to create account object references with.
     */
    public BankImpl(OA oa)
    {
        // Set the type of this LockManager object, ANDPERSISTENT indicates that this object can participate in
        // transactions and is persisted to disk. Other types include RECOVERABLE (if the object can only particpate in
        // transactions) NEITHER (if the object can neither participate in transactions, nor be transactionally recovered)
        // of UNKNOWN_TYPE (if this object is initialized invalid - to be handled later)
        super(ObjectType.ANDPERSISTENT);
        _oa = oa;
    }

    /**
     * Restore a bank object.
     *
     * @param uid   The UID of this bank.
     * @param oa    The object adapter to use to resolve and return <CODE>Account</CODE> references.
     */
    public BankImpl(Uid uid, OA oa)
    {
        // Recreate this object from its UID
        super(uid);
        _oa = oa;
    }

    /**
     * Create a new bank account under transactional control.
     *
     * @param name  The name of the account.
     *
     * @return      A CORBA object of the account.
     */
    public Account create_account(String name)
    {
        AccountImpl acc;
        AccountPOA account = null;
        if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
        {
            if (currentNumberOfAccounts < ACCOUNT_SIZE)
            {
                acc = new AccountImpl(name);
                account = new AccountPOATie(acc);
                _accounts.put(name, acc);
                accounts[currentNumberOfAccounts] = acc.get_uid().toString();
                currentNumberOfAccounts++;
            }
        }
        return com.arjuna.demo.jts.txojbank.AccountHelper.narrow(_oa.corbaReference(account));
    }

    /**
     * Get the account object.
     *
     * @param name                  The name of the account to get.
     * @return                      The account object.
     *
     * @throws NotExistingAccount   If the account did not exist.
     */
    public Account get_account(String name) throws NotExistingAccount
    {
        AccountImpl acc = (AccountImpl) _accounts.get(name);

        AccountPOA account = new AccountPOATie(acc);

        if (acc == null)
            throw new NotExistingAccount("The Account requested does not exist");

        return com.arjuna.demo.jts.txojbank.AccountHelper.narrow(_oa.corbaReference(account));
    }

    /**
     * Save the current state of this object.
     *
     * @param os            The stream to write to.
     * @param ObjectType    The type of the object (used in super call).
     *
     * @return              A boolean indicating the success of the save.
     */
    public boolean save_state(OutputObjectState os, int ObjectType)
    {
        if (!super.save_state(os, ObjectType))
            return false;

        try
        {
            os.packInt(currentNumberOfAccounts);
            if (currentNumberOfAccounts > 0)
            {
                for (int i = 0; i < currentNumberOfAccounts; i++)
                    os.packString(accounts[i]);
            }
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
        if (!super.restore_state(os, ObjectType))
        {
            return false;
        }
        try
        {
            currentNumberOfAccounts = os.unpackInt();

            if (currentNumberOfAccounts > 0)
            {
                for (int i = 0; i < currentNumberOfAccounts; i++)
                {
                    accounts[i] = os.unpackString();
                    AccountImpl acc = new AccountImpl(new Uid(accounts[i]));
                    acc.activate();
                    _accounts.put(acc.getName(), acc);
                }
            }
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
        return "/StateManager/LockManager/BankServer";
    }
}

