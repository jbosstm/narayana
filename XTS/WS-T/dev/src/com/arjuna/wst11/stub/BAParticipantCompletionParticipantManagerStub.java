/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.wst11.stub;

import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices11.wsba.State;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.BAParticipantManager;
import com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine;

import javax.xml.namespace.QName;

public class BAParticipantCompletionParticipantManagerStub implements BAParticipantManager
{
    private final ParticipantCompletionParticipantEngine coordinator ;

    public BAParticipantCompletionParticipantManagerStub(final ParticipantCompletionParticipantEngine coordinator)
        throws Exception
    {
        if (WSTLogger.logger.isTraceEnabled()) {
            WSTLogger.logger.trace(getClass().getSimpleName() + " constructor");
        }

        this.coordinator = coordinator ;
    }

    public synchronized void exit ()
        throws WrongStateException, UnknownTransactionException, SystemException
    {
        if (WSTLogger.logger.isTraceEnabled()) {
            WSTLogger.logger.trace(getClass().getSimpleName() + ".exit");
        }

        /*
         * Active -> illegal state
         * Canceling -> illegal state
         * Completed -> illegal state
         * Closing -> illegal state
         * Compensating -> illegal state
         * Failing-Active -> illegal state
         * Failing-Canceling -> illegal state
         * Failing-Compensating -> illegal state
         * NotCompleting -> illegal state
         * Exiting -> no response
         * Ended -> ended
         */
        final State state = coordinator.exit() ;

        if (WSTLogger.logger.isTraceEnabled()) {
            WSTLogger.logger.trace(getClass().getSimpleName() + ".exit. State: " + state);
        }

        if (state == State.STATE_EXITING)
        {
            throw new SystemException() ;
        }
        else if (state != State.STATE_ENDED)
        {
            throw new WrongStateException() ;
        }
    }

    public synchronized void completed ()
        throws WrongStateException, UnknownTransactionException, SystemException
    {
        if (WSTLogger.logger.isTraceEnabled()) {
            WSTLogger.logger.trace(getClass().getSimpleName() + ".completed");
        }

        // returns original state
        final State state = coordinator.completed() ;

        if (WSTLogger.logger.isTraceEnabled()) {
            WSTLogger.logger.trace(getClass().getSimpleName() + ".completed. State: " + state);
        }

        if ((state != State.STATE_ACTIVE) && (state != State.STATE_COMPLETED))
        {
            throw new WrongStateException() ;
        }
    }

    public void cannotComplete()
        throws WrongStateException, UnknownTransactionException, SystemException
    {
        if (WSTLogger.logger.isTraceEnabled()) {
            WSTLogger.logger.trace(getClass().getSimpleName() + ".cannotComplete");
        }

        /*
         * Active -> illegal state
         * Canceling -> illegal state
         * Completed -> illegal state
         * Closing -> illegal state
         * Compensating -> illegal state
         * Failing-Active -> illegal state
         * Failing-Canceling -> illegal state
         * Failing-Compensating -> illegal state
         * NotCompleting -> no response
         * Exiting -> illegal state
         * Ended -> ended
         */
        final State state = coordinator.cannotComplete() ;

        if (WSTLogger.logger.isTraceEnabled()) {
            WSTLogger.logger.trace(getClass().getSimpleName() + ".completed. State: " + state);
        }

        if (state == State.STATE_NOT_COMPLETING)
        {
            throw new SystemException() ;
        }
        else if (state != State.STATE_ENDED)
        {
            throw new WrongStateException() ;
        }
    }

    public synchronized void fail (final QName exceptionIdentifier)
        throws SystemException
    {
        if (WSTLogger.logger.isTraceEnabled()) {
            WSTLogger.logger.trace(getClass().getSimpleName() + ".fail");
        }

        /*
         * Active -> illegal state
         * Canceling -> illegal state
         * Completed -> illegal state
         * Closing -> illegal state
         * Compensating -> illegal state
         * Failing-Active -> no response
         * Failing-Canceling -> no response
         * Failing-Compensating -> no response
         * NotCompleting -> illegal state
         * Exiting -> illegal state
         * Ended -> ended
         */
        final State state = coordinator.fail(exceptionIdentifier) ;

        if (WSTLogger.logger.isTraceEnabled()) {
            WSTLogger.logger.trace(getClass().getSimpleName() + ".fail. State: " + state);
        }

        if (state != State.STATE_ENDED)
        {
            throw new SystemException() ;
        }
    }
}