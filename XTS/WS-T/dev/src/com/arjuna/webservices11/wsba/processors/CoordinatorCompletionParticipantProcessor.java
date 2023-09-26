/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wsba.processors;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsba.CoordinatorCompletionParticipantInboundEvents;
import org.jboss.ws.api.addressing.MAP;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

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
     * Check whether a participant with the given id is currently active
     * @param identifier The identifier.
     */
    public abstract boolean isActive(final String identifier) ;

    /**
     * Cancel.
     * @param cancel The cancel notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void cancel(final NotificationType cancel, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Close.
     * @param close The close notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void close(final NotificationType close, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Compensate.
     * @param compensate The compensate notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void compensate(final NotificationType compensate, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Complete.
     * @param complete The complete notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void complete(final NotificationType complete, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Exited.
     * @param exited The exited notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void exited(final NotificationType exited, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Not Completed.
     * @param notCompleted The not completed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void notCompleted(final NotificationType notCompleted, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Failed.
     * @param failed The failed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void failed(final NotificationType failed, final MAP map, final ArjunaContext arjunaContext) ;

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