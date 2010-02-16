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
import com.arjuna.webservices.wsba.ExceptionType;
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.ParticipantCompletionCoordinatorInboundEvents;
import com.arjuna.webservices.wsba.StatusType;
import com.arjuna.webservices.wsba.client.ParticipantCompletionParticipantClient;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionCoordinatorProcessor;
import com.arjuna.wsc.messaging.MessageId;
import org.jboss.jbossts.xts.recovery.participant.ba.XTSBARecoveryManager;


/**
 * The Participant Completion Coordinator processor.
 * @author kevin
 */
public class ParticipantCompletionCoordinatorProcessorImpl extends ParticipantCompletionCoordinatorProcessor
{
    /**
     * The activated object processor.
     */
    private final ActivatedObjectProcessor activatedObjectProcessor = new ActivatedObjectProcessor() ;
    
    /**
     * Activate the coordinator.
     * @param coordinator The coordinator.
     * @param identifier The identifier.
     */
    public void activateCoordinator(final ParticipantCompletionCoordinatorInboundEvents coordinator, final String identifier)
    {
        activatedObjectProcessor.activateObject(coordinator, identifier) ;
    }

    /**
     * Deactivate the coordinator.
     * @param coordinator The coordinator.
     */
    public void deactivateCoordinator(final ParticipantCompletionCoordinatorInboundEvents coordinator)
    {
        activatedObjectProcessor.deactivateObject(coordinator) ;
    }
    
    /**
     * Locate a coordinator by name.
     * @param identifier The name of the coordinator.
     */
    public ParticipantCompletionCoordinatorInboundEvents getCoordinator(final String  identifier)
    {
        return (ParticipantCompletionCoordinatorInboundEvents)activatedObjectProcessor.getObject(identifier);
    }

    /**
     * Get the coordinator with the specified identifier.
     * @param instanceIdentifier The coordinator identifier.
     * @return The coordinator or null if not known.
     */
    private ParticipantCompletionCoordinatorInboundEvents getCoordinator(final InstanceIdentifier instanceIdentifier)
    {
        final String identifier = (instanceIdentifier != null ? instanceIdentifier.getInstanceIdentifier() : null) ;
        return (ParticipantCompletionCoordinatorInboundEvents)activatedObjectProcessor.getObject(identifier) ;
    }
    
    /**
     * Cancelled.
     * @param cancelled The cancelled notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.cancelled_1 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.cancelled_1] - Unexpected exception thrown from cancelled:
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.cancelled_2 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.cancelled_2] - Cancelled called on unknown coordinator: {0}
     */
    public void cancelled(final NotificationType cancelled, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.cancelled(cancelled, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.cancelled_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.cancelled_2", new Object[] {instanceIdentifier}) ;
        }
    }
    
    /**
     * Closed.
     * @param closed The closed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.closed_1 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.closed_1] - Unexpected exception thrown from closed:
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.closed_2 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.closed_2] - Closed called on unknown coordinator: {0}
     */
    public void closed(final NotificationType closed, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.closed(closed, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.closed_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.closed_2", new Object[] {instanceIdentifier}) ;
        }
    }
    
    /**
     * Compensated.
     * @param compensated The compensated notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.compensated_1 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.compensated_1] - Unexpected exception thrown from compensated:
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.compensated_2 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.compensated_2] - Compensated called on unknown coordinator: {0}
     */
    public void compensated(final NotificationType compensated, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.compensated(compensated, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.compensated_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.compensated_2", new Object[] {instanceIdentifier}) ;
        }
    }
    
    /**
     * Completed.
     * @param completed The completed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_1 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_1] - Unexpected exception thrown from completed:
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_2 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_2] - Completed called on unknown coordinator: {0}
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_3 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_3] - Ignoring completed called on unidentified coordinator until recovery pass is complete: {0}
     */
    public void completed(final NotificationType completed, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.completed(completed, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            if (areRecoveryLogEntriesAccountedFor()) {
                // this is a resend for a lost participant
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_2", new Object[] {instanceIdentifier}) ;
            } else {
                // this may be a resend for a participant still pending recovery
                WSTLogger.arjLoggerI18N.debug("com.arjuna.ws1.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_3", new Object[] {instanceIdentifier}) ;
            }
        }
    }
    
