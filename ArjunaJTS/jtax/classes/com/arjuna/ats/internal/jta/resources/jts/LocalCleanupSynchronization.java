/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.resources.jts;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.SynchronizationRecord;
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

public class LocalCleanupSynchronization implements com.arjuna.ats.arjuna.coordinator.SynchronizationRecord
{
    public LocalCleanupSynchronization (TransactionImple tx)
    {
	_tx = tx;
    }

    public boolean beforeCompletion ()
    {
	return true;
    }

    /**
     * status is ActionStatus
     */
    
    public boolean afterCompletion (int status)
    {
	_tx.shutdown();

	return true;
    }

    @Override
    public boolean isInterposed() {
        return false;
    }

    public Uid get_uid ()
    {
	return _theUid;
    }

    private TransactionImple _tx;
    private Uid              _theUid = new Uid();

    public int compareTo(Object o) {
        SynchronizationRecord sr = (SynchronizationRecord)o;
        if(_theUid.equals(sr.get_uid())) {
            return 0;
        } else {
            return _theUid.lessThan(sr.get_uid()) ? -1 : 1;
        }
    }

}