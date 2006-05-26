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
 * $Id: CoordinatorManagerImple.java,v 1.3 2005/05/19 12:13:32 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.model.as;

import com.arjuna.mw.wscf.common.*;

import com.arjuna.mw.wscf.model.as.coordinator.*;

import com.arjuna.mwlabs.wsas.activity.ActivityImple;

import com.arjuna.mwlabs.wsas.UserActivityImple;

import com.arjuna.mw.wsas.UserActivityFactory;

import com.arjuna.mw.wsas.activity.*;

import com.arjuna.mw.wscf.model.as.CoordinatorManager;

import com.arjuna.mw.wscf.model.as.coordinator.CoordinatorManagerService;

import com.arjuna.mw.wscf.exceptions.*;

import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.NoActivityException;
import com.arjuna.mw.wsas.exceptions.HLSError;

/**
 * The coordination manager implementation. This provides users with the
 * ability to enroll participants with coordinators and to remove them (where
 * supported by the protocol definition.)
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: CoordinatorManagerImple.java,v 1.3 2005/05/19 12:13:32 nmcl Exp $
 * @since 1.0.
 */

public class CoordinatorManagerImple implements CoordinatorManager
{

    public CoordinatorManagerImple (Object type)
    {
	try
	{
	    if (type instanceof String)
	    {
		Class c = Class.forName((String) type);
		
		_theCoordinatorService = (CoordinatorManagerService) c.newInstance();
	    }
	    else
		_theCoordinatorService = (CoordinatorManagerService) type;
	}
	catch (Exception ex)
	{
	    throw new HLSError(ex.toString());
	}
    }

    /**
     * Enrol the specified participant with the coordinator associated with
     * the current thread. If the coordinator supports a priority ordering
     * of participants, then that ordering can also be specified. Any
     * qualifiers that are to be associated with the participant are also
     * provided
     *
     * @param Participant act The participant.
     * @param int priority The priority to associate with the participant in
     * the coordinator's list.
     * @param Qualifier[] quals Any qualifiers to be associated with the
     * participant.
     *
     * @exception NoActivityException Thrown if there is no activity associated
     * with the current thread.
     * @exception WrongStateException Thrown if the coordinator is not in a
     * state that allows participants to be enrolled.
     * @exception InvalidParticipantException Thrown if the participant is invalid.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void addParticipant (Participant act, int priority, Qualifier[] quals) throws NoActivityException, WrongStateException, DuplicateParticipantException, InvalidParticipantException, SystemException
    {
	if (current() == null)
	    throw new NoActivityException();

	_theCoordinatorService.addParticipant(act, priority, quals);
    }

    /**
     * Remove the specified participant from the coordinator associated with
     * the current thread.
     *
     * @param Participant act The participant to remove.
     *
     * @exception NoActivityException Thrown if there is no activity associated
     * with the current thread.
     * @exception WrongStateException Thrown if the coordinator is not in a
     * state that allows participants to be removed.
     * @exception InvalidParticipantException Thrown if the participant is invalid.
     * @exception SystemException Thrown if any other error occurs.
     */
    
    public void removeParticipant (Participant act) throws NoActivityException, InvalidParticipantException, WrongStateException, SystemException
    {
	if (current() == null)
	    throw new NoActivityException();

	_theCoordinatorService.removeParticipant(act);
    }

    /**
     * Some coordination protocol messages may have asynchronous responses
     * or it may be possible for participants to autonomously generate
     * responses to messages that have not yet been producted by the
     * coordinator. As such, this method allows a response from a participant
     * to be passed to the coordinator. In order to ensure that the protocol
     * remains valid, it is necessary for the participant to specify what
     * message produced the response: if the response was autonomously
     * generated by the participant on the assumption it would receive this
     * message from the coordinator and the coordinator subsequently decides
     * not to produce such a message, then the action taken by the participant
     * is invalid and hence so is the response.
     *
     * @param String id the unique participant identification.
     * @param Message notification the message the participant got/assumed
     * when producing the response.
     * @param Outcome response the actual response.
     * @param Qualifier[] quals any qualifiers associated with the response.
     *
     * @exception InvalidParticipantException Thrown if the coordinator has no
     * knowledge of the participant.
     * @exception WrongStateException Thrown if the coordinator is in a state
     * that does not allow it to accept responses at all or this specific
     * type of response.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void setResponse (String id, Message notification, Outcome response, Qualifier[] quals) throws InvalidParticipantException, NoCoordinatorException, WrongStateException, SystemException
    {
	if (current() == null)
	    throw new NoCoordinatorException();

	_theCoordinatorService.setResponse(id, notification, response, quals);
    }
    
    private final ActivityImple current ()
    {
	UserActivityImple imple = (UserActivityImple) UserActivityFactory.userActivity();
	
	return imple.current();
    }
    
    private CoordinatorManagerService _theCoordinatorService;
    
}

