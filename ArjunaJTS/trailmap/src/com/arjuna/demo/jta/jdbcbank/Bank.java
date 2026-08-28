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
 * $Id: Bank.java 2342 2006-03-30 13:06:17Z tjenkinson $
 */

package com.arjuna.demo.jta.jdbcbank;

import com.arjuna.ats.jdbc.TransactionalDriver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;


/**
 * This is the JDBC implementation of the Bank class used in the JBoss Transactions product trailmap. It is used to
 * create and clean the database that the example uses as well as perform the banking operations that all Bank
 * implementations in the trailmap provide, such as creating and accessing an account.
 */
public class Bank
{
    /**
     * An implementation of an SQL driver which can be used for transactional access to a database.
     */
    private TransactionalDriver arjunaJDBC2Driver;

    /**
     * The properties are used by the connection to log in to the database.
     */
    private static Properties dbProperties;

    /**
     * Creates a new JDBC-backed bank object.
     */
    public Bank()
    {
        try
        {
            // Register the driver to use
            DriverManager.registerDriver(new TransactionalDriver());

            // Populate the connections properties with the required security credentials
            dbProperties = new Properties();
            dbProperties.put(TransactionalDriver.userName, BankClient.user);
            dbProperties.put(TransactionalDriver.password, BankClient.password);

            // Create the transactional driver to use to create the database
            arjunaJDBC2Driver = new TransactionalDriver();

            // Create the table (will drop an existing table if it already exists)
            create_table();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Create a new bank account. This will reserve space in the database for the account and it's initial amount
     * of cash.
     *
     * @param accountName       The name of the account to open.
     * @param openingBalance    The opening balance of the account.
     */
    public void create_account(String accountName, float openingBalance)
    {
        try
        {
            // Obtain a connection to the database using the transactional driver
            Connection connection = arjunaJDBC2Driver.connect("jdbc:arjuna:jdbc/DB", dbProperties);

            // Create a statement and execute an update to the database to reserve space for the new bank account
            Statement stmtx = connection.createStatement();  // tx statement
            stmtx.executeUpdate("INSERT INTO accounts (name, value) VALUES ('" + accountName + "'," + openingBalance + ")");
        }
        catch (SQLException e)
        {
            // We should set the rollback_only
            System.out.println("SQL Exception  ...........");
            e.printStackTrace();
        }
    }

    /**
     * Get the current balance of a required bank account. This will look at the database record for the account
     * specified.
     *
     * @param accountName           The name of the account to view.
     * @return                      The balance of the requested account.
     *
     * @throws NotExistingAccount   If the account does not exist.
     */
    public float get_balance(String accountName) throws NotExistingAccount
    {
        float theBalance = 0;
        try
        {
            // Obtain a connection to the database using the transactional driver
            Connection connection = arjunaJDBC2Driver.connect("jdbc:arjuna:jdbc/DB", dbProperties);

            // Create a statement and execute a query on the database to determine the balance of the account
            Statement stmtx = connection.createStatement();  // tx statement
            ResultSet rs = stmtx.executeQuery("SELECT value from accounts WHERE name = '" + accountName + "'");

            // Keep iterating through the result set, though there should only be one value
            while (rs.next())
            {
                theBalance = rs.getFloat("value");
            }
        }

        catch (SQLException e)
        {
            // We should set the rollback_only
            System.out.println("SQL Exception  ...........");
            e.printStackTrace();
            throw new NotExistingAccount("The Account requested does not exist");
        }

        return theBalance;
    }

    /**
     * Debit the specified account by the required value. This will transactionally remove funds from the account by
     * executing an update on the underlying JDBC data.
     *
     * @param accountName           The name of the account to debit from.
     * @param debitValue            The amount of cash to debit from the account.
     *
     * @throws NotExistingAccount   If the account does not exist.
     */
    public void debit(String accountName, float debitValue) throws NotExistingAccount
    {
        try
        {
            // Obtain a connection to the database using the transactional driver
            Connection connection = arjunaJDBC2Driver.connect("jdbc:arjuna:jdbc/DB", dbProperties);

            // Create a statement and execute an update to the database to debit the required account
            Statement stmtx = connection.createStatement();  // tx statement
            stmtx.executeUpdate("UPDATE accounts Set value = value - " + debitValue + " WHERE name = '" + accountName + "'");
        }
        catch (SQLException e)
        {
            // We should set the rollback_only
            System.out.println("SQL Exception  ...........");
            e.printStackTrace();
            throw new NotExistingAccount("The Account requested does not exist");
        }
    }

    /**
     * Credit a specified bank account with the required amount. This method will transactionally update the underlying
     * datastore with the amount of cash to add.
     *
     * @param accountName           The name of the account to credit.
     * @param creditAmount          The amount of cash to credit the account with.
     *
     * @throws NotExistingAccount   If the account does not exist.
     */
    public void credit(String accountName, float creditAmount) throws NotExistingAccount
    {
        try
        {
            // Obtain a connection to the database using the transactional driver
            Connection conne = arjunaJDBC2Driver.connect("jdbc:arjuna:jdbc/DB", dbProperties);

            // Create a statement and execute an update to the database to credit the required account
            Statement stmtx = conne.createStatement();  // tx statement
            stmtx.executeUpdate("UPDATE accounts Set value = value + " + creditAmount + " WHERE name = '" + accountName + "'");
        }
        catch (SQLException e)
        {
            //We should set the rollback_only
            System.out.println("SQL Exception  ...........");
            e.printStackTrace();
            throw new NotExistingAccount("The Account requested does not exist");
        }
    }

    /**
     * This will allow the bank to transactionally move money from one account to another.
     *
     * @param fromAccount           The account to remove money from.
     * @param toAccount             The account to add money to.
     * @param transferAmount                The amount of money to transfer.
     *
     * @throws NotExistingAccount   If the account does not exist already.
     */
    public void transfer(String fromAccount, String toAccount, float transferAmount) throws NotExistingAccount
    {
        try
        {
            // Obtain a connection to the database using the transactional driver
            Connection connection = arjunaJDBC2Driver.connect("jdbc:arjuna:jdbc/DB", dbProperties);

            // Create a statement and execute an update to the database to credit the required account and debit the
            // other account
            Statement stmtx = connection.createStatement();  // tx statement
            stmtx.executeUpdate("UPDATE accounts Set value = value - " + transferAmount + " WHERE name = '" + fromAccount + "'");
            stmtx.executeUpdate("UPDATE accounts Set value = value + " + transferAmount + " WHERE name = '" + toAccount + "'");
        }
        catch (SQLException e)
        {
            //We should set the rollback_only
            System.out.println("SQL Exception  ...........");
            e.printStackTrace();
            throw new NotExistingAccount("The Account requested does not exist");
        }
    }

    /**
     * This utility method will create the bank account table if it does not already exist. If it does exist and the
     * client has requested that the table be cleaned then the table is first dropped.
     */
    private void create_table()
    {
        try
        {
            // Obtain a connection to the database using the transactional driver
            System.out.println("\nCreating connection to database: ");

            // A JDBC connection is used to create and drop (where neccessary) the underlying database table
            Connection connection = arjunaJDBC2Driver.connect("jdbc:arjuna:jdbc/DB", dbProperties);

            Statement stmt = connection.createStatement();  // non-tx statement
            if (BankClient.clean)
            {
                System.out.println("\nDrop the table ");
                stmt.executeUpdate("DROP TABLE accounts");
            }

            if (BankClient.clean || BankClient.create)
            {
                stmt.executeUpdate("CREATE TABLE accounts (name VARCHAR(10) NOT NULL UNIQUE, value REAL)");
                
                if (BankClient.clean)
                	BankClient.clean = false;
                
                if (BankClient.create)
                	BankClient.create = false;
            }

            stmt.close();
            connection.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }
}



