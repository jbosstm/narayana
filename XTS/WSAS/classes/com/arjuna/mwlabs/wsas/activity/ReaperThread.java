/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mwlabs.wsas.activity;

/**
 * Class to record transactions with non-zero timeout values, and
 * class to implement a transaction reaper thread which terminates
 * these transactions once their timeout elapses.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ReaperThread.java,v 1.1 2002/11/25 10:51:46 nmcl Exp $
 * @since 1.0.
 */

public class ReaperThread extends Thread
{

    public ReaperThread (ActivityReaper arg)
    {
        super("XTS Activity Reaper");
	_reaperObject = arg;
	_sleepPeriod = 0;
	_shutdown = false;

	this.setDaemon(true);
    }
    
    public void run ()
    {
        for (;;)
        {
            synchronized(this) {
                // see if we need to stop checking
                if (_shutdown) {
                    return;
                }

                _sleepPeriod = _reaperObject.sleepPeriod();
                if (_sleepPeriod > 0) {
                    try {
                        wait(_sleepPeriod);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                } else if (_sleepPeriod == 0) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
                // we might have let go of the lock so see once again if we need to stop checking
                if (_shutdown) {
                    return;
                }
            }

            // see if we have any work to do

            _reaperObject.check(System.currentTimeMillis());
        }
    }

    public synchronized void shutdown ()
    {
        _shutdown = true;
        notify();
    }

    private ActivityReaper _reaperObject;
    private long           _sleepPeriod;
    private boolean        _shutdown;

    
}