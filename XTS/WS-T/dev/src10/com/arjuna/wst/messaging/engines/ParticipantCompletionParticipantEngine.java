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
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.util.TransportTimer;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.ParticipantCompletionParticipantInboundEvents;
import com.arjuna.webservices.wsba.State;
import com.arjuna.webservices.wsba.StatusType;
import com.arjuna.webservices.wsba.client.ParticipantCompletionCoordinatorClient;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.wsc.messaging.MessageId;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.FaultedException;
import org.jboss.jbossts.xts.recovery.participant.ba.XTSBARecoveryManager;
import org.jboss.jbossts.xts10.recovery.participant.ba.BAParticipantRecoveryRecord;

/**
 * The participant completion participant state engine
 * @author kevin
 */
public class ParticipantCompletionParticipantEngine implements ParticipantCompletionParticipantInboundEvents
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
    private final BusinessAgreementWithParticipantCompletionParticipant participant ;
    /**
     * The current state.
     */
    private State state ;
    /**
     * The associated timer task or null.
     */
    private TimerTask timerTask ;
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
     * @param id The participant id.
     * @param coordinator The coordinator endpoint reference.
     * @param participant The participant.
     */
    public ParticipantCompletionParticipantEngine(final String id, final EndpointReferenceType coordinator,
        final BusinessAgreementWithParticipantCompletionParticipant participant)
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
    public ParticipantCompletionParticipantEngine(final String id, final EndpointReferenceType coordinator,
        final BusinessAgreementWithParticipantCompletionParticipant participant, final State state, final boolean recovered)
    {
        this.id = id ;
        this.instanceIdentifier = new InstanceIdentifier(id) ;
        this.coordinator = coordinator ;
        this.participant = participant ;
        this.state = state ;
        this.recovered = recovered;
        this.persisted = recovered;
    }
    
    /**
     * Handle the cancel event.
     * @param cancel The cancel notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * Active -> Canceling
     * Canceling -> Canceling
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
            if (current == State.STATE_ACTIVE)
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
            executeCancel() ;
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
     * Handle the exited event.
     * @param exited The exited notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
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
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Faulting -> Ended
     * Faulting-Active -> Ended
     * Faulting-Compensating -> Ended
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.faulted_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.faulted_1] - Unable to delete recovery record during faulted for WS-BA participant {0}
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
                if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                {
                    WSTLogger.arjLoggerI18N.warn("com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.faulted_1", new Object[] {id}) ;
                }
            }
        }
    }
    
    /**
     * Handle the getStatus event.
     * @param getStatus The getStatus notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.getStatus_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.getStatus_1] - Unknown error: {0}
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
        // KEV - implement
    }
    
    /**
     * Handle the soap fault event.
     * @param soapFault The soap fault.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.soapFault_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.soapFault_1] - Unable to delete recovery record during soapFault processing for WS-BA participant {0}
     */
    public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        boolean deleteRequired;
        synchronized(this) {
            deleteRequired = persisted;
            ended() ;
        }
        // TODO -- clarify when and why this gets called and update doc in interface. also check unknown()
        try
        {
            participant.error() ;
        }
        catch (final Throwable th) {} // ignore
        // if we just ended the participant ensure any log record gets deleted
        if (deleteRequired) {
            if (!XTSBARecoveryManager.getRecoveryManager().deleteParticipantRecoveryRecord(id)) {
                // hmm, could not delete entry -- nothing more we can do than log a message
                if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                {
                    WSTLogger.arjLoggerI18N.warn("com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.soapFault_1", new Object[] {id}) ;
                }
            }
        }
    }
    
    /**
     * Handle the completed event.
     * 
     * Active -> Completed
     * Canceling -> Canceling (invalid state)
     * Completed -> Completed
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Faulting -> Faulting (invalid state)
     * Faulting-Active -> Faulting-Active (invalid state)
     * Faulting-Compensating -> Faulting-Compensating (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended (invalid state)
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.completed_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.completed_1] - Unable to write recovery record during completed for WS-BA participant {0}
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.completed_2 [com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.completed_2] - Unable to delete recovery record during completed for WS-BA participant {0}
     */
    public State completed()
    {
        State current ;
        boolean faultRequired  = false;
        boolean deleteRequired  = false;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_ACTIVE)
            {
                changeState(State.STATE_COMPLETED) ;
            }
        }
        
        if (current == State.STATE_ACTIVE) {
            // ok we need to write the participant details to disk because it has just completed
            BAParticipantRecoveryRecord recoveryRecord = new BAParticipantRecoveryRecord(id, participant, true, coordinator);

            if (!XTSBARecoveryManager.getRecoveryManager().writeParticipantRecoveryRecord(recoveryRecord)) {
                // hmm, could not write entry log warning
                if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                {
                    WSTLogger.arjLoggerI18N.warn("com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.completed_1", new Object[] {id}) ;
                }
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
                if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                {
                    WSTLogger.arjLoggerI18N.warn("com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.completed_2", new Object[] {id}) ;
                }
            }
        } else if ((current == State.STATE_ACTIVE) || (current == State.STATE_COMPLETED))
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
            if (current == State.STATE_ACTIVE)
            {
                changeState(State.STATE_EXITING) ;
            }
        }
        
        if ((current == State.STATE_ACTIVE) || (current == State.STATE_EXITING))
        {
            sendExit() ;
        }
        
        return waitForState(State.STATE_EXITING, TransportTimer.getTransportTimeout()) ;
    }
    
    /**
     * Handle the fault event.
     * 
     * Active -> Faulting-Active
     * Canceling -> Canceling (invalid state)
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
            if (current == State.STATE_ACTIVE)
            {
                changeState(State.STATE_FAULTING_ACTIVE) ;
            }
            else if (current == State.STATE_COMPENSATING)
            {
                changeState(State.STATE_FAULTING_COMPENSATING) ;
            }
        }
        
        if ((current == State.STATE_ACTIVE) || (current == State.STATE_FAULTING_ACTIVE))
        {
            sendFault("Fault called when state active/faulting active") ;
            return waitForState(State.STATE_FAULTING_ACTIVE, TransportTimer.getTransportTimeout()) ;
        }
        else if ((current == State.STATE_COMPENSATING) || (current == State.STATE_FAULTING_COMPENSATING))
        {
            sendFault("Fault called when state compensating/faulting compensating") ;
        }
        
        return waitForState(State.STATE_FAULTING_COMPENSATING, TransportTimer.getTransportTimeout()) ;
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
            sendCompleted() ;
        }
    }
    
    /**
     * Send the exit message.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendExit_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendExit_1] - Unexpected exception while sending Exit
     */
    private void sendExit()
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendExit(addressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendExit_1", th) ;
            }
        }
    }
    
    /**
     * Send the completed message.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendCompleted_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendCompleted_1] - Unexpected exception while sending Completed
     */
    private void sendCompleted()
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendCompleted(addressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendCompleted_1", th) ;
            }
        }
        
        initiateTimer() ;
    }
    
    /**
     * Send the fault message.
     * @param message The fault message.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendFault_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendFault_1] - Unexpected exception while sending Fault
     */
    private void sendFault(final String message)
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendFault(addressingContext, instanceIdentifier, message) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendFault_1", th) ;
            }
        }
    }
    
    /**
     * Send the cancelled message.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendCancelled_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendCancelled_1] - Unexpected exception while sending Cancelled
     */
    private void sendCancelled()
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendCancelled(addressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendCancelled_1", th) ;
            }
        }
    }
    
    /**
     * Send the closed message.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendClosed_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendClosed_1] - Unexpected exception while sending Closed
     */
    private void sendClosed()
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendClosed(addressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendClosed_1", th) ;
            }
        }
    }
    
    /**
     * Send the compensated message.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendCompensated_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendCompensated_1] - Unexpected exception while sending Compensated
     */
    private void sendCompensated()
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendCompensated(addressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendCompensated_1", th) ;
            }
        }
    }
    
    /**
     * Send the status message.
     * @param state The state.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendStatus_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendStatus_1] - Unexpected exception while sending Status
     */
    private void sendStatus(final State state)
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendStatus(addressingContext, instanceIdentifier, state) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.sendStatus_1", th) ;
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
    public BusinessAgreementWithParticipantCompletionParticipant getParticipant()
    {
        return participant ;
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
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.executeCancel_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.executeCancel_1] - Unexpected exception from participant cancel fro WS-BA participant {0}
     */
    private void executeCancel()
    {
        try
        {
            participant.cancel() ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.executeCancel_1", new Object[] { id}, th) ;
            }
            /*
             * we only get here in from state ACTIVE so if we are still in state CANCELING then roll back the
             * state allowing a retry of the cancel
             */
            synchronized (this) {
                if (state == State.STATE_CANCELING) {
                    changeState(State.STATE_ACTIVE);
                }
            }
            return ;
        }
        sendCancelled() ;
        ended() ;
    }

    /**
     * Execute the close transition.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.executeClose_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.executeClose_1] - Unexpected exception from participant close for WS-BA participant {0}
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.executeClose_2 [com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.executeClose_2] - Unable to delete recovery record during close for WS-BA participant {0}
     */
    private void executeClose()
    {
        try
        {
            participant.close() ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.executeClose_1", new Object[] {id}, th) ;
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
                if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                {
                    WSTLogger.arjLoggerI18N.warn("com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.executeClose_2", new Object[] {id}) ;
                }
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
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.executeCompensate_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.executeCompensate_1] - Faulted exception from participant compensate for WS-BA participant {0}
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.executeCompensate_2 [com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.executeCompensate_2] - Unexpected exception from participant compensate for WS-BA participant {0}
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.executeCompensate_3 [com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.executeCompensate_3] - Unable to delete recovery record during compensate for WS-BA participant {0}
     */
    private void executeCompensate()
    {
        try
        {
            participant.compensate() ;
        }
        catch (final FaultedException fe)
        {
            if (WSTLogger.arjLoggerI18N.isWarnEnabled())
            {
                WSTLogger.arjLoggerI18N.warn("com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.executeCompensate_1", new Object[] {id}, fe);
            }
            // fault here because the participant doesn't want to retry the compensate            
            fault() ;
            return;
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
            
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.executeCompensate_", new Object[] {id}, th) ;
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
                        if (WSTLogger.arjLoggerI18N.isWarnEnabled())
                        {
                            WSTLogger.arjLoggerI18N.warn("com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine.executeCompensate_3", new Object[] {id}) ;
                        }
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
     * End the current participant.
     */
    private void ended()
    {
	changeState(State.STATE_ENDED) ;
        ParticipantCompletionParticipantProcessor.getProcessor().deactivateParticipant(this) ;
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
            TransportTimer.getTimer().schedule(timerTask, TransportTimer.getTransportPeriod()) ;
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
