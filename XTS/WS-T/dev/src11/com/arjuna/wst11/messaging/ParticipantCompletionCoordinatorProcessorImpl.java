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
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.base.processors.ActivatedObjectProcessor;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.wsf.common.addressing.MAP;
import com.arjuna.webservices11.wsaddr.client.SoapFaultClient;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsba.ParticipantCompletionCoordinatorInboundEvents;
import com.arjuna.webservices11.wsba.client.ParticipantCompletionParticipantClient;
import com.arjuna.webservices11.wsba.processors.ParticipantCompletionCoordinatorProcessor;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.wsc11.messaging.MessageId;
import org.oasis_open.docs.ws_tx.wsba._2006._06.ExceptionType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;
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
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.cancelled_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.cancelled_1] - Unexpected exception thrown from cancelled:
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.cancelled_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.cancelled_2] - Cancelled called on unknown coordinator: {0}
     */
    public void cancelled(final NotificationType cancelled, final MAP map, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.cancelled(cancelled, map, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.logger.isDebugEnabled())
                {
                    WSTLogger.logger.debugv("Unexpected exception thrown from cancelled:", th) ;
                }
            }
        }
        else if (WSTLogger.logger.isDebugEnabled())
        {
            WSTLogger.logger.debugv("Cancelled called on unknown coordinator: {0}", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Closed.
     * @param closed The closed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.closed_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.closed_1] - Unexpected exception thrown from closed:
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.closed_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.closed_2] - Closed called on unknown coordinator: {0}
     */
    public void closed(final NotificationType closed, final MAP map, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.closed(closed, map, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.logger.isDebugEnabled())
                {
                    WSTLogger.logger.debugv("Unexpected exception thrown from closed:", th) ;
                }
            }
        }
        else if (WSTLogger.logger.isDebugEnabled())
        {
            WSTLogger.logger.debugv("Closed called on unknown coordinator: {0}", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Compensated.
     * @param compensated The compensated notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.compensated_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.compensated_1] - Unexpected exception thrown from compensated:
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.compensated_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.compensated_2] - Compensated called on unknown coordinator: {0}
     */
    public void compensated(final NotificationType compensated, final MAP map, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.compensated(compensated, map, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.logger.isDebugEnabled())
                {
                    WSTLogger.logger.debugv("Unexpected exception thrown from compensated:", th) ;
                }
            }
        }
        else if (WSTLogger.logger.isDebugEnabled())
        {
            WSTLogger.logger.debugv("Compensated called on unknown coordinator: {0}", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Completed.
     * @param completed The completed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message__ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_1] - Unexpected exception thrown from completed:
     * @message__ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_2] - Completed called on unknown coordinator: {0}
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_3 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.completed_3] - Ignoring completed called on unidentified coordinator until recovery pass is complete: {0}
     */
    public void completed(final NotificationType completed, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.completed(completed, map, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.logger.isDebugEnabled())
                {
                    WSTLogger.logger.debugv("Unexpected exception thrown from completed:", th) ;
                }
            }
        }
        else if (WSTLogger.logger.isDebugEnabled())
        {
            if (areRecoveryLogEntriesAccountedFor()) {
                // this is a resend for a lost participant
                WSTLogger.logger.debugv("Completed called on unknown coordinator: {0}", new Object[] {instanceIdentifier}) ;
            } else {
                // this may be a resend for a participant still pending recovery
                WSTLogger.logger.debugv("Ignoring completed called on unidentified coordinator until recovery pass is complete: {0}", new Object[] {instanceIdentifier}) ;
            }
        }
    }

    /**
     * Completed.
     * @param cannotComplete The cannot complete notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.cannotComplete_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.cannotComplete_1] - Unexpected exception thrown from cannot complete:
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.cannotComplete_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.cannotComplete_2] - Cannot complete called on unknown coordinator: {0}
     */
    public void cannotComplete(final NotificationType cannotComplete, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.cannotComplete(cannotComplete, map, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.logger.isDebugEnabled())
                {
                    WSTLogger.logger.debugv("Unexpected exception thrown from completed:", th) ;
                }
            }
        } else {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Completed called on unknown coordinator: {0}", new Object[] {instanceIdentifier}) ;
            }

            sendNotCompleted(map, arjunaContext) ;
        }
    }

    /**
     * Exit.
     * @param exit The exit notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.exit_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.exit_1] - Unexpected exception thrown from exit:
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.exit_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.exit_2] - Exit called on unknown coordinator: {0}
     */
    public void exit(final NotificationType exit, final MAP map, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.exit(exit, map, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.logger.isDebugEnabled())
                {
                    WSTLogger.logger.debugv("Unexpected exception thrown from exit:", th) ;
                }
            }
        } else {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Exit called on unknown coordinator: {0}", new Object[] {instanceIdentifier}) ;
            }
            sendExited(map, arjunaContext) ;
        }
    }

    /**
     * Fail.
     * @param fail The fail notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.fail_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.fail_1] - Unexpected exception thrown from fail:
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.fail_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.fail_2] - Fail called on unknown coordinator: {0}
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.fail_3 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.fail_3] - Ignoring fail called on unidentified coordinator until recovery pass is complete: {0}
     */
    public void fail(final ExceptionType fail, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.fail(fail, map, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.logger.isDebugEnabled())
                {
                    WSTLogger.logger.debugv("Unexpected exception thrown from fail:", th) ;
                }
            }
        } else if (areRecoveryLogEntriesAccountedFor()) {
            // we can respond with a failed as the participant is not pending recovery
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Fail called on unknown coordinator: {0}", new Object[] {instanceIdentifier}) ;
            }
            sendFailed(map, arjunaContext) ;
        } else {
            // we must delay responding until we can be sure there is no participant pending recovery
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Ignoring fail called on unidentified coordinator until recovery pass is complete: {0}", new Object[] {instanceIdentifier}) ;
            }
        }
    }

    /**
     * Get Status.
     * @param getStatus The get status notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_1] - Unexpected exception thrown from getStatus:
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_2] - GetStatus called on unknown coordinator: {0}
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_3 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_3] - Unexpected exception while sending InvalidStateFault to participant {0}
     * @message com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_4 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_4] - GetStatus requested for unknown participant completion participant
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_5 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_5] - GetStatus dropped for unknown coordinator completion participant {0} while waiting on recovery scan
     */
    public void getStatus(final NotificationType getStatus, final MAP map, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.getStatus(getStatus, map, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.logger.isDebugEnabled())
                {
                    WSTLogger.logger.debugv("Unexpected exception thrown from getStatus:", th) ;
                }
            }
        }
        else if (!areRecoveryLogEntriesAccountedFor())
        {
            // drop the request until we have ensured that there is no recovered coordinator for this id

            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("GetStatus dropped for unknown coordinator completion participant {0} while waiting on recovery scan", new Object[] {instanceIdentifier}) ;
            }
        }
        else
        {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("GetStatus called on unknown coordinator: {0}", new Object[] {instanceIdentifier}) ;
            }
            
            // send an invalid state fault

            final String messageId = MessageId.getMessageId();
            final MAP faultMAP = AddressingHelper.createFaultContext(map, messageId) ;
            try
            {
                final SoapFault11 soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME,
                        WSTLogger.arjLoggerI18N.getString("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_4")) ;
                AddressingHelper.installNoneReplyTo(faultMAP);
                SoapFaultClient.sendSoapFault(soapFault, faultMAP, getFaultAction()) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isInfoEnabled())
                {
                    WSTLogger.arjLoggerI18N.info("com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.getStatus_3", new Object[] {instanceIdentifier},  th) ;
                }
            }
        }
    }

    private static String getFaultAction()
    {
        return CoordinationConstants.WSCOOR_ACTION_FAULT;
    }

    /**
     * Status.
     * @param status The status.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.status_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.status_1] - Unexpected exception thrown from status:
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.status_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.status_2] - Status called on unknown coordinator: {0}
     */
    public void status(final StatusType status, final MAP map, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.status(status, map, arjunaContext) ;
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
            WSTLogger.logger.debugv("Status called on unknown coordinator: {0}", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * SOAP fault.
     * @param soapFault The SOAP fault.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.soapFault_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.soapFault_1] - Unexpected exception thrown from soapFault:
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.soapFault_2 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.soapFault_2] - SoapFault called on unknown coordinator: {0}
     */
    public void soapFault(final SoapFault soapFault, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantCompletionCoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.soapFault(soapFault, map, arjunaContext) ;
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
            WSTLogger.logger.debugv("SoapFault called on unknown coordinator: {0}", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Send an exited message.
     *
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.sendExited_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.sendExited_1] - Unexpected exception while sending Exited
     */
    private void sendExited(final MAP map, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final MAP responseMAP = AddressingHelper.createOneWayResponseContext(map, messageId) ;
        try
        {
            // supply null endpoint so that addressing properties are used to deliver message
            ParticipantCompletionParticipantClient.getClient().sendExited(null, responseMAP, arjunaContext.getInstanceIdentifier()) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Unexpected exception while sending Exited", th) ;
            }
        }
    }

    /**
     * Send a failed message.
     *
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.sendFailed_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.sendFailed_1] - Unexpected exception while sending Failed
     */
    private void sendFailed(final MAP map, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final MAP responseMAP = AddressingHelper.createOneWayResponseContext(map, messageId) ;
        try
        {
            // supply null endpoint so that addressing properties are used to deliver message
            ParticipantCompletionParticipantClient.getClient().sendFailed(null, responseMAP, arjunaContext.getInstanceIdentifier()) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Unexpected exception while sending Failed", th) ;
            }
        }
    }

    /**
     * Send a not completed message.
     *
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message_ com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.sendNotCompleted_1 [com.arjuna.wst11.messaging.ParticipantCompletionCoordinatorProcessorImpl.sendNotCompleted_1] - Unexpected exception while sending NotCompleted
     */
    private void sendNotCompleted(final MAP map, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final MAP responseMAP = AddressingHelper.createOneWayResponseContext(map, messageId) ;
        try
        {
            // supply null endpoint so that addressing properties are used to deliver message
            ParticipantCompletionParticipantClient.getClient().sendNotCompleted(null, responseMAP, arjunaContext.getInstanceIdentifier()); ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Unexpected exception while sending NotCompleted", th) ;
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
        return (XTSBARecoveryManager.getRecoveryManager().isCoordinatorRecoveryStarted() &&
                XTSBARecoveryManager.getRecoveryManager().isSubordinateCoordinatorRecoveryStarted());
    }
}