/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hp.mwtests.ts.jta.commitmarkable.integration;

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

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.CommitMarkableResourceRecordRecoveryModule;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.hp.mwtests.ts.jta.commitmarkable.DummyXAResource;
import com.hp.mwtests.ts.jta.commitmarkable.Utils;

@RunWith(Arquillian.class)
public class CMRIntegrationTest {
	private static final Logger log = Logger.getLogger(CMRIntegrationTest.class);

	private static final String DEPENDENCIES = "Dependencies: com.h2database.h2, org.jboss.jts, org.jboss.jboss-transaction-spi\n";

	@Deployment
	public static JavaArchive createTestArchive() {
		return ShrinkWrap
				.create(JavaArchive.class, "test.jar")
				.addClasses(DummyXAResource.class, Utils.class)
				.addPackage("io.narayana.connectableresource")
				.addAsManifestResource(new StringAsset(DEPENDENCIES),
						"MANIFEST.MF")
				.addAsManifestResource(EmptyAsset.INSTANCE,
						ArchivePaths.create("beans.xml"));
	}

	@Resource(mappedName = "java:jboss/datasources/ExampleDS")
	private javax.sql.DataSource ds;

	@Inject
	private UserTransaction userTransaction;

	@Resource(mappedName = "java:jboss/TransactionManager")
	private TransactionManager tm;

	@Test
	public void testCMR() throws Exception {
		Utils.createTables(ds.getConnection());

		doTest(ds);
	}

	// @Test
	public void testCMR1() throws Exception {

		Utils.createTables(ds.getConnection());

		try {
			userTransaction.begin();

			tm.getTransaction().enlistResource(new DummyXAResource());

			Connection connection = ds.getConnection();
			Statement createStatement = connection.createStatement();
			createStatement.execute("INSERT INTO foo (bar) VALUES (1)");
			createStatement.close();

			userTransaction.commit();
		} catch (Exception e) {
			log.infof(e, "XXX txn exception cause: %s", e.getCause());
		} finally {
			try {
				if (userTransaction.getStatus() == Status.STATUS_ACTIVE
						|| userTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK)
					userTransaction.rollback();
			} catch (Throwable e) {
				System.out.printf("XXX txn did not finish: %s%n", e.getCause());
			}
		}
	}

	private final int threadCount = 5;
	private final int iterationCount = 20;
	private int waiting;
	private boolean go;
	private final Object waitLock = new Object();
	private final AtomicInteger totalExecuted = new AtomicInteger();

	public void doTest(final DataSource dataSource) throws Exception {

		// Test code
		Thread[] threads = new Thread[threadCount];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(new Runnable() {

				public void run() {
					synchronized (waitLock) {
						waiting++;
						waitLock.notify();
					}
					synchronized (CMRIntegrationTest.this) {
						while (!go) {
							try {
								CMRIntegrationTest.this.wait();
							} catch (InterruptedException ie) {
								log.error("Interrupted exception on waiting for a notification at CMRIntegrationTest", ie);
								return;
							}
						}
					}

					int success = 0;
					Connection connection = null;

					for (int i = 0; i < iterationCount; i++) {
						try {
							userTransaction.begin();
							tm.getTransaction().enlistResource(
									new DummyXAResource());

							connection = dataSource.getConnection();

							Statement createStatement = connection
									.createStatement();
							createStatement
									.execute("INSERT INTO foo (bar) VALUES (1)");

                            userTransaction.commit();
							connection.close(); // This wouldn't work for a
												// none-JCA code as commit has
												// closed the connection - it
												// helps us though as JCA seems
												// to rely on finalize
							success++;
						} catch (SQLException e) {
							log.errorf(e, "Error while invoking insertion to a database table with CMR resource");
							SQLException nextException = e.getNextException();
							while (nextException != null) {
								log.errorf("Next SQLException chained", nextException);
								nextException = nextException.getNextException();
							}
							try {
								userTransaction.rollback();
							} catch (IllegalStateException | SecurityException
									| SystemException e1) {
								log.error("Problem with transaction", e1);
								fail("Problem with transaction");
							}
						} catch (NotSupportedException | SystemException
								| IllegalStateException | RollbackException
								| SecurityException | HeuristicMixedException
								| HeuristicRollbackException e) {
							log.error("Problem with UserTransaction", e);
							fail("Problem with transaction");
						} finally {
							if (connection != null)
								try {
									connection.close();
								} catch (SQLException sqle) {
									log.error("Error on closing failed CMR resource connection", sqle);
								}
						}

					}

					totalExecuted.addAndGet(success);
				}
			});
			threads[i].start();
		}

