/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wsarjtx.processors;

import com.arjuna.schemas.ws._2005._10.wsarjtx.NotificationType;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.base.processors.Callback;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.processor.BaseNotificationProcessor;
import org.jboss.ws.api.addressing.MAP;

/**
 * The Terminator Coordinator processor.
 * @author kevin
 */
public class TerminationParticipantProcessor extends BaseNotificationProcessor
{
    /**
     * The coordinator processor.
     */
    private static final TerminationParticipantProcessor PROCESSOR = new TerminationParticipantProcessor() ;

    /**
     * Get the processor singleton.
     * @return The singleton.
     */
    public static TerminationParticipantProcessor getProcessor()
    {
        return PROCESSOR ;
    }

    /**
     * Handle a cancelled response.
     * @param cancelled The cancelled notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleCancelled(final NotificationType cancelled, final MAP map,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((TerminationParticipantCallback)callback).cancelled(cancelled, map, arjunaContext) ;
            }
        }, getIDs(arjunaContext)) ;
    }

    /**
     * Handle a closed response.
     * @param closed The closed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleClosed(final NotificationType closed, final MAP map,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((TerminationParticipantCallback)callback).closed(closed, map, arjunaContext) ;
            }
        }, getIDs(arjunaContext)) ;
    }

    /**
     * Handle a completed response.
     * @param completed The completed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleCompleted(final NotificationType completed, final MAP map,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((TerminationParticipantCallback)callback).completed(completed, map, arjunaContext) ;
            }
        }, getIDs(arjunaContext)) ;
    }

    /**
     * Handle a faulted response.
     * @param faulted The faulted notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleFaulted(final NotificationType faulted, final MAP map,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((TerminationParticipantCallback)callback).faulted(faulted, map, arjunaContext) ;
            }
        }, getIDs(arjunaContext)) ;
    }

    /**
     * Handle a SOAP fault response.
     * @param soapFault The SOAP fault.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleSoapFault(final SoapFault soapFault, final MAP map,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((TerminationParticipantCallback)callback).soapFault(soapFault, map, arjunaContext) ;
            }
        }, getIDs(arjunaContext)) ;
    }

    /**
     * Register a callback for the specific instance identifier.
     * @param instanceIdentifier The instance identifier.
     * @param callback The callback for the response.
     */
    public void registerCallback(final String instanceIdentifier, final TerminationParticipantCallback callback)
    {
        register(instanceIdentifier, callback) ;
    }
}