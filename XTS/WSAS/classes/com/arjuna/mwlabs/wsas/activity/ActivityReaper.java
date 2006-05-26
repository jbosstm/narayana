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
 * $Id: ActivityReaper.java,v 1.6 2005/05/19 12:13:18 nmcl Exp $
 */

package com.arjuna.mwlabs.wsas.activity;

import com.arjuna.mw.wsas.logging.wsasLogger;

import com.arjuna.mw.wsas.UserActivityFactory;
import com.arjuna.mw.wsas.status.*;

import com.arjuna.mw.wsas.completionstatus.*;

import com.arjuna.mw.wsas.common.Environment;

import com.arjuna.ats.internal.arjuna.template.OrderedList;
import com.arjuna.ats.internal.arjuna.template.OrderedListIterator;

/**
 * Class to record activities with non-zero timeout values, and
 * class to implement a activity reaper thread which terminates
 * these activities once their timeout elapses.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ActivityReaper.java,v 1.6 2005/05/19 12:13:18 nmcl Exp $
 * @since 1.0.
 */

public class ActivityReaper
{

    public static final String NORMAL = "NORMAL";
    public static final String DYNAMIC = "DYNAMIC";

    public final long checkingPeriod ()
    {
	return _checkPeriod;
    }

    /*
     * Should be no need to protect with a mutex since only one thread
     * is ever doing the work.
     */

    /**
     * Only check for one at a time to prevent starvation.
     *
     * Timeout is given in milliseconds.
     *
     * @message com.arjuna.mwlabs.wsas.activity.ActivityReaper_1 [com.arjuna.mwlabs.wsas.activity.ActivityReaper_1] - ActivityReaper: could not terminate.
     */
    
    public final boolean check (long timeout)
    {
	if (_list == null)
	    return true;
	
	OrderedListIterator iter = new OrderedListIterator(_list);
	ReaperElement e = null;

	while ((e = (ReaperElement) iter.iterate()) != null)
	{
	    if (timeout >= e._absoluteTimeout)
		break;
	    else
	    {
		iter = null;
		return true;
	    }
	}

	iter = null;
	
	if (e != null)
	{
	    /*
	     * Only force rollback if the activity is still running.
	     */

	    Status status = com.arjuna.mw.wsas.status.Unknown.instance();
	    
	    try
	    {
		status = e._activity.status();
	    }
	    catch (Exception ex)
	    {
	    }
	    
	    if (status instanceof Active)
	    {
		/*
		 * If this is a local activity, then we can roll it
		 * back completely. Otherwise, just mark it as rollback only.
		 */

		boolean problem = false;
		
		try
		{
		    try
		    {
			/*
			 * TODO
			 *
			 * Everything works on thread-to-activity association
			 * so we can't just tell an activity to end: we have
			 * to resume it and then tell it to end. The reason
			 * is that all HLS-es assume that the invoking thread
			 * has the current context on it.
			 */

			//			e._activity.end(Failure.instance());

			UserActivityFactory.userActivity().resume(new ActivityHierarchyImple(e._activity));
			UserActivityFactory.userActivity().end(Failure.instance());
			UserActivityFactory.userActivity().suspend();
		    }
		    catch (Exception ex)
		    {
			problem = true;
		    }
		}
		catch (Exception ex2)
		{
		    problem = true;
		}
		
		if (problem)
		{
		    boolean error = false;
		     
		    try
		    {
			e._activity.setCompletionStatus(FailureOnly.instance());
		    }
		    catch (Exception ex3)
		    {
			error = true;
		    }
		     
		    if (error)
		    {
			wsasLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wsas.activity.ActivityReaper_1");
		    }
		}
	    }
	    
	    _list.remove(e);
	}
	
	System.gc();  // do some garbage collection while we're at it!
	
	return true;
    }
    
    /**
     * @return the number of items in the reaper's list.
     */

    public final synchronized long numberOfActivities ()
    {
	return ((_list == null) ? 0 : _list.size());
    }
    
    /**
     * timeout is given in seconds, but we work in milliseconds.
     */
 
