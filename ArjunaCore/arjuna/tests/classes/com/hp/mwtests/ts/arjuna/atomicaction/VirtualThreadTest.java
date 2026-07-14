/*                                                                                                                                                                                                                  
   Copyright The Narayana Authors                                                                                                                                                                                   
   SPDX-License-Identifier: Apache-2.0                                                                                                                                                                              
 */                                                                                                                                                                                                                 
package com.hp.mwtests.ts.arjuna.atomicaction;

import com.arjuna.ats.arjuna.AtomicAction;

import com.arjuna.ats.arjuna.common.CoordinatorEnvironmentBean;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.RecordType;

import com.arjuna.ats.arjuna.coordinator.TwoPhaseCommitThreadPool;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@RunWith(JUnit4.class)
class VirtualThreadTest {
    private static final String VIRTUAL_THREADS_GROUP_NAME = "VirtualThreads";
    private static final String RESTART_EXECUTOR_METHOD_NAME = "restartExecutor";
    private static final String SHUTDOWN_EXECUTOR_METHOD_NAME = "shutdownExecutor";
    private static final String SHUTDOWN_EXECUTOR_NOW_METHOD_NAME = "shutdownExecutorNow";
    private static boolean isVirtualThreadPerTaskExecutor;
    private static MethodHandle restartExecutorMH; // method handle to restart the executor
    private static MethodHandle shutdownExecutorNowMH; // method handle to shut down the executor immediately
    private static MethodHandle shutdownExecutorMH; // method handle to shut down the executor

    @BeforeAll
    public static void beforeClass() {
        // configure asynchronous resource operations
        CoordinatorEnvironmentBean configBean = arjPropertyManager.getCoordinatorEnvironmentBean();

        configBean.setAsyncPrepare(true);
        configBean.setAsyncCommit(true);
        configBean.setAsyncRollback(true);

        configBean.setAsyncBeforeSynchronization(true);
        configBean.setAsyncAfterSynchronization(true);

        // TwoPhaseCommitThreadPool.restartExecutor executor is required, but it is private
        // so we need to use method handles (or reflection):

        // create the lookup
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        // lookup the methods
        try {
            Method restartMethod = TwoPhaseCommitThreadPool.class.getDeclaredMethod(RESTART_EXECUTOR_METHOD_NAME);
            // it's package private so make it accessible
            restartMethod.setAccessible(true);
            // and get a method handle to it
            restartExecutorMH = lookup.unreflect(restartMethod);

            Method shutdownMethod =
                    TwoPhaseCommitThreadPool.class.getDeclaredMethod(SHUTDOWN_EXECUTOR_METHOD_NAME);
            shutdownMethod.setAccessible(true);
            shutdownExecutorMH = lookup.unreflect(shutdownMethod);

            Method shutdownNowMethod =
                    TwoPhaseCommitThreadPool.class.getDeclaredMethod(SHUTDOWN_EXECUTOR_NOW_METHOD_NAME);
            shutdownNowMethod.setAccessible(true);
            shutdownExecutorNowMH = lookup.unreflect(shutdownNowMethod);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            fail(e);
        }
    }

    // restart the TwoPhaseCommitThreadPool executor and use virtual threads on java 21+
    @BeforeEach
    @EnabledForJreRange(min = JRE.JAVA_21)
    public void beforeEach() {
        isVirtualThreadPerTaskExecutor = restartExecutor(true);
    }

    // restart the TwoPhaseCommitThreadPool executor on JDKs 17, 18, 19 and 20 which don't support virtual threads
    @BeforeEach
    @EnabledForJreRange(min = JRE.JAVA_17, max = JRE.JAVA_20)
    public void beforeEach17_20() {
        isVirtualThreadPerTaskExecutor = restartExecutor(false);
    }

    private static boolean restartExecutor(boolean useVirtualThreads) {
        try {
            arjPropertyManager.getCoordinatorEnvironmentBean().
                    setUseVirtualThreadsForTwoPhaseCommitThreads(useVirtualThreads);

            // invoke the restartExecutor method on TwoPhaseCommitThreadPool
            isVirtualThreadPerTaskExecutor = (boolean) restartExecutorMH.invokeExact();
        } catch (Throwable e) {
            fail(e);
        }

        return isVirtualThreadPerTaskExecutor;
    }

    private static boolean shutdownExecutor(boolean immediately) {
        assertNotNull(shutdownExecutorMH, "shutdownExecutorMH not set");
        assertNotNull(shutdownExecutorNowMH, "shutdownExecutorNowMH not set");
        try {
            if (immediately) {
                List<Runnable> pending = (List<Runnable>) shutdownExecutorNowMH.invokeExact();
                return pending.isEmpty();
            } else {
                return (boolean) shutdownExecutorMH.invokeExact();
            }
        } catch (Throwable e) {
            fail(e);
        }

        return false;
    }

