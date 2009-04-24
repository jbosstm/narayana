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
package com.jboss.transaction.wstf.webservices.sc007.processors;

import com.arjuna.ats.arjuna.common.Uid;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import com.arjuna.wst.CompletionCoordinatorParticipant;
import com.arjuna.wst11.messaging.engines.ParticipantEngine;
import com.arjuna.webservices11.SoapFault11;
import com.jboss.transaction.wstf.webservices.sc007.InteropUtil;
import com.jboss.transaction.wstf.webservices.sc007.participant.CommitDurable2PCParticipant;
import com.jboss.transaction.wstf.webservices.sc007.participant.CommitFailureDurable2PCParticipant;
import com.jboss.transaction.wstf.webservices.sc007.participant.CommitFailureRecoveryDurable2PCParticipant;
import com.jboss.transaction.wstf.webservices.sc007.participant.CommitVolatile2PCParticipant;
import com.jboss.transaction.wstf.webservices.sc007.participant.ReadonlyDurable2PCParticipant;
import com.jboss.transaction.wstf.webservices.sc007.participant.RollbackDurable2PCParticipant;
import com.jboss.transaction.wstf.webservices.sc007.participant.VolatileAndDurableVolatile2PCParticipant;

import javax.xml.ws.addressing.AddressingProperties;

/**
 * The Participant processor.
 * @author kevin
 */
public class ParticipantProcessor
{
    /**
     * The participant.
     */
    private static ParticipantProcessor PARTICIPANT = new ParticipantProcessor() ;
    
    /**
     * Get the participant.
     * @return The participant.
     */
    public static ParticipantProcessor getParticipant()
    {
        return PARTICIPANT ;
    }
    
    /**
     * Execute the CompletionCommit
     * @param coordinatorURI The address of the coordinator to employ
     * @param addressingProperties The current addressing context.
     * @throws SoapFault11 for errors during processing
     */
    public void completionCommit(final String coordinatorURI, final AddressingProperties addressingProperties)
        throws SoapFault11
    {
        try
        {
            final CoordinationContextType context = InteropUtil.createCoordinationContext(coordinatorURI) ;
            final CompletionCoordinatorParticipant participant = InteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
            participant.commit() ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the CompletionRollback
     * @param coordinatorURI The address of the coordinator to employ.
     * @param addressingProperties The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void completionRollback(final String coordinatorURI, final AddressingProperties addressingProperties)
        throws SoapFault11
    {
        try
        {
            final CoordinationContextType context = InteropUtil.createCoordinationContext(coordinatorURI) ;
            final CompletionCoordinatorParticipant participant = InteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
            participant.rollback() ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the Commit
     * @param addressingProperties The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void commit(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault11
    {
        try
        {
            InteropUtil.registerDurable2PC(coordinationContext, new CommitDurable2PCParticipant(), new Uid().toString()) ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the Rollback
     * @param addressingProperties The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void rollback(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault11
    {
        try
        {
            InteropUtil.registerDurable2PC(coordinationContext, new RollbackDurable2PCParticipant(), new Uid().toString()) ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the Phase2Rollback
     * @param addressingProperties The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void phase2Rollback(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault11
    {
        try
        {
            InteropUtil.registerVolatile2PC(coordinationContext, new CommitVolatile2PCParticipant(), new Uid().toString()) ;
            InteropUtil.registerDurable2PC(coordinationContext, new RollbackDurable2PCParticipant(), new Uid().toString()) ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the Readonly
     * @param addressingProperties The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void readonly(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault11
    {
        try
        {
            InteropUtil.registerDurable2PC(coordinationContext, new ReadonlyDurable2PCParticipant(), new Uid().toString()) ;
            InteropUtil.registerDurable2PC(coordinationContext, new CommitDurable2PCParticipant(), new Uid().toString()) ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the VolatileAndDurable
     * @param addressingProperties The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void volatileAndDurable(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault11
    {
        try
        {
            InteropUtil.registerVolatile2PC(coordinationContext, new VolatileAndDurableVolatile2PCParticipant(coordinationContext), new Uid().toString()) ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the EarlyReadonly
     * @param addressingProperties The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void earlyReadonly(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault11
    {
        try
        {
            final ParticipantEngine engine = InteropUtil.registerVolatile2PC(coordinationContext, new CommitVolatile2PCParticipant(), new Uid().toString()) ;
            InteropUtil.registerDurable2PC(coordinationContext, new CommitDurable2PCParticipant(), new Uid().toString()) ;
            engine.earlyReadonly() ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the EarlyAborted
     * @param addressingProperties The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void earlyAborted(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault11
    {
        try
        {
            final ParticipantEngine engine = InteropUtil.registerVolatile2PC(coordinationContext, new CommitVolatile2PCParticipant(), new Uid().toString()) ;
            InteropUtil.registerDurable2PC(coordinationContext, new CommitDurable2PCParticipant(), new Uid().toString()) ;
            engine.earlyRollback() ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the ReplayCommit
     * @param addressingProperties The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void replayCommit(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault11
    {
        try
        {
            final CommitFailureRecoveryDurable2PCParticipant participant = new CommitFailureRecoveryDurable2PCParticipant() ;
            final ParticipantEngine engine = InteropUtil.registerDurable2PC(coordinationContext, participant, new Uid().toString()) ;
            participant.setEngine(engine) ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the RetryPreparedCommit
     * @param addressingProperties The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void retryPreparedCommit(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault11
    {
        try
        {
            InteropUtil.registerDurable2PC(coordinationContext, new CommitDurable2PCParticipant(), new Uid().toString()) ;
            InteropUtil.registerDurable2PC(coordinationContext, new CommitDurable2PCParticipant(), new Uid().toString()) ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the RetryPreparedAbort
     * @param addressingProperties The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void retryPreparedAbort(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault11
    {
        try
        {
            InteropUtil.registerDurable2PC(coordinationContext, new CommitDurable2PCParticipant(), new Uid().toString()) ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the RetryCommit
     * @param addressingProperties The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void retryCommit(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault11
    {
        try
        {
            InteropUtil.registerDurable2PC(coordinationContext, new CommitFailureDurable2PCParticipant(), new Uid().toString()) ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the PreparedAfterTimeout
     * @param addressingProperties The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void preparedAfterTimeout(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault11
    {
        try
        {
            InteropUtil.registerVolatile2PC(coordinationContext, new CommitVolatile2PCParticipant(), new Uid().toString()) ;
            InteropUtil.registerDurable2PC(coordinationContext, new CommitDurable2PCParticipant(), new Uid().toString()) ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
    
    /**
     * Execute the LostCommitted
     * @param addressingProperties The current addressing context.
     * @return The response.
     * @throws SoapFault11 for errors during processing
     */
    public void lostCommitted(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault11
    {
        try
        {
            InteropUtil.registerDurable2PC(coordinationContext, new CommitFailureDurable2PCParticipant(), new Uid().toString()) ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault11(th) ;
        }
    }
}
