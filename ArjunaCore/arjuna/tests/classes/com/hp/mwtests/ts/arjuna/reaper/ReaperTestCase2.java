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
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.arjuna.reaper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;

/**
 * Exercise cancellation behaviour of TransactionReaper with resources
 * that time out and, optionally, get wedged either when a cancel is
 * tried and/or when an interrupt is delivered
 *
 * @author Andrew Dinn (adinn@redhat.com), 2007-07-09
 */

@RunWith(BMUnitRunner.class)
@BMScript("reaper")
public class ReaperTestCase2  extends ReaperTestCaseControl
{
    @Test
    public void testReaper() throws Exception
    {
        TransactionReaper reaper = TransactionReaper.transactionReaper();

        // create slow reapables some of which will not respond immediately
        // to cancel requests and ensure that they get cancelled
        // and that the reaper does not get wedged

        // the rendezvous for the reapables are keyed by the reapable's uid

        Uid uid0 = new Uid();
        Uid uid1 = new Uid();
        Uid uid2 = new Uid();
        Uid uid3 = new Uid();

        // reapable0 will return CANCELLED from cancel and will rendezvous inside the cancel call
        // so we can delay it. prevent_commit should not get called so we don't care about the arguments
        TestReapable reapable0 = new TestReapable(uid0, true, true, false, false);
        // reapable1 will return CANCELLED from cancel and will not rendezvous inside the cancel call
        // prevent_commit should not get called so we don't care about the arguments
        TestReapable reapable1 = new TestReapable(uid1, true, false, false, false);
        // reapable2 will return RUNNING from cancel and will rendezvous inside the cancel call
        // the call will get delayed causing the worker to exit as a zombie
        // prevent_commit will be called from the reaper thread and will fail but will not rendezvous
        TestReapable reapable2 = new TestReapable(uid2, false, true, false, false);
        // reapable3 will return RUNNING from cancel and will not rendezvous inside the cancel call
        // prevent_commit should get called and should return true without a rendezvous
        TestReapable reapable3 = new TestReapable(uid3, false, false, true, false);

        // enable a repeatable rendezvous before checking the reapable queue
        enableRendezvous("reaper1", true);
        // enable a repeatable rendezvous when synchronizing on a timed out reapoer element so we can check that
        // the element is the one we expect.
        enableRendezvous("reaper element", true);
        // enable a repeatable rendezvous before processing a timed out reapable
        // enableRendezvous("reaper2", true);
        // enable a repeatable rendezvous before scheduling a reapable in the worker queue for cancellation
        // enableRendezvous("reaper3", true);
        // enable a repeatable rendezvous before rescheduling a reapable in the worker queue for cancellation
        // enableRendezvous("reaper4", true);
        // enable a repeatable rendezvous before interrupting a cancelled reapable
        // enableRendezvous("reaper5", true);
        // enable a repeatable rendezvous before marking a worker thread as a zombie
        // enableRendezvous("reaper6", true);
        // enable a repeatable rendezvous before marking a reapable as rollback only from the reaper thread
        // enableRendezvous("reaper7", true);
        // enable a repeatable rendezvous before checking the worker queue
        enableRendezvous("reaperworker1", true);
        // enable a repeatable rendezvous before marking a reapable as cancel
        // enableRendezvous("reaperworker2", true);
        // enable a repeatable rendezvous before calling cancel
        // enableRendezvous("reaperworker3", true);
        // enable a repeatable rendezvous before marking a reapable as rollback only from the worker thread
        // enableRendezvous("reaperworker4", true);

        // enable a repeatable rendezvous for each of the test reapables which we have marked to
        // perform a rendezvous

        enableRendezvous(uid0, true);
        enableRendezvous(uid2, true);

        // STAGE I
        // insert two reapables so they timeout at 1 second intervals then stall the first one and
        // check progress of cancellations and rollbacks for both

        reaper.insert(reapable0, 1);

        reaper.insert(reapable1, 1);

        //assertTrue(reaper.insert(reapable2, 1));

        //assertTrue(reaper.insert(reapable3, 1));

        // latch the reaper before it tries to process the queue

        triggerRendezvous("reaper1");

        // make sure they were all registered
        // the transactions queue should be
        // UID0 RUNNING
        // UID1 RUNNING

        assertEquals(2, reaper.numberOfTransactions());
        assertEquals(2, reaper.numberOfTimeouts());

        // wait long enough to ensure both reapables have timed out

        triggerWait(1000);

        // now let the reaper dequeue the first reapable, process it and queue it for the worker thread
        // to deal with

        triggerRendezvous("reaper1");

        // latch the reaper at the reaper element check

        triggerRendezvous("reaper element");

        // check that we have dequeued reapable0

        assertTrue(checkAndClearFlag(reapable0));

        // unlatch the reaper so it can process the element

        triggerRendezvous("reaper element");

        // latch the reaper before it tests the queue again

        triggerRendezvous("reaper1");

        // latch the reaperworker before it tries to dequeue from the worker queue

        triggerRendezvous("reaperworker1");

        // the transactions queue should be
        // UID1 RUNNING
        // UID0 CANCEL

        assertEquals(2, reaper.numberOfTransactions());
        assertEquals(2, reaper.numberOfTimeouts());

        // now let the worker dequeue a reapable and proceed to call cancel

        triggerRendezvous("reaperworker1");

        // latch the first reapable inside cancel

        triggerRendezvous(uid0);

        // now let the reaper check the queue for the second reapable, dequeue it and add it to the
        // worker queue

        triggerRendezvous("reaper1");

        // latch the reaper at the reaper element check

        triggerRendezvous("reaper element");

        // check that we have dequeued reapable1

        assertTrue(checkAndClearFlag(reapable1));

        // unlatch the reaper so it can process the element

        triggerRendezvous("reaper element");

        // latch the reaper before it tests the queue again

        triggerRendezvous("reaper1");

        // the transactions queue should be
        // UID0 CANCEL
        // UID1 SCHEDULE_CANCEL

        assertEquals(2, reaper.numberOfTransactions());
        assertEquals(2, reaper.numberOfTimeouts());

        // ensure we wait long enough for the cancel to time out

        triggerWait(500);

        // now let the reaper check the queue and interrupt the cancel for UID1

        triggerRendezvous("reaper1");

        // latch the reaper at the reaper element check

        triggerRendezvous("reaper element");

        // check that we have dequeued reapable0

        assertTrue(checkAndClearFlag(reapable0));

        // unlatch the reaper so it can process the element

        triggerRendezvous("reaper element");

        // latch the reaper before it tests the queue again

        triggerRendezvous("reaper1");

        // unlatch the first reapable inside cancel

        triggerRendezvous(uid0);

        // latch the worker as it is about to process the queue again

        triggerRendezvous("reaperworker1");

        // the transactions queue should be
        // UID1 SCHEDULE_CANCEL

        assertEquals(1, reaper.numberOfTransactions());
        assertEquals(1, reaper.numberOfTimeouts());

        // let the worker clear and cancel the 2nd reapable

        triggerRendezvous("reaperworker1");

        // latch the worker before it reads the worker queue

        triggerRendezvous("reaperworker1");

        // the transactions queue should be empty

        assertEquals(0, reaper.numberOfTransactions());
        assertEquals(0, reaper.numberOfTimeouts());

        // ensure that cancel was tried on reapable1 and that set rollback only was not tried on either
        // we know cancel was tried on reapable0 because we got through the rendezvous

        assertTrue(reapable1.getCancelTried());
        assertTrue(!reapable0.getRollbackTried());
        assertTrue(!reapable1.getRollbackTried());
        assertTrue(checkAndClearFlag("interrupted"));

        // STAGE II
        // now use the next pair of reapables to check that a wedged reaperworker gets tuirned into a zombie and
        // a new worker gets created to cancel the remaining reapables.
        // insert reapables so they timeout at 1 second intervals then
        // check progress of cancellations and rollbacks

        reaper.insert(reapable2, 1);

        reaper.insert(reapable3, 1);

        // make sure they were all registered
        // the transactions queue should be
        // UID2 RUNNING
        // UID3 RUNNING

        assertEquals(2, reaper.numberOfTransactions());
        assertEquals(2, reaper.numberOfTimeouts());

        // wait long enough to ensure both reapables have timed out

        triggerWait(1000);

        // now let the reaper dequeue the first reapable, process it and queue it for the worker thread
        // to deal with

        triggerRendezvous("reaper1");

        // latch the reaper at the reaper element check

        triggerRendezvous("reaper element");

        // check that we have dequeued reapable2

        assertTrue(checkAndClearFlag(reapable2));

        // unlatch the reaper so it can process the element

        triggerRendezvous("reaper element");

        // latch the reaper before it tests the queue again

        triggerRendezvous("reaper1");

        // the transactions queue should be
        // UID3 RUNNING
        // UID2 CANCEL

        assertEquals(2, reaper.numberOfTransactions());
        assertEquals(2, reaper.numberOfTimeouts());

        // now let the worker dequeue the fourth reapable and proceed to call cancel

        triggerRendezvous("reaperworker1");

        // latch the third reapable inside cancel

        triggerRendezvous(uid2);

        // now let the reaper check the queue for the fourth reapable, dequeue it and add it to the
        // worker queue

        triggerRendezvous("reaper1");

        // latch the reaper at the reaper element check

        triggerRendezvous("reaper element");

        // check that we have dequeued reapable3

        assertTrue(checkAndClearFlag(reapable3));

        // unlatch the reaper so it can process the element

        triggerRendezvous("reaper element");

        // latch the reaper before it tests the queue again

        triggerRendezvous("reaper1");

        // the transactions queue should be
        // UID2 CANCEL
        // UID3 SCHEDULE_CANCEL

        assertEquals(2, reaper.numberOfTransactions());
        assertEquals(2, reaper.numberOfTimeouts());

        // ensure we wait long enough for the cancel to time out

        triggerWait(500);

        // now let the reaper check the queue and interrupt the cancel for UID3

        triggerRendezvous("reaper1");

        // latch the reaper at the reaper element check

        triggerRendezvous("reaper element");

        // check that we have dequeued reapable2

        assertTrue(checkAndClearFlag(reapable2));

        // unlatch the reaper so it can process the element

        triggerRendezvous("reaper element");

        // latch the reaper before it tests the queue again

        triggerRendezvous("reaper1");

        assertTrue(checkAndClearFlag("interrupted"));

        // ensure we wait long enough for the cancel to time out

        triggerWait(500);

        // the transactions queue should be
        // UID3 SCHEDULE_CANCEL
        // UID2 CANCEL_INTERRUPTED

        assertEquals(2, reaper.numberOfTransactions());
        assertEquals(2, reaper.numberOfTimeouts());

        // let the reaper check the queue and reschedule the fourth reapable

        triggerRendezvous("reaper1");

        // latch the reaper at the reaper element check

        triggerRendezvous("reaper element");

        // check that we have dequeued reapable3

        assertTrue(checkAndClearFlag(reapable3));

        // unlatch the reaper so it can process the element

        triggerRendezvous("reaper element");

        // latch the reaper before it tests the queue again

        triggerRendezvous("reaper1");

        // the transactions queue should be
        // UID2 CANCEL_INTERRUPTED
        // UID3 SCHEDULE_CANCEL

        assertEquals(2, reaper.numberOfTransactions());
        assertEquals(2, reaper.numberOfTimeouts());

        // let the reaper check the queue and mark the reaper worker as a zombie

        triggerRendezvous("reaper1");

        // latch the reaper at the reaper element check

        triggerRendezvous("reaper element");

        // check that we have dequeued reapable2

        assertTrue(checkAndClearFlag(reapable2));

        // unlatch the reaper so it can process the element

        triggerRendezvous("reaper element");

        // latch the reaper before it tests the queue again

        triggerRendezvous("reaper1");

        // the reaper should have marked the thread as a zombie 
        
        assertTrue(checkAndClearFlag("zombied"));

        // the transactions queue should be
        // UID3 SCHEDULE_CANCEL

        assertEquals(1, reaper.numberOfTransactions());
        assertEquals(1, reaper.numberOfTimeouts());

        // unlatch the third reapable inside cancel

        triggerRendezvous(uid2);

        // latch the new worker as it is about to process the queue again

        triggerRendezvous("reaperworker1");

        // let the worker clear and cancel the 2nd reapable

        triggerRendezvous("reaperworker1");

        // latch the worker before it reads the worker queue

        triggerRendezvous("reaperworker1");

        // the transactions queue should be empty

        assertEquals(0, reaper.numberOfTransactions());
        assertEquals(0, reaper.numberOfTimeouts());

        // ensure that cancel was tried on reapable3 and that set rollback only was tried on reapable2
        // and reapable3 we know cancel was tried on reapable2 because we got through the rendezvous

        assertTrue(reapable3.getCancelTried());
        assertTrue(reapable2.getRollbackTried());
        assertTrue(reapable3.getRollbackTried());
    }
}
