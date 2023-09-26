/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.recovery.transactions;

import com.arjuna.ats.arjuna.objectstore.StoreManager;

/**
 * Refinement of the expired assumed scanner for Server transactions.
 * @see AssumedCompleteServerTransaction
 */

public class ExpiredServerScanner extends ExpiredAssumedCompleteScanner
{
    /**
     * Construction is caused by presence of class name as property value.
     * @see com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor
     */
    public ExpiredServerScanner ()
    {
	super(AssumedCompleteServerTransaction.typeName(),StoreManager.getRecoveryStore());
    
    }
}