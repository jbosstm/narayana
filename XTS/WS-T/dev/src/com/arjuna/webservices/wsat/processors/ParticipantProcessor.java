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
package com.arjuna.webservices.wsat.processors;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsat.NotificationType;
import com.arjuna.webservices.wsat.Participant;

/**
 * The Participant processor.
 * @author kevin
 */
public abstract class ParticipantProcessor
{
    /**
     * The participant.
     */
    private static ParticipantProcessor PARTICIPANT ;
    
    /**
     * Get the participant.
     * @return The participant.
     */
    public static synchronized ParticipantProcessor getParticipant()
    {
        return PARTICIPANT ;
    }

    /**
     * Set the participant.
     * @param participant The participant.
     * @return The previous participant.
     */
    public static synchronized ParticipantProcessor setParticipant(final ParticipantProcessor participant)
    {
        final ParticipantProcessor origParticipant = PARTICIPANT ;
        PARTICIPANT = participant ;
        return origParticipant ;
    }
    
    /**
     * Activate the participant.
     * @param participant The participant.
     * @param identifier The identifier.
     */
    public abstract void activateParticipant(final Participant participant, final String identifier) ;

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public abstract void deactivateParticipant(final Participant participant) ;

    /**
     * Commit.
     * @param commit The commit notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void commit(final NotificationType commit, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
    
    /**
     * Prepare.
     * @param prepare The prepare notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void prepare(final NotificationType prepare, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
    
    /**
     * Rollback.
     * @param rollback The rollback notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void rollback(final NotificationType rollback, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;

    /**
     * SOAP Fault.
     * @param soapFault The SOAP fault notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void soapFault(final SoapFault soapFault, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
}
