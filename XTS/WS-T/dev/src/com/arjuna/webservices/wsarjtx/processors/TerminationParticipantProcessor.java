/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
package com.arjuna.webservices.wsarjtx.processors;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.base.processors.Callback;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.processor.BaseNotificationProcessor;
import com.arjuna.webservices.wsat.NotificationType;

/**
 * The Terminator Coordinator processor.
 * @author kevin
 */
public class TerminationParticipantProcessor extends BaseNotificationProcessor
{
    /**
     * The coordinator singleton.
     */
    private static final TerminationParticipantProcessor COORDINATOR = new TerminationParticipantProcessor() ;
    
    /**
     * Get the coordinator singleton.
     * @return The singleton.
     */
    public static TerminationParticipantProcessor getCoordinator()
    {
        return COORDINATOR ;
    }

    /**
     * Handle a cancelled response.
     * @param cancelled The cancelled notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleCancelled(final NotificationType cancelled, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((TerminationParticipantCallback)callback).cancelled(cancelled, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }
    
    /**
     * Handle a closed response.
     * @param closed The closed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleClosed(final NotificationType closed, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((TerminationParticipantCallback)callback).closed(closed, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }
    
    /**
     * Handle a completed response.
     * @param completed The completed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleCompleted(final NotificationType completed, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((TerminationParticipantCallback)callback).completed(completed, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }
    
    /**
     * Handle a faulted response.
     * @param faulted The faulted notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleFaulted(final NotificationType faulted, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((TerminationParticipantCallback)callback).faulted(faulted, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }
    
    /**
     * Handle a SOAP fault response.
     * @param soapFault The SOAP fault.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleSoapFault(final SoapFault soapFault, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((TerminationParticipantCallback)callback).soapFault(soapFault, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
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
