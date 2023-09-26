/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.wst11.stub;

import com.arjuna.webservices11.wsba.State;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.BAParticipantManager;
import com.arjuna.wst11.messaging.engines.CoordinatorCompletionParticipantEngine;

import javax.xml.namespace.QName;

public class BACoordinatorCompletionParticipantManagerStub implements BAParticipantManager
{
    private final CoordinatorCompletionParticipantEngine coordinator ;

    public BACoordinatorCompletionParticipantManagerStub(final CoordinatorCompletionParticipantEngine coordinator)
        throws Exception
    {
        this.coordinator = coordinator ;
    }

    public void exit ()
        throws WrongStateException, UnknownTransactionException, SystemException
    {
        /*
         * Active -> illegal state
         * Canceling -> illegal state
         * Completed -> illegal state
         * Closing -> illegal state
         * Compensating -> illegal state
         * Faulting -> illegal state
         * Faulting-Active -> illegal state
         * Faulting-Compensating -> illegal state
         * Exiting -> no response
         * Ended -> ended
         */
        final State state = coordinator.exit() ;
        if (state == State.STATE_EXITING)
        {
            throw new SystemException() ;
        }
        else if (state != State.STATE_ENDED)
        {
            throw new WrongStateException() ;
        }
    }

    public void completed ()
        throws WrongStateException, UnknownTransactionException, SystemException
    {
        // returns original state
        final State state = coordinator.completed() ;
        if ((state != State.STATE_ACTIVE) && (state != State.STATE_COMPLETED))
        {
            throw new WrongStateException() ;
        }
    }

    public void cannotComplete()
        throws WrongStateException, UnknownTransactionException, SystemException
    {
        /*
         * Active -> illegal state
         * Canceling -> illegal state
         * Completing -> illegal state
         * Completed -> illegal state
         * Closing -> illegal state
         * Compensating -> illegal state
         * Failing-Active -> illegal state
         * Failing-Canceling -> illegal state
         * Failing-Completing -> illegal state
         * Failing-Compensating -> illegal state
         * NotCompleting -> no response
         * Exiting -> illegal state
         * Ended -> ended
         */
        final State state = coordinator.cannotComplete() ;
        if (state == State.STATE_NOT_COMPLETING)
        {
            throw new SystemException() ;
        }
        else if (state != State.STATE_ENDED)
        {
            throw new WrongStateException() ;
        }
    }

    public void fail (final QName exceptionIdentifier)
        throws SystemException
    {
        /*
         * Active -> illegal state
         * Canceling -> illegal state
         * Completing -> illegal state
         * Completed -> illegal state
         * Closing -> illegal state
         * Compensating -> illegal state
         * Failing-Active -> no response
         * Failing-Canceling -> no response
         * Failing-Completing -> no response
         * Failing-Compensating -> no response
         * NotCompleting -> illegal state
         * Exiting -> illegal state
         * Ended -> ended
         */
        final State state = coordinator.fail(exceptionIdentifier) ;
        if (state != State.STATE_ENDED)
        {
            throw new SystemException() ;
        }
    }
}