/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package com.jboss.transaction.wstf.interop;

import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.ServiceRegistry;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import com.arjuna.wst.CompletionCoordinatorParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.jboss.transaction.wstf.interop.states.Sc007PreparedAfterTimeoutState;
import com.jboss.transaction.wstf.interop.states.Sc007RetryPreparedAbortState;
import com.jboss.transaction.wstf.interop.states.Sc007RetryPreparedCommitState;
import com.jboss.transaction.wstf.interop.states.InteropWaitState;
import com.jboss.transaction.wstf.proxy.ProxyConversation;
import com.jboss.transaction.wstf.proxy.ProxyURIRewriting;
import com.jboss.transaction.wstf.webservices.sc007.InteropUtil;
import com.jboss.transaction.wstf.webservices.sc007.ParticipantStub;
import com.jboss.transaction.wstf.webservices.sc007.*;
import com.jboss.transaction.wstf.webservices.sc007.SyncParticipantStub;

/**
 * The AT endpoint test case
 * @author kevin
 */
public class Sc007TestCase extends InteropTestCase
{
    /**
     * The coordinator URI of the source.
     */
    private static final String SOURCE_COORDINATOR_URI ;
    /**
     * The service URI of the participant.
     */
    private static final String SOURCE_PARTICIPANT_URI ;
    
    /**
     * Construct the named test case.
     */
    public Sc007TestCase()
    {
        // Default the service to the local service.
        setParticipantURI(getSourceParticipantURI()) ;
        setTestTimeout(120000) ;
        setAsyncTest(true) ;
    }

