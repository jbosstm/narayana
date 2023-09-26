/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wsba;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import org.jboss.ws.api.addressing.MAP;

import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;


/**
 * The Participant events.
 */
public interface CoordinatorCompletionParticipantInboundEvents
{
    /**
     * Handle the cancel event.
     * @param cancel The cancel notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void cancel(final NotificationType cancel, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Handle the close event.
     * @param close The close notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void close(final NotificationType close, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Handle the compensate event.
     * @param compensate The compensate notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void compensate(final NotificationType compensate, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Handle the complete event.
     * @param complete The complete notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void complete(final NotificationType complete, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Handle the exited event.
     * @param exited The exited notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void exited(final NotificationType exited, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Handle the failed event.
     * @param failed The failed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void failed(final NotificationType failed, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Handle the not completed event.
     * @param notCompleted The not completed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void notCompleted(final NotificationType notCompleted, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Handle the getStatus event.
     * @param getStatus The getStatus notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void getStatus(final NotificationType getStatus, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Handle the status event.
     * @param status The status type.
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