/*
 * SPDX short identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import jakarta.transaction.TransactionSynchronizationRegistry;

import org.junit.Test;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;
import com.hp.mwtests.ts.jta.common.Synchronization;

/**
 * Exercise the TransactionSynchronizationRegistry implementation.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-03
 */
public class TransactionSynchronizationRegistryTest
{
    @Test
    public void testTSR() throws Exception {

        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        TransactionSynchronizationRegistry tsr = new TransactionSynchronizationRegistryImple();

        assertNull(tsr.getTransactionKey());

        assertEquals(tm.getStatus(), tsr.getTransactionStatus());

        tm.begin();

        assertNotNull(tsr.getTransactionKey());
        assertEquals(tm.getStatus(), tsr.getTransactionStatus());

        String key = "key";
        Object value = new Object();
        assertNull(tsr.getResource(key));
        tsr.putResource(key, value);
        assertEquals(value, tsr.getResource(key));

        Synchronization synchronization = new com.hp.mwtests.ts.jta.common.Synchronization();
        tsr.registerInterposedSynchronization(synchronization);

        assertFalse(tsr.getRollbackOnly());
        tsr.setRollbackOnly();
        assertTrue(tsr.getRollbackOnly());

        boolean gotExpectedException = false;
        try {
            tsr.registerInterposedSynchronization(synchronization);
        } catch (IllegalStateException e) {
            gotExpectedException = true;
        }
        assertTrue(gotExpectedException);

        tm.rollback();

        assertEquals(tm.getStatus(), tsr.getTransactionStatus());
    }

    @Test
    public void testTSRUseAfterCompletion() throws Exception {

        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        final CompletionCountLock ccl = new CompletionCountLock(2);
        tm.begin();
        final TransactionSynchronizationRegistry tsr = new TransactionSynchronizationRegistryImple();
        tsr.registerInterposedSynchronization(new Synchronization() {
            @Override
            public void afterCompletion(int status) {
                String key = "key";
                Object value = new Object();
                try {
                    tsr.getResource(key);
                    ccl.incrementFailedCount();
                } catch (IllegalStateException ise) {
                    // Expected
                    ccl.incrementCount();
                }
                try {
                    tsr.putResource(key, value);
                    ccl.incrementFailedCount();
                } catch (IllegalStateException ise) {
                    // Expected
                    ccl.incrementCount();
                }
            }

            @Override
            public void beforeCompletion() {
                // TODO Auto-generated method stub

            }
        });

        tm.commit();

        assertTrue(ccl.waitForCompletion());

    }

    private class CompletionCountLock {
        private int successCount;
        private int failedCount;
        private int maxCount;

        public CompletionCountLock(int maxCount) {
            this.maxCount = maxCount;
        }

        public synchronized boolean waitForCompletion() throws InterruptedException {
            while (successCount + failedCount < maxCount) {
                wait();
            }
            return failedCount == 0;
        }

        public synchronized void incrementCount() {
            this.successCount++;
            this.notify();
        }

        public synchronized void incrementFailedCount() {
            this.failedCount++;
            this.notify();
        }
    }
}