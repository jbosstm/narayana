package com.arjuna.wst11.messaging;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.base.processors.ActivatedObjectProcessor;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.wsf.common.addressing.MAP;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.CoordinatorInboundEvents;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.wsat.client.ParticipantClient;
import com.arjuna.webservices11.wsat.processors.CoordinatorProcessor;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.wsc11.messaging.MessageId;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;
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
     * Deactivate a coordinator recovered from the log.
     *
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
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     */
    public void aborted(final Notification aborted, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null) {
            try {
                coordinator.aborted(aborted, map, arjunaContext);
            }
            catch (final Throwable th) {
                WSTLogger.i18NLogger.warn_wst11_messaging_CoordinatorProcessorImpl_aborted_1(th);
            }
        } else {
            WSTLogger.i18NLogger.warn_wst11_messaging_CoordinatorProcessorImpl_aborted_2(instanceIdentifier.toString());
        }
    }

    /**
     * Committed.
     * @param committed The committed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     */
    public void committed(final Notification committed, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null) {
            try {
                coordinator.committed(committed, map, arjunaContext);
            }
            catch (final Throwable th) {
                WSTLogger.i18NLogger.warn_wst11_messaging_CoordinatorProcessorImpl_committed_1(th);
            }
        } else {
            WSTLogger.i18NLogger.warn_wst11_messaging_CoordinatorProcessorImpl_committed_2(instanceIdentifier.toString());
        }
    }

    /**
     * Prepared.
     * @param prepared The prepared notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     */
    public void prepared(final Notification prepared, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.prepared(prepared, map, arjunaContext) ;
            }
            catch (final Throwable th) {
                WSTLogger.i18NLogger.warn_wst11_messaging_CoordinatorProcessorImpl_prepared_1(th);
            }
        }
        else if (areRecoveryLogEntriesAccountedFor()) {
            WSTLogger.i18NLogger.warn_wst11_messaging_CoordinatorProcessorImpl_prepared_2(instanceIdentifier.toString());

            final String identifierValue = instanceIdentifier.getInstanceIdentifier();
            if ((identifierValue != null) && (identifierValue.length() > 0) && (identifierValue.charAt(0) == 'D')) {
                sendRollback(map, arjunaContext);
            } else {
                sendUnknownTransaction(map, arjunaContext);
            }
        }
        else {
            // there may be a participant stub waiting to be recovered from the log so drop the
            // message, forcing the caller to retry

            WSTLogger.i18NLogger.warn_wst11_messaging_CoordinatorProcessorImpl_prepared_3(instanceIdentifier.toString());
        }
    }

    /**
     * Read only.
     * @param readOnly The read only notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     */
    public void readOnly(final Notification readOnly, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null) {
            try {
                coordinator.readOnly(readOnly, map, arjunaContext);
            }
            catch (final Throwable th) {
                WSTLogger.i18NLogger.warn_wst11_messaging_CoordinatorProcessorImpl_readOnly_1(th);
            }
        } else {
            WSTLogger.i18NLogger.warn_wst11_messaging_CoordinatorProcessorImpl_readOnly_2(instanceIdentifier.toString());
        }
    }

    /**
     * SOAP Fault.
     * @param fault The SOAP fault notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     */
    public void soapFault(final SoapFault fault, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.soapFault(fault, map, arjunaContext) ;
            }
            catch (final Throwable th) {
                WSTLogger.i18NLogger.warn_wst11_messaging_CoordinatorProcessorImpl_soapFault_1(th);
            }
        }
        else {
            WSTLogger.i18NLogger.warn_wst11_messaging_CoordinatorProcessorImpl_soapFault_2(instanceIdentifier.toString());
        }
    }

    /**
     * Send an unknown transaction fault.
     *
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     */
    private void sendUnknownTransaction(final MAP map, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final MAP faultAddressingContext = AddressingHelper.createFaultContext(map, MessageId.getMessageId()) ;
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;

        try {
            final String message = WSTLogger.i18NLogger.get_wst11_messaging_CoordinatorProcessorImpl_sendUnknownTransaction_1();
            final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, AtomicTransactionConstants.WSAT_ERROR_CODE_UNKNOWN_TRANSACTION_QNAME, message) ;
            ParticipantClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
        } catch (final Throwable th) {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Unexpecting exception while sending InvalidState", th) ;
            }
        }
    }

    /**
     * Send a rollback message.
     *
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     */
    private void sendRollback(final MAP map, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final MAP responseAddressingContext = AddressingHelper.createOneWayResponseContext(map, messageId) ;
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        try
        {
            ParticipantClient.getClient().sendRollback(null, responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Unexpected exception while sending Rollback", th) ;
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
