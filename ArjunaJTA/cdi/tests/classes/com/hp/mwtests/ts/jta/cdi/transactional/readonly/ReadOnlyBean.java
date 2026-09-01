/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.cdi.transactional.readonly;

import jakarta.annotation.Resource;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;

/**
 * Deliberately not annotated with Transactional: the
 * {@link AddReadOnlyTransactionalExtension} adds a read-only
 * {@link ReadOnlyTransactionalLiteral} at deployment time (writing
 * Transactional(isReadOnly = true) in source would not compile against
 * jakarta.transaction-api 2.0.1).
 */
public class ReadOnlyBean {

    /** Completion status observed by the last transactional invocation. */
    public static volatile int completionStatus = -1;

    @Resource
    private TransactionSynchronizationRegistry tsr;

    public static void reset() {
        completionStatus = -1;
    }

    public void run() {
        if (tsr.getTransactionStatus() != Status.STATUS_ACTIVE) {
            throw new IllegalStateException("expected an active transaction");
        }

        tsr.registerInterposedSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {
            }

            @Override
            public void afterCompletion(int status) {
                completionStatus = status;
            }
        });
    }

    public void runWithoutSynchronization() {
        if (tsr.getTransactionStatus() != Status.STATUS_ACTIVE) {
            throw new IllegalStateException("expected an active transaction");
        }
    }
}
