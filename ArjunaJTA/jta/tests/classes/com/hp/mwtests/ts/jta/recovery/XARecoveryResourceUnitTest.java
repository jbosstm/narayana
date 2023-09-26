/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.recovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.transaction.xa.XAException;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryResourceImple;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryResourceManagerImple;
import com.arjuna.ats.jta.recovery.XARecoveryResource;
import com.hp.mwtests.ts.jta.common.DummyXA;

class DummyImple extends XARecoveryResourceImple
{
    public DummyImple ()
    {
        super(new Uid());
    }
    
    public boolean notAProblem (XAException ex, boolean commit)
    {
        return super.notAProblem(ex, commit);
    }
}

public class XARecoveryResourceUnitTest
{
    @Test
    public void test()
    {
        XARecoveryResourceManagerImple xarr = new XARecoveryResourceManagerImple();
        
        assertTrue(xarr.getResource(new Uid()) != null);
        assertTrue(xarr.getResource(new Uid(), null) != null);
        assertTrue(xarr.type() != null);
    }
    
    @Test
    public void testRecoveryResource ()
    {
        XARecoveryResourceImple res = new XARecoveryResourceImple(new Uid());
        
        assertEquals(res.getXAResource(), null);
        assertEquals(res.recoverable(), XARecoveryResource.INCOMPLETE_STATE);
        
        res = new XARecoveryResourceImple(new Uid(), new DummyXA(false));
        
        assertEquals(res.recoverable(), XARecoveryResource.RECOVERY_REQUIRED);
        assertEquals(res.recover(), XARecoveryResource.WAITING_FOR_RECOVERY);
    }
    
    @Test
    public void testNotAProblem ()
    {
        DummyImple impl = new DummyImple();
        
        assertTrue(impl.notAProblem(new XAException(XAException.XAER_NOTA), true));
        assertFalse(impl.notAProblem(new XAException(XAException.XA_HEURHAZ), true));
    }
}