/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.stm;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import junit.framework.TestCase;
import org.jboss.stm.annotations.Nested;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.internal.RecoverableContainer;
import org.junit.BeforeClass;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * There are no standards for STM and there are different models for how nested transactions behave.
 * The following tests investigate the behaviour of the Narayana Software Transactional Memory
 * implementation with respect to nested transactions.
 * </p>
 *
 * <p>
 * The tests are meant to supplement the STM discussion introduced
 * <a href="https://jbossts.blogspot.com/2018/09/tips-on-how-to-evaluate-stm.html">in an online article</a>
 * and include:
 * <ul>
 *   <li>checking that committing changes made by a closed nested transaction should be visible to the parent</li>
 *   <li>checking that aborting changes made by a closed nested transaction should not be visible to the parent</li>
 * </ul>
 * The tests are ran in two configurations:
 *   <li>run the nested action in the same thread as the parent action</li>
 *   <li>run the nested action in a different thread from the one used by the parent action</li>
 * </p>
 */
public class TaxonomyNestedTest extends TestCase {
    @Transactional // define some transactional memory
    @Nested // with nesting semantics

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

    // define some types for testing running nested transactions in a thread that is
    // different from the thread used to run the top level transaction
    private enum MSG_TYPE {
        EXIT, // indication for the thread to exit
        GET, // the thread should perform a transactional read
        SET // the thread should perform a transactional write
    };

    // define a message type for the parent and child threads to communicate their behaviour
    private static class Message {
        MSG_TYPE what; // the behaviour that the thread should perform
        Integer value; // the value to write if the behaviour is write
        int result; // the value read if the behaviour is read
        Throwable throwable; // the throwable if the read or write failed

        private Message(MSG_TYPE what, Integer value, int result, Throwable throwable) {
            this.what = what;
            this.value = value;
            this.result = result;
            this.throwable = throwable;
        }

        Message(MSG_TYPE what, Integer value) {
            this(what, value, -1, null);
        }
    }

    enum NESTED_TYPE {TOP_LEVEL, COMMIT, ABORT};

    // define a task for performing nested reads and writes to transactional memory
    private class ThreadedReaderWriter extends Thread {
        private AtomicAction atomicAction; // the top level transaction to run the action in
        private NESTED_TYPE nested; // whether or not to start a nested transaction and if so whether to abort or commit
        private AtomicInt atomicInt; // the transactional memory that is read or written to

        ThreadedReaderWriter(AtomicAction atomicAction, NESTED_TYPE nested, AtomicInt atomicInt) {
            this.atomicAction = atomicAction;
            this.nested = nested;
            this.atomicInt = atomicInt;
        }

        public void run () {
            Message msg = null;

            while (true) {
                AtomicAction nestedAction = null;

                try {
                    // see what kind of action the parent thread requires
                    msg = parentRequests.take();

                    if (MSG_TYPE.EXIT.equals(msg.what)) {
                        childRequests.add(msg); // the last action performed by this thread
                        break; // terminate the thread
                    }

                    if (atomicAction != null) {
                        // run the action in the parent thread
                        AtomicAction.resume(atomicAction);
                    }

                    // determine whether the action should run in a nested transaction
                    if (nested != NESTED_TYPE.TOP_LEVEL) {
                        nestedAction = new AtomicAction(); // will be nested under atomicAction

                        nestedAction.begin();
                    }

                    // perform the action
                    switch (msg.what) {
                        default:
                            continue; // nothing to do, look for the next request from the parent
                        case GET:
                            msg.result = atomicInt.get();
                            break; // empty branch since the get is done after the switch
                        case SET:
                            atomicInt.set(msg.value);
                            msg.result = atomicInt.get();
                            break;
                    }

                    if (nestedAction != null) {
                        // end the nested action
                        if (nested == NESTED_TYPE.COMMIT) {
                            nestedAction.commit();
                        } else if (nested == NESTED_TYPE.ABORT) {
                            nestedAction.abort();
                        }
                    }

                    // send the response to the parent thread
                    childRequests.add(msg);

                } catch (final Throwable ex) {
                    if (msg != null) {
                        // report the problem to the parent thread
                        msg.throwable = ex;
                        childRequests.add(msg);
                    }
                } finally {
                    if (atomicAction != null) {
                        // remove the transaction from the thread
                        AtomicAction.suspend();
                    }
                }
            }
        }
    }

