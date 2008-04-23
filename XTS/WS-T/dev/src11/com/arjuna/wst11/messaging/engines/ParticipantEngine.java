package com.arjuna.wst11.messaging.engines;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.util.TransportTimer;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.wsat.client.CoordinatorClient;
import com.arjuna.webservices11.wsat.ParticipantInboundEvents;
import com.arjuna.webservices11.wsat.State;
import com.arjuna.webservices11.wsat.processors.ParticipantProcessor;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.wst.*;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

import javax.xml.namespace.QName;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.util.TimerTask;

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
    private final W3CEndpointReference coordinator ;
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
     * @param id The participant id.
     * @param coordinator The coordinator endpoint reference.
     */
    public ParticipantEngine(final Participant participant, final String id, final W3CEndpointReference coordinator)
    {
        this(participant, id, State.STATE_ACTIVE, coordinator) ;
    }

    /**
     * Construct the engine for the participant in a specified state.
     * @param participant The participant.
     * @param id The participant id.
     * @param state The initial state.
     * @param coordinator The coordinator endpoint reference.
     */
    public ParticipantEngine(final Participant participant, final String id, final State state, final W3CEndpointReference coordinator)
    {
        this.participant = participant ;
        this.id = id ;
        this.state = state ;
        this.coordinator = coordinator ;
    }

    /**
     * Handle the commit event.
     * @param commit The commit notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * None -> None (send committed)
     * Active -> Aborting (do nothing)
     * Preparing -> Aborting (do nothing)
     * PreparedSuccess -> Committing (initiate commit)
     * Committing -> Committing (do nothing)
     * Aborting -> Aborting (do nothing)
     */
    public void commit(final Notification commit, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
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
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * None -> None (send aborted)
     * Active -> Preparing (execute prepare)
     * Preparing -> Preparing (do nothing)
     * PreparedSuccess -> PreparedSuccess (resend prepared)
     * Committing -> Committing (ignore)
     * Aborting -> Aborting (send aborted and forget)
     */
    public void prepare(final Notification prepare, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
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
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * None -> None (send aborted)
     * Active -> Aborting (execute rollback, send aborted and forget)
     * Preparing -> Aborting (execute rollback, send aborted and forget)
     * PreparedSuccess -> Aborting (execute rollback, send aborted and forget)
     * Committing -> Committing (ignore)
     * Aborting -> Aborting (send aborted and forget)
     */
    public void rollback(final Notification rollback, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
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
     * Committing -> Committing
     * PreparedSuccess -> PreparedSuccess (resend Prepared)
     * Aborting -> Aborting
     */
    public void recovery()
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
        }

        if (current == State.STATE_PREPARED_SUCCESS)
        {
            sendPrepared() ;
        }
    }

    /**
     * Handle the soap fault event.
     * @param soapFault The soap fault.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.soapFault_1 [com.arjuna.wst11.messaging.engines.ParticipantEngine.soapFault_1] - Unexpected SOAP fault for participant {0}: {1} {2}
     */
    public void soapFault(final SoapFault soapFault, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
    {
        if (WSTLogger.arjLoggerI18N.isDebugEnabled())
        {
            final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
            final SoapFaultType soapFaultType = soapFault.getSoapFaultType() ;
            final QName subCode = soapFault.getSubcode() ;
            WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.soapFault_1", new Object[] {instanceIdentifier, soapFaultType, subCode}) ;
        }

        if (CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME.equals(soapFault.getSubcode()))
        {
            forget() ;
        }
    }

    /**
     * Handle the commit decision event.
     *
     * Preparing -> PreparedSuccess (send Prepared)
     * Committing -> Committing (send committed and forget)
     */
    private void commitDecision()
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
            sendPrepared() ;
        }
        else if (current == State.STATE_COMMITTING)
        {
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
    private void commsTimeout()
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
        }

        if (current == State.STATE_PREPARED_SUCCESS)
        {
            sendPrepared() ;
        }
    }

    /**
     * Execute the commit transition.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.executeCommit_1 [com.arjuna.wst11.messaging.engines.ParticipantEngine.executeCommit_1] - Unexpected exception from participant commit
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
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.executeCommit_1", th) ;
            }
        }
    }

    /**
     * Execute the rollback transition.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.executeRollback_1 [com.arjuna.wst11.messaging.engines.ParticipantEngine.executeRollback_1] - Unexpected exception from participant rollback
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
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.executeRollback_1", th) ;
            }
        }
        return true ;
    }

    /**
     * Execute the prepare transition.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.executePrepare_1 [com.arjuna.wst11.messaging.engines.ParticipantEngine.executePrepare_1] - Unexpected exception from participant prepare
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.executePrepare_2 [com.arjuna.wst11.messaging.engines.ParticipantEngine.executePrepare_2] - Unexpected result from participant prepare: {0}
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
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.executePrepare_1", se) ;
            }
            return ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.executePrepare_1", th) ;
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
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.executePrepare_2", new Object[] {(vote == null ? "null" : vote.getClass().getName())}) ;
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
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.sendCommitted_1 [com.arjuna.wst11.messaging.engines.ParticipantEngine.sendCommitted_1] - Unexpected exception while sending Committed
     */
    private void sendCommitted()
    {
        final AddressingProperties responseAddressingContext = createContext() ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier(id) ;
        try
        {
            CoordinatorClient.getClient().sendCommitted(coordinator, responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.sendCommitted_1", th) ;
            }
        }
    }

    /**
     * Send the prepared message.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.sendPrepared_1 [com.arjuna.wst11.messaging.engines.ParticipantEngine.sendPrepared_1] - Unexpected exception while sending Prepared
     */
    private void sendPrepared()
    {
        final AddressingProperties responseAddressingContext = createContext() ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier(id) ;
        try
        {
            CoordinatorClient.getClient().sendPrepared(coordinator, responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.sendPrepared_1", th) ;
            }
        }

        initiateTimer() ;
    }

    /**
     * Send the aborted message.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.sendAborted_1 [com.arjuna.wst11.messaging.engines.ParticipantEngine.sendAborted_1] - Unexpected exception while sending Aborted
     */
    private void sendAborted()
    {
        final AddressingProperties responseAddressingContext = createContext() ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier(id) ;
        try
        {
            CoordinatorClient.getClient().sendAborted(coordinator, responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.sendAborted_1", th) ;
            }
        }
    }

    /**
     * Send the read only message.
     *
     * @message com.arjuna.wst11.messaging.engines.ParticipantEngine.sendReadOnly_1 [com.arjuna.wst11.messaging.engines.ParticipantEngine.sendReadOnly_1] - Unexpected exception while sending ReadOnly
     */
    private void sendReadOnly()
    {
        final AddressingProperties responseAddressingContext = createContext() ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier(id) ;
        try
        {
            CoordinatorClient.getClient().sendReadOnly(coordinator, responseAddressingContext, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.arjLoggerI18N.isDebugEnabled())
            {
                WSTLogger.arjLoggerI18N.debug("com.arjuna.wst11.messaging.engines.ParticipantEngine.sendReadOnly_1", th) ;
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
     * Create a response context from the incoming context.
     * @return The addressing context.
     */
    private AddressingProperties createContext()
    {
        final String messageId = MessageId.getMessageId() ;
        return AddressingHelper.createNotificationContext(messageId) ;
    }
    
    public W3CEndpointReference getCoordinator()
    {
        return coordinator;
    }
}
