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

import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.logging.tsLogger;

/**
 * Class to reap timed out transactions on behalf of the transaction reaper
 * thread which is dispatched to terminate a series of transactions when their
 * timeout elapses.
 *
 * @author Andrew Dinn (adinn@redhat.com) 2007-07-08
 */

public class ReaperWorkerThread extends Thread
{
    /**
     * counter used to number reaper worker threads
     */
    private static int counter = 0;

    public ReaperWorkerThread (TransactionReaper arg)
    {
        // no need for synchronization when doing the increment here as worker threads are not created in parallel.
        super("Transaction Reaper Worker " + counter++);
        _theReaper = arg;
        _shutdown = false;
    }

public void run ()
    {
         if (tsLogger.logger.isTraceEnabled()) {
             tsLogger.logger.trace("ReaperWorkerThread.run ()");
         }

        for (;;)
    	{
             // wait for the reaper thread to queue some TXs for
             // this thread to cancel

             if (tsLogger.logger.isTraceEnabled()) {
                 tsLogger.logger.trace("Thread "+Thread.currentThread()+" waiting for cancelled TXs");
             }

             _theReaper.waitForCancellations();

             // check for shutdown before we wait again

             if (_shutdown)
                  return;

             // get the reaper to cancel any TXs queued for cancellation.

             if (tsLogger.logger.isTraceEnabled()) {
                 tsLogger.logger.trace("Thread "+Thread.currentThread()+" performing cancellations");
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
