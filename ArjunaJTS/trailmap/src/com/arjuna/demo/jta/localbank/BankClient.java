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
 * $Id: BankClient.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.demo.jta.localbank;

import java.io.IOException;

/**
 * The <CODE>BankClient</CODE> application is an interactive CLI that allows the user of the JBoss Transactions product
 * to manipulate a volatile hashtable-backed <CODE>Bank</CODE> under transactional control.
 */
public class BankClient
{
    /**
     * A reference to the bank object to invoke updates on.
     */
    private Bank _bank;

    /**
     * Create a new BankClient indicating the Bank to execute updates and queries upon.
     *
     * @param bank  A reference to the bank to update.
     */
    private BankClient(Bank bank)
    {
        // Store this reference for calls to be executed upon
        _bank = bank;
    }

    /**
     * Display menu and return the selected operation.
     *
     * @return The users choice of command.
     */
    private static int menu()
    {
        System.out.println("");
        System.out.println("-------------------------------------------------");
        System.out.println("Bank client ");
        System.out.println("-------------------------------------------------");
        System.out.println("");
        System.out.println("Select an option : ");
        System.out.println("\t0. Quit");
        System.out.println("\t1. Create a new account.");
        System.out.println("\t2. Get an account information.");
        System.out.println("\t3. Make a transfer.");
        System.out.println("\t4. Credit an account.");
        System.out.println("\t5. Withdraw from an account.");
        System.out.println("\t6. Display this menu.");
        System.out.println("");
        System.out.print("Your choice : ");
        String choice = input();
        // Can only be null if there was a problem reading the input from the user.
        if (choice == null)
            return 0;

        // Return the int indicating the user's choice
        try
        {
            return Integer.parseInt(choice);
        }
        catch (NumberFormatException e)
        {
            // If the user input was not a number return as if the user asked to redisplay the menu.
            return 6;
        }
    }

    /**
     * This operation is used to get data from the keyboard.
     *
     * @return The input from the keyboard, or null if there is a problem reading the data.
     */
    private static String input()
    {
        try
        {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
            return reader.readLine();
        }
        catch (IOException ioe)
        {
            System.err.println("Problem reading from the keyboard, returning null");
            // Ignore this and return null
        }
        return null;
    }

    /**
     * This will display the menu in a loop asking for user input to create accounts or update them.
     */
    private void start()
    {
        while (true)
        {
            switch (menu())
            {
                case 0:
                    System.exit(0);
                case 1:
                    newAccount();
                    break;
                case 2:
                    getInfo();
                    break;
                case 3:
                    makeTransfer();
                    break;
                case 4:
                    makeCredit();
                    break;
                case 5:
                    makeWithdraw();
                    break;
                case 6:
                    break;
            }
        }
    }

