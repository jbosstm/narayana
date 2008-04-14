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
package com.arjuna.webservices11.wsba.processors;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsba.ParticipantCompletionCoordinatorInboundEvents;
import org.oasis_open.docs.ws_tx.wsba._2006._06.ExceptionType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

import javax.xml.ws.addressing.AddressingProperties;


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
     * @param coordinator The coordinator.
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
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void cancelled(final NotificationType cancelled, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Closed.
     * @param closed The closed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void closed(final NotificationType closed, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Compensated.
     * @param compensated The compensated notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void compensated(final NotificationType compensated, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Completed.
     * @param completed The completed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void completed(final NotificationType completed, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext) ;

    /**
     * Completed.
     * @param cannotComplete The cannot complete notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void cannotComplete(final NotificationType cannotComplete, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext);

    /**
     * Exit.
     * @param exit The exit notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void exit(final NotificationType exit, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Fault.
     * @param fail The fault exception.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void fail(final ExceptionType fail, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext) ;

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