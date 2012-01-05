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
 * $Id: ACCoordinator.java,v 1.5 2005/05/19 12:13:37 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.model.sagas.arjunacore;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.*;

import com.arjuna.mw.wscf.model.sagas.participants.*;
import com.arjuna.mw.wscf.model.sagas.exceptions.DuplicateSynchronizationException;
import com.arjuna.mw.wscf.model.sagas.exceptions.InvalidSynchronizationException;

import com.arjuna.mw.wscf.common.Qualifier;
import com.arjuna.mw.wscf.common.CoordinatorId;

import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;

import com.arjuna.mw.wscf.exceptions.*;

/**
 * This class represents a specific coordination instance. It inherits from
 * ArjunaCore TwoPhaseCoordinator so we can do the prepare and commit when a
 * BA client close is requested and have it roll back if anything goes wrong.
 * The BA client cancel request maps through to TwoPhaseCoordinator cancel which
 * also does what we need. Although we inherit synchronization support from
 * TwoPhaseCoordinator it is not used by the BA code.
 *
 * This class also exposes a separate complete method which implements
 * the deprectaed BA client complete operation allowing coordinator completion
 * participants to be notified that they complete. This is pretty much redundant
 * as complete gets called at close anyway.
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ACCoordinator.java,v 1.5 2005/05/19 12:13:37 nmcl Exp $
 * @since 1.0.
 * 
 */

public class BACoordinator extends TwoPhaseCoordinator
{

    private final static int DELISTED = 0;

    private final static int COMPLETED = 1;

    private final static int FAILED = 2;

	public BACoordinator()
	{
		super();

		_theId = new CoordinatorIdImple(get_uid());
	}

	public BACoordinator(Uid recovery)
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
     * ensure all ParticipantCompletion participants have completed and then send a complete message to
     * any remaining CoordinatorCompletion participants.
     * @throws WrongStateException if the transaction is neither RUNNING nor PREPARED
     * @throws SystemException if there are incomplete ParticipantCompletion participants or if one of the
     * CoordinatorCompletion participants fails to complete.
     */
    public synchronized void complete () throws WrongStateException,
            SystemException
	{
        int status = status();

        if (status == ActionStatus.RUNNING)
		{
            // check that all ParticipantCompletion participants have completed
            // throwing a wobbly if not

            if (pendingList != null)
            {
                RecordListIterator iter = new RecordListIterator(pendingList);
                AbstractRecord absRec = iter.iterate();

                while (absRec != null)
                {
                    if (absRec instanceof ParticipantRecord)
                    {
                        ParticipantRecord pr = (ParticipantRecord) absRec;

                        if (!pr.complete()) {

                            // ok, we must force a rollback

                            preventCommit();

                            wscfLogger.i18NLogger.warn_model_sagas_arjunacore_BACoordinator_1(get_uid());

                            throw new SystemException("Participant failed to complete");
                        }
                    }

                    absRec = iter.iterate();
                }
            }
		}
		else
        {
            throw new WrongStateException();
        }
    }

    /**
     * close the activity
     * @return
     * @throws SystemException
     */
    public int close () throws SystemException
    {
        // we need to make sure all coordinator completion aprticipants ahve completed
        try {
            complete();
        } catch (Exception e) {
            // carry on as end will catch any further problems
        }

        // now call end(). if any of the participant completion participants are not
        // completed they will throw a WrongStateException during prepare leading to rollback

        return end(true);
    }

    /**
     * cancel the activity
     * @return
     */
    public int cancel ()
    {
        return super.cancel();
    }

