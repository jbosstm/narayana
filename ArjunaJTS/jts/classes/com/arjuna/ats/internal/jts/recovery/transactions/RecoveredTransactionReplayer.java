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
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RecoveredTransactionReplayer.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.recovery.transactions;


import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.objectstore.*;

import org.omg.CosTransactions.*;
import java.util.Hashtable;



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
    
    public void finalize () throws Throwable
    {
	super.finalize();

	_actionUid = null;
	_actionType = null;
	_cachedRecoveredTransaction = null;
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
