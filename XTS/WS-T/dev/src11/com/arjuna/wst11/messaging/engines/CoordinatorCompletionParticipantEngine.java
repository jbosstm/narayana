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
import com.arjuna.wst11.ConfirmCompletedParticipant;
import org.jboss.wsf.common.addressing.MAP;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsba.CoordinatorCompletionParticipantInboundEvents;
import com.arjuna.webservices11.wsba.State;
import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.webservices11.wsba.client.CoordinatorCompletionCoordinatorClient;
import com.arjuna.webservices11.wsba.processors.CoordinatorCompletionParticipantProcessor;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.FaultedException;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;
import org.jboss.jbossts.xts.recovery.participant.ba.XTSBARecoveryManager;
import org.jboss.jbossts.xts11.recovery.participant.ba.BAParticipantRecoveryRecord;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.util.TimerTask;

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
    private final W3CEndpointReference coordinator ;
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
     * true if this participant has been recovered otherwise false
     */
    private boolean recovered;

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
    public CoordinatorCompletionParticipantEngine(final String id, final W3CEndpointReference coordinator,
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
     * @param recovered true if the engine has been recovered from th elog otherwise false
     */
    public CoordinatorCompletionParticipantEngine(final String id, final W3CEndpointReference coordinator,
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
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Canceling
     * Canceling -> Canceling
     * Completing -> Canceling
     * Completed -> Completed (resend Completed)
     * Closing -> Closing
     * Compensating -> Compensating
     * Failing-Active -> Failing-Active (resend Fail)
     * Failing-Canceling -> Failing-Canceling (resend Fail)
     * Failing-Completing -> Failing-Completing (resend Fail)
     * Failing-Compensating -> Failing-Compensating
     * NotCompleting -> NotCompleting (resend CannotComplete)
     * Exiting -> Exiting (resend Exit)
     * Ended -> Ended (send Canceled)
     */
    public void cancel(final NotificationType cancel, final MAP map, final ArjunaContext arjunaContext)
    {
        final State current ;
        synchronized(this)
        {
            current = state ;
            if ((current == State.STATE_ACTIVE) || (current == State.STATE_COMPLETING))
            {
                changeState(State.STATE_CANCELING) ;
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
        else if ((current == State.STATE_FAILING_ACTIVE) || (current == State.STATE_FAILING_CANCELING) ||
        	 (current == State.STATE_FAILING_COMPLETING))
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
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
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
     * Ended -> Ended (send Closed)
     */
    public void close(final NotificationType close, final MAP map, final ArjunaContext arjunaContext)
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
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completing -> Completing (invalid state)
     * Completed -> Compensating
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Completing -> Failing-Completing (invalid state)
     * Failing-Compensating -> Failing-Compensating (resend Fail)
     * NotCompleting -> NotCompleting (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended (send Compensated)
     */
    public void compensate(final NotificationType compensate, final MAP map, final ArjunaContext arjunaContext)
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
     * Handle the complete event.
     * @param complete The complete notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Completing
     * Canceling -> Canceling
     * Completing -> Completing
     * Completed -> Completed (resend Completed)
     * Closing -> Closing
     * Compensating -> Compensating
     * Failing-Active -> Failing-Active (resend Fail)
     * Failing-Canceling -> Failing-Canceling (resend Fail)
     * Failing-Completing -> Failing-Completing (resend Fail)
     * Failing-Compensating -> Failing-Compensating
     * NotCompleting -> NotCompleting (resend CannotComplete)
     * Exiting -> Exiting (resend Exit)
     * Ended -> Ended (send Fail)
     */
    public void complete(final NotificationType complete, final MAP map, final ArjunaContext arjunaContext)
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
            executeComplete() ;
        }
        else if (current == State.STATE_COMPLETED)
        {
            sendCompleted() ;
        }
        else if ((current == State.STATE_FAILING_ACTIVE) || (current == State.STATE_FAILING_CANCELING) ||
        	 (current == State.STATE_FAILING_COMPLETING) || (current == State.STATE_ENDED))
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
    }

    /**
     * Handle the exited event.
     * @param exited The exited notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completing -> Completing (invalid state)
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Completing -> Failing-Completing (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * NotCompleting -> NotCompleting (invalid state)
     * Exiting -> Ended
     * Ended -> Ended
     */
    public void exited(final NotificationType exited, final MAP map, final ArjunaContext arjunaContext)
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
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completing -> Completing (invalid state)
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Ended
     * Failing-Canceling -> Ended
     * Failing-Completing -> Ended
     * Failing-Compensating -> Ended
     * NotCompleting -> NotCompleting (invalid state)
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended
     */
    public void failed(final NotificationType failed, final MAP map, final ArjunaContext arjunaContext)
    {
        final State current ;
        boolean deleteRequired = false;
        synchronized(this)
        {
            current = state ;
            if ((current == State.STATE_FAILING_ACTIVE) || (current == State.STATE_FAILING_CANCELING) ||
                (current == State.STATE_FAILING_COMPLETING) || (current == State.STATE_FAILING_COMPENSATING))
            {
                deleteRequired = persisted;
            }
        }
        // if we just ended the participant ensure any log record gets deleted

        if (deleteRequired) {
            if (!XTSBARecoveryManager.getRecoveryManager().deleteParticipantRecoveryRecord(id)) {
                // hmm, could not delete entry -- nothing more we can do than log a message
                WSTLogger.i18NLogger.warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_failed_1(id);
            }
        }
        // now we have removed the log record we can safely get rid of the participant
        if ((current == State.STATE_FAILING_ACTIVE) || (current == State.STATE_FAILING_CANCELING) ||
            (current == State.STATE_FAILING_COMPLETING) || (current == State.STATE_FAILING_COMPENSATING))
        {
            ended();
        }
    }

    /**
     * Handle the not completed event.
     * @param notCompleted The not completed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completing -> Completing (invalid state)
     * Completed -> Completed (invalid state)
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Completing -> Failing-Completing (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * NotCompleting -> Ended
     * Exiting -> Exiting (invalid state)
     * Ended -> Ended
     */
    public void notCompleted(final NotificationType notCompleted, final MAP map, final ArjunaContext arjunaContext)
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
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     *
     */
    public void getStatus(final NotificationType getStatus, final MAP map, final ArjunaContext arjunaContext)
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
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void status(final StatusType status, final MAP map, final ArjunaContext arjunaContext)
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
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * NotCompleting -> NotCompleting (invalid state)
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
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void soapFault(final SoapFault soapFault, final MAP map, final ArjunaContext arjunaContext)
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
                WSTLogger.i18NLogger.warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_soapFault_2(id);
                participant.compensate();
            } else {
                // hmm, something went wrong -- notify the participant of the error
                WSTLogger.i18NLogger.warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_soapFault_3(id);
                participant.error();
            }
        }
        catch (final Throwable th) {} // ignore
        // if we just ended the participant ensure any log record gets deleted
        if (deleteRequired) {
            if (!XTSBARecoveryManager.getRecoveryManager().deleteParticipantRecoveryRecord(id)) {
                // hmm, could not delete entry -- nothing more we can do than log a message
                WSTLogger.i18NLogger.warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_soapFault_1(id);
            }
        }
    }

    /**
     * Handle the completed event.
     *
     * Active -> Active (invalid state)
     * Canceling -> Canceling (invalid state)
     * Completing -> Completed
     * Completed -> Completed
     * Closing -> Closing (invalid state)
     * Compensating -> Compensating (invalid state)
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Completing -> Failing-Completing (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * Exiting -> Exiting (invalid state)
     * NotCompleting -> NotCompleting (invalid state)
     * Ended -> Ended (invalid state)
     */
    public State completed()
    {
        // TODO -- check. not sure this can or should ever be called for a coordinator completion participant
        State current ;
        boolean failRequired  = false;
        boolean deleteRequired  = false;
        boolean confirm = (participant instanceof ConfirmCompletedParticipant);
        synchronized(this)
        {
            current = state ;
        }

        if (current == State.STATE_COMPLETING) {
            // ok we need to write the participant details to disk because it has just completed
            BAParticipantRecoveryRecord recoveryRecord = new BAParticipantRecoveryRecord(id, participant, false, coordinator);

            if (!XTSBARecoveryManager.getRecoveryManager().writeParticipantRecoveryRecord(recoveryRecord)) {
                // hmm, could not write entry log warning
                WSTLogger.i18NLogger.warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_completed_1(id);
                // we need to fail this transaction
                failRequired = true;
            }
        }
        // recheck state before we decide whether we need to fail -- we might have been sent a cancel while
        // writing the log

        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_COMPLETING) {
                if (!failRequired) {
                    changeState(State.STATE_COMPLETED);
                    // record the fact that we have persisted this object so later operations will delete
                    // the log record
                    persisted = true;
                    // if necessary notify the client now. n.b. this has to be done synchronized because
                    // if we release the lock then a resent COMPLETE may result in a COMPLETED being
                    // sent back and we cannot allow that until after the confirm
                    if (confirm) {
                        ((ConfirmCompletedParticipant) participant).confirmCompleted(true);
                    }
                } else {
                    // we must force a fail but we don't have a log record to delete
                    changeState(State.STATE_FAILING_COMPLETING);
                }
            } else {
                // we need to delete the log record here as the cancel would not have known it was persisted
                deleteRequired = true;
            }
        }


        // check to see if we need to send a fail or delete the log record before going ahead to complete

        if (failRequired) {
            current = fail(BusinessActivityConstants.WSBA_ELEMENT_FAIL_QNAME);
            // we can safely do this now
            if (confirm) {
                ((ConfirmCompletedParticipant) participant).confirmCompleted(false);
            }
        } else if (deleteRequired) {
            if (!XTSBARecoveryManager.getRecoveryManager().deleteParticipantRecoveryRecord(id)) {
                // hmm, could not delete entry log warning
                WSTLogger.i18NLogger.warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_completed_2(id);
            }
            // we can safely do this now
            if (confirm) {
                ((ConfirmCompletedParticipant) participant).confirmCompleted(false);
            }
        } else if ((current == State.STATE_COMPLETING) || (current == State.STATE_COMPLETED)) {
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
     * Failing-Active -> Failing-Active (invalid state)
     * Failing-Canceling -> Failing-Canceling (invalid state)
     * Failing-Completing -> Failing-Completing (invalid state)
     * Failing-Compensating -> Failing-Compensating (invalid state)
     * Exiting -> Exiting
     * NotCompleting -> NotCompleting (invalid state)
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
            return waitForState(State.STATE_EXITING, timeout) ;
        }
        return current ;
    }

    /**
     * Handle the fail event.
     *
     * Active -> Failing-Active
     * Canceling -> Failing-Canceling
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
            sendFail(exceptionIdentifier) ;
            return waitForState(State.STATE_FAILING_ACTIVE, timeout) ;
        }
        else if (current == State.STATE_CANCELING)
        {
            sendFail(exceptionIdentifier) ;
            return waitForState(State.STATE_FAILING_CANCELING, timeout) ;
        }
        else if (current == State.STATE_COMPLETING)
        {
            sendFail(exceptionIdentifier) ;
            return waitForState(State.STATE_FAILING_COMPLETING, timeout) ;
        }
        else if (current == State.STATE_COMPENSATING)
        {
            sendFail(exceptionIdentifier) ;
            return waitForState(State.STATE_FAILING_COMPENSATING, timeout) ;
        }

        return current ;
    }

    /**
     * Handle the cannot complete event.
     *
     * Active -> NotCompleting
     * Canceling -> Canceling (invalid state)
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
     * Ended -> Ended (invalid state)
     */
    public State cannotComplete()
    {
        final State current ;
        synchronized (this)
        {
            current = state ;
            if ((current == State.STATE_ACTIVE) || (current == State.STATE_COMPLETING))
            {
                changeState(State.STATE_NOT_COMPLETING) ;
            }
        }

        if ((current == State.STATE_ACTIVE) || (current == State.STATE_COMPLETING) ||
            (current == State.STATE_NOT_COMPLETING))
        {
            sendCannotComplete() ;
            return waitForState(State.STATE_NOT_COMPLETING, timeout) ;
        }
        return current ;
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
        final MAP map = createContext() ;
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendExit(coordinator, map, instanceIdentifier) ;
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
     * Send the completed message.
     */
    private void sendCompleted()
    {
        sendCompleted(false);
    }

    /**
     * Send the completed message.
     *
     * @param timedOut true if this is in response to a comms timeout
     */
    private void sendCompleted(boolean timedOut)
    {
        final MAP map = createContext() ;
        try
        {
            // if we are trying to reestablish the participant state then send getStatus otherwise send completed
            if (timedOut && checkStatus) {
                CoordinatorCompletionCoordinatorClient.getClient().sendGetStatus(coordinator, map, instanceIdentifier); ;
            } else {
                CoordinatorCompletionCoordinatorClient.getClient().sendCompleted(coordinator, map, instanceIdentifier) ;
            }
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
            } else {
                // ok, we hit our maximum period last time -- this time switch to sending getStatus
                checkStatus = true;
            }
        } else {
            if (resendPeriod > initialResendPeriod) {
                resendPeriod = initialResendPeriod;
            }
            // if we were previously checking status we need to revert to sending Completed
            if (checkStatus) {
                checkStatus = false;
            }
        }
    }

    /**
     * Send the fail message.
     * @param message The fail message.
     *
     */
    private void sendFail(final QName message)
    {
        final MAP map = createContext() ;
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendFail(coordinator, map, instanceIdentifier, message) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception while sending Fail", th) ;
            }
        }
    }

    /**
     * Send the cancelled message.
     *
     */
    private void sendCancelled()
    {
        final MAP map = createContext() ;
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendCancelled(coordinator, map, instanceIdentifier) ;
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
        final MAP map = createContext() ;
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendClosed(coordinator, map, instanceIdentifier) ;
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
        final MAP map = createContext() ;
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendCompensated(coordinator, map, instanceIdentifier) ;
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
        final MAP map = createContext() ;
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendStatus(coordinator, map, instanceIdentifier, state.getValue()) ;
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
     * Send the cannot complete message.
     *
     */
    private void sendCannotComplete()
    {
        final MAP map = createContext() ;
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendCannotComplete(coordinator, map, instanceIdentifier) ;
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception while sending CannotComplete", th) ;
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
        boolean failRequired = false;

        // TODO -- there is a potential race here with a completing thread
        // the state diagrams in the spec say that if a cancel comes in while completing we have to cancel
        // but the participant may be part way through executing a complete. strictly, that's something
        // the participant has to deal with not us
        try
        {
            participant.cancel() ;
        }
        catch (final FaultedException fe)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Faulted exception from participant cancel for WS-BA participant {0}", new Object[] { id}, fe) ;
            }
            // fail here because the participant doesn't want to retry the cancel
            fail(BusinessActivityConstants.WSBA_ELEMENT_FAIL_QNAME);
        }
        catch (final Throwable th)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Unexpected exception from participant cancel for WS-BA participant {0}", new Object[] { id}, th) ;
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
                        failRequired = true;
                        changeState(State.STATE_FAILING_CANCELING);
                    } else {
                        changeState(State.STATE_ACTIVE);
                        return;
                    }
                }
            }
        }
        if (failRequired) {
            fail(BusinessActivityConstants.WSBA_ELEMENT_FAIL_QNAME);
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
                WSTLogger.logger.tracev("Unexpected exception from participant close for WS-BA participant {0}", th) ;
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
                WSTLogger.i18NLogger.warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_executeClose_2(id);
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
        catch (final FaultedException fe)
        {
            if (WSTLogger.logger.isTraceEnabled())
            {
                WSTLogger.logger.tracev("Faulted exception from participant compensate for WS-BA participant {0}", new Object[] { id}, fe) ;
            }
            // fail here because the participant doesn't want to retry the compensate
            fail(BusinessActivityConstants.WSBA_ELEMENT_FAIL_QNAME) ;
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
                WSTLogger.logger.tracev("Unexpected exception from participant compensate for WS-BA participant {0}", new Object[] { id }, th) ;
            }
            return ;
        }

        final State current ;
        boolean failRequired = false;
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
                        failRequired = true;
                        changeState(State.STATE_FAILING_COMPENSATING);
                    }
                }
                // if we did not fail then we can decommission the participant now avoiding any further races
                // we will send the compensate after we exit the synchronized block
                if (!failRequired) {
                    ended();
                }
            }
        }
        if (failRequired) {
            fail(BusinessActivityConstants.WSBA_ELEMENT_FAIL_QNAME);
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
                WSTLogger.logger.tracev("Unexpected exception from participant complete for WS-BA participant {0}", new Object[] {id}, th) ;
            }
            return ;
        }

        State current ;
        boolean failRequired  = false;
        boolean deleteRequired  = false;
        boolean confirm = (participant instanceof ConfirmCompletedParticipant);
        synchronized (this)
        {
            current = state ;
        }

        if (current == State.STATE_COMPLETING)
        {
            // ok we need to write the participant details to disk because it has just completed
            BAParticipantRecoveryRecord recoveryRecord = new BAParticipantRecoveryRecord(id, participant, false, coordinator);

            if (!XTSBARecoveryManager.getRecoveryManager().writeParticipantRecoveryRecord(recoveryRecord)) {
                // hmm, could not write entry log warning
                WSTLogger.i18NLogger.warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_executeComplete_2(id);
                // we need to fail this transaction
                failRequired = true;
            }
        }

        // recheck state before we decide whether we need to fail -- we might have been sent a cancel while
        // writing the log

        synchronized(this)
        {
            current = state ;
            if (current == State.STATE_COMPLETING) {
                if (!failRequired) {
                    changeState(State.STATE_COMPLETED) ;
                    // record the fact that we have persisted this object so later operations will delete
                    // the log record
                    persisted = true;
                    // if necessary notify the client now. n.b. this has to be done synchronized because
                    // if we release the lock then a resent COMPLETE may result in a COMPLETED being
                    // sent back and we cannot allow that until after the confirm
                    if (confirm) {
                        ((ConfirmCompletedParticipant) participant).confirmCompleted(true);
                    }
                } else {
                    // we must force a fail but we don't have a log record to delete
                    changeState(State.STATE_FAILING_COMPLETING);
                }
            } else {
                // we cannot force a fail now so just delete
                failRequired = false;
                // we need to delete the log record here as the cancel would not have known it was persisted
                deleteRequired = true;
            }
        }

        // check to see if we need to send a fail or delete the log record before going ahead to complete

        if (failRequired) {
            current = fail(BusinessActivityConstants.WSBA_ELEMENT_FAIL_QNAME);
            // we can safely do this now
            if (confirm) {
                ((ConfirmCompletedParticipant) participant).confirmCompleted(false);
            }
        } else if (deleteRequired) {
            if (!XTSBARecoveryManager.getRecoveryManager().deleteParticipantRecoveryRecord(id)) {
                // hmm, could not delete entry log warning
                WSTLogger.i18NLogger.warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_completed_2(id);
            }
            if (confirm) {
                ((ConfirmCompletedParticipant) participant).confirmCompleted(false);
            }
        } else if (current == State.STATE_COMPLETING) {
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
    private MAP createContext()
    {
        final String messageId = MessageId.getMessageId() ;

        return AddressingHelper.createNotificationContext(messageId) ;
    }
}