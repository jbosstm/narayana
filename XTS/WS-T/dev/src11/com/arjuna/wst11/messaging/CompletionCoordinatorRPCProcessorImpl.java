package com.arjuna.wst11.messaging;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.soap.SOAPFaultException;

import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.base.processors.ActivatedObjectProcessor;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.processors.CompletionCoordinatorRPCProcessor;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.wst.*;
import com.arjuna.wst11.CompletionCoordinatorParticipant;
import org.jboss.wsf.common.addressing.MAP;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

/**
 * The Completion Coordinator processor.
 * @author kevin
 */
public class CompletionCoordinatorRPCProcessorImpl extends CompletionCoordinatorRPCProcessor
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
    public boolean commit(final Notification commit, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CompletionCoordinatorParticipant participant = getParticipant(instanceIdentifier) ;

        {
            try {
            if (participant != null)
            {
                try
                {
                    participant.commit() ;
                }
                catch (final TransactionRolledBackException trbe)
                {
                    return false;
                }
                catch (final UnknownTransactionException ute)
                {
                    SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME);
                    soapFault.addDetail().addDetailEntry(ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME).addTextNode(WSTLogger.i18NLogger.get_wst11_messaging_CompletionCoordinatorProcessorImpl_1());
                    throw new SOAPFaultException(soapFault);
                }
                catch (final SystemException se)
                {
                    SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME);
                    soapFault.addDetail().addDetailEntry(ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME).addTextNode(WSTLogger.i18NLogger.get_wst11_messaging_CompletionCoordinatorProcessorImpl_2(se));
                    throw new SOAPFaultException(soapFault);
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.logger.isTraceEnabled())
                    {
                        WSTLogger.logger.tracev("Unexpected exception thrown from commit:", th) ;
                    }
                    SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME);
                    soapFault.addDetail().addDetailEntry(ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME).addTextNode(WSTLogger.i18NLogger.get_wst11_messaging_CompletionCoordinatorProcessorImpl_2(th));
                    throw new SOAPFaultException(soapFault);
                }

                return true;
            }
            else
            {
                if (WSTLogger.logger.isTraceEnabled())
                {
                    WSTLogger.logger.tracev("Commit called on unknown participant: {0}", new Object[] {instanceIdentifier}) ;
                }
                SOAPFactory factory = SOAPFactory.newInstance();
                SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME);
                soapFault.addDetail().addDetailEntry(ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME).addTextNode(WSTLogger.i18NLogger.get_wst11_messaging_CompletionCoordinatorProcessorImpl_5());
                throw new SOAPFaultException(soapFault);
            }
            } catch (SOAPException se) {
                se.printStackTrace(System.err);
                throw new ProtocolException(se);
            }
        }
    }

    /**
     * Rollback.
     * @param rollback The rollback notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public boolean rollback(final Notification rollback, final MAP map,
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
                    return true;
                }
                catch (final UnknownTransactionException ute)
                {
                    SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME);
                    soapFault.addDetail().addDetailEntry(ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME).addTextNode(WSTLogger.i18NLogger.get_wst11_messaging_CompletionCoordinatorProcessorImpl_6());
                    throw new SOAPFaultException(soapFault);
                }
                catch (final SystemException se)
                {
                    SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME);
                    soapFault.addDetail().addDetailEntry(ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME).addTextNode(WSTLogger.i18NLogger.get_wst11_messaging_CompletionCoordinatorProcessorImpl_7(se));
                    throw new SOAPFaultException(soapFault);
                }
                catch (final Throwable th)
                {
                    if (WSTLogger.logger.isTraceEnabled())
                    {
                        WSTLogger.logger.tracev("Unexpected exception thrown from commit:", th) ;
                    }
                    SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME);
                    soapFault.addDetail().addDetailEntry(ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME).addTextNode(WSTLogger.i18NLogger.get_wst11_messaging_CompletionCoordinatorProcessorImpl_7(th));
                    throw new SOAPFaultException(soapFault);
                }
            }
            else
            {
                if (WSTLogger.logger.isTraceEnabled())
                {
                    WSTLogger.logger.tracev("Rollback called on unknown participant: {0}", new Object[] {instanceIdentifier}) ;
                }
                SOAPFactory factory = SOAPFactory.newInstance();
                SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME);
                soapFault.addDetail().addDetailEntry(ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME).addTextNode(WSTLogger.i18NLogger.get_wst11_messaging_CompletionCoordinatorProcessorImpl_10());
                throw new SOAPFaultException(soapFault);
            }
        } catch (SOAPException se) {
            se.printStackTrace(System.err);
            throw new ProtocolException(se);
        }
    }
}
