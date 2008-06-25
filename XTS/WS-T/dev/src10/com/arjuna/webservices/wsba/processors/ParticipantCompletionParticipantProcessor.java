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
package com.arjuna.webservices.wsba.processors;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.ParticipantCompletionParticipantInboundEvents;
import com.arjuna.webservices.wsba.StatusType;


/**
 * The Participant Completion Participant processor.
 * @author kevin
 */
public abstract class ParticipantCompletionParticipantProcessor
{
    /**
     * The participant processor.
     */
    private static ParticipantCompletionParticipantProcessor PROCESSOR ;
    
    /**
     * Get the processor.
     * @return The processor.
     */
    public static synchronized ParticipantCompletionParticipantProcessor getProcessor()
    {
        return PROCESSOR ;
    }
    
    /**
     * Set the processor.
     * @param processor The processor.
     * @return The previous processor.
     */
    public static synchronized ParticipantCompletionParticipantProcessor setProcessor(final ParticipantCompletionParticipantProcessor processor)
    {
        final ParticipantCompletionParticipantProcessor origProcessor = PROCESSOR ;
        PROCESSOR = processor ;
        return origProcessor ;
    }
    
    /**
     * Activate the participant.
     * @param participant The participant.
     * @param identifier The identifier.
     */
    public abstract void activateParticipant(final ParticipantCompletionParticipantInboundEvents participant, final String identifier) ;

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public abstract void deactivateParticipant(final ParticipantCompletionParticipantInboundEvents participant) ;
    
    /**
     * Cancel.
     * @param cancel The cancel notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void cancel(final NotificationType cancel, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Close.
     * @param close The close notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void close(final NotificationType close, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Compensate.
     * @param compensate The compensate notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void compensate(final NotificationType compensate, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Exited.
     * @param exited The exited notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void exited(final NotificationType exited, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Faulted.
     * @param faulted The faulted notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void faulted(final NotificationType faulted, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Get Status.
     * @param getStatus The get status notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void getStatus(final NotificationType getStatus, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Status.
     * @param status The status.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void status(final StatusType status, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * SOAP fault.
     * @param soapFault The SOAP fault.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void soapFault(final SoapFault soapFault, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
}
