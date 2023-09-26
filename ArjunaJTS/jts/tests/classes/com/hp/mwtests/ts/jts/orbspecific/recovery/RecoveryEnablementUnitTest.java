/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.recovery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.arjuna.ats.internal.jts.ORBManager;
import org.junit.After;
import org.junit.Test;

import com.arjuna.ats.internal.jts.orbspecific.recovery.RecoveryEnablement;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class RecoveryEnablementUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        ORBManager.reset();

        RecoveryEnablement rec = new RecoveryEnablement();
        
        assertTrue(rec.startRCservice());
        
        RecoveryEnablement.isNotANormalProcess();
        
        assertFalse(RecoveryEnablement.isNormalProcess());
        
        assertTrue(RecoveryEnablement.getRecoveryManagerTag() != null);
    }
}