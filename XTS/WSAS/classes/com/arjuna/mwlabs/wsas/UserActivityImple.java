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
 * $Id: UserActivityImple.java,v 1.4 2005/03/10 15:37:00 nmcl Exp $
 */

package com.arjuna.mwlabs.wsas;

import com.arjuna.mw.wsas.logging.wsasLogger;

import com.arjuna.mw.wsas.UserActivity;

import com.arjuna.mw.wsas.common.GlobalId;

import com.arjuna.mw.wsas.activity.Outcome;
import com.arjuna.mw.wsas.activity.HLS;
import com.arjuna.mw.wsas.activity.ActivityHierarchy;

import com.arjuna.mwlabs.wsas.activity.ActivityHierarchyImple;
import com.arjuna.mwlabs.wsas.activity.ActivityHandleImple;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;
import com.arjuna.mw.wsas.completionstatus.FailureOnly;

import com.arjuna.mw.wsas.status.NoActivity;

import com.arjuna.mwlabs.wsas.activity.ActivityImple;
import com.arjuna.mwlabs.wsas.activity.HLSManager;

import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.InvalidActivityException;
import com.arjuna.mw.wsas.exceptions.InvalidTimeoutException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;
import com.arjuna.mw.wsas.exceptions.NoActivityException;
import com.arjuna.mw.wsas.exceptions.NoPermissionException;
import com.arjuna.mw.wsas.exceptions.ActiveChildException;

import java.util.Stack;

import java.util.EmptyStackException;

/**
 * The user portion of the activity API. An implementation of this interface
 * presents each thread with the capability to create and manage activities.
 * It is very similar to the OTS Current and JTA UserTransaction.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: UserActivityImple.java,v 1.4 2005/03/10 15:37:00 nmcl Exp $
 * @since 1.0.
 */

public class UserActivityImple implements UserActivity
{
    /**
     * Start a new activity. If there is already an activity associated
     * with the thread then it will be nested. An implementation specific
     * timeout will be associated with the activity (which may be no
     * timeout).
     *
     * @exception WrongStateException Thrown if the any currently associated
     * activity is in a state that does not allow a new activity to be
     * enlisted.
     * @exception SystemException Thrown in any other situation.
     */

    public void start () throws WrongStateException, SystemException
    {
	try
	{
	    start(getTimeout());
	}
	catch (InvalidTimeoutException ex)
	{
	}
    }

    /**
     * Start a new activity. If there is already an activity associated
     * with the thread then it will be nested.
     *
     * @param int timeout The timeout associated with the activity. If the
     * activity has not been terminated by the time this period elapses, then
     * it will automatically be terminated.
     * @exception WrongStateException Thrown if the currently associated
     * activity is in a state that does not allow a new activity to be
     * enlisted as a child.
     * @exception InvalidTimeoutException Thrown if the specified timeout is
     * invalid within the current working environment.
     * @exception SystemException Thrown in any other situation.
     *
     * @message com.arjuna.mwlabs.wsas.UserActivityImple_1 [com.arjuna.mwlabs.wsas.UserActivityImple_1] - Activity.start caught: 
     */

