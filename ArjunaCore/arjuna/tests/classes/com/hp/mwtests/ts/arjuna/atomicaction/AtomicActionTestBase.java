/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.atomicaction;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.internal.arjuna.abstractrecords.LastResourceRecord;
import com.hp.mwtests.ts.arjuna.resources.*;
import org.junit.Test;
import org.junit.Assert;

public class AtomicActionTestBase
{
    protected static void init(boolean isAsync) {
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncPrepare(isAsync);
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncCommit(isAsync);
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncRollback(isAsync);

        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncBeforeSynchronization(isAsync);
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncAfterSynchronization(isAsync);
    }

    protected void testCommit () throws Exception
    {
        executeTest(true, ActionStatus.COMMITTED, null, new BasicRecord(), new BasicRecord());
    }

    protected void testAbort () throws Exception
    {
        executeTest(false, ActionStatus.ABORTED, null, new BasicRecord(), new BasicRecord());
    }

    protected void testPrepareWithLRRSuccess()
    {
        OnePhase onePhase = new OnePhase();
        AbstractRecord lastResourceRecord = new LastResourceRecord(onePhase);
        AbstractRecord basicRecord = new BasicRecord();

        executeTest(true, ActionStatus.COMMITTED, null, basicRecord, lastResourceRecord);

        Assert.assertEquals(OnePhase.COMMITTED, onePhase.status());
    }

    protected void testPrepareWithLRRFailOn2PCAwareResourcePrepare()
    {
        OnePhase onePhase = new OnePhase();
        AbstractRecord lastResourceRecord = new LastResourceRecord(onePhase);
        AbstractRecord shutdownRecord = new ShutdownRecord(ShutdownRecord.FAIL_IN_PREPARE);

        executeTest(true, ActionStatus.ABORTED, null, shutdownRecord, lastResourceRecord);

        Assert.assertEquals(OnePhase.ROLLEDBACK, onePhase.status());
    }

    protected void testPrepareWithLRRFailOn2PCUnawareResourcePrepare()
    {
        OnePhase onePhase = new OnePhase();
        AbstractRecord lastResourceRecord = new LastResourceShutdownRecord(onePhase, true);
        AbstractRecord basicRecord = new BasicRecord();

        executeTest(true, ActionStatus.ABORTED, null, lastResourceRecord, basicRecord);

        Assert.assertEquals(OnePhase.ROLLEDBACK, onePhase.status());
    }

    protected void testPrepareWithLRRFailOn2PCAwareResourceCommit()
    {
        OnePhase onePhase = new OnePhase();
        AbstractRecord lastResourceRecord = new LastResourceRecord(onePhase);
        AbstractRecord shutdownRecord = new ShutdownRecord(ShutdownRecord.FAIL_IN_COMMIT);

        executeTest(true, ActionStatus.COMMITTED, null, lastResourceRecord, shutdownRecord);

        Assert.assertEquals(OnePhase.COMMITTED, onePhase.status());
    }

    /**
     * Tests for correct behaviour of synchronisations (the normal case)
     * @throws Exception
     */
    protected void testCompletionWithoutFailures() throws Exception
    {
        SyncRecord[] syncs = {
                new SyncRecord(),
                new SyncRecord(true, SyncRecord.FailureMode.NONE)
        };

        executeTest(true, ActionStatus.COMMITTED, syncs, new BasicRecord(), new BasicRecord());

        for (SyncRecord sync : syncs) {
            // assert that both before and after synchronisation callbacks were triggered
            Assert.assertTrue(sync.getBeforeTimeStamp() != 0);
            Assert.assertTrue(sync.getAfterTimeStamp() != 0);
        }

        // assert that beforeCompletion was called on the non interposed synchronisation first
        Assert.assertTrue(syncs[0].getBeforeTimeStamp() <= syncs[1].getBeforeTimeStamp());
        // assert that beforeCompletion was called on the non interposed synchronisation last
        Assert.assertTrue(syncs[0].getAfterTimeStamp() >= syncs[1].getAfterTimeStamp());
    }

