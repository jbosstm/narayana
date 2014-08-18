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
