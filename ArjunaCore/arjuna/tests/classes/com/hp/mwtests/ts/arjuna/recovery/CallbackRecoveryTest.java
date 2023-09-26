/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.recovery;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryScan;

class RecoveryScanImple implements RecoveryScan
{
    public synchronized void completed()
    {
        passed = true;
        notify();
        notified = true;
    }

    public synchronized void waitForCompleted(int msecs_timeout)
    {
        if (!notified) {
            try {
                wait(msecs_timeout);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    public boolean passed = false;
    private boolean notified = false;
}

public class CallbackRecoveryTest
{
    @Test
    public void test()
    {
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);
        
        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        DummyRecoveryModule module = new DummyRecoveryModule();
        RecoveryScanImple rs = new RecoveryScanImple();

        // make sure no other modules registered for this test
        
        manager.removeAllModules(false);
        
        manager.addModule(module);

        manager.scan(rs);

        /*
         * the 30 second wait timeout here is just in case something is not working. the scan should
         * finish almost straight away
         */
        rs.waitForCompleted(30 * 1000);

        assertTrue(module.finished());
        assertTrue(rs.passed);
    }
}