    // requests from the parent to child
    private BlockingQueue<Message> parentRequests = new LinkedBlockingQueue<>();
    // responses from the child to the parent
    private BlockingQueue<Message> childRequests = new LinkedBlockingQueue<>();

    // nested transactions come in at least three flavours
    private enum NESTED_MODEL {CLOSED, OPEN, FLAT};
    // the default model is closed nested
    private static NESTED_MODEL model = NESTED_MODEL.CLOSED;

    @BeforeClass
    public void beforeClass() {
        model = NESTED_MODEL.valueOf(System.getProperty("stm.nesting.model", NESTED_MODEL.CLOSED.name()));
    }

    // perform a transactional read or write in another thread
    private int threadedReaderWriter(AtomicAction atomicAction, AtomicInt ai, boolean expectThrowable) throws InterruptedException {
        return threadedReaderWriter(atomicAction, NESTED_TYPE.TOP_LEVEL, ai, null, expectThrowable);
    }

    // perform a transactional read or write in another thread with the option to do it in a nested transaction
    private int threadedReaderWriter(AtomicAction atomicAction, NESTED_TYPE nestedType, AtomicInt ai, Integer newValue,
                                     boolean expectThrowable) throws InterruptedException {
        ThreadedReaderWriter reader = new ThreadedReaderWriter(atomicAction, nestedType, ai);
        MSG_TYPE msgType = newValue != null ? MSG_TYPE.SET : MSG_TYPE.GET;
        Message msg = new Message(msgType, newValue);

        reader.start();

        try {
            parentRequests.add(msg);
            msg = childRequests.take();

            if (expectThrowable && msg.throwable == null) {
                fail("expected a throwable");
            } else if (!expectThrowable && msg.throwable != null) {
                msg.throwable.printStackTrace();
                fail(msg.throwable.getMessage());
            }

            return msg.result;
        } finally {
            // ensure the worker thread exits cleanly
            msg.what = MSG_TYPE.EXIT;
            parentRequests.add(msg);
            childRequests.take();
        }
    }

    /**
     * Test that committing changes made by a closed nested transaction are visible to the parent transaction.
     * The nested action is executed in separate thread from the one used to run the parent action.
     */
    public void testIsClosedNestedCommit() throws Exception {
        AtomicInt ai = new RecoverableContainer<AtomicInt>().enlist(new AtomicIntImpl());
        AtomicAction parent = new AtomicAction();

        ai.set(1); // initialise the shared memory
        parent.begin(); // start a top level transaction
        {
            ai.set(2); // update the memory in the context of a parent transaction
            // other threads shuld see the value in the same transaction
            // read the value in another thread in the same transaction
            assertEquals("wrong value read in another thread in the same transaction",
                    2, threadedReaderWriter(parent, ai, false));

            // an attempt to read the value from a different transaction should fail
            assertEquals("value read in another thread in a different transaction should fail",
                    -1, threadedReaderWriter(null, ai, true));

            // update the value in a nested transaction which commits
            threadedReaderWriter(parent, NESTED_TYPE.COMMIT, ai, 3, false);

            // check that the parent thread sees the value committed in the nested transaction
            assertEquals("Parent thread did not see the write made the nested transaction",
                    3, ai.get());

            // NB other transactions would not see the value 3 however until the parent commits
            // (not demonstrated in this test)
        }
        parent.commit();

        assertEquals(3, ai.get());
    }

    /**
     * Test that aborting changes made by a closed nested transaction are mpt visible to the parent transaction.
     * The nested action is executed in separate thread from the one used to run the parent action.
     */
    public void testIsClosedNestedAbort() throws Exception {
        AtomicInt ai = new RecoverableContainer<AtomicInt>().enlist(new AtomicIntImpl());
        AtomicAction parent = new AtomicAction();

        ai.set(1); // initialise the shared memory
        parent.begin(); // start a top level transaction
        {
            ai.set(2); // update the memory in the context of a parent transaction
            // other threads shuld see the value in the same transaction
            // read the value in another thread in the same transaction
            assertEquals("wrong value read in another thread in the same transaction",
                    2, threadedReaderWriter(parent, ai, false));

            // an attempt to read the value from a different transaction should fail
            assertEquals("value read in another thread in a different transaction should fail",
                    -1, threadedReaderWriter(null, ai, true));

            // update the value in a nested transaction which aborts
            threadedReaderWriter(parent, NESTED_TYPE.ABORT, ai, 3, false);

            // check that the parent thread does not see the value made by the aborted nested transaction
            assertEquals("Parent thread did not see the write made the nested transaction",
                    2, ai.get());
        }
        parent.commit();

        assertEquals(2, ai.get());
    }

