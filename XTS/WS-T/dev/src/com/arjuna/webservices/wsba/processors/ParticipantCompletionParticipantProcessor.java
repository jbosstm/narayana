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
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.StatusType;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;


/**
 * The Participant Completion Participant processor.
 * @author kevin
 */
public abstract class ParticipantCompletionParticipantProcessor extends BaseNotificationProcessor
{
    /**
     * The participant.
     */
    private static ParticipantCompletionParticipantProcessor PARTICIPANT ;
    
    /**
     * Get the participant.
     * @return The participant.
     */
    public static synchronized ParticipantCompletionParticipantProcessor getParticipant()
    {
        return PARTICIPANT ;
    }
    
    /**
     * Set the participant.
     * @param participant The participant.
     * @return The previous participant.
     */
    public static synchronized ParticipantCompletionParticipantProcessor setParticipant(final ParticipantCompletionParticipantProcessor participant)
    {
        final ParticipantCompletionParticipantProcessor origParticipant = PARTICIPANT ;
        PARTICIPANT = participant ;
        return origParticipant ;
    }
    
    /**
     * Activate the participant.
     * @param participant The participant.
     * @param identifier The identifier.
     */
    public abstract void activateParticipant(final BusinessAgreementWithParticipantCompletionParticipant participant, final String identifier) ;

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public abstract void deactivateParticipant(final BusinessAgreementWithParticipantCompletionParticipant participant) ;
    
    /**
     * Cancel.
     * @param cancel The cancel notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void cancel(final NotificationType cancel, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;

    /**
     * Close.
     * @param close The close notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void close(final NotificationType close, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Compensate.
     * @param compensate The compensate notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void compensate(final NotificationType compensate, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Get Status.
     * @param getStatus The get status notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void getStatus(final NotificationType getStatus, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    
    /**
     * Handle a complete response.
     * @param complete The complete notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleComplete(final NotificationType complete, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((ParticipantCompletionParticipantCallback)callback).complete(complete, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }
    
    /**
     * Handle an exited response.
     * @param exited The exited notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleExited(final NotificationType exited, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((ParticipantCompletionParticipantCallback)callback).exited(exited, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }
    
    /**
     * Handle a faulted response.
     * @param faulted The faulted notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleFaulted(final NotificationType faulted, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((ParticipantCompletionParticipantCallback)callback).faulted(faulted, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }
    
    /**
     * Handle a status response.
     * @param status The status notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleStatus(final StatusType status, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((ParticipantCompletionParticipantCallback)callback).status(status, addressingContext, arjunaContext) ;
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
                ((ParticipantCompletionParticipantCallback)callback).soapFault(soapFault, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }

    /**
     * Register a callback for the specific instance identifier.
     * @param instanceIdentifier The instance identifier.
     * @param callback The callback for the response.
     */
    public void registerCallback(final String instanceIdentifier, final ParticipantCompletionParticipantCallback callback)
    {
        register(instanceIdentifier, callback) ;
    }
}
