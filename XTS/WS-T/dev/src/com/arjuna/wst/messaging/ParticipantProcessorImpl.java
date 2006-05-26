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
import com.arjuna.webservices.wsat.Participant;
import com.arjuna.webservices.wsat.client.CoordinatorClient;
import com.arjuna.webservices.wsat.processors.ParticipantProcessor;
import com.arjuna.wsc.messaging.MessageId;
import com.arjuna.wst.Aborted;
import com.arjuna.wst.Prepared;
import com.arjuna.wst.ReadOnly;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;

/**
 * The Participant processor.
 * @author kevin
 * 
 * @message com.arjuna.wst.messaging.ParticipantProcessorImpl_1 [com.arjuna.wst.messaging.ParticipantProcessorImpl_1] - Wrong state
 * @message com.arjuna.wst.messaging.ParticipantProcessorImpl_2 [com.arjuna.wst.messaging.ParticipantProcessorImpl_2] - Unknown error: {0}
 * @message com.arjuna.wst.messaging.ParticipantProcessorImpl_3 [com.arjuna.wst.messaging.ParticipantProcessorImpl_3] - Unexpected exception thrown from commit:
 * @message com.arjuna.wst.messaging.ParticipantProcessorImpl_4 [com.arjuna.wst.messaging.ParticipantProcessorImpl_4] - Commit called on unknown participant: {0}
 * @message com.arjuna.wst.messaging.ParticipantProcessorImpl_5 [com.arjuna.wst.messaging.ParticipantProcessorImpl_5] - Unknown participant
 * @message com.arjuna.wst.messaging.ParticipantProcessorImpl_6 [com.arjuna.wst.messaging.ParticipantProcessorImpl_6] - Wrong state
 * @message com.arjuna.wst.messaging.ParticipantProcessorImpl_7 [com.arjuna.wst.messaging.ParticipantProcessorImpl_7] - Unknown error: {0}
 * @message com.arjuna.wst.messaging.ParticipantProcessorImpl_8 [com.arjuna.wst.messaging.ParticipantProcessorImpl_8] - Unexpected exception thrown from prepare: 
 * @message com.arjuna.wst.messaging.ParticipantProcessorImpl_9 [com.arjuna.wst.messaging.ParticipantProcessorImpl_9] - Unknown error
 * @message com.arjuna.wst.messaging.ParticipantProcessorImpl_10 [com.arjuna.wst.messaging.ParticipantProcessorImpl_10] - Prepare called on unknown participant: {0}
 * @message com.arjuna.wst.messaging.ParticipantProcessorImpl_11 [com.arjuna.wst.messaging.ParticipantProcessorImpl_11] - Unknown participant
 * @message com.arjuna.wst.messaging.ParticipantProcessorImpl_12 [com.arjuna.wst.messaging.ParticipantProcessorImpl_12] - Wrong state
 * @message com.arjuna.wst.messaging.ParticipantProcessorImpl_13 [com.arjuna.wst.messaging.ParticipantProcessorImpl_13] - Unknown error: {0}
 * @message com.arjuna.wst.messaging.ParticipantProcessorImpl_14 [com.arjuna.wst.messaging.ParticipantProcessorImpl_14] - Unexpected exception thrown from rollback: 
 * @message com.arjuna.wst.messaging.ParticipantProcessorImpl_15 [com.arjuna.wst.messaging.ParticipantProcessorImpl_15] - Rollback called on unknown participant: {0}
 * @message com.arjuna.wst.messaging.ParticipantProcessorImpl_16 [com.arjuna.wst.messaging.ParticipantProcessorImpl_16] - Unknown participant
 * @message com.arjuna.wst.messaging.ParticipantProcessorImpl_17 [com.arjuna.wst.messaging.ParticipantProcessorImpl_17] - Unexpected exception thrown from soapFault: 
 * @message com.arjuna.wst.messaging.ParticipantProcessorImpl_18 [com.arjuna.wst.messaging.ParticipantProcessorImpl_18] - SoapFault called on unknown participant: {0}
 */
