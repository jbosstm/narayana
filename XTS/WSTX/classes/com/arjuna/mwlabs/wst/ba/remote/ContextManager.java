/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mwlabs.wst.ba.remote;

import com.arjuna.mw.wst.TxContext;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;

/**
 */

// publish via JNDI for each address space?

public class ContextManager
{

    public ContextManager ()
    {
    }
    
    // resume overwrites. Should we check first a la JTA?

    public void resume (TxContext tx) throws UnknownTransactionException, SystemException
    {
        _threadTxData.set(tx);
    }
    
    public TxContext suspend () throws SystemException
    {
        final TxContext ctx = currentTransaction() ;
        if (ctx != null)
        {
            _threadTxData.set(null) ;
        }
	return ctx;
    }

    public TxContext currentTransaction () throws SystemException
    {
	return (TxContext) _threadTxData.get();
    }
    
    private static ThreadLocal _threadTxData = new ThreadLocal();
    
}