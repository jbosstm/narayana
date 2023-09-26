/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.performance.products;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;

import jakarta.transaction.Transaction;
import jakarta.transaction.SystemException;

/**
 * TODO
 */
public class JBossTSTxWrapper implements TxWrapper
{
    AtomicAction tx;

    public JBossTSTxWrapper()
    {
    }

    public TxWrapper createWrapper()
    {
        return new JBossTSTxWrapper();
    }

    public Transaction getTransaction() throws SystemException
    {
        return com.arjuna.ats.jta.TransactionManager.transactionManager().getTransaction();
    }

    public int begin()
    {
        this.tx = new AtomicAction();
        
        return tx.begin();
    }

    public int commit()
    {
        return tx.commit();
    }

    public int abort()
    {
        return tx.abort();
    }

    public int add(AbstractRecord record)
    {
        return tx.add(record);
    }

    public boolean supportsNestedTx()
    {
        return true;
    }

    public String getName()
    {
        return "JBossTS";
    }
}