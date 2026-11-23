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
package com.jboss.transaction.txinterop.webservices.bainterop.processors;

import com.arjuna.ats.arjuna.common.Uid;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.wst11.messaging.engines.CoordinatorCompletionParticipantEngine;
import com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine;
import com.arjuna.webservices11.SoapFault11;
import com.jboss.transaction.txinterop.webservices.bainterop.BAInteropUtil;
import com.jboss.transaction.txinterop.webservices.bainterop.participant.CancelParticipant;
import com.jboss.transaction.txinterop.webservices.bainterop.participant.CoordinatorCompleteCloseParticipant;
import com.jboss.transaction.txinterop.webservices.bainterop.participant.DropFirstCompensateParticipant;
import com.jboss.transaction.txinterop.webservices.bainterop.participant.ExitParticipant;
import com.jboss.transaction.txinterop.webservices.bainterop.participant.FailParticipant;
import com.jboss.transaction.txinterop.webservices.bainterop.participant.CannotCompleteParticipant;
import com.jboss.transaction.txinterop.webservices.bainterop.participant.CompletedParticipant;
import com.jboss.transaction.txinterop.webservices.bainterop.participant.FailedCompensateParticipant;

/**
 * The Participant processor.
 * @author kevin
 */
public class BAParticipantProcessor
{
    /**
     * The participant.
     */
    private static BAParticipantProcessor PARTICIPANT = new BAParticipantProcessor() ;
    
    /**
     * Get the participant.
     * @return The participant.
     */
    public static BAParticipantProcessor getParticipant()
    {
        return PARTICIPANT ;
    }
    
    /**
     * Execute the Cancel
     * @param map The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void cancel(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault11
    {
        try
        {
            BAInteropUtil.registerParticipantCompletion(coordinationContext, new CancelParticipant(), new Uid().toString()) ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the Exit
     * @param map The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void exit(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault11
    {
        try
        {
            final ExitParticipant participant = new ExitParticipant() ;
            final CoordinatorCompletionParticipantEngine engine = BAInteropUtil.registerCoordinatorCompletion(coordinationContext, participant, new Uid().toString()) ;
            participant.setEngine(engine) ;
            participant.initialiseTimeout() ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the Fail
     * @param map The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void fail(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault11
    {
        try
        {
            final FailParticipant participant = new FailParticipant() ;
            final ParticipantCompletionParticipantEngine engine = BAInteropUtil.registerParticipantCompletion(coordinationContext, participant, new Uid().toString()) ;
            participant.setEngine(engine) ;
            participant.initialiseTimeout() ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the CannotComplete
     * @param map The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void cannotComplete(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault11
    {
        try
        {
            final CannotCompleteParticipant participant = new CannotCompleteParticipant() ;
            final CoordinatorCompletionParticipantEngine engine = BAInteropUtil.registerCoordinatorCompletion(coordinationContext, participant, new Uid().toString()) ;
            participant.setEngine(engine) ;
            participant.initialiseTimeout() ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the ParticipantCompleteClose
     * @param map The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void participantCompleteClose(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault11
    {
        try
        {
            final CompletedParticipant participant = new CompletedParticipant() ;
            final ParticipantCompletionParticipantEngine engine = BAInteropUtil.registerParticipantCompletion(coordinationContext, participant, new Uid().toString()) ;
            participant.setEngine(engine) ;
            participant.initialiseTimeout() ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the CoordinatorCompleteClose
     * @param map The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void coordinatorCompleteClose(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault11
    {
        try
        {
            BAInteropUtil.registerCoordinatorCompletion(coordinationContext, new CoordinatorCompleteCloseParticipant(), new Uid().toString()) ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the UnsolicitedComplete
     * @param map The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void unsolicitedComplete(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault11
    {
        try
        {
            final CompletedParticipant participant = new CompletedParticipant() ;
            final ParticipantCompletionParticipantEngine engine = BAInteropUtil.registerParticipantCompletion(coordinationContext, participant, new Uid().toString()) ;
            participant.setEngine(engine) ;
            participant.initialiseTimeout() ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the Compensate
     * @param map The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void compensate(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault11
    {
        try
        {
            final CompletedParticipant participant = new CompletedParticipant() ;
            final ParticipantCompletionParticipantEngine engine = BAInteropUtil.registerParticipantCompletion(coordinationContext, participant, new Uid().toString()) ;
            participant.setEngine(engine) ;
            participant.initialiseTimeout() ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the ParticipantCompensationFail
     * @param map The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void participantCompensationFail(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault11
    {
        try
        {
            final FailedCompensateParticipant participant = new FailedCompensateParticipant() ;
            final ParticipantCompletionParticipantEngine engine = BAInteropUtil.registerParticipantCompletion(coordinationContext, participant, new Uid().toString()) ;
            participant.setEngine(engine) ;
            participant.initialiseTimeout() ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the ParticipantCancelCompletedRace
     * @param map The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void participantCancelCompletedRace(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault11
    {
        try
        {
            final CompletedParticipant participant = new CompletedParticipant() ;
            final ParticipantCompletionParticipantEngine engine = BAInteropUtil.registerParticipantCompletion(coordinationContext, participant, new Uid().toString()) ;
            participant.setEngine(engine) ;
            participant.initialiseTimeout() ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the MessageLossAndRecovery
     * @param map The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void messageLossAndRecovery(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault11
    {
        try
        {
            BAInteropUtil.registerCoordinatorCompletion(coordinationContext, new DropFirstCompensateParticipant(), new Uid().toString()) ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the MixedOutcome
     * @param map The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void mixedOutcome(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault11
    {
    }
}
