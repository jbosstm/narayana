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

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.Reapable;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.arjuna.coordinator.ReaperElement;

import java.util.SortedSet;
import java.util.TreeSet;


public class ReaperTestCase3 extends TestCase
{
    public static Test suite()
    {
	return new TestSuite(ReaperTestCase3.class);
    }

    public void testReaperWait () throws Exception
    {
	TransactionReaper.create(500);
	TransactionReaper reaper = TransactionReaper.transactionReaper();

	// give the reaper worker time to start too

	Thread.sleep(1000);

	// create slow reapables some of which will not respond immediately
        // to cancel requests and ensure that they get cancelled
        // and that the reaper does not get wedged

	SlowReapable reapable1 = new SlowReapable(new Uid(), 2000, 0, true, true);
	SlowReapable reapable2 = new SlowReapable(new Uid(), 0, 0, true, true);
	SlowReapable reapable3 = new SlowReapable(new Uid(), 100, 2000, false, true);
	SlowReapable reapable4 = new SlowReapable(new Uid(), 1000, 1000, false, false);

	// insert reapables so they timeout at 1 second intervals then
	// check progress of cancellations and rollbacks

	assertTrue(reaper.insert(reapable1, 1));

	assertTrue(reaper.insert(reapable2, 2));

	assertTrue(reaper.insert(reapable3, 3));

	assertTrue(reaper.insert(reapable4, 4));

	// make sure they were all registered

	assertEquals(4, reaper.numberOfTransactions());
	assertEquals(4, reaper.numberOfTimeouts());

	// force a termination but wait for the transactions to timeout
	
	TransactionReaper.terminate(true);
	
	assertEquals(0, reaper.numberOfTransactions());
	
	assertTrue(reapable1.getCancelTried());
	assertTrue(reapable2.getCancelTried());
	assertTrue(reapable3.getCancelTried());
	assertTrue(reapable4.getCancelTried());
    }
    
    public void testReaperForce () throws Exception
    {
        TransactionReaper.create(5000);
        TransactionReaper reaper = TransactionReaper.transactionReaper();

        // give the reaper worker time to start too

        Thread.sleep(1000);

        // create slow reapables some of which will not respond immediately
        // to cancel requests and ensure that they get cancelled
        // and that the reaper does not get wedged

        SlowReapable reapable1 = new SlowReapable(new Uid(), 2000, 0, true, true);
        SlowReapable reapable2 = new SlowReapable(new Uid(), 0, 0, true, true);
        SlowReapable reapable3 = new SlowReapable(new Uid(), 100, 2000, false, true);
        SlowReapable reapable4 = new SlowReapable(new Uid(), 1000, 1000, false, false);

        // insert reapables so they timeout at 1 second intervals then
        // check progress of cancellations and rollbacks

        assertTrue(reaper.insert(reapable1, 1));

        assertTrue(reaper.insert(reapable2, 2));

        assertTrue(reaper.insert(reapable3, 3));

        assertTrue(reaper.insert(reapable4, 4));

        // make sure they were all registered

        assertEquals(4, reaper.numberOfTransactions());
        assertEquals(4, reaper.numberOfTimeouts());

        // force a termination and don't wait for the normal timeout periods
        
        TransactionReaper.terminate(false);
        
        assertEquals(0, reaper.numberOfTransactions());
        
        assertTrue(reapable1.getCancelTried());
        assertTrue(reapable2.getCancelTried());
        assertTrue(reapable3.getCancelTried());
        assertTrue(reapable4.getCancelTried());
        
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
