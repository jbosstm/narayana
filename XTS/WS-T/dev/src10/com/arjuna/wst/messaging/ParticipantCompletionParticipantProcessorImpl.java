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
package com.arjuna.wst.messaging;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFault10;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.base.processors.ActivatedObjectProcessor;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.RelationshipType;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.ParticipantCompletionParticipantInboundEvents;
import com.arjuna.webservices.wsba.StatusType;
import com.arjuna.webservices.wsba.client.ParticipantCompletionCoordinatorClient;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.wsc.messaging.MessageId;


/**
 * The Participant Completion Participant processor.
 * @author kevin
 */
public class ParticipantCompletionParticipantProcessorImpl extends ParticipantCompletionParticipantProcessor
{
    /**
     * The activated object processor.
     */
    private final ActivatedObjectProcessor activatedObjectProcessor = new ActivatedObjectProcessor() ;
    
    /**
     * Activate the participant.
     * @param participant The participant.
     * @param identifier The identifier.
     */
    public void activateParticipant(final ParticipantCompletionParticipantInboundEvents participant, final String identifier)
    {
        activatedObjectProcessor.activateObject(participant, identifier) ;
    }

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public void deactivateParticipant(final ParticipantCompletionParticipantInboundEvents participant)
    {
        activatedObjectProcessor.deactivateObject(participant) ;
    }

    /**
     * Check whether a participant with the given id is currently active
     *
     * @param identifier The identifier.
     */
    public boolean isActive(String identifier) {
        return activatedObjectProcessor.getObject(identifier) != null;        
    }

    /**
     * Get the participant with the specified identifier.
     * @param instanceIdentifier The participant identifier.
     * @return The participant or null if not known.
     */
    private ParticipantCompletionParticipantInboundEvents getParticipant(final InstanceIdentifier instanceIdentifier)
    {
        final String identifier = (instanceIdentifier != null ? instanceIdentifier.getInstanceIdentifier() : null) ;
        return (ParticipantCompletionParticipantInboundEvents)activatedObjectProcessor.getObject(identifier) ;
    }

