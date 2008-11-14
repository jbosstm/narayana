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

import java.io.IOException;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.util.TransportTimer;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsba.ExceptionType;
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.ParticipantCompletionCoordinatorInboundEvents;
import com.arjuna.webservices.wsba.State;
import com.arjuna.webservices.wsba.StatusType;
import com.arjuna.webservices.wsba.client.ParticipantCompletionParticipantClient;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionCoordinatorProcessor;
import com.arjuna.wsc.messaging.MessageId;
import com.arjuna.wst.BAParticipantManager;

/**
 * The participant completion coordinator state engine
 * @author kevin
 */
public class ParticipantCompletionCoordinatorEngine implements ParticipantCompletionCoordinatorInboundEvents
{
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
     * The associated coordinator
     */
    private BAParticipantManager coordinator ;
    /**
     * The current state.
     */
    private State state ;
    /**
     * The flag indicating that this coordinator has been recovered from the log.
     */
    private boolean recovered ;

    /**
     * Construct the initial engine for the coordinator.
     * @param id The coordinator id.
     * @param participant The participant endpoint reference.
     */
    public ParticipantCompletionCoordinatorEngine(final String id, final EndpointReferenceType participant)
    {
        this(id, participant, State.STATE_ACTIVE, false) ;
    }
    
    /**
     * Construct the engine for the coordinator in a specified state.
     * @param id The coordinator id.
     * @param participant The participant endpoint reference.
     * @param state The initial state.
     */
    public ParticipantCompletionCoordinatorEngine(final String id, final EndpointReferenceType participant,
        final State state, final boolean recovered)
    {
        this.id = id ;
        this.instanceIdentifier = new InstanceIdentifier(id) ;
        this.participant = participant ;
        this.state = state ;
        this.recovered = recovered;
    }
    
    /**
     * Set the coordinator and register
     * @param coordinator
     */
    public void setCoordinator(final BAParticipantManager coordinator)
    {
        this.coordinator = coordinator ;
        // unrecovered participants are always activated
        // we only need to reactivate recovered participants which were successfully COMPLETED or which began
        // CLOSING. any others will only have been saved because of a heuristic outcome. we can safely drop
        // it since we implement presumed abort.
        if (!recovered || state == State.STATE_COMPLETED || state == State.STATE_CLOSING) {
            ParticipantCompletionCoordinatorProcessor.getProcessor().activateCoordinator(this, id) ;
        }
    }
    
