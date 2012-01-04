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
 * $Id: CachedRecoveredTransaction.java 2342 2006-03-30 13:06:17Z  $
 */


package com.arjuna.ats.internal.jts.recovery.transactions;

import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.Status;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.ats.jts.utils.Utility;

/**
 * Any transaction that is identified by the recovery system to
 * require recovery is manipulated through
 * CachedRecoveredTransactions. CachedRecoveredTransactions delegate
 * through to a static transaction cache which ensures that there is
 * no interference between different threads that may be working with
 * the same transaction 
 * <P>
 * @author Dave Ingham (dave@arjuna.com)
 * @version $Id: CachedRecoveredTransaction.java 2342 2006-03-30 13:06:17Z  $
 * @see TransactionCache
 *
 */
public class CachedRecoveredTransaction
{
    public CachedRecoveredTransaction ( Uid actionUid, String theType )
    {
	_theTransactionUid = new Uid (actionUid);
	_theTransactionType = theType;

	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("CachedRecoveredTransaction created ["+_theTransactionUid+", "+_theTransactionType+"]");
    }
    }
    
    public Uid getTransactionUid()
    {
	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("CachedRecoveredTransaction.getTransactionUid() ="
                + _theTransactionUid);
    }
	return _theTransactionUid;
    }

    /**
     * Get the status of the transaction
     */
    public synchronized Status get_status () throws SystemException
    {

	Status theStatus = TransactionCache.get_status(_theTransactionUid, _theTransactionType);

	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("CachedRecoveredTransaction.get_status ["
                + _theTransactionUid + ", " + _theTransactionType + "] = "
                + Utility.stringStatus(theStatus));
    }
	return theStatus;
    }

    /* THIS LOGIC IS MISPLACED - it needs a javatmpl */
    boolean /* sync ? */ originalBusy()
    {
	Status originalStatus = getOriginalStatus();

	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("CachedRecoveredTransaction.originalBusy - told status is "+Utility.stringStatus(originalStatus));
    }

	switch (originalStatus.value()) {
	    // original process dead or finished with tran
	    case Status._StatusNoTransaction:
		return false;
		
	    // these states can only come from a process that is still alive
	    case Status._StatusActive:
	    case Status._StatusMarkedRollback:
	    case Status._StatusPreparing:
	    case Status._StatusCommitting:
	    case Status._StatusRollingBack:
	    case Status._StatusPrepared:
		return true;
	    
	    // the transaction is apparently still there, but has completed its
	    // phase2. should be safe to redo it (this argument is a bit shaky)
	    case Status._StatusCommitted:
	    case Status._StatusRolledBack:
		return false;
				
	    // this shouldn't happen - assume busy
	    case Status._StatusUnknown:
	    default:
		return true;
	}
    }
    
    /**
     *  what is the status of the transaction in the original process ? (if alive)
     */
    Status /* sync ? */ getOriginalStatus ()
    {
	return TransactionCache.getOriginalStatus(_theTransactionUid,_theTransactionType);
    }
    
    /** 
     * Get the recovery status of the transaction
     */
    public int getRecoveryStatus ()
    {
	return TransactionCache.getRecoveryStatus(_theTransactionUid, _theTransactionType);
    }

    /**
     * Add a new resource to a recovered transaction. This is
     * primarily to allow a new resource that has been provided
     * through a replay_completion to be added to the transaction and
     * thereby replacing the original resource that was passed in on
     * register_resource.  
     */
    public void addResourceRecord (Uid rcUid, Resource r)
    {
	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("CachedRecoveredTransaction.addResourceRecord ["
                + _theTransactionUid + ", " + _theTransactionType + "]"
                + "(" + rcUid + ")");
    }
	TransactionCache.addResourceRecord(_theTransactionUid, _theTransactionType, rcUid, r);
    }

    /**
     * Replays phase 2 of the transaction.
     */
    public void replayPhase2()
    {	

	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("CachedRecoveredTransaction.replayPhase2 ["
                + _theTransactionUid + ", " + _theTransactionType + "]");
    }

	TransactionCache.replayPhase2(_theTransactionUid, _theTransactionType);
    }

    private Uid    _theTransactionUid = null;
    private String _theTransactionType = null;
}
