/*                                                                                                                                                                                                                  
   Copyright The Narayana Authors                                                                                                                                                                                   
   SPDX-License-Identifier: Apache-2.0                                                                                                                                                                              
 */                                                                                                                                                                                                                 
package com.hp.mwtests.ts.arjuna.atomicaction;

import com.arjuna.ats.arjuna.AtomicAction;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.RecordType;

import com.arjuna.ats.arjuna.coordinator.SynchronizationRecord;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseCommitThreadPool;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(JUnit4.class)
class VirtualThreadTest {
    private static final String VIRTUAL_THREADS_GROUP_NAME = "VirtualThreads";
    private static boolean isVirtualThreadPerTaskExecutor;

    @BeforeAll
    public static void beforeClass() {
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncPrepare(true);
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncCommit(true);
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncRollback(true);

        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncBeforeSynchronization(true);
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncAfterSynchronization(true);

        isVirtualThreadPerTaskExecutor =
                TwoPhaseCommitThreadPool.getExecutorClassName().equals("java.util.concurrent.ThreadPerTaskExecutor");
    }

    @org.junit.Test
    @Test
    // test prepare and commit are called on all registered records
    public void isSane() {
        SimpleAbstractRecord[] ars = {
                new SimpleAbstractRecord(false),
                new SimpleAbstractRecord(false),
                new SimpleAbstractRecord(false)
        };
        AtomicAction aa = new AtomicAction();

        aa.begin();
        Arrays.stream(ars).forEach(aa::add);  // add the records
        aa.commit(true); // report any heuristic outcome

        Arrays.stream(ars).forEach(ar -> assertTrue(ar.prepareCalled));
        Arrays.stream(ars).forEach(ar -> assertFalse(ar.abortCalled));
        Arrays.stream(ars).forEach(ar -> assertTrue(ar.commitCalled));

        // If the VirtualThreadPerTaskExecutor is configured then check which calls ran using virtual threads.
        // These asserts are closely linked to how ArjunaCore implements async operations
        // (ie any change to the implementation must also check that the tests are still valid)
        if (isVirtualThreadPerTaskExecutor) {
            long preparesOnVT = Arrays.stream(ars).filter(
                            r -> VIRTUAL_THREADS_GROUP_NAME.equals(r.getPrepareThreadGroup()))
                    .count();
            long commitsOnVT = Arrays.stream(ars).filter(
                            r -> VIRTUAL_THREADS_GROUP_NAME.equals(r.getCommitThreadGroup()))
                    .count();
            assertEquals(2, preparesOnVT); // only 2 since the last prepare call runs on the callers thread
            // asynchronous commit is not supported if the caller has requested heuristic reporting
            assertEquals(0, commitsOnVT);
        }
    }

    @org.junit.Test
    @Test
    // test that abort is called if one of the records fails to prepare
    public void testPrepareFailReportHeuristics() {
        SimpleAbstractRecord[] ars = {
                new SimpleAbstractRecord(false),
                new SimpleAbstractRecord(true), // prepare fails causing the action to abort
                new SimpleAbstractRecord(false)
        };
        AtomicAction aa = new AtomicAction();

        aa.begin();
        Arrays.stream(ars).forEach(aa::add);
        aa.commit(true); // report heuristics

        Arrays.stream(ars).forEach(ar -> assertTrue(ar.prepareCalled));
        Arrays.stream(ars).forEach(ar -> assertTrue(ar.abortCalled)); // because prepare failed
        Arrays.stream(ars).forEach(ar -> assertFalse(ar.commitCalled));

        if (isVirtualThreadPerTaskExecutor) {
            long preparesOnVT = Arrays.stream(ars).filter(
                            r -> VIRTUAL_THREADS_GROUP_NAME.equals(r.getPrepareThreadGroup()))
                    .count();
            long abortsOnVT = Arrays.stream(ars).filter(
                            r -> r.getAbortThreadGroup().equals("VirtualThreads"))
                    .count();

            // since the middle resource failed to prepare the last one is not asked to prepare since the action aborts
            assertEquals(2, preparesOnVT);
            // asynchronous commit is not supported if the caller has requested heuristic reporting
            assertEquals(0, abortsOnVT); // aborting with a virtual thread
        }
    }

