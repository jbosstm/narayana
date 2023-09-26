/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.CommitMarkableResourceRecordRecoveryModule;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.hp.mwtests.ts.jta.commitmarkable.DummyXAResource;
import com.hp.mwtests.ts.jta.commitmarkable.Utils;
import org.wildfly.extras.creaper.core.online.FailuresAllowedBlock;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import static org.wildfly.extras.creaper.core.ManagementClient.online;
import static org.wildfly.extras.creaper.core.online.OnlineOptions.standalone;

@RunWith(Arquillian.class)
@ServerSetup(CMRIntegrationTest.SetupCMRDataSource.class)
@BMRule(name = "Fail if CMR is not used",
		targetClass = "com.arjuna.ats.arjuna.coordinator.BasicAction", targetMethod = "doCommit(boolean,com.arjuna.ats.arjuna.coordinator.AbstractRecord)",
		binding = "record:com.arjuna.ats.arjuna.coordinator.AbstractRecord = $2",
		condition = "!record.getClass().getName().equals(\"com.arjuna.ats.internal.jta.resources.arjunacore.CommitMarkableResourceRecord\") && " +
				"!record.getClass().getName().equals(\"com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord\")",
		action = "System.err.println(\"[BYTEMAN RULE] This test doesn't use CommitMarkableResourceRecord!\")," +
				"System.err.println(\"[BYTEMAN RULE] Record type is\" + record.getClass().getName())," +
				"throw new java.lang.RuntimeException(\"This test doesn't use CommitMarkableResourceRecord!\")")
public class CMRIntegrationTest {
	private static final Logger log = Logger.getLogger(CMRIntegrationTest.class);

	private static final String DEPENDENCIES = "Dependencies: com.h2database.h2, org.jboss.jts, org.jboss.jboss-transaction-spi\n";

	@Deployment
	public static WebArchive createTestArchive() {
		WebArchive archive = ShrinkWrap
				.create(WebArchive.class, "test.war")
				// ManagementClient and "org.jboss.as.arquillian.api" are needed to avoid errors in WildFly but they do not affect tests
				.addClasses(DummyXAResource.class, Utils.class, SetupCMRDataSource.class, ManagementClient.class)
				.addPackages(true, "org.jboss.as.arquillian.api")
				.addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\"></beans>"), "beans.xml");

		archive.setManifest(new StringAsset(DEPENDENCIES));

		return archive;
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

		log.infof("%s Number of transactions: %d", new Date().toString(), totalExecuted.intValue());

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

	public static class SetupCMRDataSource implements ServerSetupTask {
		@Override
		public void setup(ManagementClient managementClient, String containerId) throws Exception {
			// Creaper is employed to execute CLI commands on the instance of WildFly started by Arquillian
			OnlineManagementClient creaper = online(standalone().wrap(managementClient.getControllerClient()));

			try (FailuresAllowedBlock allowedBlock = creaper.allowFailures()) {
				// The following two CLI commands configure WildFly to use Commit Markable Resource on ExampleDS
				creaper.execute("/subsystem=datasources/data-source=ExampleDS:write-attribute(name=\"connectable\", value=\"true\")");
				creaper.execute("/subsystem=transactions/commit-markable-resource=\"java:jboss/datasources/ExampleDS\":add(name=xids, batch-size=1, immediate-cleanup=false)");
			}

			// Reboot the server
			new Administration(creaper).reload();
		}

		@Override
		public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
			// Creaper is employed to execute CLI commands on the instance of WildFly started by Arquillian
			OnlineManagementClient creaper = online(standalone().wrap(managementClient.getControllerClient()));

			try (FailuresAllowedBlock allowedBlock = creaper.allowFailures()) {
				// The following two CLI commands revert the use of Commit Markable Resource on ExampleDS
				creaper.execute("/subsystem=datasources/data-source=ExampleDS:write-attribute(name=\"connectable\", value=\"false\")");
				creaper.execute("/subsystem=transactions/commit-markable-resource=\"java:jboss/datasources/ExampleDS\":remove()");
			}
		}

		// Reboot is not needed as the server is shut down at the end of the tests
	}

}