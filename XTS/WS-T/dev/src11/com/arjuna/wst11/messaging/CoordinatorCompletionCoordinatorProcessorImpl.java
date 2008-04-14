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
import com.arjuna.webservices11.wsba.CoordinatorCompletionCoordinatorInboundEvents;
import com.arjuna.webservices11.wsba.processors.CoordinatorCompletionCoordinatorProcessor;
import com.arjuna.wsc11.messaging.MessageId;
import org.oasis_open.docs.ws_tx.wsba._2006._06.ExceptionType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

import javax.xml.ws.addressing.AddressingProperties;


/**
 * The Coordinator Completion Coordinator processor.
 * @author kevin
 */
public class CoordinatorCompletionCoordinatorProcessorImpl extends CoordinatorCompletionCoordinatorProcessor
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
    public void activateCoordinator(final CoordinatorCompletionCoordinatorInboundEvents coordinator, final String identifier)
    {
        activatedObjectProcessor.activateObject(coordinator, identifier) ;
    }

    /**
     * Deactivate the coordinator.
     * @param coordinator The coordinator.
     */
    public void deactivateCoordinator(final CoordinatorCompletionCoordinatorInboundEvents coordinator)
    {
        activatedObjectProcessor.deactivateObject(coordinator) ;
    }

    /**
     * Get the coordinator associated with the specified identifier.
     * @param instanceIdentifier The coordinator identifier.
     * @return The coordinator or null if not known.
     */
    private CoordinatorCompletionCoordinatorInboundEvents getCoordinator(final InstanceIdentifier instanceIdentifier)
    {
        final String identifier = (instanceIdentifier != null ? instanceIdentifier.getInstanceIdentifier() : null) ;
        return (CoordinatorCompletionCoordinatorInboundEvents)activatedObjectProcessor.getObject(identifier) ;
    }

    /**
     * Cancelled.
     * @param cancelled The canceled notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.canceled_1 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.cancelled_1] - Unexpected exception thrown from cancelled:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.canceled_2 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.cancelled_2] - Cancelled called on unknown coordinator: {0}
     */
    public void cancelled(final NotificationType cancelled, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

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
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.canceled_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.canceled_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Closed.
     * @param closed The closed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.closed_1 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.closed_1] - Unexpected exception thrown from closed:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.closed_2 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.closed_2] - Closed called on unknown coordinator: {0}
     */
    public void closed(final NotificationType closed, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

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
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.closed_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.closed_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Compensated.
     * @param compensated The compensated notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.compensated_1 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.compensated_1] - Unexpected exception thrown from compensated:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.compensated_2 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.compensated_2] - Compensated called on unknown coordinator: {0}
     */
    public void compensated(final NotificationType compensated, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

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
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.compensated_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.compensated_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
      * Fail.
      * @param fail The fail exceptionnotification.
      * @param addressingProperties The addressing context.
      * @param arjunaContext The arjuna context.
      *
      * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.fail_1 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.fail_1] - Unexpected exception thrown from compensated:
      * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.fail_2 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.fail_2] - Compensated called on unknown coordinator: {0}
      */
     public void fail(final ExceptionType fail, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
     {
         final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
         final CoordinatorCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

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
                     WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.fail_1", th) ;
                 }
             }
         }
         else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
         {
             WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.fail_2", new Object[] {instanceIdentifier}) ;
         }
     }
    
    /**
     * Completed.
     * @param completed The completed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.completed_1 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.completed_1] - Unexpected exception thrown from completed:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.completed_2 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.completed_2] - Completed called on unknown coordinator: {0}
     */
    public void completed(final NotificationType completed, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

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
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.completed_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.completed_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Exit.
     * @param exit The exit notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.exit_1 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.exit_1] - Unexpected exception thrown from exit:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.exit_2 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.exit_2] - Exit called on unknown coordinator: {0}
     */
    public void exit(final NotificationType exit, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

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
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.exit_1", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.exit_2", new Object[] {instanceIdentifier}) ;
            }
            sendExited(addressingProperties, arjunaContext) ;
        }
    }

    /**
     * FaulCannot completet.
     * @param cannotComplete The cannot complete notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.cannotComplete_1 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.cannotComplete_1] - Unexpected exception thrown from fault:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.cannotComplete_2 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.cannotComplete_2] - Fault called on unknown coordinator: {0}
     */
    public void cannotComplete(final NotificationType cannotComplete, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.cannotComplete(cannotComplete, addressingProperties, arjunaContext); ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.cannotComplete_1", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.cannotComplete_2", new Object[] {instanceIdentifier}) ;
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
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.getStatus_1 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.getStatus_1] - Unexpected exception thrown from getStatus:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.getStatus_2 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.getStatus_2] - GetStatus called on unknown coordinator: {0}
     */
    public void getStatus(final NotificationType getStatus, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

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
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.getStatus_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.getStatus_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Status.
     * @param status The status.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.status_1 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.status_1] - Unexpected exception thrown from status:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.status_2 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.status_2] - Status called on unknown coordinator: {0}
     */
    public void status(final StatusType status, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

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
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.status_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.status_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * SOAP fault.
     * @param soapFault The SOAP fault.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.soapFault_1 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.soapFault_1] - Unexpected exception thrown from soapFault:
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.soapFault_2 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.soapFault_2] - SoapFault called on unknown coordinator: {0}
     */
    public void soapFault(final SoapFault soapFault, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

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
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.soapFault_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.soapFault_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Send an exited message.
     *
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.sendExited_1 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.sendExited_1] - Unexpected exception while sending Exited
     */
    private void sendExited(final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final AddressingProperties responseAddressingContext = AddressingHelper.createNotificationContext(messageId) ;
        try
        {
            // TODO cannot do this without an endpoint so we have to log an error message!
            // CoordinatorCompletionParticipantClient.getClient().sendExited(responseAddressingContext, arjunaContext.getInstanceIdentifier()) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.sendExited_1", th) ;
            }
        }
    }

    /**
     * Send a faulted message.
     *
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.sendFailed_1 [com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.sendFailed_1] - Unexpected exception while sending Faulted
     */
    private void sendFailed(final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final AddressingProperties responseAddressingContext = AddressingHelper.createNotificationContext(messageId) ;
        try
        {
            // TODO cannot do this without an endpoint so we have to log an error message!
            // CoordinatorCompletionParticipantClient.getClient().sendFailed(responseAddressingContext, arjunaContext.getInstanceIdentifier()) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl.sendFailed_1", th) ;
            }
        }
    }
}