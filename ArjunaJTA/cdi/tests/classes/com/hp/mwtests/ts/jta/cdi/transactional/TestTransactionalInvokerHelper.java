/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.cdi.transactional;

import jakarta.ejb.TransactionManagementType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transactional.TxType;
import jakarta.transaction.UserTransaction;

/**
 * @author <a href="mailto:Tomasz%20Krakowiak%20%3ctomasz.krakowiak@efish.pl%3c">Tomasz Krakowiak
 *         &lt;tomasz.krakowiak@efish.pl&gt;</a>
 */
@ApplicationScoped
public class TestTransactionalInvokerHelper {

    @Inject
    private UserTransaction userTransaction;

    @Inject
    private TestTransactionalInvokerBean bean;

    public Runnable runWithTransactionManagement(TransactionManagementType transactionManagementType, boolean startTransaction, Runnable runnable) {
        switch (transactionManagementType) {
            case CONTAINER:
                if (startTransaction) {
                    return new RunInTxRequired(runnable);
                } else {
                    return new RunInTxSupports(runnable);
                }
            case BEAN:
                if (startTransaction) {
                    return new RunInTxNotSupported(new RunInBmtTransaction(runnable));
                } else {
                    return new RunInTxNotSupported(runnable);
                }
            default:
                throw new RuntimeException("Unexpected transaction management type " + transactionManagementType);
        }
    }

    public Runnable runInTxType(TxType txType, Runnable runnable) {
        switch (txType) {
            case REQUIRED:
                return new RunInTxRequired(runnable);
            case REQUIRES_NEW:
                return new RunInTxRequiresNew(runnable);
            case MANDATORY:
                return new RunInTxMandatory(runnable);
            case NOT_SUPPORTED:
                return new RunInTxNotSupported(runnable);
            case SUPPORTS:
                return new RunInTxSupports(runnable);
            case NEVER:
                return new RunInTxNever(runnable);
            default:
                throw new RuntimeException("Unexpected tx type " + txType);
        }
    }

    public Runnable runAndCheckUserTransactionAvailability(Runnable runnable, boolean expectedAvailable) {
        return new RunAndCheckUserTransactionAvailability(runnable, expectedAvailable);
    }

    private class RunAndCheckUserTransactionAvailability implements Runnable {
        Runnable runnable;
        boolean expectedAvailable;

        public RunAndCheckUserTransactionAvailability(Runnable runnable, boolean expectedAvailable) {
            this.runnable = runnable;
            this.expectedAvailable = expectedAvailable;
        }

        @Override
        public void run() {
            runnable.run();
            try {
                userTransaction.getStatus();
                if (!expectedAvailable) {
                    throw new AssertionError("Expected user transaction to be not available.");
                }
            } catch (IllegalStateException e) {
                if (expectedAvailable) {
                    throw new AssertionError("Expected user transaction to be available.");
                }
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class RunInTxRequired implements Runnable {
        Runnable runnable;

        public RunInTxRequired(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            bean.invokeInTxRequired(runnable);
        }
    }


    private class RunInTxRequiresNew implements Runnable {
        Runnable runnable;

        public RunInTxRequiresNew(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            bean.invokeInTxRequiresNew(runnable);
        }
    }


    private class RunInTxMandatory implements Runnable {
        Runnable runnable;

        public RunInTxMandatory(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            bean.invokeInTxMandatory(runnable);
        }
    }

    private class RunInTxNotSupported implements Runnable {
        Runnable runnable;

        public RunInTxNotSupported(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            bean.invokeInTxNotSupported(runnable);
        }
    }

    private class RunInTxSupports implements Runnable {
        Runnable runnable;

        public RunInTxSupports(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            bean.invokeInTxSupports(runnable);
        }
    }

    private class RunInTxNever implements Runnable {
        Runnable runnable;

        public RunInTxNever(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            bean.invokeInTxNever(runnable);
        }
    }

    private class RunInBmtTransaction implements Runnable {
        Runnable runnable;

        public RunInBmtTransaction(Runnable runnable) {
            this.runnable = runnable;
        }


        @Override
        public void run() {
            try {
                userTransaction.begin();
                try {
                    runnable.run();
                } catch (RuntimeException e) {
                    try {
                        userTransaction.rollback();
                    } catch (Throwable e1) {
                        e.addSuppressed(e1);
                    }
                }
            } catch (NotSupportedException | SystemException e) {
                throw new RuntimeException(e);
            }
        }
    }
}