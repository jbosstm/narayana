/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.jts.recovery;

import java.util.Arrays;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.recovery.ExpiryScanner;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryModule;

public class ExpiryScannerStartupUnitTest {
    @Test
    public void testExpiryBackground() throws Exception {
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryModuleClassNames(Arrays.asList(new String[] { XARecoveryModule.class.getName() }));
        recoveryPropertyManager.getRecoveryEnvironmentBean().setExpiryScanners(Arrays.asList(new ExpiryScanner[] { new ExpiryScanner() {

            @Override
            public void scan() {
                AbstractRecord.create(172);
            }

            @Override
            public boolean toBeUsed() {
                return true;
            }
        } }));

        recoveryPropertyManager.getRecoveryEnvironmentBean().setPeriodicRecoveryPeriod(1);
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);

        RecoveryManager rm = RecoveryManager.manager();

        rm.terminate(false);
    }
}