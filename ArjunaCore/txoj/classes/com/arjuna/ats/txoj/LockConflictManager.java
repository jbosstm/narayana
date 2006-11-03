/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: LockConflictManager.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.txoj;

import java.util.Calendar;
import java.util.Date;

import java.lang.InterruptedException;

/**
 * An instance of this class is used to determine what to do in the
 * event of a lock conflict for a given object. If the timeout and
 * retry values are >=0 then we use them to sleep the thread which tried
 * to get the lock. If the retry value is -100 (LockManager.waitTotalTimeout)
 * then the thread will block for up to the total timeout and be signalled
 * either when the timeout occurs, or when the lock is actually released.
 */

class LockConflictManager
{

LockConflictManager ()
    {
	_lock = new Object();
	_signals = 0;
    }

    /**
     * Wait for the specified timeout and retry. We may either sleep the
     * thread, or block it on a mutex.
     *
     * Returns the time taken to wait.
     */

int wait (int retry, int waitTime)
    {
	/*
	 * If the retry is -1 then we wait on the object as if it
	 * were a lock. Otherwise we do the usual sleep call.
	 */

	if (retry < 0)
	{
	    /*
	     * Wait for the lock object to be signalled.
	     */

	    Date d1 = Calendar.getInstance().getTime();
	    
	    synchronized (_lock)
		{
		    try
		    {
			/*
			 * Consume an old signal. May cause us to go round
			 * the loop quicker than we should, but its better
			 * than synchronizing signal and wait.
			 */

			if (_signals == 0)
			{
			    _lock.wait(waitTime);
			}
			else
			{
			    _signals--;

			    return waitTime;
			}
		    }
		    catch (InterruptedException e)
		    {
		    }
		}

	    Date d2 = Calendar.getInstance().getTime();

	    return (int) (d2.getTime() - d1.getTime());
	}
	else
	{
	    try
	    {
		/* hope things happen in time */
	    
		Thread.sleep(waitTime);
	    }
	    catch (InterruptedException e)
	    {
	    }

	    return 0;
	}
    }    

    /**
     * Signal that the lock has been released.
     */
    
void signal ()
    {
	synchronized (_lock)
	    {
		_lock.notifyAll();

		_signals++;

		if (_signals < 0)  // check for overflow
		    _signals = 1;
	    }
    }
    
private Object _lock;
private int    _signals;
    
}