    /**
     * Tests for correct behaviour of synchronisations (in the abnormal case)
     * @throws Exception
     */
    protected void testCompletionWithFailures() throws Exception
    {
        SyncRecord[] syncs = {
                new SyncRecord(false, SyncRecord.FailureMode.BEFORE_FAIL),
                new SyncRecord(true, SyncRecord.FailureMode.NONE)
        };

        executeTest(true, ActionStatus.ABORTED, syncs, new BasicRecord(), new BasicRecord());

        /*
         * since the first synch failed during the beforeCompletion callback:
         * - beforeCompletion should  be called on the first one
         * - beforeCompletion should not be called on the second one
         * - afterCompletion should be called on all synchronisations
         * - the final status of the action should be aborted
         */
        Assert.assertTrue(syncs[0].getBeforeTimeStamp() != 0);
        Assert.assertTrue(syncs[1].getBeforeTimeStamp() == 0);
        Assert.assertTrue(syncs[0].getAfterTimeStamp() != 0);
        Assert.assertTrue(syncs[1].getAfterTimeStamp() != 0);
        Assert.assertTrue(syncs[1].getStatus() == ActionStatus.ABORTED);

    }

    /**
     * Tests for correct behaviour of synchronisations (in the abnormal case)
     * @throws Exception
     */
    protected void testCompletionWithException() throws Exception
    {
        SyncRecord[] syncs = {
                new SyncRecord(false, SyncRecord.FailureMode.NONE),
                new SyncRecord(true, SyncRecord.FailureMode.NONE)
        };
        RuntimeException exception = new RuntimeException("testCompletionWithException");

        syncs[0].setBeforeThrowable(exception);
        AtomicAction a= executeTest(true, ActionStatus.ABORTED, syncs, new BasicRecord(), new BasicRecord());

        /*
         * since the first synch failed during the beforeCompletion callback:
         * - beforeCompletion should  be called on the first one
         * - beforeCompletion should not be called on the second one
         * - afterCompletion should be called on all synchronisations
         * - the synchronization should have throw the runtime exception
         * - the final status of the action should be aborted
         */
        Assert.assertTrue(syncs[0].getBeforeTimeStamp() != 0);
        Assert.assertTrue(syncs[1].getBeforeTimeStamp() == 0);
        Assert.assertTrue(syncs[0].getAfterTimeStamp() != 0);
        Assert.assertTrue(syncs[1].getAfterTimeStamp() != 0);
        Assert.assertEquals(exception, a.getDeferredThrowable());
        Assert.assertTrue(syncs[1].getStatus() == ActionStatus.ABORTED);

    }

    protected void testRegistrationDuringCompletion() throws Exception {
        SyncRecord[] syncs = {
                new SyncRecord(false, SyncRecord.FailureMode.NONE),
                new SyncRecord(true, SyncRecord.FailureMode.NONE)
        };

        /*
         * arrange for the interposed synchronisation to register a non interposed synchronisation during
         * the beforeCompletion call. This call should fail (resulting in the action being aborted) since
         * interposed syncs run after non interposed ones
         */
        syncs[1].registerSynchDuringSynch(new SyncRecord(false, SyncRecord.FailureMode.NONE));

        executeTest(true, ActionStatus.ABORTED, syncs, new BasicRecord(), new BasicRecord());
    }

    protected void testRegistrationDuringCompletion2() throws Exception {
        SyncRecord[] syncs = {
                new SyncRecord(false, SyncRecord.FailureMode.NONE),
                new SyncRecord(true, SyncRecord.FailureMode.NONE)
        };

        /*
         * arrange for the interposed synchronisation to register another interposed synchronisation during
         * the beforeCompletion call. This call should succeed (since interposed synchronisations are
         * executed in time order)
         */
        syncs[1].registerSynchDuringSynch(new SyncRecord(true, SyncRecord.FailureMode.NONE));

        executeTest(true, ActionStatus.COMMITTED, syncs, new BasicRecord(), new BasicRecord());
    }

    protected void testRegistrationDuringCompletion2b() throws Exception {
        SyncRecord earlierSync = new SyncRecord(true, SyncRecord.FailureMode.NONE);
        SyncRecord[] syncs = {
                new SyncRecord(false, SyncRecord.FailureMode.NONE),
                new SyncRecord(true, SyncRecord.FailureMode.NONE)
        };

        /*
         * Arrange for the interposed synchronisation to register another interposed synchronisation during
         * the beforeCompletion but in such a way that this second synchronisation is ordered before the
         * first one (interposed synchronisations are time ordered).
         *
         * If synchronisations are executed in parallel this should succeed but if they are executed in order
         * then it will fail.
         */
        syncs[1].registerSynchDuringSynch(earlierSync);

        int expect = arjPropertyManager.getCoordinatorEnvironmentBean().isAsyncBeforeSynchronization()
                ? ActionStatus.COMMITTED : ActionStatus.ABORTED;

        executeTest(true, expect, syncs, new BasicRecord(), new BasicRecord());
    }

