/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.hp.mwtests.ts.jdbc.recovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import jakarta.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.h2.Driver;
import org.h2.jdbcx.JdbcDataSource;
import org.jboss.byteman.agent.Transformer;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;
import com.arjuna.ats.internal.jdbc.drivers.PropertyFileDynamicClass;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jdbc.TransactionalDriver;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;

@RunWith(BMUnitRunner.class)
public class RecoveryTest {
    private static final Logger log = Logger.getLogger(RecoveryTest.class);
    private TransactionalDriver transactionalDriver;

    // small hack for H2 connection that works badly with XA
    // there is trouble of finishing XA transaction when a fail happens during XA execution
    // we need to leave such failed connection opened during recovery
    private Connection failedTxnConnection = null;

    @After
    public void cleanUp() {
        if(failedTxnConnection != null) {
            try {
                failedTxnConnection.close();
            } catch(Exception ignored) {
            }
            failedTxnConnection = null;
        }
        try {
            if(transactionalDriver != null)
                DriverManager.deregisterDriver(transactionalDriver);
        } catch (Exception e) {
            log.errorf(e, "Can't deregister driver '%s'", transactionalDriver);
        } finally {
            transactionalDriver = null;
        }
    }

	@Test
	@BMRule(
        name = "throw lang error exception",
        targetClass = "org.h2.jdbcx.JdbcXAConnection",
        targetMethod = "commit",
        action = "throw new java.lang.Error()",
        targetLocation = "AT ENTRY"
    )
	public void test() throws Exception {
		String url = "jdbc:arjuna:";
		Properties p = System.getProperties();
		p.put("jdbc.drivers", Driver.class.getName());

		System.setProperties(p);
		transactionalDriver = new TransactionalDriver();
		DriverManager.registerDriver(transactionalDriver);

		Properties dbProperties = new Properties();

		final JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
		dbProperties.put(TransactionalDriver.XADataSource, ds);

		step0_setupTables(url, dbProperties);

        // We need to do this in a different thread as otherwise the transaction would still be associated with the connection due to the java.lang.Error
	    // RMFAIL on it's own will cause H2 to close connection and that seems to discard the indoubt transactions
		step1_leaveXidsInDbTable(url, dbProperties);


		step2_checkDbIsNotCommitted(url, dbProperties);

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
			manager.removeAllModules(true);
			manager.addModule(xarm);
			AtomicActionRecoveryModule aarm = new AtomicActionRecoveryModule();

			aarm.periodicWorkFirstPass();
			Transformer.disableTriggers(true);
			aarm.periodicWorkSecondPass();
			Transformer.enableTriggers(true);
		}

		step4_finalDbCommitCheck(url, dbProperties);
	}

    @Test
    @BMRule(
        name = "throw rmfail xaexception",
        targetClass = "org.h2.jdbcx.JdbcXAConnection",
        targetMethod = "commit",
        action = "throw new javax.transaction.xa.XAException(javax.transaction.xa.XAException.XAER_RMFAIL)",
        targetLocation = "AT ENTRY"
    )
    public void directRecoverableConnection() throws Exception {
        //jdbc:arjuna: <path to properties file>, path starting at System.getProperty("user.dir")
        String url = TransactionalDriver.arjunaDriver + "ds-direct.properties";
        Properties dbProperties = new Properties();
        dbProperties.put(TransactionalDriver.dynamicClass, PropertyFileDynamicClass.class.getName());
        dbProperties.put(TransactionalDriver.userName, "");
        dbProperties.put(TransactionalDriver.password, "");

        Properties p = System.getProperties();
        p.put("jdbc.drivers", Driver.class.getName());
        System.setProperties(p);
        transactionalDriver = new TransactionalDriver();
        DriverManager.registerDriver(transactionalDriver);

        step0_setupTables(url, dbProperties);

        // rmfail means the db connection was left to be recovered
        step1_leaveXidsInDbTable(url, dbProperties);

        step2_checkDbIsNotCommitted(url, dbProperties);

        // 3. Perform recovery - checking only top down XARecoveryModule functionality
        {
            XARecoveryModule xarm = new XARecoveryModule();
            RecoveryManager manager = RecoveryManager.manager();
            manager.removeAllModules(true);
            manager.addModule(xarm);
            AtomicActionRecoveryModule aarm = new AtomicActionRecoveryModule();

            aarm.periodicWorkFirstPass();
            Transformer.disableTriggers(true);
            aarm.periodicWorkSecondPass();
            Transformer.enableTriggers(true);
        }

        step4_finalDbCommitCheck(url, dbProperties);
    }


	/**
	 * 0. Setup tables
	 */
	private void step0_setupTables(String url, Properties dbProperties) throws Exception {
        Connection conn = DriverManager.getConnection(url, dbProperties);
        log.debugf("conn step0: %s, db props %s", conn, dbProperties);

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
            conn.close();
        }
	}

	/**
	 * 1. Leave a Xid in the DB
	 */
	private void step1_leaveXidsInDbTable(String url, Properties dbProperties) throws Exception {
        Thread thread = new Thread(() -> {
            try {
                Uid[] uid = new Uid[1];
                failedTxnConnection = DriverManager.getConnection(url, dbProperties);
                log.debugf("conn step1: %s, db props %s", failedTxnConnection, dbProperties);
                TransactionManager tx = com.arjuna.ats.jta.TransactionManager
                        .transactionManager();

                tx.begin();
                tx.getTransaction().enlistResource(new DummyXAResource() {
                    @Override
                    public void start(Xid arg0, int arg1) throws XAException {
                        uid[0] = new Uid(arg0.getGlobalTransactionId());
                    }
                });

                Statement stmtx = failedTxnConnection.createStatement(); // will be a tx-statement

                stmtx.executeUpdate("INSERT INTO test_table (a, b) VALUES (1,2)");

                try {
                    tx.commit();
                } catch (Throwable e) {
                    // expected
                    ActionManager.manager().remove(uid[0]);
                }
                // do not close connection here as H2 does not handle well XA txn
                // when we close connection with failed transaction and then we want to recover it 
                // failedTxnConnection.close();
            } catch (Throwable t) {
                fail("Error injecting byteman rule to prepare unfinished xids"
                        + " of connection url: " + url + ", props: " + dbProperties);
            }
        });
        thread.start();
        thread.join();
	}

    /**
     * 2. Check its not in the DB already
     */
	private void step2_checkDbIsNotCommitted(String url, Properties dbProperties) throws Exception {
        Connection conn = DriverManager.getConnection(url, dbProperties);
        log.debugf("conn step2: %s, db props %s", conn, dbProperties);
        Statement stmt = conn.createStatement(); // non-tx statement

        ResultSet res1 = stmt.executeQuery("SELECT * FROM test_table");

        int rowCount = 0;

        while (res1.next()) {
            rowCount++;
        }

        try {
            assertEquals("Failure on commit of '" + url + "' means no record in db",
                0 , rowCount);
        } finally {
            conn.close();
        }
    }

    /**
     * 4. See if its there now
     */
	private void step4_finalDbCommitCheck(String url, Properties dbProperties) throws Exception {
        Connection conn = DriverManager.getConnection(url, dbProperties);
        log.debugf("conn step4: %s, db props %s", conn, dbProperties);
        Statement stmt = conn.createStatement(); // non-tx statement

        ResultSet res1 = stmt.executeQuery("SELECT * FROM test_table");

        int rowCount = 0;

        while (res1.next()) {
            rowCount++;
        }

        assertEquals("Recovery should commit the jdbc resource url: " + url, 1, rowCount);
        conn.close();
    }
}
