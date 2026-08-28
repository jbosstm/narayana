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
package com.arjuna.demo.jts.explicitremotebank;

import com.arjuna.orbportability.OA;

/**
 * This is an in memory, remotely available implementation of the Bank class used in the JBoss Transactions product
 * trailmap. As it is non-persistent, it is only used to perform the banking operations that all Bank implementations in
 * the trailmap provide, such as creating and accessing an account, though it is a CORBA object and as such can be
 * remotely accessed.
 */
public class BankImpl extends BankPOA
{
    /**
     * Initialize a new in memory bank represenation. As all the accounts are merely stored in a hashtable without a
     * persistent representation, this is all that is required.
     */
    private java.util.Hashtable _accounts = new java.util.Hashtable();

    /**
     * A reference to the object adapter in order to obtain CORBA references to the account objects.
     */
    private OA _oa;

    /**
     * Create a bank passing it a reference to the object adapter to use to resolve the account objects.
     *
     * @param oa    The object adapter to use to resolve references to the account objects.
     */
    public BankImpl(OA oa)
    {
        // Initialize the object adapter
        _oa = oa;
    }

    /**
     * Create a new bank account and store it in the bank. Return a CORBA object representation of the account.
     *
     * @param name  The name to associate with the account.
     *
     * @return      The <CODE>Account</CODE> CORBA object.
     */
    public Account create_account(String name)
    {
        // Create a new account with name "name"
        AccountImpl acc = new AccountImpl(name);
        // Store the object locally
        _accounts.put(name, acc);

        // Create a CORBA object representing the account
        System.out.println("create account ...........");
        // Return this to the caller
        return com.arjuna.demo.jts.explicitremotebank.AccountHelper.narrow(_oa.corbaReference(acc));
    }

    /**
     * Gain access to the bank account reference name of "name".
     *
     * @param name                  The name of the account to access.
     * @return                      The suitable account reference.
     *
     * @throws NotExistingAccount   If the account did not exist.
     */
    public Account get_account(String name) throws NotExistingAccount
    {
        // Gain a local reference to the account
        AccountImpl acc = (AccountImpl) _accounts.get(name);
        // If the account does not exist throw an exception to the user
        if (acc == null)
            throw new NotExistingAccount("The Account requested does not exist");

        // Print out a message to indicate that the <CODE>Account</CODE> local reference is being converted to a
        // CORBA reference
        System.out.println("get account information ...........");
        // "Convert" the reference
        return com.arjuna.demo.jts.explicitremotebank.AccountHelper.narrow(_oa.corbaReference(acc));
    }
}

