/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsat.NotificationType;
import com.arjuna.webservices.wsat.Participant;
import com.arjuna.webservices.wsat.ParticipantInboundEvents;
import com.arjuna.webservices.wsat.State;
import com.arjuna.webservices.wsat.client.CoordinatorClient;
import com.arjuna.webservices.wsat.processors.ParticipantProcessor;
import com.arjuna.wsc.messaging.MessageId;
import com.arjuna.wst.Aborted;
import com.arjuna.wst.Prepared;
import com.arjuna.wst.ReadOnly;
import com.arjuna.wst.Vote;

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
     * The current state.
     */
    private State state ;
    /**
     * The associated timer task or null.
     */
    private TimerTask timerTask ;
    
    /**
     * Construct the initial engine for the participant.
     * @param participant The participant.
     */
    public ParticipantEngine(final Participant participant)
    {
        this(participant, State.STATE_ACTIVE) ;
    }
    
    /**
     * Construct the engine for the participant in a specified state.
     * @param participant The participant.
     * @param state The initial state.
     */
    public ParticipantEngine(final Participant participant, final State state)
    {
        this.participant = participant ;
        this.state = state ;
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
            }
            else if ((current == State.STATE_ACTIVE) || (current == State.STATE_PREPARING))
            {
                state = State.STATE_ABORTING ;
            }
        }
        
        if (current == State.STATE_PREPARED_SUCCESS)
        {
            executeCommit(addressingContext, arjunaContext) ;
        }
        else if (current == null)
        {
            sendCommitted(addressingContext, arjunaContext) ;
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
            }
        }
        
        if (current == State.STATE_ACTIVE)
        {
            executePrepare(addressingContext, arjunaContext) ;
        }
        else if (current == State.STATE_PREPARED_SUCCESS)
        {
            sendPrepared(addressingContext, arjunaContext) ;
        }
        else if ((current == State.STATE_ABORTING) || (current == null))
        {
            sendAborted(addressingContext, arjunaContext) ;
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
                executeRollback(addressingContext, arjunaContext) ;
            }
            
            sendAborted(addressingContext, arjunaContext) ;
            
            if (current != null)
            {
                forget() ;
            }
        }
    }
    
    /**
     * Handle the soap fault event.
     * @param soapFault The soap fault.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantEngine.soapFault_1 [com.arjuna.wst.messaging.engines.ParticipantEngine.soapFault_1] - Unexpected SOAP fault for participant {0}: {1} {2}
     */
    public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
            final SoapFaultType soapFaultType = soapFault.getSoapFaultType() ;
            final QName subCode = soapFault.getSubcode() ;
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantEngine.soapFault_1", new Object[] {instanceIdentifier, soapFaultType, subCode}) ;
        }
    }
    
    /**
     * Handle the commit decision event.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * Preparing -> PreparedSuccess (send Prepared)
     * Committing -> Committing (send committed and forget)
     */
    private void commitDecision(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current ;
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
            sendPrepared(addressingContext, arjunaContext) ;
        }
        else if (current == State.STATE_COMMITTING)
        {
            sendCommitted(addressingContext, arjunaContext) ;
            forget() ;
        }
    }
    
    /**
     * Handle the readOnly decision event.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * Preparing -> None (send ReadOnly)
     */
    private void readOnlyDecision(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
        }
        
        if (current == State.STATE_PREPARING)
        {
            sendReadOnly(addressingContext, arjunaContext) ;
            forget() ;
        }
    }
    
    /**
     * Handle the rollback decision event.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * Preparing -> Aborting (send aborted)
     */
    private void rollbackDecision(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_PREPARING)
            {
                state = State.STATE_ABORTING ;
            }
        }
        
        if (current == State.STATE_PREPARING)
        {
            sendAborted(addressingContext, arjunaContext) ;
            forget() ;
        }
    }
    
    /**
     * Handle the comms timeout event.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * PreparedSuccess -> PreparedSuccess (resend Prepared)
     */
    private void commsTimeout(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
        }
        
        if (current == State.STATE_PREPARING)
        {
            sendPrepared(addressingContext, arjunaContext) ;
        }
    }
    
    /**
     * Execute the commit transition.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantEngine.executeCommit_1 [com.arjuna.wst.messaging.engines.ParticipantEngine.executeCommit_1] - Unexpected exception from participant commit
     */
    private void executeCommit(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        try
        {
            participant.commit() ;
            commitDecision(addressingContext, arjunaContext) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantEngine.executeCommit_1", th) ;
            }
        }
    }
    
    /**
     * Execute the rollback transition.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantEngine.executeRollback_1 [com.arjuna.wst.messaging.engines.ParticipantEngine.executeRollback_1] - Unexpected exception from participant rollback
     */
    private void executeRollback(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        try
        {
            participant.rollback() ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantEngine.executeRollback_1", th) ;
            }
        }
    }
    
    /**
     * Execute the prepare transition.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantEngine.executePrepare_1 [com.arjuna.wst.messaging.engines.ParticipantEngine.executePrepare_1] - Unexpected exception from participant prepare
     * @message com.arjuna.wst.messaging.engines.ParticipantEngine.executePrepare_2 [com.arjuna.wst.messaging.engines.ParticipantEngine.executePrepare_2] - Unexpected result from participant prepare: {0}
     */
    private void executePrepare(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final Vote vote ;
        try
        {
            vote = participant.prepare();
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantEngine.executePrepare_1", th) ;
            }
            rollbackDecision(addressingContext, arjunaContext) ;
            return ;
        }
        
        if (vote instanceof Prepared)
        {
            commitDecision(addressingContext, arjunaContext) ;
        }
        else if (vote instanceof ReadOnly)
        {
            readOnlyDecision(addressingContext, arjunaContext) ;
        }
        else if (vote instanceof Aborted)
        {
            rollbackDecision(addressingContext, arjunaContext) ;
        }
        else
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantEngine.executePrepare_2", new Object[] {(vote == null ? "null" : vote.getClass().getName())}) ;
            }
            rollbackDecision(addressingContext, arjunaContext) ;
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
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantEngine.sendCommitted_1 [com.arjuna.wst.messaging.engines.ParticipantEngine.sendCommitted_1] - Unexpected exception while sending Committed
     */
    private void sendCommitted(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final AddressingContext responseAddressingContext = createResponseContext(addressingContext) ;
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        try
        {
            CoordinatorClient.getClient().sendCommitted(responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantEngine.sendCommitted_1", th) ;
            }
        }
    }
    
    /**
     * Send the prepared message.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantEngine.sendPrepared_1 [com.arjuna.wst.messaging.engines.ParticipantEngine.sendPrepared_1] - Unexpected exception while sending Prepared
     */
    private void sendPrepared(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final AddressingContext responseAddressingContext = createResponseContext(addressingContext) ;
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        try
        {
            CoordinatorClient.getClient().sendPrepared(responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantEngine.sendPrepared_1", th) ;
            }
        }
        
        initiateTimer(addressingContext, arjunaContext) ;
    }
    
    /**
     * Send the aborted message.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantEngine.sendAborted_1 [com.arjuna.wst.messaging.engines.ParticipantEngine.sendAborted_1] - Unexpected exception while sending Aborted
     */
    private void sendAborted(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final AddressingContext responseAddressingContext = createResponseContext(addressingContext) ;
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        try
        {
            CoordinatorClient.getClient().sendAborted(responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantEngine.sendAborted_1", th) ;
            }
        }
    }
    
    /**
     * Send the read only message.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * 
     * @message com.arjuna.wst.messaging.engines.ParticipantEngine.sendReadOnly_1 [com.arjuna.wst.messaging.engines.ParticipantEngine.sendReadOnly_1] - Unexpected exception while sending ReadOnly
     */
    private void sendReadOnly(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final AddressingContext responseAddressingContext = createResponseContext(addressingContext) ;
        final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
        try
        {
            CoordinatorClient.getClient().sendReadOnly(responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst.messaging.engines.ParticipantEngine.sendReadOnly_1", th) ;
            }
        }
    }
    
    /**
     * Initiate the timer.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    private synchronized void initiateTimer(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        if (timerTask != null)
        {
            timerTask.cancel() ;
        }
        if (state == State.STATE_PREPARING)
        {
            timerTask = new TimerTask() {
                public void run() {
                    commsTimeout(addressingContext, arjunaContext) ;
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
     * @param addressingContext The incoming addressing context.
     * @return The response addressing context.
     */
    private AddressingContext createResponseContext(final AddressingContext addressingContext)
    {
        final String messageId = MessageId.getMessageId() ;
        return AddressingContext.createNotificationContext(addressingContext, messageId) ;
    }
}
