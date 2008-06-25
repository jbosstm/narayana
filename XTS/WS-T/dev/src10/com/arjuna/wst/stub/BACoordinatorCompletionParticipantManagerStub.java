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
package com.arjuna.wst.stub;

import com.arjuna.webservices.wsba.State;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.messaging.engines.CoordinatorCompletionParticipantEngine;

public class BACoordinatorCompletionParticipantManagerStub implements com.arjuna.wst.BAParticipantManager
{
    private final CoordinatorCompletionParticipantEngine coordinator ;
    
    public BACoordinatorCompletionParticipantManagerStub (final CoordinatorCompletionParticipantEngine coordinator)
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

    public void fault ()
        throws SystemException
    {
        /*
         * Active -> illegal state
         * Canceling -> illegal state
         * Completed -> illegal state
         * Closing -> illegal state
         * Compensating -> illegal state
         * Faulting -> illegal state
         * Faulting-Active -> no response
         * Faulting-Compensating -> no response 
         * Exiting -> illegal state
         * Ended -> ended
         */
        final State state = coordinator.fault() ;
        if (state != State.STATE_ENDED)
        {
            throw new SystemException() ;
        }
    }

    public void unknown()
        throws SystemException
    {
        error() ;
    }

    public void error()
        throws SystemException
    {
        fault() ;
    }
}
