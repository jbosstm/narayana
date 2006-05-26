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
 * $Id: ArjunaCoreHLS.java,v 1.6 2005/05/19 12:13:33 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.context.soap.ArjunaContextImple;

import com.arjuna.mw.wscf.model.twophase.outcomes.CoordinationOutcome;
import com.arjuna.mw.wscf.model.twophase.status.*;
import com.arjuna.mw.wscf.model.twophase.common.TwoPhaseResult;

import com.arjuna.mw.wsas.status.Active;
import com.arjuna.mw.wsas.status.Unknown;
import com.arjuna.mw.wsas.status.NoActivity;

import com.arjuna.mw.wscf.common.Qualifier;
import com.arjuna.mw.wscf.common.CoordinatorId;

import com.arjuna.mw.wscf.api.UserCoordinatorService;

import com.arjuna.mw.wscf.model.as.coordinator.CoordinatorManagerService;

import com.arjuna.mw.wscf.model.as.coordinator.Participant;
import com.arjuna.mw.wscf.model.as.coordinator.Coordinator;
import com.arjuna.mw.wscf.model.as.coordinator.Message;

import com.arjuna.mw.wsas.context.Context;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;

import com.arjuna.mw.wsas.UserActivityFactory;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wsas.activity.Outcome;
import com.arjuna.mw.wsas.activity.HLS;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;
import com.arjuna.mw.wsas.completionstatus.Success;

import com.arjuna.mwlabs.wsas.activity.ActivityHandleImple;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;

import com.arjuna.mw.wscf.exceptions.*;

import java.util.Hashtable;

/**
 * The ArjunaCore coordination service implementation.
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ArjunaCoreHLS.java,v 1.6 2005/05/19 12:13:33 nmcl Exp $
 * @since 1.0.
 */

public class ArjunaCoreHLS implements HLS, CoordinatorManagerService,
		UserCoordinatorService
{

	public ArjunaCoreHLS ()
	{
	}

	/**
	 * An activity has begun and is active on the current thread.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ArjunaCoreHLS_1
	 *          [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ArjunaCoreHLS_1] -
	 *          ArjunaCoreHLS.begun:
	 */

	public void begun () throws SystemException
	{
		try
		{
			ACCoordinator coord = new ACCoordinator();
			
			int status = coord.start(parentCoordinator());

			if (status != ActionStatus.RUNNING)
			{
				throw new BegunFailedException(
						wscfLogger.log_mesg.getString("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ArjunaCoreHLS_1")
								+ ActionStatus.stringForm(status));
			}
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

	public void suspended () throws SystemException
	{
	}

	/**
	 * The activity has been resumed on the current thread.
	 */

	public void resumed () throws SystemException
	{
	}

	/**
	 * The activity has completed and is no longer active on the current thread.
	 */

	public void completed () throws SystemException
	{
	}

	/**
	 * The HLS name.
	 */

	public String identity () throws SystemException
	{
		return "ArjunaCoreHLS";
	}

	/**
	 * The activity service maintains a priority ordered list of HLS
	 * implementations. If an HLS wishes to be ordered based on priority then it
	 * can return a non-negative value: the higher the value, the higher the
	 * priority and hence the earlier in the list of HLSes it will appear (and
	 * be used in).
	 * 
	 * @return a positive value for the priority for this HLS, or zero/negative
	 *         if the order is not important.
	 */

	public int priority () throws SystemException
	{
		return 0;
	}

	/**
	 * Return the context augmentation for this HLS, if any on the current
	 * activity.
	 * 
	 * @param ActivityHierarchy
	 *            current The handle on the current activity hierarchy. The HLS
	 *            may use this when determining what information to place in its
	 *            context data.
	 * 
	 * @return a context object or null if no augmentation is necessary.
	 */

	public Context context () throws SystemException
	{
		return new ArjunaContextImple(currentCoordinator());
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
		return currentCoordinator().coordinate(cs);
	}

	/**
	 * Enrol the specified participant with the coordinator associated with the
	 * current thread. If the coordinator supports a priority ordering of
	 * participants, then that ordering can also be specified. Any qualifiers
	 * that are to be associated with the participant are also provided.
	 * 
	 * @param Participant
	 *            act The participant.
	 * @param int
	 *            priority The priority to associate with the participant in the
	 *            coordinator's list.
	 * @param Qualifier[]
	 *            quals Any qualifiers to be associated with the participant.
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

	public void addParticipant (Participant act, int priority, Qualifier[] quals)
			throws WrongStateException, DuplicateParticipantException,
			NoCoordinatorException, InvalidParticipantException,
			SystemException
	{
		currentCoordinator().addParticipant(act, priority, quals);
	}

	/**
	 * Remove the specified participant from the coordinator's list. This
	 * operation may not be supported by all coordination protocols.
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

	public void removeParticipant (Participant act)
			throws InvalidParticipantException, NoCoordinatorException,
			WrongStateException, SystemException
	{
		currentCoordinator().removeParticipant(act);
	}

	/**
	 * Some coordination protocol messages may have asynchronous responses or it
	 * may be possible for participants to autonomously generate responses to
	 * messages that have not yet been producted by the coordinator. As such,
	 * this method allows a response from a participant to be passed to the
	 * coordinator. In order to ensure that the protocol remains valid, it is
	 * necessary for the participant to specify what message produced the
	 * response: if the response was autonomously generated by the participant
	 * on the assumption it would receive this message from the coordinator and
	 * the coordinator subsequently decides not to produce such a message, then
	 * the action taken by the participant is invalid and hence so is the
	 * response.
	 * 
	 * @param String
	 *            id the unique participant identification.
	 * @param Message
	 *            notification the message the participant got/assumed when
	 *            producing the response.
	 * @param Outcome
	 *            response the actual response.
	 * @param Qualifier[]
	 *            quals any qualifiers associated with the response.
	 * 
	 * @exception InvalidParticipantException
	 *                Thrown if the coordinator has no knowledge of the
	 *                participant.
	 * @exception WrongStateException
	 *                Thrown if the coordinator is in a state that does not
	 *                allow it to accept responses at all or this specific type
	 *                of response.
	 * @exception SystemException
	 *                Thrown if any other error occurs.
	 */

	public void setResponse (String id, Message notification, Outcome response, Qualifier[] quals)
			throws InvalidParticipantException, WrongStateException,
			NoCoordinatorException, SystemException
	{
		currentCoordinator().setResponse(id, notification, response, quals);
	}

	/**
	 * @exception SystemException
	 *                Thrown if any error occurs.
	 * 
	 * @return a reference to the current coordinators' parent if it is nested,
	 *         null otherwise.
	 */

	public Coordinator getParentCoordinator () throws NoCoordinatorException,
			SystemException
	{
		return currentCoordinator().getParentCoordinator();
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

	public static String className ()
	{
		return ArjunaCoreHLS.class.getName();
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

	private final ACCoordinator currentCoordinator ()
			throws NoCoordinatorException, SystemException
	{
		ACCoordinator coord = (ACCoordinator) _coordinators.get(currentActivity());

		if (coord == null)
			throw new NoCoordinatorException();
		else
			return coord;
	}

	private static Hashtable _coordinators = new Hashtable();

}
