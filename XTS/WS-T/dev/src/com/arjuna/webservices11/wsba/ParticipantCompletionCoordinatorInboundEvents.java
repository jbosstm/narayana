/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wsba;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import org.jboss.ws.api.addressing.MAP;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.ExceptionType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

/**
 * The Participant events.
 */
public interface ParticipantCompletionCoordinatorInboundEvents
{
    /**
     * Handle the cancelled event.
     * @param cancelled The cancelled notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void cancelled(final NotificationType cancelled, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Handle the closed event.
     * @param closed The closed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void closed(final NotificationType closed, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Handle the compensated event.
     * @param compensated The compensated notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void compensated(final NotificationType compensated, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Handle the completed event.
     * @param completed The completed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void completed(final NotificationType completed, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * Handle the exit event.
     * @param exit The exit notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void exit(final NotificationType exit, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Handle the fail event.
     * @param fail The fail exception.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void fail(final ExceptionType fail, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * Handle the cannotComplete event.
     * @param cannotComplete The cannotComplete notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void cannotComplete(final NotificationType cannotComplete, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * Handle the getStatus event.
     * @param getStatus The getStatus notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void getStatus(final NotificationType getStatus, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Handle the status event.
     * @param status The status.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void status(final StatusType status, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Handle the soap fault event.
     * @param soapFault The soap fault.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void soapFault(final SoapFault soapFault, final MAP map, final ArjunaContext arjunaContext) ;
}