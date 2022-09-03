/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
