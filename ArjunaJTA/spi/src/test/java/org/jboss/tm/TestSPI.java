/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.tm;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;
import jakarta.transaction.*;

/**
 * Transaction SPI integration tests
 */
@RunWith(Arquillian.class)
public class TestSPI {
    @Inject
    private jakarta.transaction.TransactionManager transactionManager;

    @Deployment
    public static WebArchive createTestArchive() {

        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage("org.jboss.tm")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void sanityCheck() {
        Assert.assertTrue(Boolean.TRUE);
    }

    @Test
    public void testTimeouts() throws Exception {
        Transaction oldTxn = null;
        final int SLEEP = 5;
        final int timeout = 2;
        final boolean[] afterCompletionViaTMTimeoutThread = {false};

        try {
            oldTxn = transactionManager.suspend();
            transactionManager.setTransactionTimeout(timeout);
            transactionManager.begin();

            Transaction newTxn = transactionManager.getTransaction();

            newTxn.registerSynchronization(new Synchronization() {
                @Override
                public void beforeCompletion() {
                }

                @Override
                public void afterCompletion(int status) {
                    System.out.printf("afterCompletion callback. Txn Status=%d%n", status);
                    afterCompletionViaTMTimeoutThread[0] = TxUtils.isTransactionManagerTimeoutThread();
                    Assert.assertTrue("afterCompletion synchronization not called via TransactionManagerTimeoutThread",
                            afterCompletionViaTMTimeoutThread[0]);
                }
            });

            Thread.sleep(SLEEP * 1000); // sleep for longer than the transaction timeout period
            int status = transactionManager.getStatus();

            Assert.assertTrue("ERROR transaction should have timed out but its state is " + status,
                    status == Status.STATUS_ROLLEDBACK);

            transactionManager.commit();

            Assert.assertTrue("testTimeouts synchronization was not invoked", afterCompletionViaTMTimeoutThread[0]);
        } catch (RollbackException e) {
            // SUCCESS: rollback exception expected
            // any other exception is a failure
        } finally {
            try {
                if (transactionManager.getStatus() == Status.STATUS_ACTIVE)
                    transactionManager.rollback();
            } catch (SystemException e) {
                System.err.printf("testTimeouts: cleanup exception %s%n", e.getMessage());
            }
            if (oldTxn != null) {
                try {
                    transactionManager.resume(oldTxn);
                } catch (SystemException e) {
                    System.err.printf("testTimeouts: resume exception %s%n", e.getMessage());
                } catch (InvalidTransactionException e) {
                    System.err.printf("testTimeouts: resume exception %s%n", e.getMessage());
                }
            }
        }

    }
}