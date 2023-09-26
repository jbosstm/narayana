/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.recovery;

import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.internal.arjuna.recovery.PeriodicRecovery;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertTrue;

public class PeriodicRecoveryTest {
    @Test
    public void testInitialDelay() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        RecoveryEnvironmentBean bean = recoveryPropertyManager.getRecoveryEnvironmentBean().setPeriodicRecoveryInitilizationOffset(1);
        PeriodicRecovery periodicRecovery = new PeriodicRecovery(false, false);
        Method doInitialWait = periodicRecovery.getClass().getDeclaredMethod("doInitialWait");
        doInitialWait.setAccessible(true);
        long l = System.currentTimeMillis();
        try {
            doInitialWait.invoke(periodicRecovery, null);
        } finally {
            doInitialWait.setAccessible(false);
        }
        assertTrue(System.currentTimeMillis() - l > 500);
    }
}