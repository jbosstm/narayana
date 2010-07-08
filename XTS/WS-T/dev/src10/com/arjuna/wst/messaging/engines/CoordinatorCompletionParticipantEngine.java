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


import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.util.TransportTimer;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsba.CoordinatorCompletionParticipantInboundEvents;
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.State;
import com.arjuna.webservices.wsba.StatusType;
import com.arjuna.webservices.wsba.client.CoordinatorCompletionCoordinatorClient;
import com.arjuna.webservices.wsba.processors.CoordinatorCompletionParticipantProcessor;
import com.arjuna.wsc.messaging.MessageId;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.FaultedException;
import org.jboss.jbossts.xts.recovery.participant.ba.XTSBARecoveryManager;
import org.jboss.jbossts.xts10.recovery.participant.ba.BAParticipantRecoveryRecord;

/**
 * The coordinator completion participant state engine
 * @author kevin
 */
public class CoordinatorCompletionParticipantEngine implements CoordinatorCompletionParticipantInboundEvents
{
    /**
     * The participant id.
     */
    private final String id ;
    /**
     * The instance identifier.
     */
    private final InstanceIdentifier instanceIdentifier ;
    /**
     * The coordinator endpoint reference.
     */
    private final EndpointReferenceType coordinator ;
    /**
     * The associated participant
     */
    private final BusinessAgreementWithCoordinatorCompletionParticipant participant ;
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
     * the amount of time we will wait for a response to a dispatched message
     */
    private long timeout;

    /**
     * true id this is a recovered participant otherwise false.
     */
    private boolean recovered ;

    /**
     * true if this participant's recovery details have been logged to disk otherwise false
     */
    private boolean persisted;

    /**
     * true if the participant should send getstatus rather than resend a completed message
     */
    private boolean checkStatus;

    /**
     * Construct the initial engine for the participant.
     * @param id The participant id.
     * @param coordinator The coordinator endpoint reference.
     * @param participant The participant.
     */
    public CoordinatorCompletionParticipantEngine(final String id, final EndpointReferenceType coordinator,
        final BusinessAgreementWithCoordinatorCompletionParticipant participant)
    {
        this(id, coordinator, participant, State.STATE_ACTIVE, false) ;
    }
    
    /**
     * Construct the engine for the participant in a specified state.
     * @param id The participant id.
     * @param coordinator The coordinator endpoint reference.
     * @param participant The participant.
     * @param state The initial state.
     */
    public CoordinatorCompletionParticipantEngine(final String id, final EndpointReferenceType coordinator,
        final BusinessAgreementWithCoordinatorCompletionParticipant participant, final State state, final boolean recovered)
    {
        this.id = id ;
        this.instanceIdentifier = new InstanceIdentifier(id) ;
        this.coordinator = coordinator ;
        this.participant = participant ;
        this.state = state ;
        this.recovered = recovered;
        this.persisted = recovered;
        this.initialResendPeriod = TransportTimer.getTransportPeriod();
        this.maxResendPeriod = TransportTimer.getMaximumTransportPeriod();
        this.timeout = TransportTimer.getTransportTimeout();
        this.resendPeriod = this.initialResendPeriod;
        // we always check the status of a recovered participant and we always start off sending completed
        // if the participant is not recovered
        this.checkStatus = recovered;
    }
    
    /**
     * Handle the cancel event.
     * @param cancel The cancel notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * Active -> Canceling
     * Canceling -> Canceling
     * Completing -> Canceling
     * Completed -> Completed (resend Completed)
     * Closing -> Closing
     * Compensating -> Compensating
     * Faulting -> Faulting
     * Faulting-Active -> Faulting (resend Fault)
     * Faulting-Compensating -> Faulting
     * Exiting -> Exiting (resend Exit)
     * Ended -> Ended (resend Cancelled)
     */
    public void cancel(final NotificationType cancel, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if ((current == State.STATE_ACTIVE) || (current == State.STATE_COMPLETING))
            {
                changeState(State.STATE_CANCELING) ;
            }
            else if ((current == State.STATE_FAULTING_ACTIVE) || (current == State.STATE_FAULTING_COMPENSATING))
            {
                changeState(State.STATE_FAULTING) ;
            }
        }
        
