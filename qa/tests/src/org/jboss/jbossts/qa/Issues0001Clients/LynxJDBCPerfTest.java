/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
/***************************************************************/
/*                                                             */
/* Name :        LynxJDBCPerfTest.java                         */
/*                                                             */
/* Description : Version of the original test from US company  */
/*               Lynx which has been modified for the QA test  */
/*               suite.                                        */
/*                                                             */
/*               Test performs x inserts using JTA interface   */
/*               and checks time taken. Test passes if         */
/*               throughput if greater than a specified value. */
/*                                                             */
/*               NOTE : Lynx test was written for Oracle.      */
/***************************************************************/
package org.jboss.jbossts.qa.Issues0001Clients;


import org.jboss.jbossts.qa.Utils.JDBCProfileStore;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;

import javax.transaction.TransactionManager;
import java.sql.*;
import java.util.Properties;

public class LynxJDBCPerfTest extends Thread
{

	private java.sql.Connection _dbConn;
	private PreparedStatement _prepStmt;

	static private TransactionManager _txMgr = null;

	static private long _start;
	static private long _end;
	static private double _iterPerSec;
	static private double _expected_iterPerSec = 30;
	static private double _noOfIterations = 1000;

	private long _count = 0;

	// Print out test stats to client err file.
	private void printStats()
	{
		try
		{
			_iterPerSec = ((double) _count) / ((double) (_end - _start)) * 1000.0;
			System.err.println("Messages Received    : " + _count);
			System.err.println("Elapsed time         : " +
					(_end - _start) + " ms");
			System.err.println("Expected Performance : " +
					((int) _expected_iterPerSec) + " iterations/sec");
			System.err.println("Actual Performance   : " +
					((int) _iterPerSec) + " iterations/sec");

		}
		catch (Exception ex)
		{
			System.err.println("Exception occurred in printStats(), ex: " + ex);
		}
	}

	// Setup DB connection and create test table.
	private void initSequeLink(String profileName)
	{
		try
		{

			// REGISTER DRIVER
			int numberOfDrivers = JDBCProfileStore.numberOfDrivers(profileName);
			for (int index = 0; index < numberOfDrivers; index++)
			{
				String driver = JDBCProfileStore.driver(profileName, index);

				Class.forName(driver);
			}

			String databaseURL = JDBCProfileStore.databaseURL(profileName);
			String databaseUser = JDBCProfileStore.databaseUser(profileName);
			String databasePassword = JDBCProfileStore.databasePassword(profileName);
			String databaseDynamicClass = JDBCProfileStore.databaseDynamicClass(profileName);

			// Get DB connection.
			Connection connection;
			if (databaseDynamicClass != null)
			{
				Properties databaseProperties = new Properties();

				databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.userName, databaseUser);
				databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.password, databasePassword);
				databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.dynamicClass, databaseDynamicClass);

				_dbConn = DriverManager.getConnection(databaseURL, databaseProperties);
			}
			else
			{
				_dbConn = DriverManager.getConnection(databaseURL, databaseUser, databasePassword);
			}

			ResultSet rs = null;
			Statement st = _dbConn.createStatement();

			// Create TEST table (drop if already exists).
			rs = st.executeQuery("SELECT * FROM USER_TABLES " +
					"WHERE TABLE_NAME = \'LYNX_TEST\'");
			if (rs.next())
			{
				st.execute("DROP TABLE LYNX_TEST");
				st.execute("DROP SEQUENCE LYNX_TESTSEQ");
			}

			st.execute("CREATE TABLE LYNX_TEST (id number(20) not null," +
					"description varchar2 (2000)," +
					"CONSTRAINT lynx_pk_id PRIMARY KEY (id) " +
					"USING index storage (initial 10k next 10k))");

			st.execute("CREATE SEQUENCE LYNX_TESTSEQ");

			_prepStmt = _dbConn.prepareStatement("insert into LYNX_TEST values (LYNX_TESTSEQ.nextval, ?)");

		}
		catch (Exception e)
		{
			System.err.println(e);
		}
	}

	// Perform actual test - insert x rows and record time taken.
	public void doTest()
	{
		try
		{
			_start = System.currentTimeMillis();
			_txMgr = com.arjuna.ats.jta.TransactionManager.transactionManager();

			for (int i = 0; i < _noOfIterations; ++i)
			{
				_count++;

				_txMgr.begin();

				Statement _Stmt = _dbConn.createStatement();

				String text = "12345678901234567890123456789012345678901234567890";
				int tmpInt = _Stmt.executeUpdate("insert into TEST values (TESTSeq.nextval, '" + text + "')");

				_Stmt.close();

				_txMgr.commit();
			}

			_end = System.currentTimeMillis();

			printStats();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.exit(0);
		}
	}

	public static void main(String[] args) throws Exception
	{

		// Process args.
		if (args.length > 1)
		{
			_expected_iterPerSec = Float.parseFloat(args[1]);
		}

		if (args.length > 2)
		{
			_noOfIterations = Float.parseFloat(args[2]);
		}

		LynxJDBCPerfTest tester = new LynxJDBCPerfTest();

		// Setup and perform test.
		tester.startTransactionManager(args);
		tester.initSequeLink(args[0]);
		tester.doTest();
		tester.shutdownTransactionManager();

		// Output Passed if performance meets expections.
		if ((_iterPerSec) > _expected_iterPerSec)
		{
			System.out.println("Passed");
		}
		else
		{
			System.out.println("Failed");
		}

	}

	private void startTransactionManager(String[] args) throws Exception
	{
		ORBInterface.initORB(args, null);
		OAInterface.initOA();
	}

	private void shutdownTransactionManager()
	{
		OAInterface.shutdownOA();
		ORBInterface.shutdownORB();
	}

}


