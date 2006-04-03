/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionCache.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.recovery.transactions;

import java.util.Hashtable;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.objectstore.*;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.orbportability.*;
import com.arjuna.common.util.propertyservice.PropertyManager;

import org.omg.CosTransactions.*;
import com.arjuna.ats.jts.recovery.RecoveryEnvironment;

import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.common.util.logging.*;

import org.omg.CORBA.SystemException;

/**
 * The transaction cache ensures that there is no interference between
 * different threads that may be working with the same
 * transaction. The cache automatically removes or refreshes
 * transactions that require no further recovery or re-recovery respectively. Various
 * volatile information for the transaction is also kept in the cache 
 * (e.g. number of attempts to recover since first activated
 * in this RecoveryManager run)
 * <P>
 * @authors Dave Ingham (dave@arjuna.com), Peter Furniss
 * @version $Id: TransactionCache.java 2342 2006-03-30 13:06:17Z  $
 * @see CachedRecoveredTransaction
 * @see RecoveredTransaction
 *
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_1 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_1] - asking the tran for original status
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_2 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_2] - no transaction in cache so not asking for original status
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_3 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_3] - Transaction {0} assumed complete - changing type.
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_4 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_4] - Transaction {0} assumed complete - will not poll any more 
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_5 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_5] - Transaction {0} recovery completed
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_6 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_6] - TransactionCache.remove {0}: transaction not in cache
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_7 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_7] - TransactionCache.remove {0}: removed transaction from cache
 * @message com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_8 [com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_8] - Non-integer value for property {0}
 */

public class TransactionCache
{
    public TransactionCache ( )
    {
    }

    /**
     * Get the status of a transaction
     */
    public static Status get_status (Uid actionUid, String theType) throws SystemException
    {
	Status theStatus = Status.StatusUnknown;
	TransactionCacheItem cacheItem = get(actionUid, theType);

	if (cacheItem != null)
	{
	    synchronized (cacheItem)
	    {
		RecoveringTransaction theTransaction = cacheItem.freshTransaction();
		if (theTransaction != null)
		{
		    theStatus = theTransaction.get_status();
		    // remember the status in the cacheitem
		    cacheItem.setStatus(theStatus);
		}
	    }
	}
	return theStatus;
    }

    /**
     * Get the status of a transaction that is already in the cache
     */
    public static Status getCachedStatus (Uid actionUid) throws SystemException
    {
	TransactionCacheItem cacheItem = getKnown (actionUid);

	if (cacheItem != null)
	{
	    return cacheItem.getStatus();
	}

	return Status.StatusNoTransaction;  // used to mean it isn't cached
    }