    public void start (int timeout) throws WrongStateException, InvalidTimeoutException, SystemException
    {
	if (timeout < 0)
	    throw new InvalidTimeoutException();
	else
	{
	    if (timeout == 0)
		timeout = getTimeout();
	}

	ActivityImple currentActivity = new ActivityImple(current());

	currentActivity.start(timeout);

	push(currentActivity);

	HLS[] hls = HLSManager.allHighLevelServices();

	if (hls != null)
	{
	    for (int i = 0; i < hls.length; i++)
	    {
		try
		{
		    hls[i].begun();
		}
		catch (SystemException ex)
		{
		    try
		    {
			setCompletionStatus(FailureOnly.instance());
		    }
		    catch (Exception e)
		    {
			wsasLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wsas.UserActivityImple_1",
						      new Object[]{e});
		    }

		    throw ex;
		}
	    }
	}
    }
    
    /**
     * Complete the activity with the current completion status.
     *
     * @exception InvalidActivityException Thrown if the current activity is
     * invalid in the execution environment.
     * @exception ActiveChildException Thrown if the current activity is a
     * parent activity with active children.
     * @exception WrongStateException Thrown if the current activity is not in a
     * state that allows it to be completed.
     * @exception ProtocolViolationException Thrown if a violation of the
     * activity service or HLS protocol occurs.
     * @exception NoActivityException Thrown if there is no activity
     * associated with the invoking thread.
     * @exception NoPermissionException Thrown if the invoking thread does
     * not have permission to terminate the transaction.
     * @exception SystemException Thrown if some other error occurred.
     *
     * @return the result of completing the activity. Null is valid and must
     * be interpreted within the context of any HLS that may exist.
     *
     * @see com.arjuna.mw.wsas.Outcome
     *
     * @message com.arjuna.mwlabs.wsas.UserActivityImple_2 [com.arjuna.mwlabs.wsas.UserActivityImple_2] - currentActivity.end threw: 
     * @message com.arjuna.mwlabs.wsas.UserActivityImple_3 [com.arjuna.mwlabs.wsas.UserActivityImple_3] - Activity.completed caught: 
     */

    public Outcome end () throws InvalidActivityException, WrongStateException, ProtocolViolationException, SystemException, NoActivityException, SystemException, NoPermissionException, ActiveChildException
    {
	ActivityImple currentActivity = current();

	if (currentActivity == null)
	    throw new NoActivityException();

	/*
	if (currentActivity.parent() != null)
	    throw new ActiveChildException();
	*/

	Outcome res = null;
	
	try
	{
	    res = currentActivity.end();
	}
	catch (Exception ex)
	{
	    wsasLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wsas.UserActivityImple_1",
					  new Object[]{ex});
	}

	HLS[] hls = HLSManager.allHighLevelServices();

	if (hls != null)
	{
	    for (int i = 0; i < hls.length; i++)
	    {
		try
		{
		    hls[i].completed();
		}
		catch (SystemException ex)
		{
		    wsasLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wsas.UserActivityImple_3",
						  new Object[]{ex});
		}
	    }
	}

	pop();
	
	return res;
    }

    /**
     * Complete the activity with the completion status provided.
     *
     * @exception InvalidActivityException Thrown if the current activity is
     * invalid in the execution environment.
     * @exception ActiveChildException Thrown if the current activity is a
     * parent activity with active children.
     * @exception WrongStateException Thrown if the current activity is not in a
     * state that allows it to be completed, or is incompatible with the
     * completion status provided.
     * @exception ProtocolViolationException Thrown if the a violation of the
     * activity service or HLS protocol occurs.
     * @exception NoActivityException Thrown if there is no activity
     * associated with the invoking thread.
     * @exception NoPermissionException Thrown if the invoking thread does
     * not have permission to terminate the transaction.
     * @exception SystemException Thrown if some other error occurred.
     *
     * @param CompletionStatus cs The CompletionStatus to use.
     *
     * @return the result of completing the activity. Null is valid and must
     * be interpreted within the context of any HLS that may exist.
     *
     * @see com.arjuna.mw.wsas.Outcome
     */

    public Outcome end (com.arjuna.mw.wsas.completionstatus.CompletionStatus cs) throws InvalidActivityException, WrongStateException, ProtocolViolationException, SystemException, NoActivityException, NoPermissionException, ActiveChildException
    {
	ActivityImple currentActivity = current();

	if (currentActivity == null)
	    throw new NoActivityException();

	/*
	if (currentActivity.parent() != null)
	    throw new ActiveChildException();
	*/

	Outcome res = null;
	
	try
	{
	    res = currentActivity.end(cs);
	}
	catch (Exception ex)
	{
	    wsasLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wsas.UserActivityImple_2",
					  new Object[]{ex});
	}

	HLS[] hls = HLSManager.allHighLevelServices();

	if (hls != null)
	{
	    for (int i = 0; i < hls.length; i++)
	    {
		try
		{
		    hls[i].completed();
		}
		catch (SystemException ex)
		{
		    wsasLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wsas.UserActivityImple_3",
						  new Object[]{ex});
		}
	    }
	}

	pop();
	
	return res;
    }

    /**
     * Set the termination status for the current activity, if any.
     *
     * @param CompletionStatus endStatus The state in which the activity
     * should attempt to terminate. This may
     * be one of the default values provided by WSAS or may be extended in
     * an implementation specific manner by an HLS.
     *
     * @exception NoActivityException Thrown if there is no activity
     * associated with the invoking thread.
     * @exception WrongStateException Thrown if the completion status is
     * incompatible with the current state of the activity.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void setCompletionStatus (CompletionStatus endStatus) throws NoActivityException, WrongStateException, SystemException
    {
	ActivityImple curr = current();
	
	if (curr == null)
	    throw new NoActivityException();
	
	curr.setCompletionStatus(endStatus);
    }

    /**
     * Get the completion status currently associated with the activity.
     *
     * @exception NoActivityException Thrown if there is no activity
     * associated with the current thread.
     * @exception SystemException Thrown if any other error occurs.
     *
     * @return the termination status for the current activity, if any.
     */

    public CompletionStatus getCompletionStatus () throws NoActivityException, SystemException
    {
	ActivityImple curr = current();
	
	if (curr == null)
	    throw new NoActivityException();
	else
	    return curr.getCompletionStatus();
    }

    /**
     * Get the timeout value currently associated with activities.
     *
     * @exception SystemException Thrown if any error occurs.
     *
     * @return the timeout value in seconds, or 0 if no application specified
     * timeout has been provided.
     */

    public int getTimeout () throws SystemException
    {
	Integer t = (Integer) _timeouts.get();
	int val = _defaultTimeout;
	
	if (t != null)
	    val = t.intValue();
	
	return val;
    }

    /**
     * Set the timeout to be associated with all subsequently created
     * activities. A default value of 0 is automatically associated with
     * each thread and this means that no application specified timeout is
     * set for activities.
     *
     * @param int timeout The timeout (in seconds) to associate with all
     * subsequently created activities. This value must be 0 or greater.
     *
     * @exception InvalidTimeoutException Thrown if the timeout value provided
     * is negative, too large, or if timeouts are simply not supported by
     * the activity implementation.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void setTimeout (int timeout) throws InvalidTimeoutException, SystemException
    {
	if (timeout < 0)
	    throw new InvalidTimeoutException();
	
	_timeouts.set(new Integer(timeout));
    }
    
    /**
     * @exception SystemException Thrown if any error occurs.
     *
     * @return the status of the current activity. If there is no
     * activity associated with the thread then NoActivity
     * will be returned.
     *
     * @see com.arjuna.mw.wsas.status.Status
     */

    public com.arjuna.mw.wsas.status.Status status () throws SystemException
    {
	ActivityImple curr = current();
	
	if (curr == null)
	    return NoActivity.instance();
	else
	    return curr.status();
    }

    /**
     * What is the name of the current activity? Use only for
     * debugging purposes!
     *
     * @exception NoActivityException Thrown if there is no activity
     * associated with the invoking thread.
     * @exception SystemException Thrown if any other error occurs.
     *
     * @return the name of the activity.
     */

    public String activityName () throws NoActivityException, SystemException
    {
	ActivityImple curr = current();
	
	if (curr == null)
	    throw new NoActivityException();  // or return null?
	else
	    return curr.activityName();
    }

    public GlobalId activityId () throws NoActivityException, SystemException
    {
	ActivityImple curr = current();
	
	if (curr == null)
	    throw new NoActivityException();  // or return null?
	else
	    return curr.getGlobalId();
    }
	
    /**
     * Suspend the current activity from this thread and return the token
     * representing the context, if any, or null otherwise. Once called, the
     * thread will have no activities associated with it.
     *
     * @exception SystemException Thrown if any error occurs.
     *
     * @return the token representing the current context, if any, or null
     * otherwise.
     *
     * @message com.arjuna.mwlabs.wsas.UserActivityImple_4 [com.arjuna.mwlabs.wsas.UserActivityImple_4] - Activity.suspended caught: 
     */

    public ActivityHierarchy suspend () throws SystemException
    {
	HLS[] hls = HLSManager.allHighLevelServices();

	if (hls != null)
	{
	    for (int i = 0; i < hls.length; i++)
	    {
		try
		{
		    hls[i].suspended();
		}
		catch (SystemException ex)
		{
		    wsasLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wsas.UserActivityImple_4",
						  new Object[]{ex});
		}
	    }
	}

	ActivityImple currentActivity = purge();
	
	if (currentActivity != null)
	    return new ActivityHierarchyImple(currentActivity);
	else
	    return null;
    }

    /**
     * Given a token representing a context, associate it with the current
     * thread of control. This will implicitly disassociate the thread from any
     * activities that it may already be associated with. If the parameter is
     * null then the thread is associated with no activity.
     *
     * @param ActivityHierarchy tx The activity to associate with this thread. This
     * may be null in which case the current thread becomes associated with
     * no activity.
     *
     * @exception InvalidActivityException Thrown if the activity handle
     * is invalid in this context.
     * @exception SystemException Thrown if any other error occurs.
     *
     * @message com.arjuna.mwlabs.wsas.UserActivityImple_5 [com.arjuna.mwlabs.wsas.UserActivityImple_5] - Activity.resumed caught: 
     * @message com.arjuna.mwlabs.wsas.UserActivityImple_51 [com.arjuna.mwlabs.wsas.UserActivityImple_51] - Unknown activity implementation!
     */

    public void resume (ActivityHierarchy tx) throws InvalidActivityException, SystemException
    {
	if (tx == null)
	{
	    purge();
	}
	else
	{
	    if (tx instanceof ActivityHierarchyImple)
	    {
		try
		{
		    for (int i = 0; i < tx.size(); i++)
		    {
			ActivityHandleImple handle = (ActivityHandleImple) tx.activity(i);
			
			push(handle.getActivity());
		    }
		}
		catch (Exception ex)
		{
		    ex.printStackTrace();
		    
		    purge();
		}
	    }
	    else
		throw new InvalidActivityException(wsasLogger.log_mesg.getString("com.arjuna.mwlabs.wsas.UserActivityImple_51"));
	}

	HLS[] hls = HLSManager.allHighLevelServices();

	if (hls != null)
	{
	    for (int i = 0; i < hls.length; i++)
	    {
		try
		{
		    hls[i].resumed();
		}
		catch (SystemException ex)
		{
		    wsasLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wsas.UserActivityImple_5",
						  new Object[]{ex});
		}
	    }
	}
    }

    public ActivityHierarchy currentActivity () throws SystemException
    {
	ActivityImple curr = current();
	
	if (curr != null)
	    return new ActivityHierarchyImple(curr);
	else
	    return null;
    }

    public final ActivityImple current ()
    {
	Stack hier = (Stack) _threadAxData.get();
	ActivityImple currentActivity = null;

	if (hier != null)
	{
	    try
	    {
		currentActivity = (ActivityImple) hier.peek();
	    }
	    catch (EmptyStackException ex)
	    {
	    }
	}

	return currentActivity;
    }
    
    private final void push (ActivityImple currentActivity)
    {
	Stack hier = (Stack) _threadAxData.get();

	if (hier == null)
	{
	    hier = new Stack();

	    _threadAxData.set(hier);
	}

	hier.push(currentActivity);
    }
    
    private final ActivityImple pop ()
    {
	Stack hier = (Stack) _threadAxData.get();
	ActivityImple currentActivity = null;
	
	if (hier != null)
	{
	    try
	    {
		currentActivity = (ActivityImple) hier.pop();

		if (hier.size() == 0)
		    _threadAxData.set(null);
	    }
	    catch (EmptyStackException ex)
	    {
	    }
	}

	return currentActivity;
    }

    private final ActivityImple purge ()
    {
	Stack hier = (Stack) _threadAxData.get();
	ActivityImple currentActivity = null;
	
	if (hier != null)
	{
	    _threadAxData.set(null) ;
	    try
	    {
		currentActivity = (ActivityImple) hier.peek();
	    }
	    catch (EmptyStackException ex)
	    {
	    }
	}

	return currentActivity;
    }
    
    private static ThreadLocal _threadAxData = new ThreadLocal() ;
    private static ThreadLocal _timeouts = new ThreadLocal() ;

    private static final int _defaultTimeout = 0;
    
}
