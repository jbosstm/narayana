/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.recovery.transactions;

import java.util.Hashtable;

import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.Status;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.jts.logging.jtsLogger;

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
		    if (jtsLogger.logger.isDebugEnabled()) {
                jtsLogger.logger.debug("asking the tran for original status");
            }
		    theStatus = theTransaction.getOriginalStatus();
		} else {
		    if (jtsLogger.logger.isDebugEnabled()) {
                jtsLogger.logger.debug("no transaction in cache so not asking for original status");
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
    public static ReplayPhaseReturnStatus replayPhase2 (Uid actionUid, String theType)
    {
    ReplayPhaseReturnStatus returnStatus = ReplayPhaseReturnStatus.STANDARD_PROCESSING;

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
				if (converting && (jtsLogger.logger.isDebugEnabled())) {
                    jtsLogger.logger.debug(" Transaction "+actionUid+" assumed complete - changing type.");
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
                jtsLogger.i18NLogger.info_recovery_transactions_TransactionCache_4(actionUid);
			    theTransaction.removeOldStoreEntry();
			    cacheItem.updateType();
			    returnStatus = ReplayPhaseReturnStatus.ASSUME_COMPLETED;
			}

		    }
		}

		/*
		 * Now remove the transaction from the cache, only removing
		 * the item if it is truly completed.
		 */

		if (fullyCompleted) {
            jtsLogger.i18NLogger.info_recovery_transactions_TransactionCache_5(actionUid);

		    remove(actionUid);

		    // should leave in cache for a while

		} else {
		    cacheItem.clearTransaction();  // just force a reactivate later
		}
	    }
	}
	return returnStatus;
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
	    if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("TransactionCache.remove "+theUid+": transaction not in cache");
        }
	}
	else
	{
	    synchronized (cacheItem) 
	    {
		_theCache.remove(theUid);
	    }
	    
	    if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("TransactionCache.remove "+theUid+": removed transaction from cache");
        }
	}
    }

    public static enum ReplayPhaseReturnStatus {
        STANDARD_PROCESSING, ASSUME_COMPLETED
    }
    
    private static final Hashtable _theCache = new Hashtable();
    private static final int attemptsBeforeConversion = jtsPropertyManager.getJTSEnvironmentBean()
            .getCommitedTransactionRetryLimit();
}