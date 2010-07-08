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
package com.arjuna.wst.messaging.engines;

import java.util.TimerTask;

import javax.xml.namespace.QName;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.util.TransportTimer;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsat.NotificationType;
import com.arjuna.webservices.wsat.ParticipantInboundEvents;
import com.arjuna.webservices.wsat.State;
import com.arjuna.webservices.wsat.client.CoordinatorClient;
import com.arjuna.webservices.wsat.processors.ParticipantProcessor;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.wsc.messaging.MessageId;
import com.arjuna.wst.*;
import org.jboss.jbossts.xts10.recovery.participant.at.ATParticipantRecoveryRecord;
import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryManager;

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
    private final EndpointReferenceType coordinator ;
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
     * true id this is a recovered participant otherwise false.
     */
    private boolean recovered ;

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
    public ParticipantEngine(final Participant participant, final String id, final EndpointReferenceType coordinator)
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
    public ParticipantEngine(final Participant participant, final String id, final State state, final EndpointReferenceType coordinator, boolean recovered)
    {
        this.participant = participant ;
        this.id = id ;
        this.state = state ;
        this.coordinator = coordinator ;
        this.recovered = recovered;
        this.persisted = recovered;
        this.initialResendPeriod = TransportTimer.getTransportPeriod();
        this.maxResendPeriod = TransportTimer.getMaximumTransportPeriod();
        this.resendPeriod = initialResendPeriod;
    }
    
    /**
     * Handle the commit event.
     * @param commit The commit notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * None -> None (send committed)
     * Active -> Aborting (do nothing)
     * Preparing -> Aborting (do nothing)
     * PreparedSuccess -> Committing (initiate commit)
     * Committing -> Committing (do nothing)
     * Aborting -> Aborting (do nothing)
     */
    public void commit(final NotificationType commit, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
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
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * None -> None (send aborted)
     * Active -> Preparing (execute prepare)
     * Preparing -> Preparing (do nothing)
     * PreparedSuccess -> PreparedSuccess (resend prepared)
     * Committing -> Committing (ignore)
     * Aborting -> Aborting (send aborted and forget)
     */
    public void prepare(final NotificationType prepare, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_ACTIVE)
            {
                state = State.STATE_PREPARING ;
            } else if (current == State.STATE_PREPARED_SUCCESS) {
                // hmm, client may have missed a prepared message -- reset the period
                resendPeriod = TransportTimer.getTransportPeriod();
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
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * None -> None (send aborted)
     * Active -> Aborting (execute rollback, send aborted and forget)
     * Preparing -> Aborting (execute rollback, send aborted and forget)
     * PreparedSuccess -> Aborting (execute rollback, send aborted and forget)
     * Committing -> Committing (ignore)
     * Aborting -> Aborting (send aborted and forget)
     *
     */
    public void rollback(final NotificationType rollback, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
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
                    WSTLogger.i18NLogger.warn_messaging_engines_ParticipantEngine_rollback_1(id);

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
     * PreparedSuccess -> PreparedSuccess
     * Committing -> PreparedSuccess (resend Prepared)
     * Aborting -> Aborting
     */
    public void recovery()
    {
	synchronized(this)
	{
	    if (timerTask != null)
	    {
                timerTask.cancel() ;
	    }
	}
        sendReplay() ;
    }
    
    /**
     * Handle the soap fault event.
     * @param soapFault The soap fault.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        if (WSTLogger.logger.isTraceEnabled())
        {
            final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
            final SoapFaultType soapFaultType = soapFault.getSoapFaultType() ;
            final QName subCode = soapFault.getSubcode() ;
            WSTLogger.logger.tracev("Unexpected SOAP fault for participant {0}: {1} {2}", new Object[] {instanceIdentifier, soapFaultType, subCode}) ;
        }
        
        if (CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME.equals(soapFault.getSubcode()))
        {
            final SoapFaultType soapFaultType = soapFault.getSoapFaultType() ;
            final QName subCode = soapFault.getSubcode() ;
            WSTLogger.i18NLogger.error_messaging_engines_ParticipantEngine_soapFault_2(id, soapFaultType.toString(), subCode);

            // unrecoverable error -- forget this participant and delete any persistent
            //  record of it
            final State current ;

            synchronized(this)
            {
                current = state;
                state = null;
            }

            if (persisted && participant instanceof Durable2PCParticipant) {
                // remove any durable participant recovery record from the persistent store
                Durable2PCParticipant durableParticipant =(Durable2PCParticipant) participant;

                // if we cannot delete the participant we record an error here
                if (!XTSATRecoveryManager.getRecoveryManager().deleteParticipantRecoveryRecord(id)) {
                    // hmm, could not delete entry -- log an error
                    WSTLogger.i18NLogger.error_messaging_engines_ParticipantEngine_soapFault_3(id);
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
     *
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
                    WSTLogger.i18NLogger.warn_messaging_engines_ParticipantEngine_commitDecision_2(id);
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
                    WSTLogger.i18NLogger.warn_messaging_engines_ParticipantEngine_commitDecision_3(id);
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

            current = state ;
        }
        
        if (current == State.STATE_PREPARED_SUCCESS)
        {
            sendPrepared(true) ;
        }
    }
    
    /**
     * Execute the commit transition.
     * 
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
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception from participant commit", th) ;
            }
        }
    }
    
    /**
     * Execute the rollback transition.
     * 
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
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception from participant rollback", th) ;
            }
        }
        return true ;
    }
    
    /**
     * Execute the prepare transition.
     * 
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
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception from participant prepare", se) ;
            }
            return ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception from participant prepare", th) ;
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
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected result from participant prepare: {0}", new Object[] {(vote == null ? "null" : vote.getClass().getName())});
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
     */
    private void sendCommitted()
    {
        final AddressingContext responseAddressingContext = createContext() ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier(id) ;
        try
        {
            CoordinatorClient.getClient().sendCommitted(responseAddressingContext, instanceIdentifier) ;
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
     * Send the prepared message.
     */
    private void sendPrepared()
    {
        sendPrepared(false);
    }

    /**
     * Send the prepared message.
     *
     * @param timedOut true if this is in response to a comms timeout
     */
    private void sendPrepared(boolean timedOut)
    {
        final AddressingContext responseAddressingContext = createContext() ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier(id) ;
        try
        {
            CoordinatorClient.getClient().sendPrepared(responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception while sending Prepared", th) ;
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
     */
    private void sendAborted()
    {
        final AddressingContext responseAddressingContext = createContext() ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier(id) ;
        try
        {
            CoordinatorClient.getClient().sendAborted(responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception while sending Aborted", th) ;
            }
        }
    }
    
    /**
     * Send the read only message.
     * 
     */
    private void sendReadOnly()
    {
        final AddressingContext responseAddressingContext = createContext() ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier(id) ;
        try
        {
            CoordinatorClient.getClient().sendReadOnly(responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception while sending ReadOnly", th) ;
            }
        }
    }
    
    /**
     * Send the replay message.
     * 
     */
    private void sendReplay()
    {
        final AddressingContext responseAddressingContext = createContext() ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier(id) ;
        try
        {
            CoordinatorClient.getClient().sendReplay(responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception while sending Replay", th) ;
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
            TransportTimer.getTimer().schedule(timerTask, TransportTimer.getTransportPeriod()) ;
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
    private AddressingContext createContext()
    {
        final String messageId = MessageId.getMessageId() ;
        return AddressingContext.createRequestContext(coordinator, messageId) ;
    }
    
    /**
     * Get the coordinator id.
     * @return The coordinator id.
     */
    public String getId()
    {
        return id ;
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
