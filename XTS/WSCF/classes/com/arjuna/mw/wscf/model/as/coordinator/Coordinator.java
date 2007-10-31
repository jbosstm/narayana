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
 * $Id: Coordinator.java,v 1.2 2005/05/19 12:13:21 nmcl Exp $
 */

package com.arjuna.mw.wscf.model.as.coordinator;

import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;

import com.arjuna.mw.wscf.common.Qualifier;

import com.arjuna.mw.wscf.exceptions.InvalidParticipantException;
import com.arjuna.mw.wscf.exceptions.DuplicateParticipantException;

import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;

/**
 * The generic coordinator interface. All coordinator implementations
 * must at least conform to this interface.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Coordinator.java,v 1.2 2005/05/19 12:13:21 nmcl Exp $
 * @since 1.0.
 */

public interface Coordinator
{

    /**
     * Coordinate the participants using the completion status provided.
     * 
     * @exception WrongStateException Thrown if the current activity is not in a
     * state that allows it to be coordinated, or is incompatible with the
     * completion status provided.
     * @exception ProtocolViolationException Thrown if a violation of the
     * activity service or HLS protocol occurs.
     * @exception SystemException Thrown if some other error occurred.
     *
     * @param cs The CompletionStatus to use.
     *
     * @return the result of completing the activity. Null is valid and must
     * be interpreted within the context of any HLS that may exist.
     *
     * @see com.arjuna.mw.wsas.activity.Outcome
     */

    public Outcome coordinate (CompletionStatus cs) throws WrongStateException, ProtocolViolationException, SystemException;

    /**
     * Register the specified participant with the coordinator.
     *
     * @param act The participant to enroll with the coordinator.
     * @param priority If the coordinator allows prioritisation of
     * participants, this priority is used to ensure participant invocations
     * occur in a specific order.
     * @param quals Any qualifiers associated with the participant
     * for this registration.
     *
     * @exception WrongStateException Thrown if the coordinator is not in
     * a state that allows participants to be registered.
     * @exception DuplicateParticipantException Thrown if the participant has
     * already been registered with this coordinator.
     * @exception InvalidParticipantException Thrown if the partcipant is invalid.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void addParticipant (Participant act, int priority, Qualifier[] quals) throws WrongStateException, DuplicateParticipantException, InvalidParticipantException, SystemException;

    /**
     * Unregister the specified participant from the coordinator.
     *
     * @param act The participant to remove.
     *
     * @exception InvalidParticipantException Thrown if the partcipant is not known
     * of by the coordinator.
     * @exception WrongStateException Thrown if the coordinator is not in a
     * state that allows participants to be removed.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void removeParticipant (Participant act) throws InvalidParticipantException, WrongStateException, SystemException;

    /**
     * Some coordination protocol will allow participants to make autonomous
     * decisions based upon their current state and assumptions about which
     * notifications a coordinator may send them.
     *
     * @param id The unique participant name.
     * @param notification The notification that the participant has
     * based the response on. For example, the response is related to a
     * "prepare" notification.
     * @param response The response to the assumed notification.
     * @param quals Any qualifiers associated with the response.
     *
     * @exception WrongStateException Thrown if the coordinator is not in
     * a state that is compatible with the received response.
     * @exception InvalidParticipantException Thrown if the participant is not known
     * to the coordinator.
     * @exception SystemException Thrown if any other error occurs.
     */
    
    public void setResponse (String id, Message notification, Outcome response, Qualifier[] quals) throws InvalidParticipantException, WrongStateException, SystemException;

    /**
     * @exception SystemException Thrown if any error occurs.
     *
     * @return The parent coordinator for this coordinator, if any.
     */

    public Coordinator getParentCoordinator () throws SystemException;

    /**
     * @exception SystemException Thrown if any error occurs.
     *
     * @return The qualifiers associated with all registered participants.
     */

    public Qualifier[] qualifiers () throws SystemException;
    
}

