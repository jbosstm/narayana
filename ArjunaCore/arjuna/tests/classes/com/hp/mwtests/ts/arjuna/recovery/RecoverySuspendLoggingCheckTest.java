/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.recovery;

import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.jboss.byteman.contrib.bmunit.WithByteman;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@WithByteman
@BMUnitConfig(debug = true)
public class RecoverySuspendLoggingCheckTest {

    @Test
    @BMRule(name = "Fail if logging statement executes", targetClass = "com.arjuna.ats.arjuna.logging.arjunaI18NLogger_$logger", targetMethod = "warn_feature_not_supported_across_all_recovery_modules", targetLocation = "AT ENTRY", action = "throw new java.lang.Error(\"JBTM-3894 logging follow-up not solved\")")
    public void testAddModuleNoWarn() {

        RecoveryEnvironmentBean _recoveryConfig = recoveryPropertyManager.getRecoveryEnvironmentBean();
        _recoveryConfig.setWaitForWorkLeftToDo(false);
        _recoveryConfig.setRecoveryModuleClassNames(
                Arrays.asList(new String[] { "com.hp.mwtests.ts.arjuna.recovery.DummyRecoveryModule" }));

        RecoveryManager _manager = RecoveryManager.manager(RecoveryManager.INDIRECT_MANAGEMENT);
        _manager.terminate();
    }

    @Test
    @BMRule(name = "Fail if logging statement doesn't execute", targetClass = "com.arjuna.ats.arjuna.logging.arjunaI18NLogger_$logger", targetMethod = "warn_feature_not_supported_across_all_recovery_modules", targetLocation = "AT ENTRY", action = "System.setProperty(\"Called\", \"true\")")
    public void testAddModuleWarn() {

        RecoveryEnvironmentBean _recoveryConfig = recoveryPropertyManager.getRecoveryEnvironmentBean();
        // don't sign off until the store is empty
        _recoveryConfig.setWaitForWorkLeftToDo(true);
        // the test set of modules
        _recoveryConfig.setRecoveryModuleClassNames(
                Arrays.asList(new String[] { "com.hp.mwtests.ts.arjuna.recovery.DummyRecoveryModule" }));

        try {
            RecoveryManager _manager = RecoveryManager.manager(RecoveryManager.INDIRECT_MANAGEMENT);
            _manager.terminate();
            assertEquals(System.getProperty("Called"), "true");
        } finally {
            System.clearProperty("Called");
        }
    }
}