    protected void testRegistrationDuringCompletion3() throws Exception {
        SyncRecord[] syncs = {
                new SyncRecord(false, SyncRecord.FailureMode.NONE),
                new SyncRecord(true, SyncRecord.FailureMode.NONE)
        };

        /*
         * arrange for the non interposed synchronisation to register an interposed synchronisation during
         * the beforeCompletion call. This call should succeed (since non interposed synchronisations run first)
         */
        syncs[0].registerSynchDuringSynch(new SyncRecord(true, SyncRecord.FailureMode.NONE));

        executeTest(true, ActionStatus.COMMITTED, syncs, new BasicRecord(), new BasicRecord());
    }

    protected void testRegistrationDuringCompletion4() throws Exception {
        SyncRecord[] syncs = {
                new SyncRecord(false, SyncRecord.FailureMode.NONE),
                new SyncRecord(true, SyncRecord.FailureMode.NONE)
        };

        /*
         * arrange for the non interposed synchronisation to register another non interposed synchronisation during
         * the beforeCompletion call. This call should succeed (since non interposed synchronisations are
         * executed in time order).
         */
        syncs[0].registerSynchDuringSynch(new SyncRecord(false, SyncRecord.FailureMode.NONE));

        executeTest(true, ActionStatus.COMMITTED, syncs, new BasicRecord(), new BasicRecord());
    }

    protected void testRegistrationDuringCompletion4b() throws Exception {
        SyncRecord earlierSync = new SyncRecord(false, SyncRecord.FailureMode.NONE);
        SyncRecord[] syncs = {
                new SyncRecord(false, SyncRecord.FailureMode.NONE),
                new SyncRecord(true, SyncRecord.FailureMode.NONE)
        };

        /*
         * Arrange for the non interposed synchronisation to register another non interposed synchronisation during
         * the beforeCompletion but in such a way that this second synchronisation is ordered before the
         * first one (non interposed synchronisations are time ordered).
         *
         * If synchronisations are executed in parallel this should succeed but if they are executed in order
         * then it will fail.
         */
        syncs[0].registerSynchDuringSynch(earlierSync);

        int expect = arjPropertyManager.getCoordinatorEnvironmentBean().isAsyncBeforeSynchronization()
                ? ActionStatus.COMMITTED : ActionStatus.ABORTED;

        executeTest(true, expect, syncs, new BasicRecord(), new BasicRecord());
    }

    protected void testHeuristicNotification(boolean reportHeuristics) throws Exception
    {
        AtomicAction A = new AtomicAction();
        DummyHeuristic[] dha = {new DummyHeuristic(), new DummyHeuristic()};

        A.begin();

        A.add(new BasicRecord());
        A.add(new BasicRecord());
        A.add(new HeuristicRecord());

        for (DummyHeuristic dh : dha)
            A.addSynchronization(dh);

        int status = A.commit(reportHeuristics);

        if (reportHeuristics)
            Assert.assertEquals(ActionStatus.H_MIXED, status);
        else if (!arjPropertyManager.getCoordinatorEnvironmentBean().isAsyncCommit())
            Assert.assertEquals(ActionStatus.COMMITTED, status);
        else
            Assert.assertTrue(status == ActionStatus.COMMITTED || status == ActionStatus.COMMITTING);

        // we only inform synchronisations of the outcome if report_heuristics is false
        int expect = reportHeuristics ? -1 : TwoPhaseOutcome.HEURISTIC_MIXED;

        for (DummyHeuristic dh : dha)
            Assert.assertEquals(expect, dh.getStatus());
    }

    protected AtomicAction executeTest(boolean isCommit, int expectedResult, SyncRecord[] syncs, AbstractRecord... records) {
        AtomicAction A = new AtomicAction();

        A.begin();

        for (AbstractRecord record : records) {
            A.add(record);
        }

        if (syncs != null) {
            for (SyncRecord sync : syncs)
                Assert.assertEquals(AddOutcome.AR_ADDED, A.addSynchronization(sync));

            Assert.assertEquals(syncs.length, A.getSynchronizations().size());
        }

        if (isCommit) {
            Assert.assertEquals(expectedResult, A.commit());
        } else {
            Assert.assertEquals(expectedResult, A.abort());
        }

        return A;
    }
}