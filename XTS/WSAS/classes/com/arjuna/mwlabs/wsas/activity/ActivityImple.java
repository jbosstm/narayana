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
 * $Id: ActivityImple.java,v 1.6 2005/05/19 12:13:18 nmcl Exp $
 */

package com.arjuna.mwlabs.wsas.activity;

import com.arjuna.mw.wsas.logging.wsasLogger;

import com.arjuna.ats.internal.arjuna.template.OrderedList;
import com.arjuna.ats.internal.arjuna.template.OrderedListIterator;

import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mw.wsas.common.GlobalId;

// TODO: obtain via configuration

import com.arjuna.mwlabs.wsas.common.arjunacore.GlobalIdImple;

import com.arjuna.mwlabs.wsas.util.HLSWrapper;

import com.arjuna.mw.wsas.status.Created;
import com.arjuna.mw.wsas.status.Active;
import com.arjuna.mw.wsas.status.Completing;
import com.arjuna.mw.wsas.status.Completed;

import com.arjuna.mw.wsas.completionstatus.Failure;
import com.arjuna.mw.wsas.completionstatus.FailureOnly;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;

import com.arjuna.mw.wsas.status.Status;

import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.InvalidActivityException;
import com.arjuna.mw.wsas.exceptions.InvalidTimeoutException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;
import com.arjuna.mw.wsas.exceptions.NoActivityException;
import com.arjuna.mw.wsas.exceptions.NoPermissionException;
import com.arjuna.mw.wsas.exceptions.HLSException;

import java.util.Hashtable;
import java.util.Stack;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * The Activity.
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ActivityImple.java,v 1.6 2005/05/19 12:13:18 nmcl Exp $
 * @since 1.0.
 */

public class ActivityImple
{

	public ActivityImple ()
	{
		this(null);
	}

	public ActivityImple (ActivityImple parent)
	{
		_parent = parent;
		_children = new Hashtable();
		_status = Created.instance();
		_completionStatus = Failure.instance();
		_activityId = new GlobalIdImple();
		_timeout = 0;
		_result = null;
	}

	/**
	 * Start a new activity.
	 * 
	 * @param int
	 *            timeout The timeout associated with the activity. If the
	 *            activity has not been terminated by the time this period
	 *            elapses, then it will automatically be terminated.
	 * @exception WrongStateException
	 *                Thrown if the currently associated activity is in a state
	 *                that does not allow a new activity to be enlisted as a
	 *                child.
	 * @exception InvalidTimeoutException
	 *                Thrown if the specified timeout is invalid within the
	 *                current working environment.
	 * @exception SystemException
	 *                Thrown in any other situation.
	 */

	public void start () throws WrongStateException, SystemException
	{
		try
		{
			start(0);
		}
		catch (InvalidTimeoutException ex)
		{
		}
	}

	/**
	 * Start a new activity. If there is already an activity associated with the
	 * thread then it will be nested.
	 * 
	 * @param int
	 *            timeout The timeout associated with the activity. If the
	 *            activity has not been terminated by the time this period
	 *            elapses, then it will automatically be terminated.
	 * @exception WrongStateException
	 *                Thrown if the currently associated activity is in a state
	 *                that does not allow a new activity to be enlisted as a
	 *                child.
	 * @exception InvalidTimeoutException
	 *                Thrown if the specified timeout is invalid within the
	 *                current working environment.
	 * @exception SystemException
	 *                Thrown in any other situation.
	 * 
	 * @message com.arjuna.mwlabs.wsas.activity.ActivityImple_1
	 *          [com.arjuna.mwlabs.wsas.activity.ActivityImple_1] - State
	 *          incompatible to start activity:
	 */

	public void start (int timeout) throws WrongStateException,
			InvalidTimeoutException, SystemException
	{
		if (timeout < 0)
			throw new InvalidTimeoutException();

		synchronized (this)
		{
			_timeout = timeout;

			if (_status.equals(Created.instance()))
			{
				try
				{
					if (_parent != null)
						_parent.addChild(this);
				}
				catch (InvalidActivityException ex)
				{
					_status = Completed.instance();

					throw new WrongStateException(ex.toString());
				}

				if (_timeout > 0)
				{
					if (!ActivityReaper.activityReaper(true).insert(this, _timeout))
					{
						setCompletionStatus(FailureOnly.instance());
					}
				}

				_status = Active.instance();
			}
			else
				throw new WrongStateException(
						wsasLogger.log_mesg.getString("com.arjuna.mwlabs.wsas.activity.ActivityImple_1")
								+ " " + this + " " + _status);
		}
	}