    @Test
    // test shutting down the TwoPhaseCommitThreadPool executor waiting for any pending and running tasks to complete
    public void testGracefullyShutdownTwoPhaseCommitThreadPool() {
        Callable<Integer> fastJob = () -> 0;
        Callable<Integer> slowJob = () -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                return -1; // to indicate a problem
            }
            return 200;
        };

        restartExecutor(false);
        Future<Integer> f = TwoPhaseCommitThreadPool.submitJob(fastJob);
        try {
            assertEquals(0, f.get());
        } catch (InterruptedException | ExecutionException e) {
            fail(e);
        }

        f = TwoPhaseCommitThreadPool.submitJob(slowJob);

        // gracefully shutdown the executor (pending jobs should complete)
        shutdownExecutor(false);

        try {
            assertEquals(200, f.get(), "Expected job to pass when the executor gracefully shuts down");
        } catch (InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    @Test
    // test shutting down the TwoPhaseCommitThreadPool executor immediately, ie forcing any running tasks to stop
    public void testDisgracefullyShutdownTwoPhaseCommitThreadPool() {
        Callable<Integer> fastJob = () -> 0;
        Callable<Integer> slowJob = () -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                return -1; // to indicate a problem
            }
            return 200;
        };

        restartExecutor(false);
        Future<Integer> f = TwoPhaseCommitThreadPool.submitJob(fastJob);
        try {
            assertEquals(0, f.get());
        } catch (InterruptedException | ExecutionException e) {
            fail(e);
        }

        f = TwoPhaseCommitThreadPool.submitJob(slowJob);
        // shutdown the executor immediately (pending jobs should not complete)
        shutdownExecutor(true);

        try {
            assertEquals(-1, f.get(), "Expected job to fail when the executor shuts down immediately");
        } catch (InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    @Test
    @EnabledForJreRange(min = JRE.JAVA_21) // virtual threads require at least JRE.JAVA_21
    public void testConfigureVirtualThreads() {
        restartExecutor(false);

        assertFalse(isVirtualThreadPerTaskExecutor, "TwoPhaseCommitThreadPool did not restart with standard threads");

        // var holder for the results of the job execution
        var ref = new Object() {
            AtomicBoolean ranOnVT = new AtomicBoolean(false);
            int expected = 1;
        };
        int finalExpected = ref.expected;

        // submit a job to the executor
        Future<Integer> future = TwoPhaseCommitThreadPool.submitJob(() -> {
            ref.ranOnVT.set(VIRTUAL_THREADS_GROUP_NAME.equals(Thread.currentThread().getThreadGroup().getName()));
            return finalExpected;
        });
        // and verify it returned the expected result
        try {
            assertEquals(ref.expected, future.get());
        } catch (InterruptedException | ExecutionException e) {
            fail("future.get");
        }
        // and verify that the executor ran using standard, as opposed to virtual, threads
        assertFalse(ref.ranOnVT.get());

        // redo exactly the same test but with the executor configured to use virtual threads:
        arjPropertyManager.getCoordinatorEnvironmentBean().setUseVirtualThreadsForTwoPhaseCommitThreads(true);
        isVirtualThreadPerTaskExecutor = restartExecutor(true); // should succeed on JRE.JAVA_21 and above
        assertTrue(isVirtualThreadPerTaskExecutor, "TwoPhaseCommitThreadPool did not restart with virtual threads");

        // reset the var holder for the results of the job execution
        ref.ranOnVT = new AtomicBoolean(false);
        ref.expected = 1;
        AtomicBoolean finalRanOnVT = ref.ranOnVT;
        // submit a job to the executor
        future = TwoPhaseCommitThreadPool.submitJob(() -> {
            finalRanOnVT.set(VIRTUAL_THREADS_GROUP_NAME.equals(Thread.currentThread().getThreadGroup().getName()));
            return ref.expected;
        });
        // and verify it returned the expected result
        try {
            assertEquals(ref.expected, future.get());
        } catch (InterruptedException | ExecutionException e) {
            fail("future.get");
        }

        // and verify that the executor ran using virtual, as opposed to standard, threads
        assertTrue(ref.ranOnVT.get());
    }

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
        Arrays.stream(ars).forEach(ar -> assertTrue(ar.prepareCalled.get()));
        Arrays.stream(ars).forEach(ar -> assertFalse(ar.abortCalled.get()));
        Arrays.stream(ars).forEach(ar -> assertTrue(ar.commitCalled.get()));

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
        Arrays.stream(ars).forEach(ar -> assertTrue(ar.prepareCalled.get()));
        Arrays.stream(ars).forEach(ar -> assertTrue(ar.abortCalled.get())); // because prepare failed
        Arrays.stream(ars).forEach(ar -> assertFalse(ar.commitCalled.get()));

        Arrays.stream(ars).forEach(ar -> assertEquals(aa, ar.prepareAction));
        Arrays.stream(ars).forEach(ar -> assertEquals(aa, ar.abortAction));
        Arrays.stream(ars).forEach(ar -> assertNull(ar.commitAction));

        if (isVirtualThreadPerTaskExecutor) {
            long preparesOnVT = Arrays.stream(ars).filter(
                            r -> VIRTUAL_THREADS_GROUP_NAME.equals(r.getPrepareThreadGroup()))
                    .count();
            long abortsOnVT = Arrays.stream(ars).filter(
                    r -> VIRTUAL_THREADS_GROUP_NAME.equals(r.getAbortThreadGroup()))
                    .count();

            // since the middle resource failed to prepare the last one is not asked to prepare since the action aborts
            // it's expected that the first two prepare calls are executed on virtual threads and the last prepare
            // runs on the callers thread
            assertEquals(2, preparesOnVT);
            // asynchronous abort is not supported if the caller has requested heuristic reporting - see BasicAction#End
            assertEquals(0, abortsOnVT); // aborting with a virtual thread
        }
    }

    /*
     * runs testPrepareFailIgnoreHeuristics in a loop to test any potential races between heuristic reporting and
     * resource abort
     */
    @Test
    public void testPrepareFailIgnoreHeuristicsExpectAbort() {
        for (int i = 0; i < 10; i++) {
            testPrepareFailIgnoreHeuristics();
        }
    }

    /**
     * Similar to testPrepareFailReportHeuristics except that heuristics are not requested.
     */
    @Test
    public void testPrepareFailIgnoreHeuristics() {
        SimpleAbstractRecord[] ars = {
                new SimpleAbstractRecord(1, false),
                new SimpleAbstractRecord(2, true), // prepare fails causing the transaction to abort
                new SimpleAbstractRecord(3, false)
        };
        AtomicAction aa = new AtomicAction();

        aa.begin();
        Arrays.stream(ars).forEach(aa::add);

        aa.commit(false); // do not report heuristics because the completion phase runs async

        // verify that abort were called
        for (SimpleAbstractRecord ar : ars) {
            int i = 0;
            // Since we didn't ask commit to report heuristics we aren't guaranteed that it has committed yet so
            // wait for a maximum of 500 milliseconds (normally a max of a few millis is enough) before giving up
            // (note that registering a synchronisation won't help because the Arjuna TM treats the completion phase
            // as asynchronous when commit is called with report_heuristics = false)
            while (!ar.isFinished() && i++ < 50) {
                Assertions.assertDoesNotThrow(() -> Thread.sleep(10), "interrupted ");
            }
            assertTrue(ar.abortCalled.get()); // fail if abort wasn't called
        }
        Arrays.stream(ars).forEach(ar -> assertFalse(ar.commitCalled.get()));

        // and verify that the correct action was associated
        Arrays.stream(ars).forEach(ar -> assertEquals(aa, ar.prepareAction));
        Arrays.stream(ars).forEach(ar -> assertEquals(aa, ar.abortAction));
        Arrays.stream(ars).forEach(ar -> assertNull(ar.commitAction));

        if (isVirtualThreadPerTaskExecutor) {
            long preparesOnVT = Arrays.stream(ars).filter(
                            r -> r.getPrepareThreadGroup().equals(VIRTUAL_THREADS_GROUP_NAME))
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
        private final long delayPrepareMillis;
        private final AtomicBoolean commitCalled = new AtomicBoolean(false);
        private final AtomicBoolean abortCalled = new AtomicBoolean(false);
        private final AtomicBoolean prepareCalled = new AtomicBoolean(false);
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
            this(id, failPrepare, 0L);
        }

        public SimpleAbstractRecord(int id, boolean failPrepare, long delayPrepareMillis) {
            this.failPrepare = failPrepare;
            this.delayPrepareMillis = delayPrepareMillis;
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
            abortThreadGroup = Thread.currentThread().getThreadGroup().getName();
            abortAction = BasicAction.Current();
            abortCalled.set(true);
            return 0;
        }

        @Override
        public int topLevelCommit() {
            commitThreadGroup = Thread.currentThread().getThreadGroup().getName();
            commitAction = BasicAction.Current();
            commitCalled.set(true);
            return TwoPhaseOutcome.FINISH_OK;
        }

        @Override
        public int topLevelPrepare() {
            prepareThreadGroup = Thread.currentThread().getThreadGroup().getName();
            prepareAction = BasicAction.Current();

            if (delayPrepareMillis != 0) {
                try {
                    Thread.sleep(delayPrepareMillis);
                } catch (InterruptedException ignore) {
                }
            }

            if (failPrepare) {
                prepareCalled.set(true);
                return TwoPhaseOutcome.PREPARE_NOTOK;
            }

            prepareCalled.set(true);

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

        public boolean isFinished() {
            return commitCalled.get() || abortCalled.get();
        }
    }
}
