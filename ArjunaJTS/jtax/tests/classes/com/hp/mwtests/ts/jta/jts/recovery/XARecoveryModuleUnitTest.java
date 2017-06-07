/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: xidcheck.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.jts.recovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.internal.jta.recovery.jts.JTSNodeNameXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryModule;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.jts.AtomicTransaction;
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

    @Test
    public void testOrphanDetection () throws Exception
    {
        final Uid uid = new Uid();
        final Xid xid = XidUtils.getXid(uid, true);
        SubordinateTransaction subordinateTransaction = SubordinationManager.getTransactionImporter().importTransaction(xid);
        TestXAResourceWrapper xar1 = new TestXAResourceWrapper("narayana", "narayana", "java:/test1")
        {
            @Override
            public int prepare(Xid xid) throws XAException {
                super.prepare(xid);
                throw new XAException(XAException.XAER_RMFAIL);
            }
        };
        assertTrue("Fail to enlist first test XAResource", subordinateTransaction.enlistResource(xar1));
        assertEquals("Subordinate transaction prepare expected to not prepared as XAException was thrown from XAResource",
            subordinateTransaction.doPrepare(), TwoPhaseOutcome.PREPARE_NOTOK);

        jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(Collections.singletonList(
            arjPropertyManager.getCoreEnvironmentBean().getNodeIdentifier()));

        XARecoveryModule xarm = new XARecoveryModule();
        xarm.addXAResourceOrphanFilter(new JTSNodeNameXAResourceOrphanFilter());
        xarm.addXAResourceRecoveryHelper(new TestXARecoveryHelper(xar1));

        xarm.periodicWorkFirstPass();
        xarm.periodicWorkSecondPass();

        // after recovery we expect rollback was called on XAResources by orphan filter
        assertEquals("XAResource1 was not prepared it can't be committed", 0, xar1.commitCount());
        assertEquals("XAResource1 has to be rolled-back, expecting orphan filter runned", 1, xar1.rollbackCount());
    }

    /**
     * This test is taken from {@link com.hp.mwtests.ts.jta.recovery.XARecoveryModuleUnitTest#testRecoverPassFailure()}.
     */
    @Test
    public void testRecoverPassFailure () throws Exception
    {
        XARecoveryModule xarm = new XARecoveryModule();
        xarm.addXAResourceOrphanFilter(new JTSNodeNameXAResourceOrphanFilter());

        final String jndiName = "java:/test";
        TestXAResourceWrapper xaResource = new TestXAResourceWrapper("narayana", "narayana", jndiName)
        {
            int count = 0;
            Xid xid = XidUtils.getXid(new Uid(), true);

            @Override
            public Xid[] recover (int i) throws XAException
            {
                count++;
                if (count == 1 || count == 5)
                {
                    super.recover(i);
                    return new Xid[]{xid};
                } else if (count > 5)
                {
                    super.recover(i);
                    return new Xid[0];
                } else
                {
                    throw new XAException();
                }
            }

            @Override
            public void rollback (Xid xid) throws XAException
            {
                if (count == 1) // This comes from the first end scan
                {
                    throw new XAException(XAException.XA_RETRY);
                }
                super.rollback(xid);
            }
        };

        xarm.addXAResourceRecoveryHelper(new TestXARecoveryHelper(xaResource));

        // The first two recovery cycles do nothing with the resource (because phase two is getting the exception)
        // When count reaches 6 it sees that the xid has gone and presumes abort so calls rollback and hence assertTrue(rolledback) passes
        jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(
            Collections.singletonList(arjPropertyManager.getCoreEnvironmentBean().getNodeIdentifier()));
    
        // 1st pass: returns one xid (count is 1)
        xarm.periodicWorkFirstPass();
        // 2nd pass: throws an exception (count is 2)
        xarm.periodicWorkSecondPass();
        assertTrue(xarm.getContactedJndiNames().contains(jndiName));
        assertEquals("Exepecting rollback fails after first periodic recovery scan",
            0, xaResource.rollbackCount());
        // 1st pass: throws an exception (count is 3)
        xarm.periodicWorkFirstPass();
        // 2nd pass: throws an exception (count is 4)
        xarm.periodicWorkSecondPass();
        assertFalse(xarm.getContactedJndiNames().contains(jndiName));
        assertEquals("Exepecting rollback fails after first and second periodic recovery scan",
            0, xaResource.rollbackCount());
        // 1st pass: returns an empty list of xids (count is 5)
        xarm.periodicWorkFirstPass();
        // 2nd pass: returns an empty list of xids (count is 6)
        xarm.periodicWorkSecondPass();
        assertTrue(xarm.getContactedJndiNames().contains(jndiName));
        assertEquals("Exepecting rollback passed after the third periodic recovery scan",
            1, xaResource.rollbackCount());
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

        orphanSafetyIntervalOrigin = jtaPropertyManager.getJTAEnvironmentBean().getOrphanSafetyInterval();
        jtaPropertyManager.getJTAEnvironmentBean().setOrphanSafetyInterval(0);
        xaRecoveryNodesOrigin = jtaPropertyManager.getJTAEnvironmentBean().getXaRecoveryNodes();
    }
    
    @After
    public void tearDown () throws Exception
    {
        jtaPropertyManager.getJTAEnvironmentBean().setOrphanSafetyInterval(orphanSafetyIntervalOrigin);
        jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(xaRecoveryNodesOrigin);

        myOA.destroy();
        myORB.shutdown();
    }

    /**
     * Helper class for being able to work with multiple {@link Xid}s, each for the separate {@link XAResource}
     * under subordinate jca transaction.
     * Normally when call SubordinationManager.getTransactionImporter().importTransaction(xid) is used
     * the JCA then uses the same Xid for all enlisted resources.
     * That's how {@link com.arjuna.ats.internal.jta.transaction.jts.TransactionImple#createXid} works.
     */
    class DummyTransactionImple extends com.arjuna.ats.internal.jta.transaction.jts.subordinate.TransactionImple
    {
        public DummyTransactionImple(AtomicTransaction imported)
        {
            super(imported);
        }

        public void commitAndDisassociate () throws javax.transaction.RollbackException, javax.transaction.HeuristicMixedException,
            javax.transaction.HeuristicRollbackException, java.lang.SecurityException, javax.transaction.SystemException, java.lang.IllegalStateException
        {
            super.commitAndDisassociate();
        }

        public void rollbackAndDisassociate () throws java.lang.IllegalStateException, java.lang.SecurityException, javax.transaction.SystemException
        {
            super.rollbackAndDisassociate();
        }
    }
    
    private ORB myORB = null;
    private RootOA myOA = null;
    int orphanSafetyIntervalOrigin;
    List<String> xaRecoveryNodesOrigin = null;
}
