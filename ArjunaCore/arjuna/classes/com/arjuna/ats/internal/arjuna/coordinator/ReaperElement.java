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

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.coordinator.Reapable;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import java.util.concurrent.atomic.AtomicInteger;

public class ReaperElement implements Comparable<ReaperElement>
{

    /*
      * Currently, once created the reaper object and thread stay around forever.
      * We could destroy both once the list of transactions is null. Depends upon
      * the relative cost of recreating them over keeping them around.
      */

	public ReaperElement(Reapable control, int timeout)
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS,
					VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION,
					"ReaperElement::ReaperElement ( " + control + ", "
							+ timeout + " )");
		}

		_control = control;
		_timeout = timeout;
		_status = RUN;
        _worker = null;

		/*
		 * Given a timeout period in seconds, calculate its absolute value from
		 * the current time of day in milliseconds.
		 */

		_absoluteTimeoutMills = (timeout * 1000) + System.currentTimeMillis();

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

        if(_absoluteTimeoutMills == other._absoluteTimeoutMills) {
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
            return (_absoluteTimeoutMills > other._absoluteTimeoutMills) ? 1 : -1;
        }
	}

	public Reapable _control;

	private long _absoluteTimeoutMills;
    private int _bias;

    // bias is used to distinguish/sort instances with the same _absoluteTimeoutMills
    // as using Uid for this purpose is expensive. JBTM-611

    private static int MAX_BIAS = 1000000;
    private static AtomicInteger biasCounter = new AtomicInteger();

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
              default:
                   return "UNKNOWN";
              }
         }

    /**
     * Returns absolute timeout in milliseconds
     * @return
     */
    public long getAbsoluteTimeout()
    {
        return _absoluteTimeoutMills;
    }

    /**
     * Sets the absolute timeout.
     *
     * @param absoluteTimeout value in milliseconds
     */
    public void setAbsoluteTimeout(long absoluteTimeout)
    {
        this._absoluteTimeoutMills = absoluteTimeout;
    }
}
