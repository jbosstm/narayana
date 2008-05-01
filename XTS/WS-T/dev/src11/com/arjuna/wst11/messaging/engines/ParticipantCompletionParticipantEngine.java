/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsba.ParticipantCompletionParticipantInboundEvents;
import com.arjuna.webservices11.wsba.State;
import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.webservices11.wsba.client.ParticipantCompletionCoordinatorClient;
import com.arjuna.webservices11.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.FaultedException;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

import javax.xml.namespace.QName;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.util.TimerTask;

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
    private final W3CEndpointReference coordinator ;
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
     * Construct the initial engine for the participant.
     * @param id The participant id.
     * @param coordinator The coordinator endpoint reference.
     * @param participant The participant.
     */
    public ParticipantCompletionParticipantEngine(final String id, final W3CEndpointReference coordinator,
        final BusinessAgreementWithParticipantCompletionParticipant participant)
    {
        this(id, coordinator, participant, State.STATE_ACTIVE) ;
    }

    /**
     * Construct the engine for the participant in a specified state.
     * @param id The participant id.
     * @param coordinator The coordinator endpoint reference.
     * @param participant The participant.
     * @param state The initial state.
     */
    public ParticipantCompletionParticipantEngine(final String id, final W3CEndpointReference coordinator,
        final BusinessAgreementWithParticipantCompletionParticipant participant, final State state)
    {
        this.id = id ;
        this.instanceIdentifier = new InstanceIdentifier(id) ;
        this.coordinator = coordinator ;
        this.participant = participant ;
        this.state = state ;
    }

    /**
     * Handle the cancel event.
     * @param cancel The cancel notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Canceling
     * Canceling -> Canceling
     * Completed -> Completed (resend Completed)
     * Closing -> Closing
     * Compensating -> Compensating
     * Failing-Active -> Failing-Active (resend Fail)
     * Failing-Canceling -> Failing-Canceling (resend Fail)
     * Failing-Compensating -> Failing-Compensating
     * NotCompleting -> NotCompleting (resend CannotComplete)
     * Exiting -> Exiting (resend Exit)
     * Ended -> Ended (resend Cancelled)
     */
    public void cancel(final NotificationType cancel, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
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

        if (current == State.STATE_ACTIVE)
        {
            executeCancel() ;
        }
        else if (current == State.STATE_COMPLETED)
        {
            sendCompleted() ;
        }
        else if ((current == State.STATE_FAILING_ACTIVE) || (current == State.STATE_FAILING_CANCELING))
        {
            sendFail(current.getValue()) ;
        }
        else if (current == State.STATE_NOT_COMPLETING)
        {
            sendCannotComplete() ;
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
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
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
     * Ended -> Ended (send Closed)
     */
    public void close(final NotificationType close, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
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
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completed -> Compensating
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Compensating -> Failing-Compensating (resend Fail)
     * NotCompleting -> NotCompleting (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended (send Compensated)
     */
    public void compensate(final NotificationType compensate, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
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

        if (current == State.STATE_COMPLETED)
        {
            if (timerTask != null)
            {
                timerTask.cancel() ;
            }
            executeCompensate() ;
        }
        else if (current == State.STATE_FAILING_COMPENSATING)
        {
            sendFail(current.getValue()) ;
        }
        else if (current == State.STATE_ENDED)
        {
            sendCompensated() ;
        }
    }

    /**
     * Handle the exited event.
     * @param exited The exited notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * NotCompleting -> NotCompleting (invalid state)
     * Exiting -> Ended
     * Ended -> Ended
     */
    public void exited(final NotificationType exited, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_EXITING)
            {
                ended() ;
            }
        }
    }

    /**
     * Handle the failed event.
     * @param failed The failed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Ended
     * Failing-Canceling -> Ended
     * Failing-Compensating -> Ended
     * NotCompleting -> NotCompleting (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended
     */
    public void failed(final NotificationType failed,  final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if ((current == State.STATE_FAILING_ACTIVE) || (current == State.STATE_FAILING_CANCELING) ||
                (current == State.STATE_FAILING_COMPENSATING))
            {
                ended() ;
            }
        }
    }

    /**
     * Handle the not completed event.
     * @param notCompleted The notCompleted notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * NotCompleting -> Ended
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended
     */
    public void notCompleted(final NotificationType notCompleted, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_NOT_COMPLETING)
            {
        	ended() ;
            }
        }
    }

    /**
     * Handle the getStatus event.
     * @param getStatus The getStatus notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.getStatus_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.getStatus_1] - Unknown error: {0}
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
     * @param status The status type.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void status(final StatusType status, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        // KEV - implement
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
	    participant.error() ;
	}
	catch (final Throwable th) {} // ignore
    }

    /**
     * Handle the completed event.
     *
     * Active -> Completed
     * Canceling -> Canceling (invalid state)
     * Completed -> Completed
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * NotCompleting -> NotCompleting (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended (invalid state)
     */
    public State completed()
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_ACTIVE)
            {
                changeState(State.STATE_COMPLETED) ;
            }
        }

        if ((current == State.STATE_ACTIVE) || (current == State.STATE_COMPLETED))
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
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * NotCompleting -> NotCompleting (invalid state)
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
     * Handle the fail event.
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
     * Ended -> Ended (invalid state)
     */
    public State fail(final QName exceptionIdentifier)
    {
        final State current ;
        synchronized (this)
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

        if ((current == State.STATE_ACTIVE) || (current == State.STATE_FAILING_ACTIVE))
        {
            sendFail(exceptionIdentifier) ;
            return waitForState(State.STATE_FAILING_ACTIVE, TransportTimer.getTransportTimeout()) ;
        }
        else if ((current == State.STATE_CANCELING) || (current == State.STATE_FAILING_CANCELING))
        {
            sendFail(exceptionIdentifier) ;
            return waitForState(State.STATE_FAILING_CANCELING, TransportTimer.getTransportTimeout()) ;
        }
        else if ((current == State.STATE_COMPENSATING) || (current == State.STATE_FAILING_COMPENSATING))
        {
            sendFail(exceptionIdentifier) ;
            return waitForState(State.STATE_FAILING_COMPENSATING, TransportTimer.getTransportTimeout()) ;
        }

        return current ;
    }

    /**
     * Handle the cannot complete event.
     *
     * Active -> NotCompleting
     * Canceling -> Canceling (invalid state)
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * NotCompleting -> NotCompleting
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended (invalid state)
     */
    public State cannotComplete()
    {
        final State current ;
        synchronized (this)
        {
            current = state ;
            if (current == State.STATE_ACTIVE)
            {
                changeState(State.STATE_NOT_COMPLETING) ;
            }
        }

        if ((current == State.STATE_ACTIVE) || (current == State.STATE_NOT_COMPLETING))
        {
            sendCannotComplete() ;
            return waitForState(State.STATE_NOT_COMPLETING, TransportTimer.getTransportTimeout()) ;
        }
        return current ;
    }

    /**
     * Handle the comms timeout event.
     *
     * Completed -> Completed (resend Completed)
     */
    private void commsTimeout()
    {
        final State current ;
        synchronized(this)
        {
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
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendExit_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendExit_1] - Unexpected exception while sending Exit
     */
    private void sendExit()
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendExit(coordinator, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendExit_1", th) ;
            }
        }
    }

    /**
     * Send the completed message.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendCompleted_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendCompleted_1] - Unexpected exception while sending Completed
     */
    private void sendCompleted()
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendCompleted(coordinator, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendCompleted_1", th) ;
            }
        }

        initiateTimer() ;
    }

    /**
     * Send the fail message.
     * @param message The fail message.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendFail_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendFail_1] - Unexpected exception while sending Fault
     */
    private void sendFail(final QName message)
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendFail(coordinator, addressingProperties, instanceIdentifier, message) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendFail_1", th) ;
            }
        }
    }

    /**
     * Send the cancelled message.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendCancelled_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendCancelled_1] - Unexpected exception while sending Cancelled
     */
    private void sendCancelled()
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendCancelled(coordinator, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendCancelled_1", th) ;
            }
        }
    }

    /**
     * Send the closed message.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendClosed_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendClosed_1] - Unexpected exception while sending Closed
     */
    private void sendClosed()
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendClosed(coordinator, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendClosed_1", th) ;
            }
        }
    }

    /**
     * Send the compensated message.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendCompensated_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendCompensated_1] - Unexpected exception while sending Compensated
     */
    private void sendCompensated()
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendCompensated(coordinator, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendCompensated_1", th) ;
            }
        }
    }

    /**
     * Send the status message.
     * @param state The state.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendStatus_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendStatus_1] - Unexpected exception while sending Status
     */
    private void sendStatus(final State state)
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendStatus(coordinator, addressingProperties, instanceIdentifier, state.getValue()) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendStatus_1", th) ;
            }
        }
    }

    /**
     * Send the cannot complete message.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendCannotComplete_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendCannotComplete_1] - Unexpected exception while sending Status
     */
    private void sendCannotComplete()
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendCannotComplete(coordinator, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.sendCannotComplete_1", th) ;
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
    public W3CEndpointReference getCoordinator()
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
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.executeCancel_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.executeCancel_1] - Unexpected exception from participant cancel
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
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.executeCancel_1", th) ;
            }
            return ;
        }
        sendCancelled() ;
        ended() ;
    }

    /**
     * Execute the close transition.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.executeClose_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.executeClose_1] - Unexpected exception from participant close
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
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.executeClose_1", th) ;
            }
            return ;
        }
        sendClosed() ;
        ended() ;
    }

    /**
     * Execute the compensate transition.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.executeCompensate_1 [com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.executeCompensate_1] - Unexpected exception from participant compensate
     */
    private void executeCompensate()
    {
        try
        {
            participant.compensate() ;
        }
        catch (final FaultedException fe)
        {
            // TODO - use the right value here -- shoudl be a QName for Faulted (Kev's 1.1 code uses JBOSSTX.FAULTED_ERROR_CODE_QNAME)
            fail(ArjunaTXConstants.WSARJTX_ELEMENT_FAULTED_QNAME);
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
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine.executeClose_1", th) ;
            }
            return ;
        }

        final State current ;
        synchronized (this)
        {
            current = state ;
            if (current == State.STATE_COMPENSATING)
            {
                ended() ;
            }
        }
        if (current == State.STATE_COMPENSATING)
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
                    commsTimeout() ;
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
    private AddressingProperties createContext()
    {
        final String messageId = MessageId.getMessageId() ;

        return AddressingHelper.createNotificationContext(messageId) ;
    }
}