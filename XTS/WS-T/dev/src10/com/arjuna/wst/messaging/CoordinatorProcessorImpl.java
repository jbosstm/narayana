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
import com.arjuna.webservices.base.processors.ActivatedObjectProcessor;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.RelationshipType;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsat.NotificationType;
import com.arjuna.webservices.wsat.CoordinatorInboundEvents;
import com.arjuna.webservices.wsat.client.ParticipantClient;
import com.arjuna.webservices.wsat.processors.CoordinatorProcessor;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.wsc.messaging.MessageId;
import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryManager;

/**
 * The Coordinator processor.
 * @author kevin
 */
public class CoordinatorProcessorImpl extends CoordinatorProcessor
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
    public void activateCoordinator(final CoordinatorInboundEvents coordinator, final String identifier)
    {
        activatedObjectProcessor.activateObject(coordinator, identifier) ;
    }

    /**
     * Deactivate the coordinator.
     * @param coordinator The coordinator.
     */
    public void deactivateCoordinator(CoordinatorInboundEvents coordinator) {
        activatedObjectProcessor.deactivateObject(coordinator);
    }

    /**
     * Get the coordinator with the specified identifier.
     * @param identifier The coordinator identifier as a String.
     * @return The coordinator or null if not known.
     */

    public CoordinatorInboundEvents getCoordinator(final String identifier)
    {
        return (CoordinatorInboundEvents)activatedObjectProcessor.getObject(identifier) ;
    }
    /**
     * Get the coordinator with the specified identifier.
     * @param instanceIdentifier The coordinator identifier as an Instanceidentifier.
     * @return The coordinator or null if not known.
     */
    private CoordinatorInboundEvents getCoordinator(final InstanceIdentifier instanceIdentifier)
    {
        final String identifier = (instanceIdentifier != null ? instanceIdentifier.getInstanceIdentifier() : null) ;
        return getCoordinator(identifier);
    }

    /**
     * Aborted.
     * @param aborted The aborted notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    public void aborted(final NotificationType aborted, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;
        
        if (coordinator != null)
        {
            try
            {
                coordinator.aborted(aborted, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                WSTLogger.i18NLogger.warn_messaging_CoordinatorProcessorImpl_aborted_1(th);
            }
        }
        else
        {
            WSTLogger.i18NLogger.warn_messaging_CoordinatorProcessorImpl_aborted_2(instanceIdentifier.toString());
        }
    }
    
    /**
     * Committed.
     * @param committed The committed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    public void committed(final NotificationType committed, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;
        
        if (coordinator != null)
        {
            try
            {
                coordinator.committed(committed, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                 WSTLogger.i18NLogger.warn_messaging_CoordinatorProcessorImpl_committed_1(th);
            }
        }
        else {
            WSTLogger.i18NLogger.warn_messaging_CoordinatorProcessorImpl_committed_2(instanceIdentifier.toString());
        }
    }
    
    /**
     * Prepared.
     * @param prepared The prepared notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    public void prepared(final NotificationType prepared, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;
        
        if (coordinator != null)
        {
            try
            {
                coordinator.prepared(prepared, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                WSTLogger.i18NLogger.warn_messaging_CoordinatorProcessorImpl_prepared_1(th);
            }
        }
        else if (areRecoveryLogEntriesAccountedFor())
        {
            WSTLogger.i18NLogger.warn_messaging_CoordinatorProcessorImpl_prepared_2(instanceIdentifier.toString());

            final String identifierValue = instanceIdentifier.getInstanceIdentifier() ;
            if ((identifierValue != null) && (identifierValue.length() > 0) && (identifierValue.charAt(0) == 'D'))
            {
        	sendRollback(addressingContext, arjunaContext) ;
            }
            else
            {
                sendInvalidState(addressingContext, arjunaContext) ;
            }
        }
        else
        {
            // there may be a participant stub waiting to be recovered from the log so drop the
            // message, forcing the caller to retry

            WSTLogger.i18NLogger.warn_messaging_CoordinatorProcessorImpl_prepared_3(instanceIdentifier.toString());
        }
    }
    
    /**
     * Read only.
     * @param readOnly The read only notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    public void readOnly(final NotificationType readOnly, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;
        
        if (coordinator != null)
        {
            try
            {
                coordinator.readOnly(readOnly, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                WSTLogger.i18NLogger.warn_messaging_CoordinatorProcessorImpl_readOnly_1(th);
            }
        }
        else {
            WSTLogger.i18NLogger.warn_messaging_CoordinatorProcessorImpl_readOnly_2(instanceIdentifier.toString());
        }
    }
    
    /**
     * Replay.
     * @param replay The replay notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    public void replay(final NotificationType replay, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;
        
        if (coordinator != null)
        {
            try
            {
                coordinator.replay(replay, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                WSTLogger.i18NLogger.warn_messaging_CoordinatorProcessorImpl_replay_1(th);
            }
        }
        else
        {
            WSTLogger.i18NLogger.warn_messaging_CoordinatorProcessorImpl_replay_2(instanceIdentifier.toString());

            
            final String identifierValue = instanceIdentifier.getInstanceIdentifier() ;
            if ((identifierValue != null) && (identifierValue.length() > 0) && (identifierValue.charAt(0) == 'D'))
            {
        	sendRollback(addressingContext, arjunaContext) ;
            }
            else
            {
                sendInvalidState(addressingContext, arjunaContext) ;
            }
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
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.soapFault(fault, addressingContext, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                WSTLogger.i18NLogger.warn_messaging_CoordinatorProcessorImpl_soapFault_1(th);
            }
        }
        else
        {
            WSTLogger.i18NLogger.warn_messaging_CoordinatorProcessorImpl_soapFault_2(instanceIdentifier.toString());
        }
    }
    
    /**
     * Send an unknown transaction fault.
     * 
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    private void sendInvalidState(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String responseMessageId = MessageId.getMessageId() ;
        final AddressingContext responseAddressingContext = AddressingContext.createRequestContext(addressingContext.getFrom(), responseMessageId) ;
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        
        final AttributedURIType requestMessageId = addressingContext.getMessageID() ;
        if (requestMessageId != null)
        {
            responseAddressingContext.addRelatesTo(new RelationshipType(requestMessageId.getValue())) ;
        }
        
        try
        {
            final String message = WSTLogger.i18NLogger.get_messaging_CoordinatorProcessorImpl_sendInvalidState_1();
            final SoapFault soapFault = new SoapFault10(SoapFaultType.FAULT_SENDER, CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME, message) ;
            ParticipantClient.getClient().sendSoapFault(responseAddressingContext, soapFault, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpecting exception while sending InvalidState", th) ;
            }
        }
    }
    
    /**
     * Send a rollback message.
     * 
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    private void sendRollback(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final AddressingContext responseAddressingContext = AddressingContext.createNotificationContext(addressingContext, messageId) ;
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        try
        {
            ParticipantClient.getClient().sendRollback(responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception while sending Rollback", th) ;
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
        return XTSATRecoveryManager.getRecoveryManager().isCoordinatorRecoveryStarted();
    }
}