    /**
     * test1_1 - 1.1 CompletionCommit
     * Participant creates and commits a transaction using the initiator's coordinator.
     * @throws Exception on failure.
     */
    public void test1_1()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_COMMITTED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final String coordinatorURI = ProxyURIRewriting.rewriteURI(ProxyConversation.getAlternateConversationId(conversationId), getSourceCoordinatorURI()) ;
            getParticipantStub().completionCommit(serviceURI, coordinatorURI) ;
            state.waitForCompletion(getTestTimeout()) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * test1_2 - 1.2 CompletionRollback
     * Participant creates and rolls back a transaction using the initiator's coordinator.
     * @throws Exception on failure.
     */
    public void test1_2()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_ABORTED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final String coordinatorURI = ProxyURIRewriting.rewriteURI(ProxyConversation.getAlternateConversationId(conversationId), getSourceCoordinatorURI()) ;
            getParticipantStub().completionRollback(serviceURI, coordinatorURI) ;
            state.waitForCompletion(getTestTimeout()) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * testAT2_1 - 2.1 Commit
     * Participant registers for Durable2PC, initiator commits, transaction commits successfully.
     * @throws Exception on failure.
     */
    public void test2_1()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_COMMITTED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = InteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().commit(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = InteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
            participant.commit() ;
            
            state.waitForCompletion(0) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * test2_2 - 2.2 Rollback
     * Participant registers for Durable2PC, initiator rolls back.
     * @throws Exception on failure.
     */
    public void test2_2()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_ABORTED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = InteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().rollback(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = InteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
            participant.rollback() ;
            
            state.waitForCompletion(0) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * test3_1 - 3.1 Phase2Rollback
     * Coordinator aborts the transaction due to an Aborted vote during the prepare phase. 
     * @throws Exception on failure.
     */
    public void test3_1()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_ABORTED, 2) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = InteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().phase2Rollback(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = InteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
            participant.commit() ;
            fail("Transaction rollback expected") ;
        }
        catch (final TransactionRolledBackException trbe)
        {
            state.waitForCompletion(0) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * test3_2 - 3.2 Readonly
     * Tests coordinator committing a transaction with a read only participant. 
     * @throws Exception on failure.
     */
    public void test3_2()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_COMMITTED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = InteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().readonly(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = InteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
            participant.commit() ;
            
            state.waitForCompletion(0) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * test3_3 - 3.3 VolatileAndDurable
     * Tests registration during the volatile prepare phase. 
     * @throws Exception on failure.
     */
    public void test3_3()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_COMMITTED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = InteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().volatileAndDurable(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = InteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
            participant.commit() ;
            
            state.waitForCompletion(0) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * test3_4 - 3.4 EarlyReadonly
     * Tests the case of a participant initiated ReadOnly message occurring prior to the prepare phase. 
     * @throws Exception on failure.
     */
    public void test3_4()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_COMMITTED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = InteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().earlyReadonly(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = InteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
            participant.commit() ;
            
            state.waitForCompletion(0) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * test3_5 - 3.5 EarlyAborted
     * Tests the case of a participant initiated Aborted message occurring prior to the prepare phase. 
     * @throws Exception on failure.
     */
    public void test3_5()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_ABORTED, 2) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = InteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().earlyAborted(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = InteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
            participant.commit() ;
            fail("Transaction rollback expected") ;
        }
        catch (final TransactionRolledBackException trbe)
        {
            state.waitForCompletion(0) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * test3_6 - 3.6 ReplayCommit
     * Participant registers for Durable2PC, initator initiates commit, participant fails after prepared, recovers and resends prepared. Transaction commits normally.
     * @throws Exception on failure.
     */
    public void test3_6()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_COMMITTED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = InteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().replayCommit(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = InteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
            participant.commit() ;
            
            state.waitForCompletion(0) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * test3_7 - 3.7 RetryPreparedCommit
     * Tests recovery from a communication failure during the prepare phase. Transaction commits normally.
     * @throws Exception on failure.
     */
    public void test3_7()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final Sc007RetryPreparedCommitState state = new Sc007RetryPreparedCommitState() ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = InteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().retryPreparedCommit(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = InteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
            participant.commit() ;
            
            state.waitForCompletion(0) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * test3_8 - 3.8 RetryPreparedAbort
     * Tests recovery from a communication failure during the prepare phase. Transaction times out.
     * @throws Exception on failure.
     */
    public void test3_8()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final Sc007RetryPreparedAbortState state = new Sc007RetryPreparedAbortState() ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = InteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().retryPreparedAbort(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = InteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
            participant.commit() ;
            fail("Transaction rollback expected") ;
        }
        catch (final TransactionRolledBackException trbe)
        {
            state.waitForCompletion(getTestTimeout()) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * test3_9 - 3.9 RetryCommit
     * Tests recovery from a communication failure during the commit phase. Transaction commits normally.
     * @throws Exception on failure.
     */
    public void test3_9()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_COMMITTED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = InteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().retryCommit(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = InteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
            participant.commit() ;
            
            state.waitForCompletion(0) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * test3_10 - 3.10 PreparedAfterTimeout
     * Tests recovery from a communication failure during the prepare phase. Transaction times out. Two participants, volatile and durable.
     * @throws Exception on failure.
     */
    public void test3_10()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final Sc007PreparedAfterTimeoutState state = new Sc007PreparedAfterTimeoutState() ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = InteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().preparedAfterTimeout(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = InteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
            participant.commit() ;
            fail("Transaction rollback expected") ;
        }
        catch (final TransactionRolledBackException trbe)
        {
            state.waitForCompletion(getTestTimeout()) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * test3_11 - 3.11 LostCommitted
     * Tests the effect of a lost Committed message.
     * @throws Exception on failure.
     */
    public void test3_11()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_COMMITTED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = InteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().lostCommitted(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = InteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
            participant.commit() ;
            
            state.waitForCompletion(0) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }
    
    /**
     * Get the source coordinator URI.
     * @return The source coordinator uri.
     */
    protected static String getSourceCoordinatorURI()
    {
        return SOURCE_COORDINATOR_URI ;
    }
    
    /**
     * Get the source participant URI.
     * @return The source participant uri.
     */
    protected static String getSourceParticipantURI()
    {
        return SOURCE_PARTICIPANT_URI ;
    }
    
    /**
     * Get the participant stub for the test.
     * @return The participant stub.
     */
    private ParticipantStub getParticipantStub()
    {
        return (getAsyncTest() ? AsyncParticipantStub.getParticipantStub() : SyncParticipantStub.getParticipantStub()) ;
    }
    
    static
    {
        final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
        SOURCE_PARTICIPANT_URI = serviceRegistry.getServiceURI(InteropConstants.SERVICE_PARTICIPANT) ;
        SOURCE_COORDINATOR_URI = serviceRegistry.getServiceURI(CoordinationConstants.ACTIVATION_SERVICE_NAME) ;
    }
}
