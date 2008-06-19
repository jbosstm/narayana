package com.arjuna.wst11.messaging;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.base.processors.ActivatedObjectProcessor;
import com.arjuna.webservices.base.processors.ReactivatedObjectProcessor;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.CoordinatorInboundEvents;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.wsat.client.ParticipantClient;
import com.arjuna.webservices11.wsat.processors.CoordinatorProcessor;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.wsc11.messaging.MessageId;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

import javax.xml.ws.addressing.AddressingProperties;

/**
 * The Coordinator processor.
 * @author kevin
 */
public class CoordinatorProcessorImpl extends CoordinatorProcessor
{
    /**
     * The activated object processor.
     */
    private final ReactivatedObjectProcessor activatedObjectProcessor = new ReactivatedObjectProcessor() ;

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
     * @param leaveGhost true if a ghost activation entry should be left to indicate that the
     * coordinator exists in a log entry and will be recovered at some later date
     */
    public void deactivateCoordinator(CoordinatorInboundEvents coordinator, boolean leaveGhost) {
        activatedObjectProcessor.deactivateObject(coordinator,  leaveGhost);
    }

    /**
     * Get the coordinator with the specified identifier.
     * @param instanceIdentifier The coordinator identifier.
     * @return The coordinator or null if not known.
     */
    private CoordinatorInboundEvents getCoordinator(final InstanceIdentifier instanceIdentifier)
    {
        final String identifier = (instanceIdentifier != null ? instanceIdentifier.getInstanceIdentifier() : null) ;
        return (CoordinatorInboundEvents)activatedObjectProcessor.getObject(identifier) ;
    }

    /**
     * Tests if there is a ghost entry with the specified identifier.
     * @param instanceIdentifier The coordinator identifier.
     * @return true if there is a ghost entry.
     */
    private boolean getGhostCoordinator(final InstanceIdentifier instanceIdentifier)
    {
        final String identifier = (instanceIdentifier != null ? instanceIdentifier.getInstanceIdentifier() : null) ;
        return activatedObjectProcessor.getGhost(identifier) ;
    }

    /**
     * Aborted.
     * @param aborted The aborted notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.aborted_1 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.aborted_1] - Unexpected exception thrown from aborted:
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.aborted_2 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.aborted_2] - Aborted called on unknown coordinator: {0}
     */
    public void aborted(final Notification aborted, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.aborted(aborted, addressingProperties, arjunaContext) ;
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
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.committed_1 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.committed_1] - Unexpected exception thrown from committed:
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.committed_2 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.committed_2] - Committed called on unknown coordinator: {0}
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.committed_3 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.committed_3] - Ignoring committed called on unidentified coordinator until recovery pass is complete: {0}
     */
    public void committed(final Notification committed, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.committed(committed, addressingProperties, arjunaContext) ;
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
            if (!getGhostCoordinator(instanceIdentifier)) {
                WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.committed_2", new Object[] {instanceIdentifier}) ;
            } else {
                WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.committed_3", new Object[] {instanceIdentifier}) ;
            }
        }
    }

    /**
     * Prepared.
     * @param prepared The prepared notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.prepared_1 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.prepared_1] - Unexpected exception thrown from prepared:
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.prepared_2 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.prepared_2] - Prepared called on unknown coordinator: {0}
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.prepared_3 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.prepared_3] - Ignoring prepared called on unidentified coordinator until recovery pass is complete: {0}
     */
    public void prepared(final Notification prepared, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.prepared(prepared, addressingProperties, arjunaContext) ;
            }
            catch (final Throwable th)
            {
                if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                {
                    WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.prepared_1", th) ;
                }
            }
        }
        else if (!getGhostCoordinator(instanceIdentifier))
        {
            if (WSTLogger.arjLoggerI18N.isWarnEnabled())
            {
                WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.prepared_2", new Object[] {instanceIdentifier}) ;
            }

            final String identifierValue = instanceIdentifier.getInstanceIdentifier() ;
            if ((identifierValue != null) && (identifierValue.length() > 0) && (identifierValue.charAt(0) == 'D'))
            {
        	    sendRollback(addressingProperties, arjunaContext) ;
            }
            else
            {
                sendInvalidState(addressingProperties, arjunaContext) ;
            }
        }
        else
        {
            // there may be a participant stub waitinng to be recovered from the log so drop the
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
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.readOnly_1 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.readOnly_1] - Unexpected exception thrown from readOnly:
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.readOnly_2 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.readOnly_2] - ReadOnly called on unknown coordinator: {0}
     */
    public void readOnly(final Notification readOnly, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.readOnly(readOnly, addressingProperties, arjunaContext) ;
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
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.soapFault_1 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.soapFault_1] - Unexpected exception thrown from soapFault:
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.soapFault_2 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.soapFault_2] - SoapFault called on unknown coordinator: {0}
     */
    public void soapFault(final SoapFault fault, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final CoordinatorInboundEvents coordinator = getCoordinator(instanceIdentifier) ;

        if (coordinator != null)
        {
            try
            {
                coordinator.soapFault(fault, addressingProperties, arjunaContext) ;
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
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendInvalidState_1 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendInvalidState_1] - Unknown Transaction.
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendInvalidState_2 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendInvalidState_2] - Unexpecting exception while sending InvalidState
     */
    private void sendInvalidState(final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final AddressingProperties faultAddressingContext = AddressingHelper.createFaultContext(addressingProperties, MessageId.getMessageId()) ;
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;

        try {
            final String message = WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendInvalidState_1") ;
            final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME, message) ;
            ParticipantClient.getClient().sendSoapFault(faultAddressingContext, soapFault, instanceIdentifier) ;
        } catch (final Throwable th) {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendInvalidState_2", th) ;
            }
        }
    }

    /**
     * Send a rollback message.
     *
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendRollback_1 [com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendRollback_1] - Unexpected exception while sending Rollback
     */
    private void sendRollback(final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final AddressingProperties responseAddressingContext = AddressingHelper.createOneWayResponseContext(addressingProperties, messageId) ;
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
}
