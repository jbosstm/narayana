/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.recovery;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;

public class RecoveryManagerTest
{
    @Test
    public void test()
    {
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);
        System.setProperty("com.arjuna.ats.jta.xaRecoveryNode", "1");
        System.setProperty("XAResourceRecovery1", "com.hp.mwtests.ts.jta.recovery.DummyXARecoveryResource");

        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);

        manager.scan();
        manager.scan();
        System.clearProperty("com.arjuna.ats.jta.xaRecoveryNode");
        System.clearProperty("XAResourceRecovery1");
    }
}