    /**
     * Handle the cancelled event.
     * @param cancelled The cancelled notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * Active -> Active (invalid state)
     * Canceling -> Ended
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Faulting -> Faulting (invalid state)
     * Faulting-Active -> Faulting (invalid state)
     * Faulting-Compensating ->Faulting (invalid state) 
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended
     */
    public void cancelled(final NotificationType cancelled, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_CANCELING)
            {
                ended() ;
            }
            else if ((current == State.STATE_FAULTING_ACTIVE) || (current == State.STATE_FAULTING_COMPENSATING))
            {
                changeState(State.STATE_FAULTING) ;
            }
        }
    }
    
    /**
     * Handle the closed event.
     * @param closed The closed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completed -> Completed (invalid state)
     * Closing -> Ended
     * Compensating -> Compensating (invalid state)
     * Faulting -> Faulting (invalid state)
     * Faulting-Active -> Faulting (invalid state)
     * Faulting-Compensating -> Faulting (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended
     */
    public void closed(final NotificationType closed, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_CLOSING)
            {
                ended() ;
            }
            else if ((current == State.STATE_FAULTING_ACTIVE) || (current == State.STATE_FAULTING_COMPENSATING))
            {
                changeState(State.STATE_FAULTING) ;
            }
        }
    }
    
    /**
     * Handle the compensated event.
     * @param compensated The compensated notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Ended
     * Faulting -> Faulting (invalid state)
     * Faulting-Active -> Faulting (invalid state)
     * Faulting-Compensating -> Faulting (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended
     */
    public void compensated(final NotificationType compensated, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_COMPENSATING)
            {
                ended() ;
            }
            else if ((current == State.STATE_FAULTING_ACTIVE) || (current == State.STATE_FAULTING_COMPENSATING))
            {
                changeState(State.STATE_FAULTING) ;
            }
        }
    }
    
    /**
     * Handle the completed event.
     * @param completed The completed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * Active -> Completed
     * Canceling -> Compensating
     * Completed -> Completed
     * Closing -> Closing (resend close)
     * Compensating -> Compensating (resend compensate)
     * Faulting -> Faulting (invalid state)
     * Faulting-Active -> Faulting (invalid state)
     * Faulting-Compensating -> Faulting
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended
     */
    public void completed(final NotificationType completed, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if ((current == State.STATE_ACTIVE) || (current == State.STATE_CANCELING))
            {
                changeState(State.STATE_COMPLETED) ;
            }
            else if ((current == State.STATE_FAULTING_ACTIVE) || (current == State.STATE_FAULTING_COMPENSATING))
            {
                changeState(State.STATE_FAULTING) ;
            }
        }
        
        if ((current == State.STATE_ACTIVE) || (current == State.STATE_CANCELING))
        {
            executeCompleted() ;
        }
        else if (current == State.STATE_CLOSING)
        {
            sendClose() ;
        }
        else if ((current == State.STATE_CANCELING) || (current == State.STATE_COMPENSATING))
        {
            sendCompensate() ;
        }
    }
    
    /**
     * Handle the exit event.
     * @param exit The exit notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * Active -> Exiting
     * Canceling -> Exiting
     * Completed -> Completed
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Faulting -> Faulting (invalid state)
     * Faulting-Active -> Faulting (invalid state)
     * Faulting-Compensating -> Faulting (invalid state)
     * Exiting -> Exiting
     * Ended -> Ended (resend Exited)
     */
    public void exit(final NotificationType exit, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if ((current == State.STATE_ACTIVE) || (current == State.STATE_CANCELING))
            {
                changeState(State.STATE_EXITING) ;
            }
            else if ((current == State.STATE_FAULTING_ACTIVE) || (current == State.STATE_FAULTING_COMPENSATING))
            {
                changeState(State.STATE_FAULTING) ;
            }
        }
        
        if ((current == State.STATE_ACTIVE) || (current == State.STATE_CANCELING))
        {
            executeExit() ;
        }
        else if (current == State.STATE_ENDED)
        {
            sendExited() ;
        }
    }
    
    /**
     * Handle the fault event.
     * @param fault The fault exception.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * Active -> Faulting-Active
     * Canceling -> Faulting-Active
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Faulting-Compensating
     * Faulting -> Faulting
     * Faulting-Active -> Faulting
     * Faulting-Compensating -> Faulting
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended (resend Faulted)
     */
    public void fault(final ExceptionType fault, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if ((current == State.STATE_ACTIVE) || (current == State.STATE_CANCELING))
            {
                changeState(State.STATE_FAULTING_ACTIVE) ;
            }
            else if (current == State.STATE_COMPENSATING)
            {
                changeState(State.STATE_FAULTING_COMPENSATING) ;
            }
            else if ((current == State.STATE_FAULTING_ACTIVE) || (current == State.STATE_FAULTING_COMPENSATING))
            {
                changeState(State.STATE_FAULTING) ;
            }
        }
        
        if (current == State.STATE_ACTIVE)
        {
            executeFault() ;
        }
        else if ((current == State.STATE_CANCELING) || (current == State.STATE_COMPENSATING))
        {
            executeFault() ;
        }
        else if (current == State.STATE_ENDED)
        {
            sendFaulted() ;
        }
    }
    
    /**
     * Handle the getStatus event.
     * @param getStatus The getStatus notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
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
     * @param status The status.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void status(final StatusType status, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        // KEV - implement
    }
    
    /**
     * Handle the get status event.
     * @return The state.
     */
    public synchronized State getStatus()
    {
        return state ;
    }
    
    /**
     * Handle the cancel event.
     * @return The state.
     */
    public State cancel()
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_ACTIVE)
            {
                changeState(State.STATE_CANCELING) ;
            }
        }
        
        if ((current == State.STATE_ACTIVE) || (current == State.STATE_CANCELING))
        {
            sendCancel() ;
        }
        
        return waitForState(State.STATE_CANCELING, TransportTimer.getTransportTimeout()) ;
    }
    
    /**
     * Handle the compensate event.
     * @return The state.
     */
    public State compensate()
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_COMPLETED)
            {
                changeState(State.STATE_COMPENSATING) ;
            }
        }
        
        if ((current == State.STATE_COMPLETED) || (current == State.STATE_COMPENSATING))
        {
            sendCompensate() ;
        }
        
        return waitForState(State.STATE_COMPENSATING, TransportTimer.getTransportTimeout()) ;
    }
    
    /**
     * Handle the close event.
     * @return The state.
     */
    public State close()
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_COMPLETED)
            {
                changeState(State.STATE_CLOSING) ;
            }
        }
        
        if ((current == State.STATE_COMPLETED) || (current == State.STATE_CLOSING))
        {
            sendClose() ;
        }
        
        waitForState(State.STATE_CLOSING, TransportTimer.getTransportTimeout()) ;
        synchronized(this)
        {
            if (state != State.STATE_CLOSING)
            {
                // if this is a recovered participant then forget will not have
                // deactivated the entry so that this (recovery) thread can
                // detect it and update its log entry. so we need to deactivate
                // the entry here.

                if (recovered) {
                    ParticipantCompletionCoordinatorProcessor.getProcessor().deactivateCoordinator(this) ;
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

            return State.STATE_CLOSING;
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
	ended() ;
	try
	{
	    coordinator.fault() ;
	}
	catch (final Throwable th) {} // ignore
    }
    
    /**
     * Send the close message.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.sendClose_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.sendClose_1] - Unexpected exception while sending Close
     */
    private void sendClose()
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendClose(addressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.sendClose_1", th) ;
            }
        }
    }
    
    /**
     * Send the compensate message.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.sendCompensate_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.sendCompensate_1] - Unexpected exception while sending Compensate
     */
    private void sendCompensate()
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendCompensate(addressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.sendCompensate_1", th) ;
            }
        }
    }
    
    /**
     * Send the cancel message.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.sendCancel_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.sendCancel_1] - Unexpected exception while sending Cancel
     */
    private void sendCancel()
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendCancel(addressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.sendCancel_1", th) ;
            }
        }
    }
    
    /**
     * Send the exited message.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.sendExited_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.sendExited_1] - Unexpected exception while sending Exited
     */
    private void sendExited()
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendExited(addressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.sendExited_1", th) ;
            }
        }
    }
    
    /**
     * Send the faulted message.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.sendFaulted_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.sendFaulted_1] - Unexpected exception while sending Faulted
     */
    private void sendFaulted()
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendFaulted(addressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.sendFaulted_1", th) ;
            }
        }
    }
    
    /**
     * Send the status message.
     * @param state The state.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.sendStatus_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.sendStatus_1] - Unexpected exception while sending Status
     */
    private void sendStatus(final State state)
    {
        final AddressingContext addressingContext = createContext() ;
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendStatus(addressingContext, instanceIdentifier, state) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.sendStatus_1", th) ;
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
     * Get the participant endpoint reference
     * @return The participant endpoint reference
     */
    public EndpointReferenceType getParticipant()
    {
        return participant ;
    }
    
    /**
     * Get the associated coordinator.
     * @return The associated coordinator.
     */
    public BAParticipantManager getCoordinator()
    {
        return coordinator ;
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
     * Execute the completed transition.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.executeCompleted_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.executeCompleted_1] - Unexpected exception from coordinator completed
     */
    private void executeCompleted()
    {
        try
        {
            coordinator.completed() ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.executeCompleted_1", th) ;
            }
        }
    }
    
    /**
     * Execute the exit transition.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.executeExit_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.executeExit_1] - Unexpected exception from coordinator exit
     */
    private void executeExit()
    {
        try
        {
            coordinator.exit() ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.executeExit_1", th) ;
            }
            return ;
        }
        sendExited() ;
        ended() ;
    }
    
    /**
     * Executing the fault transition.
     * 
     * @throws SoapFault for SOAP errors.
     * @throws IOException for transport errors.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.executeFault_1 [com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.executeFault_1] - Unexpected exception from coordinator fault
     */
    private void executeFault()
    {
        try
        {
            coordinator.fault() ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine.executeFault_1", th) ;
            }
            return ;
        }
        sendFaulted() ;
        ended() ;
    }
    
    /**
     * End the current coordinator.
     */
    private void ended()
    {
        changeState(State.STATE_ENDED) ;
        // participants which have not been recovered from the log can be deactivated now.

        // participants which have been recovered are left for the recovery thread to deactivate.
        // this is because the recovery thread may have timed out waiting for a response to
        // the commit message and gone on to complete its scan and suspend. the next scan
        // will detect this activated participant and note that it has completed. if a crash
        // happens in between the recovery thread can safely recreate and reactivate the
        // participant and resend the commit since the commit/committed exchange is idempotent.

        if (!recovered) {
            ParticipantCompletionCoordinatorProcessor.getProcessor().deactivateCoordinator(this) ;
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
