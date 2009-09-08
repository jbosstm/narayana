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

import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.Reapable;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.common.Uid;

import org.junit.Test;
import static org.junit.Assert.*;

public class ReaperTestCase3  extends ReaperTestCaseControl
{
    @Test
    public void testReaperWait() throws Exception
    {
        TransactionReaper.create(500);
        TransactionReaper reaper = TransactionReaper.transactionReaper();

        // give the reaper worker time to start too

        Thread.sleep(1000);

        // create test reapables some of which will not respond immediately to cancel requests

        Uid uid0 = new Uid();
        Uid uid1 = new Uid();
        Uid uid2 = new Uid();
        Uid uid3 = new Uid();

        // reapable0 will return CANCELLED from cancel and will rendezvous inside the cancel call
        // so we can delay it. prevent_commit should not get called so we don't care about the arguments
        TestReapable reapable0 = new TestReapable(uid0, true, false, false, false);
        // reapable1 will return CANCELLED from cancel and will not rendezvous inside the cancel call
        // prevent_commit should not get called so we don't care about the arguments
        TestReapable reapable1 = new TestReapable(uid1, true, false, false, false);
        // reapable2 will return CANCELLED from cancel and will not rendezvous inside the cancel call
        // prevent_commit should not get called so we don't care about the arguments
        TestReapable reapable2 = new TestReapable(uid2, true, false, false, false);
        // reapable3 will return CANCELLED from cancel and will not rendezvous inside the cancel call
        // prevent_commit should not get called so we don't care about the arguments
        TestReapable reapable3 = new TestReapable(uid3, true, false, false, false);
        // enable a repeatable rendezvous before checking the reapable queue
        enableRendezvous("reaper1", true);
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

        // enableRendezvous(uid0, true);

        // insert reapables so they timeout at 1 second intervals then
        // check progress of cancellations and rollbacks

        assertTrue(reaper.insert(reapable0, 1));

        assertTrue(reaper.insert(reapable1, 2));

        assertTrue(reaper.insert(reapable2, 3));

        assertTrue(reaper.insert(reapable3, 4));

        // latch the reaper before it checks the queue

        triggerRendezvous("reaper1");

        // make sure they were all registered

        assertEquals(4, reaper.numberOfTransactions());
        assertEquals(4, reaper.numberOfTimeouts());

        // ensure the first reapable is ready

        triggerWait(1000);

        // let the reaper process the first reapable then latch it again before it checks the queue

        triggerRendezvous("reaper1");

        triggerRendezvous("reaper1");

        // latch the worker before it checks the worker queue

        triggerRendezvous("reaperworker1");

        // let the worker process the first reapable then latch it again before it checks the queue

        triggerRendezvous("reaperworker1");

        triggerRendezvous("reaperworker1");

        // force a termination waiting for the normal timeout periods
        // byteman rules will ensure that the reaper and reaperworker rendezvous get deleted
        // under this call

        TransactionReaper.terminate(true);

        assertEquals(0, reaper.numberOfTransactions());

        assertTrue(reapable0.getCancelTried());
        assertTrue(reapable1.getCancelTried());
        assertTrue(reapable2.getCancelTried());
        assertTrue(reapable3.getCancelTried());
    }

