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
 * $Id: ACCoordinator.java,v 1.7 2005/06/09 09:41:27 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.model.twophase.arjunacore;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.*;

import com.arjuna.mw.wscf.model.twophase.participants.*;
import com.arjuna.mw.wscf.model.twophase.exceptions.*;

import com.arjuna.mw.wscf.common.Qualifier;
import com.arjuna.mw.wscf.common.CoordinatorId;

import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;

import com.arjuna.mw.wscf.exceptions.*;

/**
 * This class represents a specific coordination instance. It is essentially an
 * ArjunaCore TwoPhaseCoordinator, which gives us access to two-phase with
 * synchronization support but without thread management.
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ACCoordinator.java,v 1.7 2005/06/09 09:41:27 nmcl Exp $
 * @since 1.0.
 * 
 * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ATCoordinator_1
 *          [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ATCoordinator_1] -
 *          ArjunaCore does not support removal of participants
 * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ATCoordinator_2
 *          [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ATCoordinator_2] -
 *          Null is an invalid parameter!
 * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ATCoordinator_3
 *          [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ATCoordinator_3] -
 *          Wrong state for operation!
 */

public class ATCoordinator extends TwoPhaseCoordinator
{

	private final static int ROLLEDBACK = 0;
	private final static int READONLY = 1;

	public ATCoordinator()
	{
		super();
	
		_theId = new CoordinatorIdImple(get_uid());
	}

	public ATCoordinator(Uid recovery)
	{
		super(recovery);
				
		_theId = new CoordinatorIdImple(get_uid());
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
	 * Enrol the specified participant with the coordinator associated with the
	 * current thread.
	 * 
	 * @param act The participant.
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
			SystemException
	{		
		if (act == null)
			throw new InvalidParticipantException();

		AbstractRecord rec = new ParticipantRecord(act, new Uid());

		if (add(rec) != AddOutcome.AR_ADDED)
			throw new WrongStateException();
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
			throws InvalidParticipantException, WrongStateException,
			SystemException
	{
		if (act == null)
			throw new InvalidParticipantException();
		else
			throw new WrongStateException(
					wscfLogger.arjLoggerI18N.getString("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ATCoordinator_1"));
	}

	/**
	 * Enrol the specified synchronization with the coordinator associated with
	 * the current thread.
	 * 
	 * @param act The synchronization to add.
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
			InvalidSynchronizationException, SystemException
	{
		if (act == null)
			throw new InvalidSynchronizationException();

		SynchronizationRecord rec = new SynchronizationRecord(act, new Uid());

		if (addSynchronization(rec) != AddOutcome.AR_ADDED)
			throw new WrongStateException();
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
			throws InvalidSynchronizationException, WrongStateException,
			SystemException
	{
		if (act == null)
			throw new InvalidSynchronizationException();
		else
			throw new WrongStateException(
					wscfLogger.arjLoggerI18N.getString("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ATCoordinator_1"));
	}

	/**
	 * @exception SystemException
	 *                Thrown if any error occurs.
	 * 
	 * @return the complete list of qualifiers that have been registered with
	 *         the current coordinator.
	 */

	public Qualifier[] qualifiers () throws SystemException
	{
		return null;
	}

	/**
	 * @exception SystemException
	 *                Thrown if any error occurs.
	 * 
	 * @return The unique identity of the current coordinator.
	 */

	public CoordinatorId identifier () throws SystemException
	{
		return _theId;
	}

	public synchronized void participantRolledBack (String participantId)
			throws InvalidParticipantException, WrongStateException,
			SystemException
	{
		if (participantId == null)
			throw new SystemException(
					wscfLogger.arjLoggerI18N.getString("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ATCoordinator_2"));

		if (status() == ActionStatus.RUNNING)
			changeParticipantStatus(participantId, ROLLEDBACK);
		else
			throw new WrongStateException();
	}

	public synchronized void participantReadOnly (String participantId)
			throws InvalidParticipantException, SystemException
	{
		if (participantId == null)
			throw new SystemException(
					wscfLogger.arjLoggerI18N.getString("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ATCoordinator_2"));

		if (status() == ActionStatus.RUNNING)
		{
			changeParticipantStatus(participantId, READONLY);
		}
		else
			throw new SystemException(
					wscfLogger.arjLoggerI18N.getString("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ATCoordinator_3"));
	}

    @Override
    public String type ()
	{
		return "/StateManager/BasicAction/AtomicAction/TwoPhaseCoordinator/TwoPhase/ATCoordinator";
	}
	
	private final void changeParticipantStatus (String participantId, int status)
			throws InvalidParticipantException, SystemException
	{
		/*
		 * Transaction is active, so we can look at the pendingList only.
		 */

		// TODO allow transaction status to be changed during commit - exit
		// could come in late
		boolean found = false;

		if (pendingList != null)
		{
			RecordListIterator iter = new RecordListIterator(pendingList);
			AbstractRecord absRec = iter.iterate();

			try
			{
				while ((absRec != null) && !found)
				{
					if (absRec instanceof ParticipantRecord)
					{
						ParticipantRecord pr = (ParticipantRecord) absRec;
						Participant participant = (Participant) pr.value();

						if (participantId.equals(participant.id()))
						{
							found = true;

							if (status == READONLY)
								pr.readonly();
							else
								pr.rolledback();
						}
					}

					absRec = iter.iterate();
				}
			}
			catch (Exception ex)
			{
				throw new SystemException(ex.toString());
			}
		}

		if (!found)
			throw new InvalidParticipantException();
	}

	private CoordinatorIdImple _theId;

}
