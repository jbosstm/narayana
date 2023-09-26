/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.recovery.transactions;

import java.util.Enumeration;

import org.omg.CosTransactions.Status;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;
import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.ats.jts.utils.Utility;

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
		     if (_recoveryStore.currentState(currentUid, _transactionType) != StateStatus.OS_UNKNOWN)
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