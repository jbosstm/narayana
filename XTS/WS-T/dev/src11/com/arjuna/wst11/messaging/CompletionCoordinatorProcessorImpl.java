package com.arjuna.wst11.messaging;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.base.processors.ActivatedObjectProcessor;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.wsf.common.addressing.MAP;
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
                            WSTLogger.i18NLogger.get_wst11_messaging_CompletionCoordinatorProcessorImpl_1()) ;
                    CompletionInitiatorClient.getClient().sendSoapFault(participant.getParticipant(), faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final SystemException se)
                {
                    final MAP faultAddressingContext = AddressingHelper.createFaultContext(map, messageId) ;
                    final String pattern = WSTLogger.i18NLogger.get_wst11_messaging_CompletionCoordinatorProcessorImpl_2();
                    final String message = MessageFormat.format(pattern, new Object[] {se}) ;
                    final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    CompletionInitiatorClient.getClient().sendSoapFault(participant.getParticipant(), faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.logger.isTraceEnabled())
                    {
                        WSTLogger.logger.tracev("Unexpected exception thrown from commit:", th) ;
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
                if (WSTLogger.logger.isTraceEnabled())
                {
                    WSTLogger.logger.tracev("Commit called on unknown participant: {0}", new Object[] {instanceIdentifier}) ;
                }
                final MAP faultAddressingContext = AddressingHelper.createFaultContext(map, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                        WSTLogger.i18NLogger.get_wst11_messaging_CompletionCoordinatorProcessorImpl_5()) ;
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
                            WSTLogger.i18NLogger.get_wst11_messaging_CompletionCoordinatorProcessorImpl_6()) ;
                    CompletionInitiatorClient.getClient().sendSoapFault(participant.getParticipant(), faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (SystemException systemException)
                {
                    final MAP faultAddressingContext = AddressingHelper.createFaultContext(map, messageId) ;
                    final String message = WSTLogger.i18NLogger.get_wst11_messaging_CompletionCoordinatorProcessorImpl_7();
                    final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME, message) ;
                    CompletionInitiatorClient.getClient().sendSoapFault(participant.getParticipant(), faultAddressingContext, soapFault, instanceIdentifier) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.logger.isTraceEnabled())
                    {
                        WSTLogger.logger.tracev("Unexpected exception thrown from rollback:", th) ;
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
                if (WSTLogger.logger.isTraceEnabled())
                {
                    WSTLogger.logger.tracev("Rollback called on unknown participant: {0}", new Object[] {instanceIdentifier}) ;
                }
                final MAP faultAddressingContext = AddressingHelper.createFaultContext(map, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME,
                        WSTLogger.i18NLogger.get_wst11_messaging_CompletionCoordinatorProcessorImpl_10()) ;
                CompletionInitiatorClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
            }
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace(System.err);
        }
    }
}
