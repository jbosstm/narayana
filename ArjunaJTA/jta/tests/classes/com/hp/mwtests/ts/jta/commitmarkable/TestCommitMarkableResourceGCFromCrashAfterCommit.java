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

import java.sql.Connection;
import java.util.Enumeration;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.h2.jdbcx.JdbcDataSource;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.CommitMarkableResourceRecordRecoveryModule;

@RunWith(BMUnitRunner.class)
public class TestCommitMarkableResourceGCFromCrashAfterCommit extends
		TestCommitMarkableResourceBase {

	@Test
	public void testFailAfterCommitH2() throws Exception {
		final DataSource dataSource = new JdbcDataSource();
		((JdbcDataSource) dataSource)
				.setURL("jdbc:h2:mem:JBTMDB;MVCC=TRUE;DB_CLOSE_DELAY=-1");

		// Test code
		Utils.createTables(dataSource.getConnection());

		// We can't just instantiate one as we need to be using the
		// same one as
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

		jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
				.transactionManager();

		tm.begin();

		Connection localJDBCConnection = dataSource.getConnection();
		localJDBCConnection.setAutoCommit(false);
		XAResource nonXAResource = new JDBCConnectableResource(
				localJDBCConnection);
		tm.getTransaction().enlistResource(nonXAResource);

		XAResource xaResource = new SimpleXAResource();
		tm.getTransaction().enlistResource(xaResource);

		localJDBCConnection.createStatement().execute(
				"INSERT INTO foo (bar) VALUES (1)");

		tm.commit();

		Xid committed = ((JDBCConnectableResource) nonXAResource)
				.getStartedXid();
		assertNotNull(committed);
		// The recovery module has to perform lookups
		new InitialContext().rebind("commitmarkableresource", dataSource);
		// Check if the item is still in the db
		recoveryModule.periodicWorkFirstPass();
		recoveryModule.periodicWorkSecondPass();
		assertFalse(recoveryModule.wasCommitted("commitmarkableresource",
				committed));
	}
}
