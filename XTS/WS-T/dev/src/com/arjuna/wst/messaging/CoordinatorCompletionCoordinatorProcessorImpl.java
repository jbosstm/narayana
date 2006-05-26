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

import java.text.MessageFormat;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices.wsba.ExceptionType;
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.client.CoordinatorCompletionParticipantClient;
import com.arjuna.webservices.wsba.processors.CoordinatorCompletionCoordinatorProcessor;
import com.arjuna.wsc.messaging.MessageId;
import com.arjuna.wst.BAParticipantManager;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;


/**
 * The Coordinator Completion Coordinator processor.
 * @author kevin
 * 
 * @message com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_6 [com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_6] - Wrong state
 * @message com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_7 [com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_7] - Unknown Transaction
 * @message com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_8 [com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_8] - Unknown error: {0}
 * @message com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_9 [com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_9] - Unexpected exception thrown from exit:
 * @message com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_10 [com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_10] - Exit called on unknown participant: {0}
 * @message com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_11 [com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_11] - Unknown participant
 * @message com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_12 [com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_12] - Unknown error: {0}
 * @message com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_13 [com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_13] - Unexpected exception thrown from fault:
 * @message com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_14 [com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_14] - Fault called on unknown participant: {0}
 * @message com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_15 [com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_15] - Unknown participant
 */
public class CoordinatorCompletionCoordinatorProcessorImpl extends CoordinatorCompletionCoordinatorProcessor
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
    public void activateParticipant(final BAParticipantManager participant, final String identifier)
    {
        activatedObjectProcessor.activateObject(participant, identifier) ;
    }

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public void deactivateParticipant(final BAParticipantManager participant)
    {
        activatedObjectProcessor.deactivateObject(participant) ;
    }
    
    /**
     * Get the participant with the specified identifier.
     * @param instanceIdentifier The participant identifier.
     * @return The participant or null if not known.
     */
    private BAParticipantManager getParticipant(final InstanceIdentifier instanceIdentifier)
    {
        final String identifier = (instanceIdentifier != null ? instanceIdentifier.getInstanceIdentifier() : null) ;
        return (BAParticipantManager)activatedObjectProcessor.getObject(identifier) ;
    }
    
    /**
     * Exit.
     * @param exit The exit notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void exit(final NotificationType exit, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final BAParticipantManager participant = getParticipant(instanceIdentifier) ;

        try
        {
            if (participant != null)
            {
                final String messageId = MessageId.getMessageId() ;
                try
                {
                    participant.exit() ;
                }
                catch (final WrongStateException wse)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.WRONGSTATE_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_6")) ;
                    CoordinatorCompletionParticipantClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final UnknownTransactionException ute)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_7")) ;
                    CoordinatorCompletionParticipantClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final SystemException se)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_8") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    CoordinatorCompletionParticipantClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_9", th) ;
                    }
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault(th) ;
                    CoordinatorCompletionParticipantClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                final AddressingContext responseAddressingContext =
                    AddressingContext.createNotificationContext(addressingContext, messageId) ;
                CoordinatorCompletionParticipantClient.getClient().sendExited(responseAddressingContext, instanceIdentifier) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_10", new Object[] {instanceIdentifier}) ;
                }
                final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                    WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_11")) ;
                CoordinatorCompletionParticipantClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
            }
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace(System.err);
        }
    }


    /**
     * Fault.
     * @param fault The fault notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void fault(final ExceptionType fault, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final BAParticipantManager participant = getParticipant(instanceIdentifier) ;

        try
        {
            if (participant != null)
            {
                final String messageId = MessageId.getMessageId() ;
                try
                {
                    participant.fault() ;
                }
                catch (final SystemException se)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_12") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    CoordinatorCompletionParticipantClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_13", th) ;
                    }
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault(th) ;
                    CoordinatorCompletionParticipantClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                final AddressingContext responseAddressingContext =
                    AddressingContext.createNotificationContext(addressingContext, messageId) ;
                CoordinatorCompletionParticipantClient.getClient().sendFaulted(responseAddressingContext, instanceIdentifier) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_14", new Object[] {instanceIdentifier}) ;
                }
                final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                    WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl_15")) ;
                CoordinatorCompletionParticipantClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
            }
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace(System.err);
        }
    }
    
    /**
     * Get Status.
     * @param getStatus The get status notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void getStatus(final NotificationType getStatus, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        // Not implemented yet
    }
}
