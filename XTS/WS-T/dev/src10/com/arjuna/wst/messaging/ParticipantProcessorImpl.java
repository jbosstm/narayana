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
import com.arjuna.webservices.base.processors.ActivatedObjectProcessor;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsat.NotificationType;
import com.arjuna.webservices.wsat.ParticipantInboundEvents;
import com.arjuna.webservices.wsat.client.CoordinatorClient;
import com.arjuna.webservices.wsat.processors.ParticipantProcessor;
import com.arjuna.wsc.messaging.MessageId;

/**
 * The Participant processor.
 * @author kevin
 */
public class ParticipantProcessorImpl extends ParticipantProcessor
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
    public void activateParticipant(final ParticipantInboundEvents participant, final String identifier)
    {
        activatedObjectProcessor.activateObject(participant, identifier) ;
    }

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public void deactivateParticipant(final ParticipantInboundEvents participant)
    {
        activatedObjectProcessor.deactivateObject(participant) ;
    }
    
    /**
     * Get the participant with the specified identifier.
     * @param instanceIdentifier The participant identifier.
     * @return The participant or null if not known.
     */
    private ParticipantInboundEvents getParticipant(final InstanceIdentifier instanceIdentifier)
    {
        final String identifier = (instanceIdentifier != null ? instanceIdentifier.getInstanceIdentifier() : null) ;
        return (ParticipantInboundEvents)activatedObjectProcessor.getObject(identifier) ;
    }

    /**
     * Commit.
     * @param commit The commit notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantProcessorImpl.commit_1 [com.arjuna.wst.messaging.ParticipantProcessorImpl.commit_1] - Unexpected exception thrown from commit:
     * @message com.arjuna.wst.messaging.ParticipantProcessorImpl.commit_2 [com.arjuna.wst.messaging.ParticipantProcessorImpl.commit_2] - Commit called on unknown participant: {0}
     */
    public void commit(final NotificationType commit, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;
        
        if (participant != null)
        {
            try
            {
                participant.commit(commit, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                {
                    WSTLogger.arjLoggerI18N.warn("com.arjuna.wst.messaging.ParticipantProcessorImpl.commit_1", th) ; 
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isWarnEnabled())
            {
                WSTLogger.arjLoggerI18N.warn("com.arjuna.wst.messaging.ParticipantProcessorImpl.commit_2", new Object[] {instanceIdentifier}) ; 
            }
            sendCommitted(addressingContext, arjunaContext) ;
        }
    }
    
    /**
     * Prepare.
     * @param prepare The prepare notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantProcessorImpl.prepare_1 [com.arjuna.wst.messaging.ParticipantProcessorImpl.prepare_1] - Unexpected exception thrown from prepare: 
     * @message com.arjuna.wst.messaging.ParticipantProcessorImpl.prepare_2 [com.arjuna.wst.messaging.ParticipantProcessorImpl.prepare_2] - Prepare called on unknown participant: {0}
     */
    public void prepare(final NotificationType prepare, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.prepare(prepare, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                {
                    WSTLogger.arjLoggerI18N.warn("com.arjuna.wst.messaging.ParticipantProcessorImpl.prepare_1", th) ; 
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isWarnEnabled())
            {
                WSTLogger.arjLoggerI18N.warn("com.arjuna.wst.messaging.ParticipantProcessorImpl.prepare_2", new Object[] {instanceIdentifier}) ;
            }
            sendAborted(addressingContext, arjunaContext) ;
        }
    }
    
    /**
     * Rollback.
     * @param rollback The rollback notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantProcessorImpl.rollback_1 [com.arjuna.wst.messaging.ParticipantProcessorImpl.rollback_1] - Unexpected exception thrown from rollback: 
     * @message com.arjuna.wst.messaging.ParticipantProcessorImpl.rollback_2 [com.arjuna.wst.messaging.ParticipantProcessorImpl.rollback_2] - Rollback called on unknown participant: {0}
     */
    public void rollback(final NotificationType rollback, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.rollback(rollback, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                {
                    WSTLogger.arjLoggerI18N.warn("com.arjuna.wst.messaging.ParticipantProcessorImpl.rollback_1", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isWarnEnabled())
            {
                WSTLogger.arjLoggerI18N.warn("com.arjuna.wst.messaging.ParticipantProcessorImpl.rollback_2", new Object[] {instanceIdentifier}) ;
            }
            sendAborted(addressingContext, arjunaContext) ;
        }
    }

    /**
     * SOAP Fault.
     * @param fault The SOAP fault notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantProcessorImpl.soapFault_1 [com.arjuna.wst.messaging.ParticipantProcessorImpl.soapFault_1] - Unexpected exception thrown from soapFault: 
     * @message com.arjuna.wst.messaging.ParticipantProcessorImpl.soapFault_2 [com.arjuna.wst.messaging.ParticipantProcessorImpl.soapFault_2] - SoapFault called on unknown participant: {0}
     */
    public void soapFault(final SoapFault fault, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.soapFault(fault, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                {
                    WSTLogger.arjLoggerI18N.warn("com.arjuna.wst.messaging.ParticipantProcessorImpl.soapFault_1", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isWarnEnabled())
            {
                WSTLogger.arjLoggerI18N.warn("com.arjuna.wst.messaging.ParticipantProcessorImpl.soapFault_2", new Object[] {instanceIdentifier}) ;
            }
        }
    }
    
    /**
     * Send a committed message.
     * 
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantProcessorImpl.sendCommitted_1 [com.arjuna.wst.messaging.ParticipantProcessorImpl.sendCommitted_1] - Unexpected exception while sending Committed
     */
    private void sendCommitted(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final AddressingContext responseAddressingContext = AddressingContext.createNotificationContext(addressingContext, messageId) ;
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        try
        {
            CoordinatorClient.getClient().sendCommitted(responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantProcessorImpl.sendCommitted_1", th) ;
            }
        }
    }
    
    /**
     * Send an aborted message.
     * 
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantProcessorImpl.sendAborted_1 [com.arjuna.wst.messaging.ParticipantProcessorImpl.sendAborted_1] - Unexpected exception while sending Aborted
     */
    private void sendAborted(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final AddressingContext responseAddressingContext = AddressingContext.createNotificationContext(addressingContext, messageId) ;
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        try
        {
            CoordinatorClient.getClient().sendAborted(responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantProcessorImpl.sendAborted_1", th) ;
            }
        }
    }
}
