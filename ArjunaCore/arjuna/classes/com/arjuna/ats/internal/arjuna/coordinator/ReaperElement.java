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
 * $Id: ReaperElement.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.coordinator;

import java.util.concurrent.atomic.AtomicInteger;

import com.arjuna.ats.arjuna.coordinator.Reapable;
import com.arjuna.ats.arjuna.logging.tsLogger;

public class ReaperElement implements Comparable<ReaperElement>
{

    /*
      * Currently, once created the reaper object and thread stay around forever.
      * We could destroy both once the list of transactions is null. Depends upon
      * the relative cost of recreating them over keeping them around.
      */

    public ReaperElement(Reapable control, int cancelIntervalSeconds) {
        this(control, cancelIntervalSeconds, Long.MAX_VALUE);
    }

    public ReaperElement(Reapable control, int cancelIntervalSeconds, long traceGracePeriodMills)
	{
		if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("ReaperElement::ReaperElement ( " + control + ", "
                    + cancelIntervalSeconds + " )");
        }

		_control = control;
		_timeout = cancelIntervalSeconds;
		_status = RUN;
        _worker = null;

        long now = System.currentTimeMillis();

		/*
		 * Given a timeout period in seconds, calculate its absolute value from
		 * the current time of day in milliseconds.
		 */
        _transactionTimeoutAbsoluteMillis = (cancelIntervalSeconds * 1000L) + now;
        _nextCheckAbsoluteMillis = _transactionTimeoutAbsoluteMillis;

        // if stack tracing will kick in before timeout, the wakeup time is that instead
        if(traceGracePeriodMills < cancelIntervalSeconds*1000L) {
            _nextCheckAbsoluteMillis = traceGracePeriodMills + now;
        }