    /**
     * Cancel.
     * @param cancel The cancel notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    public void cancel(final NotificationType cancel, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.cancel(cancel, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.logger.isDebugEnabled())
                {
                    WSTLogger.logger.debugv("Unexpected exception thrown from cancel:", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Cancel called on unknown participant: {0}", new Object[] {instanceIdentifier}) ;
            }
            sendCancelled(addressingContext, arjunaContext) ;
        }
    }

    /**
     * Close.
     * @param close The close notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    public void close(final NotificationType close, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.close(close, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.logger.isDebugEnabled())
                {
                    WSTLogger.logger.debugv("Unexpected exception thrown from close:", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Close called on unknown participant: {0}", new Object[] {instanceIdentifier}) ;
            }
            sendClosed(addressingContext, arjunaContext) ;
        }
    }
    
    /**
     * Compensate.
     * @param compensate The compensate notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    public void compensate(final NotificationType compensate, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.compensate(compensate, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.logger.isDebugEnabled())
                {
                    WSTLogger.logger.debugv("Unexpected exception thrown from compensate:", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Compensate called on unknown participant: {0}", new Object[] {instanceIdentifier}) ;
            }
            sendCompensated(addressingContext, arjunaContext) ;
        }
    }
    
    /**
     * Exited.
     * @param exited The exited notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    public void exited(final NotificationType exited, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.exited(exited, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.logger.isDebugEnabled())
                {
                    WSTLogger.logger.debugv("Unexpected exception thrown from exited:", th) ;
                }
            }
        }
        else if (WSTLogger.logger.isDebugEnabled())
        {
            WSTLogger.logger.debugv("Exited called on unknown participant: {0}", new Object[] {instanceIdentifier}) ;
        }
    }
    
    /**
     * Faulted.
     * @param faulted The faulted notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    public void faulted(final NotificationType faulted, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.faulted(faulted, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.logger.isDebugEnabled())
                {
                    WSTLogger.logger.debugv("Unexpected exception thrown from faulted:", th) ;
                }
            }
        }
        else if (WSTLogger.logger.isDebugEnabled())
        {
            WSTLogger.logger.debugv("Faulted called on unknown participant: {0}", new Object[] {instanceIdentifier}) ;
        }
    }
    
    /**
     * Get Status.
     * @param getStatus The get status notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    public void getStatus(final NotificationType getStatus, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.getStatus(getStatus, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.logger.isDebugEnabled())
                {
                    WSTLogger.logger.debugv("Unexpected exception thrown from getStatus:", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Complete called on unknown participant: {0}", new Object[] {instanceIdentifier}) ;
            }
            // send an invalid state fault

            final String responseMessageId = MessageId.getMessageId() ;
            final AddressingContext responseAddressingContext = AddressingContext.createRequestContext(addressingContext.getFrom(), responseMessageId) ;

            final AttributedURIType requestMessageId = addressingContext.getMessageID() ;
            if (requestMessageId != null)
            {
                responseAddressingContext.addRelatesTo(new RelationshipType(requestMessageId.getValue())) ;
            }
            final String messageId = MessageId.getMessageId();
            final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
            try
            {
                final String message = WSTLogger.i18NLogger.get_messaging_ParticipantCompletionParticipantProcessorImpl_getStatus_4();
                final SoapFault soapFault = new SoapFault10(SoapFaultType.FAULT_SENDER, CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME, message) ;
                ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(responseAddressingContext, soapFault, instanceIdentifier) ;
            }
            catch (final Throwable th) {
                WSTLogger.i18NLogger.info_messaging_ParticipantCompletionParticipantProcessorImpl_getStatus_3(instanceIdentifier.toString(), th);
            }
        }
    }
    
    /**
     * Status.
     * @param status The status type.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    public void status(final StatusType status, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.status(status, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.logger.isDebugEnabled())
                {
                    WSTLogger.logger.debugv("Unexpected exception thrown from status:", th) ;
                }
            }
        }
        else if (WSTLogger.logger.isDebugEnabled())
        {
            WSTLogger.logger.debugv("Status called on unknown participant: {0}", new Object[] {instanceIdentifier}) ;
        }
    }
    
    /**
     * SOAP Fault.
     * @param fault The SOAP fault notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    public void soapFault(final SoapFault fault, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.soapFault(fault, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.logger.isDebugEnabled())
                {
                    WSTLogger.logger.debugv("Unexpected exception thrown from soapFault:", th) ;
                }
            }
        }
        else if (WSTLogger.logger.isDebugEnabled())
        {
            WSTLogger.logger.debugv("SoapFault called on unknown participant: {0}", new Object[] {instanceIdentifier}) ;
        }
    }
    
    /**
     * Send a cancelled message.
     * 
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    private void sendCancelled(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final AddressingContext responseAddressingContext = AddressingContext.createRequestContext(addressingContext.getFrom(), messageId) ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendCancelled(responseAddressingContext, arjunaContext.getInstanceIdentifier()) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Unexpected exception while sending Cancelled", th) ;
            }
        }
    }
    
    /**
     * Send a closed message.
     * 
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    private void sendClosed(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final AddressingContext responseAddressingContext = AddressingContext.createRequestContext(addressingContext.getFrom(), messageId) ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendClosed(responseAddressingContext, arjunaContext.getInstanceIdentifier()) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Unexpected exception while sending Closed", th) ;
            }
        }
    }
    
    /**
     * Send a compensated message.
     * 
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    private void sendCompensated(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final AddressingContext responseAddressingContext = AddressingContext.createRequestContext(addressingContext.getFrom(), messageId) ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendCompensated(responseAddressingContext, arjunaContext.getInstanceIdentifier()) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Unexpected exception while sending Compensated", th) ;
            }
        }
    }
}
