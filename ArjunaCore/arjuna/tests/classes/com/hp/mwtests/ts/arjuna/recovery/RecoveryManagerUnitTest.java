/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.recovery;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;

public class RecoveryManagerUnitTest
{
    @Test
    public void testSuspendResume () throws Exception
    {
        recoveryPropertyManager.getRecoveryEnvironmentBean().setPeriodicRecoveryPeriod(1);
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);

        RecoveryManager rm = RecoveryManager.manager();       
        
        rm.scan(null);
        
        rm.suspend(false);
        rm.resume();
        
        assertTrue(rm.getModules() != null);
        
        rm.removeModule(null, true);
        rm.removeAllModules(true);
        
        rm.terminate(false);
    }
}