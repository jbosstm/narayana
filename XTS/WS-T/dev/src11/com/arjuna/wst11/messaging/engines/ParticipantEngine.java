package com.arjuna.wst11.messaging.engines;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.util.TransportTimer;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.jbossts.xts.wsaddr.map.MAP;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.wsat.client.CoordinatorClient;
import com.arjuna.webservices11.wsat.ParticipantInboundEvents;
import com.arjuna.webservices11.wsat.State;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.wsat.processors.ParticipantProcessor;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.wst.*;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;
import org.jboss.jbossts.xts11.recovery.participant.at.ATParticipantRecoveryRecord;
import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryManager;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.util.TimerTask;

/**
 * The participant state engine
 * @author kevin
 */
public class ParticipantEngine implements ParticipantInboundEvents
{
    /**
     * The associated participant
     */
    private final Participant participant ;
    /**
     * The participant id.
     */
    private final String id ;
    /**
     * The coordinator endpoint reference.
     */
    private final W3CEndpointReference coordinator ;
    /**
     * The current state.
     */
    private State state ;
    /**
     * The associated timer task or null.
     */
    private TimerTask timerTask ;

    /**
     * the time which will elapse before the next message resend. this is incrementally increased
     * until it reaches RESEND_PERIOD_MAX
     */
    private long resendPeriod;

    /**
     * the initial period we will allow between resends.
     */
    private long initialResendPeriod;

    /**
     * the maximum period we will allow between resends. n.b. the coordinator uses the value returned
     * by getTransportTimeout as the limit for how long it waits for a response. however, we can still
     * employ a max resend period in excess of this value. if a message comes in after the coordinator
     * has given up it will catch it on the next retry.
     */
    private long maxResendPeriod;

    /**
     * true if this participant has been recovered otherwise false
     */
    private boolean recovered;

    /**
     * true if this participant's recovery details have been logged to disk otherwise false
     */
    private boolean persisted;

    /**
     * Construct the initial engine for the participant.
     * @param participant The participant.
     * @param id The participant id.
     * @param coordinator The coordinator endpoint reference.
     */
    public ParticipantEngine(final Participant participant, final String id, final W3CEndpointReference coordinator)
    {
        this(participant, id, State.STATE_ACTIVE, coordinator, false) ;
    }

    /**
     * Construct the engine for the participant in a specified state.
     * @param participant The participant.
     * @param id The participant id.
     * @param state The initial state.
     * @param coordinator The coordinator endpoint reference.
     */
    public ParticipantEngine(final Participant participant, final String id, final State state, final W3CEndpointReference coordinator, final boolean recovered)
    {
        this.participant = participant ;
        this.id = id ;
        this.state = state ;
        this.coordinator = coordinator ;
        this.recovered = recovered;
        this.persisted = recovered;
        this.initialResendPeriod = TransportTimer.getTransportPeriod();
        this.maxResendPeriod = TransportTimer.getMaximumTransportPeriod();
        this.resendPeriod = this.initialResendPeriod;
    }

