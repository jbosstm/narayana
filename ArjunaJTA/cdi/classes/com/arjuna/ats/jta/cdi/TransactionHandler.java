/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.ats.jta.cdi;

import com.arjuna.ats.jta.logging.jtaLogger;

import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;

public final class TransactionHandler {

    /**
     * For cases that the transaction should be marked for rollback
     * ie. when {@link RuntimeException} is thrown or when {@link Error} is thrown
     * or when the exception si marked in {@link Transactional#rollbackOn()}
     * then {@link Transaction#setRollbackOnly()} is invoked.
     */
    public static void handleExceptionNoThrow(Transactional transactional, Throwable t, Transaction tx)
            throws IllegalStateException, SystemException {

        for (Class<?> dontRollbackOnClass : transactional.dontRollbackOn()) {
            if (dontRollbackOnClass.isAssignableFrom(t.getClass())) {
                return;
            }
        }

        for (Class<?> rollbackOnClass : transactional.rollbackOn()) {
            if (rollbackOnClass.isAssignableFrom(t.getClass())) {
                tx.setRollbackOnly();
                return;
            }
        }

        // RuntimeException and Error are un-checked exceptions and rollback is expected
        if (t instanceof RuntimeException || t instanceof Error) {
            tx.setRollbackOnly();
            return;
        }
    }

    /**
     * <p>
     * It finished the transaction.
     * </p>
     * <p>
     * Call {@link TransactionManager#rollback()} when the transaction si marked for {@link Status#STATUS_MARKED_ROLLBACK}.
     * otherwise the transaction is committed.
     * Either way there is executed the {@link Runnable} 'afterEndTransaction' after the transaction is finished.
     * </p>
     */
    public static void endTransaction(TransactionManager tm, Transaction tx, RunnableWithException afterEndTransaction) throws Exception {
        try {
            if (tx != tm.getTransaction()) {
                throw new RuntimeException(jtaLogger.i18NLogger.get_wrong_tx_on_thread());
            }

            if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                tm.rollback();
            } else {
                tm.commit();
            }
        } finally {
            afterEndTransaction.run();
        }
    }
}