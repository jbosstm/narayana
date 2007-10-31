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
 * Copyright (C) 2002, 2003, 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: CoordinatorManager.java,v 1.4 2005/05/19 12:13:24 nmcl Exp $
 */

package com.arjuna.mw.wscf.model.sagas.api;

import com.arjuna.mw.wscf.model.sagas.participants.*;

import com.arjuna.mw.wscf.exceptions.*;

import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.NoActivityException;

/**
 * The CoordinatorManager is the way in which services can enlist
 * participants with the current coordinator.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: CoordinatorManager.java,v 1.4 2005/05/19 12:13:24 nmcl Exp $
 * @since 1.0.
 */

public interface CoordinatorManager extends UserCoordinator
{

    /**
     * Enrol the specified participant with the coordinator associated with
     * the current thread.
     *
     * @param act The participant.
     *
     * @exception NoActivityException Thrown if there is no activity associated
     * with the current thread.
     * @exception WrongStateException Thrown if the coordinator is not in a
     * state that allows participants to be enrolled.
     * @exception DuplicateParticipantException Thrown if the participant has
     * already been enrolled and the coordination protocol does not support
     * multiple entries.
     * @exception InvalidParticipantException Thrown if the participant is invalid.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void enlistParticipant (Participant act) throws NoActivityException, WrongStateException, DuplicateParticipantException, InvalidParticipantException, SystemException;

    /**
     * Remove the specified participant from the coordinator associated with
     * the current thread.
     *
     * @param participantId The participant to remove.
     *
     * @exception NoActivityException Thrown if there is no activity associated
     * with the current thread.
     * @exception WrongStateException Thrown if the coordinator is not in a
     * state that allows participants to be removed.
     * @exception InvalidParticipantException Thrown if the participant is invalid.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void delistParticipant (String participantId) throws NoActivityException, InvalidParticipantException, WrongStateException, SystemException;

    /**
     * The participant has completed its work and it ready to compensate
     * if necessary.
     *
     * @param participantId The participant.
     *
     * @exception NoActivityException Thrown if there is no activity associated
     * with the current thread.
     * @exception WrongStateException Thrown if the coordinator is not in a
     * state that allows participants to be removed.
     * @exception InvalidParticipantException Thrown if the participant is invalid.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void participantCompleted (String participantId) throws NoActivityException, InvalidParticipantException, WrongStateException, SystemException;

    /**
     * A participant has faulted during normal execution or compensation.
     * The saga will attempt to undo. The WS-T specification is a little
     * vague here - we assume the entire transaction has to undo.
     *
     * @param participantId The participant.
     *
     * @exception NoActivityException Thrown if there is no activity associated
     * with the current thread.
     * @exception InvalidParticipantException Thrown if the participant is invalid.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void participantFaulted (String participantId) throws NoActivityException, InvalidParticipantException, SystemException;
    
}

