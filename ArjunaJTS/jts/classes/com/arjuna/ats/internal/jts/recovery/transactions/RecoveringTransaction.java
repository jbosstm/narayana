/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.recovery.transactions;

import java.util.Date;

import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.Status;

import com.arjuna.ats.arjuna.common.Uid;

/**
 * interface for cachable recoveredtransactions
 * 
 * Needed because the different types of recovered transaction inherit from
 * (non-recovery) base types by different routes
 *
 * used by @{link TransactionCache}
 *
 * Some methods are present only in the Recovered[*]Transactions.
 * Some are present in all the base types
 */

public interface RecoveringTransaction
{
    /** only in Recovered{Server}Transaction */
    public void replayPhase2();
    
    /** only in Recovered{Server}Transaction */
    public int getRecoveryStatus ();

    public void addResourceRecord (Uid rcUid, Resource r);
    
    /** present in both base classes OTS_Transaction and OTS_ServerTransaction */
    public Status get_status () throws SystemException;
    public Status getOriginalStatus ();
    
    public boolean allCompleted();
    /**
     * Tell transaction it is assumed to be complete and should convert itself
     * to the appropriate assumed complete type.
     *
     * @returns true if a change is made, false if already assumed complete
     */
    public boolean assumeComplete();
    
    public void removeOldStoreEntry();
    public String type();
    
    /**
     * When was the transaction last attempted. Only used for assumed complete
     * transactions (so perhaps it ought to be in another interface)
     */
    public Date getLastActiveTime();

}