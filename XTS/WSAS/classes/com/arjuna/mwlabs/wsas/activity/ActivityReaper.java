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
 * $Id: ActivityReaper.java,v 1.6 2005/05/19 12:13:18 nmcl Exp $
 */

package com.arjuna.mwlabs.wsas.activity;

import com.arjuna.mw.wsas.logging.wsasLogger;

import com.arjuna.mw.wsas.UserActivityFactory;
import com.arjuna.mw.wsas.status.*;

import com.arjuna.mw.wsas.completionstatus.*;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;

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

    public final long sleepPeriod ()
    {
        synchronized (_list) {
            if (_list.isEmpty()) {
                // sleep until a wakeup is notified
                return 0;
            } else {
                long currentTime = System.currentTimeMillis();
                long firstTimeout = _list.first()._absoluteTimeout;
                if (currentTime >= firstTimeout) {
                    // don't sleep just start work now
                    return -1;
                } else {
                    // sleep for required number of milliseconds
                    return (firstTimeout - currentTime);
                }
            }
        }
    }

    /*
     * Should be no need to protect with a mutex since only one thread
     * is ever doing the work.
     */

    /**
     * Only check for one at a time to prevent starvation.
     *
     * Timeout is given in millisecon>ds.
     *
     * @message com.arjuna.mwlabs.wsas.activity.ActivityReaper_1 [com.arjuna.mwlabs.wsas.activity.ActivityReaper_1] - ActivityReaper: could not terminate.
     */

    public final boolean check (long timeout)
    {
        ReaperElement element = null;

        synchronized(_list) {
            if (_list.isEmpty()) {
                return false;
            }

            element = _list.first();
            if (timeout < element._absoluteTimeout) {
                return false;
            }
        }

	    /*
	     * Only force rollback if the activity is still running.
	     */

	    Status status = com.arjuna.mw.wsas.status.Unknown.instance();

	    try
	    {
		status = element._activity.status();
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

			UserActivityFactory.userActivity().resume(new ActivityHierarchyImple(element._activity));
			UserActivityFactory.userActivity().end(Failure.instance());
			UserActivityFactory.userActivity().suspend();
		    }
		    catch (Exception ex)
		    {
			problem = true;
		    }

		if (problem)
		{
		    boolean error = false;

		    try
		    {
			element._activity.setCompletionStatus(FailureOnly.instance());
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

        synchronized (_list) {
            _list.remove(element);
        }

	return true;
    }

    /**
     * @return the number of items in the reaper's list.
     */

    public final long numberOfActivities ()
    {
        synchronized(_list) {
            return _list.size();
        }
    }

    /**
     * timeout is given in seconds, but we work in milliseconds.
     */

    public final boolean insert (ActivityImple activity, int timeout)
    {
	/*
	 * Ignore if the timeout is zero, since this means the activity
	 * should never timeout.
	 */

	if (timeout == 0)
	    return true;

	ReaperElement e = new ReaperElement(activity, timeout);

        synchronized (_list) {
            _list.add(e);
        }

        // notify the reaper thread that the list has changed
        synchronized (_reaperThread) {
            _reaperThread.notify();
        }

        return true;
    }

    public final boolean remove (ActivityImple act)
    {
        if (act == null) {
            return false;
        }

        boolean found = false;
        synchronized (_list) {
            Iterator<ReaperElement> iter = _list.iterator();
            ReaperElement e = null;
            while (iter.hasNext() && !found) {
                e = iter.next();
                if (e._activity.equals(act)) {
                    _list.remove(e);
                    found = true;
                }
            }
        }

        if (found) {
            // notify the reaper thread that the list has changed
            synchronized (_reaperThread) {
                _reaperThread.notify();
            }
        }
        return false;
    }

    /**
     * Currently we let the reaper thread run at same priority as other
     * threads. Could get priority from environment.
     */

    public static synchronized ActivityReaper create ()
    {
        // TODO -- problem here because nothing calls shutdown

	if (_theReaper == null)
	{
	    ActivityReaper._theReaper = new ActivityReaper();

	    _reaperThread = new ReaperThread(ActivityReaper._theReaper);
	    //	    _reaperThread.setPriority(Thread.MIN_PRIORITY);

	    _reaperThread.setDaemon(true);

	    _reaperThread.start();
	}

	return _theReaper;
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

    /**
     * make sure the reaper thread exits
     */
    public static synchronized void shutdown()
    {
        if (_theReaper != null) {
            _reaperThread.shutdown();
        }
    }

    ActivityReaper ()
    {
        _list = new TreeSet<ReaperElement>();
    }

    static final void reset ()
    {
	_theReaper = null;
    }

    private SortedSet<ReaperElement> _list;

    private static ActivityReaper _theReaper = null;
    private static ReaperThread   _reaperThread = null;

}



