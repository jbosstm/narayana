/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
package com.arjuna.wst11.messaging;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.base.processors.ActivatedObjectProcessor;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsba.ParticipantCompletionCoordinatorInboundEvents;
import com.arjuna.webservices11.wsba.processors.ParticipantCompletionCoordinatorProcessor;
import com.arjuna.wsc11.messaging.MessageId;
import org.oasis_open.docs.ws_tx.wsba._2006._06.ExceptionType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

import javax.xml.ws.addressing.AddressingProperties;


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
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.cancelled_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.cancelled_1] - Unexpected exception thrown from cancelled:
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.cancelled_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.cancelled_2] - Cancelled called on unknown coordinator: {0}
     */
    public void cancelled(final NotificationType cancelled, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.cancelled(cancelled, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.cancelled_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.cancelled_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Closed.
     * @param closed The closed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.closed_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.closed_1] - Unexpected exception thrown from closed:
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.closed_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.closed_2] - Closed called on unknown coordinator: {0}
     */
    public void closed(final NotificationType closed, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.closed(closed, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.closed_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.closed_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Compensated.
     * @param compensated The compensated notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.compensated_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.compensated_1] - Unexpected exception thrown from compensated:
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.compensated_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.compensated_2] - Compensated called on unknown coordinator: {0}
     */
    public void compensated(final NotificationType compensated, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.compensated(compensated, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.compensated_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.compensated_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Completed.
     * @param completed The completed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_1] - Unexpected exception thrown from completed:
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_2] - Completed called on unknown coordinator: {0}
     */
    public void completed(final NotificationType completed, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.completed(completed, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Completed.
     * @param cannotComplete The cannot complete notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.cannotComplete_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.cannotComplete_1] - Unexpected exception thrown from cannot complete:
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.cannotComplete_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.cannotComplete_2] - Cannot complete called on unknown coordinator: {0}
     */
    public void cannotComplete(final NotificationType cannotComplete, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.cannotComplete(cannotComplete, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Exit.
     * @param exit The exit notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.exit_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.exit_1] - Unexpected exception thrown from exit:
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.exit_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.exit_2] - Exit called on unknown coordinator: {0}
     */
    public void exit(final NotificationType exit, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.exit(exit, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.exit_1", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.exit_2", new Object[] {instanceIdentifier}) ;
            }
            sendExited(addressingProperties, arjunaContext) ;
        }
    }

    /**
     * Fault.
     * @param fail The fault notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.fault_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.fault_1] - Unexpected exception thrown from fault:
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.fault_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.fault_2] - Fault called on unknown coordinator: {0}
     */
    public void fail(final ExceptionType fail, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.fail(fail, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.fault_1", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.fault_2", new Object[] {instanceIdentifier}) ;
            }
            sendFailed(addressingProperties, arjunaContext) ;
        }
    }

    /**
     * Get Status.
     * @param getStatus The get status notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_1] - Unexpected exception thrown from getStatus:
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_2] - GetStatus called on unknown coordinator: {0}
     */
    public void getStatus(final NotificationType getStatus, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.getStatus(getStatus, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Status.
     * @param status The status.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.status_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.status_1] - Unexpected exception thrown from status:
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.status_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.status_2] - Status called on unknown coordinator: {0}
     */
    public void status(final StatusType status, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.status(status, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.status_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.status_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * SOAP fault.
     * @param soapFault The SOAP fault.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.soapFault_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.soapFault_1] - Unexpected exception thrown from soapFault:
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.soapFault_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.soapFault_2] - SoapFault called on unknown coordinator: {0}
     */
    public void soapFault(final SoapFault soapFault, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.soapFault(soapFault, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.soapFault_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.soapFault_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Send an exited message.
     *
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.sendExited_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.sendExited_1] - Unexpected exception while sending Exited
     */
    private void sendExited(final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final AddressingProperties responseAddressingProperties = AddressingHelper.createNotificationContext(messageId) ;
        try
        {
            // cannot do this without a coordinator endpoint and that is the only way it gets called
            // ParticipantCompletionParticipantClient.getClient().sendExited(responseAddressingProperties, arjunaContext.getInstanceIdentifier()) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.sendExited_1", th) ;
            }
        }
    }

    /**
     * Send a faulted message.
     *
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.sendFaulted_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.sendFaulted_1] - Unexpected exception while sending Faulted
     */
    private void sendFailed(final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final AddressingProperties responseAddressingProperties = AddressingHelper.createNotificationContext(  messageId) ;
        try
        {
            // cannot do this without a coordinator endpoint and that is the only way it gets called
            // ParticipantCompletionParticipantClient.getClient().sendFaulted(responseAddressingProperties, arjunaContext.getInstanceIdentifier()) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.sendFailed_1", th) ;
            }
        }
    }
}