    @org.junit.Test
    @Test
    public void testPrepareFailIgnoreHeuristics() {
        SimpleAbstractRecord[] ars = {
                new SimpleAbstractRecord(1, false),
                new SimpleAbstractRecord(2, true), // prepare fails causing the transaction to abort
                new SimpleAbstractRecord(3, false)
        };
        AtomicAction aa = new AtomicAction();
        // since the abort runs async we need to register a synchronization to determine when it has finished
        final Boolean[] completed = {false};
        var sr = new SynchronizationRecord() {
            final Uid uid = new Uid();

            @Override
            public int compareTo(Object o) {
                return 0;
            }

            @Override
            public Uid get_uid() {
                return uid;
            }

            @Override
            public boolean beforeCompletion() {
                return true;
            }

            @Override
            public boolean afterCompletion(int status) {
                completed[0] = true;
                return true;
            }

            @Override
            public boolean isInterposed() {
                return false;
            }
        };

        aa.begin();
        aa.addSynchronization(sr);
        Arrays.stream(ars).forEach(aa::add);

        int status = aa.commit(false); // do not report heuristics

        // Since we didn't ask commit to report heuristics we aren't guaranteed that it has committed yet
        // But note that the code runs so fast that just the mere fact of registering the synchronization is enough
        // to allow the async abort to finish
        if (status == ActionStatus.COMMITTING) {
            for (int i = 0; i < 10; i++) {
                if (!completed[0]) {
                    Assertions.assertDoesNotThrow(() -> Thread.sleep(50), "interrupted ");
                }
            }
            assertTrue(completed[0], "AtomicAction still committing after 500 ms");
        }

        Arrays.stream(ars).forEach(ar -> assertTrue(ar.prepareCalled));
        Arrays.stream(ars).forEach(ar -> assertTrue(ar.abortCalled));
        Arrays.stream(ars).forEach(ar -> assertFalse(ar.commitCalled));

        if (isVirtualThreadPerTaskExecutor) {
            long preparesOnVT = Arrays.stream(ars).filter(
                            r -> r.getPrepareThreadGroup().equals("VirtualThreads"))
                    .count();
            long abortsOnVT = Arrays.stream(ars).filter(
                            r -> VIRTUAL_THREADS_GROUP_NAME.equals(r.getAbortThreadGroup()))
                    .count();
            // since the middle resource failed to prepare the last one is not asked to prepare since the action aborts
            assertEquals(2, preparesOnVT);
            // in this example commit was requested
            assertEquals(3, abortsOnVT);
        }
    }

//    @org.junit.Test
//    @Test
//    @EnabledForJreRange(min = JRE.JAVA_21)
    public void testCanAbortSuspendedTransaction()
    {
        boolean virtualThreadsSupported = false;
        AtomicAction aa = new AtomicAction();
        aa.begin();
        assertNotNull(AtomicAction.Current());
        AtomicAction.suspend();
        assertNull(AtomicAction.Current());
        SimpleAbstractRecord[] ars = {
                new SimpleAbstractRecord(false),
                new SimpleAbstractRecord(true),
                new SimpleAbstractRecord(false)
        };
        Arrays.stream(ars).forEach(aa::add);
        /*
         * call commit without reporting heuristics so that the commit calls will happen asynchronously
         * this fails because BasicAction.phase2Commit takes synchronizationLock and when calling
         * if (!reportHeuristics && TxControl.asyncCommit
         */
        boolean shouldReportHeuristics = false;
        aa.commit(shouldReportHeuristics); // should abort because the second resource fails the prepare phase
        // all resources should have had prepare called and none should have been asked to commit
        Arrays.stream(ars).forEach(ar -> assertTrue(ar.prepareCalled));
        Arrays.stream(ars).forEach(ar -> assertFalse(ar.commitCalled));
        assertTrue(ars[0].wasAborted());
        assertTrue(ars[1].wasAborted());
        assertTrue(ars[2].wasAborted());
        // the last (or only) 2PC aware resource will be asked to prepare on the callers thread
        // resources are asked to prepare in the reverse order from which they were enlisted
        // but since this behaviour could change just check that n-1 participants prepared on a virtual thread
        int abortedOnVTCount = 0;
        for (SimpleAbstractRecord ar : ars) {
            if (ar.wasAbortedWithVirtualThread()) {
                abortedOnVTCount += 1;
            }
        }

        if (virtualThreadsSupported) {
            assertEquals(ars.length - 1, abortedOnVTCount);
        }
    }

//    @org.junit.Test
//    @Test
//    @EnabledForJreRange(min = JRE.JAVA_21)
    public void testCanCommitSuspendedTransaction()
    {
        boolean virtualThreadsSupported = false;
        AtomicAction aa = new AtomicAction();
        aa.begin();
        assertNotNull(AtomicAction.Current());
        AtomicAction.suspend();
        assertNull(AtomicAction.Current());
        SimpleAbstractRecord[] ars = {
                new SimpleAbstractRecord(false),
                new SimpleAbstractRecord(false),
                new SimpleAbstractRecord(false)
        };
        Arrays.stream(ars).forEach(aa::add);
        aa.commit();
        // the last (or only) 2PC aware resource will be asked to prepare on the callers thread
        // resources are asked to prepare in the reverse order from which they were enlisted
        // but since this behaviour could change just check that n-1 participants prepared on a virtual thread
        int preparedOnVTCount = 0;
        for (SimpleAbstractRecord ar : ars) {
            assertTrue(ar.wasCommitted());
            if (ar.wasPreparedWithVirtualThread()) {
                preparedOnVTCount += 1;
            }
        }

        if (virtualThreadsSupported) {
            assertEquals(ars.length - 1, preparedOnVTCount);
        }
    }

