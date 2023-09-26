/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.recovery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.transaction.xa.Xid;

import com.arjuna.ats.internal.jta.recovery.arjunacore.NameScopedXAResource;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.recovery.arjunacore.RecoveryXids;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.common.DummyXA;
import com.hp.mwtests.ts.jta.common.TestResource;


public class RecoveryXidsUnitTest
{
    @Test
    public void test()
    {
        TestResource tr = new TestResource();
        RecoveryXids rxids = new RecoveryXids(new NameScopedXAResource(tr, null));
        Xid[] xids = new XidImple[2];

        xids[0] = new XidImple(new Uid());
        xids[1] = new XidImple(new Uid());

        RecoveryXids dup1 = new RecoveryXids(new NameScopedXAResource(new DummyXA(false), null));
        RecoveryXids dup2 = new RecoveryXids(new NameScopedXAResource(tr, null));

        assertFalse(rxids.equals(dup1));
        assertTrue(rxids.equals(dup2));

        rxids.nextScan(xids);
        rxids.nextScan(xids);

        xids[1] = new XidImple(new Uid());

        rxids.nextScan(xids);

        Object[] trans = rxids.toRecover();
        assertEquals(0, trans.length);

        try {
            Thread.sleep(20010);
        } catch(InterruptedException e) {}


        rxids.nextScan(xids); // force cleanup.
        trans = rxids.toRecover();

        assertEquals(2, trans.length);

        assertTrue( trans[0].equals(xids[0]) || trans[1].equals(xids[0]));
        assertTrue( trans[0].equals(xids[1]) || trans[1].equals(xids[1]));

        assertTrue(rxids.contains(xids[0]));

        assertFalse(rxids.updateIfEquivalentRM(new NameScopedXAResource(new TestResource(), null), null));
        assertTrue(rxids.updateIfEquivalentRM(new NameScopedXAResource(new TestResource(), null), xids));

        assertFalse(rxids.isSameRM(new NameScopedXAResource(new TestResource(), null)));
    }
}