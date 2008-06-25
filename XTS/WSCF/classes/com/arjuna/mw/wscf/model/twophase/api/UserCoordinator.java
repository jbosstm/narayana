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
 * $Id: UserCoordinator.java,v 1.2 2005/05/19 12:13:25 nmcl Exp $
 */

package com.arjuna.mw.wscf.model.twophase.api;

import com.arjuna.mw.wscf.common.CoordinatorId;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;

import com.arjuna.mw.wscf.exceptions.*;

import com.arjuna.mw.wscf.model.twophase.exceptions.*;

import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.InvalidTimeoutException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;
import com.arjuna.mw.wsas.exceptions.NoActivityException;
import com.arjuna.mw.wsas.exceptions.NoPermissionException;
import com.arjuna.mw.wsas.exceptions.InvalidActivityException;

/**
 * The user portion of the coordinator API. An implementation of this interface
 * presents each thread with the capability to create and manage coordinators.
 * It is very similar to the OTS Current and JTA UserTransaction.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: UserCoordinator.java,v 1.2 2005/05/19 12:13:25 nmcl Exp $
 * @since 1.0.
 */

public interface UserCoordinator
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

    public void begin () throws WrongStateException, SystemException;

    /**
     * Start a new activity. If there is already an activity associated
     * with the thread then it will be nested. If the activity is still
     * active when the specified timeout elapses, it will be terminated.
     *
     * @param timeout The timeout associated with the activity (in
     * seconds). If the activity has not been terminated by the time this
     * period elapses, then it will automatically be terminated.
     * @exception WrongStateException Thrown if the currently associated
     * activity is in a state that does not allow a new activity to be
     * enlisted as a child.
     * @exception InvalidTimeoutException Thrown if the specified timeout is
     * invalid within the current working environment.
     * @exception SystemException Thrown in any other situation.
     */

    public void begin (int timeout) throws WrongStateException, InvalidTimeoutException, SystemException;

    /**
     * Confirm the current activity.
     *
     * @exception InvalidActivityException Thrown if the current activity is
     * invalid in the execution environment.
     * @exception WrongStateException Thrown if the current activity is not in a
     * state that allows it to be completed in the status requested.
     * @exception ProtocolViolationException Thrown if the a violation of the
     * activity service or HLS protocol occurs.
     * @exception NoPermissionException Thrown if the invoking thread does
     * not have permission to terminate the transaction.
     * @exception SystemException Thrown if some other error occurred.
     */

    public void confirm () throws InvalidActivityException, WrongStateException, ProtocolViolationException, NoCoordinatorException, CoordinatorCancelledException, HeuristicMixedException, HeuristicHazardException, NoPermissionException, SystemException;

    /**
     * Cancel the activity.
     *
     * @exception InvalidActivityException Thrown if the current activity is
     * invalid in the execution environment.
     * @exception WrongStateException Thrown if the current activity is not in a
     * state that allows it to be completed, or is incompatible with the
     * completion status provided.
     * @exception ProtocolViolationException Thrown if the a violation of the
     * activity service or HLS protocol occurs.
     * @exception NoPermissionException Thrown if the invoking thread does
     * not have permission to terminate the transaction.
     * @exception SystemException Thrown if some other error occurred.
     *
     * @see com.arjuna.mw.wsas.activity.Outcome
     */

    public void cancel () throws InvalidActivityException, WrongStateException, ProtocolViolationException, NoCoordinatorException, CoordinatorConfirmedException, HeuristicMixedException, HeuristicHazardException, NoPermissionException, SystemException;

    /**
     * Set the termination status for the current activity to cancel only.
     *
     * @exception WrongStateException Thrown if the completion status is
     * incompatible with the current state of the activity.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void setCancelOnly () throws NoCoordinatorException, WrongStateException, SystemException;

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
     * @param timeout The timeout (in seconds) to associate with all
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
     * @exception NoActivityException Thrown if there is no activity
     * associated with the invoking thread.
     * @exception SystemException Thrown if some other error occurred.
     *
     * @return the unique coordinator id for the current coordinator. This
     * may or may not be the same as the activity id.
     */

    public CoordinatorId identifier () throws NoActivityException, SystemException;

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
     * @param tx The activity to associate with this thread. This
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

