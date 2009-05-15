package com.arjuna.wst11.messaging;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.base.processors.ActivatedObjectProcessor;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.jbossts.xts.wsaddr.map.MAP;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.client.CompletionInitiatorClient;
import com.arjuna.webservices11.wsat.processors.CompletionCoordinatorProcessor;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst11.CompletionCoordinatorParticipant;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

import java.text.MessageFormat;

/**
 * The Completion Coordinator processor.
 * @author kevin
 * @message com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_1 [com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_1] - Unknown transaction
 * @message com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_2 [com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_2] - Unknown error: {0}
 * @message com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_3 [com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_3] - Unexpected exception thrown from commit:
 * @message com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_4 [com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_4] - Commit called on unknown participant: {0}
 * @message com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_5 [com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_5] - Unknown participant
 * @message com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_6 [com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_6] - Unknown transaction
 * @message com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_7 [com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_7] - Unknown error: {0}
 * @message com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_8 [com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_8] - Unexpected exception thrown from rollback:
 * @message com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_9 [com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_9] - Rollback called on unknown participant: {0}
 * @message com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_10 [com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_10] - Unknown participant
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
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void commit(final Notification commit, final MAP map,
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
                    final MAP responseAddressingContext =
                        AddressingHelper.createResponseContext(map, messageId) ;
                    CompletionInitiatorClient.getClient().sendAborted(participant.getParticipant(), responseAddressingContext, instanceIdentifier) ;
                    return ;
                }
                catch (final UnknownTransactionException ute)
                {
                    final MAP faultAddressingContext = AddressingHelper.createFaultContext(map, messageId) ;
                    final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_1")) ;
                    CompletionInitiatorClient.getClient().sendSoapFault(participant.getParticipant(), faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final SystemException se)
                {
                    final MAP faultAddressingContext = AddressingHelper.createFaultContext(map, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_2") ;
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    CompletionInitiatorClient.getClient().sendSoapFault(participant.getParticipant(), faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_3", th) ;
                    }
                    final MAP faultAddressingContext = AddressingHelper.createFaultContext(map, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault11(th) ;
                    CompletionInitiatorClient.getClient().sendSoapFault(participant.getParticipant(), faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                final MAP responseAddressingContext = AddressingHelper.createResponseContext(map, messageId) ;
                CompletionInitiatorClient.getClient().sendCommitted(participant.getParticipant(), responseAddressingContext, instanceIdentifier) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_4", new Object[] {instanceIdentifier}) ;
                }
                final MAP faultAddressingContext = AddressingHelper.createFaultContext(map, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_5")) ;
                CompletionInitiatorClient.getClient().sendSoapFault(participant.getParticipant(), faultAddressingContext, soapFault, instanceIdentifier) ;
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
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void rollback(final Notification rollback, final MAP map,
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
                    final MAP faultAddressingContext = AddressingHelper.createFaultContext(map, messageId) ;
                    final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                        WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_6")) ;
                    CompletionInitiatorClient.getClient().sendSoapFault(participant.getParticipant(), faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (SystemException systemException)
                {
                    final MAP faultAddressingContext = AddressingHelper.createFaultContext(map, messageId) ;
                    final String pattern = WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_7") ;
                    final String message = MessageFormat.format(pattern, new Object[] {systemException}) ;
                    final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    CompletionInitiatorClient.getClient().sendSoapFault(participant.getParticipant(), faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_8", th) ;
                    }
                    final MAP faultAddressingContext = AddressingHelper.createFaultContext(map, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault11(th) ;
                    CompletionInitiatorClient.getClient().sendSoapFault(participant.getParticipant(), faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                final MAP responseAddressingContext = AddressingHelper.createResponseContext(map, messageId) ;
                CompletionInitiatorClient.getClient().sendAborted(participant.getParticipant(), responseAddressingContext, instanceIdentifier) ;
            }
            else
            {
                if (WSTLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_9", new Object[] {instanceIdentifier}) ;
                }
                final MAP faultAddressingContext = AddressingHelper.createFaultContext(map, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                    WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.CompletionCoordinatorProcessorImpl_10")) ;
                CompletionInitiatorClient.getClient().sendSoapFault(participant.getParticipant(), faultAddressingContext, soapFault, instanceIdentifier) ;
            }
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace(System.err);
        }
    }
}
