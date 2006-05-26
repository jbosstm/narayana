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
package com.arjuna.webservices.wsba.processors;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.base.processors.Callback;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.processor.BaseNotificationProcessor;
import com.arjuna.webservices.wsba.ExceptionType;
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.StatusType;
import com.arjuna.wst.BAParticipantManager;


/**
 * The Coordinator Completion Coordinator processor.
 * @author kevin
 */
public abstract class CoordinatorCompletionCoordinatorProcessor extends BaseNotificationProcessor
{
    /**
     * The coordinator.
     */
    private static CoordinatorCompletionCoordinatorProcessor COORDINATOR ;
    
    /**
     * Get the coordinator.
     * @return The singleton.
     */
    public static synchronized CoordinatorCompletionCoordinatorProcessor getCoordinator()
    {
        return COORDINATOR ;
    }
    
    /**
     * Set the coordinator.
     * @param coordinator The coordinator.
     * @return The previous coordinator.
     */
    public static synchronized CoordinatorCompletionCoordinatorProcessor setCoordinator(final CoordinatorCompletionCoordinatorProcessor coordinator)
    {
        final CoordinatorCompletionCoordinatorProcessor origCoordinator = COORDINATOR ;
        COORDINATOR = coordinator ;
        return origCoordinator ;
    }
    
    /**
     * Activate the participant.
     * @param participant The participant.
     * @param identifier The identifier.
     */
    public abstract void activateParticipant(final BAParticipantManager participant, final String identifier) ;

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public abstract void deactivateParticipant(final BAParticipantManager participant) ;
    
    /**
     * Handle a cancelled response.
     * @param cancelled The cancelled notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleCancelled(final NotificationType cancelled, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((CoordinatorCompletionCoordinatorCallback)callback).cancelled(cancelled, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }
    
    /**
     * Handle a closed response.
     * @param closed The closed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleClosed(final NotificationType closed, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((CoordinatorCompletionCoordinatorCallback)callback).closed(closed, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }
    
    /**
     * Handle a compensated response.
     * @param compensated The compensated notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleCompensated(final NotificationType compensated, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((CoordinatorCompletionCoordinatorCallback)callback).compensated(compensated, addressingContext, arjunaContext) ;
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
                ((CoordinatorCompletionCoordinatorCallback)callback).completed(completed, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }
    
    /**
     * Handle a Status.
     * @param status The status notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleStatus(final StatusType status, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((CoordinatorCompletionCoordinatorCallback)callback).status(status, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }
    
    /**
     * Handle a fault response.
     * @param fault The fault notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleFault(final ExceptionType fault, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((CoordinatorCompletionCoordinatorCallback)callback).fault(fault, addressingContext, arjunaContext) ;
            }
            public void executeUnknownIds(final String[] ids) {
                fault(fault, addressingContext, arjunaContext) ;
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
                ((CoordinatorCompletionCoordinatorCallback)callback).soapFault(soapFault, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }
    
    /**
     * Exit.
     * @param exit The exit notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void exit(final NotificationType exit, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;

    /**
     * Fault.
     * @param fault The fault notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void fault(final ExceptionType fault, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;

    /**
     * Get Status.
     * @param getStatus The get status notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void getStatus(final NotificationType getStatus, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;

    /**
     * Register a callback for the specific instance identifier.
     * @param instanceIdentifier The instance identifier.
     * @param callback The callback for the response.
     */
    public void registerCallback(final String instanceIdentifier, final CoordinatorCompletionCoordinatorCallback callback)
    {
        register(instanceIdentifier, callback) ;
    }
}
