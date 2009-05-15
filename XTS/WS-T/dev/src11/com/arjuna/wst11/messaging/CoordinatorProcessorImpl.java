package com.arjuna.wst11.messaging;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.base.processors.ActivatedObjectProcessor;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.jbossts.xts.wsaddr.map.MAP;
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
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.aborted_1 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.aborted_1] - Unexpected exception thrown from aborted:
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.aborted_2 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.aborted_2] - Aborted called on unknown coordinator: {0}
     */
    public void aborted(final Notification aborted, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.aborted(aborted, map, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                {
                    WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.aborted_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isWarnEnabled())
        {
            WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.aborted_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Committed.
     * @param committed The committed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.committed_1 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.committed_1] - Unexpected exception thrown from committed:
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.committed_2 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.committed_2] - Committed called on unknown coordinator: {0}
     */
    public void committed(final Notification committed, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.committed(committed, map, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                {
                    WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.committed_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isWarnEnabled())
        {
            WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.committed_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * Prepared.
     * @param prepared The prepared notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.prepared_1 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.prepared_1] - Unexpected exception thrown from prepared:
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.prepared_2 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.prepared_2] - Prepared called on unknown coordinator: {0}
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.prepared_3 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.prepared_3] - Ignoring prepared called on unidentified coordinator until recovery pass is complete: {0}
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
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                {
                    WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.prepared_1", th) ;
                }
            }
        }
        else if (areRecoveryLogEntriesAccountedFor())
        {
            if (WSTLogger.arjLoggerI18N.isWarnEnabled())
            {
                WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.prepared_2", new Object[] {instanceIdentifier}) ;
            }

            final String identifierValue = instanceIdentifier.getInstanceIdentifier() ;
            if ((identifierValue != null) && (identifierValue.length() > 0) && (identifierValue.charAt(0) == 'D'))
            {
        	    sendRollback(map, arjunaContext) ;
            }
            else
            {
                sendUnknownTransaction(map, arjunaContext) ;
            }
        }
        else
        {
            // there may be a participant stub waiting to be recovered from the log so drop the
            // message, forcing the caller to retry

            if (WSTLogger.arjLoggerI18N.isWarnEnabled())
            {
                WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.prepared_3", new Object[] {instanceIdentifier}) ;
            }
        }
    }

    /**
     * Read only.
     * @param readOnly The read only notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.readOnly_1 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.readOnly_1] - Unexpected exception thrown from readOnly:
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.readOnly_2 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.readOnly_2] - ReadOnly called on unknown coordinator: {0}
     */
    public void readOnly(final Notification readOnly, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.readOnly(readOnly, map, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                {
                    WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.readOnly_1", th) ;
                }
            }
        }
        else if (WSTLogger.arjLoggerI18N.isWarnEnabled())
        {
            WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.readOnly_2", new Object[] {instanceIdentifier}) ;
        }
    }

    /**
     * SOAP Fault.
     * @param fault The SOAP fault notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.soapFault_1 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.soapFault_1] - Unexpected exception thrown from soapFault:
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.soapFault_2 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.soapFault_2] - SoapFault called on unknown coordinator: {0}
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
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                {
                    WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.soapFault_1", th) ;
                }
            }
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isWarnEnabled())
            {
                WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.soapFault_2", new Object[] {instanceIdentifier}) ;
            }
        }
    }

    /**
     * Send an unknown transaction fault.
     *
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendUnknownTransaction_1 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendUnknownTransaction_1] - Unknown Transaction.
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendUnknownTransaction_2 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendUnknownTransaction_2] - Unexpecting exception while sending InvalidState
     */
    private void sendUnknownTransaction(final MAP map, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final MAP faultAddressingContext = AddressingHelper.createFaultContext(map, MessageId.getMessageId()) ;
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;

        try {
            final String message = WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendUnknownTransaction_1") ;
            final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, AtomicTransactionConstants.WSAT_ERROR_CODE_UNKNOWN_TRANSACTION_QNAME, message) ;
            ParticipantClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
        } catch (final Throwable th) {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendUnknownTransaction_2", th) ;
            }
        }
    }

    /**
     * Send a rollback message.
     *
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendRollback_1 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendRollback_1] - Unexpected exception while sending Rollback
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
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendRollback_1", th) ;
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
