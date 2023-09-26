/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.resources.jts;

import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;

/**
 * This synchronization is responsible for removing the JTA transaction
 * from the internal table. We don't need one for the purely local JTA
 * implementation, since the transaction implementation will do this
 * itself. However, in the JTS implementation, where a subordinate JTA
 * transaction may be proxied in another JVM, we have to rely on the
 * synchronization to do the garbage collection, since that transaction
 * implementation won't be driven through commit or rollback - it'll go
 * through the 2PC methods at the JTS interposition hierarchy level.
 */

public class CleanupSynchronization implements jakarta.transaction.Synchronization
{
    public CleanupSynchronization (TransactionImple tx)
    {
	_tx = tx;
    }
    
    public void beforeCompletion ()
    {
    }

    public void afterCompletion (int status)
    {
	_tx.shutdown();
    }

    private TransactionImple _tx;
    
}