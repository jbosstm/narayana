/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.recovery;

import org.junit.Test;

import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;

public class RecoveryModuleUnitTest
{
    @Test
    public void testAA () throws Exception
    {
        AtomicActionRecoveryModule aarm = new AtomicActionRecoveryModule();
        
        aarm.periodicWorkFirstPass();
        aarm.periodicWorkSecondPass();
    }
}