    /**
     * This operation will create a new zero balance bank account and then update the balance of this account to the
     * amount required, all under transactional control.
     */
    private void newAccount()
    {
        // Display the relevant menu for this choice
        System.out.println("");
        System.out.println("- Create a new account -");
        System.out.println("------------------------");
        System.out.println("");

        // Obtain a name to associate with the account
        System.out.print("Name : ");
        String name = input();
        if (name == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        // Obtain the balance to initially credit the account with
        System.out.print("Initial balance : ");
        String sbalance = input();
        float fbalance = 0;
        if (sbalance == null)
        {
            System.out.println("Abort operation...");
            return;
        }
        try
        {
            fbalance = new Float(sbalance).floatValue();
        }
        catch (java.lang.Exception ex)
        {
            System.out.println("Invalid float number, abort operation...");
            return;
        }

        try
        {
            // Obtain a reference to the user transaction object inorder to transactionally perform this work, this
            // could also be done via JNDI.
            javax.transaction.UserTransaction userTransaction = com.arjuna.ats.jta.UserTransaction.userTransaction();
            System.out.println("Beginning a User transaction to create account");
            // Begin the transaction so all work now will be executed under transactional control
            userTransaction.begin();
            try
            {
                // Create the account transactionally
                Account acc = _bank.create_account(name);
                acc.credit(fbalance);
            }
            catch (Exception e)
            {
                System.out.println("Problems updating the account, rollback the transaction");
                userTransaction.setRollbackOnly();
            }
            System.out.println("Attempt to commit the account creation transaction");
            // Commit the transaction and disassociate this thread from the transaction
            userTransaction.commit();
        }
        catch (Exception e)
        {
            // If there is a problem executing the transaction
            System.err.println("ERROR - " + e);
            e.printStackTrace(System.err);
        }
    }

    /**
     * This operation is used to get information about an account. It will query the underlying <CODE>Bank</CODE> object
     * for an account holders current balance.
     */
    private void getInfo()
    {
        // Display the relevant menu for this choice
        System.out.println("");
        System.out.println("- Get information about an account -");
        System.out.println("------------------------------------");
        System.out.println("");

        // Get the name of the account to display information about
        System.out.print("Name : ");
        String name = input();
        if (name == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        try
        {
            // Obtain a reference to the user transaction object inorder to transactionally perform this work, this
            // could also be done via JNDI.
            javax.transaction.UserTransaction userTransaction = com.arjuna.ats.jta.UserTransaction.userTransaction();
            System.out.println("Beginning a User transaction to get balance");
            // Begin the transaction so all work now will be executed under transactional control
            userTransaction.begin();
            try
            {
                Account acc = _bank.get_account(name);
                System.out.println("Balance : " + acc.balance());
            }
            catch (NotExistingAccount nea)
            {
                // This is a rather artifical example as the operation is only a read operation
                System.out.println("The requested account does not exist!");
                userTransaction.setRollbackOnly();
            }
            System.out.println("Attempt to commit the account creation transaction");
            // Commit the transaction and disassociate this thread from the transaction
            userTransaction.commit();
        }
        catch (Exception e)
        {
            // If there is a problem executing the transaction
            System.err.println("ERROR - " + e);
            e.printStackTrace(System.err);
        }
    }

    /**
     * This operation is used to make a transfer from an account to another account.
     */
    private void makeTransfer()
    {
        // Display the relevant menu for this choice
        System.out.println("");
        System.out.println("- Make a transfer -");
        System.out.println("-------------------");
        System.out.println("");

        // Get the name of the account to remove money from
        System.out.print("Take money from : ");
        String debtor = input();
        if (debtor == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        // Get the name of the account to add money to
        System.out.print("Put money to : ");
        String creditor = input();
        if (creditor == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        // Get the amount of money to transfer
        System.out.print("Transfer amount : ");
        String samount = input();
        float famount = 0;
        if (samount == null)
        {
            System.out.println("Abort operation...");
            return;
        }
        try
        {
            famount = new Float(samount).floatValue();
        }
        catch (java.lang.Exception ex)
        {
            System.out.println("Invalid float number, abort operation...");
            return;
        }

        try
        {
            // Obtain a reference to the user transaction object inorder to transactionally perform this work, this
            // could also be done via JNDI.
            javax.transaction.UserTransaction userTransaction = com.arjuna.ats.jta.UserTransaction.userTransaction();
            System.out.println("Beginning a User transaction to get transfer money");
            // Begin the transaction so all work now will be executed under transactional control
            userTransaction.begin();
            try
            {
                Account supplier = _bank.get_account(debtor);
                Account consumer = _bank.get_account(creditor);
                supplier.debit(famount);
                consumer.credit(famount);
            }
            catch (NotExistingAccount nea)
            {
                // Indicate that the transfer operation should be rolled back
                System.out.println("The requested account does not exist!");
                userTransaction.setRollbackOnly();
            }
            catch (Exception e)
            {
                // Indicate that the transfer operation should be rolled back
                System.out.println("There was a problem interracting with the accounts!");
                userTransaction.setRollbackOnly();
            }
            System.out.println("Attempt to commit the account creation transaction");
            // Commit the transaction and disassociate this thread from the transaction
            userTransaction.commit();
        }
        catch (Exception e)
        {
            // If there is a problem executing the transaction
            System.err.println("ERROR - " + e);
            e.printStackTrace(System.err);
        }
    }

    /**
     * This operation is used to credit an account. It will ask the user to enter the name of an account and an amount
     * to credit this account with.
     */
    private void makeCredit()
    {
        // Display the relevant menu for this choice
        System.out.println("");
        System.out.println("- Credit an Account -");
        System.out.println("-------------------");
        System.out.println("");

        // Get the name of the account to credit
        System.out.print("Give the Account name : ");
        String name_consumer = input();
        if (name_consumer == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        // Get the amount of money to credit the account with
        System.out.print("Amount to credit : ");
        String samount = input();
        float famount = 0;
        if (samount == null)
        {
            System.out.println("Abort operation...");
            return;
        }
        try
        {
            famount = new Float(samount).floatValue();
        }
        catch (java.lang.Exception ex)
        {
            System.out.println("Invalid float number, abort operation...");
            return;
        }

        try
        {
            // Obtain a reference to the user transaction object inorder to transactionally perform this work, this
            // could also be done via JNDI.
            javax.transaction.UserTransaction userTransaction = com.arjuna.ats.jta.UserTransaction.userTransaction();
            System.out.println("Beginning a User transaction to credit an account");
            // Begin the transaction so all work now will be executed under transactional control
            userTransaction.begin();
            try
            {
                Account consumer = _bank.get_account(name_consumer);
                consumer.credit(famount);
            }
            catch (NotExistingAccount nea)
            {
                // Indicate that the transfer operation should be rolled back
                System.out.println("The requested account does not exist!");
                userTransaction.setRollbackOnly();
            }
            catch (Exception e)
            {
                // Indicate that the transfer operation should be rolled back
                System.out.println("There was a problem interracting with the accounts!");
                userTransaction.setRollbackOnly();
            }
            System.out.println("Attempt to commit the account creation transaction");
            // Commit the transaction and disassociate this thread from the transaction
            userTransaction.commit();
        }
        catch (Exception e)
        {
            // If there is a problem executing the transaction
            System.err.println("ERROR - " + e);
            e.printStackTrace(System.err);
        }
    }

    /**
     * This operation is used to debit money from an accout. It will prompt the user to enter the name of the account
     * and the amount to debit.
     */
    private void makeWithdraw()
    {
        // Display the relevant menu for this choice
        System.out.println("");
        System.out.println("- Withrdaw from an Account -");
        System.out.println("-------------------");
        System.out.println("");

        // Get the name of the account to debit
        System.out.print("Give the Account name : ");
        String name_debit = input();
        if (name_debit == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        // Get the amount of money to withdraw from this account
        System.out.print("Amount to withdraw : ");
        String samount = input();
        float famount = 0;
        if (samount == null)
        {
            System.out.println("Abort operation...");
            return;
        }
        try
        {
            famount = new Float(samount).floatValue();
        }
        catch (java.lang.Exception ex)
        {
            System.out.println("Invalid float number, abort operation...");
            return;
        }

        try
        {
            // Obtain a reference to the user transaction object inorder to transactionally perform this work, this
            // could also be done via JNDI.
            javax.transaction.UserTransaction userTransaction = com.arjuna.ats.jta.UserTransaction.userTransaction();
            System.out.println("Beginning a User transaction to withdraw from an account");
            // Begin the transaction so all work now will be executed under transactional control
            userTransaction.begin();
            try
            {
                Account debiter = _bank.get_account(name_debit);
                debiter.debit(famount);
            }
            catch (NotExistingAccount nea)
            {
                // Indicate that the transfer operation should be rolled back
                System.out.println("The requested account does not exist!");
                userTransaction.setRollbackOnly();
            }
            catch (Exception e)
            {
                // Indicate that the transfer operation should be rolled back
                System.out.println("There was a problem interracting with the accounts!");
                userTransaction.setRollbackOnly();
            }
            System.out.println("Attempt to commit the account creation transaction");
            // Commit the transaction and disassociate this thread from the transaction
            userTransaction.commit();
        }
        catch (Exception e)
        {
            // If there is a problem executing the transaction
            System.err.println("ERROR - " + e);
            e.printStackTrace(System.err);
        }
    }


    /**
     * This is the entry point into the JBoss Transaction product trailmap sample that uses a <CODE>Hashtable</CODE> to
     * store the bank accounts repository. It is part of the JTA trailmap when using local objects.
     *
     * @param args Not used in this example.
     */
    public static void main(String[] args)
    {
        // The volatile <CODE>Bank</CODE> implementation
        Bank bank = new Bank();

        // A client that can invoke operations on this <CODE>Bank</CODE>
        BankClient client = new BankClient(bank);

        // Start the client, this will run the CLI portion of the client code
        client.start();
    }
}