        // add additional variation to distinguish instances created in the same millisecond.
        _bias = getBiasCounter();

	}
	
	public String toString ()
	{
	    return "ReaperElement < "+_control+", "+_timeout+", "+statusName()+", "+_worker+" >";
	}

	/**
	 * Order by absoluteTimeout first, then by Uid.
	 * This is required so that the set maintained by the TransactionReaper
	 * is in timeout order for efficient processing.
	 *
	 * @param other the ReaperElement to compare
	 * @return 0 if equal, 1 if this is greater, -1 if this is smaller
	 */
	public int compareTo(ReaperElement other)
	{
        if(this == other) {
            return 0;
        }

        if(_nextCheckAbsoluteMillis == other._nextCheckAbsoluteMillis) {
            if (_bias == other._bias) {
                if(_control.get_uid().equals(other._control.get_uid())) {
                    return 0;
                } else if (_control.get_uid().greaterThan(other._control.get_uid())) {
                    return 1;
                } else {
                    return -1;
                }
            } else {
                return (_bias > other._bias) ? 1 : -1;
            }
        } else {
            return (_nextCheckAbsoluteMillis > other._nextCheckAbsoluteMillis) ? 1 : -1;
        }
	}

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReaperElement that = (ReaperElement) o;

        // could be made more efficient, but this is easier to maintain consistently with compareTo.
        // note that the comparison includes _absoluteTimeoutMills which is mutable, so weird things
        // may happen if trying to locate the obj in a collection whilst concurrently mutating it.
        // fortunately such comparisons are generally done with the obj identity shortcut anyhow.
        return (compareTo(that) == 0);
    }

    @Override
    public int hashCode()
    {
        return _control.get_uid().hashCode();
    }

    public final Reapable _control;

	private long _nextCheckAbsoluteMillis;
	private long _transactionTimeoutAbsoluteMillis;
    private final int _bias;

    // bias is used to distinguish/sort instances with the same _absoluteTimeoutMills
    // as using Uid for this purpose is expensive. JBTM-611

    private static final int MAX_BIAS = 1000000;
    private static final AtomicInteger biasCounter = new AtomicInteger();

    private static int getBiasCounter()
    {
        int value = 0;
        do {
            value = biasCounter.getAndIncrement();
            if(value == MAX_BIAS) {
                biasCounter.set(0);
            }
        } while(value >= MAX_BIAS);

        return value; // range 0 to MAX_BIAS-1 inclusive.
    }

    public int _timeout;

        /*
         * status field to track the progress of the reaper worker which is
         * attempting to cancel the associated TX. this is necessary to ensure
         * that the the reaper only interrupts the worker in the windows where
         * it may be exposed to being wedged by client code and is sure to be
         * able to catch and handle an interrupt without dying. all accesses
         * to this field must be synchronized on the containing element.
         *
         * this field is always initialised to RUN, indicating that
         * the element is associated with a running transaction. the
         * following transitions can occur, performed either by the
         * reaper (R) or the worker (W)
         *
         * RUN --> TRACE (R)
         * TRACE --> RUN (W)
         *
         * the element may loop between RUN and TRACE zero or more times in its life
         * before going into SCHEDULE_CANCEL. It's assumed that capturing the traces
         * is potentially expensive but that it won't become wedged as cancellation
         * can, so it doesn't lead to other states in the same way as cancellation.
         *
         * RUN --> SCHEDULE_CANCEL (R)
         *
         * SCHEDULE_CANCEL --> CANCEL (W)
         *
         * CANCEL --> COMPLETE (W)
         *
         * CANCEL --> FAIL (W)
         *
         * CANCEL --> CANCEL_INTERRUPTED (R)
         *
         * CANCEL_INTERRUPTED --> COMPLETE (W)
         *
         * CANCEL_INTERRUPTED --> FAIL (W)
         *
         * CANCEL_INTERRUPTED --> ZOMBIE (R)
         */

        public int _status;

        /*
         * the reaper worker which is attempting to cancel the associated TX
         */

        public Thread _worker;

        /*
         * status values for progressing reaper element from default
         * RUN status through stages of cancellation from
         * SCHEDULE_CANCEL to ZOMBIE. the reaper thread can only
         * interrupt a reaper worker thread if it is wedged while
         * the element is in state CANCEL.
         */

        /*
         * status of a reaper element for a TX which has not yet timed out
         */

        public static final int RUN = 0;

        /*
         * status of a reaper element which has been queued for
         * cancellation by a reaper worker
         */

        public static final int SCHEDULE_CANCEL = 1;

        /*
         * status of a reaper element while the reaper worker is under
         * a call  to the TX cancel operation  and, hence, potentially
         * exposed to  being wedged. the reaper may safely interrupt
         * the worker when it is in this state
         */

        public static final int CANCEL = 2;


        /*
         * status of a reaper element if the reaper thread has
         * interrupted the worker during the CANCEL state. if the
         * reaper discovers the thread is still in this state
         * following a suitable delay then the thread is seriously
         * wedged (possibly on a non-interruptible i/o) and requires
         * notice of termination (by resetting the state to ZOMBIE)
         * and replacement by a new worker thread.
         */

        public static final int CANCEL_INTERRUPTED = 3;

        /*
         * status of a reaper element if the reaper worker has been
         * unable to cancel the TX and has failed to mark it as
         * rollback only. it is safe for the reaper to remove the
         * element from the transactions list if it is in this state
         * (modulo synchronization) although the worker should do so
         * with minimal delay.
         */

        public static final int FAIL = 4;

        /*
         * status of a reaper element if the reaper worker has been
         * able to cancel the tx or has marked it as rollback only. it
         * is safe for the reaper to remove the element from the
         * transactions list if it is in this state (modulo
         * synchronization) although the worker should do so with
         * minimal delay.
         */

        public static final int COMPLETE = 5;

        /*
         * status of a reaper element if the worker got so wedged it
         * failed to respond to an interrupt either during
         * cancellation or marking as rollback only. if the worker
         * wakes up and finds the element in this state then it must
         * exit. the reaper will have ensured that the failure to
         * cancel and rollback the transaction has been logged and
         * will have removed the element from the transactions list.
         */

        public static final int ZOMBIE = 6;

        /**
         * status of a reaper element  which has been queued for
         * stack trace capture by a reaper worker.
         */
        public static final int TRACE = 7;

        /*
         * convenience method to provide printable string for current status
         * for use in debugging/logging. should only be called while
         * synchronized.
         */

         public final String statusName()
         {
              switch (_status)
              {
              case RUN:
                   return "RUN";
              case SCHEDULE_CANCEL:
                   return "SCHEDULE_CANCEL";
              case CANCEL:
                   return "CANCEL";
              case CANCEL_INTERRUPTED:
                   return "CANCEL_INTERRUPTED";
              case FAIL:
                   return "FAIL";
              case COMPLETE:
                   return "COMPLETE";
              case ZOMBIE:
                   return "ZOMBIE";
              case TRACE:
                  return "TRACE";
              default:
                   return "UNKNOWN";
              }
         }

    /**
     * Returns the absolute time of the next status check, in milliseconds
     * @return The absolute wakeup time, in millis
     */
    public long getNextCheckAbsoluteMillis()
    {
        return _nextCheckAbsoluteMillis;
    }

    /**
     * Sets the absolute time of the next status check (i.e. wakeup) for this element.
     *
     * @param nextCheckAbsoluteMillis value in milliseconds
     */
    public void setNextCheckAbsoluteMillis(long nextCheckAbsoluteMillis)
    {
        this._nextCheckAbsoluteMillis = nextCheckAbsoluteMillis;
    }

    /**
     * Returns the absolute time of the transaction expiry, in milliseconds
     * @return The absolute timeout time, in millis
     */
    public long getTransactionTimeoutAbsoluteMillis() {
        return _transactionTimeoutAbsoluteMillis;
    }
}
