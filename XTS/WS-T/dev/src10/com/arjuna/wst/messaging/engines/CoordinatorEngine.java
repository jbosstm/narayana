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
import com.arjuna.webservices.SoapFault10;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.util.TransportTimer;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsaddr.RelationshipType;
import com.arjuna.webservices.wsarj.ArjunaContext;      
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsat.CoordinatorInboundEvents;
import com.arjuna.webservices.wsat.NotificationType;
import com.arjuna.webservices.wsat.State;
import com.arjuna.webservices.wsat.client.ParticipantClient;
import com.arjuna.webservices.wsat.processors.CoordinatorProcessor;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.wsc.messaging.MessageId;

/**
 * The coordinator state engine
 * @author kevin
 */
public class CoordinatorEngine implements CoordinatorInboundEvents
{
    /**
     * Flag indicating this is a coordinator for a durable participant.
     */
    private final boolean durable ;
    /**
     * The coordinator id.
     */
    private final String id ;
    /**
     * The instance identifier.
     */
    private final InstanceIdentifier instanceIdentifier ;
    /**
     * The participant endpoint reference.
     */
    private final EndpointReferenceType participant ;
    /**
     * The current state.
     */
    private State state ;
    /**
     * The flag indicating that this coordinator has been recovered from the log.
     */
    private boolean recovered ;
    /**
     * The flag indicating a read only response.
     */
    private boolean readOnly ;
    /**
     * The associated timer task or null.
     */
    private TimerTask timerTask ;
    
    /**
     * Construct the initial engine for the coordinator.
     * @param id The coordinator id.
     * @param durable true if the participant is durable, false if volatile. 
     * @param participant The participant endpoint reference.
     */
    public CoordinatorEngine(final String id, final boolean durable, final EndpointReferenceType participant)
    {
        this(id, durable, participant, false, State.STATE_ACTIVE) ;
    }
    
    /**
     * Construct the engine for the coordinator in a specified state.
     * @param id The coordinator id.
     * @param durable true if the participant is durable, false if volatile. 
     * @param participant The participant endpoint reference.
     * @param state The initial state.
     */
    public CoordinatorEngine(final String id, final boolean durable, final EndpointReferenceType participant, boolean recovered, final State state)
    {
        this.id = id ;
        this.instanceIdentifier = new InstanceIdentifier(id) ;
        this.durable = durable ;
        this.participant = participant ;
        this.recovered = recovered;
        this.state = state ;

        // unrecovered participants are always activated
        // we only need to reactivate recovered participants which were successfully prepared
        // any others will only have been saved because of a heuristic outcome e.g. a comms
        // timeout at prepare will write a heuristic record for an ABORTED TX including a
        // participant in state PREPARING. we can safely drop it since we implement presumed abort.

        if (!recovered || state == State.STATE_PREPARED_SUCCESS) {
            CoordinatorProcessor.getProcessor().activateCoordinator(this, id) ;
        }
    }
    
    /**
     * Handle the aborted event.
     * @param aborted The aborted notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * None -> None (ignore)
     * Active -> Aborting (forget)
     * Preparing -> Aborting (forget)
     * PreparedSuccess -> PreparedSuccess (invalid state)
     * Committing -> Committing (invalid state)
     * Aborting -> Aborting (forget)
     */
    public synchronized void aborted(final NotificationType aborted, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current = state ;
        if (current == State.STATE_ACTIVE)
        {
            changeState(State.STATE_ABORTING) ;
        }
        else if ((current == State.STATE_PREPARING) || (current == State.STATE_ABORTING))
        {
            forget() ;
        }
    }
    
    /**
     * Handle the committed event.
     * @param committed The committed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * None -> None (ignore)
     * Active -> Aborting (invalid state)
     * Preparing -> Aborting (invalid state)
     * PreparedSuccess -> PreparedSuccess (invalid state)
     * Committing -> Committing (forget)
     * Aborting -> Aborting (invalid state)
     */
    public synchronized void committed(final NotificationType committed, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current = state ;
        if (current == State.STATE_ACTIVE)
        {
            changeState(State.STATE_ABORTING) ;
        }
        else if ((current == State.STATE_PREPARING) || (current == State.STATE_COMMITTING))
        {
            forget() ;
        }
    }
    
