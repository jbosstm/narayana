/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wsarjtx.processors;

import com.arjuna.schemas.ws._2005._10.wsarjtx.NotificationType;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.base.processors.Callback;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import org.jboss.ws.api.addressing.MAP;

/**
 * The Terminator Coordinator callback.
 * @author kevin
 */
public abstract class TerminationParticipantCallback extends Callback
{
    /**
     * A cancelled response.
     * @param cancelled The cancelled notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void cancelled(final NotificationType cancelled, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * A closed response.
     * @param closed The closed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void closed(final NotificationType closed, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * A completed response.
     * @param completed The completed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void completed(final NotificationType completed, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * A faulted response.
     * @param faulted The faulted notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void faulted(final NotificationType faulted, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * A SOAP fault response.
     * @param soapFault The SOAP fault.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void soapFault(final SoapFault soapFault, final MAP map,
        final ArjunaContext arjunaContext) ;
}