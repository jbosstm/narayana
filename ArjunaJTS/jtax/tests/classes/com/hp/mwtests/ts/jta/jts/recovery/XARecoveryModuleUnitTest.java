/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.recovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryModule;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.SubordinateAtomicTransaction;
import com.arjuna.ats.internal.jta.utils.jts.XidUtils;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.hp.mwtests.ts.jta.common.RecoveryXAResource;
import com.hp.mwtests.ts.jta.recovery.TestXAResourceWrapper;

public class XARecoveryModuleUnitTest
{
    @Test
    public void testNull ()
    {
        XARecoveryModule xarm = new XARecoveryModule();
        
        xarm.periodicWorkFirstPass();
        xarm.periodicWorkSecondPass();
    }
    
    @Test
    public void testRecover () throws Exception
    {
        ArrayList<String> r = new ArrayList<String>();
        TransactionImple tx = new TransactionImple();
        
        assertTrue(tx.enlistResource(new RecoveryXAResource()));
        
        SubordinateAtomicTransaction sat = new SubordinateAtomicTransaction(tx.get_uid(), tx.getTxId(), 0);
        
        assertEquals(sat.doPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
        
        r.add("com.hp.mwtests.ts.jta.recovery.DummyXARecoveryResource");

        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceRecoveryClassNames(r);
        
        XARecoveryModule xarm = new XARecoveryModule();
        
        for (int i = 0; i < 11; i++)
        {
            xarm.periodicWorkFirstPass();
            xarm.periodicWorkSecondPass();
        }
    }
    
    /**
     * Test which uses method {@link TransactionImple#enlistResource(XAResource, Object[])} could be used
     * with empty object array and still works.
     */
    @Test
    public void testEmptyResourceEnlistmentParams() throws Exception {
        final Uid uid = new Uid();
        final Xid xid = XidUtils.getXid(uid, true);
        SubordinateTransaction subordinateTransaction = SubordinationManager.getTransactionImporter().importTransaction(xid);
        TransactionImple subordinateTransactionImple = (TransactionImple) subordinateTransaction;

        TestXAResourceWrapper xar = new TestXAResourceWrapper("narayana", "narayana", "java:/test1");

        subordinateTransactionImple.enlistResource(xar, new Object[]{});

        int statusPrepare = subordinateTransaction.doPrepare();
        subordinateTransaction.doCommit();

        assertEquals("transaction should be prepared", TwoPhaseOutcome.PREPARE_OK, statusPrepare);
        assertEquals("XAResource can't be rolled-back", 0, xar.rollbackCount());
        assertEquals("XAResource has to be committed", 1, xar.commitCount());
    }

    @Before
    public void setUp () throws Exception
    {
        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);
    }
    
    @After
    public void tearDown () throws Exception
    {
        myOA.destroy();
        myORB.shutdown();
    }
    
    private ORB myORB = null;
    private RootOA myOA = null;
}