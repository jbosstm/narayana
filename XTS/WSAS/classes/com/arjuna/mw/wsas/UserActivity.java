/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
 * $Id: UserActivity.java,v 1.2 2005/05/19 12:13:15 nmcl Exp $
 */

package com.arjuna.mw.wsas;

import com.arjuna.mw.wsas.common.GlobalId;

import com.arjuna.mw.wsas.activity.Outcome;
import com.arjuna.mw.wsas.activity.ActivityHierarchy;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;

import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.InvalidActivityException;
import com.arjuna.mw.wsas.exceptions.InvalidTimeoutException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;
import com.arjuna.mw.wsas.exceptions.NoActivityException;
import com.arjuna.mw.wsas.exceptions.NoPermissionException;
import com.arjuna.mw.wsas.exceptions.ActiveChildException;

/**
 * The user portion of the activity API. An implementation of this interface
 * presents each thread with the capability to create and manage activities.
 * It is very similar to the OTS Current and JTA UserTransaction.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: UserActivity.java,v 1.2 2005/05/19 12:13:15 nmcl Exp $
 * @since 1.0.
 */

public interface UserActivity
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

    public void start () throws WrongStateException, SystemException;

    /**
     * Start a new activity. If there is already an activity associated
     * with the thread then it will be nested. If the activity is still
     * active when the specified timeout elapses, it will be terminated.
     *
     * @param int timeout The timeout associated with the activity (in
     * seconds). If the activity has not been terminated by the time this
     * period elapses, then it will automatically be terminated.
     * @exception WrongStateException Thrown if the currently associated
     * activity is in a state that does not allow a new activity to be
     * enlisted as a child.
     * @exception InvalidTimeoutException Thrown if the specified timeout is
     * invalid within the current working environment.
     * @exception SystemException Thrown in any other situation.
     */

    public void start (int timeout) throws WrongStateException, InvalidTimeoutException, SystemException;

    /**
     * Complete the activity with the current completion status.
     *
     * @exception InvalidActivityException Thrown if the current activity is
     * invalid in the execution environment.
     * @exception ActiveChildException Thrown if the current activity is a
     * parent activity with active children.
     * @exception WrongStateException Thrown if the current activity is not in a
     * state that allows it to be completed in the status requested.
     * @exception ProtocolViolationException Thrown if the a violation of the
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
     * @see com.arjuna.mw.wsas.activity.Outcome
     */

    public Outcome end () throws InvalidActivityException, WrongStateException, ProtocolViolationException, NoActivityException, ActiveChildException, SystemException, NoPermissionException;

    /**
     * Complete the activity with the completion status provided.
     *
     * @exception InvalidActivityException Thrown if the current activity is
     * invalid in the execution environment.
     * @exception ActiveChildException Thrown if the current activity is a
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
     * @see com.arjuna.mw.wsas.activity.Outcome
     */

    public Outcome end (com.arjuna.mw.wsas.completionstatus.CompletionStatus cs) throws InvalidActivityException, WrongStateException, ProtocolViolationException, SystemException, ActiveChildException, NoActivityException, NoPermissionException;

    /**
     * Set the termination status for the current activity, if any.
     *
     * @param CompletionStatus endStatus The state in which the activity
     * should attempt to terminate. This may be one of the default values
     * provided by WSAS or may be extended in an implementation specific
     * manner by an HLS.
     *
     * @exception NoActivityException Thrown if there is no activity
     * associated with the invoking thread.
     * @exception WrongStateException Thrown if the completion status is
     * incompatible with the current state of the activity.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void setCompletionStatus (CompletionStatus endStatus) throws NoActivityException, WrongStateException, SystemException;

    /**
     * Get the completion status currently associated with the activity.
     *
     * @exception NoActivityException Thrown if there is no activity
     * associated with the current thread.
     * @exception SystemException Thrown if any other error occurs.
     *
     * @return the termination status for the current activity, if any.
     */

    public CompletionStatus getCompletionStatus () throws NoActivityException, SystemException;

    /**
     * Get the timeout value currently associated with activities.
     *
     * @exception SystemException Thrown if any error occurs.
     *
     * @return the timeout value in seconds, or 0 if no application specified
     * timeout has been provided.
     */

    public int getTimeout () throws SystemException;

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

    public void setTimeout (int timeout) throws InvalidTimeoutException, SystemException;
    
    /**
     * @exception SystemException Thrown if any error occurs.
     *
     * @return the status of the current activity. If there is no
     * activity associated with the thread then NoActivity
     * will be returned.
     *
     * @see com.arjuna.mw.wsas.status.Status
     */

    public com.arjuna.mw.wsas.status.Status status () throws SystemException;

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

    public String activityName () throws NoActivityException, SystemException;

    /**
     * @return the unique activity id.
     *
     * @exception NoActivityException Thrown if there is no activity
     * associated with the invoking thread.
     * @exception SystemException Thrown if any other error occurs.
     */

    public GlobalId activityId () throws NoActivityException, SystemException;
    
    /**
     * Suspend the current activity from this thread and return the token
     * representing the context, if any, or null otherwise. Once called, the
     * thread will have no activities associated with it.
     *
     * @exception SystemException Thrown if any error occurs.
     *
     * @return the token representing the current context, if any, or null
     * otherwise.
     */

    public ActivityHierarchy suspend () throws SystemException;

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
     */

    public void resume (ActivityHierarchy tx) throws InvalidActivityException, SystemException;

    /**
     * @return the token representing the current activity context hierarchy,
     * or null if there is none associated with the invoking thread.
     *
     * @exception SystemException Thrown if any error occurs.
     */

    public ActivityHierarchy currentActivity () throws SystemException;

}