    private static class SimpleAbstractRecord extends AbstractRecord {
        private final int id;
        private final boolean failPrepare;
        private boolean commitCalled;
        private boolean abortCalled;
        private boolean prepareCalled;
        String prepareThreadGroup;
        String commitThreadGroup;
        String abortThreadGroup;
        private boolean prepareCalledWithVirtualThread;
        private boolean commitCalledWithVirtualThread;
        private boolean abortCalledWithVirtualThread;

        public SimpleAbstractRecord(boolean failPrepare) {
            this(0, failPrepare);
        }

        public SimpleAbstractRecord(int id, boolean failPrepare) {
            this.failPrepare = failPrepare;
            this.id = id;
        }

        public int getId() {
            return id;
        }

        @Override
        public int typeIs() {
            return RecordType.USER_DEF_FIRST0;
        }

		public boolean wasCommitted() {
			return commitCalled;
		}

		public boolean wasPrepared() {
			return prepareCalled;
		}

		public boolean wasAborted() {
			return abortCalled;
		}

        public String getPrepareThreadGroup() {
            return prepareThreadGroup;
        }

        public String getCommitThreadGroup() {
            return commitThreadGroup;
        }

        public String getAbortThreadGroup() {
            return abortThreadGroup;
        }

        public boolean wasCommittedWithVirtualThread() {
            return commitCalledWithVirtualThread;
		}

        public boolean wasPreparedWithVirtualThread() {
            return prepareCalledWithVirtualThread;
        }

        public boolean wasAbortedWithVirtualThread() {
            return abortCalledWithVirtualThread;
        }

        @Override
        public Object value() {
            return null;
        }

        @Override
        public void setValue(Object o) {
        }

        @Override
        public int nestedAbort() {
            return 0;
        }

        @Override
        public int nestedCommit() {
            return 0;
        }

        @Override
        public int nestedPrepare() {
            return 0;
        }

        private boolean isRunningOnAVirtualThread() {
            // check for running on a virtual thread using the thread group name as opposed to Thread.currentThread().isVirtual()
            return Thread.currentThread().getThreadGroup().getName().equals("VirtualThreads");
        }

        @Override
        public int topLevelAbort() {
            abortThreadGroup = Thread.currentThread().getThreadGroup().getName();
            abortCalled = true;
            return 0;
        }

        @Override
        public int topLevelCommit() {
            commitThreadGroup = Thread.currentThread().getThreadGroup().getName();
            commitCalled = true;
            return TwoPhaseOutcome.FINISH_OK;
        }

        @Override
        public int topLevelPrepare() {
            prepareThreadGroup = Thread.currentThread().getThreadGroup().getName();
            prepareCalled = true;
            if (failPrepare) {
                return TwoPhaseOutcome.PREPARE_NOTOK;
            }
            return TwoPhaseOutcome.PREPARE_OK;
        }

        @Override
        public void merge(AbstractRecord a) {

        }

        @Override
        public void alter(AbstractRecord a) {

        }

        @Override
        public boolean shouldAdd(AbstractRecord a) {
            return true; // force it onto the pending list
        }

        @Override
        public boolean shouldAlter(AbstractRecord a) {
            return false;
        }

        @Override
        public boolean shouldMerge(AbstractRecord a) {
            return false;
        }

        @Override
        public boolean shouldReplace(AbstractRecord a) {
            return false;
        }
    }
}
