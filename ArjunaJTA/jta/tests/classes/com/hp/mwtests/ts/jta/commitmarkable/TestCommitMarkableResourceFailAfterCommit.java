/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.commitmarkable;

import org.h2.jdbcx.JdbcDataSource;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BMUnitRunner.class)
public class TestCommitMarkableResourceFailAfterCommit extends
		FailAfterCommitBase {

	@Test
	@BMScript("commitMarkableResourceFailAfterCommit")
	public void testFailAfterCommitH2() throws Exception {
		final JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setURL("jdbc:h2:mem:JBTMDB;DB_CLOSE_DELAY=-1");

		doTest(dataSource);
	}
}