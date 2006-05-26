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
 * $Id: CoordinatorControl.java,v 1.9 2005/06/09 09:41:27 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.model.twophase.arjunacore;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.mw.wscf.model.twophase.outcomes.CoordinationOutcome;
import com.arjuna.mw.wscf.model.twophase.status.*;
import com.arjuna.mw.wscf.model.twophase.common.TwoPhaseResult;
import com.arjuna.mw.wscf.model.twophase.participants.*;

import com.arjuna.mw.wsas.status.Active;
import com.arjuna.mw.wsas.status.Unknown;
import com.arjuna.mw.wsas.status.NoActivity;

import com.arjuna.mw.wscf.common.Qualifier;
import com.arjuna.mw.wscf.common.CoordinatorId;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;

import com.arjuna.mw.wsas.UserActivityFactory;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mwlabs.wsas.activity.ActivityHandleImple;
import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.subordinate.SubordinateCoordinator;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;
import com.arjuna.mw.wsas.completionstatus.Success;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;
import com.arjuna.mw.wsas.exceptions.NoActivityException;

import com.arjuna.mw.wscf.exceptions.*;

import com.arjuna.mw.wscf.model.twophase.exceptions.*;

import java.util.Hashtable;

/**
 * The ArjunaCore coordination service implementation.
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: CoordinatorControl.java,v 1.9 2005/06/09 09:41:27 nmcl Exp $
 * @since 1.0.
 * 
 * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.CoordinatorControl_1
 *          [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.CoordinatorControl_1] -
 *          CoordinatorControl.begin:
 */

public class CoordinatorControl
{

	public CoordinatorControl ()
	{
	}

	/**
	 * An activity has begun and is active on the current thread.
	 */

	public void begin () throws SystemException
	{		
		try
		{
			ACCoordinator coord = new ACCoordinator();
			int status = coord.start(parentCoordinator());

			if (status != ActionStatus.RUNNING)
				throw new BegunFailedException(
						wscfLogger.log_mesg.getString("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.CoordinatorControl_1")
								+ ActionStatus.stringForm(status));
			else
			{
				_coordinators.put(currentActivity(), coord);
			}
		}
		catch (SystemException ex)
		{
			throw ex;
		}
		catch (Exception ex)
		{
			throw new UnexpectedException(ex.toString());
		}
	}

	/**
	 * The current activity is completing with the specified completion status.
	 * 
	 * @param CompletionStatus
	 *            cs The completion status to use.
	 * 
	 * @return The result of terminating the relationship of this HLS and the
	 *         current activity.
	 */

	public Outcome complete (CompletionStatus cs) throws SystemException
	{
		ACCoordinator current = currentCoordinator();
		int outcome;

		if ((cs != null) && (cs instanceof Success))
		{
			// commit

			outcome = current.end(true);
		}
		else
		{
			// abort

			outcome = current.cancel();
		}

		_coordinators.remove(currentActivity());

		int result;

		switch (outcome)
		{
		case ActionStatus.ABORTED:
			result = TwoPhaseResult.CANCELLED;
			break;
		case ActionStatus.COMMITTED:
			result = TwoPhaseResult.CONFIRMED;
			break;
		case ActionStatus.H_ROLLBACK:
			result = TwoPhaseResult.HEURISTIC_CANCEL;
			break;
		case ActionStatus.H_COMMIT:
			result = TwoPhaseResult.HEURISTIC_CONFIRM;
			break;
		case ActionStatus.H_MIXED:
			result = TwoPhaseResult.HEURISTIC_MIXED;
			break;
		case ActionStatus.H_HAZARD:
			result = TwoPhaseResult.HEURISTIC_HAZARD;
			break;
		default:
			result = TwoPhaseResult.FINISH_ERROR;
			break;
		}

		return new CoordinationOutcome(cs, result);
	}

	/**
	 * The activity has been suspended.
	 */

	public void suspend () throws SystemException
	{
	}

	/**
	 * The activity has been resumed on the current thread.
	 */

	public void resume () throws SystemException
	{
	}

	/**
	 * The activity has completed and is no longer active on the current thread.
	 */

	public void completed () throws SystemException
	{
	}

