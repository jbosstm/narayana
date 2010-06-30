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
 * $Id: CoordinatorServiceImple.java,v 1.5 2005/05/19 12:13:37 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.model.sagas.arjunacore;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.mw.wscf.model.sagas.api.UserCoordinator;
import com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager;

import com.arjuna.mw.wscf.model.sagas.outcomes.CoordinationOutcome;
import com.arjuna.mw.wscf.model.sagas.common.TwoPhaseResult;
import com.arjuna.mw.wscf.model.sagas.participants.*;

import com.arjuna.mw.wscf.common.CoordinatorId;

import com.arjuna.mwlabs.wsas.UserActivityImple;

import com.arjuna.mwlabs.wsas.activity.ActivityImple;
import com.arjuna.mwlabs.wsas.activity.CompositeOutcomeImple;

import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mw.wsas.UserActivityFactory;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;

import com.arjuna.mw.wsas.completionstatus.*;

import com.arjuna.mw.wsas.status.NoActivity;

import com.arjuna.mw.wscf.exceptions.*;

import com.arjuna.mw.wscf.model.sagas.exceptions.*;

import com.arjuna.mw.wsas.exceptions.*;

/**
 * The user portion of the coordinator API. An implementation of this interface
 * presents each thread with the capability to create and manage coordinators.
 * It is very similar to the OTS Current and JTA UserTransaction.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: CoordinatorServiceImple.java,v 1.5 2005/05/19 12:13:37 nmcl Exp $
 * @since 1.0.
 *
 */

public class CoordinatorServiceImple implements UserCoordinator, CoordinatorManager
{

    public CoordinatorServiceImple ()
    {
	super();

	_coordManager = new CoordinatorControl();
    }

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

    public void begin () throws WrongStateException, SystemException
    {
	UserActivityFactory.userActivity().start();
    }

    /**
     * Start a new activity. If there is already an activity associated
     * with the thread then it will be nested.
     *
     * @param timeout The timeout associated with the activity. If the
     * activity has not been terminated by the time this period elapses, then
     * it will automatically be terminated.
     * @exception WrongStateException Thrown if the currently associated
     * activity is in a state that does not allow a new activity to be
     * enlisted as a child.
     * @exception InvalidTimeoutException Thrown if the specified timeout is
     * invalid within the current working environment.
     * @exception SystemException Thrown in any other situation.
     */

    public void begin (int timeout) throws WrongStateException, InvalidTimeoutException, SystemException
    {
	UserActivityFactory.userActivity().start(timeout);
    }	


    /**
     * Create a subordinate coordinator via the coordination control.
     */
    public BACoordinator createSubordinate() throws SystemException
    {
        return _coordManager.createSubordinate();
    }
    /**
     * Confirm the activity.
     *
     * @exception InvalidActivityException Thrown if the current activity is a
     * parent activity with active children.
     * @exception WrongStateException Thrown if the current activity is not in a
     * state that allows it to be completed in the status requested.
     * @exception ProtocolViolationException Thrown if the a violation of the
     * activity service or HLS protocol occurs.
     * @exception NoPermissionException Thrown if the invoking thread does
     * not have permission to terminate the transaction.
     * @exception SystemException Thrown if some other error occurred.
     */

