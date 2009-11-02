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
 * (C) 2006-2007,
 * @author JBoss Inc.
 */

package com.arjuna.ats.internal.arjuna.coordinator;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.ats.arjuna.coordinator.TransactionReaper;

import com.arjuna.common.util.logging.*;

/**
 * Class to reap timed out transactions on behalf of the transaction reaper
 * thread which is dispatched to terminate a series of transactions when their
 * timeout elapses.
 *
 * @author Andrew Dinn (adinn@redhat.com) 2007-07-08
 */

public class ReaperWorkerThread extends Thread
{

    public ReaperWorkerThread (TransactionReaper arg)
    {
        _theReaper = arg;
        _shutdown = false;
    }

    /**
     * @message com.arjuna.ats.internal.arjuna.coordinator.ReaperWorkerThread_1 [com.arjuna.ats.internal.arjuna.coordinator.ReaperWorkThread_1] - Thread {0} waiting for cancelled TXs
     * @message com.arjuna.ats.internal.arjuna.coordinator.ReaperWorkerThread_2 [com.arjuna.ats.internal.arjuna.coordinator.ReaperWorkThread_2] - Thread {0} performing cancellations
     */

public void run ()
    {
         if (tsLogger.arjLogger.isDebugEnabled())
         {
              tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                                       FacilityCode.FAC_ATOMIC_ACTION, "ReaperWorkerThread.run ()");
         }

        for (;;)
    	{
             // wait for the reaper thread to queue some TXs for
             // this thread to cancel

             if (tsLogger.arjLoggerI18N.isDebugEnabled())
             {
                  tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                                               FacilityCode.FAC_ATOMIC_ACTION,
                                               "com.arjuna.ats.internal.arjuna.coordinator.ReaperWorkerThread_1",
                                               new Object[]{Thread.currentThread()});
             }

             _theReaper.waitForCancellations();

             // check for shutdown before we wait again

             if (_shutdown)
                  return;

             // get the reaper to cancel any TXs queued for cancellation.

             if (tsLogger.arjLoggerI18N.isDebugEnabled())
             {
                  tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                                               FacilityCode.FAC_ATOMIC_ACTION,
                                               "com.arjuna.ats.internal.arjuna.coordinator.ReaperWorkerThread_2",
                                               new Object[]{Thread.currentThread()});
             }

             _theReaper.doCancellations();

            // check for shutdown before we wait again

            if (_shutdown)
    	        return;
        }
    }

    public void shutdown ()
    {
	_shutdown = true;
    }

    private TransactionReaper _theReaper;
    private boolean           _shutdown;

}
