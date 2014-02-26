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

import static org.junit.Assert.fail;

import java.sql.SQLException;

import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;

public class TestCommitMarkableResourceMultiEnlist {

	private String resetPropertiesFile;

	@Before
	public void setup() throws Exception {

		resetPropertiesFile = System
				.getProperty("com.arjuna.ats.arjuna.common.propertiesFile");
		if (resetPropertiesFile == null) {
			System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile",
					"commitmarkableresourcejbossts-properties.xml");
		}
		RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
	}

	@After
	public void tearDown() {
		if (resetPropertiesFile != null) {
			System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile",
					resetPropertiesFile);
		} else {
			System.clearProperty("com.arjuna.ats.arjuna.common.propertiesFile");
		}
	}

	@Test
	public void testFailDoubleEnlist() throws NotSupportedException,
			SystemException, IllegalStateException, RollbackException,
			SQLException {
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setURL("jdbc:h2:mem:JBTMDB;MVCC=TRUE");

		javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
				.transactionManager();

		tm.begin();

		tm.getTransaction().enlistResource(
				new JDBCConnectableResource(dataSource.getConnection()));
		if (tm.getTransaction().enlistResource(
				new JDBCConnectableResource(dataSource.getConnection()))) {
			fail();
		}

		tm.rollback();
	}
}