	/**
	 * If the application requires and if the coordination protocol supports it,
	 * then this method can be used to execute a coordination protocol on the
	 * currently enlisted participants at any time prior to the termination of
	 * the coordination scope.
	 * 
	 * This implementation only supports coordination at the end of the
	 * activity.
	 * 
	 * @param CompletionStatus
	 *            cs The completion status to use when determining how to
	 *            execute the protocol.
	 * 
	 * @exception WrongStateException
	 *                Thrown if the coordinator is in a state the does not allow
	 *                coordination to occur.
	 * @exception ProtocolViolationException
	 *                Thrown if the protocol is violated in some manner during
	 *                execution.
	 * @exception SystemException
	 *                Thrown if any other error occurs.
	 * 
	 * @return The result of executing the protocol, or null.
	 */

	public Outcome coordinate (CompletionStatus cs) throws WrongStateException,
			ProtocolViolationException, SystemException
	{
		return null;
	}

	/**
	 * @exception SystemException
	 *                Thrown if any error occurs.
	 * 
	 * @return the status of the current coordinator. If there is no activity
	 *         associated with the thread then NoActivity will be returned.
	 * 
	 * @see com.arjuna.mw.wsas.status.Status
	 */

	public com.arjuna.mw.wsas.status.Status status () throws SystemException
	{
		int currentStatus = currentCoordinator().status();

		switch (currentStatus)
		{
		case ActionStatus.CREATED:
		case ActionStatus.RUNNING:
			return Active.instance();
		case ActionStatus.PREPARING:
			return Preparing.instance();
		case ActionStatus.ABORTING:
			return Cancelling.instance();
		case ActionStatus.ABORTED:
			return Cancelled.instance();
		case ActionStatus.ABORT_ONLY:
			return CancelOnly.instance();
		case ActionStatus.PREPARED:
			return Prepared.instance();
		case ActionStatus.COMMITTING:
			return Confirming.instance();
		case ActionStatus.COMMITTED:
			return Confirmed.instance();
		case ActionStatus.H_ROLLBACK:
			return HeuristicCancel.instance();
		case ActionStatus.H_COMMIT:
			return HeuristicConfirm.instance();
		case ActionStatus.H_MIXED:
			return HeuristicMixed.instance();
		case ActionStatus.H_HAZARD:
			return HeuristicHazard.instance();
		case ActionStatus.NO_ACTION:
			return NoActivity.instance();
		case ActionStatus.DISABLED:
		case ActionStatus.INVALID:
		case ActionStatus.CLEANUP:
		default:
			return Unknown.instance();
		}
	}

	/**
	 * Not supported by basic ArjunaCore.
	 * 
	 * @exception SystemException
	 *                Thrown if any error occurs.
	 * 
	 * @return the complete list of qualifiers that have been registered with
	 *         the current coordinator.
	 */

	public Qualifier[] qualifiers () throws NoCoordinatorException,
			SystemException
	{
		return currentCoordinator().qualifiers();
	}

	/**
	 * @exception SystemException
	 *                Thrown if any error occurs.
	 * 
	 * @return The unique identity of the current coordinator.
	 */

	public CoordinatorId identifier () throws NoCoordinatorException,
			SystemException
	{
		return currentCoordinator().identifier();
	}

	/**
	 * Enrol the specified participant with the coordinator associated with the
	 * current thread.
	 * 
	 * @param Participant
	 *            act The participant.
	 * 
	 * @exception WrongStateException
	 *                Thrown if the coordinator is not in a state that allows
	 *                participants to be enrolled.
	 * @exception DuplicateParticipantException
	 *                Thrown if the participant has already been enrolled and
	 *                the coordination protocol does not support multiple
	 *                entries.
	 * @exception InvalidParticipantException
	 *                Thrown if the participant is invalid.
	 * @exception SystemException
	 *                Thrown if any other error occurs.
	 */

	public void enlistParticipant (Participant act) throws WrongStateException,
			DuplicateParticipantException, InvalidParticipantException,
			NoCoordinatorException, SystemException
	{
		currentCoordinator().enlistParticipant(act);
	}

	/**
	 * Remove the specified participant from the coordinator's list.
	 * 
	 * @exception InvalidParticipantException
	 *                Thrown if the participant is not known of by the
	 *                coordinator.
	 * @exception WrongStateException
	 *                Thrown if the state of the coordinator does not allow the
	 *                participant to be removed (e.g., in a two-phase protocol
	 *                the coordinator is committing.)
	 * @exception SystemException
	 *                Thrown if any other error occurs.
	 */

	public void delistParticipant (Participant act)
			throws InvalidParticipantException, NoCoordinatorException,
			WrongStateException, SystemException
	{
		currentCoordinator().delistParticipant(act);
	}

