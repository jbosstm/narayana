/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.sagas.participants;

import com.arjuna.mw.wscf.exceptions.*;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;

/**
 * This is the interface that all two-phase aware participants must define.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ParticipantWithComplete.java,v 1.3 2005/05/19 12:13:24 nmcl Exp $
 * @since 1.0.
 */

public interface ParticipantWithComplete extends Participant
{

    /**
     * Complete the participant.
     *
     * @exception InvalidParticipantException Thrown if the participant identity is invalid
     *            (e.g., refers to an unknown participant.)
     * @exception WrongStateException Thrown if the state of the participant is such that
     *            it cannot cancel.
     * @exception SystemException Thrown if some other error occurred.
     */

    public void complete () throws InvalidParticipantException, WrongStateException, SystemException;

}