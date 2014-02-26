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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.Enumeration;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Ignore;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;

import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.CommitMarkableResourceRecordRecoveryModule;

public class TestCommitMarkableResource extends TestCommitMarkableResourceBase {
	// @Ignore
	@Test
	public void testH2() throws Exception {
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setURL("jdbc:h2:mem:JBTMDB;MVCC=TRUE;DB_CLOSE_DELAY=-1");

		doTest(dataSource);
	}

	@Ignore
	@Test
	public void testPostgres() throws Exception {

		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setPortNumber(5432);
		dataSource.setUser("sa");
		dataSource.setPassword("sa");
		dataSource.setServerName("localhost");
		dataSource.setDatabaseName("commitmarkableresource");

		doTest(dataSource);
	}

	private void doTest(DataSource dataSource) throws Exception {

		// Test code
		Utils.createTables(dataSource.getConnection());

		javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
				.transactionManager();

		tm.begin();

		Connection localJDBCConnection = dataSource.getConnection();
		localJDBCConnection.setAutoCommit(false);
		XAResource nonXAResource = new JDBCConnectableResource(
				localJDBCConnection);
		tm.getTransaction().enlistResource(nonXAResource);

		tm.getTransaction().enlistResource(new DummyXAResource());

		localJDBCConnection.createStatement().execute(
				"INSERT INTO foo (bar) VALUES (1)");

		tm.commit();

		// This is test code, it allows us to verify that the correct XID was
		// removed
		Xid committed = ((JDBCConnectableResource) nonXAResource)
				.getStartedXid();
		assertNotNull(committed);

		// We can't just instantiate one as we need to be using the same one as
		// the transaction
		// manager would have used to mark the transaction for GC
		CommitMarkableResourceRecordRecoveryModule recoveryModule = null;
		Vector recoveryModules = manager.getModules();
		if (recoveryModules != null) {
			Enumeration modules = recoveryModules.elements();

			while (modules.hasMoreElements()) {
				RecoveryModule m = (RecoveryModule) modules.nextElement();

				if (m instanceof CommitMarkableResourceRecordRecoveryModule) {
					recoveryModule = (CommitMarkableResourceRecordRecoveryModule) m;
				}
			}
		}

		recoveryModule = new CommitMarkableResourceRecordRecoveryModule();
		// The recovery module has to perform lookups
		new InitialContext().rebind("commitmarkableresource", dataSource);
		// Run the first pass it will load the committed Xids into memory
		recoveryModule.periodicWorkFirstPass();
		assertTrue(recoveryModule.wasCommitted("commitmarkableresource",
				committed));
		recoveryModule.periodicWorkSecondPass();

		// Make sure that the resource was GC'd by the CRRRM
		assertFalse(recoveryModule.wasCommitted("commitmarkableresource",
				committed));
	}

}