	/**
	 * Enrol the specified synchronization with the coordinator associated with
	 * the current thread.
	 * 
	 * @param Synchronization
	 *            act The synchronization to remove.
	 * 
	 * @exception WrongStateException
	 *                Thrown if the coordinator is not in a state that allows
	 *                participants to be enrolled.
	 * @exception DuplicateSynchronizationException
	 *                Thrown if the participant has already been enrolled and
	 *                the coordination protocol does not support multiple
	 *                entries.
	 * @exception InvalidSynchronizationException
	 *                Thrown if the participant is invalid.
	 * @exception SystemException
	 *                Thrown if any other error occurs.
	 */

	public void enlistSynchronization (Synchronization act)
			throws WrongStateException, DuplicateSynchronizationException,
			InvalidSynchronizationException, NoCoordinatorException,
			SystemException
	{
		currentCoordinator().enlistSynchronization(act);
	}

	/**
	 * Remove the specified synchronization from the coordinator's list.
	 * 
	 * @exception InvalidSynchronizationException
	 *                Thrown if the participant is not known of by the
	 *                coordinator.
	 * @exception WrongStateException
	 *                Thrown if the state of the coordinator does not allow the
	 *                participant to be removed (e.g., in a two-phase protocol
	 *                the coordinator is committing.)
	 * @exception SystemException
	 *                Thrown if any other error occurs.
	 */

	public void delistSynchronization (Synchronization act)
			throws InvalidSynchronizationException, NoCoordinatorException,
			WrongStateException, SystemException
	{
		currentCoordinator().delistSynchronization(act);
	}

	public void participantRolledBack (String participantId)
			throws NoActivityException, InvalidParticipantException,
			WrongStateException, SystemException
	{
		currentCoordinator().participantRolledBack(participantId);
	}

	public void participantReadOnly (String participantId)
			throws NoActivityException, InvalidParticipantException,
			SystemException
	{
		currentCoordinator().participantReadOnly(participantId);
	}

	/**
	 * Create a subordinate transaction, i.e., one which can be driven
	 * through prepare, commit and rollback. Such a transaction is not
	 * interposed with any parent transaction because the parent may
	 * be physically remote from the child. Such interposition is the
	 * responsibility of the invoker.
	 * 
	 * @return the subordinate transaction. The transaction is not
	 * associated with the thread and is not interposed. It is running.
	 * 
	 * @throws SystemException throw if any error occurs.
	 */
	
	public final ACCoordinator createSubordinate () throws SystemException
	{	
		try
		{
			SubordinateCoordinator coord = new SubordinateCoordinator();
			int status = coord.start(null);

			if (status != ActionStatus.RUNNING)
			{
				throw new BegunFailedException(
						wscfLogger.log_mesg.getString("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.CoordinatorControl_1")
								+ ActionStatus.stringForm(status));
			}
			else
			{
				/*
				 * TODO does this need to be added to the list?
				 */
				
				// _coordinators.put(currentActivity(), coord);
				
				return coord;
			}
		}
		catch (SystemException ex)
		{
			throw ex;
		}
		catch (Exception ex)
		{
			throw new UnexpectedException(ex.toString());
		}
	}
	
	final ACCoordinator currentCoordinator () throws NoCoordinatorException,
			SystemException
	{
		ACCoordinator coord = (ACCoordinator) _coordinators.get(currentActivity());
		
		if (coord == null)
			throw new NoCoordinatorException();
		else
			return coord;
	}

	private final ActivityHandleImple currentActivity () throws SystemException
	{
		try
		{
			ActivityHierarchy hier = UserActivityFactory.userActivity().currentActivity();

			if (hier.size() > 0)
				return (ActivityHandleImple) hier.activity(hier.size() - 1);
			else
				return null;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();

			throw new SystemException(ex.toString());
		}
	}

	private final ACCoordinator parentCoordinator () throws SystemException
	{
		try
		{
			ActivityHierarchy hier = UserActivityFactory.userActivity().currentActivity();
			ActivityHandleImple parentActivity = null;
			ACCoordinator parentCoordinator = null;

			if (hier.size() > 1)
			{
				parentActivity = (ActivityHandleImple) hier.activity(hier.size() - 2);

				parentCoordinator = (ACCoordinator) _coordinators.get(parentActivity);
			}

			return parentCoordinator;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();

			return null;
		}
	}

	private static Hashtable _coordinators = new Hashtable();

}
