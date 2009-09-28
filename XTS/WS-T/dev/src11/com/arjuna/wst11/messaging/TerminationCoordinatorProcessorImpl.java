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

import com.arjuna.schemas.ws._2005._10.wsarjtx.NotificationType;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.base.processors.ActivatedObjectProcessor;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.wsf.common.addressing.MAP;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsarjtx.client.TerminationParticipantClient;
import com.arjuna.webservices11.wsarjtx.processors.TerminationCoordinatorProcessor;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst11.BusinessActivityTerminator;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.text.MessageFormat;

/**
 * The Terminator Participant processor.
 * @author kevin
 *
 * @message com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_1 [com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_1] - Unknown transaction
 * @message com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_2 [com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_2] - Unknown error: {0}
 * @message com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_3 [com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_3] - Unexpected exception thrown from cancel:
 * @message com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_4 [com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_4] - Cancel called on unknown participant: {0}
 * @message com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_5 [com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_5] - Unknown participant
 * @message com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_6 [com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_6] - Unknown transaction
 * @message com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_7 [com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_7] - Transaction rolled back
 * @message com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_8 [com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_8] - Unknown error: {0}
 * @message com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_9 [com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_9] - Unexpected exception thrown from close:
 * @message com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_10 [com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_10] - Close called on unknown participant: {0}
 * @message com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_11 [com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_11] - Unknown participant
 * @message com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_12 [com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_12] - Unknown transaction
 * @message com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_13 [com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_13] - Unknown error: {0}
 * @message com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_14 [com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_14] - Unexpected exception thrown from complete:
 * @message com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_15 [com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_15] - Complete called on unknown participant: {0}
 * @message com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_16 [com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_16] - Unknown participant
 */
