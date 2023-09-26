/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.twophase.participants;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.mw.wscf.model.twophase.vote.Vote;
import com.arjuna.mw.wscf.model.twophase.exceptions.*;

import com.arjuna.mw.wscf.exceptions.*;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;

/**
 * This is the interface that all two-phase aware participants must define.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Participant.java,v 1.3 2005/05/19 12:13:26 nmcl Exp $
 * @since 1.0.
 */

// TODO the nested transaction methods

public interface Participant
{

    /**
     * Prepare the participant for top-level.
     *
     * @exception InvalidParticipantException Thrown if the participant identity is invalid
     *            (e.g., refers to an unknown participant.)
     * @exception WrongStateException Thrown if the state of the participant is such that
     *            it cannot prepare.
     * @exception HeuristicHazardException Thrown if upon preparing, the participant finds that
     *            some of its enlisted participants have return statuses which
     *            mean it cannot determine what the result of issuing prepare
     *            to them has been.
     * @exception HeuristicMixedException Thrown if upon preparing, the participant finds that
     *            some of its enlisted participants have return statuses which
     *            mean some of them cancelled and some of them confirmed.
     * @exception SystemException Thrown if some other error occurred.
     *
     * @return the vote.
     */
    
    public Vote prepare () throws InvalidParticipantException, WrongStateException, HeuristicHazardException, HeuristicMixedException, SystemException;

    /**
     * Confirm the participant at top-level.
     *
     * @exception InvalidParticipantException Thrown if the participant identity is invalid
     *            (e.g., refers to an unknown participant.)
     * @exception WrongStateException Thrown if the state of the participant is such that
     *            it cannot confirm.
     * @exception HeuristicHazardException Thrown if upon preparing, the participant finds that
     *            some of its enlisted participants have return statuses which
     *            mean it cannot determine what the result of issuing confirm
     *            to them has been.
     * @exception HeuristicMixedException Thrown if upon preparing, the participant finds that
     *            some of its enlisted participants have return statuses which
     *            mean some of them cancelled and some of them confirmed.
     * @exception HeuristicCancelException Thrown if the participant rolls
     * back rather than commits.
     * @exception SystemException Thrown if some other error occurred.
     */

    public void confirm () throws InvalidParticipantException, WrongStateException, HeuristicHazardException, HeuristicMixedException, HeuristicCancelException, SystemException;

    /**
     * Cancel the participant at top-level.
     *
     * @exception InvalidParticipantException Thrown if the participant identity is invalid
     *            (e.g., refers to an unknown participant.)
     * @exception WrongStateException Thrown if the state of the participant is such that
     *            it cannot cancel.
     * @exception HeuristicHazardException Thrown if upon preparing, the participant finds that
     *            some of its enlisted participants have return statuses which
     *            mean it cannot determine what the result of issuing cancel
     *            to them has been.
     * @exception HeuristicMixedException Thrown if upon preparing, the participant finds that
     *            some of its enlisted participants have return statuses which
     *            mean some of them cancelled and some of them confirmed.
     * @exception HeuristicConfirmException Thrown if the participant commits
     * rather than rolls back.
     * @exception SystemException Thrown if some other error occurred.
     */

    public void cancel () throws InvalidParticipantException, WrongStateException, HeuristicHazardException, HeuristicMixedException, HeuristicConfirmException, SystemException;

    /**
     * Confirm the participant in a single phase.
     *
     * @exception InvalidParticipantException Thrown if the participant identity is invalid
     *            (e.g., refers to an unknown participant.)
     * @exception WrongStateException Thrown if the state of the participant is such that
     *            it cannot cancel.
     * @exception HeuristicHazardException Thrown if upon preparing, the participant finds that
     *            some of its enlisted participants have return statuses which
     *            mean it cannot determine what the result of issuing cancel
     *            to them has been.
     * @exception HeuristicMixedException Thrown if upon preparing, the participant finds that
     *            some of its enlisted participants have return statuses which
     *            mean some of them cancelled and some of them confirmed.
     * @exception HeuristicCancelException Thrown if the participant rolls
     * back rather than commit.
     * @exception SystemException Thrown if some other error occurred.
     */

    public void confirmOnePhase () throws InvalidParticipantException, WrongStateException, HeuristicHazardException, HeuristicMixedException, HeuristicCancelException, SystemException;

    /**
     * Inform the participant that is can forget the heuristic result.
     *
     * @exception InvalidParticipantException Thrown if the participant identity is invalid.
     * @exception WrongStateException Thrown if the participant is in an invalid state.
     * @exception SystemException Thrown in the event of a general fault.
     */

    public void forget () throws InvalidParticipantException, WrongStateException, SystemException;

    public String id () throws SystemException;
    
    // recovery information
    
    public boolean save_state (OutputObjectState os);
    public boolean restore_state (InputObjectState os);

}