    public final synchronized boolean insert (ActivityImple activity, int timeout)
    {
	/*
	 * Ignore if the timeout is zero, since this means the activity
	 * should never timeout.
	 */

	if (timeout == 0)
	    return true;
    
	ActivityReaper._lifetime += timeout;
	
	/*
	 * If the timeout for this activity is less than the
	 * current timeout for the reaper thread (or one is not set for
	 * the reaper thread) then use that timeout and interrupt the thread
	 * to get it to recheck.
	 */

	if ((timeout < _checkPeriod) || (_checkPeriod == Long.MAX_VALUE))
	{
	    _checkPeriod = timeout*1000;  // convert to milliseconds!
	    ActivityReaper._reaperThread.interrupt();
	}
	
	ReaperElement e = new ReaperElement(activity, timeout);

	if ((_list != null) && _list.insert(e))
	    return true;
	else
	{
	    e = null;
	    return false;
	}
    }

    public final synchronized boolean remove (ActivityImple act)
    {
	if ((_list == null) || (act == null))
	    return false;
    
	ReaperElement e = null;
	OrderedListIterator iter = new OrderedListIterator(_list);
	boolean result = false;
	boolean found = false;
	
	while (((e = (ReaperElement) iter.iterate()) != null) && !found)
	{
	    try
	    {
		found = e._activity.equals(act);
	    }
	    catch (Exception e2)
	    {
		break;
	    }
	}

	iter = null;

	if (found)
	{
	    result = _list.remove(e);

	    e = null;
	}

	return result;
    }

    /**
     * Currently we let the reaper thread run at same priority as other
     * threads. Could get priority from environment.
     */

    public static synchronized ActivityReaper create (long checkPeriod)
    {
	if (ActivityReaper._theReaper == null)
	{
	    String mode = System.getProperty(Environment.REAPER_MODE);
	    
	    if (mode != null)
	    {
		if (mode.compareTo(ActivityReaper.DYNAMIC) == 0)
		    ActivityReaper._dynamic = true;
	    }
	    
	    if (!ActivityReaper._dynamic)
	    {
		String timeoutEnv = System.getProperty(Environment.REAPER_TIMEOUT);

		if (timeoutEnv != null)
		{
		    Long l = null;
		
		    try
		    {
			l = new Long(timeoutEnv);
			checkPeriod = l.longValue();
			
			l = null;
		    }
		    catch (NumberFormatException e)
		    {
			e.printStackTrace();
		    }
		}
	    }
	    else
		checkPeriod = Long.MAX_VALUE;
		
	    ActivityReaper._theReaper = new ActivityReaper(checkPeriod);
	    
	    _reaperThread = new ReaperThread(ActivityReaper._theReaper);
	    //	    _reaperThread.setPriority(Thread.MIN_PRIORITY);

	    _reaperThread.setDaemon(true);
	    
	    _reaperThread.start();
	}

	return ActivityReaper._theReaper;
    }

    public static synchronized ActivityReaper create ()
    {
	return create(ActivityReaper.defaultCheckPeriod);
    }
    
    public static synchronized ActivityReaper activityReaper ()
    {
	return activityReaper(false);
    }

    /*
     * If parameter is true then do a create.
     */

    public static synchronized ActivityReaper activityReaper (boolean createReaper)
    {
	if (createReaper)
	    return create();
	else
	    return _theReaper;
    }
    
    /*
     * Don't bother synchronizing as this is only an estimate anyway.
     */

    public static final long activityLifetime ()
    {
	return ActivityReaper._lifetime;
    }

    public static final long defaultCheckPeriod = 120000;  // in milliseconds

    ActivityReaper (long checkPeriod)
    {
	_list = new OrderedList();
	_checkPeriod = checkPeriod;

	if (_list == null)
	{
	    throw new OutOfMemoryError();
	}
    }

    static final void reset ()
    {
	_theReaper = null;
    }
    
    private OrderedList _list;
    private long        _checkPeriod;
    
    private static ActivityReaper _theReaper = null;
    private static ReaperThread   _reaperThread = null;
    private static boolean        _dynamic = false;
    private static long           _lifetime = 0;
 
}



