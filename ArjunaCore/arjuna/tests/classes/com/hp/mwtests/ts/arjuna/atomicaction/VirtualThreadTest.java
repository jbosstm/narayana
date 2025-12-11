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
import com.arjuna.ats.arjuna.coordinator.BasicAction;
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
    // test that prepare and commit are called on all registered records
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

        // verify that prepare and commit were called and that the correct action was associated
        Arrays.stream(ars).forEach(ar -> assertTrue(ar.prepareCalled));
        Arrays.stream(ars).forEach(ar -> assertFalse(ar.abortCalled));
        Arrays.stream(ars).forEach(ar -> assertTrue(ar.commitCalled));

        Arrays.stream(ars).forEach(ar -> assertEquals(aa, ar.prepareAction));
        Arrays.stream(ars).forEach(ar -> assertNull(ar.abortAction));
        Arrays.stream(ars).forEach(ar -> assertEquals(aa, ar.commitAction));

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
            /**
             * asynchronous commit is not supported if the caller has requested heuristic reporting see
             * @see com.arjuna.ats.arjuna.coordinator.BasicAction#End(boolean)
             */
            assertEquals(0, commitsOnVT);
        }
    }

    /**
     * test that abort is called if one of the records fails to prepare:
     * Commit a transaction with three resources, the middle one will fail the prepare phase.
     * Since there is no point in preparing the last resource it will be told to abort instead.
     * The two prepared resources, the first two, will then be asked to abort, therefore in total
     * <p>
     * Asynchronous abort is not supported if the caller has requested heuristic reporting so
     * a virtual thread will not be used for and of the resource rollback calls:
     * @see com.arjuna.ats.arjuna.coordinator.BasicAction (#End(boolean))
     */
    @org.junit.Test
    @Test
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

        // verify that prepare and abort were called and that the correct action was associated
        Arrays.stream(ars).forEach(ar -> assertTrue(ar.prepareCalled));
        Arrays.stream(ars).forEach(ar -> assertTrue(ar.abortCalled)); // because prepare failed
        Arrays.stream(ars).forEach(ar -> assertFalse(ar.commitCalled));

        Arrays.stream(ars).forEach(ar -> assertEquals(aa, ar.prepareAction));
        Arrays.stream(ars).forEach(ar -> assertEquals(aa, ar.abortAction));
        Arrays.stream(ars).forEach(ar -> assertNull(ar.commitAction));

        if (isVirtualThreadPerTaskExecutor) {
            long preparesOnVT = Arrays.stream(ars).filter(
                            r -> VIRTUAL_THREADS_GROUP_NAME.equals(r.getPrepareThreadGroup()))
                    .count();
            long abortsOnVT = Arrays.stream(ars).filter(
                            r -> r.getAbortThreadGroup().equals("VirtualThreads"))
                    .count();

            // since the middle resource failed to prepare the last one is not asked to prepare since the action aborts
            // it's expected that the first two prepare calls are executed on virtual threads and the last prepare
            // runs on the callers thread
            assertEquals(2, preparesOnVT);
            // asynchronous abort is not supported if the caller has requested heuristic reporting - see BasicAction#End
            assertEquals(0, abortsOnVT); // aborting with a virtual thread
        }
    }

    /**
     * Similar to testPrepareFailReportHeuristics except that heuristics are not requested.
     */
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

        // verify that prepare and abort were called and that the correct action was associated
        Arrays.stream(ars).forEach(ar -> assertTrue(ar.prepareCalled));
        Arrays.stream(ars).forEach(ar -> assertTrue(ar.abortCalled));
        Arrays.stream(ars).forEach(ar -> assertFalse(ar.commitCalled));

        Arrays.stream(ars).forEach(ar -> assertEquals(aa, ar.prepareAction));
        Arrays.stream(ars).forEach(ar -> assertEquals(aa, ar.abortAction));
        Arrays.stream(ars).forEach(ar -> assertNull(ar.commitAction));

        if (isVirtualThreadPerTaskExecutor) {
            long preparesOnVT = Arrays.stream(ars).filter(
                            r -> r.getPrepareThreadGroup().equals("VirtualThreads"))
                    .count();
            long abortsOnVT = Arrays.stream(ars).filter(
                            r -> VIRTUAL_THREADS_GROUP_NAME.equals(r.getAbortThreadGroup()))
                    .count();
            // since the middle resource failed to prepare the last one is not asked to prepare since the action aborts
            assertEquals(2, preparesOnVT);
            // in this example commit was requested but since one of the prepare calls failed the code
            // that tests the condition (!reportHeuristics && TxControl.asyncCommit) in BasicAction#End will be
            // true and the abort call runs async - although it is counter-intuitive it is how the current
            // code base handles this situation. The lambda this::phase2Abort is submitted to the thread pool
            // and a virtual thread will be used to execute the lambda, but note that BasicAction#phase2Abort itself
            // executes the resource abort calls one after the other (ie we don't use separate threads for each abort
            // call).
            // Remark: There are further opportunities for executing resource calls asynchronously when asyncCommit
            // and asyncAbort are configured (see JBTM-4025).
            assertEquals(3, abortsOnVT);
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
        BasicAction prepareAction;
        BasicAction commitAction;
        BasicAction abortAction;

        public SimpleAbstractRecord(boolean failPrepare) {
            this(0, failPrepare);
        }

        public SimpleAbstractRecord(int id, boolean failPrepare) {
            this.failPrepare = failPrepare;
            this.id = id;
        }

        @Override
        public int typeIs() {
            return RecordType.USER_DEF_FIRST0;
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

        @Override
        public int topLevelAbort() {
            abortCalled = true;
            abortThreadGroup = Thread.currentThread().getThreadGroup().getName();
            abortAction = BasicAction.Current();
            return 0;
        }

        @Override
        public int topLevelCommit() {
            commitCalled = true;
            commitThreadGroup = Thread.currentThread().getThreadGroup().getName();
            commitAction = BasicAction.Current();
            return TwoPhaseOutcome.FINISH_OK;
        }

        @Override
        public int topLevelPrepare() {
            prepareCalled = true;
            prepareThreadGroup = Thread.currentThread().getThreadGroup().getName();
            prepareAction = BasicAction.Current();
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
