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
package com.arjuna.wst11.messaging;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.base.processors.ActivatedObjectProcessor;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsba.CoordinatorCompletionParticipantInboundEvents;
import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.webservices11.wsba.State;
import com.arjuna.webservices11.wsba.client.CoordinatorCompletionCoordinatorClient;
import com.arjuna.webservices11.wsba.processors.CoordinatorCompletionParticipantProcessor;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.wsc11.messaging.MessageId;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.namespace.QName;


/**
 * The Coordinator Completion Participant processor.
 * @author kevin
 */
public class CoordinatorCompletionParticipantProcessorImpl extends CoordinatorCompletionParticipantProcessor
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
    public void activateParticipant(final CoordinatorCompletionParticipantInboundEvents participant, final String identifier)
    {
        activatedObjectProcessor.activateObject(participant, identifier) ;
    }

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public void deactivateParticipant(final CoordinatorCompletionParticipantInboundEvents participant)
    {
        activatedObjectProcessor.deactivateObject(participant) ;
    }

    /**
     * Get the participant with the specified identifier.
     * @param instanceIdentifier The participant identifier.
     * @return The participant or null if not known.
     */
    private CoordinatorCompletionParticipantInboundEvents getParticipant(final InstanceIdentifier instanceIdentifier)
    {
        final String identifier = (instanceIdentifier != null ? instanceIdentifier.getInstanceIdentifier() : null) ;
        return (CoordinatorCompletionParticipantInboundEvents)activatedObjectProcessor.getObject(identifier) ;
    }

    /**
     * Cancel.
     * @param cancel The cancel notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.cancel_1 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.cancel_1] - Unexpected exception thrown from cancel:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.cancel_2 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.cancel_2] - Cancel called on unknown participant: {0}
     */
    public void cancel(final NotificationType cancel, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.cancel(cancel, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.cancel_1", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.cancel_2", new Object[] {instanceIdentifier}) ;
            }
            sendCancelled(addressingProperties, arjunaContext) ;
        }
    }

    /**
     * Close.
     * @param close The close notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.close_1 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.close_1] - Unexpected exception thrown from close:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.close_2 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.close_2] - Close called on unknown participant: {0}
     */
    public void close(final NotificationType close, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.close(close, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.close_1", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.close_2", new Object[] {instanceIdentifier}) ;
            }
            sendClosed(addressingProperties, arjunaContext) ;
        }
    }

    /**
     * Compensate.
     * @param compensate The compensate notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.compensate_1 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.compensate_1] - Unexpected exception thrown from compensate:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.compensate_2 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.compensate_2] - Compensate called on unknown participant: {0}
     */
    public void compensate(final NotificationType compensate, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.compensate(compensate, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.compensate_1", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.compensate_2", new Object[] {instanceIdentifier}) ;
            }
            sendCompensated(addressingProperties, arjunaContext) ;
        }
    }

    /**
     * Complete.
     * @param complete The complete notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.complete_1 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.complete_1] - Unexpected exception thrown from complete:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.complete_2 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.complete_2] - Complete called on unknown participant: {0}
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.complete_3 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.complete_3] - Complete called on unknown participant
     */
    public void complete(final NotificationType complete, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.complete(complete, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.complete_1", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.complete_2", new Object[] {instanceIdentifier}) ;
            }
            sendFail(addressingProperties, arjunaContext, State.STATE_ENDED.getValue()) ;
        }
    }

    /**
     * Exited.
     * @param exited The exited notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.exited_1 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.exited_1] - Unexpected exception thrown from exited:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.exited_2 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.exited_2] - Exited called on unknown participant: {0}
     */
    public void exited(final NotificationType exited, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.exited(exited, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.exited_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.exited_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Not Completed.
     * @param notCompleted The not completed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.notCompleted_1 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.notCompleted_1] - Unexpected exception thrown from notCompleted:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.notCompleted_2 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.notCompleted_2] - NotCompleted called on unknown participant: {0}
     */
    public void notCompleted(final NotificationType notCompleted, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.notCompleted(notCompleted, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.notCompleted_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.notCompleted_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Failed.
     * @param failed The failed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.failed_1 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.failed_1] - Unexpected exception thrown from failed:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.failed_2 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.failed_2] - Failed called on unknown participant: {0}
     */
    public void failed(final NotificationType failed, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.failed(failed, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.faulted_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.faulted_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Get Status.
     * @param getStatus The get status notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.getStatus_1 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.getStatus_1] - Unexpected exception thrown from getStatus:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.getStatus_2 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.getStatus_2] - GetStatus called on unknown participant: {0}
     */
    public void getStatus(final NotificationType getStatus, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.getStatus(getStatus, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.getStatus_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.getStatus_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Status.
     * @param status The status.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.status_1 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.status_1] - Unexpected exception thrown from status:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.status_2 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.status_2] - Status called on unknown participant: {0}
     */
    public void status(final StatusType status, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.status(status, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.status_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.status_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * SOAP Fault.
     * @param fault The SOAP fault notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.soapFault_1 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.soapFault_1] - Unexpected exception thrown from soapFault:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.soapFault_2 [com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.soapFault_2] - SoapFault called on unknown participant: {0}
     */
    public void soapFault(final SoapFault fault, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.soapFault(fault, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.soapFault_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionParticipantProcessorImpl.soapFault_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Send a cancelled message.
     *
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.sendCancelled_1 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.sendCancelled_1] - Unexpected exception while sending Cancelled
     */
    private void sendCancelled(final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final AddressingProperties responseAddressingProperties = AddressingHelper.createOneWayResponseContext(addressingProperties, messageId) ;

        try
        {
            // supply null endpoint to indicate addressing properties should be used to route message
            CoordinatorCompletionCoordinatorClient.getClient().sendCancelled(null, responseAddressingProperties, arjunaContext.getInstanceIdentifier()) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.sendCancelled_1", th) ;
            }
        }
    }

    /**
     * Send a closed message.
     *
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.sendClosed_1 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.sendClosed_1] - Unexpected exception while sending Closed
     */
    private void sendClosed(final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final AddressingProperties responseAddressingProperties = AddressingHelper.createOneWayResponseContext(addressingProperties, messageId) ;

        try
        {
            // supply null endpoint to indicate addressing properties should be used to route message
            CoordinatorCompletionCoordinatorClient.getClient().sendClosed(null, responseAddressingProperties, arjunaContext.getInstanceIdentifier()) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.sendClosed_1", th) ;
            }
        }
    }

    /**
     * Send a compensated message.
     *
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.sendCompensated_1 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.sendCompensated_1] - Unexpected exception while sending Compensated
     */
    private void sendCompensated(final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final AddressingProperties responseAddressingProperties = AddressingHelper.createOneWayResponseContext(addressingProperties, messageId) ;

        try
        {
            // supply null endpoint to indicate addressing properties should be used to route message
            CoordinatorCompletionCoordinatorClient.getClient().sendCompensated(null, responseAddressingProperties, arjunaContext.getInstanceIdentifier()) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.sendCompensated_1", th) ;
            }
        }
    }

    /**
     * Send a fail message.
     *
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     * @param exceptionIdentifier The exception identifier.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.sendFail_1 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.sendFail_1] - Unexpected exception while sending Fail
     */
    private void sendFail(final AddressingProperties addressingProperties, final ArjunaContext arjunaContext, final QName exceptionIdentifier)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final AddressingProperties responseAddressingProperties = AddressingHelper.createFaultContext(addressingProperties, messageId) ;

        try
        {
            // supply null endpoint to indicate addressing properties should be used to route message
            CoordinatorCompletionCoordinatorClient.getClient().sendFail(null, responseAddressingProperties, arjunaContext.getInstanceIdentifier(), exceptionIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.sendFail_1", th) ;
            }
        }
    }
}