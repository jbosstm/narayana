/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.recovery ;

import java.util.ArrayList;
import java.util.List;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.recovery.RecoveryActivator;
import com.arjuna.common.internal.util.ClassloadingUtility;

/**
 * RecoveryActivators are dynamically loaded. The recoveryActivator to load
 * are specified by properties beginning with "recoveryActivator"
 * <P>
 * @author Malik Saheb
 * @since ArjunaTS 3.0
 */

public class RecActivatorLoader
{
    public RecActivatorLoader()
    {
        loadRecoveryActivators();
    }

    // These are loaded in list iteration order.
    private void loadRecoveryActivators ()
    {
        List<String> activatorNames = recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryActivatorClassNames();

        for(String activatorName : activatorNames) {
            RecoveryActivator recoveryActivator = ClassloadingUtility.loadAndInstantiateClass(RecoveryActivator.class, activatorName, null);
            if(recoveryActivator != null) {
                _recoveryActivators.add(recoveryActivator);
            }
        }
    }

    public void startRecoveryActivators() throws RuntimeException
    {
        tsLogger.logger.debug("Start RecoveryActivators");

        for(RecoveryActivator recoveryActivator : _recoveryActivators)
        {
            if(!recoveryActivator.startRCservice()) {
                throw new RuntimeException( tsLogger.i18NLogger.get_recovery_RecActivatorLoader_initfailed(recoveryActivator.getClass().getCanonicalName()));
            }
        }
    }

    // this refers to the recovery activators specified in the recovery manager
    // property file which are dynamically loaded.
    private final List<RecoveryActivator> _recoveryActivators = new ArrayList<RecoveryActivator>();
}