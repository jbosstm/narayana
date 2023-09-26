/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.recovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.transaction.xa.XAException;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryResourceImple;
import com.arjuna.ats.jta.recovery.XARecoveryResource;
import com.hp.mwtests.ts.jta.jts.common.DummyXA;

class DummyXARecoveryResource extends XARecoveryResourceImple
{
    public DummyXARecoveryResource ()
    {
        super(new Uid());
    }
    
    public boolean notAProblem (XAException ex, boolean commit)
    {
        return super.notAProblem(ex, commit);
    }
}


public class XARecoveryResourceImpleUnitTest
{
    @Test
    public void test ()
    {
        XARecoveryResourceImple xares = new XARecoveryResourceImple(new Uid());
        
        assertEquals(xares.getXAResource(), null);
        assertEquals(xares.recoverable(), XARecoveryResource.INFLIGHT_TRANSACTION);
        
        xares = new XARecoveryResourceImple(new Uid(), new DummyXA(false));
        
        assertEquals(xares.recover(), XARecoveryResource.FAILED_TO_RECOVER);
        
        DummyXARecoveryResource dummy = new DummyXARecoveryResource();
        
        assertTrue(dummy.notAProblem(new XAException(XAException.XAER_NOTA), true));
        assertFalse(dummy.notAProblem(new XAException(XAException.XAER_DUPID), false));
    }
}