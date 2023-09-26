/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.commitmarkable;

import static org.junit.Assert.fail;

import java.sql.SQLException;

import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;

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

		System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile",
					"commitmarkableresourcejbossts-properties.xml");

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
		dataSource.setURL("jdbc:h2:mem:JBTMDB");

		jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
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