public class ParticipantProcessorImpl extends ParticipantProcessor
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
    public void activateParticipant(final Participant participant, final String identifier)
    {
        activatedObjectProcessor.activateObject(participant, identifier) ;
    }

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public void deactivateParticipant(final Participant participant)
    {
        activatedObjectProcessor.deactivateObject(participant) ;
    }
    
    /**
     * Get the participant with the specified identifier.
     * @param instanceIdentifier The participant identifier.
     * @return The participant or null if not known.
     */
    private Participant getParticipant(final InstanceIdentifier instanceIdentifier)
    {
        final String identifier = (instanceIdentifier != null ? instanceIdentifier.getInstanceIdentifier() : null) ;
        return (Participant)activatedObjectProcessor.getObject(identifier) ;
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
        final Participant participant = getParticipant(instanceIdentifier) ;

        try
        {
            if (participant != null)
            {
                final String messageId = MessageId.getMessageId() ;
                try
                {
                    participant.commit() ;
                }
                catch (final WrongStateException wse)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.WRONGSTATE_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantProcessorImpl_1")) ;
                    CoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final SystemException se)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantProcessorImpl_2") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    CoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantProcessorImpl_3", th) ; 
                    }
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault(th) ;
                    CoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                final AddressingContext responseAddressingContext =
                    AddressingContext.createNotificationContext(addressingContext, messageId) ;
                CoordinatorClient.getClient().sendCommitted(responseAddressingContext, instanceIdentifier) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantProcessorImpl_4", new Object[] {instanceIdentifier}) ; 
                }
                final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                    WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantProcessorImpl_5")) ;
                CoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
            }
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace(System.err);
        }
    }
    
    /**
     * Prepare.
     * @param prepare The prepare notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void prepare(final NotificationType prepare, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final Participant participant = getParticipant(instanceIdentifier) ;

        try
        {
            if (participant != null)
            {
                final String messageId = MessageId.getMessageId() ;
                final Vote vote ;
                try
                {
                    vote = participant.prepare();
                }
                catch (final WrongStateException wse)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.WRONGSTATE_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantProcessorImpl_6")) ;
                    CoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final SystemException se)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantProcessorImpl_7") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    CoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantProcessorImpl_8", th) ; 
                    }
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault(th) ;
                    CoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                final AddressingContext responseAddressingContext =
                    AddressingContext.createNotificationContext(addressingContext, messageId) ;
                if (vote instanceof Prepared)
                {
                    CoordinatorClient.getClient().sendPrepared(responseAddressingContext, instanceIdentifier) ;
                }
                else if (vote instanceof ReadOnly)
                {
                    CoordinatorClient.getClient().sendReadOnly(responseAddressingContext, instanceIdentifier) ;
                }
                else if (vote instanceof Aborted)
                {
                    CoordinatorClient.getClient().sendAborted(responseAddressingContext, instanceIdentifier) ;
                }
                else
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantProcessorImpl_9")) ;
                    CoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                }
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantProcessorImpl_10", new Object[] {instanceIdentifier}) ;
                }
                final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                    WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantProcessorImpl_11")) ;
                CoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
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
        final Participant participant = getParticipant(instanceIdentifier) ;

        try
        {
            if (participant != null)
            {
                final String messageId = MessageId.getMessageId() ;
                try
                {
                    participant.rollback() ;
                }
                catch (final WrongStateException wse)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.WRONGSTATE_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantProcessorImpl_12")) ;
                    CoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final SystemException se)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantProcessorImpl_13") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    CoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantProcessorImpl_14", th) ;
                    }
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault(th) ;
                    CoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                final AddressingContext responseAddressingContext =
                    AddressingContext.createNotificationContext(addressingContext, messageId) ;
                CoordinatorClient.getClient().sendAborted(responseAddressingContext, instanceIdentifier) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantProcessorImpl_15", new Object[] {instanceIdentifier}) ;
                }
                final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                    WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.ParticipantProcessorImpl_16")) ;
                CoordinatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
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
        final Participant participant = getParticipant(instanceIdentifier) ;

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
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantProcessorImpl_17", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.ParticipantProcessorImpl_18", new Object[] {instanceIdentifier}) ;
            }
        }
    }
}
