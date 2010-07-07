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
import com.arjuna.ats.arjuna.objectstore.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.jts.utils.*;
import com.arjuna.ats.arjuna.exceptions.*;

import com.arjuna.ats.jts.logging.jtsLogger;

/**
 * This class is a plug-in module for the recovery manager.  This is a
 * generic class from which TopLevel and Server transaction recovery
 * modules inherit.
 *
 * This class does not implement {@link com.arjuna.CosRecovery.RecoveryModule}
 * (the plug-in definition) itself - this is left to the subclass.
 *
 */
public abstract class TransactionRecoveryModule
{
    public TransactionRecoveryModule ()
    {
	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("TransactionRecoveryModule created");
    }

	if (_recoveryStore == null)
	{
	    _recoveryStore = StoreManager.getRecoveryStore();
	}
    }

    /**
     * This is called periodically by the RecoveryManager
     */
    protected void periodicWorkFirstPass ()
    {
        jtsLogger.i18NLogger.info_recovery_transactions_TransactionRecoveryModule_11();
	// Sanity check - make sure we know what type of transaction we're looking for
	if (_transactionType == null) {
        jtsLogger.i18NLogger.warn_recovery_transactions_TransactionRecoveryModule_2();
        return;
    }

	// Build a Vector of transaction Uids found in the ObjectStore
	_transactionUidVector = new Vector();
	InputObjectState uids = new InputObjectState();

	boolean anyTransactions = false;

	try
	{
	    if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("TransactionRecoveryModule: scanning for "+_transactionType);
        }

	    anyTransactions = _recoveryStore.allObjUids(_transactionType, uids);
	}
	catch (ObjectStoreException e1)
	{
        jtsLogger.i18NLogger.warn_recovery_transactions_TransactionRecoveryModule_4(e1);
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

			if (jtsLogger.logger.isDebugEnabled()) {
                jtsLogger.logger.debug("found transaction "+newUid);
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
        jtsLogger.i18NLogger.info_recovery_transactions_TransactionRecoveryModule_12();

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
            jtsLogger.i18NLogger.info_recovery_transactions_TransactionRecoveryModule_6(currentUid);

		    recoverTransaction(currentUid);
		} else {
		    // Transaction has gone away - probably completed normally
		    if (jtsLogger.logger.isDebugEnabled()) {
                jtsLogger.logger.debug("Transaction "+currentUid+" in state unknown (?)");
            }
		}
	    }
	    catch (ObjectStoreException e4)
	    {
		// Transaction has gone away - probably completed normally
	
		if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("Transaction "+currentUid+" is not in object store - assumed completed");
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
        jtsLogger.logger.debug("TransactionRecoveryModule.initialise()");
    }
    }

    private void recoverTransaction (Uid tranUid)
	//protected void recoverTransaction (Uid tranUid)
    {
	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("TransactionRecoveryModule.recoverTransaction(" + tranUid + ")");
    }
	
	Status currentStatus = Status.StatusUnknown;

	CachedRecoveredTransaction cachedRecoveredTransaction = new CachedRecoveredTransaction (tranUid, _transactionType);

	currentStatus = cachedRecoveredTransaction.get_status();

	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("Activated transaction "+tranUid+" status = "+Utility.stringStatus(currentStatus));
    }

	// but first check that the original transaction isn't in mid-flight
	if ( cachedRecoveredTransaction.originalBusy() ) 
	{
	    if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("Transaction "+tranUid+" still busy");
        }
	    return;
	}
		
	cachedRecoveredTransaction.replayPhase2();
	cachedRecoveredTransaction = null;
    }

    protected String	   _transactionType = null;
    //private static ObjectStore _recoveryStore = null;

    //private Vector	     _transactionUidVector;

    protected static RecoveryStore _recoveryStore = null;

    protected Vector	     _transactionUidVector;


    /*
     * Read the properties to set the configurable options
     */
    static
    {
	 // TBD: Inventory.inventory().addToList(new OTS_RecoveryResourceRecordSetup());
    }
};


