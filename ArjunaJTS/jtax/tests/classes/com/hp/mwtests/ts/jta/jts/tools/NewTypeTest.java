/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.jts.tools;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.management.MBeanException;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.tools.osb.mbean.OSBTypeHandler;
import com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowser;
import com.arjuna.ats.arjuna.tools.osb.mbean.UidWrapper;
import com.arjuna.ats.internal.jta.tools.osb.mbean.jts.JCAServerTransactionHeaderReader;
/**
 * An example of how to instrument new record types.
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class NewTypeTest extends JTSOSBTestBase {

    @Test
    public void testInstrumentNewType() throws MBeanException {
        OSBTypeHandler osbTypeHandler = new OSBTypeHandler(
                true,
                "com.arjuna.ats.internal.jta.tools.osb.mbean.jts.ArjunaTransactionImpleWrapper",
                "com.arjuna.ats.internal.jta.tools.osb.mbean.jts.JTSActionBean",
                "StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple/ServerTransaction/JCA",
                JCAServerTransactionHeaderReader.class.getName()
        );

        JCAServerTransactionHeaderReader headerStateReader = (JCAServerTransactionHeaderReader) osbTypeHandler.getHeaderStateReader();
        RecoveringSubordinateServerTransaction txn = new RecoveringSubordinateServerTransaction(new Uid()); // , new XidImple(new Uid()))

        ObjStoreBrowser osb = createObjStoreBrowser(false);

        osb.registerHandler(osbTypeHandler);

        generatedHeuristicHazard(txn);

        osb.probe();

        UidWrapper w = osb.findUid(txn.get_uid());

        assertNotNull(w);
        assertTrue(headerStateReader.isWasInvoked());
    }

    private static class RecoveringSubordinateServerTransaction
            extends com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.coordinator.ServerTransaction {

        public RecoveringSubordinateServerTransaction(Uid recoveringActUid) {
            super(recoveringActUid); //, new XidImple(new Uid()));
        }
    }
}