	/**
	 * Complete the activity with the current completion status.
	 * 
	 * @exception InvalidActivityException
	 *                Thrown if the current activity is not known about by the
	 *                activity system.
	 * @exception WrongStateException
	 *                Thrown if the current activity is not in a state that
	 *                allows it to be completed.
	 * @exception ProtocolViolationException
	 *                Thrown if the a violation of the activity service or HLS
	 *                protocol occurs.
	 * @exception NoPermissionException
	 *                Thrown if the invoking thread does not have permission to
	 *                terminate the transaction.
	 * @exception SystemException
	 *                Thrown if some other error occurred.
	 * 
	 * @return the result of completing the activity. Null is valid and must be
	 *         interpreted within the context of any HLS that may exist.
	 * 
	 * @see com.arjuna.mw.wsas.Outcome
	 */

	public Outcome end () throws InvalidActivityException, WrongStateException,
			ProtocolViolationException, NoPermissionException, SystemException
	{
		return end(_completionStatus);
	}

	/**
	 * Complete the activity with the completion status provided.
	 * 
	 * @exception InvalidActivityException
	 *                Thrown if the current activity is not known about by the
	 *                activity system.
	 * @exception WrongStateException
	 *                Thrown if the current activity is not in a state that
	 *                allows it to be completed, or is incompatible with the
	 *                completion status provided.
	 * @exception ProtocolViolationException
	 *                Thrown if the a violation of the activity service or HLS
	 *                protocol occurs.
	 * @exception NoPermissionException
	 *                Thrown if the invoking thread does not have permission to
	 *                terminate the transaction.
	 * @exception SystemException
	 *                Thrown if some other error occurred.
	 * 
	 * @param CompletionStatus
	 *            cs The CompletionStatus to use.
	 * 
	 * @return the result of completing the activity. Null is valid and must be
	 *         interpreted within the context of any HLS that may exist.
	 * 
	 * @see com.arjuna.mw.wsas.Outcome
	 * 
	 * @message com.arjuna.mwlabs.wsas.activity.ActivityImple_2
	 *          [com.arjuna.mwlabs.wsas.activity.ActivityImple_2] - Activity
	 *          cannot complete as it has active children:
	 * @message com.arjuna.mwlabs.wsas.activity.ActivityImple_3
	 *          [com.arjuna.mwlabs.wsas.activity.ActivityImple_3] - Cannot
	 *          complete activity in status:
	 */

	// ActivityFailedException rather than an Outcome status?
	public Outcome end (com.arjuna.mw.wsas.completionstatus.CompletionStatus cs)
			throws InvalidActivityException, WrongStateException,
			ProtocolViolationException, NoPermissionException, SystemException
	{
		/*
		 * TODO
		 * 
		 * We need an exception that can be thrown to say that the activity is
		 * completing.
		 */

		synchronized (this)
		{
			if (_status.equals(Active.instance()))
			{
				if (activeChildren())
				{
					/*
					 * Can we do equivalent of rollback on all children and then
					 * rollback this?
					 */

					throw new InvalidActivityException(
							wsasLogger.log_mesg.getString("com.arjuna.mwlabs.wsas.activity.ActivityImple_2")
									+ " " + this);
				}

				Outcome current = null;

				try
				{
					setCompletionStatus(cs);
				}
				catch (Exception ex)
				{
					// ignore and complete with the status we have.
				}

				_status = Completing.instance();

				try
				{
					Iterator<HLSWrapper> iter = HLSManager.HLServices();
					HLSWrapper elem;

					while (iter.hasNext())
					{
                        elem = iter.next();

						Outcome result = null;

						try
						{
							result = elem.hls().complete(getCompletionStatus());
						}
						catch (SystemException ex)
						{
							result = new OutcomeImple(new HLSException(ex),
									getCompletionStatus());
						}

						current = HLSManager.getOutcomeManager().processOutcome(current, result);
					}
				}
				catch (SystemException ex)
				{
					/*
					 * Currently if an exception occurs and we get here, then we
					 * forget all of the other outcomes and just return the
					 * exception. Does this make sense? How will applications be
					 * able to tell which HLSes have processed the outcome and
					 * which have not?
					 */

					current = new OutcomeImple(new HLSException(ex),
							Failure.instance());
				}

				if (_parent != null)
				{
					_parent.removeChild(this);
					_parent = null;
				}

				_status = Completed.instance();

				if (current == null)
					current = new OutcomeImple(getCompletionStatus());

				_result = current;

				return _result;
			}
			else
			{
				if (_result != null)
					return _result;
				else
				{
					// we can't have terminated yet!

					throw new WrongStateException(
							wsasLogger.log_mesg.getString("com.arjuna.mwlabs.wsas.activity.ActivityImple_3")
									+ " " + _status);
				}
			}
		}
	}