public class TerminationCoordinatorProcessorImpl extends TerminationCoordinatorProcessor
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
    public void activateParticipant(final BusinessActivityTerminator participant, final String identifier)
    {
        activatedObjectProcessor.activateObject(participant, identifier) ;
    }

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public void deactivateParticipant(final BusinessActivityTerminator participant)
    {
        activatedObjectProcessor.deactivateObject(participant) ;
    }

   /**
     * Get the participant with the specified identifier.
     * @param instanceIdentifier The participant identifier.
     * @return The participant or null if not known.
     */
    public BusinessActivityTerminator getParticipant(final InstanceIdentifier instanceIdentifier)
    {
        final String identifier = (instanceIdentifier != null ? instanceIdentifier.getInstanceIdentifier() : null) ;
        return (com.arjuna.wst11.BusinessActivityTerminator)activatedObjectProcessor.getObject(identifier) ;
    }

    /**
     * Cancel.
     * @param cancel The cancel notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void cancel(final NotificationType cancel, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final BusinessActivityTerminator participant = getParticipant(instanceIdentifier) ;

        try
        {
            if (participant != null)
            {
                W3CEndpointReference endpoint = participant.getEndpoint();

                final String messageId = MessageId.getMessageId() ;
                try
                {
                    participant.cancel() ;
                }
                catch (final FaultedException fe)
                {
                    final MAP responseMAP = AddressingHelper.createNotificationContext(messageId) ;
                    TerminationParticipantClient.getClient().sendFaulted(endpoint, responseMAP, instanceIdentifier) ;
                }
                catch (final UnknownTransactionException ute)
                {
                    final MAP faultMAP = AddressingHelper.createFaultContext(map, messageId) ;
                    final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_1")) ;
                    TerminationParticipantClient.getClient().sendSoapFault(endpoint, faultMAP, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final SystemException se)
                {
                    final MAP faultMAP = AddressingHelper.createFaultContext(map, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_2") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    TerminationParticipantClient.getClient().sendSoapFault(endpoint, faultMAP, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_3", th) ;
                    }
                    final MAP faultMAP = AddressingHelper.createFaultContext(map, messageId) ;
                    final SoapFault soapFault = new SoapFault11(th) ;
                    TerminationParticipantClient.getClient().sendSoapFault(endpoint, faultMAP, soapFault, instanceIdentifier) ;
                    return ;
                }
                final MAP responseMAP = AddressingHelper.createNotificationContext(messageId) ;
                TerminationParticipantClient.getClient().sendCancelled(endpoint, responseMAP, instanceIdentifier) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_4", new Object[] {instanceIdentifier}) ;
                }
                final MAP faultMAP =
                        AddressingHelper.createFaultContext(map, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_5")) ;
                 TerminationParticipantClient.getClient().sendSoapFault(soapFault, faultMAP, instanceIdentifier) ;
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
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void close(final NotificationType close, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final BusinessActivityTerminator participant = getParticipant(instanceIdentifier) ;

        try
        {
            if (participant != null)
            {
                W3CEndpointReference endpoint = participant.getEndpoint();

                final String messageId = MessageId.getMessageId() ;
                try
                {
                    participant.close() ;
                }
                catch (final UnknownTransactionException ute)
                {
                    final MAP faultMAP = AddressingHelper.createFaultContext(map, messageId) ;
                    final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_6")) ;
                    TerminationParticipantClient.getClient().sendSoapFault(endpoint, faultMAP, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final TransactionRolledBackException trbe)
                {
                    final MAP faultMAP = AddressingHelper.createFaultContext(map, messageId) ;
                    final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.TRANSACTIONROLLEDBACK_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_7")) ;
                    TerminationParticipantClient.getClient().sendSoapFault(endpoint, faultMAP, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final SystemException se)
                {
                    final MAP faultMAP = AddressingHelper.createFaultContext(map, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_8") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    TerminationParticipantClient.getClient().sendSoapFault(endpoint, faultMAP, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_9", th) ;
                    }
                    final MAP faultMAP = AddressingHelper.createFaultContext(map, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault11(th) ;
                    TerminationParticipantClient.getClient().sendSoapFault(endpoint, faultMAP, soapFault, instanceIdentifier) ;
                    return ;
                }
                final MAP responseMAP = AddressingHelper.createNotificationContext(messageId) ;
                TerminationParticipantClient.getClient().sendClosed(endpoint, responseMAP, instanceIdentifier) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_10", new Object[] {instanceIdentifier}) ;
                }
                final MAP faultMAP =
                        AddressingHelper.createFaultContext(map, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_11")) ;
                TerminationParticipantClient.getClient().sendSoapFault(soapFault, faultMAP, instanceIdentifier) ;
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
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void complete(final NotificationType complete, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final BusinessActivityTerminator participant = getParticipant(instanceIdentifier) ;

        try
        {
            if (participant != null)
            {
                W3CEndpointReference endpoint = participant.getEndpoint();

                final String messageId = MessageId.getMessageId() ;
                try
                {
                    participant.complete() ;
                }
                catch (final FaultedException fe)
                {
                    final MAP responseMAP = AddressingHelper.createNotificationContext(messageId) ;
                    TerminationParticipantClient.getClient().sendFaulted(endpoint, responseMAP, instanceIdentifier) ;
                }
                catch (final UnknownTransactionException ute)
                {
                    final MAP faultMAP = AddressingHelper.createFaultContext(map, messageId) ;
                    final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_12")) ;
                    TerminationParticipantClient.getClient().sendSoapFault(endpoint, faultMAP, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final SystemException se)
                {
                    final MAP faultMAP = AddressingHelper.createFaultContext(map, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_13") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    TerminationParticipantClient.getClient().sendSoapFault(endpoint, faultMAP, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_14", th) ;
                    }
                    final MAP faultMAP = AddressingHelper.createFaultContext(map, messageId) ;
                    final SoapFault soapFault = new SoapFault11(th) ;
                    TerminationParticipantClient.getClient().sendSoapFault(endpoint, faultMAP, soapFault, instanceIdentifier) ;
                    return ;
                }
                final MAP responseMAP = AddressingHelper.createNotificationContext(messageId) ;
                TerminationParticipantClient.getClient().sendCompleted(endpoint, responseMAP, instanceIdentifier) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_15", new Object[] {instanceIdentifier}) ;
                }
                final MAP faultMAP =
                        AddressingHelper.createFaultContext(map, MessageId.getMessageId()) ;
                final SoapFault soapFault =
                        new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                                WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_16")) ;
                TerminationParticipantClient.getClient().sendSoapFault(soapFault, faultMAP, instanceIdentifier) ;
            }
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace(System.err);
        }
    }

    /**
     * handle a soap fault sent by the participant.
     *
     * kev's code just prints a log message?
     *
     * @param soapFault The soap fault
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     * @message com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_17 [com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_17] - Service {0} received unexpected fault: {1}
     */
    public void soapFault(final SoapFault soapFault, final MAP map,
        final ArjunaContext arjunaContext)
    {
        // in this case all we do is log a message

        if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.TerminationCoordinatorProcessorImpl_17",
                    new Object[] {ArjunaTXConstants.SERVICE_TERMINATION_COORDINATOR, soapFault}) ;
        }
    }
}