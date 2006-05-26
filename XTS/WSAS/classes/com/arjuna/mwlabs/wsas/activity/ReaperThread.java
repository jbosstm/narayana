/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
	_sleepPeriod = _reaperObject.checkingPeriod();
	_shutdown = false;

	this.setDaemon(true);
    }
    
    public void run ()
    {
	for (;;)
	{
	    /*
	     * Cannot assume we sleep for the entire period. We may
	     * be interrupted. If we are, just run a check anyway and
	     * ignore.
	     */

	    boolean done = false;
	    
	    while (!done)
	    {
		_sleepPeriod = _reaperObject.checkingPeriod();

		long oldPeriod = _sleepPeriod;
		long beforeTime = System.currentTimeMillis();

		try
		{
		    Thread.sleep(_sleepPeriod);

		    done = true;
		}
		catch (InterruptedException e1)
		{
		    /*
		     * Has timeout been changed?
		     */

		    if (_reaperObject.checkingPeriod() != oldPeriod)
		    {
			done = true;
		    }
		    else
		    {
			long afterTime = System.currentTimeMillis();

			if (afterTime - beforeTime < _reaperObject.checkingPeriod())
			{
			    done = true;
			}
		    }
		}
		catch (Exception e2)
		{
		    done = true;
		}
	    }

	    if (_shutdown)
		return;

	    _reaperObject.check(System.currentTimeMillis());

	    if (_reaperObject.numberOfActivities() == 0)
	    {
		_sleepPeriod = Long.MAX_VALUE;
	    }
	}
    }

    public void shutdown ()
    {
	_shutdown = true;
    }

    private ActivityReaper _reaperObject;
    private long           _sleepPeriod;
    private boolean        _shutdown;

    
}