	/**
	 * Set the termination status for the current activity, if any.
	 * 
	 * @param CompletionStatus
	 *            endStatus The state in which the activity should attempt to
	 *            terminate. This may be one of the default values provided by
	 *            WSAS or may be extended in an implementation specific manner
	 *            by an HLS.
	 * 
	 * @exception NoActivityException
	 *                Thrown if there is no activity associated with the
	 *                invoking thread.
	 * @exception WrongStateException
	 *                Thrown if the completion status is incompatible with the
	 *                current state of the activity.
	 * @exception SystemException
	 *                Thrown if any other error occurs.
	 * 
	 * @message com.arjuna.mwlabs.wsas.activity.ActivityImple_4
	 *          [com.arjuna.mwlabs.wsas.activity.ActivityImple_4] - Cannot set
	 *          completion status on activity as the status is incompatible:
	 */

	public void setCompletionStatus (CompletionStatus endStatus)
			throws WrongStateException, SystemException
	{
		synchronized (this)
		{
			if (_status.equals(Active.instance()))
			{
				completionValid(endStatus);

				_completionStatus = endStatus;
			}
			else
				throw new WrongStateException(
						wsasLogger.log_mesg.getString("com.arjuna.mwlabs.wsas.activity.ActivityImple_4")
								+ " " + this + " " + _status);
		}
	}

	/**
	 * Get the completion status currently associated with the activity.
	 * 
	 * @exception NoActivityException
	 *                Thrown if there is no activity associated with the current
	 *                thread.
	 * @exception SystemException
	 *                Thrown if any other error occurs.
	 * 
	 * @return the termination status for the current activity, if any.
	 */

	public CompletionStatus getCompletionStatus () throws SystemException
	{
		synchronized (this)
		{
			return _completionStatus;
		}
	}

	/**
	 * Get the timeout value currently associated with activities.
	 * 
	 * @exception SystemException
	 *                Thrown if any error occurs.
	 * 
	 * @return the timeout value in seconds, or 0 if no application specified
	 *         timeout has been provided.
	 */

	public int getTimeout () throws SystemException
	{
		return _timeout;
	}

	/**
	 * @exception SystemException
	 *                Thrown if any error occurs.
	 * 
	 * @return the status of the current activity. If there is no activity
	 *         associated with the thread then NoActivity will be returned.
	 * 
	 * @see com.arjuna.mw.wsas.status.Status
	 */

	public com.arjuna.mw.wsas.status.Status status () throws SystemException
	{
		synchronized (this)
		{
			return _status;
		}
	}

	/**
	 * What is the name of the current activity? Use only for debugging
	 * purposes!
	 * 
	 * @exception NoActivityException
	 *                Thrown if there is no activity associated with the
	 *                invoking thread.
	 * @exception SystemException
	 *                Thrown if any other error occurs.
	 * 
	 * @return the name of the activity.
	 */

	public String activityName () throws NoActivityException, SystemException
	{
		return "ActivityImple: " + toString();
	}

	public String toString ()
	{
		return _activityId.stringForm();
	}

	/**
	 * @return the unique identifier for this activity.
	 */

	public GlobalId getGlobalId ()
	{
		return _activityId;
	}

	/**
	 * @return The parent of the activity, or null if it is top-level.
	 */

	public ActivityImple parent ()
	{
		return _parent;
	}

	/**
	 */

	public boolean equals (Object obj)
	{
		if (obj != null)
		{
			if (obj != this)
			{
				if (obj instanceof ActivityImple)
				{
					if (((ActivityImple) obj).getGlobalId().equals(getGlobalId()))
						return true;
				}
				else
					return true;
			}
		}

		return false;
	}

	/**
	 * Return the activity hierarchy that this activity is within. The zeroth
	 * element is the parent.
	 */

	public ActivityImple[] hierarchy ()
	{
		Stack hier = new Stack();
		ActivityImple ptr = this;

		while (ptr != null)
		{
			hier.push(ptr);

			ptr = ptr.parent();
		}

		int hierSize = hier.size();
		ActivityImple[] toReturn = new ActivityImple[hierSize];

		for (int i = 0; i < hierSize; i++)
			toReturn[i] = (ActivityImple) hier.pop();

		return toReturn;
	}

	/**
	 * Check whether the specified completion status is compatible with the one
	 * currently assigned to the activity.
	 * 
	 * @param CompletionStatus
	 *            cs The completion status to check.
	 * 
	 * @exception WrongStateException
	 *                Thrown if the specified status is incompatible with that
	 *                currently possessed by this activity.
	 * 
	 * @message com.arjuna.mwlabs.wsas.activity.ActivityImple_5
	 *          [com.arjuna.mwlabs.wsas.activity.ActivityImple_5] - Cannot
	 *          change completion status, value is incompatible:
	 */