    /**
     * Test that aborting changes made by a closed nested transaction are not visible to the parent transaction.
     * The nested action is executed in the same thread as the one used to run the parent action.
     */
    public void testNestedAbort() throws Exception {
        AtomicIntImpl aiImple = new AtomicIntImpl();
        // STM is managed by Containers. Enlisting the above implementation
        // with the container returns a proxy which will enforce STM semantics
        AtomicInt ai = new RecoverableContainer<AtomicInt>().enlist(aiImple);
        AtomicAction outer = new AtomicAction();
        AtomicAction inner = new AtomicAction();

        ai.set(1);
        outer.begin();
        {
            ai.set(2);
            // note that other transactions would see ai == 1
            inner.begin();
            {
                assertEquals(2, ai.get()); // inner sees the outer's changes
                ai.set(3);
                inner.abort();
            }

            int status;

            switch (model) {
                case FLAT:
                    assertEquals(1, ai.get()); // inner abort causes outer to abort too
                    // note that other transactions would see ai == 1

                    status = outer.commit();
                    assertEquals(ActionStatus.ABORTED, status); // assert outer is aborted
                    // other transactions would still see ai == 1
                    break;
                case OPEN:
                    assertEquals("inner abort should have effected the outer", 2, ai.get());
                    // note that other transactions would see ai == 1

                    // assert that out is still active
                    status = outer.commit();
                    assertEquals(ActionStatus.COMMITTED, status); // assert outer is aborted
                    // note that other transactions would see ai == 2
                    break;
                case CLOSED:
                    assertNotNull(" outer should be active", AtomicAction.Current());
                    assertEquals("inner abort should have no effect on the outer", 2, ai.get()); // inner abort has no effect on the outer
                    // (note that other transactions would see ai == 1)
                    status = outer.commit();
                    //assert that outer is committed
                    assertEquals(ActionStatus.COMMITTED, status);
                    // note that other transactions see ai == 2
                    break;
            }
        }
    }

    /**
     * Test that committing changes made by a closed nested transaction are visible to the parent transaction.
     * The nested action is executed in the same thread as the one used to run the parent action.
     */
    public void testNestedCommit() throws Exception {
        AtomicIntImpl aiImple = new AtomicIntImpl();
        // STM is managed by Containers. Enlisting the above implementation
        // with the container returns a proxy which will enforce STM semantics
        AtomicInt ai = new RecoverableContainer<AtomicInt>().enlist(aiImple);
        AtomicAction outer = new AtomicAction();
        AtomicAction inner = new AtomicAction();

        ai.set(1);
        outer.begin();
        {
            ai.set(2);
            // note that other transactions would see ai == 1

            inner.begin();
            {
                assertEquals(2, ai.get()); // inner sees the outer's changes
                ai.set(3);
                inner.commit();
            }

            int status;

            switch (model) {
                case FLAT:
                    assertEquals("outer should npt see the commits made by the innerr",
                            2, ai.get());
                    // note that other transactions would see ai == 1
                    assertNotNull(" outer should be active", AtomicAction.Current());

                    status = outer.commit();
                    assertEquals(ActionStatus.COMMITTED, status);
                    assertEquals("inner commit is not visible to outer after commit",
                            3, ai.get());
                    break;
                case OPEN:
                    assertNotNull(" outer should be active", AtomicAction.Current());
                    assertEquals("inner commit is visible to outer",
                            3, ai.get());
                    // note that other transactions would see ai == 3

                    status = outer.abort(); // aborting the outer should have no effect on the inner's changes
                    assertEquals(ActionStatus.ABORTED, status); // assert outer is aborted
                    // (note that other transactions would see ai == 3)

                    assertEquals("inner commit should be visible to outer even though the outer aborted",
                            3, ai.get());
                    break;
                case CLOSED:
                    assertNotNull(" outer should be active", AtomicAction.Current());
                    assertEquals("inner commit is visible to outer before commit",
                            3, ai.get());
                    // note that other transactions would see ai == 1

                    status = outer.commit();
                    assertEquals(ActionStatus.COMMITTED, status); // assert outer is committed
                    assertEquals("inner commit is visible to outer after commit",
                            3, ai.get());
                    // note that other transactions would see ai == 3
                    break;
            }
        }
    }
}