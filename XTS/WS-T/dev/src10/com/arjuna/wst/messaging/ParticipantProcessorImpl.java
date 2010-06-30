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
import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryManager;

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
     * Check whether a participant with the given id is currently active
     * @param identifier The identifier.
     */
    public boolean isActive(final String identifier)
    {
        // if there is an entry in the table then it is active or completed and pending delete

        return (activatedObjectProcessor.getObject(identifier) != null);
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
     */
    public void commit(final NotificationType commit, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;

        /**
         * ensure the AT participant recovery manager is running
         */
        XTSATRecoveryManager recoveryManager = XTSATRecoveryManager.getRecoveryManager();

        if (recoveryManager == null) {
            // log warning and drop this message -- it will be resent
            WSTLogger.i18NLogger.warn_messaging_ParticipantProcessorImpl_commit_3(instanceIdentifier.toString());

            return;
        }

        final ParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.commit(commit, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                 WSTLogger.i18NLogger.warn_messaging_ParticipantProcessorImpl_commit_1(th);
            }
        }
        else if (!recoveryManager.isParticipantRecoveryStarted())
        {
            WSTLogger.i18NLogger.warn_messaging_ParticipantProcessorImpl_commit_4(instanceIdentifier.toString());
        }
        else if (recoveryManager.findParticipantRecoveryRecord(instanceIdentifier.getInstanceIdentifier()) != null)
        {
            WSTLogger.i18NLogger.warn_messaging_ParticipantProcessorImpl_commit_5(instanceIdentifier.toString());
        }
        else
        {
            WSTLogger.i18NLogger.warn_messaging_ParticipantProcessorImpl_commit_2(instanceIdentifier.toString());
            sendCommitted(addressingContext, arjunaContext) ;
        }
    }
    
    /**
     * Prepare.
     * @param prepare The prepare notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
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
                WSTLogger.i18NLogger.warn_messaging_ParticipantProcessorImpl_prepare_1(th);
            }
        }
        else
        {
            WSTLogger.i18NLogger.warn_messaging_ParticipantProcessorImpl_prepare_2(instanceIdentifier.toString());
            sendAborted(addressingContext, arjunaContext) ;
        }
    }
    
    /**
     * Rollback.
     * @param rollback The rollback notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    public void rollback(final NotificationType rollback, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;

        /**
         * ensure the AT participant recovery manager is running
         */
        XTSATRecoveryManager recoveryManager = XTSATRecoveryManager.getRecoveryManager();

        if (recoveryManager == null) {
            // log warning and drop this message -- it will be resent
            WSTLogger.i18NLogger.warn_messaging_ParticipantProcessorImpl_rollback_3(instanceIdentifier.toString());
        }

        final ParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.rollback(rollback, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                WSTLogger.i18NLogger.warn_messaging_ParticipantProcessorImpl_rollback_1(th);
            }
        }
        else if (!recoveryManager.isParticipantRecoveryStarted())
        {
            WSTLogger.i18NLogger.warn_messaging_ParticipantProcessorImpl_rollback_4(instanceIdentifier.toString());
        }
        else if (recoveryManager.findParticipantRecoveryRecord(instanceIdentifier.getInstanceIdentifier()) != null)
        {
            WSTLogger.i18NLogger.warn_messaging_ParticipantProcessorImpl_rollback_5(instanceIdentifier.toString());
        }
        else
        {
            WSTLogger.i18NLogger.warn_messaging_ParticipantProcessorImpl_rollback_2(instanceIdentifier.toString());
            sendAborted(addressingContext, arjunaContext) ;
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
        final ParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.soapFault(fault, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                WSTLogger.i18NLogger.warn_messaging_ParticipantProcessorImpl_soapFault_1(th);
            }
        }
        else
        {
             WSTLogger.i18NLogger.warn_messaging_ParticipantProcessorImpl_soapFault_2(instanceIdentifier.toString());
        }
    }
    
    /**
     * Send a committed message.
     * 
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
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
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Unexpected exception while sending Committed", th) ;
            }
        }
    }
    
    /**
     * Send an aborted message.
     * 
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
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
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Unexpected exception while sending Aborted", th) ;
            }
        }
    }
}
