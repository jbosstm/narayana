/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.commitmarkable;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.tools.osb.mbean.OSEntryBean;
import com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowser;
import com.arjuna.ats.arjuna.tools.osb.mbean.UidWrapper;
import org.h2.jdbcx.JdbcDataSource;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(BMUnitRunner.class)
public class TestCommitMarkableResourceMBeansFailAfterPrepare extends
        FailAfterPrepareBase {
    @Test
    @BMScript("commitMarkableResourceFailAfterPrepare")
    public void testObjStoreBrowser() throws Exception {
        final DataSource dataSource = new JdbcDataSource();
        ObjStoreBrowser osb = new ObjStoreBrowser();
        Uid uid = generateCMRRecord(dataSource);

        osb.start();
        osb.probe();
        // there should be one MBean corresponding to the Transaction
        UidWrapper w = osb.findUid(uid);
        assertNotNull("ObjStoreBrowser could not find CMR uid wrapper", w);
        OSEntryBean ai = w.getMBean();
        assertNotNull("ObjStoreBrowser could not find CMR uid", ai);
        assertEquals("ObjStoreBrowser found the wrong uid", ai.getUid(), uid);

        checkCMRRecovery(dataSource);

        w = osb.findUid(uid);
        assertNotNull("CMR uid still exists after a recovery can", w);
    }
}