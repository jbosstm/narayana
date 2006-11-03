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
package com.arjuna.wst.messaging;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.base.processors.ActivatedObjectProcessor;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.wsaddr.AddressingContext;
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
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.cancel_1 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.cancel_1] - Unexpected exception thrown from cancel:
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.cancel_2 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.cancel_2] - Cancel called on unknown participant: {0}
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
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.cancel_1", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.cancel_2", new Object[] {instanceIdentifier}) ;
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
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.close_1 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.close_1] - Unexpected exception thrown from close:
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.close_2 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.close_2] - Close called on unknown participant: {0}
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
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.close_1", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.close_2", new Object[] {instanceIdentifier}) ;
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
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.compensate_1 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.compensate_1] - Unexpected exception thrown from compensate:
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.compensate_2 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.compensate_2] - Compensate called on unknown participant: {0}
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
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.compensate_1", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.compensate_2", new Object[] {instanceIdentifier}) ;
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
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.exited_1 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.exited_1] - Unexpected exception thrown from exited:
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.exited_2 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.exited_2] - Exited called on unknown participant: {0}
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
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.exited_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.exited_2", new Object[] {instanceIdentifier}) ;
        }
    }
    
    /**
     * Faulted.
     * @param faulted The faulted notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.faulted_1 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.faulted_1] - Unexpected exception thrown from faulted:
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.faulted_2 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.faulted_2] - Faulted called on unknown participant: {0}
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
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.faulted_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.faulted_2", new Object[] {instanceIdentifier}) ;
        }
    }
    
    /**
     * Get Status.
     * @param getStatus The get status notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.getStatus_1 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.getStatus_1] - Unexpected exception thrown from getStatus:
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.getStatus_2 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.getStatus_2] - Complete called on unknown participant: {0}
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
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.getStatus_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.getStatus_2", new Object[] {instanceIdentifier}) ;
        }
    }
    
    /**
     * Status.
     * @param status The status type.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.status_1 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.status_1] - Unexpected exception thrown from status:
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.status_2 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.status_2] - Status called on unknown participant: {0}
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
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.status_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.status_2", new Object[] {instanceIdentifier}) ;
        }
    }
    
    /**
     * SOAP Fault.
     * @param soapFault The SOAP fault notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.soapFault_1 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.soapFault_1] - Unexpected exception thrown from soapFault:
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.soapFault_2 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.soapFault_2] - SoapFault called on unknown participant: {0}
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
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.soapFault_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.soapFault_2", new Object[] {instanceIdentifier}) ;
        }
    }
    
    /**
     * Send a cancelled message.
     * 
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.sendCancelled_1 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.sendCancelled_1] - Unexpected exception while sending Cancelled
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
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.sendCancelled_1", th) ;
            }
        }
    }
    
    /**
     * Send a closed message.
     * 
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.sendClosed_1 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.sendClosed_1] - Unexpected exception while sending Closed
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
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.sendClosed_1", th) ;
            }
        }
    }
    
    /**
     * Send a compensated message.
     * 
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.sendCompensated_1 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.sendCompensated_1] - Unexpected exception while sending Compensated
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
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl.sendCompensated_1", th) ;
            }
        }
    }
}
