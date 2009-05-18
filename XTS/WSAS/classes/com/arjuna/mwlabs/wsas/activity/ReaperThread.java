/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ReaperThread.java,v 1.1 2002/11/25 10:51:46 nmcl Exp $
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
    }

    private ActivityReaper _reaperObject;
    private long           _sleepPeriod;
    private boolean        _shutdown;

    
}

