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
 * $Id: TransactionRecoveryModule.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.recovery.transactions;

import org.omg.CosTransactions.*;

import java.util.*;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.objectstore.*;
import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.jts.utils.*;
import com.arjuna.ats.arjuna.exceptions.*;

import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.common.util.logging.*;

import java.io.IOException;

/**
 * This class is a plug-in module for the recovery manager.  This is a
 * generic class from which TopLevel and Server transaction recovery
 * modules inherit.
 *
 * This class does not implement {@link com.arjuna.CosRecovery.RecoveryModule}
 * (the plug-in definition) itself - this is left to the subclass.
 *
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_1 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_1] - TransactionRecoveryModule created
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_2 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_2] - TransactionRecoveryModule: transaction type not set
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_3 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_3] - TransactionRecoveryModule: scanning for {0}
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_4 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_4] - TransactionRecoveryModule: Object store exception: 
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_5 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_5] - found transaction  {0}
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_6 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_6] - Transaction {0} still in ActionStore
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_7 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_7] - Transaction {0} in state unknown (?).
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_8 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_8] - Transaction {0} is not in object store - assumed completed
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_9 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_9] - Activated transaction {0} status = {1}
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_10 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_10] - Transaction {0} still busy
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_11 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_11] - TransactionRecoveryModule.periodicWorkFirstPass()
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_12 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_12] - TransactionRecoveryModule.periodicWorkSecondPass()
 */
public abstract class TransactionRecoveryModule
{
    public TransactionRecoveryModule ()
    {
	if (jtsLogger.loggerI18N.isDebugEnabled())
	    {
		jtsLogger.loggerI18N.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, 
					   FacilityCode.FAC_CRASH_RECOVERY, 
					   "com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_1");
	    }

	if (_transactionStore == null)
	{
	    _transactionStore = TxControl.getStore();
	}
    }

    /**
     * This is called periodically by the RecoveryManager
     */
    protected void periodicWorkFirstPass ()
    {
	if (jtsLogger.loggerI18N.isInfoEnabled())
	    {
		jtsLogger.loggerI18N.info("com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_11");
	    }
	// Sanity check - make sure we know what type of transaction we're looking for
	if (_transactionType == null)
	{
	    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_2");
	    return;
	}

	// Build a Vector of transaction Uids found in the ObjectStore
	_transactionUidVector = new Vector();
	InputObjectState uids = new InputObjectState();

	boolean anyTransactions = false;

	try
	{
	    if (jtsLogger.loggerI18N.isDebugEnabled())
	    {
		jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, 
					   FacilityCode.FAC_CRASH_RECOVERY, 
					   "com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_3", new Object[]{_transactionType});
	    }

	    anyTransactions = _transactionStore.allObjUids(_transactionType, uids);
	}
	catch (ObjectStoreException e1)
	{
	    jtsLogger.loggerI18N.warn("om.hp.mwlabs.ts.jts.recovery.transactions.TransactionRecoveryModule_4", e1);
	}

	if (anyTransactions)
	{
	    Uid theUid = null;

	    boolean moreUids = true;

	    while (moreUids)
	    {
		try
		{
		    theUid = UidHelper.unpackFrom(uids);

		    if (theUid.equals(Uid.nullUid()))
		    {
			moreUids = false;
		    }
		    else
		    {
			Uid newUid = new Uid (theUid);

			if (jtsLogger.loggerI18N.isDebugEnabled())
			    {
				jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
							   VisibilityLevel.VIS_PUBLIC, 
							   FacilityCode.FAC_CRASH_RECOVERY, 
							   "com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_5", new Object[]{newUid});
			    }
			_transactionUidVector.addElement(newUid);
		    }
		}
		catch (Exception e2)
		{
		    moreUids = false;
		}
	    }
	}
    }

    /*
     * We may have caught some transactions in flight that are
     * going to complete normally. We'll wait a short time
     * before rechecking if they are still around. If so, we
     * process them.
     */

    protected void periodicWorkSecondPass ()
    {
	if (jtsLogger.loggerI18N.isInfoEnabled())
	    {
		jtsLogger.loggerI18N.info("com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_12");
	    }

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
		    if (jtsLogger.loggerI18N.isInfoEnabled())
		    {
			jtsLogger.loggerI18N.info("com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_6", new Object[]{currentUid});
		    }

		    recoverTransaction(currentUid);
		} else {
		    // Transaction has gone away - probably completed normally
		    if (jtsLogger.loggerI18N.isDebugEnabled())
			{
			    jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
						       VisibilityLevel.VIS_PUBLIC, 
						       FacilityCode.FAC_CRASH_RECOVERY, 
						       "com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_7", new Object[]{currentUid});
			}
		}
	    }
	    catch (ObjectStoreException e4)
	    {
		// Transaction has gone away - probably completed normally
	
		if (jtsLogger.loggerI18N.isDebugEnabled())
		    {
			jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
						   VisibilityLevel.VIS_PUBLIC, 
						   FacilityCode.FAC_CRASH_RECOVERY, 
						   "com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_8", new Object[]{currentUid});
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
					   "TransactionRecoveryModule.initialise()");
	    }
    }

    private void recoverTransaction (Uid tranUid)
	//protected void recoverTransaction (Uid tranUid)
    {
	if (jtsLogger.logger.isDebugEnabled())
	    {
		jtsLogger.logger.debug(DebugLevel.FUNCTIONS, 
				       VisibilityLevel.VIS_PUBLIC, 
				       FacilityCode.FAC_CRASH_RECOVERY, 
				       "TransactionRecoveryModule.recoverTransaction("+tranUid+")");
	    }
	
	Status currentStatus = Status.StatusUnknown;

	CachedRecoveredTransaction cachedRecoveredTransaction = new CachedRecoveredTransaction (tranUid, _transactionType);

	currentStatus = cachedRecoveredTransaction.get_status();

	if (jtsLogger.loggerI18N.isDebugEnabled())
	    {
		jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
					   VisibilityLevel.VIS_PUBLIC, 
					   FacilityCode.FAC_CRASH_RECOVERY, 
					   "com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_9", new Object[]{tranUid, Utility.stringStatus(currentStatus)});
	    }

	// but first check that the original transaction isn't in mid-flight
	if ( cachedRecoveredTransaction.originalBusy() ) 
	{
	    if (jtsLogger.loggerI18N.isDebugEnabled())
		{
		    jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
					       VisibilityLevel.VIS_PUBLIC, 
					       FacilityCode.FAC_CRASH_RECOVERY, 
					       "com.arjuna.ats.internal.jts.recovery.transactions.TransactionRecoveryModule_10", new Object[]{tranUid});
		}
	    return;
	}
		
	cachedRecoveredTransaction.replayPhase2();
	cachedRecoveredTransaction = null;
    }

    protected String	   _transactionType = null;
    //private static ObjectStore _transactionStore = null;

    //private Vector	     _transactionUidVector;

    protected static ObjectStore _transactionStore = null;

    protected Vector	     _transactionUidVector;


    /*
     * Read the properties to set the configurable options
     */
    static
    {
	 // TBD: Inventory.inventory().addToList(new OTS_RecoveryResourceRecordSetup());
    }
};


