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
 * $Id: Bank.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.demo.jta.localbank;

/**
 * This is the in memory implementation of the Bank class used in the JBoss Transactions product trailmap. As it is
 * non-persistent, it is only used to perform the banking operations that all Bank implementations in the trailmap
 * provide, such as creating and accessing an account.
 */
public class Bank
{
    /**
     * Initialize a new in memory bank represenation. As all the accounts are merely stored in a hashtable without a
     * persistent representation, this is all that is required.
     */
    private java.util.Hashtable _accounts = new java.util.Hashtable();

    /**
     * Create a new bank account with an initial balance of zero.
     *
     * @param name  The name to associate with the new account object.
     * @return      The newly created account object.
     */
    public Account create_account(String name)
    {
        // Create a new account
        Account acc = new Account();

        // Add the new account into the underlying data structure
        _accounts.put(name, acc);

        // Return the account object.
        return acc;
    }

    /**
     * Obtain access to a specified bank account. In the local bank implementation of the Bank class used in the Arjuna
     * Transaction product trailmap operations can be directly invoked on an <CODE>Account</CODE> object rather than
     * multiplexing all calls through the <CODE>Bank</CODE> object.
     *
     * @param name                  The name of the account to obtain a handle on.
     * @return                      The account.
     *
     * @throws NotExistingAccount   If the account has not yet been created.
     */
    public Account get_account(String name) throws NotExistingAccount
    {
        // Try to obtain a reference to the requested bank account object
        Account acc = (Account) _accounts.get(name);

        if (acc == null)
            // If the account does not exist, propagate an exception to the user notifying them of the problem
            throw new NotExistingAccount("The Account requested does not exist");

        // Return the reference to the bank account object
        return acc;
    }

}

