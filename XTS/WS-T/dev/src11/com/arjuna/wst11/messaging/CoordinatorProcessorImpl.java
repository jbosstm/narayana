package com.arjuna.wst11.messaging;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.base.processors.ActivatedObjectProcessor;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.CoordinatorInboundEvents;
import com.arjuna.webservices11.wsat.processors.CoordinatorProcessor;
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
     * Deactivate the coordinator.
     * @param coordinator The coordinator.
     */
    public void deactivateCoordinator(final CoordinatorInboundEvents coordinator)
    {
        activatedObjectProcessor.deactivateObject(coordinator) ;
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
            WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.committed_2", new Object[] {instanceIdentifier}) ;
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
        else
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
        final String responseMessageId = MessageId.getMessageId() ;
        final AddressingProperties responseAddressingContext = AddressingHelper.createNotificationContext(responseMessageId) ;
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;

        /*
         * TODO - fix this. cannot send invalid state fault as we have no participant end point!

        final AttributedURI requestMessageId = addressingProperties.getMessageID() ;
        if (requestMessageId != null)
        {
            AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
            Relationship relatesToValue = builder.newRelationship(requestMessageId.getURI());
            Relationship[] relatesToSet = { relatesToValue };
            responseAddressingContext.setRelatesTo(relatesToSet);
        }

        try {
            final String message = WSTLogger.log_mesg.getString("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendInvalidState_1") ;
            final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME, message) ;
            ParticipantClient.getClient().sendSoapFault(responseAddressingContext, soapFault, instanceIdentifier) ;
        } catch (final Throwable th) {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendInvalidState_2", th) ;
            }
        }
        */
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
        final AddressingProperties responseAddressingContext = AddressingHelper.createNotificationContext(messageId) ;
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        /*
         * TODO - fix this. cannot send rollback as we have no participant end point!

        try
        {
            ParticipantClient.getClient().sendRollback(responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.CoordinatorProcessorImpl.sendRollback_1", th) ;
            }
        }
        */
    }
}
