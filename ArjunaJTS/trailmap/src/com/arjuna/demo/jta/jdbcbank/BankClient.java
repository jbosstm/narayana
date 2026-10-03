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
package com.arjuna.demo.jta.jdbcbank;

import com.arjuna.ats.jdbc.common.jdbcPropertyManager;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Properties;

/**
 * The <CODE>BankClient</CODE> application is an interactive CLI that allows the user of the JBoss Transactions product
 * to manipulate a database backed bank under transactional control.
 */
public class BankClient
{
    /**
     * A reference to the bank object to invoke updates on.
     */
    private Bank _bank;

    /**
     * Whether the bank object's underlying database should be created.
     */
    static boolean create = false;

    /**
     * Whether the bank object's underlying database should be dropped (if existing) before creation.
     */
    static boolean clean = false;

    /**
     * The oracle database's username.
     */
    static String user = "scott";

    /**
     * The oracle database's password.
     */
    static String password = "tiger";

    /**
     * The host to connect to where the <CODE>Bank</CODE> database is running.
     */
    private static String host = null;

    /**
     * The port to connect on where the <CODE>Bank</CODE> database is running.
     */
    private static String port = null;

    /**
     * The name of the database where the <CODE>Bank</CODE> table resides.
     */
    private static String dbName = null;

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
     * Create a new bank account. This will reserve space in the database for the account and it's initial amount
     * of cash.
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
            // Obtain a reference to the user transaction object inorder to transactionally perform this work
            javax.transaction.UserTransaction userTransaction = com.arjuna.ats.jta.UserTransaction.userTransaction();
            System.out.println("Beginning a User transaction to create account");
            // Begin the transaction so all work now will be executed under transactional control
            userTransaction.begin();
            // Create the account transactionally
            _bank.create_account(name, fbalance);
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
     * Get the current balance of a required bank account. This will look at the database record for the account
     * specified.
     */
    private void getInfo()
    {
        // Display the relevant menu for this choice
        System.out.println("");
        System.out.println("- Get information about an account -");
        System.out.println("------------------------------------");
        System.out.println("");

        // Obtain the name of the account to interrogate
        System.out.print("Name : ");
        String name = input();
        if (name == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        try
        {
            // Obtain a reference to the user transaction object inorder to transactionally perform this work
            javax.transaction.UserTransaction userTransaction = com.arjuna.ats.jta.UserTransaction.userTransaction();
            System.out.println("Beginning a User transaction to get balance");
            // Begin the transaction so all work now will be executed under transactional control
            userTransaction.begin();
            try
            {
                System.out.println("Balance : " + _bank.get_balance(name));
            }
            catch (NotExistingAccount nea)
            {
                // Quite an artifical example as rolling back the read operation is not too useful but
                // indicates how a transaction can be manipulated
                System.out.println("The requested account does not exist!");
                userTransaction.setRollbackOnly();
            }
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
     * This will allow the bank to transactionally move money from one account to another.
     */
    private void makeTransfer()
    {
        // Display the relevant menu for this choice
        System.out.println("");
        System.out.println("- Make a transfer -");
        System.out.println("-------------------");
        System.out.println("");

        // Obtain a name to debit money from
        System.out.print("Take money from : ");
        String debtor = input();
        if (debtor == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        // Obtain the name to credit money to
        System.out.print("Put money to : ");
        String creditor = input();
        if (creditor == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        // Get the amount of money to transfer between the accounts
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

        try
        {
            // Obtain a reference to the user transaction object inorder to transactionally perform this work
            javax.transaction.UserTransaction userTransaction = com.arjuna.ats.jta.UserTransaction.userTransaction();
            System.out.println("Beginning a User transaction to transfer money");
            // Begin the transaction so all work now will be executed under transactional control
            userTransaction.begin();
            try
            {
                _bank.transfer(debtor, creditor, famount);
            }
            catch (NotExistingAccount nea)
            {
                // Indicate to rollback the transfer operation
                System.out.println("The requested account does not exist!");
                userTransaction.setRollbackOnly();
            }
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
     * Credit a specified bank account with the required amount. This method will transactionally update the underlying
     * datastore with the amount of cash to add.
     */
    private void makeCredit()
    {
        // Display the relevant menu for this choice
        System.out.println("");
        System.out.println("- Credit an Account -");
        System.out.println("-------------------");
        System.out.println("");

        // Get the name of the account to credit money to
        System.out.print("Give the Account name : ");
        String name_consumer = input();
        if (name_consumer == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        // Get the amount of money to credit this account with
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
            // Obtain a reference to the user transaction object inorder to transactionally perform this work
            javax.transaction.UserTransaction userTransaction = com.arjuna.ats.jta.UserTransaction.userTransaction();
            System.out.println("Beginning a User transaction to credit an account");
            // Begin the transaction so all work now will be executed under transactional control
            userTransaction.begin();
            try
            {
                _bank.credit(name_consumer, famount);
            }
            catch (NotExistingAccount nea)
            {
                // Indicate to rollback the credit operation
                System.out.println("The requested account does not exist!");
                userTransaction.setRollbackOnly();
            }
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
     * Debit the specified account by the required value. This will transactionally remove funds from the account by
     * executing an update on the underlying JDBC data.
     */
    private void makeWithdraw()
    {
        // Display the relevant menu for this choice
        System.out.println("");
        System.out.println("- Withrdaw from an Account -");
        System.out.println("-------------------");
        System.out.println("");

        // Get the name of the account to withdraw money from
        System.out.print("Give the Account name : ");
        String name_debit = input();
        if (name_debit == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        // Get the amount of money to withdraw
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
            // Obtain a reference to the user transaction object inorder to transactionally perform this work
            javax.transaction.UserTransaction userTransaction = com.arjuna.ats.jta.UserTransaction.userTransaction();
            System.out.println("Beginning a User transaction to withdraw from an account");
            // Begin the transaction so all work now will be executed under transactional control
            userTransaction.begin();
            try
            {
                _bank.debit(name_debit, famount);
            }
            catch (NotExistingAccount nea)
            {
                // Indicate to rollback the credit operation
                System.out.println("The requested account does not exist!");
                userTransaction.setRollbackOnly();
            }
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
     * This is the entry point into the JBoss Transaction product trailmap sample that uses a JDBC database to store
     * the bank accounts repository. At minimum it should be invoked with the "-host [db_host] -port [db_port]" flags.
     *
     * @param args The arguments to configure this run of the JDBC backed trailmap example
     */
    public static void main(String[] args)
    {
        // Parse the command line options to determine any required configuration for the bank application
        for (int i = 0; i < args.length; i++)
        {
            // Where the database resides
            if (args[i].compareTo("-host") == 0)
                host = args[i + 1];
            // Which port the database is listening on
            if (args[i].compareTo("-port") == 0)
                port = args[i + 1];
            // The name of the database
            if (args[i].compareTo("-dbName") == 0)
                dbName = args[i + 1];
            // The username to connect to the database
            if (args[i].compareTo("-username") == 0)
                user = args[i + 1];
            // The password to connect to the database
            if (args[i].compareTo("-password") == 0)
                password = args[i + 1];
            // If any existing table should be dropped
            if (args[i].compareTo("-clean") == 0)
                clean = true;
            // If the table should be created
            if (args[i].compareTo("-create") == 0)
                create = true;
            // Hack our check to ensure that the argument length is OK (indicates host and port
            if (args.length < 4 || args[i].compareTo("-help") == 0)
            {
                // Display help information about the parameters supported by this trailmap example
                System.out.println("Usage: java BankClient [-host] [-port] [-dbName] [-username] [-password] [-clean] [-create] [-help]");
                System.exit(0);
            }
        }
        try
        {
            // Create a new oracle XA DataSource, this datasource is used to indicate the location of the database to
            // store bank account details within
            // Use dynamic class loading to remove any trailmap build time dependency on the Oracle driver
            Class oracleXADataSource = Class.forName("oracle.jdbc.xa.client.OracleXADataSource");
            DataSource ds = (DataSource) oracleXADataSource.newInstance();
            // Use reflection as the datasource API does not provide a setURL method
            Method setUrlMethod = oracleXADataSource.getMethod("setURL", new Class[]{String.class});
            // Use the oracle thin driver rather than oci
            setUrlMethod.invoke(ds, new Object[]{new String("jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName)});

            // The datasource must be able to be resolved by the transaction manager so place it in a file system
            // based JNDI, this implies that other machines can see this file system location
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
            env.put(Context.PROVIDER_URL, "file:/tmp/JNDI");
            InitialContext ctx = new InitialContext(env);
            // This is the object to use so rebind rather than bind, some JNDI implementations would protect this
            // with the requirement to add <CODE>Context.SECURITY_CREDENTIALS</CODE> or the like
            ctx.rebind("jdbc/DB", ds);
        }
        catch (Exception ex)
        {
            System.err.println("Cannot create the Oracle datasource or bind it into JNDI");
            ex.printStackTrace();
            System.exit(0);
        }
        // Initialize the property manager with the correct JNDI credentials to use to log in to JNDI
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
        properties.put(Context.PROVIDER_URL, "file:/tmp/JNDI");
        jdbcPropertyManager.getJDBCEnvironmentBean().setJndiProperties(properties);

        // Create a local representation of a bank and then create a bank client to interact with it
        Bank bank = new Bank();
        BankClient client = new BankClient(bank);
        // Start the bank client CLI
        client.start();
    }
}

