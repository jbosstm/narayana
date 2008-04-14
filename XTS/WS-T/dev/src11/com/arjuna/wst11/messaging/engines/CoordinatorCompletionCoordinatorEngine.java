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
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsba.CoordinatorCompletionCoordinatorInboundEvents;
import com.arjuna.webservices11.wsba.State;
import com.arjuna.webservices11.wsba.processors.CoordinatorCompletionCoordinatorProcessor;
import com.arjuna.webservices11.wsba.client.CoordinatorCompletionParticipantClient;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.wst11.BAParticipantManager;
import org.oasis_open.docs.ws_tx.wsba._2006._06.ExceptionType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

import javax.xml.namespace.QName;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

/**
 * The coordinator completion coordinator state engine
 * @author kevin
 */
public class CoordinatorCompletionCoordinatorEngine implements CoordinatorCompletionCoordinatorInboundEvents
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
    private State state;

    /**
     * Construct the initial engine for the coordinator.
     * @param id The coordinator id.
     * @param participant The participant endpoint reference.
     */
    public CoordinatorCompletionCoordinatorEngine(final String id, final W3CEndpointReference participant)
    {
        this(id, participant, State.STATE_ACTIVE) ;
    }

    /**
     * Construct the engine for the coordinator in a specified state.
     * @param id The coordinator id.
     * @param participant The participant endpoint reference.
     * @param state The initial state.
     */
    public CoordinatorCompletionCoordinatorEngine(final String id, final W3CEndpointReference participant,
        final State state)
    {
        this.id = id ;
        this.instanceIdentifier = new InstanceIdentifier(id) ;
        this.participant = participant ;
        this.state = state ;
    }

    /**
     * Set the coordinator and register
     * @param coordinator
     */
    public void setCoordinator(final BAParticipantManager coordinator)
    {
        this.coordinator = coordinator ;
        CoordinatorCompletionCoordinatorProcessor.getProcessor().activateCoordinator(this, id) ;
    }

    /**
     * Handle the cancelled event.
     * @param cancelled The cancelled notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Active (invalid state)
     * Canceling-Active -> Ended
     * Canceling-Completing -> Ended
     * Completing -> Completing (invalid state)
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Completing -> Failing-Completing (invalid state)
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
            if ((current == State.STATE_CANCELING) || (current == State.STATE_CANCELING_ACTIVE) ||
                (current == State.STATE_CANCELING_COMPLETING))
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
     * Canceling-Active -> Canceling-Active (invalid state)
     * Canceling-Completing -> Canceling-Completing (invalid state)
     * Completing -> Completing (invalid state)
     * Completed -> Completed (invalid state)
     * Closing -> Ended
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Completing -> Failing-Completing (invalid state)
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
     * Canceling-Active -> Canceling-Active (invalid state)
     * Canceling-Completing -> Canceling-Completing (invalid state)
     * Completing -> Completing (invalid state)
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Ended
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Completing -> Failing-Completing (invalid state)
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
     * Active -> Active (invalid state)
     * Canceling-Active -> Canceling-Active (invalid state)
     * Canceling-Completing -> Canceling-Completing (invalid state)
     * Completing -> Completing (invalid state)
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Ended
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Completing -> Failing-Completing (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
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
            if ((current == State.STATE_CANCELING_COMPLETING) || (current == State.STATE_COMPLETING))
            {
                changeState(State.STATE_COMPLETED) ;
            }
            else if (current == State.STATE_ACTIVE)
            {
        	ended() ;
            }
        }

        if (current == State.STATE_CLOSING)
        {
            sendClose() ;
        }
        else if (current == State.STATE_COMPENSATING)
        {
            sendCompensate() ;
        }
        else if (current == State.STATE_ACTIVE)
        {
            // TODO - we cannot send a fault here
            // sendInvalidStateFault() ;
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
     * Canceling-Active -> Exiting
     * Canceling-Completing -> Exiting
     * Completing -> Exiting
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Faulting -> Faulting (invalid state)
     * Faulting-Active -> Faulting (invalid state)
     * Faulting-Compensating -> Faulting (invalid state)
     * Exiting -> Exiting
     * Ended -> Ended (resend Exited)
     */
    public void exit(final NotificationType exit, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if ((current == State.STATE_ACTIVE) || (current == State.STATE_CANCELING_ACTIVE) ||
        	(current == State.STATE_CANCELING_COMPLETING) || (current == State.STATE_COMPLETING))
            {
                changeState(State.STATE_EXITING) ;
            }
        }

        if ((current == State.STATE_ACTIVE) || (current == State.STATE_CANCELING_ACTIVE) ||
            (current == State.STATE_CANCELING_COMPLETING) || (current == State.STATE_COMPLETING))
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
     * Canceling-Active -> Failing-Canceling
     * Canceling-Completing -> Failing-Canceling
     * Completing -> Failing-Completing
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Failing-Compensating
     * Failing-Active -> Failing-Active
     * Failing-Canceling -> Failing-Canceling
     * Failing-Completing -> Failing-Completing
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
            else if ((current == State.STATE_CANCELING_ACTIVE) || (current == State.STATE_CANCELING_COMPLETING))
            {
                changeState(State.STATE_FAILING_CANCELING) ;
            }
            else if (current == State.STATE_COMPLETING)
            {
                changeState(State.STATE_FAILING_COMPLETING) ;
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
        else if ((current == State.STATE_CANCELING_ACTIVE) || (current == State.STATE_CANCELING_COMPLETING) ||
        	 (current == State.STATE_COMPLETING) || (current == State.STATE_COMPENSATING))
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
     * Active -> NotComleting
     * Canceling-Active -> NotCompleting
     * Canceling-Completing -> NotCompleting
     * Completing -> NotCompleting
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Completing -> Failing-Completing (invalid state)
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
            if ((current == State.STATE_ACTIVE) || (current == State.STATE_CANCELING_ACTIVE) ||
        	(current == State.STATE_CANCELING_COMPLETING) || (current == State.STATE_COMPLETING))
            {
                changeState(State.STATE_NOT_COMPLETING) ;
            }
        }

        if ((current == State.STATE_ACTIVE) || (current == State.STATE_CANCELING_ACTIVE) ||
            (current == State.STATE_CANCELING_COMPLETING) || (current == State.STATE_COMPLETING))
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
     *
     * Active -> Canceling-Active
     * Canceling-Active -> Canceling-Active
     * Canceling-Completing -> Canceling-Completing
     * Completing -> Canceling-Completing
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Completing -> Failing-Completing (invalid state)
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
                changeState(State.STATE_CANCELING_ACTIVE) ;
            }
            else if (current == State.STATE_COMPLETING)
            {
                changeState(State.STATE_CANCELING_COMPLETING) ;
            }
        }

        if (current == State.STATE_ACTIVE)
        {
            sendCancel() ;
            return waitForState(State.STATE_CANCELING_ACTIVE, TransportTimer.getTransportTimeout()) ;
        }
        else if (current == State.STATE_COMPLETING)
        {
            sendCancel() ;
            return waitForState(State.STATE_CANCELING_COMPLETING, TransportTimer.getTransportTimeout()) ;
        }
        return current ;
    }

    /**
     * Handle the compensate event.
     * @return The state.
     *
     * Active -> Active (invalid state)
     * Canceling-Active -> Canceling-Active (invalid state)
     * Canceling-Completing -> Canceling-Completing (invalid state)
     * Completing -> Completing (invalid state)
     * Completed -> Compensating
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Completing -> Failing-Completing (invalid state)
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

        if (current == State.STATE_COMPLETED)
        {
            sendCompensate() ;
            return waitForState(State.STATE_COMPENSATING, TransportTimer.getTransportTimeout()) ;
        }

        return current ;
    }

    /**
     * Handle the complete event.
     * @return The state.
     *
     * Active -> Completing
     * Canceling-Active -> Canceling-Active (invalid state)
     * Canceling-Completing -> Canceling-Completing (invalid state)
     * Completing -> Completing
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Completing -> Failing-Completing (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * NotCompleting -> NotCompleting (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended (invalid state)
     */
    public State complete()
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_ACTIVE)
            {
                changeState(State.STATE_COMPLETING) ;
            }
        }

        if (current == State.STATE_ACTIVE)
        {
            sendComplete() ;
            return waitForState(State.STATE_COMPLETING, TransportTimer.getTransportTimeout()) ;
        }
        return current ;
    }

    /**
     * Handle the close event.
     * @return The state.
     *
     * Active -> Active (invalid state)
     * Canceling-Active -> Canceling-Active (invalid state)
     * Canceling-Completing -> Canceling-Completing (invalid state)
     * Completing -> Completing (invalid state)
     * Completed -> Closing
     * Closing -> Closing
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Completing -> Failing-Completing (invalid state)
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

        if (current == State.STATE_COMPLETED)
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
            coordinator.fail(soapFault.getSubcode()) ;
        }
        catch (final Throwable th) {} // ignore
    }

    /**
     * Send the close message.
     *
     * @message com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendClose_1 [com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendClose_1] - Unexpected exception while sending Close
     */
    private void sendClose()
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            CoordinatorCompletionParticipantClient.getClient().sendClose(participant, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendClose_1", th) ;
            }
        }
    }

    /**
     * Send the compensate message.
     *
     * @message com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendCompensate_1 [com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendCompensate_1] - Unexpected exception while sending Compensate
     */
    private void sendCompensate()
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            CoordinatorCompletionParticipantClient.getClient().sendCompensate(participant, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendCompensate_1", th) ;
            }
        }
    }

    /**
     * Send the complete message.
     *
     * @message com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendComplete_1 [com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendComplete_1] - Unexpected exception while sending Complete
     */
    private void sendComplete()
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            CoordinatorCompletionParticipantClient.getClient().sendComplete(participant, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendComplete_1", th) ;
            }
        }
    }

    /**
     * Send the cancel message.
     *
     * @message com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendCancel_1 [com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendCancel_1] - Unexpected exception while sending Cancel
     */
    private void sendCancel()
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            CoordinatorCompletionParticipantClient.getClient().sendCancel(participant, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendCancel_1", th) ;
            }
        }
    }

    /**
     * Send the exited message.
     *
     * @message com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendExited_1 [com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendExited_1] - Unexpected exception while sending Exited
     */
    private void sendExited()
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            CoordinatorCompletionParticipantClient.getClient().sendExited(participant, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendExited_1", th) ;
            }
        }
    }

    /**
     * Send the failed message.
     *
     * @message com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendFailed_1 [com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendFailed_1] - Unexpected exception while sending Faulted
     */
    private void sendFailed()
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            CoordinatorCompletionParticipantClient.getClient().sendFailed(participant, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendFailed_1", th) ;
            }
        }
    }

    /**
     * Send the not completed message.
     *
     * @message com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendNotCompleted_1 [com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendNotCompleted_1] - Unexpected exception while sending NotCompleted
     */
    private void sendNotCompleted()
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            CoordinatorCompletionParticipantClient.getClient().sendNotCompleted(participant, addressingProperties, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendNotCompleted_1", th) ;
            }
        }
    }
    /**
     * Send the status message.
     * @param state The state.
     *
     * @message com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendStatus_1 [com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendStatus_1] - Unexpected exception while sending Status
     */
    private void sendStatus(final State state)
    {
        final AddressingProperties addressingProperties = createContext() ;
        try
        {
            CoordinatorCompletionParticipantClient.getClient().sendStatus(participant, addressingProperties, instanceIdentifier, state.getValue()) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.sendStatus_1", th) ;
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
     * @message com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.executeCompleted_1 [com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.executeCompleted_1] - Unexpected exception from coordinator completed
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
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.executeCompleted_1", th) ;
            }
        }
    }

    /**
     * Execute the exit transition.
     *
     * @message com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.executeExit_1 [com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.executeExit_1] - Unexpected exception from coordinator exit
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
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.executeExit_1", th) ;
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
     * @message com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.executeFail_1 [com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.executeFail_1] - Unexpected exception from coordinator fail
     */
    private void executeFail(final QName exceptionIdentifier)
    {
        try
        {
            coordinator.fail(exceptionIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.executeFail_1", th) ;
            }
            return ;
        }
        sendFailed() ;
        ended() ;
    }

    /**
     * Executing the cannot complete transition.
     *
     * @throws com.arjuna.webservices.SoapFault for SOAP errors.
     * @throws java.io.IOException for transport errors.
     *
     * @message com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.executeCannotComplete_1 [com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.executeCannotComplete_1] - Unexpected exception from coordinator cannotComplete
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
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine.executeCannotComplete_1", th) ;
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
        CoordinatorCompletionCoordinatorProcessor.getProcessor().deactivateCoordinator(this) ;
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