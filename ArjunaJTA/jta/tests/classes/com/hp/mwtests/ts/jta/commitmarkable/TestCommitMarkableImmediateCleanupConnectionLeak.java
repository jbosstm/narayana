/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.commitmarkable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Test;

import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

public class TestCommitMarkableImmediateCleanupConnectionLeak extends TestCommitMarkableResourceBase {

	private boolean previousImmediateCleanup;

	@After
	public void restoreImmediateCleanup() {
		BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class)
				.setPerformImmediateCleanupOfCommitMarkableResourceBranches(previousImmediateCleanup);
	}

	@Test
	public void testPrepareConnectionClosedWhenImmediateCleanupEnabled() throws Exception {
		JTAEnvironmentBean jtaEnvironmentBean = BeanPopulator
				.getDefaultInstance(JTAEnvironmentBean.class);
		previousImmediateCleanup = jtaEnvironmentBean
				.isPerformImmediateCleanupOfCommitMarkableResourceBranches();
		jtaEnvironmentBean
				.setPerformImmediateCleanupOfCommitMarkableResourceBranches(true);

		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setURL("jdbc:h2:mem:CMRImmediateCleanupLeak;DB_CLOSE_DELAY=-1");

		try (Connection setupConnection = dataSource.getConnection()) {
			Utils.createTables(setupConnection);
		}

		jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
				.transactionManager();

		try (Connection localJDBCConnection = dataSource.getConnection();
				Statement insertStatement = localJDBCConnection.createStatement();
				BegunTransaction tx = BegunTransaction.begin(tm)) {
			localJDBCConnection.setAutoCommit(false);
			CloseTrackingConnectableResource nonXAResource = new CloseTrackingConnectableResource(
					localJDBCConnection, dataSource);
			tm.getTransaction().enlistResource(nonXAResource);

			tm.getTransaction().enlistResource(new DummyXAResource());

			insertStatement.execute("INSERT INTO foo (bar) VALUES (1)");

			tx.commit();

			assertEquals("prepare and afterCompletion should each obtain a Connection",
					2, nonXAResource.obtainedCount());
			assertTrue("the prepare Connection must be closed after a successful 2PC commit",
					nonXAResource.allClosed());

			try (Connection verificationConnection = dataSource.getConnection();
					Statement statement = verificationConnection.createStatement();
					ResultSet result = statement.executeQuery("select count(*) from xids")) {
				assertTrue("xids count query must return a row", result.next());
				assertEquals("immediate cleanup should DELETE the xids row", 0,
						result.getInt(1));
			}

			assertFalse(localJDBCConnection.isClosed());
		}
	}

	private static final class BegunTransaction implements AutoCloseable {
		private final jakarta.transaction.TransactionManager tm;
		private boolean completed;

		static BegunTransaction begin(jakarta.transaction.TransactionManager tm)
				throws Exception {
			tm.begin();
			return new BegunTransaction(tm);
		}

		private BegunTransaction(jakarta.transaction.TransactionManager tm) {
			this.tm = tm;
		}

		void commit() throws Exception {
			tm.commit();
			completed = true;
		}

		@Override
		public void close() {
			if (!completed) {
				try {
					tm.rollback();
				} catch (Exception e) {
					// commit may already have aborted the transaction
				}
			}
		}
	}

	private static final class CloseTrackingConnectableResource extends JDBCConnectableResource {

		private final DataSource dataSource;
		private final List<AtomicBoolean> closedFlags = new ArrayList<AtomicBoolean>();

		CloseTrackingConnectableResource(Connection businessConnection, DataSource dataSource)
				throws SQLException {
			super(businessConnection);
			this.dataSource = dataSource;
		}

		int obtainedCount() {
			return closedFlags.size();
		}

		boolean allClosed() {
			for (AtomicBoolean closed : closedFlags) {
				if (!closed.get()) {
					return false;
				}
			}
			return !closedFlags.isEmpty();
		}

		@Override
		public Object getConnection() throws Throwable {
			final AtomicBoolean closed = new AtomicBoolean(false);
			closedFlags.add(closed);
			final Connection delegate = dataSource.getConnection();
			return Proxy.newProxyInstance(Connection.class.getClassLoader(),
					new Class[] { Connection.class }, (proxy, method, args) -> {
						if ("close".equals(method.getName())
								&& (args == null || args.length == 0)) {
							closed.set(true);
						}
						try {
							return method.invoke(delegate, args);
						} catch (InvocationTargetException e) {
							throw e.getCause();
						}
					});
		}
	}
}
