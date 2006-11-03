/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
 * $Id: Participant.java,v 1.4 2005/05/19 12:13:24 nmcl Exp $
 */

package com.arjuna.mw.wscf.model.sagas.participants;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.mw.wscf.model.sagas.exceptions.*;

import com.arjuna.mw.wscf.exceptions.*;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;

/**
 * This is the interface that all two-phase aware participants must define.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Participant.java,v 1.4 2005/05/19 12:13:24 nmcl Exp $
 * @since 1.0.
 */

public interface Participant
{

    /**
     * Confirm the participant at top-level.
     *
     * @exception InvalidParticipantException Thrown if the participant identity is invalid
     *            (e.g., refers to an unknown participant.)
     * @exception WrongStateException Thrown if the state of the participant is such that
     *            it cannot confirm.
     * @exception SystemException Thrown if some other error occurred.
     */

    public void close () throws InvalidParticipantException, WrongStateException, SystemException;

    /**
     * Cancel the participant at top-level.
     *
     * @exception InvalidParticipantException Thrown if the participant identity is invalid
     *            (e.g., refers to an unknown participant.)
     * @exception WrongStateException Thrown if the state of the participant is such that
     *            it cannot cancel.
     * @exception SystemException Thrown if some other error occurred.
     */

    public void cancel () throws InvalidParticipantException, WrongStateException, SystemException;

    /**
     * Compensate the participant.
     *
     * @exception InvalidParticipantException Thrown if the participant identity is invalid
     *            (e.g., refers to an unknown participant.)
     * @exception WrongStateException Thrown if the state of the participant is such that
     *            it cannot cancel.
     * @exception SystemException Thrown if some other error occurred.
     */

    public void compensate () throws CompensateFailedException, InvalidParticipantException, WrongStateException, SystemException;

    /**
     * Inform the participant that is can forget the heuristic result.
     *
     * @exception InvalidParticipantException Thrown if the participant identity is invalid.
     * @exception WrongStateException Thrown if the participant is in an invalid state.
     * @exception SystemException Thrown in the event of a general fault.
     */

    public void forget () throws InvalidParticipantException, WrongStateException, SystemException;

    public String id () throws SystemException;
    
    public boolean save_state (OutputObjectState os);
    public boolean restore_state (InputObjectState os);
    
}

