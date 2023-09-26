/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.jts.recovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryModule;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.utils.XAUtils;
import com.arjuna.ats.internal.jta.utils.jts.XidUtils;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.recovery.RecoveryEnablement;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.common.opPropertyManager;
import com.hp.mwtests.ts.jta.recovery.TestXAResourceWrapper;

/**
 * Testing imported subordinate transaction to work in recovery process.
 */
public class ImportedTransactionRecoveryUnitTest {

    private ORB myORB = null;
    private RootOA myOA = null;
    int orphanSafetyIntervalOrigin;
    List<String> xaRecoveryNodesOrigin = null;
    RecoveryManager recoveryManager = null;

    @Before
    public void setUp () throws Exception {
        final Map<String, String> orbInitializationProperties = new HashMap<String, String>();
        orbInitializationProperties.put("com.arjuna.orbportability.orb.PreInit1",
                "com.arjuna.ats.internal.jts.recovery.RecoveryInit");
        opPropertyManager.getOrbPortabilityEnvironmentBean()
                .setOrbInitializationProperties(orbInitializationProperties);

        final Properties initORBProperties = new Properties();
        initORBProperties.setProperty("com.sun.CORBA.POA.ORBServerId", "1");
        initORBProperties.setProperty("com.sun.CORBA.POA.ORBPersistentServerPort", ""
                + jtsPropertyManager.getJTSEnvironmentBean().getRecoveryManagerPort());

        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);
        myORB.initORB(new String[] {}, initORBProperties);
        myOA.initOA();
        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);

        final List<String> recoveryExtensions = new ArrayList<String>();
        recoveryExtensions.add(XARecoveryModule.class.getName());
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryModuleClassNames(recoveryExtensions);

        final List<String> recoveryActivatorClassNames = new ArrayList<String>();
        recoveryActivatorClassNames.add(RecoveryEnablement.class.getName());
        recoveryPropertyManager.getRecoveryEnvironmentBean()
                .setRecoveryActivatorClassNames(recoveryActivatorClassNames);

        recoveryManager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        recoveryManager.initialize();

        orphanSafetyIntervalOrigin = jtaPropertyManager.getJTAEnvironmentBean().getOrphanSafetyInterval();
        jtaPropertyManager.getJTAEnvironmentBean().setOrphanSafetyInterval(0);
        xaRecoveryNodesOrigin = jtaPropertyManager.getJTAEnvironmentBean().getXaRecoveryNodes();
    }

    @After
    public void tearDown () throws Exception {
        jtaPropertyManager.getJTAEnvironmentBean().setOrphanSafetyInterval(orphanSafetyIntervalOrigin);
        jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(xaRecoveryNodesOrigin);

        recoveryManager.terminate();
        myOA.destroy();
        myORB.shutdown();
    }

    /**
     * <p>
     * Testing that multiple {@link XAResource}s could be used under imported transaction.<br>
     * Previously there was no check of xid format which resulted to fact of merging usage of multiple XAResources
     * under one Xid even when the subordinate transaction was under management of Narayana (meaning created by Narayana).
     * That could lead to non-recoverable behavior for particular XAResource.
     * <p>
     * By adding check for format of xid Narayana generates new Xid for any XAResource used under transaction
     * created by Narayana. If transaction comes from EIS there is returned the same Xid (as expected).
     */
    @Test
    public void testMultipleXAResourceForImportedJcaTransaction() throws Exception {
        final Xid xid = XidUtils.getXid(new Uid(), true);
        String parentNodeName = "parent1";
        XATxConverter.setNodeName(((XidImple)xid).getXID(), parentNodeName);
        SubordinateTransaction subordinateTransaction = SubordinationManager.getTransactionImporter().importTransaction(xid);

        TestXAResourceWrapper xar1 = new TestXAResourceWrapper("narayana", "narayana", "java:/test1") {
            boolean wasThrown = false;
            @Override
            public void commit(Xid xid, boolean onePhase) throws XAException {
                if(!wasThrown) {
                    wasThrown = true;
                    throw new XAException(XAException.XAER_RMFAIL);
                } else {
                    super.commit(xid, onePhase);
                }
            }
        };
        TestXAResourceWrapper xar2 = new TestXAResourceWrapper("narayana", "narayana", "java:/test2") {
            boolean wasThrown = false;
            @Override
            public void commit(Xid xid, boolean onePhase) throws XAException {
                if(!wasThrown) {
                    wasThrown = true;
                    throw new XAException(XAException.XAER_RMFAIL);
                } else {
                    super.commit(xid, onePhase);
                }
            }
        };

        assertTrue("Fail to enlist first test XAResource", subordinateTransaction.enlistResource(xar1));
        assertTrue("Fail to enlist second XAResource", subordinateTransaction.enlistResource(xar2));

        assertEquals("transaction should be prepared", TwoPhaseOutcome.PREPARE_OK, subordinateTransaction.doPrepare());
        assertFalse("first resource should fail on transaction commit, thus whole txn can't be committed",
            subordinateTransaction.doCommit());

        assertNotEquals("XAResources should be enlisted with different xids", xar1.getXid(), xar2.getXid());

        // xid format verification of node names
        assertEquals("Expecting the original Xid has redefined node from node identifier"
            + "to parent node name by XATxConverter call", parentNodeName, XAUtils.getXANodeName(xid));
        assertNull("Expecting the original xid has no subordinate name definied as it was not imported",
            XATxConverter.getSubordinateNodeName(((XidImple)xid).getXID()));
        // imported Xid verified here: the subordinate transaction parent node is node name
        // and subordinate is the current node identifier
        assertEquals("Expecting the subordinate Xid has defined parent node name as original Xid node name",
            parentNodeName, XAUtils.getXANodeName(subordinateTransaction.baseXid()));
        assertNull("Expecting null as JTS subordinate Xid does not support use of the subordinate node name",
                XAUtils.getSubordinateNodeName(subordinateTransaction.baseXid()));
        assertEquals("XAResource1 xid expects to have node name equal to the name of imported transaction node",
            parentNodeName, XAUtils.getXANodeName(xar1.getXid()));
        assertEquals("XAResource2 xid expects to have node name equal to the name of imported transaction node",
            parentNodeName, XAUtils.getXANodeName(xar2.getXid()));

        ((XARecoveryModule) recoveryManager.getModules().get(0))
            .addXAResourceRecoveryHelper(new TestXARecoveryHelper(xar1, xar2));

        recoveryManager.scan();

        assertEquals("XAResource1 can't be rolled-back", 0, xar1.rollbackCount());
        assertEquals("XAResource2 can't be rolled-back", 0, xar2.rollbackCount());
        assertEquals("XAResource1 has to be committed", 1, xar1.commitCount());
        assertEquals("XAResource2 has to be committed", 1, xar2.commitCount());
    }
}