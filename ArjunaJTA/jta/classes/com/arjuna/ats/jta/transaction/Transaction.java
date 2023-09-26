/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta.transaction;

import java.util.Map;

import jakarta.transaction.RollbackException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.xa.TxInfo;

/*
 * Extended methods.
 */

public interface Transaction extends jakarta.transaction.Transaction
{

    public static final int XACONNECTION = 0;
    public static final int XAMODIFIER = 1;

    public boolean enlistResource (XAResource xaRes, Object[] params) throws RollbackException, IllegalStateException, jakarta.transaction.SystemException;

    public int getXAResourceState (XAResource xaRes);

    // Methods used to support JTA 1.1 TransactionSynchronizationRegistry implementation
	public Object getTxLocalResource(Object key);
	public void putTxLocalResource(Object key, Object value);
    public boolean isAlive();

    Map<Uid, String> getSynchronizations();
    Map<XAResource, TxInfo> getResources();
    int getTimeout(); // total lifetime set, in seconds
    long getRemainingTimeoutMills(); // time remaining until possible expire, in ms. 0 if unknown.
    
    public Uid get_uid(); // get the tx id.
    
    public Xid getTxId ();  // get the global Xid (no branch qualifier).
}