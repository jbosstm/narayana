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
 * ArjunaCore BasicAction which gives us independent prepare, commit and abort
 * without synchronization support and without thread management. We don't
 * inherit from TwoPhaseCoordinator because we don't ever initiate a 2phase
 * operation directly -- we map the BA Termination protocol complete message
 * to prepare, the close message to commit and the cancel to abort so these
 * operations are decoupled actions. Also, since we don't call end we cannot
 * usefully use the synchronization support (not that we need it anyway).
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ACCoordinator.java,v 1.5 2005/05/19 12:13:37 nmcl Exp $
 * @since 1.0.
 * 
 * @message com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_1
 *          [com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_1] -
 *          Cannot complete business activity until all ParticipantCompletion participants have completed
 * @message com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_2
 *          [com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_2] -
 *          Null is an invalid parameter.
 * @message com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_3
 *          [com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_3] -
 *          Wrong state for operation!
 * @message com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_4
 *          [com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_4] -
 *          Complete of action-id {0} failed.
 * @message com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_5
 *          [com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_5] - Received
 *          heuristic: {0} .
 * @message com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_6
 *          [com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_6] - Action marked as AbortOnly {0}.
 */

public class ACCoordinator extends BasicAction
{

    private final static int DELISTED = 0;

    private final static int COMPLETED = 1;

    private final static int FAILED = 2;

	public ACCoordinator ()
	{
		super();

		_theId = new CoordinatorIdImple(get_uid());
	}

	public ACCoordinator (Uid recovery)
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
     * start the transaction
     *
     * @param parentAction
     * @return
     */
    public int start (BasicAction parentAction)
    {
        if (parentAction != null)
            parentAction.addChildAction(this);

        return super.Begin(parentAction);
    }

