/*
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2013
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.jta.commitmarkable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import io.narayana.perf.Measurement;
import io.narayana.perf.Worker;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;
import org.postgresql.ds.PGConnectionPoolDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.xa.PGXADataSource;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.CommitMarkableResourceRecordRecoveryModule;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;

import org.junit.Assert;

public class PerformanceTestCommitMarkableResource extends
		TestCommitMarkableResourceBase {

	private final Object waitLock = new Object();
	private AtomicInteger totalExecuted = new AtomicInteger();

	private String dbType = System.getProperty("dbType", "h2");

	public void doTest(final Handler xaHandler, String testName) throws Exception {
        String fullTestName = getClass().getName() + testName;

        Worker<Void> worker = new Worker<Void> () {
            javax.transaction.TransactionManager tm = null;
            @Override
            public void init() {
                tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
            }

            @Override
            public void fini() {
					try {
						xaHandler.finishWork();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

//					totalExecuted.addAndGet(success);
            }

            @Override
            public Void doWork(Void context, int batchSize, Measurement<Void> measurement) {
                for (int i =0; i < batchSize; i++) {
                    try {
                        tm.begin();
                        tm.getTransaction().enlistResource(new DummyXAResource());

                        xaHandler.enlistResource(tm.getTransaction());

                        tm.commit();
                        // System.out.println("done");
                        totalExecuted.incrementAndGet();
                    } catch (SQLException e) {
                        measurement.incrementErrorCount();

                        if (measurement.getNumberOfErrors() == 1) {
                            System.err.println("boom");
                            e.printStackTrace();
                            if (e.getCause() != null) {
                                e.getCause().printStackTrace();
                            }
                            SQLException nextException = e.getNextException();
                            while (nextException != null) {
                                nextException.printStackTrace();
                                nextException = nextException
                                        .getNextException();
                            }
                            Throwable[] suppressed = e.getSuppressed();
                            for (int j = 0; j < suppressed.length; j++) {
                                suppressed[j].printStackTrace();
                            }
                            try {
                                tm.rollback();
                            } catch (IllegalStateException | SecurityException
                                    | SystemException e1) {
                                e1.printStackTrace();
                                fail("Problem with transaction");
                            }
                        }
                    } catch (NotSupportedException | SystemException
                            | IllegalStateException | RollbackException
                            | SecurityException | HeuristicMixedException
                            | HeuristicRollbackException e) {
                        measurement.incrementErrorCount();

                        e.printStackTrace();
                        fail("Problem with transaction");
                    }
                }

                return context;
            }
        };

        int warmUpCount = 0; // TODO if non zero then make sure the db is reset after the warm up loop
        int batchSize = 50;
        int threadCount = 20;

        Measurement measurement = new Measurement.Builder(fullTestName)
                .maxTestTime(0L).numberOfCalls(batchSize * threadCount)
                .numberOfThreads(threadCount).batchSize(batchSize)
                .numberOfWarmupCalls(warmUpCount).build().measure(worker, worker);

        System.out.printf("%s%n", measurement.getInfo());


		System.out.println(new Date() + "  Number of transactions: "+ totalExecuted.intValue());

		long additionalCleanuptime = xaHandler.postRunCleanup(measurement.getNumberOfMeasurements(),
                measurement.getNumberOfCalls(), measurement.getNumberOfThreads());

		long timeInMillis = measurement.getTotalMillis() + additionalCleanuptime;
        long throughput = Math.round((totalExecuted.intValue() / (timeInMillis / 1000d)));

        System.out.println("  Total transactions: " + totalExecuted.intValue());
		System.out.println("  Total time millis: " + timeInMillis);
		System.out.println("  Average transaction time: " + timeInMillis / totalExecuted.intValue());
		System.out.println("  Transactions per second: " + throughput);

		xaHandler.checkFooSize(measurement.getNumberOfMeasurements(), measurement.getBatchSize(), measurement.getNumberOfThreads());

        Assert.assertEquals(0, measurement.getNumberOfErrors());
        Assert.assertFalse(measurement.getInfo(), measurement.shouldFail());

	}

	private void checkSize(String string, Statement statement, int expected)
			throws SQLException {
		ResultSet result = statement.executeQuery("select count(*) from "
				+ string);
		result.next();
		int actual = result.getInt(1);
		result.close();
		assertEquals(expected, actual);
	}

	// @org.junit.Ignore
	@Test
	public void testCommitMarkableResource() throws Exception {
		System.out.println("testCommitMarkableResource: " + new Date());

		ConnectionPoolDataSource dataSource = null;
		DataSource recoveryDataSource = null;

		// General options
		// BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class)
		// .setPerformImmediateCleanupOfCommitMarkableResourceBranches(true);
		// BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class)
		// .setNotifyCommitMarkableRecoveryModuleOfCompleteBranches(
		// false);

		if (dbType.equals("oracle")) {
			// ORA-01795: maximum number of expressions in a list is 1000
			BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class)
					.setCommitMarkableResourceRecordDeleteBatchSize(1000);
			Class clazz = Class
					.forName("oracle.jdbc.pool.OracleConnectionPoolDataSource");
			dataSource = (ConnectionPoolDataSource) clazz.newInstance();
			clazz.getMethod("setDriverType", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "thin" });
			clazz.getMethod("setServerName", new Class[] { String.class })
					.invoke(dataSource,
							new Object[] { "tywin.buildnet.ncl.jboss.com" });
			clazz.getMethod("setNetworkProtocol", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "tcp" });
			clazz.getMethod("setDatabaseName", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "orcl" });
			clazz.getMethod("setUser", new Class[] { String.class }).invoke(
					dataSource, new Object[] { "dtf11" });
			clazz.getMethod("setPassword", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "dtf11" });
			clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
					dataSource, new Object[] { 1521 });
			recoveryDataSource = (DataSource) dataSource;
		} else if (dbType.equals("sybase")) {

			// wide table support?
			BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class)
					.setCommitMarkableResourceRecordDeleteBatchSize(2000);
			Class clazz = Class
					.forName("com.sybase.jdbc3.jdbc.SybConnectionPoolDataSource");
			dataSource = (ConnectionPoolDataSource) clazz.newInstance();

			clazz.getMethod("setServerName", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "192.168.1.5" });
			clazz.getMethod("setDatabaseName", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "LOCALHOST" });
			clazz.getMethod("setUser", new Class[] { String.class }).invoke(
					dataSource, new Object[] { "sa" });
			clazz.getMethod("setPassword", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "sybase" });
			clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
					dataSource, new Object[] { 5000 });

			Class clazz2 = Class.forName("com.sybase.jdbc3.jdbc.SybDataSource");
			recoveryDataSource = (DataSource) clazz2.newInstance();
			clazz2.getMethod("setServerName", new Class[] { String.class })
					.invoke(recoveryDataSource, new Object[] { "192.168.1.5" });
			clazz2.getMethod("setDatabaseName", new Class[] { String.class })
					.invoke(recoveryDataSource, new Object[] { "LOCALHOST" });
			clazz2.getMethod("setUser", new Class[] { String.class }).invoke(
					recoveryDataSource, new Object[] { "sa" });
			clazz2.getMethod("setPassword", new Class[] { String.class })
					.invoke(recoveryDataSource, new Object[] { "sybase" });
			clazz2.getMethod("setPortNumber", new Class[] { int.class })
					.invoke(recoveryDataSource, new Object[] { 5000 });
		} else if (dbType.equals("h2")) {

			// Smaller batch size as H2 uses a hashtable in the delete which is
			// inefficent for bytearray clause
			BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class)
					.setCommitMarkableResourceRecordDeleteBatchSize(100);
			dataSource = new JdbcDataSource();
			((JdbcDataSource) dataSource)
					.setURL("jdbc:h2:mem:JBTMDB;MVCC=TRUE;DB_CLOSE_DELAY=-1");
			recoveryDataSource = ((JdbcDataSource) dataSource);
		} else if (dbType.equals("postgres")) {

			dataSource = new PGConnectionPoolDataSource();
			((PGConnectionPoolDataSource) dataSource).setPortNumber(5432);
			((PGConnectionPoolDataSource) dataSource).setUser("dtf11");
			((PGConnectionPoolDataSource) dataSource).setPassword("dtf11");
			((PGConnectionPoolDataSource) dataSource)
					.setServerName("tywin.buildnet.ncl.jboss.com");
			((PGConnectionPoolDataSource) dataSource)
					.setDatabaseName("jbossts");
			recoveryDataSource = new PGSimpleDataSource();
			((PGSimpleDataSource) recoveryDataSource).setPortNumber(5432);
			((PGSimpleDataSource) recoveryDataSource).setUser("dtf11");
			((PGSimpleDataSource) recoveryDataSource).setPassword("dtf11");
			((PGSimpleDataSource) recoveryDataSource)
					.setServerName("tywin.buildnet.ncl.jboss.com");
			((PGSimpleDataSource) recoveryDataSource)
					.setDatabaseName("jbossts");
		} else if (dbType.equals("mysql")) {
			// com.mysql.jdbc.PacketTooBigException: Packet for query is too
			// large (1318148 > 1048576). You can change this value on the
			// server by setting the max_allowed_packet' variable
			BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class)
					.setCommitMarkableResourceRecordDeleteBatchSize(3500);

			dataSource = new MysqlConnectionPoolDataSource();
			// need paranoid as otherwise it sends a connection change user
			((MysqlConnectionPoolDataSource) dataSource)
					.setUrl("jdbc:mysql://tywin.buildnet.ncl.jboss.com:3306/jbossts?user=dtf11&password=dtf11&paranoid=true");
			recoveryDataSource = (DataSource) dataSource;
		} else if (dbType.equals("db2")) {
			Class clazz = Class
					.forName("com.ibm.db2.jcc.DB2ConnectionPoolDataSource");
			dataSource = (ConnectionPoolDataSource) clazz.newInstance();
			clazz.getMethod("setServerName", new Class[] { String.class })
					.invoke(dataSource,
							new Object[] { "tywin.buildnet.ncl.jboss.com" });
			clazz.getMethod("setDatabaseName", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "BTDB1" });
			clazz.getMethod("setUser", new Class[] { String.class }).invoke(
					dataSource, new Object[] { "db2" });
			clazz.getMethod("setPassword", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "db2" });
			clazz.getMethod("setDriverType", new Class[] { int.class }).invoke(
					dataSource, new Object[] { 4 });
			clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
					dataSource, new Object[] { 50001 });

			Class clazz2 = Class.forName("com.ibm.db2.jcc.DB2DataSource");
			recoveryDataSource = (DataSource) clazz2.newInstance();
			clazz2.getMethod("setServerName", new Class[] { String.class })
					.invoke(recoveryDataSource,
							new Object[] { "tywin.buildnet.ncl.jboss.com" });
			clazz2.getMethod("setDatabaseName", new Class[] { String.class })
					.invoke(recoveryDataSource, new Object[] { "BTDB1" });
			clazz2.getMethod("setUser", new Class[] { String.class }).invoke(
					recoveryDataSource, new Object[] { "db2" });
			clazz2.getMethod("setPassword", new Class[] { String.class })
					.invoke(recoveryDataSource, new Object[] { "db2" });
			clazz2.getMethod("setDriverType", new Class[] { int.class })
					.invoke(recoveryDataSource, new Object[] { 4 });
			clazz2.getMethod("setPortNumber", new Class[] { int.class })
					.invoke(recoveryDataSource, new Object[] { 50001 });
		} else if (dbType.equals("sqlserver")) {
			Class clazz = Class
					.forName("com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource");
			dataSource = (ConnectionPoolDataSource) clazz.newInstance();

			clazz.getMethod("setServerName", new Class[] { String.class })
					.invoke(dataSource,
							new Object[] { "dev30.mw.lab.eng.bos.redhat.com" });
			clazz.getMethod("setDatabaseName", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "dballo01" });
			clazz.getMethod("setUser", new Class[] { String.class }).invoke(
					dataSource, new Object[] { "dballo01" });
			clazz.getMethod("setPassword", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "dballo01" });
			clazz.getMethod("setSendStringParametersAsUnicode",
					new Class[] { Boolean.class }).invoke(dataSource,
					new Object[] { false });
			clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
					dataSource, new Object[] { 3918 });
			recoveryDataSource = (DataSource) dataSource;
		}
		PooledConnection pooledConnection = dataSource.getPooledConnection();
		Utils.createTables(pooledConnection.getConnection());
		pooledConnection.close();

		doTest(new Handler(dataSource, recoveryDataSource), "_testCommitMarkableResource_" + dbType);
	}

	// @org.junit.Ignore
	@Test
	public void testXAResource() throws Exception {
		System.out.println("testXAResource: " + new Date());

		XADataSource dataSource = null;

		if (dbType.equals("oracle")) {
			Class clazz = Class
					.forName("oracle.jdbc.xa.client.OracleXADataSource");
			dataSource = (XADataSource) clazz.newInstance();
			clazz.getMethod("setDriverType", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "thin" });
			clazz.getMethod("setServerName", new Class[] { String.class })
					.invoke(dataSource,
							new Object[] { "tywin.buildnet.ncl.jboss.com" });
			clazz.getMethod("setNetworkProtocol", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "tcp" });
			clazz.getMethod("setDatabaseName", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "orcl" });
			clazz.getMethod("setUser", new Class[] { String.class }).invoke(
					dataSource, new Object[] { "dtf11" });
			clazz.getMethod("setPassword", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "dtf11" });
			clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
					dataSource, new Object[] { 1521 });
		} else if (dbType.equals("sybase")) {
			Class clazz = Class
					.forName("com.sybase.jdbc3.jdbc.SybXADataSource");
			dataSource = (XADataSource) clazz.newInstance();

			clazz.getMethod("setServerName", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "192.168.1.5" });
			clazz.getMethod("setDatabaseName", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "LOCALHOST" });
			clazz.getMethod("setUser", new Class[] { String.class }).invoke(
					dataSource, new Object[] { "sa" });
			clazz.getMethod("setPassword", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "sybase" });
			clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
					dataSource, new Object[] { 5000 });
		} else if (dbType.equals("h2")) {
			dataSource = new org.h2.jdbcx.JdbcDataSource();
			((JdbcDataSource) dataSource)
					.setURL("jdbc:h2:mem:JBTMDB2;MVCC=TRUE;DB_CLOSE_DELAY=-1");
		} else if (dbType.equals("postgres")) {

			dataSource = new PGXADataSource();
			((PGXADataSource) dataSource).setPortNumber(5432);
			((PGXADataSource) dataSource).setUser("dtf11");
			((PGXADataSource) dataSource).setPassword("dtf11");
			((PGXADataSource) dataSource)
					.setServerName("tywin.buildnet.ncl.jboss.com");
			((PGXADataSource) dataSource).setDatabaseName("jbossts");
		} else if (dbType.equals("mysql")) {

			dataSource = new MysqlXADataSource();
			((MysqlXADataSource) dataSource)
					.setServerName("tywin.buildnet.ncl.jboss.com");
			((MysqlXADataSource) dataSource).setPortNumber(3306);
			((MysqlXADataSource) dataSource).setDatabaseName("jbossts");
			((MysqlXADataSource) dataSource).setUser("dtf11");
			((MysqlXADataSource) dataSource).setPassword("dtf11");
		} else if (dbType.equals("db2")) {
			Class clazz = Class.forName("com.ibm.db2.jcc.DB2XADataSource");
			dataSource = (XADataSource) clazz.newInstance();
			clazz.getMethod("setServerName", new Class[] { String.class })
					.invoke(dataSource,
							new Object[] { "tywin.buildnet.ncl.jboss.com" });
			clazz.getMethod("setDatabaseName", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "BTDB1" });
			clazz.getMethod("setUser", new Class[] { String.class }).invoke(
					dataSource, new Object[] { "db2" });
			clazz.getMethod("setPassword", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "db2" });
			clazz.getMethod("setDriverType", new Class[] { int.class }).invoke(
					dataSource, new Object[] { 4 });
			clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
					dataSource, new Object[] { 50001 });
		} else if (dbType.equals("sqlserver")) {
			Class clazz = Class
					.forName("com.microsoft.sqlserver.jdbc.SQLServerXADataSource");
			dataSource = (XADataSource) clazz.newInstance();

			clazz.getMethod("setServerName", new Class[] { String.class })
					.invoke(dataSource,
							new Object[] { "dev30.mw.lab.eng.bos.redhat.com" });
			clazz.getMethod("setDatabaseName", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "crashrec" });
			clazz.getMethod("setUser", new Class[] { String.class }).invoke(
					dataSource, new Object[] { "crashrec" });
			clazz.getMethod("setPassword", new Class[] { String.class })
					.invoke(dataSource, new Object[] { "crashrec" });
			clazz.getMethod("setSendStringParametersAsUnicode",
					new Class[] { Boolean.class }).invoke(dataSource,
					new Object[] { false });
			clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
					dataSource, new Object[] { 3918 });
		}

		Utils.createTables(dataSource);

		doTest(new Handler(dataSource), "_testXAResource_" + dbType);
	}

	private class Handler {

		private ThreadLocal<XAConnection> xaConnection = new ThreadLocal<XAConnection>();
		private XADataSource xaDataSource;
		private ConnectionPoolDataSource dataSource;
		private ThreadLocal<Connection> connection = new ThreadLocal<Connection>();
		private ThreadLocal<PooledConnection> pooledConnection = new ThreadLocal<PooledConnection>();
		private Object recoveryDataSource;

		public Handler(XADataSource xaDataSource) {
			this.xaDataSource = xaDataSource;
		}

		public Handler(ConnectionPoolDataSource dataSource,
				DataSource recoveryDataSource) {
			this.dataSource = dataSource;
			this.recoveryDataSource = recoveryDataSource;
		}

		private void enlistResource(Transaction transaction)
				throws SQLException, IllegalStateException, RollbackException,
				SystemException {
			if (xaDataSource != null) {
				if (this.xaConnection.get() == null) {
					this.xaConnection.set(xaDataSource.getXAConnection());
					this.connection.set(xaConnection.get().getConnection());
				}
				XAResource xaResource = xaConnection.get().getXAResource();
				transaction.enlistResource(xaResource);

				Statement createStatement = connection.get().createStatement();
				createStatement.execute("INSERT INTO "
						+ Utils.getXAFooTableName() + " (bar) VALUES (1)");
				createStatement.close();
			} else {
				if (this.pooledConnection.get() == null) {
					this.pooledConnection.set(dataSource.getPooledConnection());
				}
				Connection connection = this.pooledConnection.get()
						.getConnection();
				connection.setAutoCommit(false);

				XAResource nonXAResource = new JDBCConnectableResource(
						connection);
				transaction.enlistResource(nonXAResource);

				Statement createStatement = connection.createStatement();
				createStatement.execute("INSERT INTO foo (bar) VALUES (1)");
				createStatement.close();
			}
		}

		private void finishWork() throws SQLException {
			if (xaConnection.get() != null) {
				connection.get().close();
				connection.set(null);
				xaConnection.get().close();
				xaConnection.set(null);
			}
			if (pooledConnection.get() != null) {
				pooledConnection.get().close();
			}
		}

		public long postRunCleanup(int numberOfMeasurements,
		    int numberOfCalls, int numberOfThreads) throws NamingException, SQLException,
				ObjectStoreException {
			if (dataSource != null) {
				PooledConnection pooledConnection = null;
				Connection connection = null;
				try {
					pooledConnection = dataSource.getPooledConnection();
					connection = pooledConnection.getConnection();
					Statement statement = connection.createStatement();
					CommitMarkableResourceRecordRecoveryModule crrrm = null;
					RecoveryManager recMan = RecoveryManager.manager();
					Vector recoveryModules = recMan.getModules();
					if (recoveryModules != null) {
						Enumeration modules = recoveryModules.elements();

						while (modules.hasMoreElements()) {
							RecoveryModule m = (RecoveryModule) modules
									.nextElement();

							if (m instanceof CommitMarkableResourceRecordRecoveryModule) {
								crrrm = (CommitMarkableResourceRecordRecoveryModule) m;
								break;
							}
						}
					}
					int expectedReapableRecords = BeanPopulator
							.getDefaultInstance(JTAEnvironmentBean.class)
							.isPerformImmediateCleanupOfCommitMarkableResourceBranches() ? 0
							: numberOfMeasurements * numberOfCalls;
					checkSize("xids", statement, expectedReapableRecords);
					if (expectedReapableRecords > 0) {
						// The recovery module has to perform lookups
						new InitialContext().rebind("commitmarkableresource",
								recoveryDataSource);
						long startTime = System.currentTimeMillis();
						crrrm.periodicWorkFirstPass();
						crrrm.periodicWorkSecondPass();
						long endTime = System.currentTimeMillis();

						checkSize("xids", statement, 0);
						statement.close();

						System.out.println("  Total cleanup time: "
								+ (endTime - startTime)
								+ " Average cleanup time: "
								+ (endTime - startTime)
								/ expectedReapableRecords);

						return endTime - startTime;
					} else {
						statement.close();
					}
				} finally {
					if (connection != null) {
						connection.close();
					}

					if (pooledConnection != null) {
						pooledConnection.close();
					}
				}
			}
			return 0;
		}

		public void checkFooSize(int numberOfRuns, int batchSize, int numberOfThreads) throws SQLException {
			Connection connection = null;
			XAConnection xaConnection = null;
			PooledConnection pooledConnection = null;
			String tableToCheck = null;
			if (dataSource != null) {
				pooledConnection = dataSource.getPooledConnection();
				connection = pooledConnection.getConnection();
				tableToCheck = "foo";
			} else {
				xaConnection = xaDataSource.getXAConnection();
				connection = xaConnection.getConnection();
				tableToCheck = Utils.getXAFooTableName();
			}
			Statement statement = connection.createStatement();
			checkSize(tableToCheck, statement, numberOfRuns * numberOfThreads * batchSize);
			statement.close();
			connection.close();
			if (xaConnection != null) {
				xaConnection.close();
			}
			if (pooledConnection != null) {
				pooledConnection.close();
			}
		}
	}
}
