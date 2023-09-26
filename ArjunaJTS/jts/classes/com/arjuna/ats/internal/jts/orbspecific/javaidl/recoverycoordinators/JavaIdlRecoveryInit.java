/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.orbspecific.javaidl.recoverycoordinators;

import com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCreator;
import com.arjuna.ats.internal.jts.recovery.recoverycoordinators.RcvCoManager;
import com.arjuna.ats.jts.logging.jtsLogger;

/**
 * Initialises Java IDL orb RecoveryCoordinator IOR creation mechanism
 *
 * An instance of this class is constructed by RecoveryEnablement and 
 * registered as an OAAttribute whose initialise method is called after
 * root POA is set up
 *
 * All orbs are likely to be the same, constructing a GenericRecoveryCreator,
 * but with an orb-specific manager
 *
 */

public class JavaIdlRecoveryInit
{
    public JavaIdlRecoveryInit()
    {
        RcvCoManager theManager = new JavaIdlRCManager();

        // and register it (which will cause creation of a GenericRecoveryCreator
        // and it's registration with CosTransactions)
        GenericRecoveryCreator.register(theManager);

        if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("JavaIdl RecoveryCoordinator creator setup");
        }
    }

}