    /**
     * Handle the prepared event.
     * @param prepared The prepared notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * None -> Durable: (send rollback), Volatile: Invalid state: none
     * Active -> Aborting (invalid state)
     * Preparing -> PreparedSuccess (Record Vote)
     * PreparedSuccess -> PreparedSuccess (ignore)
     * Committing -> Committing (resend Commit)
     * Aborting -> Aborting (resend Rollback and forget)
     */
    public void prepared(final NotificationType prepared, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_ACTIVE)
            {
                changeState(State.STATE_ABORTING) ;
            }
            else if (current == State.STATE_PREPARING)
            {
                changeState(State.STATE_PREPARED_SUCCESS) ;
            }
        }
        if (current == State.STATE_COMMITTING)
        {
            sendCommit() ;
        }
        else if ((current == State.STATE_ABORTING) || ((current == null) && !readOnly))
        {
            if (durable)
            {
                sendRollback() ;
            }
            else
            {
        	sendInvalidState(addressingContext, arjunaContext) ;
            }
            if (current != null)
            {
        	forget() ;
            }
        }
    }
    
    /**
     * Handle the readOnly event.
     * @param readOnly The readOnly notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * None -> None (ignore)
     * Active -> Active (forget)
     * Preparing -> Preparing (forget)
     * PreparedSuccess -> PreparedSuccess (invalid state)
     * Committing -> Committing (invalid state)
     * Aborting -> Aborting (forget)
     */
    public synchronized void readOnly(final NotificationType readOnly, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current = state ;
        if ((current == State.STATE_ACTIVE) || (current == State.STATE_PREPARING) ||
            (current == State.STATE_ABORTING))
        {
            if (current != State.STATE_ABORTING)
            {
                this.readOnly = true ;
            }
            forget() ;
        }
    }
    
    /**
     * Handle the replay event.
     * @param replay The replay notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * None -> Durable: (send rollback), Volatile: Invalid state: none
     * Active -> Aborting (send rollback)
     * Preparing -> Aborting (send rollback)
     * PreparedSuccess -> PreparedSuccess (ignore)
     * Committing -> Committing (send commit)
     * Aborting -> Aborting (send rollback)
     */
    public void replay(final NotificationType replay, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if ((current == State.STATE_ACTIVE) || (current == State.STATE_PREPARING))
            {
                changeState(State.STATE_ABORTING) ;
            }
        }
        
        if ((current == State.STATE_ACTIVE) || (current == State.STATE_PREPARING) ||
            (current == State.STATE_ABORTING) || ((current == null) && durable))
        {
            sendRollback() ;
        }
        else if (current == State.STATE_COMMITTING)
        {
            sendCommit() ;
        }
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
        if (WSTLogger.logger.isDebugEnabled())
        {
            final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
            final SoapFaultType soapFaultType = soapFault.getSoapFaultType() ;
            final QName subCode = soapFault.getSubcode() ;
            WSTLogger.logger.debugv("Unexpected SOAP fault for coordinator {0}: {1} {2}", new Object[] {instanceIdentifier, soapFaultType, subCode}) ;
        }
    }
    
    /**
     * Handle the prepare event.
     * 
     * None -> None (invalid state)
     * Active -> Preparing (send prepare)
     * Preparing -> Preparing (resend prepare)
     * PreparedSuccess -> PreparedSuccess (do nothing)
     * Committing -> Committing (invalid state)
     * Aborting -> Aborting (invalid state)
     */
    public State prepare()
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_ACTIVE)
            {
                changeState(State.STATE_PREPARING) ;
            }
        }
        
        if ((current == State.STATE_ACTIVE) || (current == State.STATE_PREPARING))
        {
            sendPrepare() ;
        }
        
        waitForState(State.STATE_PREPARING, TransportTimer.getTransportTimeout()) ;

        synchronized(this)
        {
            if (state != State.STATE_PREPARING)
            {
                return state ;
            }

            if (timerTask != null)
            {
        	timerTask.cancel() ;

                timerTask = null;
            }

            // ok, we leave the participant stub active because the coordinator will attempt
            // to roll it back when it notices that this has failed

            return state ;
        }
    }
    
    /**
     * Handle the commit event.
     * 
     * None -> None (invalid state)
     * Active -> Active (invalid state)
     * Preparing -> Preparing (invalid state)
     * PreparedSuccess -> Committing (send commit)
     * Committing -> Committing (resend commit)
     * Aborting -> Aborting (invalid state)
     */
    public State commit()
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_PREPARED_SUCCESS)
            {
                changeState(State.STATE_COMMITTING) ;
            }
        }
        
        if ((current == State.STATE_PREPARED_SUCCESS) || (current == State.STATE_COMMITTING))
        {
            sendCommit() ;
        }
        
        waitForState(State.STATE_COMMITTING, TransportTimer.getTransportTimeout()) ;

        synchronized(this)
        {
            if (state != State.STATE_COMMITTING)
            {
                // if this is a recovered participant then forget will not have
                // deactivated the entry so that this (recovery) thread can
                // detect it and update its log entry. so we need to deactivate
                // the entry here.

                if (recovered) {
                    CoordinatorProcessor.getProcessor().deactivateCoordinator(this) ;
                }

                return state ;
            }

            // the participant is still uncommitted so it will be rewritten to the log.
            // it remains activated in case a committed message comes in between now and
            // the next scan. the recovery code will detect this active participant when
            // rescanning the log and use it instead of recreating a new one.
            // we need to mark this one as recovered so it does not get deleted until
            // the next scan

            recovered = true;

            return State.STATE_COMMITTING;
        }
    }
    
    /**
     * Handle the rollback event.
     * 
     * None -> None (invalid state)
     * Active -> Aborting (send rollback)
     * Preparing -> Aborting (send rollback)
     * PreparedSuccess -> Aborting (send rollback)
     * Committing -> Committing (invalid state)
     * Aborting -> Aborting (do nothing)
     */
    public State rollback()
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if ((current == State.STATE_ACTIVE) || (current == State.STATE_PREPARING) ||
                (current == State.STATE_PREPARED_SUCCESS))
            {
                changeState(State.STATE_ABORTING) ;
            }
        }
        
        if ((current == State.STATE_ACTIVE) || (current == State.STATE_PREPARING) ||
            (current == State.STATE_PREPARED_SUCCESS))
        {
            sendRollback() ;
        }
        else if (current == State.STATE_ABORTING)
        {
            forget() ;
        }
        
        return waitForState(State.STATE_ABORTING, TransportTimer.getTransportTimeout()) ;
    }
    
    /**
     * Handle the comms timeout event.
     *
     * Preparing -> Preparing (resend Prepare)
     * Committing -> Committing (resend Commit)
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
        
        if (current == State.STATE_PREPARING)
        {
            sendPrepare() ;
        }
        else if (current == State.STATE_COMMITTING)
        {
            sendCommit() ;
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
     * Get the participant endpoint reference
     * @return The participant endpoint reference
     */
    public EndpointReferenceType getParticipant()
    {
        return participant ;
    }
    
    /**
     * Is the participant durable?
     * @return true if durable, false otherwise.
     */
    public boolean isDurable()
    {
        return durable ;
    }
    
    /**
     * Is the participant recovered?
     * @return true if recovered, false otherwise.
     */
    public boolean isRecovered()
    {
        return recovered ;
    }

    /**
     * Was this a read only response?
     * @return true if a read only response, false otherwise.
     */
    public synchronized boolean isReadOnly()
    {
        return readOnly ;
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
     * Forget the current coordinator.
     */
    private void forget()
    {
        // first, change state to null to indicate that the participant has completed.

        changeState(null) ;
                   
        // participants which have not been recovered from the log can be deactivated now.

        // participants which have been recovered are left for the recovery thread to deactivate.
        // this is because the recovery thread may have timed out waiting for a response to
        // the commit message and gone on to complete its scan and suspend. the next scan
        // will detect this activated participant and note that it has completed. if a crash
        // happens in between the recovery thread can safely recreate and reactivate the
        // participant and resend the commit since the commit/committed exchange is idempotent.

        if (!recovered) {
            CoordinatorProcessor.getProcessor().deactivateCoordinator(this) ;
        }
    }
    
    /**
     * Send the prepare message.
     * 
     */
    private void sendPrepare()
    {
        TimerTask newTimerTask = createTimerTask();
        synchronized (this) {
            // cancel any existing timer task

            if (timerTask != null) {
                timerTask.cancel();
            }

            // install the new timer task. this signals our intention to post a prepare which may need
            // rescheduling later but allows us to drop the lock on this while we are in the comms layer.
            // our intention can be revised by another thread by reassigning the field to a new task
            // or null

            timerTask = newTimerTask;
        }

        // ok now try the prepare

        try
        {
            ParticipantClient.getClient().sendPrepare(createContext(), instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Unexpecting exception while sending Prepare", th) ;
            }
        }
        
        // reobtain the lock before deciding whether to schedule the timer

        synchronized (this) {
            if (timerTask == newTimerTask) {
                // the timer task has not been cancelled so schedule it if appropriate
                if (state == State.STATE_PREPARING) {
                    scheduleTimer(newTimerTask);
                } else {
                    // no need to schedule it so get rid of it
                    timerTask = null;
                }
            }
        }
    }
    
    /**
     * Send the commit message.
     * 
     */
    private void sendCommit()
    {
        TimerTask newTimerTask = createTimerTask();
        synchronized (this) {
            // cancel any existing timer task

            if (timerTask != null) {
                timerTask.cancel();
            }

            // install the new timer task. this signals our intention to post a commit which may need
            // rescheduling later but allows us to drop the lock on this while we are in the comms layer.
            // our intention can be revised by another thread by reassigning the field to a new task
            // or null

            timerTask = newTimerTask;
        }

        // ok now try the commit

        try
        {
            ParticipantClient.getClient().sendCommit(createContext(), instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Unexpecting exception while sending Commit", th) ;
            }
        }
        
        // reobtain the lock before deciding whether to schedule the timer

        synchronized (this) {
            if (timerTask == newTimerTask) {
                // the timer task has not been cancelled so schedule it if appropriate
                if (state == State.STATE_COMMITTING) {
                    scheduleTimer(newTimerTask);
                } else {
                    // no need to schedule it so get rid of it
                    timerTask = null;
                }
            }
        }
    }
    
    /**
     * Send the rollback message.
     * 
     */
    private void sendRollback()
    {
        try
        {
            ParticipantClient.getClient().sendRollback(createContext(), instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Unexpecting exception while sending Rollback", th) ;
            }
        }
    }
    
    /**
     * Send the InvalidState message.
     * 
     */
    private void sendInvalidState(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        try
        {
            final AddressingContext responseContext = createContext() ;
            final AttributedURIType messageId = addressingContext.getMessageID() ;
            final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
            if (messageId != null)
            {
                responseContext.addRelatesTo(new RelationshipType(messageId.getValue())) ;
            }

            final String message = WSTLogger.i18NLogger.get_messaging_engines_CoordinatorEngine_sendInvalidState_1();
            final SoapFault soapFault = new SoapFault10(SoapFaultType.FAULT_SENDER, CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME, message) ;
            ParticipantClient.getClient().sendSoapFault(responseContext, soapFault, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isDebugEnabled())
            {
                WSTLogger.logger.debugv("Unexpecting exception while sending InvalidState", th) ;
            }
        }
    }
    
    /**
     * create a timer task to handle a comms timeout
     *
     * @return the timer task
     */
    private TimerTask createTimerTask()
    {
        return new TimerTask() {
            public void run() {
                commsTimeout(this) ;
            }
        } ;
    }

    /**
     * schedule a timer task to handle a commms timeout
     * @param timerTask the timer task to be scheduled
     */

    private void scheduleTimer(TimerTask timerTask)
    {
        TransportTimer.getTimer().schedule(timerTask, TransportTimer.getTransportPeriod()) ;
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
        if ((state == State.STATE_PREPARING) || (state == State.STATE_COMMITTING))
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
        return AddressingContext.createRequestContext(participant, messageId) ;
    }
}