    public void close () throws InvalidActivityException, WrongStateException, ProtocolViolationException, NoCoordinatorException, CoordinatorCancelledException, NoPermissionException, SystemException
    {
	try
	{
	    Outcome res = UserActivityFactory.userActivity().end(Success.instance());

	    /*
	     * TODO
	     *
	     * What happens if the coordinator has already been terminated?
	     */

	    if (res != null)
	    {
		// TODO properly! One HLS service per activity.

		if (res instanceof CompositeOutcomeImple)
		    res = ((CompositeOutcomeImple) res).get(CoordinationOutcome.class.getName());

		if (res instanceof CoordinationOutcome)
		{
		    CoordinationOutcome co = (CoordinationOutcome) res;
		    
		    switch (co.result())
		    {
		    case TwoPhaseResult.FINISH_OK:
		    case TwoPhaseResult.CONFIRMED:
		    case TwoPhaseResult.HEURISTIC_CONFIRM:
			break;
		    case TwoPhaseResult.CANCELLED:
		    case TwoPhaseResult.HEURISTIC_CANCEL:
			throw new CoordinatorCancelledException();
		    case TwoPhaseResult.HEURISTIC_MIXED:
			throw new ProtocolViolationException("HeuristicMixed");
		    case TwoPhaseResult.FINISH_ERROR:
			throw new WrongStateException();
		    case TwoPhaseResult.HEURISTIC_HAZARD:
		    default:
			throw new ProtocolViolationException("HeuristicHazard");
		    }
		}
		else
		    throw new ProtocolViolationException(wscfLogger.i18NLogger.get_model_sagas_arjunacore_CoordinatorServiceImple_1());
	    }
	}
	catch (NoActivityException ex)
	{
	    throw new NoCoordinatorException();
	}
	catch (ActiveChildException ex)
	{
	    // ?? assume the coordination protocol will cancel children anyway.
	}
    }
    
    /**
     * Cancel the activity.
     *
     * @exception InvalidActivityException Thrown if the current activity is a
     * parent activity with active children.
     * @exception WrongStateException Thrown if the current activity is not in a
     * state that allows it to be completed, or is incompatible with the
     * completion status provided.
     * @exception ProtocolViolationException Thrown if the a violation of the
     * activity service or HLS protocol occurs.
     * @exception NoPermissionException Thrown if the invoking thread does
     * not have permission to terminate the transaction.
     * @exception SystemException Thrown if some other error occurred.
     */

    public void cancel () throws InvalidActivityException, WrongStateException, ProtocolViolationException, NoCoordinatorException, CoordinatorConfirmedException, NoPermissionException, SystemException
    {
	try
	{
	    Outcome res = UserActivityFactory.userActivity().end(Failure.instance());
	    
	    if (res != null)
	    {
		if (res instanceof CompositeOutcomeImple)
		    res = ((CompositeOutcomeImple) res).get(CoordinationOutcome.class.getName());

		if (res instanceof CoordinationOutcome)
		{
		    CoordinationOutcome co = (CoordinationOutcome) res;
		    
		    switch (co.result())
		    {
		    case TwoPhaseResult.CONFIRMED:
		    case TwoPhaseResult.HEURISTIC_CONFIRM:
			throw new CoordinatorConfirmedException();
		    case TwoPhaseResult.FINISH_OK:
		    case TwoPhaseResult.CANCELLED:
		    case TwoPhaseResult.HEURISTIC_CANCEL:
			break;
		    case TwoPhaseResult.HEURISTIC_MIXED:
			throw new ProtocolViolationException("HeuristicMixed");
		    case TwoPhaseResult.FINISH_ERROR:
			throw new WrongStateException();
		    case TwoPhaseResult.HEURISTIC_HAZARD:
		    default:
			throw new ProtocolViolationException("HeuristicHazard");
		    }
		}
		else
		    throw new ProtocolViolationException(wscfLogger.i18NLogger.get_model_sagas_arjunacore_CoordinatorServiceImple_1());
	    }
	}
	catch (NoActivityException ex)
	{
	    throw new NoCoordinatorException();
	}
	catch (ActiveChildException ex)
	{
	    // ?? assume the coordination protocol will cancel children anyway.
	}
    }

    /**
     * Complete the activity.
     *
     * @exception InvalidActivityException Thrown if the current activity is a
     * parent activity with active children.
     * @exception WrongStateException Thrown if the current activity is not in a
     * state that allows it to be completed, or is incompatible with the
     * completion status provided.
     * @exception ProtocolViolationException Thrown if the a violation of the
     * activity service or HLS protocol occurs.
     * @exception NoPermissionException Thrown if the invoking thread does
     * not have permission to terminate the transaction.
     * @exception SystemException Thrown if some other error occurred.
     */

