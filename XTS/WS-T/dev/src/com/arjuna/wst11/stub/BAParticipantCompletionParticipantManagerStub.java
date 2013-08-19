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
package com.arjuna.wst11.stub;

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
        this.coordinator = coordinator ;
    }

    public synchronized void exit ()
        throws WrongStateException, UnknownTransactionException, SystemException
    {
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
        // returns original state
        final State state = coordinator.completed() ;
        if ((state != State.STATE_ACTIVE) && (state != State.STATE_COMPLETED))
        {
            throw new WrongStateException() ;
        }
    }

    public synchronized void synchronousCompleted() throws WrongStateException, UnknownTransactionException, SystemException
    {
        final State state = coordinator.synchronousCompleted() ;
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
        if (state != State.STATE_ENDED)
        {
            throw new SystemException() ;
        }
    }
}