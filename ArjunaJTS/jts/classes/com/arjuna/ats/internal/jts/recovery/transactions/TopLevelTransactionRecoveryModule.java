/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TopLevelTransactionRecoveryModule.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.recovery.transactions;

import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;

import com.arjuna.ats.arjuna.recovery.RecoveryModule;

import com.arjuna.ats.jts.logging.jtsLogger;

import com.arjuna.common.util.logging.*;


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

    public void finalize () throws Throwable
    {
	super.finalize();
	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("TopLevelTransactionRecoveryModule destoryed");
    }
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