    public void complete () throws InvalidActivityException, WrongStateException, ProtocolViolationException, NoCoordinatorException, NoPermissionException, SystemException
    {
	_coordManager.complete();
    }
    
    /**
     * Set the termination status for the current activity to cancel only.
     *
     * @exception WrongStateException Thrown if the completion status is
     * incompatible with the current state of the activity.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void setCancelOnly () throws NoCoordinatorException, WrongStateException, SystemException
    {
	try
	{
	    UserActivityFactory.userActivity().setCompletionStatus(FailureOnly.instance());
	}
	catch (NoActivityException ex)
	{
	    throw new NoCoordinatorException();
	}
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
	return UserActivityFactory.userActivity().getTimeout();
    }	

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

    public void setTimeout (int timeout) throws InvalidTimeoutException, SystemException
    {
	UserActivityFactory.userActivity().setTimeout(timeout);
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
	
	return _coordManager.status();
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
     */

    public ActivityHierarchy suspend () throws SystemException
    {
	return UserActivityFactory.userActivity().suspend();
    }

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

    public void resume (ActivityHierarchy tx) throws InvalidActivityException, SystemException
    {
	UserActivityFactory.userActivity().resume(tx);
    }

    /**
     * Enrol the specified participant with the coordinator associated with
     * the current thread.
     *
     * @param act The participant.
     *
     * @exception WrongStateException Thrown if the coordinator is not in a
     * state that allows participants to be enrolled.
     * @exception DuplicateParticipantException Thrown if the participant has
     * already been enrolled and the coordination protocol does not support
     * multiple entries.
     * @exception InvalidParticipantException Thrown if the participant is invalid.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void enlistParticipant (Participant act) throws WrongStateException, DuplicateParticipantException, InvalidParticipantException, NoCoordinatorException, SystemException
    {
	_coordManager.enlistParticipant(act);
    }

    /**
     * Remove the specified participant from the coordinator's list.
     *
     * @exception InvalidParticipantException Thrown if the participant is not known
     * of by the coordinator.
     * @exception WrongStateException Thrown if the state of the coordinator
     * does not allow the participant to be removed (e.g., in a two-phase
     * protocol the coordinator is committing.)
     * @exception SystemException Thrown if any other error occurs.
     */
    
    public void delistParticipant (String participantId) throws InvalidParticipantException, NoCoordinatorException, WrongStateException, SystemException
    {
	_coordManager.delistParticipant(participantId);
    }

    public void participantCompleted (String participantId) throws NoActivityException, InvalidParticipantException, WrongStateException, SystemException
    {
	_coordManager.participantCompleted(participantId);
    }

    public void participantFaulted (String participantId) throws NoActivityException, InvalidParticipantException, SystemException
    {
	_coordManager.participantFaulted(participantId);

	try
	{
	    setCancelOnly();
	}
	catch (Exception ex)
	{
	    throw new SystemException(ex.toString());
	}
    }
    
    public void participantCannotComplete (String participantId) throws NoActivityException, InvalidParticipantException, WrongStateException, SystemException
    {
	_coordManager.participantCannotComplete(participantId);

	try
	{
	    setCancelOnly();
	}
	catch (Exception ex)
	{
	    throw new SystemException(ex.toString());
	}
    }

    /**
     * @return the token representing the current activity context hierarchy,
     * or null if there is none associated with the invoking thread.
     *
     * @exception SystemException Thrown if any error occurs.
     */

    public ActivityHierarchy currentActivity () throws SystemException
    {
	return UserActivityFactory.userActivity().currentActivity();
    }

    /**
     * @return the unique coordinator identifier.
     */

    public CoordinatorId identifier () throws NoActivityException, SystemException
    {
	ActivityImple curr = current();
	
	if (curr == null)
	    throw new NoActivityException();

	return _coordManager.identifier();
    }

    public final ActivityImple current ()
    {
	UserActivityImple imple = (UserActivityImple) UserActivityFactory.userActivity();
	
	return imple.current();
    }

    private CoordinatorControl _coordManager;
    
}

