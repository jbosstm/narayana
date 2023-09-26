/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.recovery.transactions;

import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.jts.logging.jtsLogger;


// todo - make add a protected getTransactionType() method

/**
 * This class is a plug-in module for the recovery manager.
 * It is responsible for the recovery of server transactions
 *
 */

public class TopLevelTransactionRecoveryModule extends TransactionRecoveryModule
		    implements RecoveryModule
{
    public TopLevelTransactionRecoveryModule ()
    {
	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("TopLevelTransactionRecoveryModule created");
    }

	// Set the transaction type that this module wants to recover
	if (_transactionType == null)
	    _transactionType = ArjunaTransactionImple.typeName();
    }

    /**
     * This is called periodically by the RecoveryManager
     */
    public void periodicWorkFirstPass ()
    {
        jtsLogger.i18NLogger.info_recovery_transactions_TopLevelTransactionRecoveryModule_3();
	super.periodicWorkFirstPass();
    }

    public void periodicWorkSecondPass ()
    {
        jtsLogger.i18NLogger.info_recovery_transactions_TopLevelTransactionRecoveryModule_4();
	super.periodicWorkSecondPass();
    }

    /**
     * Set-up routine
     */
    protected void initialise ()
    {
	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("TopLevelTransactionRecoveryModule.initialise()");
    }
	super.initialise();
    }

};