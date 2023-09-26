/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wsba.processors;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsba.ParticipantCompletionCoordinatorInboundEvents;
import org.jboss.ws.api.addressing.MAP;
import org.oasis_open.docs.ws_tx.wsba._2006._06.ExceptionType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

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
     * Locate a coordinator by name.
     * @param identifier The name of the coordinator.
     */
    public abstract ParticipantCompletionCoordinatorInboundEvents getCoordinator(final String  identifier) ;

    /**
     * Cancelled.
     * @param cancelled The cancelled notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void cancelled(final NotificationType cancelled, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Closed.
     * @param closed The closed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void closed(final NotificationType closed, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Compensated.
     * @param compensated The compensated notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void compensated(final NotificationType compensated, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Completed.
     * @param completed The completed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void completed(final NotificationType completed, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * Completed.
     * @param cannotComplete The cannot complete notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void cannotComplete(final NotificationType cannotComplete, final MAP map, final ArjunaContext arjunaContext);

    /**
     * Exit.
     * @param exit The exit notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void exit(final NotificationType exit, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Fault.
     * @param fail The fault exception.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void fail(final ExceptionType fail, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * Get Status.
     * @param getStatus The get status notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void getStatus(final NotificationType getStatus, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Status.
     * @param status The status.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void status(final StatusType status, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * SOAP fault.
     * @param soapFault The SOAP fault.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void soapFault(final SoapFault soapFault, final MAP map,
        final ArjunaContext arjunaContext) ;
}