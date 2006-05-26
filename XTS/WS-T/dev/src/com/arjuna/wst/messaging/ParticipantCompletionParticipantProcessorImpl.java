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
import com.arjuna.webservices.wsba.client.ParticipantCompletionCoordinatorClient;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.wsc.messaging.MessageId;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;


/**
 * The Participant Completion Participant processor.
 * @author kevin
 * 
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_1 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_1] - Wrong state
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_2 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_2] - Unknown error: {0}
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_3 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_3] - Unexpected exception thrown from cancel:
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_4 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_4] - Cancel called on unknown participant: {0}
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_5 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_5] - Unknown participant
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_6 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_6] - Wrong state
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_7 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_7] - Unknown error: {0}
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_8 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_8] - Unexpected exception thrown from close:
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_9 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_9] - Close called on unknown participant: {0}
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_10 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_10] - Unknown participant
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_11 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_11] - Wrong state
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_12 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_12] - Unknown error: {0}
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_13 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_13] - Unexpected exception thrown from compensate:
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_14 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_14] - Compensate called on unknown participant: {0}
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_15 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_15] - Unknown error: {0}
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_16 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_16] - Unexpected exception thrown from getStatus:
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_17 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_17] - Complete called on unknown participant: {0}
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_18 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_18] - Unexpected exception thrown from soapFault:
 * @message com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_19 [com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_19] - SoapFault called on unknown participant: {0}
 *  
 */
public class ParticipantCompletionParticipantProcessorImpl extends ParticipantCompletionParticipantProcessor
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
    public void activateParticipant(final BusinessAgreementWithParticipantCompletionParticipant participant, final String identifier)
    {
        activatedObjectProcessor.activateObject(participant, identifier) ;
    }

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public void deactivateParticipant(final BusinessAgreementWithParticipantCompletionParticipant participant)
    {
        activatedObjectProcessor.deactivateObject(participant) ;
    }
    
    /**
     * Get the participant with the specified identifier.
     * @param instanceIdentifier The participant identifier.
     * @return The participant or null if not known.
     */
    private BusinessAgreementWithParticipantCompletionParticipant getParticipant(final InstanceIdentifier instanceIdentifier)
    {
        final String identifier = (instanceIdentifier != null ? instanceIdentifier.getInstanceIdentifier() : null) ;
        return (BusinessAgreementWithParticipantCompletionParticipant)activatedObjectProcessor.getObject(identifier) ;
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
        final BusinessAgreementWithParticipantCompletionParticipant participant = getParticipant(instanceIdentifier) ;

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
                        WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_1")) ;
                    ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final SystemException se)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_2") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_3", th) ;
                    }
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault(th) ;
                    ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                final AddressingContext responseAddressingContext =
                    AddressingContext.createNotificationContext(addressingContext, messageId) ;
                ParticipantCompletionCoordinatorClient.getClient().sendCancelled(responseAddressingContext, instanceIdentifier) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_4", new Object[] {instanceIdentifier}) ;
                }
                final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                    WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_5")) ;
                ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
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
        final BusinessAgreementWithParticipantCompletionParticipant participant = getParticipant(instanceIdentifier) ;

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
                        WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_6")) ;
                    ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final SystemException se)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_7") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_8", th) ;
                    }
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault(th) ;
                    ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                final AddressingContext responseAddressingContext =
                    AddressingContext.createNotificationContext(addressingContext, messageId) ;
                ParticipantCompletionCoordinatorClient.getClient().sendClosed(responseAddressingContext, instanceIdentifier) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_9", new Object[] {instanceIdentifier}) ;
                }
                final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                     WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_10")) ;
                ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
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
        final BusinessAgreementWithParticipantCompletionParticipant participant = getParticipant(instanceIdentifier) ;

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
                    ParticipantCompletionCoordinatorClient.getClient().sendFault(responseAddressingContext, instanceIdentifier, null) ;
                    return ;
                }
                catch (final WrongStateException wse)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.WRONGSTATE_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_11")) ;
                    ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final SystemException se)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_12") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_13", th) ;
                    }
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault(th) ;
                    ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                final AddressingContext responseAddressingContext =
                    AddressingContext.createNotificationContext(addressingContext, messageId) ;
                ParticipantCompletionCoordinatorClient.getClient().sendCompensated(responseAddressingContext, instanceIdentifier) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_14", new Object[] {instanceIdentifier}) ;
                }
                final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                    "Unknown participant") ;
                ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
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
        final BusinessAgreementWithParticipantCompletionParticipant participant = getParticipant(instanceIdentifier) ;

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
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_15") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_16", th) ;
                    }
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault(th) ;
                    ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                final AddressingContext responseAddressingContext =
                    AddressingContext.createNotificationContext(addressingContext, messageId) ;
                ParticipantCompletionCoordinatorClient.getClient().sendStatus(responseAddressingContext, instanceIdentifier, state) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_17", new Object[] {instanceIdentifier}) ;
                }
                final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                    "Unknown participant") ;
                ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
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
        final BusinessAgreementWithParticipantCompletionParticipant participant = getParticipant(instanceIdentifier) ;

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
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_18", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl_19", new Object[] {instanceIdentifier}) ;
            }
        }
    }
}
