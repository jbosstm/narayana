/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wscf.tests;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wscf.exceptions.InvalidParticipantException;
import com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicCancelException;
import com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicConfirmException;
import com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicHazardException;
import com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicMixedException;
import com.arjuna.mw.wscf.model.twophase.participants.Participant;
import com.arjuna.mw.wscf.model.twophase.vote.Vote;
import com.arjuna.mw.wscf.model.twophase.vote.VoteConfirm;

import java.io.IOException;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TwoPhaseParticipant.java,v 1.3 2005/01/15 21:21:06 kconner Exp $
 * @since 1.0.
 */

public class TwoPhaseParticipant implements Participant
{
    public TwoPhaseParticipant(String id)
    {
	_id = id;
    }

    public Vote prepare () throws InvalidParticipantException, WrongStateException, HeuristicHazardException, HeuristicMixedException, SystemException
    {
	System.out.println("TwoPhaseParticipant.prepare");

	return new VoteConfirm();
    }

    public void confirm () throws InvalidParticipantException, WrongStateException, HeuristicHazardException, HeuristicMixedException, HeuristicCancelException, SystemException
    {
	System.out.println("TwoPhaseParticipant.confirm");
    }

    public void cancel () throws InvalidParticipantException, WrongStateException, HeuristicHazardException, HeuristicMixedException, HeuristicConfirmException, SystemException
    {
	System.out.println("TwoPhaseParticipant.cancel");
    }

    public void confirmOnePhase () throws InvalidParticipantException, WrongStateException, HeuristicHazardException, HeuristicMixedException, HeuristicCancelException, SystemException
    {
	System.out.println("TwoPhaseParticipant.confirmOnePhase");
    }

    public void forget () throws InvalidParticipantException, WrongStateException, SystemException
    {
	System.out.println("TwoPhaseParticipant.forget");
    }

    public String id () throws SystemException
    {
	return _id;
    }

    public boolean save_state(OutputObjectState os)
    {
        try {
            os.packString(_id);
        } catch (IOException ioe) {
            return false;
        }
        return true ;
    }

    public boolean restore_state(InputObjectState os)
    {
        try {
            _id = os.unpackString();
        } catch (IOException e) {
            return false;
        }
        return true ;
    }

    private String _id;

}