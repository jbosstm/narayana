/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.common.util.logging.*;

import java.io.IOException;

/**
 * This class is a plug-in module for the recovery manager.
 * It is responsible for the recovery of server transactions
 * 
 * @message com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_1 [com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_1] - ServerTransactionRecoveryModule created
 * @message com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_2 [com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_2] - ServerTransactionRecoveryModule destroyed
 * @message com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_3 [com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_3] - ServerTransactionRecoveryModule - First Pass
 * @message com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_4 [com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_4] - ServerTransactionRecoveryModule - Second Pass
 * @message com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_5 [com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_5] - ServerTransactionRecoveryModule - Transaction {0} still in ActionStore
 * @message com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_6 [com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_6] - ServerTransactionRecoveryModule - Transaction {0} still in state unknown (?). 
 * @message com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_7 [com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_7] - ServerTransactionRecoveryModule - Transaction {0} is not in object store - assumed completed
 * @message com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_8 [com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_8] - Activated transaction {0} status = {1}
 * @message com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_9 [com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_9] - Transaction {0} still busy

 */

public class ServerTransactionRecoveryModule extends TransactionRecoveryModule
		implements RecoveryModule
{
    public ServerTransactionRecoveryModule ()
    {
	if (jtsLogger.loggerI18N.isDebugEnabled())
	  {
	      jtsLogger.loggerI18N.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, 
					 FacilityCode.FAC_CRASH_RECOVERY, 
					 "com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_1");
	  }
	
	if (_transactionType == null)
	    _transactionType = ServerTransaction.typeName();
    }

    public void finalize () throws Throwable
    {
	super.finalize();
	if (jtsLogger.loggerI18N.isDebugEnabled())
	  {
	      jtsLogger.loggerI18N.debug(DebugLevel.DESTRUCTORS, VisibilityLevel.VIS_PUBLIC, 
					 FacilityCode.FAC_CRASH_RECOVERY, 
					 "com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_2");
	  }
    }

    /**
     * This is called periodically by the RecoveryManager
     */
    public void periodicWorkFirstPass ()
    {
	if (jtsLogger.loggerI18N.isInfoEnabled())
	  {
	      jtsLogger.loggerI18N.info("com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_3");
	  }
	super.periodicWorkFirstPass();
    }

    public void periodicWorkSecondPass ()
    {
	if (jtsLogger.loggerI18N.isInfoEnabled())
	  {
	      jtsLogger.loggerI18N.info("com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_4");
	  }
	//super.periodicWorkSecondPass();


	
	// Process the Vector of transaction Uids
	
	Enumeration transactionUidEnum = _transactionUidVector.elements();
	while (transactionUidEnum.hasMoreElements())
	    {
		Uid currentUid = (Uid) transactionUidEnum.nextElement();
		
		try
		    {
		     // Is the intentions list still there? Is this the best way to check?
		     if (_transactionStore.currentState(currentUid, _transactionType) != ObjectStore.OS_UNKNOWN)
		       {
			   if (jtsLogger.loggerI18N.isInfoEnabled())
			       {
				   jtsLogger.loggerI18N.info("com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_5", new Object[]{currentUid});
			       }
			   recoverTransaction(currentUid);
		       } else {
			   if (jtsLogger.loggerI18N.isDebugEnabled())
			       {
				   jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
							      VisibilityLevel.VIS_PUBLIC, 
							      FacilityCode.FAC_CRASH_RECOVERY, 
							      "com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_6", new Object[]{currentUid});
			       }
		       }
		    }
		catch (ObjectStoreException e4)
		    {
			if (jtsLogger.loggerI18N.isDebugEnabled())
			       {
				   jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
							      VisibilityLevel.VIS_PUBLIC, 
							      FacilityCode.FAC_CRASH_RECOVERY, 
							      "com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_7", new Object[]{currentUid});
			       }
		    }
	    }

    }
    
    /**
     * Set-up routine
     */
    protected void initialise ()
    {
	if (jtsLogger.logger.isDebugEnabled())
	    {
		jtsLogger.logger.debug(DebugLevel.FUNCTIONS, 
				       VisibilityLevel.VIS_PUBLIC, 
				       FacilityCode.FAC_CRASH_RECOVERY, 
				       "ServerTransactionRecoveryModule.initialise()");
	    }
	super.initialise();
    }



    protected void recoverTransaction (Uid tranUid)
    { 
	if (jtsLogger.logger.isDebugEnabled())
	    {
		jtsLogger.logger.debug(DebugLevel.FUNCTIONS, 
				       VisibilityLevel.VIS_PUBLIC, 
				       FacilityCode.FAC_CRASH_RECOVERY, 
				       "ServerTransactionRecoveryModule.recoverTransaction()"+tranUid+")");
	    }
       Status currentStatus = Status.StatusUnknown;
       
       CachedRecoveredTransaction cachedRecoveredTransaction = new CachedRecoveredTransaction (tranUid, _transactionType);
       
       currentStatus = cachedRecoveredTransaction.get_status();
      
       if (jtsLogger.loggerI18N.isDebugEnabled())
	   {
	       jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
					  VisibilityLevel.VIS_PUBLIC, 
					  FacilityCode.FAC_CRASH_RECOVERY, 
					  "com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_8", new Object[]{tranUid, Utility.stringStatus(currentStatus)});
	   }
       // but first check that the original transaction isn't in mid-flight
       if ( cachedRecoveredTransaction.originalBusy() && (currentStatus != Status.StatusPrepared) ) 
	   {
	       if (jtsLogger.loggerI18N.isDebugEnabled())
		   {
		       jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
						  VisibilityLevel.VIS_PUBLIC, 
						  FacilityCode.FAC_CRASH_RECOVERY, 
						  "com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule_9", new Object[]{tranUid});
		   }
	       return;
	   }
       
       cachedRecoveredTransaction.replayPhase2();
       cachedRecoveredTransaction = null;
    }
	
}