    /**
     * Get the status of a transaction as it is in the original process
     * (so type is not needed)
     * NoTransaction means the original process has gone
     */
    public static Status getOriginalStatus (Uid actionUid, String theType) throws SystemException
    {
	Status theStatus = Status.StatusUnknown;
	TransactionCacheItem cacheItem = get (actionUid, theType);

	if (cacheItem != null)
	{
	    synchronized (cacheItem)
	    {
		RecoveringTransaction theTransaction = cacheItem.freshTransaction();
		if (theTransaction != null)
		{
		    if (jtsLogger.loggerI18N.isDebugEnabled())
			{
			    jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
						       VisibilityLevel.VIS_PUBLIC, 
						       FacilityCode.FAC_CRASH_RECOVERY, 
						       "com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_1");
			}
		    theStatus = theTransaction.getOriginalStatus();
		} else {
		    if (jtsLogger.loggerI18N.isDebugEnabled())
			{
			    jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
						       VisibilityLevel.VIS_PUBLIC, 
						       FacilityCode.FAC_CRASH_RECOVERY, 
						       "com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_2");
			}
		}
	    }
	}
	return theStatus;
    }

    public static int getRecoveryStatus (Uid actionUid, String theType)
    {
	int theRecoveryStatus = RecoveryStatus.NEW;
	TransactionCacheItem cacheItem = get (actionUid, theType);

	if (cacheItem != null)
	{
	    synchronized (cacheItem)
	    {
		RecoveringTransaction theTransaction = cacheItem.transaction();
		if (theTransaction != null)
		{
		    theRecoveryStatus = theTransaction.getRecoveryStatus();
		}
	    }
	}
	return theRecoveryStatus;
    }


    /**
     * Add a new resource to a recovered transaction. This is
     * primarily to allow a new resource that has been provided
     * through a replay_completion to be added to the transaction and
     * thereby replacing the original resource that was passed in on
     * register_resource.
     */
    public static void addResourceRecord (Uid actionUid, String theType, Uid rcUid, Resource r)
    {
	TransactionCacheItem cacheItem = get (actionUid, theType);

	if (cacheItem != null)
	{
	    synchronized (cacheItem)
	    {
		RecoveringTransaction theTransaction = cacheItem.freshTransaction();
		if (theTransaction != null)
		{
		    // As long as the transaction activated okay then try and add
		    // the record.
		    if (theTransaction.getRecoveryStatus() != RecoveryStatus.ACTIVATE_FAILED) {
			theTransaction.addResourceRecord(rcUid, r);
		    }
		    // with a new resource record, start counting attempts from zero
		    cacheItem.resetAttemptCount();
		}
	    }
	}
    }

    /**
     * Replays phase 2 of a transaction.
     */
    public static void replayPhase2 (Uid actionUid, String theType)
    {
	TransactionCacheItem cacheItem = get (actionUid, theType);

	if (cacheItem != null)
	{
	    synchronized (cacheItem)
	    {
		boolean fullyCompleted = false;
		RecoveringTransaction theTransaction = cacheItem.freshTransaction();

		if (theTransaction != null)
		{
		    // As long as the transaction activated okay then
		    // try to replay phase 2.
		    if (theTransaction.getRecoveryStatus() != RecoveryStatus.ACTIVATE_FAILED)
		    {
			/* if the transaction is known to be committed, make only a
			 * limited number of attempts before assuming the subordinate
			 * resources have received a commit order. In case they have not
			 * the transaction will be preserved as "assumedcomplete". If the
			 * subordinate sends a replay_completion, the transaction will be
			 * reactivated, and a commit sent (to the new resource reference)
			 * This only applies to transactions that are known to be committed -
			 * a server transaction in prepared state will retry indefinitely
			 *
			 * attempt count is only for this run of the recovery manager, so
			 * is kept by the cache, not the transaction
			 */
			boolean converting = false;
			if ( cacheItem.getStatus() == Status.StatusCommitted )
			{
			    // will skip this if transaction previously unknown
			    int previousAttempts = cacheItem.countAttempts();
			    if (previousAttempts >= attemptsBeforeConversion) {
				converting = theTransaction.assumeComplete();
				if (converting && (jtsLogger.loggerI18N.isDebugEnabled()))
				    {
					jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
								   VisibilityLevel.VIS_PUBLIC, 
								   FacilityCode.FAC_CRASH_RECOVERY, 
								   "com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_3", new Object[]{actionUid});
				    }
			    }
			}
			// replayPhase2 will cause a re-persist unless it completes
			// in which case it will cause a removal, so we mark it for 
			// removal from the cache
			theTransaction.replayPhase2();

			cacheItem.setStatus(theTransaction.get_status());

			/*
			 * This appears to be always false. Why?!
			 */

			fullyCompleted = theTransaction.allCompleted(); // only remove if committed?

			if (converting && !fullyCompleted) {
			    if (jtsLogger.loggerI18N.isInfoEnabled())
				{
				    jtsLogger.loggerI18N.info("com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_4", new Object[]{actionUid});
				}
			    theTransaction.removeOldStoreEntry();
			    cacheItem.updateType();
			}

		    }
		}

		/*
		 * Now remove the transaction from the cache, only removing
		 * the item if it is truly completed.
		 */

		if (fullyCompleted) {
		    if (jtsLogger.loggerI18N.isInfoEnabled())
			{
			    jtsLogger.loggerI18N.info("com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_5", new Object[]{actionUid});
			}

		    remove(actionUid);

		    // should leave in cache for a while

		} else {
		    cacheItem.clearTransaction();  // just force a reactivate later
		}
	    }
	}
    }

    // get an item that is already known - or nothing
    private static synchronized TransactionCacheItem getKnown (Uid theUid)
    {
	TransactionCacheItem cacheItem = (TransactionCacheItem) _theCache.get(theUid);

	return cacheItem;
    }

    private static synchronized TransactionCacheItem get (Uid theUid, String theType)
    {
	TransactionCacheItem cacheItem = (TransactionCacheItem) _theCache.get(theUid);

	if (cacheItem == null)
	{
	    // No entry in cache -> create it
	    cacheItem = new TransactionCacheItem(theUid, theType);
	    _theCache.put(theUid, cacheItem);
	}
	return cacheItem;
    }

    private static void remove (Uid theUid)
    {
	TransactionCacheItem cacheItem = (TransactionCacheItem) _theCache.get(theUid);

	if (cacheItem == null)
	{
	    if (jtsLogger.loggerI18N.isDebugEnabled())
		{
		    jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
					       VisibilityLevel.VIS_PUBLIC, 
					       FacilityCode.FAC_CRASH_RECOVERY, 
					       "com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_6", new Object[]{theUid});
		}
	}
	else
	{
	    synchronized (cacheItem) 
	    {
		_theCache.remove(theUid);
	    }
	    
	    if (jtsLogger.loggerI18N.isDebugEnabled())
		{
		    jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
					       VisibilityLevel.VIS_PUBLIC, 
					       FacilityCode.FAC_CRASH_RECOVERY, 
					       "com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_7", new Object[]{theUid});
		}
	}
    }
    
    private static Hashtable _theCache = new Hashtable();
    private static int attemptsBeforeConversion = 3;

 static
     {
	String retryLimitString = jtsPropertyManager.propertyManager.getProperty(com.arjuna.ats.jts.recovery.RecoveryEnvironment.COMMITTED_TRANSACTION_RETRY_LIMIT);

	if (retryLimitString != null)
	{
	    try
	    {
		Integer i = new Integer(retryLimitString);

		attemptsBeforeConversion = i.intValue();
	    }
	    catch (Exception e)
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache_8", new Object[]{RecoveryEnvironment.COMMITTED_TRANSACTION_RETRY_LIMIT}); 
	    }
	}
     }


}
