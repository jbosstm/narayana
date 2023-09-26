/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.commitmarkable;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BMUnitRunner.class)
public class TestCommitMarkableResourceFailAfterPrepare extends
        FailAfterPrepareBase {

    @Test
    @BMScript("commitMarkableResourceFailAfterPrepare")
    public void testFailAfterPrepare() throws Exception {
        final DataSource dataSource = new JdbcDataSource();

        generateCMRRecord(dataSource);
        checkCMRRecovery(dataSource);
    }
}