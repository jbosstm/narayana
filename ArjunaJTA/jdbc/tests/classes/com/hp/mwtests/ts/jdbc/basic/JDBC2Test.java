/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JDBC2Test.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jdbc.basic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.XADataSource;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;
import org.h2.Driver;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.internal.jdbc.DynamicClass;
import com.arjuna.ats.jdbc.TransactionalDriver;

public class JDBC2Test
{
	protected Connection conn = null;
	protected boolean commit = true;
	protected boolean nested = false;
	protected boolean reuseconn = false;
	protected Properties dbProperties = null;
	protected String url = null;

	@Test
    public void testBC() throws SystemException, NotSupportedException, SQLException, HeuristicRollbackException, HeuristicMixedException, RollbackException {

        try (Statement stmt = conn.createStatement() ) {
            try {
                stmt.executeUpdate("DROP TABLE test_table");
            } catch (Exception e) {
                // Ignore
            } finally {
                stmt.executeUpdate("CREATE TABLE test_table (a INTEGER,b INTEGER)");
            }
        }

        jakarta.transaction.TransactionManager tx = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tx.begin();

        TransactionSynchronizationRegistryImple transactionSynchronizationRegistryImple = new TransactionSynchronizationRegistryImple();
        transactionSynchronizationRegistryImple.registerInterposedSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {
                try {
                    ResultSet res = conn.prepareStatement("SELECT * FROM test_table").executeQuery();
                    int rowCount1 = 0;
                    while (res.next()) {
                        rowCount1++;
                    }
                    conn.createStatement().execute("INSERT INTO test_table (a, b) VALUES (1,2)");
                    res = conn.prepareStatement("SELECT * FROM test_table").executeQuery();
                    int rowCount2 = 0;
                    while (res.next()) {
                        rowCount2++;
                    }
                    conn.close();
                    if (rowCount2 != rowCount1 + 1) {
                        fail("Number of rows = " + rowCount2 + ", test was expecting " + rowCount1 + 1);
                    }

                } catch (Exception e) {
                    fail(e.getMessage());
                }
            }

            @Override
            public void afterCompletion(int status) {

            }
        });


        tx.commit();
    }

    @Before
	public void setup() throws Exception
    {
        url = "jdbc:arjuna:";
        Properties p = System.getProperties();
        p.put("jdbc.drivers", Driver.class.getName());
        
        System.setProperties(p);
        DriverManager.registerDriver(new TransactionalDriver());

        dbProperties = new Properties();
        
        JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:./h2/foo");
        dbProperties.put(TransactionalDriver.XADataSource, ds);
        dbProperties.put(TransactionalDriver.poolConnections, "false");
		
		conn = DriverManager.getConnection(url, dbProperties);
	}

    @Test
	public void test() throws Exception
	{
		Statement stmt = null;  // non-tx statement
		Statement stmtx = null;	// will be a tx-statement

			stmt = conn.createStatement();  // non-tx statement

            try
            {
                stmt.executeUpdate("DROP TABLE test_table");
                stmt.executeUpdate("DROP TABLE test_table2");
	    }
            catch (Exception e)
	    {
		// Ignore
	    }

                stmt.executeUpdate("CREATE TABLE test_table (a INTEGER,b INTEGER)");
                stmt.executeUpdate("CREATE TABLE test_table2 (a INTEGER,b INTEGER)");

        jakarta.transaction.UserTransaction tx = com.arjuna.ats.jta.UserTransaction.userTransaction();

        try
        {
            System.out.println("Starting top-level transaction.");


            tx.begin();

            if (nested)
            {
                System.out.println("Starting nested transaction.");

                tx.begin();
            }

            stmtx = conn.createStatement(); // will be a tx-statement

            try
            {
                System.out.println("\nAdding entries to table 1.");

                stmtx.executeUpdate("INSERT INTO test_table (a, b) VALUES (1,2)");

                ResultSet res1 = null;

                System.out.println("\nInspecting table 1.");

                    res1 = stmtx.executeQuery("SELECT * FROM test_table");

                    int rowCount = 0;

                    while (res1.next())
                    {
                        System.out.println("Column 1: " + res1.getInt(1));
                        System.out.println("Column 2: " + res1.getInt(2));

                        rowCount++;
                    }

                    if ( rowCount != 1)
                    {
                        throw new Exception("Number of rows = "+rowCount+", test was expecting 1");
                    }

                System.out.println("\nAdding entries to table 2.");

                stmtx.executeUpdate("INSERT INTO test_table2 (a, b) VALUES (3,4)");

                    res1 = stmtx.executeQuery("SELECT * FROM test_table2");

                System.out.println("\nInspecting table 2.");

                    rowCount = 0;

                    while (res1.next())
                    {
                        System.out.println("Column 1: " + res1.getInt(1));
                        System.out.println("Column 2: " + res1.getInt(2));
                        rowCount++;
                    }

                    if ( rowCount != 1 )
                    {
                        throw new Exception("Row count = "+rowCount+", test was expecting 1");
                    }
            }
            catch (SQLException e)
            {
                e.printStackTrace(System.err);

                tx.rollback();

                if (nested)
                    tx.rollback();

                try
                {
                    conn.close();
                }
                catch (Exception ex)
                {
                }

                fail();
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);

                if (nested)
                    tx.rollback();

                tx.rollback();

                try
                {
                    conn.close();
                }
                catch (Exception ex)
                {
                }

                fail();
            }

            System.out.print("\nNow attempting to ");

            if (commit)
            {
                System.out.print("commit ");

                tx.commit();
            }
            else
            {
                System.out.print("rollback ");

                tx.rollback();
            }

            System.out.println("changes.");

            System.out.println("\nNow checking state of table 1.");

			tx.begin();

            if (!reuseconn)
            {
				conn = DriverManager.getConnection(url, dbProperties);
            }

            stmtx = conn.createStatement();

            ResultSet res2 = null;

                res2 = stmtx.executeQuery("SELECT * FROM test_table");

                int rowCount = 0;
                while (res2.next())
                {
                    System.out.println("Column 1: " + res2.getInt(1));
                    System.out.println("Column 2: " + res2.getInt(2));
                    rowCount++;
                }

                if ( commit )
                {
                    if ( rowCount != 1 )
                    {
                        throw new Exception("Committed row count = "+rowCount+", test expected 1");
                    }
                }
                else
                {
                    if ( rowCount != 0 )
                    {
                        throw new Exception("Rolledback row count = "+rowCount+", test expected 0");
                    }
                }

            conn.close(); // connections must be closed before committing a JTA transaction
            tx.commit();

            tx.begin();

            assertTrue(conn.isClosed());

            if (!reuseconn)
            {
                conn = DriverManager.getConnection(url, dbProperties);
            }

            System.out.println("\nNow checking state of table 2.");

            stmtx = conn.createStatement();

                res2 = stmtx.executeQuery("SELECT * FROM test_table2");

                rowCount = 0;

                while (res2.next())
                {
                    System.out.println("Column 1: " + res2.getInt(1));
                    System.out.println("Column 2: " + res2.getInt(2));
                    rowCount++;
                }

                if ( commit )
                {
                    if ( rowCount != 1 )
                    {
                        throw new Exception("Committed row count = "+rowCount+", test expected 1");
                    }
                }
                else
                {
                    if ( rowCount != 0 )
                    {
                        throw new Exception("Rolledback row count = "+rowCount+", test expected 0");
                    }
                }

            tx.commit();
        }
        catch (Exception ex)
        {
            try
            {
                tx.rollback();
            }
            catch (Exception exp)
            {
            }

            fail();
        }

        try
        {
            conn.close();
        }
        catch (Exception e)
        {
        }
    }

    @Test
    public void testCloseUnused() throws Exception {

        conn.close();

        assertTrue(conn.isClosed());
    }

    @Test
    public void testCloseUsed() throws Exception {
        conn.close();
        jakarta.transaction.UserTransaction tx = com.arjuna.ats.jta.UserTransaction.userTransaction();

        assertTrue(conn.isClosed());

        tx.begin();
        conn.createStatement().close();

        assertFalse(conn.isClosed());

        conn.close();
        tx.commit();

        assertTrue(conn.isClosed());
    }
}