	/**
	 * Enrol the specified participant with the coordinator associated with the
	 * current thread.
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
		if (act == null) throw new InvalidParticipantException();

		AbstractRecord rec = new ParticipantRecord((Participant)act, new Uid());

		if (add(rec) != AddOutcome.AR_ADDED) throw new WrongStateException();
		else
		{
			/*
			 * Presume nothing protocol, so we need to write the intentions list
			 * every time a participant is added.
			 */
		}
	}

	/**
	 * Remove the specified participant from the coordinator's list. this is the target of the
	 * 
	 * @exception InvalidParticipantException
	 *                Thrown if the participant is not known of by the
	 *                coordinator.
	 * @exception WrongStateException
	 *                Thrown if the state of the coordinator does not allow the
	 *                participant to be removed
	 * @exception SystemException
	 *                Thrown if any other error occurs.
	 */

	public synchronized void delistParticipant (String participantId)
			throws InvalidParticipantException, WrongStateException,
			SystemException
	{
		if (participantId == null)
			throw new SystemException(
                    wscfLogger.i18NLogger.get_model_sagas_arjunacore_BACoordinator_2());

        int status = status();
        // exit is only legitimate when the TX is in these states
        switch (status) {
            case ActionStatus.RUNNING:
            case ActionStatus.ABORT_ONLY:
                changeParticipantStatus(participantId, DELISTED);
                break;
            default:
                throw new WrongStateException(
                        wscfLogger.i18NLogger.get_model_sagas_arjunacore_BACoordinator_3());
        }
    }

	public synchronized void participantCompleted (String participantId)
			throws InvalidParticipantException, WrongStateException,
			SystemException
	{
		if (participantId == null)
			throw new SystemException(
                    wscfLogger.i18NLogger.get_model_sagas_arjunacore_BACoordinator_2());

        int status = status();
        // completed is only legitimate when the TX is in these states
        switch (status) {
            case ActionStatus.RUNNING:
            case ActionStatus.ABORT_ONLY:
                changeParticipantStatus(participantId, COMPLETED);
                break;
            default:
                throw new WrongStateException(
                        wscfLogger.i18NLogger.get_model_sagas_arjunacore_BACoordinator_3());
        }
	}

	public synchronized void participantFaulted (String participantId)
			throws InvalidParticipantException, SystemException
	{
		if (participantId == null)
			throw new SystemException(
                    wscfLogger.i18NLogger.get_model_sagas_arjunacore_BACoordinator_2());

        int status = status();
        // faulted is only legitimate when the TX is in these states
        switch (status) {
            case ActionStatus.RUNNING:
                // if a participant notifies this then we need to mark the transaction as abort only
                preventCommit();
                // !!! deliberate drop through !!!
            case ActionStatus.ABORT_ONLY:
            case ActionStatus.COMMITTING:
            case ActionStatus.COMMITTED:    // this can happen during recovery processing
            case ActionStatus.ABORTING:
                changeParticipantStatus(participantId, FAILED);
                break;
            default:
                throw new SystemException(
                        wscfLogger.i18NLogger.get_model_sagas_arjunacore_BACoordinator_3());
        }
    }

    // n.b. this is only appropriate for the 1.1 protocol

    public synchronized void participantCannotComplete (String participantId)
            throws InvalidParticipantException, WrongStateException, SystemException
    {
        if (participantId == null)
            throw new SystemException(
                    wscfLogger.i18NLogger.get_model_sagas_arjunacore_BACoordinator_2());

        int status = status();
        // cannot complete is only legitimate when the TX is in these states
        switch (status) {
            case ActionStatus.RUNNING:
                // if a participant notifies this then we need to mark the transaction as abort only
                preventCommit();
                // !!! deliberate drop through !!!
            case ActionStatus.ABORT_ONLY:
                changeParticipantStatus(participantId, DELISTED);
                break;
            default:
                throw new WrongStateException(
                        wscfLogger.i18NLogger.get_model_sagas_arjunacore_BACoordinator_3());
        }
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
                    wscfLogger.i18NLogger.get_model_sagas_arjunacore_BACoordinator_4());
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

    public String type ()
    {
        return "/StateManager/BasicAction/AtomicAction/Sagas/BACoordinator";
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

							if (status == DELISTED) {
                                pr.delist(false);
                            } else if (status == FAILED) {
                                pr.delist(true);
                            } else {
								pr.completed();
                            }
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

		if (!found) throw new InvalidParticipantException();
	}

	private CoordinatorIdImple _theId;

}
