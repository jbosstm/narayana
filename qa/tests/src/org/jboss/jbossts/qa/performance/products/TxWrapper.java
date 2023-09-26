/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.performance.products;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;

import jakarta.transaction.Transaction;
import jakarta.transaction.SystemException;

/**
 * Minimal interface that TM products should implement for controlling
 * transactions.
 * @see BaseWrapper for the default implementation of this interface from which
 * other implementations may subclass
 */
public interface TxWrapper
{
    /**
     * Objtain a new wrapper
     * @return a wrapper arround a real transaction
     */
    TxWrapper createWrapper();

    /**
     * Start a new transaction
     * @return one of the com.arjuna.ats.arjuna.coordinator.ActionStatus constants
     */
    int begin();

    /**
     * Commit the current transaction
     * @return one of the com.arjuna.ats.arjuna.coordinator.ActionStatus constants
     */
    int commit();
    int abort();

    /**
     * Enlist a resource within the current transaction. For JTA products other than JBossTS
     * this record should be converted to an XAResource and enlisted with the transaction
     * @param record
     * @return one of the com.arjuna.ats.arjuna.coordinator.AddOutcome constants
     */
    int add(AbstractRecord record);
    Transaction getTransaction() throws SystemException;
    boolean supportsNestedTx();
    String getName();
}