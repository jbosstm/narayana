/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.stm;

import junit.framework.TestCase;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.internal.RecoverableContainer;

import com.arjuna.ats.arjuna.AtomicAction;
import java.util.concurrent.atomic.AtomicBoolean;

// these tests are discussed in the article https://jbossts.blogspot.com/2018/09/tips-on-how-to-evaluate-stm.html
public class TaxonomyTest extends TestCase {
    @Transactional
    public interface AtomicInt {
        int get() throws Exception;
        void set(int value) throws Exception;
    }

    public class AtomicIntImpl implements AtomicInt {
        private int state;

        @ReadLock
        public int get() throws Exception {
            return state;
        }

        @WriteLock
        public void set(int value) throws Exception {
            state = value;
        }
    }

    /**
     * Weak Isolation – non transactional code may interfere with transactional code
     * Strong Isolation – non transactional code is upgraded to transactional operations
     *
     * Narayana STM follows the weak isolation model
     */
    public void testWeakIsolation() throws Exception {
        AtomicIntImpl aiImple = new AtomicIntImpl();
        // STM is managed by Containers. Enlisting the above implementation
        // with the container returns a proxy which will enforce STM semantics
        AtomicInt ai = new RecoverableContainer<AtomicInt>().enlist(aiImple);
        AtomicAction tx = new AtomicAction();
        final AtomicBoolean aiUpdated = new AtomicBoolean(false);
        final AtomicBoolean aiUpdatedInOtherThread = new AtomicBoolean(false);

        // set up code that will access the memory outside of a transaction
        Thread ot = new Thread(() -> {
            try {
                synchronized (aiUpdated) {
                    waitForCondition(aiUpdated); // wait for the other thread to set the value

                    // non transactional code should see changes
                    assertEquals(2, aiImple.get());
                    aiImple.set(10);
                    updateCondition(aiUpdatedInOtherThread); // tell the other thread the update happened
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });

        ot.start();

        try {
            ai.set(1); // initialise the shared mmeory
            tx.begin(); // start a transaction
            {
                ai.set(2); // conditionally set the value to 2

                updateCondition(aiUpdated); // trigger the non-transactional code to update the memory
                waitForCondition(aiUpdatedInOtherThread); // and wait for it to do the update

                // weak isolation means that transactional code should see the changes made by non transactional code
                assertEquals(10, ai.get());
                tx.commit(); // commit the changes made to the shared memory
            }

            ot.join();
        } finally {
            updateCondition(aiUpdated);
        }

        // changes made by non transactional code are visible even after commit
        assertEquals(10, ai.get());
        assertEquals(aiImple.get(), ai.get());
    }

    /**
     * Exceptions should have no effect on the running transaction
     */
    public void testExceptionDoesNotAbort() throws Exception {
        AtomicInt ai = new RecoverableContainer<AtomicInt>().enlist(new AtomicIntImpl());
        AtomicAction tx = new AtomicAction();

        ai.set(1);
        tx.begin();
        {
            try {
                ai.set(2);
                throw new Exception();
            } catch (Exception e) {
                assertEquals(2, ai.get());
                // the transaction should still be active
                ai.set(3);
                tx.commit();
            }
        }

        assertEquals(3, ai.get());
    }

    private void waitForCondition(AtomicBoolean condition) {
        int waitAttempts = MAX_WAIT_ATTEMPTS;

        synchronized (condition) {
            while (!condition.get() && waitAttempts-- > 0) {
                try {
                    condition.wait(WAIT_INTERVAL);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        if (!condition.get()) {
            fail("Did not receive condition notify");
        }
    }

    private void updateCondition(AtomicBoolean condition) {
        synchronized (condition) {
            condition.set(true);
            condition.notify();
        }
    }

    private final static int MAX_WAIT_ATTEMPTS = 1;
    private final static long WAIT_INTERVAL = 1000L;
}