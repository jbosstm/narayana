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
import com.arjuna.webservices.wsba.ExceptionType;
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.ParticipantCompletionCoordinatorInboundEvents;
import com.arjuna.webservices.wsba.StatusType;


/**
 * The Participant Completion Coordinator processor.
 * @author kevin
 */
public abstract class ParticipantCompletionCoordinatorProcessor
{
    /**
     * The coordinator processor.
     */
    private static ParticipantCompletionCoordinatorProcessor PROCESSOR ;
    
    /**
     * Get the processor.
     * @return The processor.
     */
    public static synchronized ParticipantCompletionCoordinatorProcessor getProcessor()
    {
        return PROCESSOR ;
    }
    
    /**
     * Set the processor.
     * @param processor The processor.
     * @return The previous processor.
     */
    public static synchronized ParticipantCompletionCoordinatorProcessor setProcessor(final ParticipantCompletionCoordinatorProcessor processor)
    {
        final ParticipantCompletionCoordinatorProcessor origProcessor = PROCESSOR ;
        PROCESSOR = processor ;
        return origProcessor ;
    }
    
    /**
     * Activate the coordinator.
     * @param coordinatorState The coordinator.
     * @param identifier The identifier.
     */
    public abstract void activateCoordinator(final ParticipantCompletionCoordinatorInboundEvents coordinator, final String identifier) ;

    /**
     * Deactivate the coordinator.
     * @param coordinator The coordinator.
     */
    public abstract void deactivateCoordinator(final ParticipantCompletionCoordinatorInboundEvents coordinator) ;
    
    /**
     * Cancelled.
     * @param cancelled The cancelled notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void cancelled(final NotificationType cancelled, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Closed.
     * @param closed The closed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void closed(final NotificationType closed, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Compensated.
     * @param compensated The compensated notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void compensated(final NotificationType compensated, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;

    /**
     * Completed.
     * @param completed The completed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void completed(final NotificationType completed, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
    
    /**
     * Exit.
     * @param exit The exit notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void exit(final NotificationType exit, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Fault.
     * @param fault The fault exception.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void fault(final ExceptionType fault, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
    
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
