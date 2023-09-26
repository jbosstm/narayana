/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.recovery.transactions;


import java.util.Hashtable;

import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.Status;

import com.arjuna.ats.arjuna.common.Uid;



/**
 * Threaded object used to replay phase 2 of the commit protocol in a
 * background thread.  
 */
public class RecoveredTransactionReplayer extends Thread
{
    public RecoveredTransactionReplayer( Uid actionUid, String actionType )
    {
	_actionUid = actionUid;
	_actionType = actionType;
	_cachedRecoveredTransaction = new CachedRecoveredTransaction (_actionUid, _actionType);

	_recoveringCache.put(_actionUid, this);
    }
    
    /**
     * @since JTS 2.1.1.
     */

public final void tidyup ()
    {
	/*
	 * Now notify any waiters that this recovery phase has
	 * ended. This is used when multiple resources may call
	 * replay_completion together, and we cannot replace more than
	 * one at a time in the intentions list!
	 */

	synchronized (_actionType)
	    {
		_recoveringCache.remove(_actionUid);

		_actionType.notifyAll();
	    }
    }

    /**
     * Get the status of the recovered transaction
     */
    public final Status getStatus ()
    {
	return _cachedRecoveredTransaction.get_status();
    }

    /**
     * Get the recovery status of the transaction
     */
    public int getRecoveryStatus ()
    {
	return _cachedRecoveredTransaction.getRecoveryStatus();
    }

    /**
     * Swap an old Resource that was registered with the transaction
     * for a new one. The old one is identified by its
     * RecoveryCoordinator that was returned when the Resource was
     * registered. The second parameter is the new Resouce.  
     */

    public final void swapResource (Uid rcUid, Resource r)
    {
	_cachedRecoveredTransaction.addResourceRecord(rcUid, r);
    }
    
    /**
     * Starts the thread to replay phase 2 of the transaction in the background.
     */
    public final void replayPhase2()
    {
	start();
    }
    
    public final void run()
    {
	_cachedRecoveredTransaction.replayPhase2();
	_cachedRecoveredTransaction = null;

	tidyup();
    }

    /**
     * @since JTS 2.1.1.
     */

public static Object isPresent (Uid theUid)
    {
	RecoveredTransactionReplayer rp = (RecoveredTransactionReplayer) _recoveringCache.get(theUid);

	if (rp != null)
	    return rp._actionType;
	else
	    return null;
    }
    
    private Uid			       _actionUid = null;
    private String		       _actionType = null;
    private CachedRecoveredTransaction _cachedRecoveredTransaction = null;

private static Hashtable _recoveringCache = new Hashtable();

}