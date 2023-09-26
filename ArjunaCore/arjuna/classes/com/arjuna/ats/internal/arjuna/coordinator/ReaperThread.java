/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.coordinator;

import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.logging.tsLogger;

/**
 * Class to record transactions with non-zero timeout values, and
 * class to implement a transaction reaper thread which terminates
 * these transactions once their timeout elapses.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ReaperThread.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class ReaperThread extends Thread
{

public ReaperThread (TransactionReaper arg)
    {
        super("Transaction Reaper");
	reaperObject = arg;
	sleepPeriod = reaperObject.checkingPeriod();
	_shutdown = false;
    }

public void run ()
    {
    	if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("ReaperThread.run ()");
        }

    	for (;;)
    	{
    	    /*
    	     * Cannot assume we sleep for the entire period. We may
    	     * be interrupted. If we are, just run a check anyway and
    	     * ignore.
    	     */
    
            synchronized(reaperObject)
            {
                // test our condition -- things may have changed while we were checking

                if (_shutdown) {
                    return;
                }

		sleepPeriod = reaperObject.checkingPeriod();
        
                if (sleepPeriod > 0)
                {
                     try
                     {
                          if (tsLogger.logger.isTraceEnabled()) {
                              tsLogger.logger.trace("Thread "+Thread.currentThread()+" sleeping for "+Long.toString(sleepPeriod));
                          }

                          reaperObject.wait(sleepPeriod);
                     }
                     catch (InterruptedException e1) {}

                    // test our condition -- things may have changed while we were waiting

                    if (_shutdown) {
                        return;
                    }
                }
            }
    
            if (tsLogger.logger.isTraceEnabled()) {
                tsLogger.logger.trace("ReaperThread.run ()");
            }

    	    reaperObject.check();
    	}
    }

    public void shutdown ()
    {
	_shutdown = true;
    }

    private TransactionReaper reaperObject;
    private long              sleepPeriod;
    private boolean           _shutdown;

    
}