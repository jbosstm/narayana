/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator.listener;

import com.arjuna.ats.arjuna.common.Uid;

/**
 * An instance of this interface will be called whenever a transaction is either timed-out
 * or set rollback-only by the transaction reaper.
 * 
 * @author marklittle
 */

public interface ReaperMonitor
{
    /**
     * The indicated transaction has been rolled back by the reaper.
     * 
     * @param txId the transaction id.
     */
    
    public void rolledBack (Uid txId);
    
    /**
     * The indicated transaction has been marked as rollback-only by the reaper.
     * 
     * @param txId the transaction id.
     */
    
    public void markedRollbackOnly (Uid txId);
    
    // TODO notify of errors?
}