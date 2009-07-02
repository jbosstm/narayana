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

/**
 * Exercise cancellation behaviour of TransactionReaper with resources
 * that time out and, optionally, get wedged either when a cancel is
 * tried and/or when an interrupt is delivered
 *
 * @author Andrwe Dinn (adinn@redhat.com), 2007-07-09
 */

public class ReaperTestCase2
{
    @Test
    public void testReaper() throws Exception
    {
        TransactionReaper.create(500);
        TransactionReaper reaper = TransactionReaper.transactionReaper();

        // give the reaper worker time to start too

        Thread.sleep(1000);

        // create slow reapables some of which will not respond immediately
        // to cancel requests and ensure that they get cancelled
        // and that the reaper does not get wedged

        SlowReapable reapable1 = new SlowReapable(new Uid(), 2000, 0, true, true, false);
        SlowReapable reapable2 = new SlowReapable(new Uid(), 0, 0, true, true, false);
        SlowReapable reapable3 = new SlowReapable(new Uid(), 100, 2000, false, true, false);
        SlowReapable reapable4 = new SlowReapable(new Uid(), 1000, 1000, false, false, false);

        // insert reapables so they timeout at 1 second intervals then
        // check progress of cancellations and rollbacks

        assertTrue(reaper.insert(reapable1, 1));

        assertTrue(reaper.insert(reapable2, 2));

        assertTrue(reaper.insert(reapable3, 3));

        assertTrue(reaper.insert(reapable4, 4));

        // make sure they were all registered

        assertEquals(4, reaper.numberOfTransactions());
        assertEquals(4, reaper.numberOfTimeouts());

        // n.b. the reaper will not operate in dynamic mode by default
        // so we have to allow an extra checkPeriod millisecs for it
        // to detect timeouts (it may go back to sleep a few
        // milliseconds before a transaction times out). also by
        // default the reaper waits 500 msecs for a cancel to take
        // effect before interrupting and 500 msecs for an interrupt
        // to take effect before making a wedged worker a zombie. so
        // these need to be factored into this thread's delays when
        // tetsing the state of the reapables.

        // wait at most 2 seconds for the first reapable to be cancelled

        int count = 0;

        while (!reapable1.getCancelTried() && count < 20) {
            count++;
            Thread.sleep(100);
        }

        assertTrue(count < 20);

        // ensure that the second one gets cancelled even if  the
        // first one is wedged

        count = 0;

        while (reapable2.getRunning() && count < 15) {
            count++;
            Thread.sleep(100);
        }

        assertTrue(count < 15);

        // ensure also that the second one gave up at the cancel and
        // not the mark for rollback

        assertTrue(reapable2.getCancelTried());

        // This assert causes timing issues on some build machines
        // - skip for now pending a more robust fix e.g. code injection
        //assertTrue(!reapable2.getRollbackTried());

        // ensure that the first one responded to the interrupt and
        // marks itself for rollback only

        count = 0;

        while (reapable1.getRunning() && count < 10) {
            count++;
            Thread.sleep(100);
        }

        assertTrue(count < 10);
        // the first one should not be running because it marks itself ActionStatus.ABORTED
        // - the reaper should not attempt to roll it back in this case
        assertTrue(!reapable1.getRollbackTried());

        // check that the third one refuses the cancel and gets marked
        // for rollback instead

        count = 0;

        while (!reapable3.getCancelTried() && count < 25) {
            count++;
            Thread.sleep(100);
        }

        assertTrue(count < 25);

        // ensure that it gets marked for rollback

        count = 0;

        while (reapable3.getRunning() && count < 10) {
            count++;
            Thread.sleep(100);
        }

        assertTrue(count < 10);

        assertTrue(reapable3.getRollbackTried());

        // ensure the fourth one gets cancelled and marked for rolback
        // even though it does not play ball

        count = 0;

        while (reapable4.getRunning() && count < 25) {
            count++;
            Thread.sleep(100);
        }

        assertTrue(count < 25);

        assertTrue(reapable4.getCancelTried());
        assertTrue(reapable4.getRollbackTried());

        // we should have a zombie worker so check that the thread
        // which tried the cancel is still runnning then sleep a bit
        // to give it time to complete and check it has exited

        Thread worker = reapable4.getCancelThread();

        assertTrue(worker.isAlive());

        count = 0;

        while (worker.isAlive() && count < 30) {
            count++;
            Thread.sleep(100);
        }

        assertTrue(count < 30);
    }

    public class SlowReapable implements Reapable
    {
        public SlowReapable(Uid uid, int callDelay, int interruptDelay, boolean doCancel, boolean doRollback, boolean doComplete)
        {
            this.uid = uid;
            this.callDelay = callDelay;
            this.interruptDelay = interruptDelay;
            this.doCancel = doCancel;
            this.doRollback = doRollback;
            this.doComplete = doComplete;
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
        private boolean doComplete;
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
