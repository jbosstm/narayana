/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.internal.async;

import java.util.concurrent.Callable;

import com.arjuna.ats.arjuna.AtomicAction;


/**
 * Executes the transaction.
 * 
 * @author marklittle
 *
 */

public class TransactionExecutorAbort implements Callable<Integer>
{
    public TransactionExecutorAbort (AtomicAction tx)
    {
        _theTransaction = tx;
    }

    @Override
    public Integer call () throws Exception
    {
        return _theTransaction.abort();
    }
    
    private AtomicAction _theTransaction;
}