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
 * (C) 2005-2007,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionReaper.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.listener.ReaperMonitor;

import com.arjuna.ats.internal.arjuna.coordinator.*;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.common.util.logging.*;

import java.util.*;

/**
 * Class to record transactions with non-zero timeout values, and class to
 * implement a transaction reaper thread which terminates these transactions
 * once their timeout elapses.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: TransactionReaper.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 *
 *
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_1
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_1] -
 *          TransactionReaper - could not create transaction list. Out of
 *          memory.
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_2
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_2] -
 *          TransactionReaper::check - comparing {0}
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_3
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_3] -
 *          TransactionReaper::getTimeout for {0} returning {1}
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_17
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_17] -
 *          TransactionReaper::getRemainingTimeoutMillis for {0} returning {1}
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_4
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_4] -
 *          TransactionReaper::check interrupting cancel in progress for {0}
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_5
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_5] -
 *          TransactionReaper::check worker zombie count {0] exceeds specified limit
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_6
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_6] -
 *          TransactionReaper::check worker {0} not responding to interrupt when cancelling TX {1} -- worker marked as zombie and TX scheduled for mark-as-rollback
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_7
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_7] -
 *          TransactionReaper::doCancellations worker {0} successfully canceled TX {1}
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_8
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_8] -
 *          TransactionReaper::doCancellations worker {0} failed to cancel TX {1} -- rescheduling for mark-as-rollback
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_9
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_9] -
 *          TransactionReaper::doCancellations worker {0} exception during cancel of TX {1} -- rescheduling for mark-as-rollback
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_10
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_10] -
 *          TransactionReaper::check successfuly marked TX {0} as rollback only
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_11
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_11] -
 *          TransactionReaper::check failed to mark TX {0}  as rollback only
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_12
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_12] -
 *          TransactionReaper::check exception while marking TX {0} as rollback only
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_13
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_13] -
 *          TransactionReaper::doCancellations worker {0} missed interrupt when cancelling TX {1} -- exiting as zombie (zombie count decremented to {2})
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_14
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_14] -
 *          TransactionReaper::doCancellations worker {0} successfuly marked TX {1} as rollback only
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_15
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_15] -
 *          TransactionReaper::doCancellations worker {0} failed to mark TX {1}  as rollback only
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_16
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_16] -
 *          TransactionReaper::doCancellations worker {0} exception while marking TX {1} as rollback only
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_18
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_18] -
 *          TransactionReaper::check timeout for TX {0} in state  {1}
 * @message com.arjuna.ats.arjuna.coordinator.TransactionReaper_19
 *          [com.arjuna.ats.arjuna.coordinator.TransactionReaper_19] -
 *          TransactionReaper NORMAL mode is deprecated. Update config to use PERIODIC for equivalent behaviour.
 */

public class TransactionReaper
{

	public static final String NORMAL = "NORMAL";

	public static final String DYNAMIC = "DYNAMIC";

    public static final String PERIODIC = "PERIODIC"; // the new name for 'NORMAL'