    /**
     * Exit.
     * @param exit The exit notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.exit_1 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.exit_1] - Unexpected exception thrown from exit:
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.exit_2 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.exit_2] - Exit called on unknown coordinator: {0}
     */
    public void exit(final NotificationType exit, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.exit(exit, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.exit_1", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.exit_2", new Object[] {instanceIdentifier}) ;
            }
            sendExited(addressingContext, arjunaContext) ;
        }
    }
    
    /**
     * Fault.
     * @param fault The fault notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.fault_1 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.fault_1] - Unexpected exception thrown from fault:
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.fault_2 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.fault_2] - Fault called on unknown coordinator: {0}
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.fault_3 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.fault_3] - Ignoring fault called on unidentified coordinator until recovery pass is complete: {0}
     */
    public void fault(final ExceptionType fault, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.fault(fault, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.fault_1", th) ;
                }
            }
        }
        else if (areRecoveryLogEntriesAccountedFor())
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.fault_2", new Object[] {instanceIdentifier}) ;
            }
            sendFaulted(addressingContext, arjunaContext) ;
        }
        else
        {
            // we must delay responding until we can be sure there is no participant pending recovery
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.fault_3", new Object[] {instanceIdentifier}) ;
            }
        }
    }
    
    /**
     * Get Status.
     * @param getStatus The get status notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_1 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_1] - Unexpected exception thrown from getStatus:
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_2 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_2] - GetStatus called on unknown coordinator: {0}
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_3 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_3] - Unexpected exception while sending InvalidStateFault to participant {0}
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_4 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_4] - GetStatus requested for unknown coordinator completion participant
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_5 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_4] - GetStatus dropped for unknown coordinator completion participant {0} awaiting recovery scan completion
     */
    public void getStatus(final NotificationType getStatus, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.getStatus(getStatus, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_1", th) ;
                }
            }
        }
        else if (!areRecoveryLogEntriesAccountedFor())
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_5", new Object[] {instanceIdentifier}) ;
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_2", new Object[] {instanceIdentifier}) ;
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
                final String message = WSTLogger.arjLoggerI18N.getString("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_4") ;
                final SoapFault soapFault = new SoapFault10(SoapFaultType.FAULT_SENDER, CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME, message) ;
                ParticipantCompletionParticipantClient.getClient().sendSoapFault(responseAddressingContext, soapFault, instanceIdentifier) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isInfoEnabled())
                {
                    WSTLogger.arjLoggerI18N.info("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_3", new Object[] {instanceIdentifier},  th) ;
                }
            }
        }
    }
    
    /**
     * Status.
     * @param status The status.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.status_1 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.status_1] - Unexpected exception thrown from status:
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.status_2 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.status_2] - Status called on unknown coordinator: {0}
     */
    public void status(final StatusType status, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.status(status, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.status_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.status_2", new Object[] {instanceIdentifier}) ;
        }
    }
    
    /**
     * SOAP fault.
     * @param soapFault The SOAP fault.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.soapFault_1 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.soapFault_1] - Unexpected exception thrown from soapFault:
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.soapFault_2 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.soapFault_2] - SoapFault called on unknown coordinator: {0}
     */
    public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.soapFault(soapFault, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.soapFault_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.soapFault_2", new Object[] {instanceIdentifier}) ;
        }
    }
    
    /**
     * Send an exited message.
     * 
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.sendExited_1 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.sendExited_1] - Unexpected exception while sending Exited
     */
    private void sendExited(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final AddressingContext responseAddressingContext = AddressingContext.createRequestContext(addressingContext.getFrom(), messageId) ;
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendExited(responseAddressingContext, arjunaContext.getInstanceIdentifier()) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.sendExited_1", th) ;
            }
        }
    }
    
    /**
     * Send a faulted message.
     * 
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.sendFaulted_1 [com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.sendFaulted_1] - Unexpected exception while sending Faulted
     */
    private void sendFaulted(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final AddressingContext responseAddressingContext = AddressingContext.createRequestContext(addressingContext.getFrom(), messageId) ;
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendFaulted(responseAddressingContext, arjunaContext.getInstanceIdentifier()) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl.sendFailed_1", th) ;
            }
        }
    }

    /**
     * Tests if there may be unknown coordinator entries in the recovery log.
     *
     * @return false if there may be unknown coordinator entries in the recovery log.
     */

    private static boolean areRecoveryLogEntriesAccountedFor()
    {
        return XTSBARecoveryManager.getRecoveryManager().isCoordinatorRecoveryStarted();
    }
}
