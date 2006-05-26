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
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.State;
import com.arjuna.webservices.wsba.client.CoordinatorCompletionCoordinatorClient;
import com.arjuna.webservices.wsba.processors.CoordinatorCompletionParticipantProcessor;
import com.arjuna.wsc.messaging.MessageId;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;


/**
 * The Coordinator Completion Participant processor.
 * @author kevin
 * 
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_1 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_1] - Wrong state
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_2 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_2] - Unknown error: {0}
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_3 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_3] - Unexpected exception thrown from cancel:
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_4 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_4] - Cancel called on unknown participant: {0}
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_5 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_5] - Unknown participant
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_6 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_6] - Wrong state
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_7 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_7] - Unknown error: {0}
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_8 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_8] - Unexpected exception thrown from close:
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_9 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_9] - Close called on unknown participant: {0}
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_10 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_10] - Unknown participant
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_11 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_11] - Wrong state
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_12 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_12] - Unknown error: {0}
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_13 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_13] - Unexpected exception thrown from compensate:
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_14 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_14] - Compensate called on unknown participant: {0}
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_15 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_15] - Unknown participant
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_16 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_16] - Wrong state
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_17 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_17] - Unknown error: {0}
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_18 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_18] - Unexpected exception thrown from complete:
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_19 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_19] - Complete called on unknown participant: {0}
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_20 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_20] - Unknown participant
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_21 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_21] - Unknown error: {0}
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_22 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_22] - Unexpected exception thrown from getStatus:
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_23 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_23] - Complete called on unknown participant: {0}
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_24 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_24] - Unknown participant
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_25 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_25] - Unexpected exception thrown from soapFault:
 * @message com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_26 [com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_26] - SoapFault called on unknown participant: {0}
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
    public void activateParticipant(final BusinessAgreementWithCoordinatorCompletionParticipant participant, final String identifier)
    {
        activatedObjectProcessor.activateObject(participant, identifier) ;
    }

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public void deactivateParticipant(final BusinessAgreementWithCoordinatorCompletionParticipant participant)
    {
        activatedObjectProcessor.deactivateObject(participant) ;
    }
    
    /**
     * Get the participant with the specified identifier.
     * @param instanceIdentifier The participant identifier.
     * @return The participant or null if not known.
     */
    private BusinessAgreementWithCoordinatorCompletionParticipant getParticipant(final InstanceIdentifier instanceIdentifier)
    {
        final String identifier = (instanceIdentifier != null ? instanceIdentifier.getInstanceIdentifier() : null) ;
        return (BusinessAgreementWithCoordinatorCompletionParticipant)activatedObjectProcessor.getObject(identifier) ;
    }

    /**
     * Cancel.
     * @param cancel The cancel notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void cancel(final NotificationType cancel, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final BusinessAgreementWithCoordinatorCompletionParticipant participant = getParticipant(instanceIdentifier) ;

        try
        {
            if (participant != null)
            {
                final String messageId = MessageId.getMessageId() ;
                try
                {
                    participant.cancel() ;
                }
                catch (final WrongStateException wse)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.WRONGSTATE_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_1")) ;
                    CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final SystemException se)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_2") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_3", th) ;
                    }
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault(th) ;
                    CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                final AddressingContext responseAddressingContext =
                    AddressingContext.createNotificationContext(addressingContext, messageId) ;
                CoordinatorCompletionCoordinatorClient.getClient().sendCancelled(responseAddressingContext, instanceIdentifier) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_4", new Object[] {instanceIdentifier}) ;
                }
                final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                    WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_5")) ;
                CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
            }
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace(System.err);
        }
    }
    
    /**
     * Close.
     * @param close The close notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void close(final NotificationType close, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final BusinessAgreementWithCoordinatorCompletionParticipant participant = getParticipant(instanceIdentifier) ;

        try
        {
            if (participant != null)
            {
                final String messageId = MessageId.getMessageId() ;
                try
                {
                    participant.close() ;
                }
                catch (final WrongStateException wse)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.WRONGSTATE_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_6")) ;
                    CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final SystemException se)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_7") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_8", th) ;
                    }
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault(th) ;
                    CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                final AddressingContext responseAddressingContext =
                    AddressingContext.createNotificationContext(addressingContext, messageId) ;
                CoordinatorCompletionCoordinatorClient.getClient().sendClosed(responseAddressingContext, instanceIdentifier) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_9", new Object[] {instanceIdentifier}) ;
                }
                final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                    WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_10")) ;
                CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
            }
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace(System.err);
        }
    }
    
    /**
     * Compensate.
     * @param compensate The compensate notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void compensate(final NotificationType compensate, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final BusinessAgreementWithCoordinatorCompletionParticipant participant = getParticipant(instanceIdentifier) ;

        try
        {
            if (participant != null)
            {
                final String messageId = MessageId.getMessageId() ;
                try
                {
                    participant.compensate() ;
                }
                catch (final FaultedException fe)
                {
                    final AddressingContext responseAddressingContext =
                        AddressingContext.createNotificationContext(addressingContext, messageId) ;
                    CoordinatorCompletionCoordinatorClient.getClient().sendFault(responseAddressingContext, instanceIdentifier, null) ;
                    return ;
                }
                catch (final WrongStateException wse)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.WRONGSTATE_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_11")) ;
                    CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final SystemException se)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_12") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_13", th) ;
                    }
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault(th) ;
                    CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                final AddressingContext responseAddressingContext =
                    AddressingContext.createNotificationContext(addressingContext, messageId) ;
                CoordinatorCompletionCoordinatorClient.getClient().sendCompensated(responseAddressingContext, instanceIdentifier) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_14", new Object[] {instanceIdentifier}) ;
                }
                final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                    WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_15")) ;
                CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
            }
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace(System.err);
        }
    }
    
    /**
     * Complete.
     * @param complete The complete notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void complete(final NotificationType complete, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final BusinessAgreementWithCoordinatorCompletionParticipant participant = getParticipant(instanceIdentifier) ;

        try
        {
            if (participant != null)
            {
                final String messageId = MessageId.getMessageId() ;
                try
                {
                    participant.complete() ;
                }
                catch (final WrongStateException wse)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.WRONGSTATE_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_16")) ;
                    CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final SystemException se)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_17") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_18", th) ;
                    }
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault(th) ;
                    CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                final AddressingContext responseAddressingContext =
                    AddressingContext.createNotificationContext(addressingContext, messageId) ;
                CoordinatorCompletionCoordinatorClient.getClient().sendCompleted(responseAddressingContext, instanceIdentifier) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_19", new Object[] {instanceIdentifier}) ;
                }
                final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                    WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_20")) ;
                CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
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
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final BusinessAgreementWithCoordinatorCompletionParticipant participant = getParticipant(instanceIdentifier) ;

        try
        {
            if (participant != null)
            {
                final String messageId = MessageId.getMessageId() ;
                final State state ;
                try
                {
                    state = State.toState(participant.status()) ;
                }
                catch (final SystemException se)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_21") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_22", th) ;
                    }
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault(th) ;
                    CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                final AddressingContext responseAddressingContext =
                    AddressingContext.createNotificationContext(addressingContext, messageId) ;
                CoordinatorCompletionCoordinatorClient.getClient().sendStatus(responseAddressingContext, instanceIdentifier, state) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_23", new Object[] {instanceIdentifier}) ;
                }
                final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                    WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_24")) ;
                CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
            }
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace(System.err);
        }
    }
    
    /**
     * SOAP Fault.
     * @param soapFault The SOAP fault notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void soapFault(final SoapFault fault, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final BusinessAgreementWithCoordinatorCompletionParticipant participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.error() ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_25", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl_25", new Object[] {instanceIdentifier}) ;
            }
        }
    }
}
