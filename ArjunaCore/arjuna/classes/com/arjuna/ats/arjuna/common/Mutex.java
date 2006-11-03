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
 * Copyright (C) 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Mutex.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.common;

import java.lang.InterruptedException;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.common.util.logging.*;

/**
 * This is a reentrant Mutex implementation.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Mutex.java 2342 2006-03-30 13:06:17Z  $
 * @since 1.0.
 *
 * @message com.arjuna.ats.arjuna.common.Mutex_1 [com.arjuna.ats.arjuna.common.Mutex_1] - Mutex being destroyed with waiters.
 * @message com.arjuna.ats.arjuna.common.Mutex_2 [com.arjuna.ats.arjuna.common.Mutex_2] - Mutex.unlock - called by non-owning thread!
 * 
 */

public class Mutex
{

    public static final int LOCKED = 0;
    public static final int UNLOCKED = 1;
    public static final int WOULD_BLOCK = 2;
    public static final int ERROR = 3;

    public Mutex ()
    {
	//	this(true);
	this(false);
    }
    
    public Mutex (boolean reentrant)
    {
	_lock = new Object();
	_users = 0;
	_owner = null;
	_reentrant = reentrant;
	_recursiveCalls = 0;
    }
    
    public void finalize ()
    {
	if (_users != 0)
	{
	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.common.Mutex_1");
	}

	synchronized (_lock)
	{
	    _lock.notifyAll();
	}
    }

    /**
     * Classic mutex operations.
     */
    
    public int lock ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_GENERAL, "Mutex::lock()");
	}
	
	synchronized (_lock)
	{
	    if (_users == 0)
	    {
		_users = 1;
		    
		if (_reentrant)
		{
		    _owner = Thread.currentThread();
		    _recursiveCalls = 1;
		}
	    }
	    else
	    {
		boolean done = false;
		    
		if (_reentrant)
		{
		    if (_owner == Thread.currentThread())
		    {
			_recursiveCalls++;

			done = true;
		    }
		}

		if (!done)
		{
		    _users++;
		    
		    try
		    {
			_lock.wait();
		    }
		    catch (InterruptedException e)
		    {
		    }
		}
	    }
	}

	return Mutex.LOCKED;
    }
	    
    public int unlock ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_GENERAL, "Mutex::unlock()");
	}
	
	synchronized (_lock)
	{
	    if (_users <= 0)
	    {
		if (tsLogger.arjLoggerI18N.isWarnEnabled())
		{
		    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.common.Mutex_2");
		}
		
		return Mutex.LOCKED;
	    }
	    else
	    {
		boolean done = false;
		    
		if (_reentrant)
		{
		    if (_owner == Thread.currentThread())
		    {
			if (--_recursiveCalls == 0)
			    _owner = null;

			done = true;
		    }
		    else
		    {
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
			    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.common.Mutex_2");
			}
			
			return Mutex.LOCKED;
		    }
		}

		if (!done)
		{
		    if (--_users >= 0)
		    {
			_lock.notify();
		    }
		}
	    }
	}
	
	return Mutex.UNLOCKED;
    }
    
    public int tryLock ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS,
				     VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_GENERAL, "Mutex::tryLock()");
	}
	
	synchronized (_lock)
	{
	    if (_users == 0)
		return lock();
	    else
	    {
		if (_reentrant)
		{
		    if (_owner == Thread.currentThread())
			return lock();
		}
		
		return Mutex.WOULD_BLOCK;
	    }
	}
    }

    private Object  _lock;
    private int     _users;
    private Thread  _owner;
    private boolean _reentrant;
    private int     _recursiveCalls;
    
}