    @Test
    public void testReaperForce() throws Exception
    {
        TransactionReaper.create(5000);
        TransactionReaper reaper = TransactionReaper.transactionReaper();

        // give the reaper worker time to start too

        Thread.sleep(1000);

        // create test reapables some of which will not respond immediately to cancel requests

        Uid uid0 = new Uid();
        Uid uid1 = new Uid();
        Uid uid2 = new Uid();
        Uid uid3 = new Uid();

        // reapable0 will return CANCELLED from cancel and will rendezvous inside the cancel call
        // so we can delay it. prevent_commit should not get called so we don't care about the arguments
        TestReapable reapable0 = new TestReapable(uid0, true, false, false, false);
        // reapable1 will return CANCELLED from cancel and will not rendezvous inside the cancel call
        // prevent_commit should not get called so we don't care about the arguments
        TestReapable reapable1 = new TestReapable(uid1, true, false, false, false);
        // reapable2 will return CANCELLED from cancel and will not rendezvous inside the cancel call
        // prevent_commit should not get called so we don't care about the arguments
        TestReapable reapable2 = new TestReapable(uid2, true, false, false, false);
        // reapable3 will return CANCELLED from cancel and will not rendezvous inside the cancel call
        // prevent_commit should not get called so we don't care about the arguments
        TestReapable reapable3 = new TestReapable(uid3, true, false, false, false);
        // enable a repeatable rendezvous before checking the reapable queue
        enableRendezvous("reaper1", true);
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

        // enableRendezvous(uid0, true);

        assertTrue(reaper.insert(reapable0, 1));

        assertTrue(reaper.insert(reapable1, 2));

        assertTrue(reaper.insert(reapable2, 3));

        assertTrue(reaper.insert(reapable3, 4));

        // latch the reaper before it checks the queue

        triggerRendezvous("reaper1");

        // make sure they were all registered

        assertEquals(4, reaper.numberOfTransactions());
        assertEquals(4, reaper.numberOfTimeouts());

        // ensure the first reapable is ready

        triggerWait(1000);

        // let the reaper process the first reapable then latch it again before it checks the queue

        triggerRendezvous("reaper1");

        triggerRendezvous("reaper1");

        // latch the worker before it checks the worker queue

        triggerRendezvous("reaperworker1");

        // let the worker process the first reapable then latch it again before it checks the queue

        triggerRendezvous("reaperworker1");

        triggerRendezvous("reaperworker1");

        // force a termination and don't wait for the normal timeout periods
        // byteman rules will ensure that the reaper and reaperworker rendezvous gte deleted
        // under this call

        TransactionReaper.terminate(false);

        assertEquals(0, reaper.numberOfTransactions());

        assertTrue(reapable0.getCancelTried());
        assertTrue(reapable1.getCancelTried());
        assertTrue(reapable2.getCancelTried());
        assertTrue(reapable3.getCancelTried());

        /*
        * Since we've (hopefully) just run two tests with new reapers in the same VM
        * we've also shown that it's possible to start/terminate/start again!
        */
    }

    public class SlowReapable implements Reapable
    {
        public SlowReapable(Uid uid, int callDelay, int interruptDelay, boolean doCancel, boolean doRollback)
        {
            this.uid = uid;
            this.callDelay = callDelay;
            this.interruptDelay = interruptDelay;
            this.doCancel = doCancel;
            this.doRollback = doRollback;
            cancelTried = false;
            rollbackTried = false;
            running = true;
        }

        public boolean running()
        {
            return getRunning();
        }

        public boolean preventCommit()
        {
            setRollbackTried();
            clearRunning();
            return doRollback;
        }

        public int cancel()
        {
            boolean interrupted = false;

            setCancelTried();

            // track the worker trying to do the cancel so we can
            // detect if it becomes a zombie

            setCancelThread(Thread.currentThread());

            if (callDelay > 0) {
                try {
                    Thread.sleep(callDelay);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if (interrupted && interruptDelay > 0) {
                try {
                    Thread.sleep(interruptDelay);
                } catch (InterruptedException e) {
                }
            }

            if (doCancel) {
                clearRunning();
                return ActionStatus.ABORTED;
            } else {
                return ActionStatus.RUNNING;
            }
        }

        public Uid get_uid()
        {
            return uid;
        }

        private Uid uid;
        private int callDelay; // in milliseconds
        private int interruptDelay; // in milliseconds
        private boolean doCancel;
        private boolean doRollback;
        private boolean cancelTried;
        private boolean rollbackTried;
        private boolean running;
        private Thread cancelThread;

        public synchronized void setCancelTried()
        {
            cancelTried = true;
        }

        public synchronized boolean getCancelTried()
        {
            return cancelTried;
        }

        public synchronized void setCancelThread(Thread cancelThread)
        {
            this.cancelThread = cancelThread;
        }

        public synchronized Thread getCancelThread()
        {
            return cancelThread;
        }

        public synchronized void setRollbackTried()
        {
            rollbackTried = true;
        }

        public synchronized boolean getRollbackTried()
        {
            return rollbackTried;
        }

        public synchronized void clearRunning()
        {
            running = false;
        }

        public synchronized boolean getRunning()
        {
            return running;
        }
    }
}
