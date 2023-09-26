/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.recovery;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;

public class EmbeddedRecoveryTest
{
    @Test
    public void test()
    {
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);

        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        DummyRecoveryModule module = new DummyRecoveryModule();

        // make sure no other modules registered for this test

        manager.removeAllModules(false);

        manager.addModule(module);

        manager.scan();

        assertTrue(module.finished());
    }
}