/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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
                 tsLogger.logger.trace("Thread "+Thread.currentThread()+" waiting for transaction check tasks");
             }

             _theReaper.waitForWork();

             // check for shutdown before we wait again

             if (_shutdown)
                  return;

             // get the reaper to cancel any TXs queued for cancellation.

             if (tsLogger.logger.isTraceEnabled()) {
                 tsLogger.logger.trace("Thread "+Thread.currentThread()+" performing transaction check work");
             }

             _theReaper.doWork();

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