/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
     * aborting changes made by a closed nested transaction should not be visible to the parent
     */
    public void testIsClosedNestedAbort() throws Exception {
        AtomicInt ai = new RecoverableContainer<AtomicInt>().enlist(new AtomicIntImpl());
        AtomicAction outer = new AtomicAction();
        AtomicAction inner = new AtomicAction();

        ai.set(1); // initialise the shared memory
        outer.begin(); // start a top level transaction
        {
            ai.set(2); // modify state
            inner.begin(); // start a nested transacction
            {
                ai.set(3); // and modify the state

                inner.abort();
            }
            outer.commit();
        }

        // since the child aborted and nesting follows the closed mode the value should still be 2
        assertEquals(2, ai.get());
    }

    /**
     * committing changes made by a closed nested transaction should be visible to the parent
     */
    public void testIsClosedNestedCommit() throws Exception {
        AtomicInt ai = new RecoverableContainer<AtomicInt>().enlist(new AtomicIntImpl());
        AtomicAction parent = new AtomicAction();
        AtomicAction child = new AtomicAction();

        ai.set(1); // initialise the shared memory
        parent.begin(); // start a top level transaction
        {
            ai.set(2); // update the memory in the context of the parent transaction
            child.begin(); // start a child transaction
            {
                ai.set(3); // update the memory in a child transaction
                // NB the parent would still see the value as 2 (not shown in this test)
                child.commit();
            }
            // but now the parent should see the value as 3 (since the child committed)
            assertEquals(3, ai.get());
            // NB other transactions would not see the value 3 however until the parent commits
            // (not demonstrated in this test)
        }
        parent.commit();

        assertEquals(3, ai.get());
    }

    /**
     * changes made by a closed nested transaction should not be visible to the parent
     */
    public void testIsClosedNestedVisible() throws Exception {
        AtomicInt ai = new RecoverableContainer<AtomicInt>().enlist(new AtomicIntImpl());
        AtomicAction outer = new AtomicAction();
        AtomicAction inner = new AtomicAction();
        final AtomicBoolean aiUpdated = new AtomicBoolean(false);

        Thread ot = new Thread(() -> {
            try {
                waitForCondition(aiUpdated); // wait for the other thread to set the value

                assertEquals(2, ai.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            ai.set(1);
            outer.begin();
            {
                ai.set(2);

                ot.start();

                outer.addThread(ot);

                inner.begin();
                {
                    ai.set(3);
                    // the outer transaction should still see the value as 2
                    updateCondition(aiUpdated); // tell the other thread to continue

                    inner.abort();
                }

                outer.removeThread(ot);
                outer.commit();
            }
        } finally {
            updateCondition(aiUpdated);
        }

        assertEquals(2, ai.get());
    }

    /**
     * changes made by a parent transaction should not be visible to a closed nested transaction
     */
    public void testIsClosedNestedParentNotVisible() throws Exception {
        AtomicInt ai = new RecoverableContainer<AtomicInt>().enlist(new AtomicIntImpl());
        AtomicAction outer = new AtomicAction();
        AtomicAction inner = new AtomicAction();
        final AtomicBoolean aiUpdated = new AtomicBoolean(false);

        Thread ot = new Thread(() -> {
            try {
                waitForCondition(aiUpdated); // wait for the other thread to set the value

                assertEquals(2, ai.get());
                ai.set(4);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            ai.set(1);
            outer.begin();
            {
                ai.set(2);

                ot.start();

                outer.addThread(ot);

                inner.begin();
                {
                    ai.set(3);
                    // the outer transaction should still see the value as 2
                    updateCondition(aiUpdated); // tell the other thread to continue

                    assertEquals(3, ai.get()); // must not see the value 4
                    inner.abort();
                }

                outer.removeThread(ot);
                outer.commit();
            }
        } finally {
            updateCondition(aiUpdated);
        }

        assertEquals(2, ai.get());
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
            } catch (Exception e) {
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
