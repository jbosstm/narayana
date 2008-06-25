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
 * (C) 2006,
 * @author JBoss Inc.
 *
 * $Id$
 */

package com.hp.mwtests.ts.jdbc.basic;

import org.jboss.dtf.testframework.unittest.LocalHarness;
import org.jboss.dtf.testframework.unittest.Test;

import java.sql.*;

/**
 * Exercises the JDBC3.0 specific methods on the transactional JDBC wrapper.
 *
 * Note: some drivers dont fully support JDBC 3.0, so some of these
 * tests may fail though no fault of our own.
 */
public class JDBC3Test extends JDBC2Test
{
	private void testHoldability()
	{
		System.out.println("testHoldability...");

		try
		{
			conn.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
			if(!(conn.getHoldability() == ResultSet.HOLD_CURSORS_OVER_COMMIT))
			{
				System.err.println("holdability incorrect, set "+ResultSet.HOLD_CURSORS_OVER_COMMIT+" but read back "+conn.getHoldability());
				assertFailure();
			}
			conn.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
			if(!(conn.getHoldability() == ResultSet.CLOSE_CURSORS_AT_COMMIT))
			{
				System.err.println("holdability incorrect, set "+ResultSet.CLOSE_CURSORS_AT_COMMIT+" but read back "+conn.getHoldability());
				assertFailure();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace(System.err);
			assertFailure();
		}
	}

	private void testSavepointNoTx()
	{
		System.out.println("testSavepointNoTx...");

		// savepoint methods should work if we don't have an XA tx

		try
		{
			conn.setAutoCommit(false);

			Savepoint savepoint = conn.setSavepoint();
			Savepoint mySavepoint = conn.setSavepoint("mySavepoint");
			conn.rollback(mySavepoint);
			conn.releaseSavepoint(savepoint);
		}
		catch (SQLException e)
		{
			e.printStackTrace(System.err);
			assertFailure();
		}
	}

	private void testSavepointTx()
	{
		System.out.println("testSavepointTx...");

		// it is not permitted to use savepoint methods if we have an XA tx...

		javax.transaction.UserTransaction tx = com.arjuna.ats.jta.UserTransaction.userTransaction();

		try
		{
			tx.begin();
		}
		catch(Exception e)
		{
			e.printStackTrace(System.err);
			assertFailure();
		}

		boolean gotExpectedException = false;
		Savepoint savepoint = null;
		Savepoint mySavepoint = null;

		try
		{
			savepoint = conn.setSavepoint();
		}
		catch(SQLException e) {
			gotExpectedException = true;
		}

		if(!gotExpectedException) {
			System.err.println("Failed to get expected exception from setSavepoint inside tx");
			assertFailure();
		}

		gotExpectedException = false;

		try
		{
			mySavepoint = conn.setSavepoint("mySavepoint");
		}
		catch(SQLException e) {
			gotExpectedException = true;
		}

		if(!gotExpectedException) {
			System.err.println("Failed to get expected exception from setSavepoint(String) inside tx");
			assertFailure();
		}

		gotExpectedException = false;

		try
		{
			conn.rollback(mySavepoint);
		}
		catch(SQLException e) {
			gotExpectedException = true;
		}

		if(!gotExpectedException) {
			System.err.println("Failed to get expected exception from rollback(String) inside tx");
			assertFailure();
		}

		try
		{
			conn.releaseSavepoint(savepoint);
		}
		catch (SQLException e)
		{
			gotExpectedException = true;
		}

		if(!gotExpectedException) {
			System.err.println("Failed to get expected exception from releaseSavepoint(String) inside tx");
			assertFailure();
		}

		try
		{
			tx.rollback();
		}
		catch(Exception e)
		{
			e.printStackTrace(System.err);
			assertFailure();
		}
	}

	private void testStatements()
	{
		System.out.println("testStatements...");

		javax.transaction.UserTransaction tx = com.arjuna.ats.jta.UserTransaction.userTransaction();

		Statement stmt = null;

		try
		{
			tx.begin();
		}
		catch(Exception e)
		{
			e.printStackTrace(System.err);
			assertFailure();
		}

		try
		{
			conn = DriverManager.getConnection(url, dbProperties);

			stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
			stmt.close();

			stmt = conn.prepareStatement("SELECT a FROM b", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
			stmt.close();

			stmt = conn.prepareCall("SELECT a FROM b", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
			stmt.close();

			stmt = conn.prepareStatement("SELECT a FROM b", Statement.NO_GENERATED_KEYS);
			stmt.close();

			stmt = conn.prepareStatement("SELECT a FROM b", new int[] {1});
			stmt.close();

			stmt = conn.prepareStatement("SELECT a FROM b", new String[] {"a"});
			stmt.close();

			conn.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace(System.err);
			assertFailure();
		}

		try
		{
			tx.rollback();
		}
		catch(Exception e)
		{
			e.printStackTrace(System.err);
			assertFailure();
		}
	}


	public void run(String[] args) {
		setup(args);

		testHoldability();
		testSavepointNoTx();
		testSavepointTx();
		testStatements();

		if(getTestResult() == Test.UNCERTAIN) {
			assertSuccess();
		}
	}

	// java com.hp.mwtests.ts.jdbc.basic.JDBC3Test -oracle -url jdbc:arjuna:oracle:thin:@localhost:1521:orcl -user test -password testpass
	public static void main(String[] args)
    {
        JDBC3Test test = new JDBC3Test();
        test.initialise(null,null,args,new LocalHarness());
        test.runTest();
    }
}
