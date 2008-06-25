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
 * Copyright (C) 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionCacheItem.java 2342 2006-03-30 13:06:17Z  $
 */


package com.arjuna.ats.internal.jts.recovery.transactions;

import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.objectstore.*;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.*;
import com.arjuna.common.util.propertyservice.PropertyManager;

import org.omg.CosTransactions.*;

import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.common.util.logging.*;


/**
 * Would be an inner class of TransactionCache, except it is used by a static
 * method of TransactionCache.
 * (handles the creation of different kinds of transaction).
 *
 *  @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionCacheItem_1 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionCacheItem_1] - Transaction {0} previously assumed complete
 *  @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionCacheItem_2 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionCacheItem_2] - TransactionCacheItem.loadTransaction - unknown type: {0}
 */
class TransactionCacheItem
{
    private Uid		      _uid;
    private RecoveringTransaction _transaction;
    private String		  _type;
    private int		      _attempts;    
    private Status		  _knownStatus;
    
    TransactionCacheItem(Uid uid,String type)
    {
	_uid = new Uid(uid);   // copy as usual (I wonder ... )
	_type = type;
	_attempts=0;
	// NoTransaction is used to mean that the status is not
	// actually known
	_knownStatus = Status.StatusNoTransaction;
	loadTransaction(true);
    }
    
    /**
     *  activate the transaction. Allow for the possibility that the transaction
     *  has been assumed complete since it was last heard of
     */
    private boolean loadTransaction(boolean firstLoad)
    {
	if (_type.equals(ArjunaTransactionImple.typeName()))
	{
	    _transaction = new RecoveredTransaction(_uid);
	    if ( firstLoad && _transaction.getRecoveryStatus() == RecoveryStatus.ACTIVATE_FAILED) {
		//  this is a previously unknown transaction, and its not in the store
		//  perhaps it was previously assumed complete
		RecoveringTransaction assumed = new AssumedCompleteTransaction(_uid);
		if ( assumed.getRecoveryStatus() != RecoveryStatus.ACTIVATE_FAILED ) {
		    if (jtsLogger.loggerI18N.isDebugEnabled())
			{
			    jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
						       VisibilityLevel.VIS_PUBLIC, 
						       FacilityCode.FAC_CRASH_RECOVERY, 
						       "com.arjuna.ats.internal.jts.recovery.transactions.TransactionCacheItem_1, new Object[]{_uid}");
			}
		    _transaction = assumed;
		    _type = _transaction.type();
		} 
	    }
	}
	else if (_type.equals(ServerTransaction.typeName()))
	{
	    _transaction = new RecoveredServerTransaction(_uid);
	    if ( firstLoad && _transaction.getRecoveryStatus() == RecoveryStatus.ACTIVATE_FAILED) {
		//  this is a previously unknown transaction, and its not in the store
		//  perhaps it was previously assumed complete
		RecoveringTransaction assumed = new AssumedCompleteServerTransaction(_uid);
		if ( assumed.getRecoveryStatus() != RecoveryStatus.ACTIVATE_FAILED ) {
		    if (jtsLogger.loggerI18N.isDebugEnabled())
			{
			    jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
						       VisibilityLevel.VIS_PUBLIC, 
						       FacilityCode.FAC_CRASH_RECOVERY, 
						       "com.arjuna.ats.internal.jts.recovery.transactions.TransactionCacheItem_1, new Object[]{_uid}");
			}
		    _transaction = assumed;
		    _type = _transaction.type();
		} 
	    }
	}
	else if (_type.equals(AssumedCompleteTransaction.typeName()))
	{
	    _transaction = new AssumedCompleteTransaction(_uid);
	}
	else if (_type.equals(AssumedCompleteServerTransaction.typeName()))
	{
	    _transaction = new AssumedCompleteServerTransaction(_uid);
	}
	else
	{
	    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.recovery.transactions.TransactionCacheItem_2", new Object[]{_type});
	    _transaction = null;
	    return false;
	}
	return true;
    }

    /**
     *  the transaction type has been changed
     */
    void updateType()
    {
	_type = _transaction.type();
    }

    /**
     * forget the activated copy of the transaction. Makes it subject to garbage collecting
     */    
    void clearTransaction()
    {
	_transaction = null;
    }
    
    RecoveringTransaction transaction()
    {
	return _transaction;
    }
    
    /**
     *  Make sure the transaction is freshly activated and hasn't been replayed
     *   assumed to be called from code synchronized on the TransactionCacheItem
     */
    
    RecoveringTransaction freshTransaction()
    {
	if (_transaction == null || _transaction.getRecoveryStatus() == RecoveryStatus.REPLAYED)
	{
	    /*
	     * Not sure why we do a reload at all here. But if we do it
	     * based on whether the transaction has really completed, things
	     * stop working. Needs further investigation since there may
	     * be a possible memory leak here.
	     *
	     * TO DO
	     */

	    //	    if (!_transaction.allCompleted())
	    {
		// The transaction has been replayed, but they want a fresh one
		// Destroy this transaction and create a new
		// one thereby reactivating it.
		// Reactivate as the appropriate transaction class

		loadTransaction(false);
	    }
	}

	return _transaction;
    }
    
    /**
     * keep (and return) a counter - used to record repeated failures
     */
    int countAttempts()
    {
	return _attempts++;
    }
    
    /**
     * reset the attempt account
     */
    void resetAttemptCount()
    {
	_attempts = 0;
    }
    
    /**
     * mutator for known status
     */
    void setStatus(Status status)
    {
	_knownStatus = status;
    }
    
    /**
     * mutator for known status
     */
    Status getStatus()
    {
	return _knownStatus;
    }
}
