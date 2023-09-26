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

public class TransactionExecutorCommit implements Callable<Integer>
{
    public TransactionExecutorCommit (boolean reportHeuristics, AtomicAction tx)
    {
        _heuristics = reportHeuristics;
        _theTransaction = tx;
    }

    @Override
    public Integer call () throws Exception
    {
        return _theTransaction.commit(_heuristics);
    }
    
    private boolean _heuristics;
    private AtomicAction _theTransaction;
}