        if (current == State.STATE_ACTIVE)
        {
            executeCancel(false) ;
        }
        else if (current == State.STATE_COMPLETING)
        {
            executeCancel(true) ;
        }
        else if (current == State.STATE_COMPLETED)
        {
            sendCompleted() ;
        }
        else if (current == State.STATE_FAULTING_ACTIVE)
        {
            sendFault("Cancel called when State faulting active") ;
        }
        else if (current == State.STATE_EXITING)
        {
            sendExit() ;
        }
        else if (current == State.STATE_ENDED)
        {
            sendCancelled() ;
        }
    }
    
    /**
     * Handle the close event.
     * @param close The close notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completing -> Completing (invalid state)
     * Completed -> Closing
     * Closing -> Closing
     * Compensating -> Compensating (invalid state)
     * Faulting -> Faulting (invalid state)
     * Faulting-Active -> Faulting (invalid state)
     * Faulting-Compensating -> Faulting (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended (send Closed)
     */
    public void close(final NotificationType close, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_COMPLETED)
            {
                changeState(State.STATE_CLOSING) ;
            }
            else if ((current == State.STATE_FAULTING_ACTIVE) || (current == State.STATE_FAULTING_COMPENSATING))
            {
                changeState(State.STATE_FAULTING) ;
            }
        }
        
        if (current == State.STATE_COMPLETED)
        {
            if (timerTask != null)
            {
                timerTask.cancel() ;
            }
            executeClose() ;
        }
        else if (current == State.STATE_ENDED)
        {
            sendClosed() ;
        }
    }
    
    /**
     * Handle the compensate event.
     * @param compensate The compensate notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completing -> Completing (invalid state)
     * Completed -> Compensating
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating
     * Faulting -> Faulting (invalid state)
     * Faulting-Active -> Faulting (invalid state)
     * Faulting-Compensating -> Faulting (resend fault)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended (send compensated)
     */
    public void compensate(final NotificationType compensate, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_COMPLETED)
            {
                changeState(State.STATE_COMPENSATING) ;
            }
            else if ((current == State.STATE_FAULTING_ACTIVE) || (current == State.STATE_FAULTING_COMPENSATING))
            {
                changeState(State.STATE_FAULTING) ;
            }
        }
        
        if (current == State.STATE_COMPLETED)
        {
            if (timerTask != null)
            {
                timerTask.cancel() ;
            }
            executeCompensate() ;
        }
        else if (current == State.STATE_FAULTING_COMPENSATING)
        {
            sendFault("Compensate called when state faulting compensating") ;
        }
        else if (current == State.STATE_ENDED)
        {
            sendCompensated() ;
        }
    }
    
    /**
     * Handle the complete event.
     * @param complete The complete notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * Active -> Completing
     * Canceling -> Canceling
     * Completing -> Completing
     * Completed -> Completed (resend Completed)
     * Closing -> Closing
     * Compensating -> Compensating
     * Faulting -> Faulting
     * Faulting-Active -> Faulting (resend Fault)
     * Faulting-Compensating -> Faulting
     * Exiting -> Exiting (resend Exit)
     * Ended -> Ended
     */
    public void complete(final NotificationType complete, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_ACTIVE)
            {
                changeState(State.STATE_COMPLETING) ;
            }
            else if ((current == State.STATE_FAULTING_ACTIVE) || (current == State.STATE_FAULTING_COMPENSATING))
            {
                changeState(State.STATE_FAULTING) ;
            }
        }
        
        if (current == State.STATE_ACTIVE)
        {
            executeComplete() ;
        }
        else if (current == State.STATE_COMPLETED)
        {
            sendCompleted() ;
        }
        else if (current == State.STATE_FAULTING_ACTIVE)
        {
            sendFault("Complete called when state faulting active") ;
        }
        else if (current == State.STATE_EXITING)
        {
            sendExit() ;
        }
    }
    
    /**
     * Handle the exited event.
     * @param exited The exited notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completing -> Completing (invalid state)
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Faulting -> Faulting (invalid state)
     * Faulting-Active -> Faulting (invalid state)
     * Faulting-Compensating -> Faulting (invalid state)
     * Exiting -> Ended
     * Ended -> Ended
     */
    public void exited(final NotificationType exited, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if ((current == State.STATE_FAULTING_ACTIVE) || (current == State.STATE_FAULTING_COMPENSATING))
            {
                changeState(State.STATE_FAULTING) ;
            }
            else if (current == State.STATE_EXITING)
            {
                ended() ;
            }
        }
    }
    
    /**
     * Handle the faulted event.
     * @param faulted The faulted notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completing -> Completing (invalid state)
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Faulting -> Ended
     * Faulting-Active -> Ended
     * Faulting-Compensating -> Ended
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended
     */
    public void faulted(final NotificationType faulted, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current ;
        boolean deleteRequired = false;
        synchronized(this)
        {
            current = state ;
            if ((current == State.STATE_FAULTING) || (current == State.STATE_FAULTING_ACTIVE) ||
                (current == State.STATE_FAULTING_COMPENSATING))
            {
                deleteRequired = persisted;
                ended() ;
            }
        }
        // if we just ended the participant ensure any log record gets deleted

        if (deleteRequired) {
            if (!XTSBARecoveryManager.getRecoveryManager().deleteParticipantRecoveryRecord(id)) {
                // hmm, could not delete entry -- nothing more we can do than log a message
                WSTLogger.i18NLogger.warn_messaging_engines_CoordinatorCompletionParticipantEngine_faulted_1(id);
            }
        }
    }
    
    /**
     * Handle the getStatus event.
     * @param getStatus The getStatus notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     */
    public void getStatus(final NotificationType getStatus, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
	final State current ;
	synchronized(this)
	{
	    current = state ;
	}
	sendStatus(current) ;
    }
    
    /**
     * Handle the status event.
     * @param status The status type.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void status(final StatusType status, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        // TODO --  check that the status is actually what we expect

        // revert to sending completed messages and reset the resend period to the initial period
        checkStatus = false;
        updateResendPeriod(false);
    }
    
    /**
     * Handle the recovery event.
     *
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completed -> Completed (resend completed)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Faulting -> Ended
     * Faulting-Active -> Ended
     * Faulting-Compensating -> Ended
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended (invalid state)
     */
    public void recovery()
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
        }

        if (current == State.STATE_COMPLETED)
        {
            sendCompleted(true);
        }
    }

    /**
     * Handle the soap fault event.
     * @param soapFault The soap fault.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        boolean deleteRequired;
        boolean checkingStatus;
        synchronized(this) {
            deleteRequired = persisted;
            // make sure delete is attempted only once
            persisted = false;
            checkingStatus = (state == State.STATE_COMPLETED && checkStatus);
            ended() ;
        }
        // TODO -- update doc in interface and user guide.
        try
        {
            boolean isInvalidState = soapFault.getSubcode().equals(CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME);
            if (checkingStatus && isInvalidState) {
                // coordinator must have died before reaching close so just cancel
                WSTLogger.i18NLogger.warn_messaging_engines_CoordinatorCompletionParticipantEngine_soapFault_2(id);
                participant.compensate();
            } else {
                // hmm, something went wrong -- notify the participant of the error
                WSTLogger.i18NLogger.warn_messaging_engines_CoordinatorCompletionParticipantEngine_soapFault_3(id);
                participant.error() ;
            }
        }
        catch (final Throwable th) {} // ignore
        // if we just ended the participant ensure any log record gets deleted
        if (deleteRequired) {
            if (!XTSBARecoveryManager.getRecoveryManager().deleteParticipantRecoveryRecord(id)) {
                // hmm, could not delete entry -- nothing more we can do than log a message
                WSTLogger.i18NLogger.warn_messaging_engines_CoordinatorCompletionParticipantEngine_soapFault_1(id);
            }
        }
    }
    
    /**
     * Handle the completed event.
     * 
     * Active -> Completed
     * Canceling -> Canceling (invalid state)
     * Completing -> Completed
     * Completed -> Completed
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Faulting -> Faulting (invalid state)
     * Faulting-Active -> Faulting-Active (invalid state)
     * Faulting-Compensating -> Faulting-Compensating (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended (invalid state)
     */
    public State completed()
    {
        State current ;
        boolean faultRequired  = false;
        boolean deleteRequired  = false;
        synchronized(this)
        {
            current = state ;
            if ((current == State.STATE_ACTIVE) || (current == State.STATE_COMPLETING))
            {
                changeState(State.STATE_COMPLETED) ;
            }
        }
        if (current == State.STATE_COMPLETING) {
            // ok we need to write the participant details to disk because it has just completed
            BAParticipantRecoveryRecord recoveryRecord = new BAParticipantRecoveryRecord(id, participant, false, coordinator);

            if (!XTSBARecoveryManager.getRecoveryManager().writeParticipantRecoveryRecord(recoveryRecord)) {
                // hmm, could not write entry log warning
                WSTLogger.i18NLogger.warn_messaging_engines_ParticipantCompletionParticipantEngine_completed_1(id);
                // we need to fail this transaction
                faultRequired = true;
            }
        }
        // recheck state before we decide whether we need to fail -- we might have been sent a cancel while
        // writing the log

        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_COMPLETED) {
                if (!faultRequired) {
                    // record the fact that we have persisted this object so later operations will delete
                    // the log record
                    persisted = true;
                } else {
                    // we must force a fault but we don't have a log record to delete
                    changeState(State.STATE_FAULTING_ACTIVE);
                }
            } else {
                // we need to delete the log record here as the cancel would not have known it was persisted
                deleteRequired = true;
            }
        }

                // check to see if we need to send a fail or delete the log record before going ahead to complete

        if (faultRequired) {
            current = fault();
        } else if (deleteRequired) {
            if (!XTSBARecoveryManager.getRecoveryManager().deleteParticipantRecoveryRecord(id)) {
                // hmm, could not delete entry log warning
                WSTLogger.i18NLogger.warn_messaging_engines_ParticipantCompletionParticipantEngine_completed_2(id);
            }
        } else if ((current == State.STATE_COMPLETING) || (current == State.STATE_COMPLETED))
        {
            sendCompleted() ;
        }
        
        return current ;
    }
    
    /**
     * Handle the exit event.
     * 
     * Active -> Exiting
     * Canceling -> Canceling (invalid state)
     * Completing -> Exiting
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Faulting -> Faulting (invalid state)
     * Faulting-Active -> Faulting-Active (invalid state)
     * Faulting-Compensating -> Faulting-Compensating (invalid state)
     * Exiting -> Exiting
     * Ended -> Ended (invalid state)
     */
    public State exit()
    {
        final State current ;
        synchronized (this)
        {
            current = state ;
            if ((current == State.STATE_ACTIVE) || (current == State.STATE_COMPLETING))
            {
                changeState(State.STATE_EXITING) ;
            }
        }
        
        if ((current == State.STATE_ACTIVE) || (current == State.STATE_COMPLETING) ||
            (current == State.STATE_EXITING))
        {
            sendExit() ;
        }
        
        return waitForState(State.STATE_EXITING, timeout) ;
    }
    
    /**
     * Handle the fault event.
     * 
     * Active -> Faulting-Active
     * Canceling -> Canceling (invalid state)
     * Completing -> Faulting-Active
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Faulting-Compensating
     * Faulting -> Faulting
     * Faulting-Active -> Faulting-Active
     * Faulting-Compensating -> Faulting-Compensating
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended (invalid state)
     */
    public State fault()
    {
        final State current ;
        synchronized (this)
        {
            current = state ;
            if ((current == State.STATE_ACTIVE) || (current == State.STATE_COMPLETING))
            {
                changeState(State.STATE_FAULTING_ACTIVE) ;
            }
            else if (current == State.STATE_COMPENSATING)
            {
                changeState(State.STATE_FAULTING_COMPENSATING) ;
            }
        }
        
        if ((current == State.STATE_ACTIVE) || (current == State.STATE_COMPLETING) ||
            (current == State.STATE_FAULTING_ACTIVE))
        {
            sendFault("Fault called when state active/faulting active") ;
            return waitForState(State.STATE_FAULTING_ACTIVE, timeout) ;
        }
        else if ((current == State.STATE_COMPENSATING) || (current == State.STATE_FAULTING_COMPENSATING))
        {
            sendFault("Fault called when state compensating/faulting compensating") ;
        }
        
        return waitForState(State.STATE_FAULTING_COMPENSATING, timeout) ;
    }
    
    /**
     * Handle the comms timeout event.
     * 
     * Completed -> Completed (resend Completed)
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
        
        if (current == State.STATE_COMPLETED)
        {
            sendCompleted(true) ;
        }
    }
    
    /**
     * Send the exit message.
     * 
     */
    private void sendExit()
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendExit(addressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception while sending Exit", th) ;
            }
        }
    }
    
    /**
     * Send the completed message
     */

    private void sendCompleted()
    {
        sendCompleted(false);
    }

    /**
     * Send the completed message.
     * 
     */
    private void sendCompleted(boolean timedOut)
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendCompleted(addressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception while sending Completed", th) ;
            }
        }
        
        // if we timed out the increase the resend period otherwise make sure it is reset to the
        // initial resend period

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
     * Send the fault message.
     * @param message The fault message.
     * 
     */
    private void sendFault(final String message)
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendFault(addressingContext, instanceIdentifier, message) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception while sending Fault", th) ;
            }
        }
    }
    
    /**
     * Send the cancelled message.
     * 
     */
    private void sendCancelled()
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendCancelled(addressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception while sending Cancelled", th) ;
            }
        }
    }
    
    /**
     * Send the closed message.
     * 
     */
    private void sendClosed()
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendClosed(addressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception while sending Closed", th) ;
            }
        }
    }
    
    /**
     * Send the compensated message.
     * 
     */
    private void sendCompensated()
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendCompensated(addressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception while sending Compensated", th) ;
            }
        }
    }
    
    /**
     * Send the status message.
     * @param state The state.
     * 
     */
    private void sendStatus(final State state)
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendStatus(addressingContext, instanceIdentifier, state) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception while sending Status", th) ;
            }
        }
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
     * Get the coordinator endpoint reference
     * @return The coordinator endpoint reference
     */
    public EndpointReferenceType getCoordinator()
    {
        return coordinator ;
    }
    
    /**
     * Get the associated participant.
     * @return The associated participant.
     */
    public BusinessAgreementWithCoordinatorCompletionParticipant getParticipant()
    {
        return participant ;
    }
    
    /**
     * check whether this participant's details have been recovered from the log
     * @return true if the participant is recovered otherwise false
     */
    public boolean isRecovered()
    {
        return recovered;
    }

    /**
     * Change the state and notify any listeners.
     * @param state The new state.
     */
    private synchronized void changeState(final State state)
    {
        if (this.state != state)
        {
            this.state = state ;
            notifyAll() ;
        }
    }
    
    /**
     * Wait for the state to change from the specified state.
     * @param origState The original state.
     * @param delay The maximum time to wait for (in milliseconds).
     * @return The current state.
     */
    private State waitForState(final State origState, final long delay)
    {
        final long end = System.currentTimeMillis() + delay ;
        synchronized(this)
        {
            while(state == origState)
            {
                final long remaining = end - System.currentTimeMillis() ;
                if (remaining <= 0)
                {
                    break ;
                }
                try
                {
                    wait(remaining) ;
                }
                catch (final InterruptedException ie) {} // ignore
            }
            return state ;
        }
    }

    /**
     * Execute the cancel transition.
     * 
     */
    private void executeCancel(boolean duringComplete)
    {
        boolean faultRequired = false;

        // TODO -- there is a potential race here with a completing thread
        // the state diagrams in the spec say that if a cancel comes in while completing we have to cancel
        // but the participant may be part way through executing a complete. strictly, that's something
        // the participant has to deal with not us
        try
        {
            participant.cancel() ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception from participant cancel for WS-BA participant {0}", new Object[] {id}, th) ;
            }
            /*
             * we can get here from state ACTIVE or CONPLETING. we could roll back the state as though the cancel
             * never happened. the only problem is that coming from state COMPLETING we don't know whether the
             * completing thread has logged its state or logged it and then deleted it because it saw the transition
             * to CANCELING. so we roll back to ACTIVE but fail if we have come from COMPLETING
             */
            synchronized (this) {
                if (state == State.STATE_CANCELING) {
                    if (duringComplete) {
                        faultRequired = true;
                        changeState(State.STATE_FAULTING_ACTIVE);
                    } else {
                        changeState(State.STATE_ACTIVE);
                        return ;
                    }
                }
            }
        }
        if (faultRequired) {
            fault();
        } else {
            sendCancelled() ;
            ended() ;
        }
    }

    /**
     * Execute the close transition.
     * 
     */
    private void executeClose()
    {
        try
        {
            participant.close() ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception from participant close for WS-BA participant {0}", new Object[] {id}, th) ;
            }
            // restore previous state so we can retry the close otherwise we get stuck in state closing forever
            changeState(State.STATE_COMPLETED);

            initiateTimer();
            return ;
        }
        // delete any log record for the participant
        if (persisted) {
            // if we cannot delete the participant record we effectively drop the close message
            // here in the hope that we have better luck next time..
            if (!XTSBARecoveryManager.getRecoveryManager().deleteParticipantRecoveryRecord(id)) {
                // hmm, could not delete entry -- leave it so we can maybe retry later
                WSTLogger.i18NLogger.warn_messaging_engines_ParticipantCompletionParticipantEngine_executeClose_2(id);
                // restore previous state so we can retry the close otherwise we get stuck in state closing forever

                changeState(State.STATE_COMPLETED);

                initiateTimer();

                return;
            }
        }

        sendClosed() ;
        ended() ;
    }
    
    /**
     * Execute the compensate transition.
     * 
     */
    private void executeCompensate()
    {
        try
        {
            participant.compensate() ;
        }
        catch (final FaultedException fe) {
            WSTLogger.i18NLogger.warn_messaging_engines_CoordinatorCompletionParticipantEngine_executeCompensate_1(id, fe);
            // fault here because the aprticipant doesn't want to retry the compensate
            fault();
        }
        catch (final Throwable th)
        {
            final State current ;
            synchronized (this)
            {
                current = state ;
                if (current == State.STATE_COMPENSATING)
                {
                    changeState(State.STATE_COMPLETED) ;
                }
            }
            if (current == State.STATE_COMPENSATING)
            {
                initiateTimer() ;
            }
            
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception from participant compensate for WS-BA participant {0}", th) ;
            }
            return ;
        }
        
        final State current ;
        boolean faultRequired = false;
        synchronized (this)
        {
            current = state ;
            // need to do this while synchronized so no fail calls can get in on between

            if (current == State.STATE_COMPENSATING)
            {
                if (persisted) {
                    if (!XTSBARecoveryManager.getRecoveryManager().deleteParticipantRecoveryRecord(id)) {
                        // we have to fail since we don't want to run the compensate method again
                        WSTLogger.i18NLogger.warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_executeCompensate_3(id);
                        faultRequired = true;
                        changeState(State.STATE_FAULTING_COMPENSATING);
                    }
                }
                // if we did not fail then we can decommission the participant now avoiding any further races
                // we will send the compensate after we exit the synchronized block
                if (!faultRequired) {
                    ended();
                }
            }
        }
        if (faultRequired) {
            fault();
        } else if (current == State.STATE_COMPENSATING)
        {
            sendCompensated() ;
        }
    }
    
    /**
     * Execute the complete transition.
     * 
     */
    private void executeComplete()
    {
        try
        {
            participant.complete() ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception from participant complete for WS-BA  parfticipant {0}", new Object[] {id}, th) ;
            }
            return ;
        }
        
        State current ;
        boolean faultRequired  = false;
        boolean deleteRequired  = false;
        synchronized (this)
        {
            current = state ;
            if (current == State.STATE_COMPLETING)
            {
                changeState(State.STATE_COMPLETED) ;
            }
        }
        if (current == State.STATE_COMPLETING)
        {
            // ok we need to write the participant details to disk because it has just completed
            BAParticipantRecoveryRecord recoveryRecord = new BAParticipantRecoveryRecord(id, participant, false, coordinator);

            if (!XTSBARecoveryManager.getRecoveryManager().writeParticipantRecoveryRecord(recoveryRecord)) {
                // hmm, could not write entry log warning
                 WSTLogger.i18NLogger.warn_messaging_engines_CoordinatorCompletionParticipantEngine_executeComplete_2(id);
                // we need to fail this transaction
                faultRequired = true;
            }
        }

        // recheck state before we decide whether we need to fail -- we might have been sent a cancel while
        // writing the log

        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_COMPLETED) {
                if (!faultRequired) {
                    // record the fact that we have persisted this object so later operations will delete
                    // the log record
                    persisted = true;
                } else {
                    // we must force a fail but we don't have a log record to delete
                    changeState(State.STATE_FAULTING_ACTIVE);
                }
            } else {
                // we cannot force a fail now so just delete
                faultRequired = false;
                // we need to delete the log record here as the cancel would not have known it was persisted
                deleteRequired = true;
            }
        }

        // check to see if we need to send a fail or delete the log record before going ahead to complete

        if (faultRequired) {
            current = fault();
        } else if (deleteRequired) {
            if (!XTSBARecoveryManager.getRecoveryManager().deleteParticipantRecoveryRecord(id)) {
                // hmm, could not delete entry log warning
                WSTLogger.i18NLogger.warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_completed_2(id);
            }
        } else if (current == State.STATE_COMPLETING || current == State.STATE_COMPLETED)
        {
            sendCompleted() ;
        }
    }
    
    /**
     * End the current participant.
     */
    private void ended()
    {
	changeState(State.STATE_ENDED) ;
        CoordinatorCompletionParticipantProcessor.getProcessor().deactivateParticipant(this) ;
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
        
        if (state == State.STATE_COMPLETED)
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
     * Create a context for the outgoing message.
     * @return The addressing context.
     */
    private AddressingContext createContext()
    {
        final String messageId = MessageId.getMessageId() ;
        return AddressingContext.createRequestContext(coordinator, messageId) ;
    }
}