		synchronized (waitLock) {
			while (waiting < threads.length) {
				waitLock.wait();
			}
		}
		long startTime;
		synchronized (CMRIntegrationTest.this) {
			go = true;
			CMRIntegrationTest.this.notifyAll();
			startTime = System.currentTimeMillis();
		}

		for (Thread thread : threads) {
			thread.join();
		}

		long endTime = System.currentTimeMillis();

		log.infof("%T Number of transactions: %d", new Date(), totalExecuted.intValue());

		long additionalCleanuptime = 0L; // postRunCleanup(dataSource);

		long timeInMillis = (endTime - startTime) + additionalCleanuptime;
		log.infof("  Total time millis: %d%n", timeInMillis);
		log.infof("  Average transaction time: %d%n", timeInMillis / totalExecuted.intValue());
		log.infof("  Transactions per second: %d%n", Math.round((totalExecuted.intValue() / (timeInMillis / 1000d))));

		checkFooSize(dataSource);
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

	public void checkFooSize(DataSource dataSource) throws SQLException,
			HeuristicRollbackException, RollbackException,
			HeuristicMixedException, SystemException, NotSupportedException {
		userTransaction.begin();
		Connection connection = dataSource.getConnection();
		String tableToCheck = "foo";
		Statement statement = connection.createStatement();
		checkSize(tableToCheck, statement, threadCount * iterationCount);
		statement.close();
		userTransaction.commit();
		connection.close();
	}

	private CommitMarkableResourceRecordRecoveryModule getCRRRM() {
		RecoveryManager recMan = RecoveryManager.manager();
		Vector<RecoveryModule> recoveryModules = recMan.getModules();

		if (recoveryModules != null) {
			Enumeration<RecoveryModule> modules = recoveryModules.elements();

			while (modules.hasMoreElements()) {
				RecoveryModule m = modules.nextElement();

				if (m instanceof CommitMarkableResourceRecordRecoveryModule) {
					return (CommitMarkableResourceRecordRecoveryModule) m;
				}
			}
		}

		return null;
	}

	public long postRunCleanup(DataSource dataSource) throws SQLException {
		Connection connection = dataSource.getConnection();
		CommitMarkableResourceRecordRecoveryModule crrrm = getCRRRM();

		int expectedReapableConnectableResourceRecords = BeanPopulator
				.getDefaultInstance(JTAEnvironmentBean.class)
				.isPerformImmediateCleanupOfCommitMarkableResourceBranches() ? 0
				: threadCount * iterationCount;

		try {
			Statement statement = connection.createStatement();

			checkSize("xids", statement,
					expectedReapableConnectableResourceRecords);

			if (expectedReapableConnectableResourceRecords > 0) {
				// The recovery module has to perform lookups
				long startTime = System.currentTimeMillis();
				crrrm.periodicWorkFirstPass();
				crrrm.periodicWorkSecondPass();
				long endTime = System.currentTimeMillis();

				checkSize("xids", statement, 0);
				statement.close();

				log.infof("  Total cleanup time: %d  Average cleanup time: %s",
						(endTime - startTime),(endTime - startTime) / expectedReapableConnectableResourceRecords);

				return endTime - startTime;
			} else {
				statement.close();
			}
		} finally {
			connection.close();
		}

		return 0;
	}

}
