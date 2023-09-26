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

public class TransactionExecutorBegin implements Callable<Integer>
{
    public TransactionExecutorBegin (int timeout, AtomicAction tx)
    {
        _timeout = timeout;
        _theTransaction = tx;
    }

    @Override
    public Integer call () throws Exception
    {
        return _theTransaction.begin(_timeout);
    }
    
    private int _timeout;
    private AtomicAction _theTransaction;
}