    public TransactionReaper(long checkPeriod)
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS,
					VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION,
					"TransactionReaper::TransactionReaper ( " + checkPeriod
							+ " )");
		}

		_checkPeriod = checkPeriod;

		if (_transactions == null)
		{
			if (tsLogger.arjLoggerI18N.isFatalEnabled())
			{
				tsLogger.arjLoggerI18N
						.fatal("com.arjuna.ats.arjuna.coordinator.TransactionReaper_1");
			}

			throw new OutOfMemoryError();
		}
	}

	public final long checkingPeriod()
	{
		if (_dynamic)
		{
			try
			{
				final ReaperElement head = (ReaperElement) _transactions.first();  //_list.peak();
				return head.getAbsoluteTimeout() - System.currentTimeMillis();
			}
			catch (final NoSuchElementException nsee)
			{
				return Long.MAX_VALUE; // list is empty, so we can sleep until something is inserted.
			}
		}
		else
		{
                     // if we have a cancel in progress which needs
                     // checking up on then we have to wake up in time
                     // for it whether we are using a static or
                     // dynamic model

                     try
                     {
                          final ReaperElement head = (ReaperElement) _transactions.first();  //_list.peak();
                          if (head._status != ReaperElement.RUN) {
                               long waitTime = head.getAbsoluteTimeout() - System.currentTimeMillis();
                               if (waitTime < _checkPeriod)
                               {
                                    return head.getAbsoluteTimeout() - System.currentTimeMillis();
                               }
                          }
                     }
                    catch (final NoSuchElementException nsee) {}

		    return _checkPeriod;
		}
	}

	/**
         * process all entries in the timeout queue which have
         * expired. entries for newly expired transactions are passed
         * to a worker thread for cancellation and requeued for
         * subsequent progress checks. the worker is given a kick if
         * such checks find it is wedged.
	 *
	 * Timeout is given in milliseconds.
	 */

	public final boolean check()
	{
	    if (tsLogger.arjLogger.debugAllowed())
	    {
		tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS,
					 VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION,
					 "TransactionReaper::check ()");
	    }

	    do
	    {
		final ReaperElement e ;
		try
		{
		    e = (ReaperElement)_transactions.first();
		}
		catch (final NoSuchElementException nsee)
		{
		    return true ;
		}

		if (tsLogger.arjLoggerI18N.isDebugEnabled())
		{
		    tsLogger.arjLoggerI18N
			.debug(
			    DebugLevel.FUNCTIONS,
			    VisibilityLevel.VIS_PUBLIC,
			    FacilityCode.FAC_ATOMIC_ACTION,
			    "com.arjuna.ats.arjuna.coordinator.TransactionReaper_2",
			    new Object[]
			    { Long.toString(e.getAbsoluteTimeout()) });
		}

		final long now = System.currentTimeMillis();

		if (now < e.getAbsoluteTimeout())
		{
		    // go back to sleep

		    break;
		}

		if (tsLogger.arjLoggerI18N.isWarnEnabled())
		{
		    tsLogger.arjLoggerI18N
			.warn(
                    "com.arjuna.ats.arjuna.coordinator.TransactionReaper_18",
                    new Object[]
                    { e._control.get_uid() , e.statusName() });
		}

		// if we have to synchronize on multiple objects we always
		// do so in a fixed order ReaperElement before Reaper and
		// ReaperElement before Reaper._cancelQueue in order to
		// ensure we don't deadlock. We never sychronize on the
		// reaper and the cancel queue at the same time.

		synchronized(e) {
		    switch (e._status)
		    {
		    case ReaperElement.RUN:
		    {
			// this tx has just timed out. remove it from the
			// TX list, update the timeout to take account of
			// cancellation period and reinsert as a cancelled
			// TX. this ensures we process it again if it does
			// not get cancelled in time

			e._status = ReaperElement.SCHEDULE_CANCEL;

			synchronized (this)
			{
			    _transactions.remove(e);

			    e.setAbsoluteTimeout((System.currentTimeMillis() + _cancelWaitPeriod));
			    _transactions.add(e);
			}

			if (tsLogger.arjLogger.debugAllowed())
			{
			    tsLogger.arjLogger
				.debug(
				    DebugLevel.FUNCTIONS,
				    VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION,
				    "Reaper scheduling TX for cancellation " + e._control.get_uid());
			}

			// insert into cancellation queue for a worker
			// thread to process and then make sure a worker
			// thread is awake

			synchronized (_workQueue)
			{
			    _workQueue.add(e);
			    _workQueue.notifyAll();
			}
		    }
		    break;
		    case ReaperElement.SCHEDULE_CANCEL:
		    {
			// hmm, a worker is taking its time to
			// start processing this scheduled entry.
			// we may just be running slow ... but the
			// worker may be wedged under a cancel for
			// some other TX. add an extra delay to
			// give the worker more time to complete
			// its current task and progress this
			// entry to the CANCEL state. if the
			// worker *is* wedged then this will
			// ensure the wedged TX entry comes to the
			// front of the queue.

			synchronized (this)
			{
			    _transactions.remove(e);

			    e.setAbsoluteTimeout((System.currentTimeMillis() + _cancelWaitPeriod));

			    _transactions.add(e);
			}

			if (tsLogger.arjLogger.debugAllowed())
			{
			    tsLogger.arjLogger
				.debug(
				    DebugLevel.FUNCTIONS,
				    VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION,
				    "Reaper deferring interrupt for TX scheduled for cancel " + e._control.get_uid());
			}
		    }
		    break;
		    case ReaperElement.CANCEL:
		    {
			// ok, the worker must be wedged under a
			// call to cancel() -- kick the thread and
			// reschedule the element for a later
			// check to ensure the thread responded to
			// the kick

			e._status = ReaperElement.CANCEL_INTERRUPTED;

			e._worker.interrupt();

			synchronized (this)
			{
			    _transactions.remove(e);

			    e.setAbsoluteTimeout((System.currentTimeMillis() + _cancelFailWaitPeriod));

			    _transactions.add(e);
			}

			// log that we interrupted cancel()

			if (tsLogger.arjLoggerI18N.isDebugEnabled())
			{
			    tsLogger.arjLoggerI18N
				.debug(
				    DebugLevel.FUNCTIONS,
				    VisibilityLevel.VIS_PUBLIC,
				    FacilityCode.FAC_ATOMIC_ACTION,
				    "com.arjuna.ats.arjuna.coordinator.TransactionReaper_4",
				    new Object[]{e._control.get_uid()});
			}
		    }
		    break;
		    case ReaperElement.CANCEL_INTERRUPTED:
		    {
			// cancellation got truly wedged -- mark
			// the element as a zombie so the worker
			// exits when (if?) it wakes up and create
			// a new worker thread to handle further
			// cancellations. then mark the
			// transaction as rollback only.

			e._status = ReaperElement.ZOMBIE;

			synchronized(this)
			{
			    _zombieCount++;

			    if (tsLogger.arjLogger.isDebugEnabled())
			    {
				tsLogger.arjLogger
				    .debug(
					DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					FacilityCode.FAC_ATOMIC_ACTION, "Reaper " + Thread.currentThread() + " got a zombie " + e._worker + " (zombie count now " + _zombieCount + ") cancelling "  + e._control.get_uid());
			    }

			    if (_zombieCount == _zombieMax)
			    {
				// log zombie overflow error call()

				if (tsLogger.arjLoggerI18N.isErrorEnabled())
				{
				    tsLogger.arjLoggerI18N
					.error(
					    "com.arjuna.ats.arjuna.coordinator.TransactionReaper_5",
					    new Object[]{new Integer(_zombieCount)});
				}
			    }
			}

			_reaperWorkerThread = new ReaperWorkerThread(TransactionReaper._theReaper);
			_reaperWorkerThread.setDaemon(true);

			_reaperWorkerThread.start();

			// log a failed cancel()

			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
			    tsLogger.arjLoggerI18N
				.warn(
				    "com.arjuna.ats.arjuna.coordinator.TransactionReaper_6",
				    new Object[]{e._worker,
						 e._control.get_uid()});
			}

			// ok, since the worker was wedged we need to
			// remove the entry from the timeouts and
			// transactions lists then mark this tx as
			// rollback only. we have to log a message
			// whether we succeed, fail or get interrupted

			synchronized(this)
			{
			    removeElement(e);
			}

			try
			{
			    if (e._control.preventCommit()) {

				// log a successful preventCommit()

				if (tsLogger.arjLoggerI18N.isWarnEnabled())
				{
				    tsLogger.arjLoggerI18N
					.warn(
					    "com.arjuna.ats.arjuna.coordinator.TransactionReaper_10",
					    new Object[]{e._control.get_uid()});
				}
				
				notifyListeners(e._control, false);
			    }
			    else
			    {
				// log a failed preventCommit()

				if (tsLogger.arjLoggerI18N.isWarnEnabled())
				{
				    tsLogger.arjLoggerI18N
					.warn(
					       "com.arjuna.ats.arjuna.coordinator.TransactionReaper_11",
					       new Object[]{e._control.get_uid()});
				}
			    }
			}
			catch(Exception e1)
			{
			    // log an exception under preventCommit()

			    if (tsLogger.arjLoggerI18N.isWarnEnabled()) {
				tsLogger.arjLoggerI18N
				    .warn(
					"com.arjuna.ats.arjuna.coordinator.TransactionReaper_12",
					new Object[]{e._control.get_uid()},
                    e1);
			    }
			}
		    }
		    break;
		    case ReaperElement.FAIL:
		    case ReaperElement.COMPLETE:
		    {
			// ok, the worker should remove the tx
			// from the transactions queue very soon
			// but we need to progress to the next
			// entry so we will steal in and do it
			// first

			synchronized(this)
			{
			    removeElement(e);
			}
		    }
		    break;

		    }
		}
	    } while(true) ;

	    return true;
	}

        public final void waitForCancellations()
        {
             synchronized(_workQueue)
             {
                  try
                  {
                       while (_workQueue.isEmpty())
                       {
                            _workQueue.wait();
                       }
                  }
                  catch (InterruptedException e)
                  {
                  }
             }
        }

	public final void doCancellations()
        {
	    for (;;)
	    {
		ReaperElement e;

		// see if we have any cancellations to process

		synchronized(_workQueue)
		{
		    try
		    {
			e = (ReaperElement)_workQueue.remove(0);
		    }
		    catch (IndexOutOfBoundsException ioobe) {break;}
		}


		// ok, current status must be SCHEDULE_CANCEL.
		// progress state to CANCEL and call cancel()


		if (tsLogger.arjLogger.debugAllowed())
		{
		    tsLogger.arjLogger
			.debug(
			    DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
			    FacilityCode.FAC_ATOMIC_ACTION, "Reaper Worker " + Thread.currentThread() + " attempting to cancel "  + e._control.get_uid());
		}

		boolean cancelled = false;
		Exception exception = null;

		synchronized(e)
		{
		    e._worker = Thread.currentThread();
		    e._status = ReaperElement.CANCEL;
		    e.notifyAll();
		}

		// we are now exposed to at most one interrupt from
		// the reaper. test for running and try the cancel if
		// required

		try
		{
		    if (e._control.running()) {

		        // try to cancel the transaction

		        if (e._control.cancel() == ActionStatus.ABORTED)
		        {
		            cancelled = true;

		            if (TxStats.enabled()) {
		                // note that we also count timeouts as application rollbacks via
		                // the stats unpdate in the TwoPhaseCoordinator cancel() method.
		                TxStats.getInstance().incrementTimeouts();
		            }

		            notifyListeners(e._control, true);
		        }
		    }
		}
		catch (Exception e1)
		{
		    exception = e1;
		}

		// ok, close the interrupt window by resetting the
		// state -- unless we have been told to go away by
		// being set to ZOMBIE

		synchronized (e)
		{
		    if (e._status == ReaperElement.ZOMBIE)
		    {
			// we need to decrement the zombie count and
			// force an immediate thread exit. the reaper
			// will have removed the entry from the
			// transactions list and started another
			// worker thread.

			ReaperWorkerThread worker = (ReaperWorkerThread)Thread.currentThread();
			worker.shutdown();

			synchronized(this)
			{
			    _zombieCount--;
			}

			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
			    tsLogger.arjLoggerI18N
				.warn(
				    "com.arjuna.ats.arjuna.coordinator.TransactionReaper_13",
				    new Object[]{Thread.currentThread(),
						 e._control.get_uid(),
						 new Integer(_zombieCount)});
			}

			// this gets us out of the for(;;) loop and
			// the shutdown call above makes sure we exit
			// after returning

			break;
		    }
		    else if (cancelled &&
			     e._status == ReaperElement.CANCEL_INTERRUPTED)
		    {
			// ok the call to cancel() returned true but
			// we cannot trust it because the reaper sent
			// the thread an interrupt

			cancelled = false;
			e._status = ReaperElement.FAIL;
			e.notifyAll();
		    }
		    else
		    {
			e._status = (cancelled
				     ? ReaperElement.COMPLETE
				     : ReaperElement.FAIL);
			e.notifyAll();
		    }
		}

		// log a message notifying success, failure or
		// exception during cancel(), remove the element from
		// the transactions queue and mark TX as rollback only

		if (cancelled)
		{
		    if (tsLogger.arjLoggerI18N.isWarnEnabled())
		    {
			tsLogger.arjLoggerI18N
			    .warn(
				"com.arjuna.ats.arjuna.coordinator.TransactionReaper_7",
				new Object[]{Thread.currentThread(),
					     e._control.get_uid()});
		    }
		}
		else if (e._control.running())
		{
		    if (exception != null)
		    {
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
			    tsLogger.arjLoggerI18N
				.warn(
				    "com.arjuna.ats.arjuna.coordinator.TransactionReaper_9",
				    new Object[]{Thread.currentThread(),
						 e._control.get_uid()},
                        exception);
			}
		    }
		    else
		    {
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
			    tsLogger.arjLoggerI18N
				.warn(
				    "com.arjuna.ats.arjuna.coordinator.TransactionReaper_8",
				    new Object[]{Thread.currentThread(),
						 e._control.get_uid()});
			}
		    }

		    try
		    {
			if (e._control.preventCommit()) {
			    // log a successful preventCommit()

			    if (tsLogger.arjLoggerI18N.isWarnEnabled())
			    {
				tsLogger.arjLoggerI18N
				    .warn(
					"com.arjuna.ats.arjuna.coordinator.TransactionReaper_14",
					new Object[]{Thread.currentThread(),
						     e._control.get_uid()});
			    }
			    
	                    notifyListeners(e._control, false);
			}
			else
			{
			    // log a failed preventCommit()

			    if (tsLogger.arjLoggerI18N.isWarnEnabled())
			    {
				tsLogger.arjLoggerI18N
				    .warn(
					"com.arjuna.ats.arjuna.coordinator.TransactionReaper_15",
					new Object[]{Thread.currentThread(),
						     e._control.get_uid()});
			    }
			}
		    }
		    catch(Exception e1)
		    {
			// log an exception under preventCommit()

			if (tsLogger.arjLoggerI18N.isWarnEnabled()) {
			    tsLogger.arjLoggerI18N
				.warn(
				    "com.arjuna.ats.arjuna.coordinator.TransactionReaper_16",
				    new Object[]{Thread.currentThread(),
						 e._control.get_uid()},
                    e1);
			}
		    }
		}

		synchronized(this)
		{
			removeElement(e);
		}

	    }
        }

	/**
	 * @return the number of items in the reaper's list.
	 * @since JTS 2.2.
	 */

	public final long numberOfTransactions()
	{
		return _transactions.size();
	}

        /**
         * Return the number of timeouts registered.
         * @return The number of timeouts registered.
         */
        public final long numberOfTimeouts()
        {
                return _timeouts.size();
        }

        public final void addListener (ReaperMonitor listener)
        {
            _listeners.add(listener);
        }
        
        public final boolean removeListener (ReaperMonitor listener)
        {
            return _listeners.remove(listener);
        }
        
	/**
	 * timeout is given in seconds, but we work in milliseconds.
	 */

	public final boolean insert(Reapable control, int timeout)
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION,
					"TransactionReaper::insert ( " + control + ", " + timeout
							+ " )");
		}

		/*
		 * Ignore if the timeout is zero, since this means the transaction
		 * should never timeout.
		 */

		if (timeout == 0)
			return true;

		/**
		 * Ignore if it's already in the list with a different timeout.
		 * (This should never happen)
		 */
		if (_timeouts.containsKey(control)) {
			return false; // remove this, rewrite put instead.
		}

		ReaperElement e = new ReaperElement(control, timeout);

		synchronized (this)
		{
			TransactionReaper._lifetime += timeout;

			_timeouts.put(control, e);
			boolean rtn = _transactions.add(e);

			if(_dynamic && _transactions.first() == e)
			{
				notifyAll(); // force recalc of next wakeup time, taking into account the newly inserted element
			}

			return rtn;
		}
	}

	public final boolean remove(java.lang.Object control)
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION,
					"TransactionReaper::remove ( " + control + " )");
		}

		if (control == null)
			return false;

		ReaperElement key;

                synchronized(this)
                {
                    key = (ReaperElement)_timeouts.remove(control);
                    if(key == null) {
                            return false;
                    }
		}

		// if a cancellation is in progress then we have to
		// see it through as we have to ensure that the worker
		// thread does not get wedged. so we have to tell the
		// control has gone away. in order to test the status
		// we need to synchronize on the element before we
		// synchronize on this so we can ensure that we don't
		// deadlock ourselves.

		synchronized(key)
		{
                    if (key._status != ReaperElement.RUN)
                    {
                         // we are cancelling this TX anyway and need
                         // to track the progress of the cancellation
                         // using this entry so we cnanot remove it

                         return false;
                    }

		    synchronized(this)
		    {
		        removeElement(key);

		        return true;
		    }
        }
	}

    /**
	 * Given the transaction instance, this will return the time left before the
	 * transaction is automatically rolled back if it has not been terminated.
	 *
	 * @param control
	 * @return the remaining time in milliseconds.
	 */

	public final long getRemainingTimeoutMills(Object control)
	{
        if ((_transactions.size() == 0) || (control == null))
        {
            if (tsLogger.arjLogger.debugAllowed())
            {
                tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS,
                        VisibilityLevel.VIS_PUBLIC,
                        FacilityCode.FAC_ATOMIC_ACTION,
                        "TransactionReaper::getRemainingTimeout for " + control
                                + " returning 0");
            }

            return 0;
        }

        final ReaperElement reaperElement = (ReaperElement)_timeouts.get(control);
        long timeout = 0;

        if (reaperElement == null)
        {
            timeout = 0;
        }
        else
        {
            // units are in milliseconds at this stage.
            timeout = reaperElement.getAbsoluteTimeout() - System.currentTimeMillis();
        }

        if (tsLogger.arjLoggerI18N.isDebugEnabled()) {
            tsLogger.arjLoggerI18N
                    .debug(
                            DebugLevel.FUNCTIONS,
                            VisibilityLevel.VIS_PUBLIC,
                            FacilityCode.FAC_ATOMIC_ACTION,
                            "com.arjuna.ats.arjuna.coordinator.TransactionReaper_17",
                            new Object[]
                                    { control, timeout });
        }

        return timeout;
	}

	/**
	 * Given a Control, return the associated timeout, or 0 if we do not know
	 * about it.
	 *
	 * Return in seconds!
	 */

	public final int getTimeout(Object control)
	{
		if ((_transactions.size() == 0) || (control == null))
		{
			if (tsLogger.arjLogger.debugAllowed())
			{
				tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS,
						VisibilityLevel.VIS_PUBLIC,
						FacilityCode.FAC_ATOMIC_ACTION,
						"TransactionReaper::getTimeout for " + control
								+ " returning 0");
			}

			return 0;
		}

		final ReaperElement reaperElement = (ReaperElement)_timeouts.get(control);

                final Integer timeout ;
		if(reaperElement == null) {
			timeout = new Integer(0);
		} else {
		        timeout = new Integer(reaperElement._timeout) ;
                }

		tsLogger.arjLoggerI18N
				.debug(
						DebugLevel.FUNCTIONS,
						VisibilityLevel.VIS_PUBLIC,
						FacilityCode.FAC_ATOMIC_ACTION,
						"com.arjuna.ats.arjuna.coordinator.TransactionReaper_3",
						new Object[]
								{ control, timeout });

		return timeout.intValue();
	}

	/*
         * Terminate the transaction reaper. This is a synchronous operation
         * and will only return once the reaper has been shutdown cleanly.
         *
         * Note, this method assumes that the transaction system has been
         * shutdown already so no new transactions can be created, or we
         * could be here for a long time!
         *
         * @param waitForTransactions if <code>true</code> then the reaper will
         * wait until all transactions have terminated (or been terminated by it).
         * If <code>false</code> then the reaper will call setRollbackOnly on all
         * the transactions.
         */

	private final void shutdown (boolean waitForTransactions)
	{
        // the reaper thread synchronizes and waits on this

	    synchronized (this)
	    {
	        _inShutdown = true;

	        /*
	         * If the caller does not want to wait for the normal transaction timeout
	         * periods to elapse before terminating, then we first start by enabling
	         * our time machine!
	         */

	        if (!waitForTransactions)
	        {
	            Iterator iter = _transactions.iterator();
	            ReaperElement e;

	            while (iter.hasNext())
	            {
	                e = (ReaperElement) iter.next();

	                e.setAbsoluteTimeout(0);
	            }
	        }

	        /*
	         * Wait for all of the transactions to
	         * terminate normally.
	         */

	        while (_transactions.size() > 0)
	        {
	            try
	            {
	                this.wait();
	            }
	            catch (final Exception ex)
	            {
	            }
	        }


            _reaperThread.shutdown();

            notifyAll();
        }
        try
        {
            _reaperThread.join();
        }
        catch (final Exception ex)
        {
        }

        _reaperThread = null;

        // the reaper worker thread synchronizes and wais on the work queue

        synchronized(_workQueue) {
            _reaperWorkerThread.shutdown();
            _workQueue.notifyAll();
            // hmm, not sure we really need to do this but . . .
            _reaperWorkerThread.interrupt();
        }

        try
        {
            _reaperWorkerThread.join();
        }
        catch (final Exception ex)
        {
        }

        _reaperWorkerThread = null;
	}

	/*
	 * Remove element from list and trigger waiter if we are
	 * being shutdown.
	 *
	 * n.b. must only be called when synchronized on this
	 */

	private final void removeElement (ReaperElement e)
	{
        _timeouts.remove(e._control);
        _transactions.remove(e);

        if (_inShutdown && (_transactions.size() == 0))
        {
            this.notifyAll();
        }
	}

	private final void notifyListeners (Reapable element, boolean rollback)
	{
	    // notify listeners. Ignore errors.

	    for (int i = 0; i < _listeners.size(); i++)
	    {
	        try
	        {
	            if (rollback)
	                _listeners.get(i).rolledBack(element.get_uid());
	            else
	                _listeners.get(i).markedRollbackOnly(element.get_uid());
	        }
	        catch (final Throwable ex)
	        {
	            // ignore
	        }
	    }
	}
	
	/**
	 * Currently we let the reaper thread run at same priority as other threads.
	 * Could get priority from environment.
	 */

	public static synchronized TransactionReaper create(long checkPeriod)
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION,
					"TransactionReaper::create ( " + checkPeriod + " )");
		}

		if (TransactionReaper._theReaper == null)
		{
            // default to dynamic mode
            TransactionReaper._dynamic = true;

            String mode =  arjPropertyManager.getCoordinatorEnvironmentBean().getTxReaperMode();

				if (mode.compareTo(TransactionReaper.PERIODIC) == 0) {
					TransactionReaper._dynamic = false;
                }

                if(mode.compareTo(TransactionReaper.NORMAL) == 0) {
                    TransactionReaper._dynamic = false;

                    if (tsLogger.arjLoggerI18N.isWarnEnabled())
			        {
				        tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.TransactionReaper_19");
    			    }
                }

            if (!TransactionReaper._dynamic)
			{
                checkPeriod = arjPropertyManager.getCoordinatorEnvironmentBean().getTxReaperTimeout();
			}
			else
				checkPeriod = Long.MAX_VALUE;

			TransactionReaper._theReaper = new TransactionReaper(checkPeriod);

            TransactionReaper._theReaper._cancelWaitPeriod = arjPropertyManager.getCoordinatorEnvironmentBean().getTxReaperCancelWaitPeriod();

            // must give TX at least 10 millisecs to
            // respond to cancel

            if (TransactionReaper._theReaper._cancelWaitPeriod < 10) {
                TransactionReaper._theReaper._cancelWaitPeriod = 10;
            }

            TransactionReaper._theReaper._cancelFailWaitPeriod = arjPropertyManager.getCoordinatorEnvironmentBean().getTxReaperCancelFailWaitPeriod();

            // must give TX at least 10 millisecs to
            // respond to cancel

            if (TransactionReaper._theReaper._cancelFailWaitPeriod < 10) {
                TransactionReaper._theReaper._cancelFailWaitPeriod = 10;
            }

            TransactionReaper._theReaper._zombieMax = arjPropertyManager.getCoordinatorEnvironmentBean().getTxReaperZombieMax();

            // we start bleating if the zombie count
            // reaches zombieMax so it has to be at
            // least 1

            if (TransactionReaper._theReaper._zombieMax <= 0) {
                TransactionReaper._theReaper._zombieMax = 1;
            }

			_reaperThread = new ReaperThread(TransactionReaper._theReaper);
			// _reaperThread.setPriority(Thread.MIN_PRIORITY);

			_reaperThread.setDaemon(true);

			_reaperWorkerThread = new ReaperWorkerThread(TransactionReaper._theReaper);
			_reaperWorkerThread.setDaemon(true);

			_reaperThread.start();

			_reaperWorkerThread.start();
		}

		return TransactionReaper._theReaper;
	}

	public static TransactionReaper create()
	{
		return create(TransactionReaper.defaultCheckPeriod);
	}

	public static TransactionReaper transactionReaper()
	{
		return transactionReaper(false);
	}

	/*
	 * If parameter is true then do a create.
	 */

	public static synchronized TransactionReaper transactionReaper(
			boolean createReaper)
	{
		if (createReaper)
			return create();
		else
			return _theReaper;
	}

	/**
	 * Terminate the transaction reaper. This is a synchronous operation
	 * and will only return once the reaper has been shutdown cleanly.
	 *
	 * Note, this method assumes that the transaction system has been
	 * shutdown already so no new transactions can be created, or we
	 * could be here for a long time!
	 *
	 * @param waitForTransactions if <code>true</code> then the reaper will
	 * wait until all transactions have terminated (or been terminated by it).
	 * If <code>false</code> then the reaper will call setRollbackOnly on all
	 * the transactions.
	 */

	public static synchronized void terminate (boolean waitForTransactions)
	{
	    if (_theReaper != null)
	    {
	        _theReaper.shutdown(waitForTransactions);
	        _theReaper = null;
	    }
	}

    public static boolean isDynamic() {
        return _dynamic;
    }

    /*
	 * Don't bother synchronizing as this is only an estimate anyway.
	 */

	public static final synchronized long transactionLifetime()
	{
		return TransactionReaper._lifetime;
	}

	public static final long defaultCheckPeriod = 120000; // in milliseconds
	public static final long defaultCancelWaitPeriod = 500; // in milliseconds
	public static final long defaultCancelFailWaitPeriod = 500; // in milliseconds
	public static final int defaultZombieMax = 8;

	static final void reset()
	{
		_theReaper = null;
	}

	private SortedSet _transactions = Collections.synchronizedSortedSet(new TreeSet()); // C of ReaperElement
	private Map _timeouts = Collections.synchronizedMap(new HashMap()); // key = Reapable, value = ReaperElement

	private List _workQueue = new LinkedList(); // C of ReaperElement
	
	private Vector<ReaperMonitor> _listeners = new Vector<ReaperMonitor>();

	private long _checkPeriod = 0;

        /**
	 * number of millisecs delay afer a cancel() is scheduled
	 * before the reaper tries to interrupt the worker thread
	 * executing the cancel()
	 */
	private long _cancelWaitPeriod = 0;

        /**
	 * number of millisecs delay afer a worker thread is
	 * interrupted before the reaper writes the it off as a zombie
	 * and starts a new thread
	 */
	private long _cancelFailWaitPeriod = 0;

        /**
	 * threshold for count of non-exited zombies at which system
	 * starts logging error messages
	 */
	private int _zombieMax = 0;

	private static TransactionReaper _theReaper = null;

	private static ReaperThread _reaperThread = null;

	private static ReaperWorkerThread _reaperWorkerThread = null;

	private static boolean _dynamic = true;

	private static long _lifetime = 0;

	private static int _zombieCount = 0;

	private boolean _inShutdown = false;
}
