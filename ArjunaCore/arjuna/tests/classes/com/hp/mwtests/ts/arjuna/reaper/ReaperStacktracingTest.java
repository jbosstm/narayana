/*
 * Copyright Red Hat.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        MockReapable reapable = new MockReapable(new Uid());
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

    public void assertWithinTolerance(long a, long b) {
        long diff = Math.abs(a-b);
        // 100ms should be enough to give us some wiggle room with startup overhead and thread scheduling
        assertTrue(diff < 100);
    }

    public class MockReapable implements Reapable
    {
        private Uid uid;
        public List<Long> cancelCallTimes = new ArrayList<>();
        public List<Long> recordStackTracesCallTimes = new ArrayList<>();
        public List<Long> outputCapturedStackTracesCallTimes = new ArrayList<>();

        public MockReapable(Uid uid)
        {
            this.uid = uid;
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
