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
package com.arjuna.demo.jts.txojbank;

import com.arjuna.ats.jts.OTSManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import org.omg.CosTransactions.Current;

import java.io.IOException;

/**
 * The <CODE>BankClient</CODE> application is an interactive CLI that allows the user of the JBoss Transactions product
 * to manipulate a database backed bank under transactional control.
 */
public class BankClient
{
    /**
     * A reference to the bank to invoke banking operations upon.
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
     * This operation is used to create a new account.
     */
    private void newAccount()
    {
        System.out.println("");
        System.out.println("- Create a new account -");
        System.out.println("------------------------");
        System.out.println("");

        System.out.print("Name : ");
        String name = input();
        if (name == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        System.out.print("Initial balance : ");
        String balance = input();
        if (balance == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        float fbalance = 0;
        try
        {
            fbalance = new Float(balance).floatValue();
        }
        catch (java.lang.Exception ex)
        {
            System.out.println("Invalid float number, abort operation...");
            return;
        }

        try
        {
            Current current = OTSManager.get_current();
            System.out.println("Beginning a User transaction to create account");
            current.begin();
            Account acc = _bank.create_account(name);
            System.out.println("Credit the Account");
            acc.credit(fbalance);
            System.out.println("Attempt to commit the account creation transaction");
            current.commit(false);
        }
        catch (Exception e)
        {
            System.err.println("ERROR - " + e);
        }
    }

    /**
     * This operation is used to get information about an account.
     */
    private void getInfo()
    {
        System.out.println("");
        System.out.println("- Get information about an account -");
        System.out.println("------------------------------------");
        System.out.println("");

        System.out.print("Name : ");

        String name = input();
        if (name == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        try
        {
            Current current = OTSManager.get_current();
            System.out.println("Beginning a User transaction to get balance");
            current.begin();
            try
            {
                Account acc = _bank.get_account(name);
                System.out.println("Balance : " + acc.balance());
            }
            catch (NotExistingAccount nea)
            {
                System.out.println("Account not Found");
                current.rollback_only();
            }

            current.commit(false);
        }
        catch (Exception e)
        {
            System.err.println("ERROR - " + e);
        }
    }

    /**
     * This operation is used to make a transfer from an account to another account.
     */
    private void makeTransfer()
    {
        System.out.println("");
        System.out.println("- Make a transfer -");
        System.out.println("-------------------");
        System.out.println("");

        System.out.print("Take money from : ");

        String name_supplier = input();
        if (name_supplier == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        System.out.print("Put money to : ");
        String name_consumer = input();
        if (name_consumer == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        System.out.print("Transfer amount : ");
        String amount = input();
        if (amount == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        float famount = 0;
        try
        {
            famount = new Float(amount).floatValue();
        }
        catch (java.lang.Exception ex)
        {
            System.out.println("Invalid float number, abort operation...");
            return;
        }

        Current current = null;
        try
        {
            current = OTSManager.get_current();
            System.out.println("Beginning a User transaction to Transfer money");
            current.begin();
            try
            {
                Account supplier = _bank.get_account(name_supplier);
                Account consumer = _bank.get_account(name_consumer);

                supplier.debit(famount);
                consumer.credit(famount);
            }
            catch (NotExistingAccount nea)
            {
                System.out.println("Account not Found");
                current.rollback_only();
            }

            current.commit(false);
        }
        catch (Exception e)
        {
            System.err.println("ERROR - " + e);
        }
    }

    /**
     * This operation is used to credit an account.
     */
    private void makeCredit()
    {
        System.out.println("");
        System.out.println("- Credit an Account -");
        System.out.println("-------------------");
        System.out.println("");

        System.out.print("Give the Account name : ");
        String name_consumer = input();
        if (name_consumer == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        System.out.print("Amount to credit : ");
        String amount = input();
        if (amount == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        float famount = 0;
        try
        {
            famount = new Float(amount).floatValue();
        }
        catch (java.lang.Exception ex)
        {
            System.out.println("Invalid float number, abort operation...");
            return;
        }

        try
        {
            Current current = OTSManager.get_current();
            System.out.println("Beginning a User transaction to  credit an account");
            current.begin();
            try
            {
                Account consumer = _bank.get_account(name_consumer);
                consumer.credit(famount);
            }
            catch (NotExistingAccount nea)
            {
                System.out.println("The requested account does not exist!");
                current.rollback_only();
            }

            current.commit(false);
        }
        catch (Exception e)
        {
            System.err.println("ERROR - " + e);
        }
    }

    /**
     * This operation is used to credit an account.
     */
    private void makeWithdraw()
    {
        System.out.println("");
        System.out.println("- Withrdaw from an Account -");
        System.out.println("-------------------");
        System.out.println("");

        System.out.print("Give the Account name : ");
        String name_debit = input();
        if (name_debit == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        System.out.print("Amount to withdraw : ");
        String amount = input();
        if (amount == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        float famount = 0;
        try
        {
            famount = new Float(amount).floatValue();
        }
        catch (java.lang.Exception ex)
        {
            System.out.println("Invalid float number, abort operation...");
            return;
        }

        try
        {
            Current current = OTSManager.get_current();
            System.out.println("Beginning a User transaction to withdraw from an account");
            current.begin();
            try
            {
                Account debiter = _bank.get_account(name_debit);
                debiter.debit(famount);
            }
            catch (NotExistingAccount nea)
            {
                System.out.println("The requested account does not exist!");
                current.rollback_only();
            }
            current.commit(false);
        }
        catch (Exception e)
        {
            System.err.println("ERROR - " + e);
        }
    }

    /**
     * The client entry point.
     *
     * @param args  The arguments to use for this trailmap example, not used.
     */
    public static void main(String[] args)
    {
        // Define an ORB suitable for use by the JBoss Transactions product ORB portability layer.
        ORB myORB = null;
        // Define an object adapter suitable for use by the JBoss Transactions product ORB portability layer.
        RootOA myOA = null;
        try
        {
            // Initialize the ORB reference using the JBoss Transactions product ORB portability layer.
            myORB = ORB.getInstance("ClientSide");
            // Initialize the object adapter reference using the JBoss Transactions product ORB portability layer.
            myOA = OA.getRootOA(myORB);
            // Initialize the ORB using the JBoss Transactions product ORB portability layer.
            myORB.initORB(args, null);
            // Initialize the object adapter reference using the JBoss Transactions product ORB portability layer.
            myOA.initOA();
        }
        catch (Exception e)
        {
            // The ORB has not been correctly configured!
            // Display as much help as possible to the user track down the configuration problem
            System.err.println("Trailmap Error: ORB Initialisation failed: " + e);
            e.printStackTrace();
            System.exit(0);
        }

        // Obtain a reference to the BankImpl CORBA representation inorder to be able to use it to transactionally
        // invoke banking operations as part of the JBoss Transactions product trailmap
        // Define the reference to invoke
        Bank bank = null;
        // Obtain the reference for bank
        try
        {
            // Read the IOR from file
            java.io.FileInputStream file = new java.io.FileInputStream("ObjectId");
            java.io.InputStreamReader input = new java.io.InputStreamReader(file);
            java.io.BufferedReader reader = new java.io.BufferedReader(input);
            // This contains the IOR
            String stringTarget = reader.readLine();

            // Convert the IOR into a CORBA object
            org.omg.CORBA.Object obj = myORB.orb().string_to_object(stringTarget);
            // Narrow the object into an object implementing <CODE>Hello</CODE>
            System.out.println("Try to convert the obj ref to a Bank object !");
            bank = BankHelper.narrow(obj);
        }
        catch (java.io.IOException ioe)
        {
            // The IOR could not be read from file
            // Display as much help as possible to the user track down the configuration problem
            System.out.println("Trailmap Error: Could not read the IOR of the BankImpl (from file ./ObjectId): " + ioe);
            ioe.printStackTrace();
            System.exit(0);
        }

        // Create the bank client to invoke user-chosen banking operations on the Bank reference
        BankClient client = new BankClient(bank);

        // Start the client requesting user input
        client.start();
    }
}
