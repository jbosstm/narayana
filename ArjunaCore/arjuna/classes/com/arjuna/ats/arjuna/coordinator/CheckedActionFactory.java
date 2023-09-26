/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.common.Uid;

/**
 * Manages the CheckedAction instances on a per transaction basis.
 */

public interface CheckedActionFactory
{
    /**
     * Return a CheckedAction instance for a transaction. The same instance can be
     * given to multiple transactions.
     * 
     * @param txId the transaction id.
     * @param actionType the type of the transaction.
     * @return the CheckedAction instance.
     */
    
    public CheckedAction getCheckedAction (final Uid txId, final String actionType);
}