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