/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
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

import com.arjuna.ats.jdbc.*;

import java.util.Properties;
import java.sql.*;

import com.arjuna.mwlabs.testframework.unittest.*;

public class JDBC2Test extends Test
{
    public static final int CLOUDSCAPE = 0;
    public static final int ORACLE = 1;
    public static final int SEQUELINK = 2;
    public static final int JNDI = 3;

	protected Connection conn = null;
	protected Connection conn2 = null;
	protected boolean commit = false;
	protected boolean nested = false;
	protected boolean reuseconn = false;
	protected Properties dbProperties = null;
	protected String url = null;

	protected void setup(String[] args)
    {
        int dbType = JNDI;
        String user = null;//"test";
        String password = null;//"test";
        String dynamicClass = null; //"com.arjuna.ats.internal.jdbc.drivers.jndi";
        String host = null;
        String port = null;

        for (int i = 0; i < args.length; i++)
        {
            if (args[i].compareTo("-oracle") == 0)
            {
                dbType = ORACLE;
                user = "tester";
                password = "tester";
                dynamicClass = "com.arjuna.ats.internal.jdbc.drivers.oracle_8_1_6";
            }
            if (args[i].equalsIgnoreCase("-jndi"))
            {
                dbType = JNDI;
                dynamicClass = "com.arjuna.ats.internal.jdbc.drivers.jndi";
            }
            if (args[i].compareTo("-sequelink") == 0)
            {
                dbType = SEQUELINK;
                user = "tester";
                password = "tester";
                dynamicClass = "com.arjuna.ats.internal.jdbc.drivers.sequelink_5_1";
            }
            if (args[i].compareTo("-host") == 0)
                host = args[i + 1];
            if (args[i].compareTo("-port") == 0)
                port = args[i + 1];
            if (args[i].compareTo("-commit") == 0)
                commit = true;
            if (args[i].compareTo("-nested") == 0)
                nested = true;
            if (args[i].compareTo("-reuseconn") == 0)
                reuseconn = true;
            if (args[i].compareTo("-url") == 0)
                url = args[i + 1];
            if (args[i].compareTo("-dynamicClass") == 0)
                dynamicClass = args[i + 1];
	    if (args[i].equalsIgnoreCase("-user"))
		user = args[i+1];
	    if (args[i].equalsIgnoreCase("-password"))
		password = args[i+1];
            if (args[i].compareTo("-help") == 0)
            {
                System.out.println("Usage: JDBCTest2 [-commit] [-nested] [-reuseconn] [-oracle] [-sequelink] [-cloudscape] [-url] [-dynamicClass]");
                System.exit(0);
            }
        }

        if (url == null)
        {
            switch (dbType)
            {
                case CLOUDSCAPE:
                    url = "jdbc:arjuna:cloudscape:mysql;create=true";
                    break;
                case SEQUELINK:
                    {
                        url = "jdbc:arjuna:sequelink://" + host;

                        if (port != null)
                            url = url + ":" + port;
                    }
                    break;
                case ORACLE:
                    {
                        if (port == null)
                            url = "jdbc:arjuna:oracle:thin:@" + host + ":JDBCTest";
                        else
                            url = "jdbc:arjuna:oracle:thin:@" + host + ":" + port + ":JDBCTest";
                    }
                    break;
                case JNDI:
                    System.err.println("JNDI URL not specified");
                    assertFailure();
                    break;
                default:
                    // noop
            }

        }

        Properties p = System.getProperties();

        switch (dbType)
        {
            case CLOUDSCAPE:
                p.put("jdbc.drivers", "COM.cloudscape.core.JDBCDriver");
                break;
            case ORACLE:
                p.put("jdbc.drivers", "oracle.jdbc.driver.OracleDriver");
                break;
            case SEQUELINK:
                p.put("jdbc.drivers", "com.merant.sequelink.jdbc.SequeLinkDriver");
                break;
        }

        System.setProperties(p);

        try
        {
            DriverManager.registerDriver(new TransactionalDriver());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            assertFailure();
        }

        dbProperties = new Properties();

            System.out.println("\nCreating connection to database: " + url);

	    if ( user != null )
            	dbProperties.put(TransactionalDriver.userName, user);

	    if ( password != null )
            	dbProperties.put(TransactionalDriver.password, password);

	    if ( dynamicClass != null )
            	dbProperties.put(TransactionalDriver.dynamicClass, dynamicClass);

		try
		{
			conn = DriverManager.getConnection(url, dbProperties);
            conn2 = DriverManager.getConnection(url, dbProperties);
		} catch(SQLException e) {
			e.printStackTrace();
			assertFailure();
		}
	}

	public void run(String[] args)
	{
		setup(args);

		Statement stmt = null;  // non-tx statement
		Statement stmtx = null;	// will be a tx-statement

		if(conn == null || conn2 == null) {
			return;
		}

		try
		{
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
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            assertFailure();
        }

        javax.transaction.UserTransaction tx = com.arjuna.ats.jta.UserTransaction.userTransaction();

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

                try
                {
                    res1 = stmtx.executeQuery("SELECT * FROM test_table");
                }
                catch (Exception e)
                {
                    e.printStackTrace(System.err);
                    assertFailure();
                }

                try
                {
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
                }
                catch (Exception e)
                {
                    e.printStackTrace(System.err);
                    assertFailure();
                }

                System.out.println("\nAdding entries to table 2.");

                stmtx.executeUpdate("INSERT INTO test_table2 (a, b) VALUES (3,4)");

                try
                {
                    res1 = stmtx.executeQuery("SELECT * FROM test_table2");
                }
                catch (Exception e)
                {
                    e.printStackTrace(System.err);
                    assertFailure();
                }

                System.out.println("\nInspecting table 2.");

                try
                {
                    int rowCount = 0;

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
                catch (Exception e)
                {
                    e.printStackTrace(System.err);
                    assertFailure();
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

                assertFailure();
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

                assertFailure();
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

            try
            {
                res2 = stmtx.executeQuery("SELECT * FROM test_table");
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);
                assertFailure();
            }

            try
            {
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

                assertSuccess();
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);
                assertFailure();
            }

            tx.commit();

            tx.begin();

            if (!reuseconn)
            {
                conn = DriverManager.getConnection(url, dbProperties);
            }

            System.out.println("\nNow checking state of table 2.");

            stmtx = conn.createStatement();

            try
            {
                res2 = stmtx.executeQuery("SELECT * FROM test_table2");
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);
                assertFailure();
            }

            try
            {
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
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);
                assertFailure();
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

            assertFailure();
        }

        try
        {
            conn.close();
        }
        catch (Exception e)
        {
        }

        System.out.println("Test completed successfully.");
    }

    public static void main(String[] args)
    {
        JDBC2Test test = new JDBC2Test();
        test.initialise(null,null,args,new LocalHarness());
        test.runTest();
    }
}
