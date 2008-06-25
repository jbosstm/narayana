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
package com.arjuna.webservices11.wsba.processors;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsba.CoordinatorCompletionParticipantInboundEvents;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

import javax.xml.ws.addressing.AddressingProperties;


/**
 * The Coordinator Completion Participant processor.
 * @author kevin
 */
public abstract class CoordinatorCompletionParticipantProcessor
{
    /**
     * The participant processor.
     */
    private static CoordinatorCompletionParticipantProcessor PROCESSOR ;

    /**
     * Get the processor.
     * @return The processor.
     */
    public static synchronized CoordinatorCompletionParticipantProcessor getProcessor()
    {
        return PROCESSOR ;
    }

    /**
     * Set the processor.
     * @param processor The processor.
     * @return The previous processor.
     */
    public static synchronized CoordinatorCompletionParticipantProcessor setProcessor(final CoordinatorCompletionParticipantProcessor processor)
    {
        final CoordinatorCompletionParticipantProcessor origProcessor = PROCESSOR ;
        PROCESSOR = processor ;
        return origProcessor ;
    }

    /**
     * Activate the participant.
     * @param participant The participant.
     * @param identifier The identifier.
     */
    public abstract void activateParticipant(final CoordinatorCompletionParticipantInboundEvents participant, final String identifier) ;

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public abstract void deactivateParticipant(final CoordinatorCompletionParticipantInboundEvents participant) ;

    /**
     * Cancel.
     * @param cancel The cancel notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void cancel(final NotificationType cancel, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Close.
     * @param close The close notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void close(final NotificationType close, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Compensate.
     * @param compensate The compensate notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void compensate(final NotificationType compensate, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Complete.
     * @param complete The complete notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void complete(final NotificationType complete, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Exited.
     * @param exited The exited notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void exited(final NotificationType exited, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Not Completed.
     * @param notCompleted The not completed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void notCompleted(final NotificationType notCompleted, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Failed.
     * @param failed The failed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void failed(final NotificationType failed, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Get Status.
     * @param getStatus The get status notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void getStatus(final NotificationType getStatus, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Status.
     * @param status The status.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void status(final StatusType status, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * SOAP fault.
     * @param soapFault The SOAP fault.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void soapFault(final SoapFault soapFault, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext) ;
}