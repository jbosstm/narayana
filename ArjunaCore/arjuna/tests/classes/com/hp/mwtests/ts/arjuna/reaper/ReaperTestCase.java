/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 *
 * (C) 2007,
 * @author JBoss, a division of Red Hat.
 */
package com.hp.mwtests.ts.arjuna.reaper;

import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.Reapable;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.arjuna.coordinator.ReaperElement;

import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Exercises some aspects of the TransactionReaper functionality.
 *
 * @author jonathan.halliday@redhat.com, 2007-04-30
 */

@RunWith(BMUnitRunner.class)
@BMScript("reaper")
public class ReaperTestCase extends ReaperTestCaseControl
{
    @Test
    public void testReaper() throws Exception
    {

        TransactionReaper reaper = TransactionReaper.transactionReaper();


        Reapable reapable = new MockReapable(new Uid());
        Reapable reapable2 = new MockReapable(new Uid());
        Reapable reapable3 = new MockReapable(new Uid());

        ReaperElement reaperElement = new ReaperElement(reapable, 30);
        ReaperElement reaperElement2 = new ReaperElement(reapable2, 20);
        ReaperElement reaperElement3 = new ReaperElement(reapable3, 10);

        // test that ordering is by timeout, regardless of insertion order
        SortedSet sortedSet = new TreeSet();
        sortedSet.add(reaperElement);
        sortedSet.add(reaperElement3);
        sortedSet.add(reaperElement2);

        assertEquals(sortedSet.first(), reaperElement3);
        assertEquals(sortedSet.last(), reaperElement);

        // test insertion of timeout=0 is a nullop
        reaper.insert(reapable, 0);
        assertEquals(0, reaper.numberOfTransactions());
        assertEquals(0, reaper.numberOfTimeouts());
        reaper.remove(reapable);

        // test that duplicate insertion fails
        reaper.insert(reapable, 10);
        assertEquals(1, reaper.numberOfTransactions());
        assertEquals(1, reaper.numberOfTimeouts());
        try {
            reaper.insert(reapable, 10);
            fail("duplicate insert failed to blow up");
        } catch(Exception e) {
        }
        reaper.remove(reapable);
        assertEquals(0, reaper.numberOfTransactions());
        assertEquals(0, reaper.numberOfTimeouts());

        // test that timeout change fails
        reaper.insert(reapable, 10);
        try {
            reaper.insert(reapable, 20);
            fail("timeout change insert failed to blow up");
        } catch(Exception e) {
        }
        assertEquals(1, reaper.numberOfTransactions());
        assertEquals(1, reaper.numberOfTimeouts());
        assertEquals(10, reaper.getTimeout(reapable));
        reaper.remove(reapable);
        assertEquals(0, reaper.numberOfTransactions());
        assertEquals(0, reaper.numberOfTimeouts());

        // enable a repeatable rendezvous before checking the reapable queue
        enableRendezvous("reaper1", true);
        // enable a repeatable rendezvous before scheduling a reapable in the worker queue for cancellation
        enableRendezvous("reaper2", true);
        // enable a repeatable rendezvous before checking the worker queue
        enableRendezvous("reaperworker1", true);

        // test reaping
        reaper.insert(reapable, 1); // seconds
        reaper.insert(reapable2, 2);

        // the reaper will be latched before it processes any of the reapables
        triggerRendezvous("reaper1");
        assertEquals(2, reaper.numberOfTransactions());
        assertEquals(2, reaper.numberOfTimeouts());
        // ensure we have waited at lest 1 second so the first reapable is timed out
        triggerWait(1000);
        // let the reaper proceed with the dequeue and add the entry to the work queue
        triggerRendezvous("reaper1");
        triggerRendezvous("reaper2");
        triggerRendezvous("reaper2");
        // now latch the reaper worker at the dequeue
        triggerRendezvous("reaperworker1");
        // we shoudl still have two reapables in the reaper queue
        assertEquals(2, reaper.numberOfTransactions());
        assertEquals(2, reaper.numberOfTimeouts());
        // now let the worker process the work queue element -- it should not call cancel since the
        // mock reapable will not claim to be running
        triggerRendezvous("reaperworker1");
        // latch the reaper and reaper worker before they check their respective queues
        // latch the reaper before it dequeues the next reapable
        triggerRendezvous("reaper1");
        triggerRendezvous("reaperworker1");
        // we should now have only 1 element in the reaper queue
        assertEquals(1, reaper.numberOfTransactions());
        assertEquals(1, reaper.numberOfTimeouts());
        // ensure we have waited at lest 1 second so the second reapable is timed out
        triggerWait(1000);
        // now let the reaper proceed with the next dequeue and enqueue the reapable for the worker to process
        triggerRendezvous("reaper1");
        triggerRendezvous("reaper2");
        triggerRendezvous("reaper2");
        // relatch the reaper next time round the loop so we can be sure it is not monkeying around
        // with the transactions queue
        triggerRendezvous("reaper1");
        // the worker is still latched so we should still have one entry in the work queue
        assertEquals(1, reaper.numberOfTransactions());
        assertEquals(1, reaper.numberOfTimeouts());
        // now let the worker process the work queue element -- it should not call cancel since the
        // mock reapable wil not claim to be running
        triggerRendezvous("reaperworker1");
        // latch reaper worker again so we know it has finished processing the element
        triggerRendezvous("reaperworker1");
        assertEquals(0, reaper.numberOfTransactions());
        assertEquals(0, reaper.numberOfTimeouts());
    }

    public class MockReapable implements Reapable
    {
        public MockReapable(Uid uid)
        {
            this.uid = uid;
        }

        public boolean running()
        {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean preventCommit()
        {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public int cancel()
        {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Uid get_uid()
        {
            return uid;
        }

        private Uid uid;
    }
}
