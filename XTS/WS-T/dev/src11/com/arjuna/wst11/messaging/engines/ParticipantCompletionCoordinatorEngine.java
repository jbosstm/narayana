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
package com.arjuna.wst11.messaging.engines;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.util.TransportTimer;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsba.ParticipantCompletionCoordinatorInboundEvents;
import com.arjuna.webservices11.wsba.State;
import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.webservices11.wsba.processors.ParticipantCompletionCoordinatorProcessor;
import com.arjuna.webservices11.wsba.client.ParticipantCompletionParticipantClient;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.wst11.BAParticipantManager;
import org.oasis_open.docs.ws_tx.wsba._2006._06.ExceptionType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

import javax.xml.namespace.QName;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

/**
 * The participant completion coordinator state engine
 * @author kevin                     cannot
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
    private final W3CEndpointReference participant ;
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
    public ParticipantCompletionCoordinatorEngine(final String id, final W3CEndpointReference participant)
    {
        this(id, participant, State.STATE_ACTIVE, false) ;
    }

    /**
     * Construct the engine for the coordinator in a specified state.
     * @param id The coordinator id.
     * @param participant The participant endpoint reference.
     * @param state The initial state.
     */
    public ParticipantCompletionCoordinatorEngine(final String id, final W3CEndpointReference participant,
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
        // them since we implement presumed abort.
        if (!recovered || state == State.STATE_COMPLETED || state == State.STATE_CLOSING) {
            ParticipantCompletionCoordinatorProcessor.getProcessor().activateCoordinator(this, id) ;
        }
    }

    /**
     * Handle the cancelled event.
     * @param cancelled The cancelled notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Active (invalid state)
     * Canceling -> Ended
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * NotCompleting -> NotCompleting (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended
     */
    public void cancelled(final NotificationType cancelled, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_CANCELING)
            {
                ended() ;
            }
        }
    }

    /**
     * Handle the closed event.
     * @param closed The closed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completed -> Completed (invalid state)
     * Closing -> Ended
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * NotCompleting -> NotCompleting (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended
     */
    public void closed(final NotificationType closed, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_CLOSING)
            {
                ended() ;
            }
        }
    }

    /**
     * Handle the compensated event.
     * @param compensated The compensated notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Ended
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * NotCompleting -> NotCompleting (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended
     */
    public void compensated(final NotificationType compensated, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_COMPENSATING)
            {
                ended() ;
            }
        }
    }

    /**
     * Handle the completed event.
     * @param completed The completed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Completed
     * Canceling -> Compensating
     * Completed -> Completed
     * Closing -> Closing (resend Close)
     * Compensating -> (resend Compensate)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Compensating -> Failing-Compensating
     * NotCompleting -> NotCompleting (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended
     */
    public void completed(final NotificationType completed, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_ACTIVE)
            {
                changeState(State.STATE_COMPLETED) ;
            }
            else if (current == State.STATE_CANCELING)
            {
        	state = State.STATE_COMPENSATING ;
            }
        }

        if (current == State.STATE_ACTIVE)
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
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Exiting
     * Canceling -> Exiting
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * NotCompleting -> NotCompleting (invalid state)
     * Exiting -> Exiting
     * Ended -> Ended (resend Exited)
     */
    public void exit(final NotificationType exit, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if ((current == State.STATE_ACTIVE) || (current == State.STATE_CANCELING))
            {
                changeState(State.STATE_EXITING) ;
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
     * Handle the fail event.
     * @param fail The fail exception.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Failing-Active
     * Canceling -> Failing-Canceling
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Failing-Compensating
     * Failing-Active -> Failing-Active
     * Failing-Canceling -> Failing-Canceling
     * Failing-Compensating -> Failing-Compensating
     * NotCompleting -> NotCompleting (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended (resend Failed)
     */
    public void fail(final ExceptionType fail, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_ACTIVE)
            {
        	changeState(State.STATE_FAILING_ACTIVE) ;
            }
            else if (current == State.STATE_CANCELING)
            {
                changeState(State.STATE_FAILING_CANCELING) ;
            }
            else if (current == State.STATE_COMPENSATING)
            {
                changeState(State.STATE_FAILING_COMPENSATING) ;
            }
        }

        if (current == State.STATE_ACTIVE)
        {
            executeFail(fail.getExceptionIdentifier()) ;
        }
        else if ((current == State.STATE_CANCELING) || (current == State.STATE_COMPENSATING))
        {
            sendFailed() ;
            ended() ;
        }
        else if (current == State.STATE_ENDED)
        {
            sendFailed() ;
        }
    }

    /**
     * Handle the cannot complete event.
     * @param cannotComplete The cannotComplete exception.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> NotCompleting
     * Canceling -> NotCompleting
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * NotCompleting -> NotCompleting
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended (resend NotCompleted)
     */
    public void cannotComplete(final NotificationType cannotComplete, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if ((current == State.STATE_ACTIVE) || (state == State.STATE_CANCELING))
            {
                changeState(State.STATE_NOT_COMPLETING) ;
            }
        }

        if ((current == State.STATE_ACTIVE) || (current == State.STATE_CANCELING))
        {
            executeCannotComplete() ;
        }
        else if (current == State.STATE_ENDED)
        {
            sendNotCompleted() ;
        }
    }
    /**
     * Handle the getStatus event.
     * @param getStatus The getStatus notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void getStatus(final NotificationType getStatus, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
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
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void status(final StatusType status, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        // TODO - is this correct?

        final State current ;
	    synchronized(this)
	    {
	        current = state ;
	    }
        sendStatus(current) ;
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
     *
     * Active -> Canceling
     * Canceling -> Canceling
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * NotCompleting -> NotCompleting (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended (invalid state)
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
            return waitForState(State.STATE_CANCELING, TransportTimer.getTransportTimeout()) ;
        }
        return current ;
    }

    /**
     * Handle the compensate event.
     * @return The state.
     *
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completed -> Compensating
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * NotCompleting -> NotCompleting (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended (invalid state)
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
            return waitForState(State.STATE_COMPENSATING, TransportTimer.getTransportTimeout()) ;
        }
        return current ;
    }

    /**
     * Handle the close event.
     * @return The state.
     *
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completed -> Closing
     * Closing -> Closing
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * NotCompleting -> NotCompleting (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended (invalid state)
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
            return waitForState(State.STATE_CLOSING, TransportTimer.getTransportTimeout()) ;
        }
        return current ;
    }

    /**
     * Handle the soap fault event.
     * @param soapFault The soap fault.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void soapFault(final SoapFault soapFault, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
	ended() ;
	try
	{
	    // TODO - we cannot do this with JaxWS. need to log something
	}
	catch (final Throwable th) {} // ignore
    }

    /**
     * Send the close message.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendClose_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendClose_1] - Unexpected exception while sending Close
     */
    private void sendClose()
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendClose(participant, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendClose_1", th) ;
            }
        }
    }

    /**
     * Send the compensate message.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendCompensate_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendCompensate_1] - Unexpected exception while sending Compensate
     */
    private void sendCompensate()
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendCompensate(participant, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendCompensate_1", th) ;
            }
        }
    }

    /**
     * Send the cancel message.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendCancel_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendCancel_1] - Unexpected exception while sending Cancel
     */
    private void sendCancel()
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendCancel(participant, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendCancel_1", th) ;
            }
        }
    }

    /**
     * Send the exited message.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendExited_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendExited_1] - Unexpected exception while sending Exited
     */
    private void sendExited()
    {
        final AddressingProperties addressingProperties  = createContext() ;
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendExited(participant, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendExited_1", th) ;
            }
        }
    }

    /**
     * Send the faulted message.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendFailed_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendFailed_1] - Unexpected exception while sending Faulted
     */
    private void sendFailed()
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendFailed(participant, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendFailed_1", th) ;
            }
        }
    }

    /**
     * Send the not completed message.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendNotCompleted_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendNotCompleted_1] - Unexpected exception while sending NotCompleted
     */
    private void sendNotCompleted()
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendNotCompleted(participant, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendNotCompleted_1", th) ;
            }
        }
    }

    /**
     * Send the status message.
     * @param state The state.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendStatus_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendStatus_1] - Unexpected exception while sending Status
     */
    private void sendStatus(final State state)
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendStatus(participant, addressingProperties, instanceIdentifier, state.getValue()) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.sendStatus_1", th) ;
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
    public W3CEndpointReference getParticipant()
    {
        return participant ;
    }

    /**
     * Get the associated coordinator.
     * @return The associated coordinator.
     */
    public BAParticipantManager getCoordinator()
    {
        return coordinator;
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
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.executeCompleted_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.executeCompleted_1] - Unexpected exception from coordinator completed
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
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.executeCompleted_1", th) ;
            }
        }
    }

    /**
     * Execute the exit transition.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.executeExit_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.executeExit_1] - Unexpected exception from coordinator exit
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
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.executeExit_1", th) ;
            }
            return ;
        }
        sendExited() ;
        ended() ;
    }

    /**
     * Executing the fail transition.
     *
     * @throws com.arjuna.webservices.SoapFault for SOAP errors.
     * @throws java.io.IOException for transport errors.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.executeFault_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.executeFault_1] - Unexpected exception from coordinator fault
     */
    private void executeFail(QName fail)
    {
        try
        {
            coordinator.fail(fail) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.executeFault_1", th) ;
            }
            return ;
        }
        sendFailed() ;
        ended() ;
    }

    /**
     * Executing the cannot complete transition.
     *
     * @throws SoapFault for SOAP errors.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.executeCannotComplete_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.executeCannotComplete_1] - Unexpected exception from coordinator error
     */
    private void executeCannotComplete()
    {
        try
        {
            coordinator.cannotComplete() ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine.executeCannotComplete_1", th) ;
            }
            return ;
        }
        sendNotCompleted() ;
        ended() ;
    }
    /**
     * End the current coordinator.
     */
    private void ended()
    {
        changeState(State.STATE_ENDED) ;
        ParticipantCompletionCoordinatorProcessor.getProcessor().deactivateCoordinator(this) ;
    }

    /**
     * Create a context for the outgoing message.
     * @return The addressing context.
     */
    private AddressingProperties createContext()
    {
        final String messageId = MessageId.getMessageId() ;

        return AddressingHelper.createNotificationContext(messageId);
    }
}