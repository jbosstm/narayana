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
import com.arjuna.webservices.wsat.NotificationType;
import com.arjuna.webservices.wsat.client.CompletionInitiatorClient;
import com.arjuna.webservices.wsat.processors.CompletionCoordinatorProcessor;
import com.arjuna.wsc.messaging.MessageId;
import com.arjuna.wst.CompletionCoordinatorParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;

/**
 * The Completion Coordinator processor.
 * @author kevin
 * @message com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_1 [com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_1] - Unknown transaction
 * @message com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_2 [com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_2] - Unknown error: {0}
 * @message com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_3 [com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_3] - Unexpected exception thrown from commit: 
 * @message com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_4 [com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_4] - Commit called on unknown participant: {0}
 * @message com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_5 [com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_5] - Unknown participant
 * @message com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_6 [com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_6] - Unknown transaction
 * @message com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_7 [com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_7] - Unknown error: {0}
 * @message com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_8 [com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_8] - Unexpected exception thrown from rollback: 
 * @message com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_9 [com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_9] - Rollback called on unknown participant: {0}
 * @message com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_10 [com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_10] - Unknown participant
 */
public class CompletionCoordinatorProcessorImpl extends CompletionCoordinatorProcessor
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
    public void activateParticipant(final CompletionCoordinatorParticipant participant, final String identifier)
    {
        activatedObjectProcessor.activateObject(participant, identifier) ;
    }

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public void deactivateParticipant(final CompletionCoordinatorParticipant participant)
    {
        activatedObjectProcessor.deactivateObject(participant) ;
    }
    
    /**
     * Get the participant with the specified identifier.
     * @param instanceIdentifier The participant identifier.
     * @return The participant or null if not known.
     */
    private CompletionCoordinatorParticipant getParticipant(final InstanceIdentifier instanceIdentifier)
    {
        final String identifier = (instanceIdentifier != null ? instanceIdentifier.getInstanceIdentifier() : null) ;
        return (CompletionCoordinatorParticipant)activatedObjectProcessor.getObject(identifier) ;
    }
    
    /**
     * Commit.
     * @param commit The commit notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void commit(final NotificationType commit, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CompletionCoordinatorParticipant participant = getParticipant(instanceIdentifier) ;

        try
        {
            if (participant != null)
            {
                final String messageId = MessageId.getMessageId() ;
                try
                {
                    participant.commit() ;
                }
                catch (final TransactionRolledBackException trbe)
                {
                    final AddressingContext responseAddressingContext =
                        AddressingContext.createNotificationContext(addressingContext, messageId) ;
                    CompletionInitiatorClient.getClient().sendAborted(responseAddressingContext, instanceIdentifier) ;
                    return ;
                }
                catch (final UnknownTransactionException ute)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_1")) ;
                    CompletionInitiatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final SystemException se)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_2") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    CompletionInitiatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_3", th) ;
                    }
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault(th) ;
                    CompletionInitiatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                final AddressingContext responseAddressingContext =
                    AddressingContext.createNotificationContext(addressingContext, messageId) ;
                CompletionInitiatorClient.getClient().sendCommitted(responseAddressingContext, instanceIdentifier) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_4", new Object[] {instanceIdentifier}) ;
                }
                final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_5")) ;
                CompletionInitiatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
            }
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace(System.err);
        }
    }
    
    /**
     * Rollback.
     * @param rollback The rollback notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void rollback(final NotificationType rollback, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CompletionCoordinatorParticipant participant = getParticipant(instanceIdentifier) ;

        try
        {
            if (participant != null)
            {
                final String messageId = MessageId.getMessageId() ;
                try
                {
                    participant.rollback() ;
                }
                catch (final UnknownTransactionException ute)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_6")) ;
                    CompletionInitiatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (SystemException systemException)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_7") ;
                    final String message = MessageFormat.format(pattern, new Object[] {systemException}) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    CompletionInitiatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_8", th) ;
                    }
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault(th) ;
                    CompletionInitiatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                final AddressingContext responseAddressingContext =
                    AddressingContext.createNotificationContext(addressingContext, messageId) ;
                CompletionInitiatorClient.getClient().sendAborted(responseAddressingContext, instanceIdentifier) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_9", new Object[] {instanceIdentifier}) ;
                }
                final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                    WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl_10")) ;
                CompletionInitiatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
            }
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace(System.err);
        }
    }
}
