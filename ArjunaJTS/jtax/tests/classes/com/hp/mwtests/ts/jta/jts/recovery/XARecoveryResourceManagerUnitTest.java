/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.recovery;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryResourceManagerImple;
import com.hp.mwtests.ts.jta.jts.common.DummyXA;


public class XARecoveryResourceManagerUnitTest
{
    @Test
    public void test ()
    {
        XARecoveryResourceManagerImple man = new XARecoveryResourceManagerImple();
        
        assertTrue(man.getResource(new Uid()) != null);
        assertTrue(man.getResource(new Uid(), new DummyXA(false)) != null);
    }
}