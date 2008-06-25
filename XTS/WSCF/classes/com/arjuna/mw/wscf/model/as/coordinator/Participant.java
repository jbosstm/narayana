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
 * $Id: Participant.java,v 1.4 2005/05/19 12:13:21 nmcl Exp $
 */

package com.arjuna.mw.wscf.model.as.coordinator;

import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.state.InputObjectState;

import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;

/**
 * The Participant represents the participant interface. Because WSCF is a generic
 * coordination framework, the basic participant is obviously generic.
 * Specific participants bound to specific coordination types can be wrapped
 * by this interface, thus hiding it from users.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Participant.java,v 1.4 2005/05/19 12:13:21 nmcl Exp $
 * @since 1.0.
 */

public interface Participant
{

    /**
     * @exception SystemException Thrown if any error occurs.
     *
     * @return the unique identity for this participant.
     */

    public String identity () throws SystemException;


    /**
     * Process the message from the coordinator and return a response, which
     * may be null - validity is down to the specific coordinator.
     *
     * @param notification The message to be processed.
     *
     * @exception WrongStateException Thrown if the participant is not in a
     * state which is valid given the input message.
     * @exception ProtocolViolationException Thrown if the participant has
     * violated the coordination protocol.
     * @exception SystemException Thrown if any other error occurs.
     * 
     * @return the Outcome message representing the result of dealing with
     * the notification. Null may be a valid response.
     */

    public Outcome processMessage (Message notification) throws WrongStateException, ProtocolViolationException, SystemException;

    /*
     * These methods are required so that the coordinator can serialise and
     * de-serialise information about the inferior during completion and
     * recovery.
     */

    /**
     * Pack the state of the participant into the object buffer.
     *
     * @return <code>true</code> if successful, <code>false</code> otherwise.
     */

    public boolean packState (OutputObjectState os);

    /**
     * Unpack the state of the participant from the object buffer.
     *
     * @return <code>true</code> if successful, <code>false</code> otherwise.
     */

    public boolean unpackState (InputObjectState os);
    
}

