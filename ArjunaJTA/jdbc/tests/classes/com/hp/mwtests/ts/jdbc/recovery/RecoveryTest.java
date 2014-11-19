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

package com.hp.mwtests.ts.jdbc.recovery;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.h2.Driver;
import org.h2.jdbcx.JdbcDataSource;
import org.jboss.byteman.agent.Transformer;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jdbc.TransactionalDriver;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;

@RunWith(BMUnitRunner.class)
public class RecoveryTest {
	@Test
	@BMRule(name = "throw exception", targetClass = "org.h2.jdbcx.JdbcXAConnection", targetMethod = "commit", action = "throw new javax.transaction.xa.XAException(javax.transaction.xa.XAException.XAER_RMFAIL)", targetLocation = "AT ENTRY")
	public void test() throws Exception {
		String url = "jdbc:arjuna:";
		Properties p = System.getProperties();
		p.put("jdbc.drivers", Driver.class.getName());

		System.setProperties(p);
		DriverManager.registerDriver(new TransactionalDriver());

		Properties dbProperties = new Properties();

		final JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:./h2/foo");// mem:test;DB_CLOSE_DELAY=-1");
		dbProperties.put(TransactionalDriver.XADataSource, ds);

		// 0. Setup tables
		{
			Connection conn = DriverManager.getConnection(url, dbProperties);

			Statement stmt = conn.createStatement(); // non-tx statement

			try {
				stmt.executeUpdate("DROP TABLE test_table");
			} catch (SQLException e) {
				if (e.getErrorCode() != 42102) {
					throw e;
				}
			} finally {
				stmt.executeUpdate("CREATE TABLE test_table (a INTEGER,b INTEGER)");
				stmt.close();
			}

			conn.close();
		}

		// 1. Leave a Xid in the DB
		{

			Connection conn = DriverManager.getConnection(url, dbProperties);
			javax.transaction.TransactionManager tx = com.arjuna.ats.jta.TransactionManager
					.transactionManager();

			tx.begin();
			tx.getTransaction().enlistResource(new DummyXAResource());

			Statement stmtx = conn.createStatement(); // will be a tx-statement

			stmtx.executeUpdate("INSERT INTO test_table (a, b) VALUES (1,2)");

			tx.commit();
			// conn.close();
		}

		// 2. Check its not in the DB already
		{
			Connection conn = DriverManager.getConnection(url, dbProperties);
			Statement stmt = conn.createStatement(); // non-tx statement

			ResultSet res1 = stmt.executeQuery("SELECT * FROM test_table");

			int rowCount = 0;

			while (res1.next()) {
				rowCount++;
			}

			assertTrue(rowCount == 0);
			conn.close();
		}

		// 3. Perform recovery
		{
			XARecoveryModule xarm = new XARecoveryModule();
			xarm.addXAResourceRecoveryHelper(new XAResourceRecoveryHelper() {

				@Override
				public boolean initialise(String p) throws Exception {
					return false;
				}

				@Override
				public XAResource[] getXAResources() throws Exception {

					return new XAResource[] { ds.getXAConnection()
							.getXAResource() };
				}
			});

			RecoveryManager manager = RecoveryManager.manager();
			manager.addModule(xarm);
			AtomicActionRecoveryModule aarm = new AtomicActionRecoveryModule();

			aarm.periodicWorkFirstPass();
			Transformer.disableTriggers(true);
			aarm.periodicWorkSecondPass();
			Transformer.enableTriggers(true);
		}

		// 4. See if its there now
		{

			Connection conn = DriverManager.getConnection(url, dbProperties);
			Statement stmt = conn.createStatement(); // non-tx statement

			ResultSet res1 = stmt.executeQuery("SELECT * FROM test_table");

			int rowCount = 0;

			while (res1.next()) {
				rowCount++;
			}

			assertTrue(rowCount == 1);
			conn.close();
		}

	}

	private class DummyXAResource implements XAResource {

		@Override
		public void commit(Xid arg0, boolean arg1) throws XAException {
		}

		@Override
		public void end(Xid arg0, int arg1) throws XAException {
		}

		@Override
		public void forget(Xid arg0) throws XAException {
		}

		@Override
		public int getTransactionTimeout() throws XAException {
			return 0;
		}

		@Override
		public boolean isSameRM(XAResource arg0) throws XAException {
			return false;
		}

		@Override
		public int prepare(Xid arg0) throws XAException {
			return 0;
		}

		@Override
		public Xid[] recover(int arg0) throws XAException {
			return null;
		}

		@Override
		public void rollback(Xid arg0) throws XAException {
		}

		@Override
		public boolean setTransactionTimeout(int arg0) throws XAException {
			return false;
		}

		@Override
		public void start(Xid arg0, int arg1) throws XAException {
		}
	}
}