	public final void completionValid (CompletionStatus cs)
			throws WrongStateException
	{
		if (!_completionStatus.equals(cs))
		{
			if (_completionStatus.equals(FailureOnly.instance()))
				throw new WrongStateException(
						wsasLogger.log_mesg.getString("com.arjuna.mwlabs.wsas.activity.ActivityImple_5")
								+ " " + _completionStatus + " " + cs);
		}
	}

	public int hashCode ()
	{
		return _activityId.hashCode();
	}

	/**
	 * Add the specified activity as a child of this activity.
	 * 
	 * @param ActivityImple
	 *            child The child activity.
	 * 
	 * @exception WrongStateException
	 *                Thrown if the parent activity is not in a state that
	 *                allows children to be added.
	 * @exception InvalidActivityException
	 *                Thrown if the child activity is invalid.
	 * @exception SystemException
	 *                Thrown if some other error occurs.
	 * 
	 * @message com.arjuna.mwlabs.wsas.activity.ActivityImple_6
	 *          [com.arjuna.mwlabs.wsas.activity.ActivityImple_6] - Cannot
	 *          enlist null child!
	 * @message com.arjuna.mwlabs.wsas.activity.ActivityImple_7
	 *          [com.arjuna.mwlabs.wsas.activity.ActivityImple_7] - Cannot
	 *          enlist child activity with parent as parent's status is:
	 */

	final void addChild (ActivityImple child) throws WrongStateException,
			InvalidActivityException, SystemException
	{
		if (child == null)
			throw new InvalidActivityException(
					wsasLogger.log_mesg.getString("com.arjuna.mwlabs.wsas.activity.ActivityImple_6"));

		synchronized (this)
		{
			if (_status.equals(Active.instance()))
			{
				_children.put(child.getGlobalId(), child);
			}
			else
				throw new WrongStateException(
						wsasLogger.log_mesg.getString("com.arjuna.mwlabs.wsas.activity.ActivityImple_7")
								+ " " + _status);
		}
	}

	/**
	 * Remove the specified child activity from this activity.
	 * 
	 * @param ActivityImple
	 *            child The child activity to remove.
	 * 
	 * @exception WrongStateException
	 *                Thrown if the parent activity is not in a state that
	 *                allows children to be removed.
	 * @exception InvalidActivityException
	 *                Thrown if the child activity is invalid.
	 * @exception SystemException
	 *                Thrown if some other error occurs.
	 * 
	 * @message com.arjuna.mwlabs.wsas.activity.ActivityImple_8
	 *          [com.arjuna.mwlabs.wsas.activity.ActivityImple_8] - Cannot
	 *          remove null child!
	 * @message com.arjuna.mwlabs.wsas.activity.ActivityImple_9
	 *          [com.arjuna.mwlabs.wsas.activity.ActivityImple_9] - The
	 *          following child activity is unknown to the parent:
	 * @message com.arjuna.mwlabs.wsas.activity.ActivityImple_10
	 *          [com.arjuna.mwlabs.wsas.activity.ActivityImple_10] - Cannot
	 *          remove child activity from parent as parent's status is:
	 */

	final void removeChild (ActivityImple child) throws WrongStateException,
			InvalidActivityException, SystemException
	{
		if (child == null)
			throw new InvalidActivityException(
					wsasLogger.log_mesg.getString("com.arjuna.mwlabs.wsas.activity.ActivityImple_8"));

		synchronized (this)
		{
			if (_status.equals(Active.instance()))
			{
				if (_children.get(child.getGlobalId()) == null)
					throw new InvalidActivityException(
							wsasLogger.log_mesg.getString("com.arjuna.mwlabs.wsas.activity.ActivityImple_9")
									+ child);
			}
			else
				throw new WrongStateException(
						wsasLogger.log_mesg.getString("com.arjuna.mwlabs.wsas.activity.ActivityImple_10")
								+ _status);
		}
	}

	/**
	 * @return <code>true</code> if this activity has active children, <code>
	 * false</code>
	 *         otherwise.
	 */

	private final boolean activeChildren ()
	{
		Enumeration e = _children.keys();

		while (e.hasMoreElements())
		{
			ActivityImple child = (ActivityImple) _children.get(e.nextElement());

			try
			{
				if ((child != null)
						&& (child.status().equals(Active.instance())))
				{
					return true;
				}
			}
			catch (Exception ex)
			{
				return true; // what else can we do?
			}
		}

		return false;
	}

	private ActivityImple _parent;
	private Hashtable _children;
	private Status _status;
	private CompletionStatus _completionStatus;
	private GlobalIdImple _activityId;
	private int _timeout;
	private Outcome _result;

}