    /**
     * ensure that none of required participants are still active throwing a SystemException if they are
     * @param allParticipants true if no participants, including CooordinatorCompletion participants,
     * may still be active and false if only CooordinatorCompletion participants may still be active.
     * @throws SystemException if any of the required participants has not completed.
     */
    void ensureNotActive(boolean allParticipants) throws SystemException
    {
        if (pendingList != null)
        {
            RecordListIterator iter = new RecordListIterator(pendingList);
            AbstractRecord absRec = iter.iterate();

            while (absRec != null)
            {
                if (absRec instanceof ParticipantRecord)
                {
                    ParticipantRecord pr = (ParticipantRecord) absRec;

                    if ((allParticipants || pr.isParticipantCompletion()) && pr.isActive())
                    {
                        throw new SystemException(
                                wscfLogger.log_mesg
                                        .getString("com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_1"));
                    }
                }

                absRec = iter.iterate();
            }
        }
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

            ensureNotActive(false);

            doComplete();

		}
		else
        {
            throw new WrongStateException();
        }
    }

    /**
     * invoke the prepare operation in order to drive all CoordinatorCompletion participants to completion
     * @throws SystemException if any of the participants fails to complete
     */
    private void doComplete() throws SystemException
    {
        // we can emerge from this with outcome PREPARE_NOTOK if one of the CoordinatorCompletion participants
        // failed to respond to a completed request. in that case the action status will be PREPARING
        int outcome = super.prepare(true);

        if (outcome == TwoPhaseOutcome.PREPARE_NOTOK) {
            if (wscfLogger.arjLoggerI18N.isWarnEnabled())
            {
                wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_4", new Object[] { get_uid() });
            }

            int heuristicDecision = getHeuristicDecision();

            if (heuristicDecision != TwoPhaseOutcome.PREPARE_OK)
            {
                if (wscfLogger.arjLoggerI18N.isWarnEnabled())
                {
                    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_5", new Object[] { TwoPhaseOutcome.stringForm(heuristicDecision) });
                }
            }

            if (wscfLogger.arjLoggerI18N.isWarnEnabled())
            {
                wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_6",new Object[] { get_uid() });
            }

        }
    }

    /**
     * if all is well with the TX invoke the phase2Commit operation in order to drive all participants to
     * closed status, if not invoke Abort.
     * @return the outcome of the commit or abort
     * @throws SystemException if there are _any_ uncompleted participants, either because the client has
     * not called close to close CoordinatorCompletion participants or because not all ParticipantCompletion
     * participants have notified completion.
     */
    public int close () throws SystemException
    {
        int outcome;

        if (parent() != null)
            parent().removeChildAction(this);

        if (status() == ActionStatus.RUNNING) {
            // check that _all_ participants have completed throwing a SystemException if not

            ensureNotActive(true);

            // ok, we do a complete anyway to ensure we log the participant list and progress to state
            // PREPARED. this should not fail since the participants ought to just say yeah yeah whateva

            doComplete();
        }

        switch (status()) {
            case ActionStatus.PREPARING:
            {
                // we failed during prepare so we have to do a phase2Abort in order to clean up
                // this means we may end up leaving a log entry for an aborted transaction with
                // a heuristic outcome
                super.phase2Abort(true);
                outcome = status();
            }
            break;
            // we managed to prepare so we need to do a phase2Commit
            case ActionStatus.PREPARED:
            case ActionStatus.COMMITTING:
            {
                if (parent() != null)
                    parent().removeChildAction(this);

                super.phase2Commit(true);
                // remap the status if there are heuristics
                outcome = status();
                int heuristicDecision = getHeuristicDecision();

                switch (heuristicDecision)
                {
                case TwoPhaseOutcome.PREPARE_OK:
                case TwoPhaseOutcome.FINISH_OK:
                    break;
                case TwoPhaseOutcome.HEURISTIC_ROLLBACK:
                    outcome = ActionStatus.H_ROLLBACK;
                case TwoPhaseOutcome.HEURISTIC_COMMIT:
                    outcome = ActionStatus.H_COMMIT;
                case TwoPhaseOutcome.HEURISTIC_MIXED:
                    outcome = ActionStatus.H_MIXED;
                case TwoPhaseOutcome.HEURISTIC_HAZARD:
                default:
                    outcome = ActionStatus.H_HAZARD;
                }
            }
            break;
            // this covers the case where the tx is marked as ABORT_ONLY
            default:
            {
                if (parent() != null)
                    parent().removeChildAction(this);

                // no heuristics to deal with
                
                outcome = super.Abort();
            }
        }

        return outcome;
    }

    /**
     * invoke Abort.
     * @return the outcome of the abort
     */
    public int cancel ()
    {
        // we cannot do this as per TwoPhaseCoordinator because TxControl and TxStats are package private
        // but note that JTS does not count this either
        //if (TxControl.enableStatistics)
        //    TxStats.incrementApplicationRollbacks();

        if (parent() != null)
            parent().removeChildAction(this);

        int outcome;

        switch (status()) {

            // PREPARING/PREPARED/COMMITTING mean we have committed or tried to commit in which case we
            // may have entries in the pending and prepared lists and we may have written a log record
            // which we need to update. it also means we can end up leaving a log entry for an aborted
            // transaction with a heuristic outcome

            case ActionStatus.PREPARING:
            case ActionStatus.PREPARED:
            case ActionStatus.COMMITTING:
            {
                super.phase2Abort(true);
                outcome = status();
            }
            break;
            // RUNNING or ABORT_ONLY means we never got to commit so we can just do a normal abort
            case ActionStatus.RUNNING:
            case ActionStatus.ABORT_ONLY:
            {
                outcome = super.Abort();
            }
            break;
            // nothing to do if we are in any other state
            default:
            {
                outcome = status();
            }
            break;
        }

        return outcome;
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

	public void enlistParticipant (RecoverableParticipant act) throws WrongStateException,
			DuplicateParticipantException, InvalidParticipantException,
			SystemException
	{
		if (act == null) throw new InvalidParticipantException();

		AbstractRecord rec = new ParticipantRecord((RecoverableParticipant)act, new Uid());

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
					wscfLogger.log_mesg
							.getString("com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_2"));

        int status = status();
        // exit is only legitimate when the TX is in these states
        switch (status) {
            case ActionStatus.RUNNING:
            case ActionStatus.ABORT_ONLY:
                changeParticipantStatus(participantId, DELISTED);
                break;
            default:
                throw new WrongStateException(
					wscfLogger.log_mesg
							.getString("com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_3"));
        }
    }

	public synchronized void participantCompleted (String participantId)
			throws InvalidParticipantException, WrongStateException,
			SystemException
	{
		if (participantId == null)
			throw new SystemException(
					wscfLogger.log_mesg
							.getString("com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_2"));

        int status = status();
        // completed is only legitimate when the TX is in these states
        switch (status) {
            case ActionStatus.RUNNING:
            case ActionStatus.ABORT_ONLY:
                changeParticipantStatus(participantId, COMPLETED);
                break;
            default:
                throw new WrongStateException(
					wscfLogger.log_mesg
							.getString("com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_3"));
        }
	}

	public synchronized void participantFaulted (String participantId)
			throws InvalidParticipantException, SystemException
	{
		if (participantId == null)
			throw new SystemException(
					wscfLogger.log_mesg
							.getString("com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_2"));

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
					wscfLogger.log_mesg
							.getString("com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_3"));
        }
    }

    // n.b. this is only appropriate for the 1.1 protocol

    public synchronized void participantCannotComplete (String participantId)
            throws InvalidParticipantException, WrongStateException, SystemException
    {
        if (participantId == null)
            throw new SystemException(
                    wscfLogger.log_mesg
                            .getString("com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_2"));

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
                    wscfLogger.log_mesg
                            .getString("com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator_3"));
        }
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
        return "/StateManager/BasicAction/AtomicAction/Sagas/ACCoordinator";
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
