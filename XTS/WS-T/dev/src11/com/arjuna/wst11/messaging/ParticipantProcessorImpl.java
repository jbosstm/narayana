package com.arjuna.wst11.messaging;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.base.processors.ActivatedObjectProcessor;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.processors.ParticipantProcessor;
import com.arjuna.webservices11.wsat.ParticipantInboundEvents;
import com.arjuna.webservices11.wsat.client.CoordinatorClient;
import com.arjuna.wsc11.messaging.MessageId;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;
import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryManager;

/**
 * The Participant processor.
 * @author kevin
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
    public void activateParticipant(final ParticipantInboundEvents participant, final String identifier)
    {
        activatedObjectProcessor.activateObject(participant, identifier) ;
    }

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public void deactivateParticipant(final ParticipantInboundEvents participant)
    {
        activatedObjectProcessor.deactivateObject(participant) ;
    }

    /**
     * Check whether a participant with the given id is currently active
     * @param identifier The identifier.
     */
    public boolean isActive(final String identifier)
    {
        // if there is an entry in the table then it is active or completed and pending delete

        return (activatedObjectProcessor.getObject(identifier) != null);
    }

    /**
     * Get the participant with the specified identifier.
     * @param instanceIdentifier The participant identifier.
     * @return The participant or null if not known.
     */
    private ParticipantInboundEvents getParticipant(final InstanceIdentifier instanceIdentifier)
    {
        final String identifier = (instanceIdentifier != null ? instanceIdentifier.getInstanceIdentifier() : null) ;
        return (ParticipantInboundEvents)activatedObjectProcessor.getObject(identifier) ;
    }

    /**
     * Commit.
     * @param commit The commit notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     */
    public void commit(final Notification commit, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;

        /**
         * ensure the AT participant recovery manager is running
         */
        XTSATRecoveryManager recoveryManager = XTSATRecoveryManager.getRecoveryManager();

        if (recoveryManager == null) {
            // log warning and drop this message -- it will be resent
            WSTLogger.i18NLogger.warn_wst11_messaging_ParticipantProcessorImpl_commit_3(instanceIdentifier.toString());

            return;
        }

        final ParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.commit(commit, map, arjunaContext) ;
            }
            catch (final Throwable th) {
                WSTLogger.i18NLogger.warn_wst11_messaging_ParticipantProcessorImpl_commit_1(th);
            }
        }
        else if (!recoveryManager.isParticipantRecoveryStarted()) {
            WSTLogger.i18NLogger.warn_wst11_messaging_ParticipantProcessorImpl_commit_4(instanceIdentifier.toString());
        }
        else if (recoveryManager.findParticipantRecoveryRecord(instanceIdentifier.getInstanceIdentifier()) != null) {
            WSTLogger.i18NLogger.warn_wst11_messaging_ParticipantProcessorImpl_commit_5(instanceIdentifier.toString());
        }
        else {
            WSTLogger.i18NLogger.warn_wst11_messaging_ParticipantProcessorImpl_commit_2(instanceIdentifier.toString());
            sendCommitted(map, arjunaContext);
        }
    }

    /**
     * Prepare.
     * @param prepare The prepare notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     */
    public void prepare(final Notification prepare, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        final ParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.prepare(prepare, map, arjunaContext) ;
            }
            catch (final Throwable th) {
                WSTLogger.i18NLogger.warn_wst11_messaging_ParticipantProcessorImpl_prepare_1(th);
            }
        }
        else {
            WSTLogger.i18NLogger.warn_wst11_messaging_ParticipantProcessorImpl_prepare_2(instanceIdentifier.toString());
            sendAborted(map, arjunaContext);
        }
    }

    /**
     * Rollback.
     * @param rollback The rollback notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     */
    public void rollback(final Notification rollback, final MAP map,
        final ArjunaContext arjunaContext)
    {
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;

        /**
         * ensure the AT participant recovery manager is running
         */
        XTSATRecoveryManager recoveryManager = XTSATRecoveryManager.getRecoveryManager();

        if (recoveryManager == null) {
            // log warning and drop this message -- it will be resent
            WSTLogger.i18NLogger.warn_wst11_messaging_ParticipantProcessorImpl_rollback_3(instanceIdentifier.toString());

        }

        final ParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.rollback(rollback, map, arjunaContext) ;
            }
            catch (final Throwable th) {
                WSTLogger.i18NLogger.warn_wst11_messaging_ParticipantProcessorImpl_rollback_1(th);
            }
        }
        else if (!recoveryManager.isParticipantRecoveryStarted()) {
            WSTLogger.i18NLogger.warn_wst11_messaging_ParticipantProcessorImpl_rollback_4(instanceIdentifier.toString());
        }
        else if (recoveryManager.findParticipantRecoveryRecord(instanceIdentifier.getInstanceIdentifier()) != null) {
            WSTLogger.i18NLogger.warn_wst11_messaging_ParticipantProcessorImpl_rollback_5(instanceIdentifier.toString());
        }
        else {
            WSTLogger.i18NLogger.warn_wst11_messaging_ParticipantProcessorImpl_rollback_2(instanceIdentifier.toString());
            sendAborted(map, arjunaContext);
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
        final ParticipantInboundEvents participant = getParticipant(instanceIdentifier) ;

        if (participant != null)
        {
            try
            {
                participant.soapFault(fault, map, arjunaContext) ;
            }
            catch (final Throwable th) {
                WSTLogger.i18NLogger.warn_wst11_messaging_ParticipantProcessorImpl_soapFault_1(th);
            }
        }
        else {
            WSTLogger.i18NLogger.warn_wst11_messaging_ParticipantProcessorImpl_soapFault_2(instanceIdentifier.toString());
        }
    }

    /**
     * Send a committed message.
     *
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     */
    private void sendCommitted(final MAP map, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final MAP responseMAP = AddressingHelper.createResponseContext(map, messageId) ;
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        try
        {
            CoordinatorClient.getClient().sendCommitted(null, responseMAP, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception while sending Committed", th) ;
            }
        }
    }

    /**
     * Send an aborted message.
     *
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     */
    private void sendAborted(final MAP map, final ArjunaContext arjunaContext)
    {
        // KEV add check for recovery
        final String messageId = MessageId.getMessageId() ;
        final MAP responseMAP = AddressingHelper.createResponseContext(map, messageId) ;
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        try
        {
            CoordinatorClient.getClient().sendAborted(null, responseMAP, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception while sending Aborted", th) ;
            }
        }
    }
}