    /**
     * Handle the commit event.
     * @param commit The commit notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * None -> None (send committed)
     * Active -> Aborting (do nothing)
     * Preparing -> Aborting (do nothing)
     * PreparedSuccess -> Committing (initiate commit)
     * Committing -> Committing (do nothing)
     * Aborting -> Aborting (do nothing)
     */
    public void commit(final Notification commit, final MAP map, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_PREPARED_SUCCESS)
            {
                state = State.STATE_COMMITTING ;
                if (timerTask != null)
                {
                    timerTask.cancel() ;
                }
            }
            else if ((current == State.STATE_ACTIVE) || (current == State.STATE_PREPARING))
            {
                state = State.STATE_ABORTING ;
            }
        }

        if (current == State.STATE_PREPARED_SUCCESS)
        {
            executeCommit() ;
        }
        else if (current == null)
        {
            sendCommitted() ;
        }
    }

    /**
     * Handle the prepare event.
     * @param prepare The prepare notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * None -> None (send aborted)
     * Active -> Preparing (execute prepare)
     * Preparing -> Preparing (do nothing)
     * PreparedSuccess -> PreparedSuccess (resend prepared)
     * Committing -> Committing (ignore)
     * Aborting -> Aborting (send aborted and forget)
     */
    public void prepare(final Notification prepare, final MAP map, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_ACTIVE)
            {
                state = State.STATE_PREPARING ;
            }
        }

        if (current == State.STATE_ACTIVE)
        {
            executePrepare() ;
        }
        else if (current == State.STATE_PREPARED_SUCCESS)
        {
            sendPrepared() ;
        }
        else if ((current == State.STATE_ABORTING) || (current == null))
        {
            sendAborted() ;
            forget() ;
        }
    }

    /**
     * Handle the rollback event.
     * @param rollback The rollback notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * None -> None (send aborted)
     * Active -> Aborting (execute rollback, send aborted and forget)
     * Preparing -> Aborting (execute rollback, send aborted and forget)
     * PreparedSuccess -> Aborting (execute rollback, send aborted and forget)
     * Committing -> Committing (ignore)
     * Aborting -> Aborting (send aborted and forget)
     *
     *  @message com.arjuna.wst11.messaging.engines.ParticipantEngine.rollback_1 [com.arjuna.wst11.messaging.engines.ParticipantEngine.rollback_1] could not delete recovery record for participant {0}
     */
    public void rollback(final Notification rollback, final MAP map, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;

            if ((current == State.STATE_ACTIVE) || (current == State.STATE_PREPARING) ||
                (current == State.STATE_PREPARED_SUCCESS))
            {
                state = State.STATE_ABORTING ;
            }
        }

        if (current != State.STATE_COMMITTING)
        {
            if ((current == State.STATE_ACTIVE) || (current == State.STATE_PREPARING) ||
                (current == State.STATE_PREPARED_SUCCESS))
            {
                // n.b. if state is PREPARING the participant may still be in the middle
                // of prepare or may even be told to prepare after this is called. according
                // to the spec that is not our lookout. however, rollback should only get
                // called once here.

                if (!executeRollback())
                {
                    return ;
                }
            }

            // if the participant managed to persist the log record then we should try
            // to delete it. note that persisted can only be set to true by the PREPARING
            // thread. if it detects a transtiion to ABORTING while it is doing the log write
            // it will clear up itself.

            if (persisted && participant instanceof Durable2PCParticipant) {
                // if we cannot delete the participant we effectively drop the rollback message
                // here in the hope that we have better luck next time..
                if (!XTSATRecoveryManager.getRecoveryManager().deleteParticipantRecoveryRecord(id)) {
                    // hmm, could not delete entry -- leave it so we can maybe retry later
                    if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                    {
                        WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.engines.ParticipantEngine.rollback_1", new Object[] {id}) ;
                    }

                    return;
                }
            }

            sendAborted() ;

            if (current != null)
            {
                forget() ;
            }
        }
    }

    /**
     * Handle the early rollback event.
     *
     * None -> None
     * Active -> Aborting (execute rollback, send aborted and forget)
     * Preparing -> Aborting (execute rollback, send aborted and forget)
     * PreparedSuccess -> PreparedSuccess
     * Committing -> Committing
     * Aborting -> Aborting
     */
    public void earlyRollback()
    {
        rollbackDecision() ;
    }

    /**
     * Handle the early readonly event.
     *
     * None -> None
     * Active -> None (send ReadOnly)
     * Preparing -> None (send ReadOnly)
     * PreparedSuccess -> PreparedSuccess
     * Committing -> Committing
     * Aborting -> Aborting
     */
    public void earlyReadonly()
    {
        readOnlyDecision() ;
    }

    /**
     * Handle the recovery event.
     *
     * None -> None
     * Active -> Active
     * Preparing -> Preparing
     * Committing -> Committing
     * PreparedSuccess -> PreparedSuccess (resend Prepared)
     * Aborting -> Aborting
     */
    public void recovery()
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
        }

        if (current == State.STATE_PREPARED_SUCCESS)
        {
            sendPrepared() ;
        }
    }

    /**
     * Handle the soap fault event.
     * @param soapFault The soap fault.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.soapFault_1 [com.arjuna.wst11.messaging.engines.ParticipantEngine.soapFault_1] - Unexpected SOAP fault for participant {0}: {1} {2}
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.soapFault_2 [com.arjuna.wst11.messaging.engines.ParticipantEngine.soapFault_2] - Unrecoverable error for participant {0} : {1} {2}
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.soapFault_3 [com.arjuna.wst11.messaging.engines.ParticipantEngine.soapFault_3] - Unable to delete recovery record at commit for participant {0}
     */
    public void soapFault(final SoapFault soapFault, final MAP map, final ArjunaContext arjunaContext)
    {
        if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
            final SoapFaultType soapFaultType = soapFault.getSoapFaultType() ;
            final QName subCode = soapFault.getSubcode() ;
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.soapFault_1", new Object[] {instanceIdentifier, soapFaultType, subCode}) ;
        }

        QName subcode = soapFault.getSubcode();

        if (CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME.equals(subcode) ||
                AtomicTransactionConstants.WSAT_ERROR_CODE_INCONSISTENT_INTERNAL_STATE_QNAME.equals(subcode) ||
                AtomicTransactionConstants.WSAT_ERROR_CODE_UNKNOWN_TRANSACTION_QNAME.equals(subcode))
        {
            if (WSTLogger.arjLoggerI18N.isErrorEnabled())
            {
                final SoapFaultType soapFaultType = soapFault.getSoapFaultType() ;
                final QName subCode = soapFault.getSubcode() ;
                WSTLogger.arjLoggerI18N.error("com.arjuna.wst11.messaging.engines.ParticipantEngine.soapFault_2", new Object[] {id, soapFaultType, subCode}) ;
            }

            // unrecoverable error -- forget this participant and delete any persistent
            //  record of it
            final State current ;

            synchronized(this)
            {
                current = state;
                state = null;
            }

            if (current == State.STATE_PREPARED_SUCCESS &&
                    AtomicTransactionConstants.WSAT_ERROR_CODE_UNKNOWN_TRANSACTION_QNAME.equals(subcode)) {
                // we need to tell this participant to roll back
                executeRollback();
            }

            if (persisted && participant instanceof Durable2PCParticipant) {
                // remove any durable participant recovery record from the persistent store
                Durable2PCParticipant durableParticipant =(Durable2PCParticipant) participant;

                // if we cannot delete the participant we record an error here
                if (!XTSATRecoveryManager.getRecoveryManager().deleteParticipantRecoveryRecord(id)) {
                    // hmm, could not delete entry -- log an error
                    if (WSTLogger.arjLoggerI18N.isErrorEnabled())
                    {
                        WSTLogger.arjLoggerI18N.error("com.arjuna.wst11.messaging.engines.ParticipantEngine.soapFault_3", new Object[] {id}) ;
                    }
                }
            }

            forget() ;
        }
    }

    /**
     * Handle the commit decision event.
     *
     * Preparing -> PreparedSuccess (send Prepared)
     * Committing -> Committing (send committed and forget)

     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.commitDecision_1 [com.arjuna.wst11.messaging.engines.ParticipantEngine.commitDecision_1] - Exception rolling back participant
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.commitDecision_2 [com.arjuna.wst11.messaging.engines.ParticipantEngine.commitDecision_2] - Unable to delete recovery record during prepare for participant {0}
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.commitDecision_3 [com.arjuna.wst11.messaging.engines.ParticipantEngine.commitDecision_3] - Unable to delete recovery record at commit for participant {0}
     */
    private void commitDecision()
    {
        State current ;
        boolean rollbackRequired  = false;
        boolean deleteRequired  = false;

        synchronized(this)
        {
            current = state ;

            if (current == State.STATE_PREPARING)
            {
                state = State.STATE_PREPARED_SUCCESS ;
            }
        }

        if (current == State.STATE_PREPARING)
        {
            // ok, we need to write the recovery details to log and send prepared.
            // if we cannot write the log then we have to rollback the participant
            //  and send aborted.
            if (participant instanceof Durable2PCParticipant) {
                // write a durable participant recovery record to the persistent store
                Durable2PCParticipant durableParticipant =(Durable2PCParticipant) participant;

                ATParticipantRecoveryRecord recoveryRecord = new ATParticipantRecoveryRecord(id, durableParticipant, coordinator);

                if (!XTSATRecoveryManager.getRecoveryManager().writeParticipantRecoveryRecord(recoveryRecord)) {
                    // we need to rollback and send aborted unless some other thread
                    //gets there first
                    rollbackRequired = true;
                }
            }
            // recheck state in case a rollback or readonly came in while we were writing the
            // log record

            synchronized (this) {
                current = state;

                if (current == State.STATE_PREPARED_SUCCESS) {
                    if (rollbackRequired) {
                        // if we change state to aborting then we are responsible for
                        // calling rollback and sending aborted but we have no log record
                        // to delete
                        state = State.STATE_ABORTING;
                    } else {
                        // this ensures any subsequent commit or rollback deletes the log record
                        // so we still have no log record to delete here
                        persisted = true;
                    }
                } else if (!rollbackRequired) {
                    // an incoming rollback or readonly changed the state to aborted or null so
                    // it will already have performed a rollback if required but we need to
                    // delete the log record since the rollback/readonly thread did not know
                    // about it
                    deleteRequired = true;
                }
            }

            if (rollbackRequired)
            {
                // we need to do the rollback and send aborted

                executeRollback();

                sendAborted();
                forget();
            } else if (deleteRequired) {
                // just try to delete the log entry -- any required aborted has already been sent

                if (!XTSATRecoveryManager.getRecoveryManager().deleteParticipantRecoveryRecord(id)) {
                    // hmm, could not delete entry log warning
                    if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                    {
                        WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.engines.ParticipantEngine.commitDecision_2", new Object[] {id}) ;
                    }
                }
            } else {
                // whew got through -- send a prepared
                sendPrepared() ;
            }
        }
        else if (current == State.STATE_COMMITTING)
        {
            if (persisted && participant instanceof Durable2PCParticipant) {
                // remove any durable participant recovery record from the persistent store
                Durable2PCParticipant durableParticipant =(Durable2PCParticipant) participant;

                // if we cannot delete the participant we effectively drop the commit message
                // here in the hope that we have better luck next time.
                if (!XTSATRecoveryManager.getRecoveryManager().deleteParticipantRecoveryRecord(id)) {
                    // hmm, could not delete entry -- log a warning
                    if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                    {
                        WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.engines.ParticipantEngine.commitDecision_3", new Object[] {id}) ;
                    }
                    // now revert back to PREPARED_SUCCESS and drop message awaiting a retry

                    synchronized (this) {
                        state = State.STATE_PREPARED_SUCCESS;
                    }

                    return;
                }
            }

            sendCommitted() ;
            forget() ;
        }
    }

    /**
     * Handle the readOnly decision event.
     *
     * Active -> None (send ReadOnly)
     * Preparing -> None (send ReadOnly)
     */
    private void readOnlyDecision()
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if ((current == State.STATE_ACTIVE) || (current == State.STATE_PREPARING))
            {
        	state = null ;
            }
        }

        if ((current == State.STATE_ACTIVE) || (current == State.STATE_PREPARING))
        {
            sendReadOnly() ;
            forget() ;
        }
    }

    /**
     * Handle the rollback decision event.
     *
     * Active -> Aborting (send aborted)
     * Preparing -> Aborting (send aborted)
     */
    private void rollbackDecision()
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if ((current == State.STATE_PREPARING) || (current == State.STATE_ACTIVE))
            {
                state = State.STATE_ABORTING ;
            }
        }

        if ((current == State.STATE_PREPARING) || (current == State.STATE_ACTIVE))
        {
            sendAborted() ;
            forget() ;
        }
    }

    /**
     * Handle the comms timeout event.
     *
     * PreparedSuccess -> PreparedSuccess (resend Prepared)
     */
    private void commsTimeout(TimerTask caller)
    {
        final State current ;
        synchronized(this)
        {
            if (timerTask != caller) {
                // the timer was cancelled but it went off before it could be cancelled

                return;
            }

            // double the resend period up to our maximum limit

            if (resendPeriod < maxResendPeriod) {
                resendPeriod = resendPeriod * 14 / 10; // approximately doubles every two resends

                if (resendPeriod > maxResendPeriod) {
                    resendPeriod = maxResendPeriod;
                }
            }
            current = state ;
        }

        if (current == State.STATE_PREPARED_SUCCESS)
        {
            sendPrepared() ;
        }
    }

    /**
     * Execute the commit transition.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.executeCommit_1 [com.arjuna.wst11.messaging.engines.ParticipantEngine.executeCommit_1] - Unexpected exception from participant commit
     */
    private void executeCommit()
    {
        try
        {
            participant.commit() ;
            commitDecision() ;
        }
        catch (final Throwable th)
        {
            synchronized(this)
            {
                if (state == State.STATE_COMMITTING)
                {
            	    state = State.STATE_PREPARED_SUCCESS ;
                }
            }
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.executeCommit_1", th) ;
            }
        }
    }

    /**
     * Execute the rollback transition.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.executeRollback_1 [com.arjuna.wst11.messaging.engines.ParticipantEngine.executeRollback_1] - Unexpected exception from participant rollback
     */
    private boolean executeRollback()
    {
        try
        {
            participant.rollback() ;
        }
        catch (final SystemException se)
        {
            return false ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.executeRollback_1", th) ;
            }
        }
        return true ;
    }

    /**
     * Execute the prepare transition.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.executePrepare_1 [com.arjuna.wst11.messaging.engines.ParticipantEngine.executePrepare_1] - Unexpected exception from participant prepare
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.executePrepare_2 [com.arjuna.wst11.messaging.engines.ParticipantEngine.executePrepare_2] - Unexpected result from participant prepare: {0}
     */
    private void executePrepare()
    {
        final Vote vote ;
        try
        {
            vote = participant.prepare();
        }
        catch (final SystemException se)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.executePrepare_1", se) ;
            }
            return ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.executePrepare_1", th) ;
            }
            rollbackDecision() ;
            return ;
        }

        if (vote instanceof Prepared)
        {
            commitDecision() ;
        }
        else if (vote instanceof ReadOnly)
        {
            readOnlyDecision() ;
        }
        else if (vote instanceof Aborted)
        {
            rollbackDecision() ;
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.executePrepare_2", new Object[] {(vote == null ? "null" : vote.getClass().getName())}) ;
            }
            rollbackDecision() ;
        }
    }

    /**
     * Forget the current participant.
     */
    private void forget()
    {
        synchronized(this)
        {
            state = null ;
        }
        ParticipantProcessor.getProcessor().deactivateParticipant(this) ;
    }

    /**
     * Send the committed message.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.sendCommitted_1 [com.arjuna.wst11.messaging.engines.ParticipantEngine.sendCommitted_1] - Unexpected exception while sending Committed
     */
    private void sendCommitted()
    {
        final MAP responseAddressingContext = createContext() ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier(id) ;
        try
        {
            CoordinatorClient.getClient().sendCommitted(coordinator, responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.sendCommitted_1", th) ;
            }
        }
    }

    /**
     * Send the prepared message.
     *
     */
    private void sendPrepared()
    {
        sendPrepared(false);
    }

    /**
     * Send the prepared message.
     *
     * @param timedOut true if this is in response to a comms timeout
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.sendPrepared_1 [com.arjuna.wst11.messaging.engines.ParticipantEngine.sendPrepared_1] - Unexpected exception while sending Prepared
     */
    private void sendPrepared(boolean timedOut)
    {
        final MAP responseAddressingContext = createContext() ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier(id) ;
        try
        {
            CoordinatorClient.getClient().sendPrepared(coordinator, responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.sendPrepared_1", th) ;
            }
        }

        updateResendPeriod(timedOut);

        initiateTimer() ;
    }

    private synchronized void updateResendPeriod(boolean timedOut)
    {
        // if we timed out then we multiply the resend period by ~= sqrt(2) up to the maximum
        // if not we make sure it is reset to the initial period

        if (timedOut) {
            if (resendPeriod < maxResendPeriod) {
                long newPeriod  = resendPeriod * 14 / 10;  // approximately doubles every two resends

                if (newPeriod > maxResendPeriod) {
                    newPeriod = maxResendPeriod;
                }
                resendPeriod = newPeriod;
            }
        } else {
            if (resendPeriod > initialResendPeriod) {
                resendPeriod = initialResendPeriod;
            }
        }
    }

    /**
     * Send the aborted message.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.sendAborted_1 [com.arjuna.wst11.messaging.engines.ParticipantEngine.sendAborted_1] - Unexpected exception while sending Aborted
     */
    private void sendAborted()
    {
        final MAP responseAddressingContext = createContext() ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier(id) ;
        try
        {
            CoordinatorClient.getClient().sendAborted(coordinator, responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.sendAborted_1", th) ;
            }
        }
    }

    /**
     * Send the read only message.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.sendReadOnly_1 [com.arjuna.wst11.messaging.engines.ParticipantEngine.sendReadOnly_1] - Unexpected exception while sending ReadOnly
     */
    private void sendReadOnly()
    {
        final MAP responseAddressingContext = createContext() ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier(id) ;
        try
        {
            CoordinatorClient.getClient().sendReadOnly(coordinator, responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.sendReadOnly_1", th) ;
            }
        }
    }

    /**
     * Initiate the timer.
     */
    private synchronized void initiateTimer()
    {
        if (timerTask != null)
        {
            timerTask.cancel() ;
        }

        if (state == State.STATE_PREPARED_SUCCESS)
        {
            timerTask = new TimerTask() {
                public void run() {
                    commsTimeout(this) ;
                }
            } ;
            TransportTimer.getTimer().schedule(timerTask, resendPeriod) ;
        }
        else
        {
            timerTask = null ;
        }
    }

    /**
     * Create a response context from the incoming context.
     * @return The addressing context.
     */
    private MAP createContext()
    {
        final String messageId = MessageId.getMessageId() ;
        return AddressingHelper.createNotificationContext(messageId) ;
    }
    
    /**
     * Get the coordinator id.
     * @return The coordinator id.
     */
    public String getId()
    {
        return id ;
    }

    public W3CEndpointReference getCoordinator()
    {
        return coordinator;
    }

    /**
     * Is the participant persisted to disk?
     * @return true if persisted, false otherwise.
     */
    public boolean isPersisted()
    {
        return persisted ;
    }

    /**
     * Is the participant recovered?
     * @return true if recovered, false otherwise.
     */
    public boolean isRecovered()
    {
        return recovered ;
    }
}
