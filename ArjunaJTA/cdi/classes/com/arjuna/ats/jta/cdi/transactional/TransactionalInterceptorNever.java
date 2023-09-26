/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.ats.jta.cdi.transactional;

import com.arjuna.ats.jta.logging.jtaLogger;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.TransactionalException;

/**
 * @author paul.robinson@redhat.com 25/05/2013
 */

@Interceptor
@Transactional(Transactional.TxType.NEVER)
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 200)
public class TransactionalInterceptorNever extends TransactionalInterceptorBase {
    public TransactionalInterceptorNever() {
        super(true);
    }

    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Exception {
        return super.intercept(ic);
    }

    @Override
    protected Object doIntercept(TransactionManager tm, Transaction tx, InvocationContext ic) throws Exception {
        if (tx != null) {
            throw new TransactionalException(jtaLogger.i18NLogger.get_tx_never(), new InvalidTransactionException());
        }
        return invokeInNoTx(ic);
    }
}