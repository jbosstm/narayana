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
package com.arjuna.wst.messaging.engines;

import java.util.TimerTask;

import javax.xml.namespace.QName;

import com.arjuna.webservices.SoapFault;
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
        this(id, durable, participant, State.STATE_ACTIVE) ;
    }
    
    /**
     * Construct the engine for the coordinator in a specified state.
     * @param id The coordinator id.
     * @param durable true if the participant is durable, false if volatile. 
     * @param participant The participant endpoint reference.
     * @param state The initial state.
     */
    public CoordinatorEngine(final String id, final boolean durable, final EndpointReferenceType participant, final State state)
    {
        this.id = id ;
        this.instanceIdentifier = new InstanceIdentifier(id) ;
        this.durable = durable ;
        this.participant = participant ;
        this.state = state ;
        CoordinatorProcessor.getProcessor().activateCoordinator(this, id) ;
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
     * @message com.arjuna.wst.messaging.engines.CoordinatorEngine.soapFault_1 [com.arjuna.wst.messaging.engines.CoordinatorEngine.soapFault_1] - Unexpected SOAP fault for coordinator {0}: {1} {2}
     */
    public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
            final SoapFaultType soapFaultType = soapFault.getSoapFaultType() ;
            final QName subCode = soapFault.getSubcode() ;
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.CoordinatorEngine.soapFault_1", new Object[] {instanceIdentifier, soapFaultType, subCode}) ;
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
        
        final State result = waitForState(State.STATE_PREPARING, TransportTimer.getTransportTimeout()) ;
        if (result != State.STATE_PREPARING)
        {
            return result ;
        }
        
        synchronized(this)
        {
            if ((state == State.STATE_PREPARING) && (timerTask != null))
            {
        	timerTask.cancel() ;
            }
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
        
        final State result = waitForState(State.STATE_COMMITTING, TransportTimer.getTransportTimeout()) ;
        if (result != State.STATE_COMMITTING)
        {
            return result ;
        }
        
        synchronized(this)
        {
            if ((state == State.STATE_COMMITTING) && (timerTask != null))
            {
        	timerTask.cancel() ;
            }
            return state ;
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
    private void commsTimeout()
    {
        final State current ;
        synchronized(this)
        {
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
        changeState(null) ;
        CoordinatorProcessor.getProcessor().deactivateCoordinator(this) ;
    }
    
    /**
     * Send the prepare message.
     * 
     * @message com.arjuna.wst.messaging.engines.CoordinatorEngine.sendPrepare_1 [com.arjuna.wst.messaging.engines.CoordinatorEngine.sendPrepare_1] - Unexpecting exception while sending Prepare
     */
    private void sendPrepare()
    {
        try
        {
            ParticipantClient.getClient().sendPrepare(createContext(), instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.CoordinatorEngine.sendPrepare_1", th) ;
            }
        }
        
        initiateTimer() ;
    }
    
    /**
     * Send the commit message.
     * 
     * @message com.arjuna.wst.messaging.engines.CoordinatorEngine.sendCommit_1 [com.arjuna.wst.messaging.engines.CoordinatorEngine.sendCommit_1] - Unexpecting exception while sending Commit
     */
    private void sendCommit()
    {
        try
        {
            ParticipantClient.getClient().sendCommit(createContext(), instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.CoordinatorEngine.sendCommit_1", th) ;
            }
        }
        
        initiateTimer() ;
    }
    
    /**
     * Send the rollback message.
     * 
     * @message com.arjuna.wst.messaging.engines.CoordinatorEngine.sendRollback_1 [com.arjuna.wst.messaging.engines.CoordinatorEngine.sendRollback_1] - Unexpecting exception while sending Rollback
     */
    private void sendRollback()
    {
        try
        {
            ParticipantClient.getClient().sendRollback(createContext(), instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.CoordinatorEngine.sendRollback_1", th) ;
            }
        }
    }
    
    /**
     * Send the InvalidState message.
     * 
     * @message com.arjuna.wst.messaging.engines.CoordinatorEngine.sendInvalidState_1 [com.arjuna.wst.messaging.engines.CoordinatorEngine.sendInvalidState_1] - Inconsistent internal state.
     * @message com.arjuna.wst.messaging.engines.CoordinatorEngine.sendInvalidState_2 [com.arjuna.wst.messaging.engines.CoordinatorEngine.sendInvalidState_2] - Unexpecting exception while sending InvalidState
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
            
            final String message = WSTLogger.log_mesg.getString("com.arjuna.wst.messaging.engines.CoordinatorEngine.sendInvalidState_1") ;
            final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME, message) ;
            ParticipantClient.getClient().sendSoapFault(responseContext, soapFault, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.CoordinatorEngine.sendInvalidState_2", th) ;
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
        if ((state == State.STATE_PREPARING) || (state == State.STATE_COMMITTING))
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
    private AddressingContext createContext()
    {
        final String messageId = MessageId.getMessageId() ;
        return AddressingContext.createRequestContext(participant, messageId) ;
    }
}
