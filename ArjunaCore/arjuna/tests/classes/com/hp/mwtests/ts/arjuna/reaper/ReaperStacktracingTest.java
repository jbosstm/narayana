/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.reaper;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.Reapable;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Exercises TransactionReaper periodic stack tracing functionality.
 *
 * @author jonathan.halliday@redhat.com, 2021-03
 */
public class ReaperStacktracingTest {

    @Test
    public void testReaper() throws Exception {

        // run a single tx through its lifecycle and check it's doing stack trace and termination at expected times

        // traces should take place at 300, 500, 700, 900 ms
        arjPropertyManager.getCoordinatorEnvironmentBean().setTxReaperTraceGracePeriod(300);
        arjPropertyManager.getCoordinatorEnvironmentBean().setTxReaperTraceInterval(200);

        TransactionReaper reaper = TransactionReaper.transactionReaper();
        MockReapable reapable = new MockReapable(new Uid(), 0);
        long startTime = System.currentTimeMillis();
        reaper.insert(reapable, 1);
        Thread.sleep(1200);
        TransactionReaper.terminate(false);

        // check the stacktrace calls took place as expected
        assertEquals(4, reapable.recordStackTracesCallTimes.size());
        long interval = arjPropertyManager.getCoordinatorEnvironmentBean().getTxReaperTraceInterval();
        long first = startTime+arjPropertyManager.getCoordinatorEnvironmentBean().getTxReaperTraceGracePeriod();
        assertWithinTolerance(first,  reapable.recordStackTracesCallTimes.get(0));
        assertWithinTolerance(first+(interval), reapable.recordStackTracesCallTimes.get(1));
        assertWithinTolerance(first+(interval*2), reapable.recordStackTracesCallTimes.get(2));
        assertWithinTolerance(first+(interval*3), reapable.recordStackTracesCallTimes.get(3));

        // we should be asked to dump the captured traces once, right before being cancelled
        assertEquals(1, reapable.outputCapturedStackTracesCallTimes.size());
        assertWithinTolerance(startTime+1000, reapable.outputCapturedStackTracesCallTimes.get(0));
        assertEquals(1, reapable.cancelCallTimes.size());
        assertWithinTolerance(startTime+1000, reapable.cancelCallTimes.get(0));
    }

    @Test
    public void testSlowTracingReaper() throws Exception {

        // [JBTM-3468] arrange matters such that a tracing request is blocked by another tx's slow timeout,
        // such that the reaper will see the TRACE itself timeout and need to be requeued:

        arjPropertyManager.getCoordinatorEnvironmentBean().setTxReaperTraceGracePeriod(1100);
        arjPropertyManager.getCoordinatorEnvironmentBean().setTxReaperTraceInterval(100);

        TransactionReaper reaper = TransactionReaper.transactionReaper();
        MockReapable tracingReapable = new MockReapable(new Uid(), 0);
        MockReapable blockingReapable = new MockReapable(new Uid(), 1000);

        reaper.insert(blockingReapable, 1);
        reaper.insert(tracingReapable, 2);

        Thread.sleep(2200);
        TransactionReaper.terminate(false);

        assertFalse(blockingReapable.cancelCallTimes.isEmpty());
        assertTrue(blockingReapable.recordStackTracesCallTimes.isEmpty());
        assertFalse(tracingReapable.cancelCallTimes.isEmpty());
        assertFalse(tracingReapable.recordStackTracesCallTimes.isEmpty());
    }

    public void assertWithinTolerance(long a, long b) {
        long diff = Math.abs(a-b);
        // 100ms should be enough to give us some wiggle room with startup overhead and thread scheduling
        assertTrue(diff < 100);
    }

    public class MockReapable implements Reapable
    {
        private Uid uid;
        private final int wedgeTime;
        public List<Long> cancelCallTimes = new ArrayList<>();
        public List<Long> recordStackTracesCallTimes = new ArrayList<>();
        public List<Long> outputCapturedStackTracesCallTimes = new ArrayList<>();

        public MockReapable(Uid uid, int wedgeTime)
        {
            this.uid = uid;
            this.wedgeTime = wedgeTime;
        }

        public boolean running()
        {
            return true;
        }

        public boolean preventCommit()
        {
            return false;
        }

        public Uid get_uid()
        {
            return uid;
        }

        public int cancel()
        {
            cancelCallTimes.add(System.currentTimeMillis());
            try {
                if(wedgeTime > 0) {
                    Thread.sleep(wedgeTime);
                }
            } catch (InterruptedException e) {}
            return ActionStatus.ABORTED;
        }

        @Override
        public void recordStackTraces() {
            recordStackTracesCallTimes.add(System.currentTimeMillis());
        }

        @Override
        public void outputCapturedStackTraces() {
            outputCapturedStackTracesCallTimes.add(System.currentTimeMillis());
        }
    }
}