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
 * $Id: ServerTransactionRecoveryModule.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.recovery.transactions;

import org.omg.CosTransactions.*;

import java.util.*;

import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.objectstore.*;
import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.jts.utils.*;
import com.arjuna.ats.arjuna.exceptions.*;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;

import com.arjuna.ats.jts.logging.jtsLogger;

import java.io.IOException;

/**
 * This class is a plug-in module for the recovery manager.
 * It is responsible for the recovery of server transactions
 *
 */

public class ServerTransactionRecoveryModule extends TransactionRecoveryModule
		implements RecoveryModule
{
    public ServerTransactionRecoveryModule ()
    {
	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("ServerTransactionRecoveryModule created");
    }
	
	if (_transactionType == null)
	    _transactionType = ServerTransaction.typeName();
    }

    /**
     * This is called periodically by the RecoveryManager
     */
    public void periodicWorkFirstPass ()
    {
        jtsLogger.i18NLogger.info_recovery_transactions_ServerTransactionRecoveryModule_3();
	super.periodicWorkFirstPass();
    }

    public void periodicWorkSecondPass ()
    {
        jtsLogger.i18NLogger.info_recovery_transactions_ServerTransactionRecoveryModule_4();
	//super.periodicWorkSecondPass();


	
	// Process the Vector of transaction Uids
	
	Enumeration transactionUidEnum = _transactionUidVector.elements();
	while (transactionUidEnum.hasMoreElements())
	    {
		Uid currentUid = (Uid) transactionUidEnum.nextElement();
		
		try
		    {
		     // Is the intentions list still there? Is this the best way to check?
		     if (_transactionStore.currentState(currentUid, _transactionType) != StateStatus.OS_UNKNOWN)
		       {
                   jtsLogger.i18NLogger.info_recovery_transactions_ServerTransactionRecoveryModule_5(currentUid);
    			   recoverTransaction(currentUid);
		       } else {
			   if (jtsLogger.logger.isDebugEnabled()) {
                   jtsLogger.logger.debug("ServerTransactionRecoveryModule - Transaction "+currentUid+" still in state unknown (?).");
               }
		       }
		    }
		catch (ObjectStoreException e4)
		    {
			if (jtsLogger.logger.isDebugEnabled()) {
                jtsLogger.logger.debug("ServerTransactionRecoveryModule - Transaction "+currentUid+" is not in object store - assumed completed");
            }
		    }
	    }

    }
    
    /**
     * Set-up routine
     */
    protected void initialise ()
    {
	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("ServerTransactionRecoveryModule.initialise()");
    }
	super.initialise();
    }



    protected void recoverTransaction (Uid tranUid)
    { 
	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("ServerTransactionRecoveryModule.recoverTransaction()" + tranUid + ")");
    }
       Status currentStatus = Status.StatusUnknown;
       
       CachedRecoveredTransaction cachedRecoveredTransaction = new CachedRecoveredTransaction (tranUid, _transactionType);
       
       currentStatus = cachedRecoveredTransaction.get_status();
      
       if (jtsLogger.logger.isDebugEnabled()) {
           jtsLogger.logger.debug("Activated transaction "+tranUid+" status = "+Utility.stringStatus(currentStatus));
       }
       // but first check that the original transaction isn't in mid-flight
       if ( cachedRecoveredTransaction.originalBusy() && (currentStatus != Status.StatusPrepared) ) 
	   {
	       if (jtsLogger.logger.isDebugEnabled()) {
               jtsLogger.logger.debug("Transaction "+tranUid+" still busy");
           }
	       return;
	   }
       
       cachedRecoveredTransaction.replayPhase2();
       cachedRecoveredTransaction = null;
    }
	
}


