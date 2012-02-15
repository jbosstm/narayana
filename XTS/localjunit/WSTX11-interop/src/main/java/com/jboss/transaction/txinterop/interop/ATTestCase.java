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
package com.jboss.transaction.txinterop.interop;

import javax.inject.Named;

import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.ServiceRegistry;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import com.arjuna.wst.CompletionCoordinatorParticipant;
import com.arjuna.wst.TransactionRolledBackException;
import com.jboss.transaction.txinterop.interop.states.ATInteropPreparedAfterTimeoutState;
import com.jboss.transaction.txinterop.interop.states.ATInteropRetryPreparedAbortState;
import com.jboss.transaction.txinterop.interop.states.ATInteropRetryPreparedCommitState;
import com.jboss.transaction.txinterop.interop.states.InteropWaitState;
import com.jboss.transaction.txinterop.proxy.ProxyConversation;
import com.jboss.transaction.txinterop.proxy.ProxyURIRewriting;
import com.jboss.transaction.txinterop.webservices.atinterop.*;

/**
 * The AT endpoint test case
 * @author kevin
 */
public class ATTestCase extends InteropTestCase
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
    public ATTestCase()
    {
        // Default the service to the local service.
        setParticipantURI(getSourceParticipantURI()) ;
        setTestTimeout(120000) ;
        setAsyncTest(true) ;
    }

    /**
     * testAT1_1 - 1.1 CompletionCommit
     * Participant creates and commits a transaction using the initiator's coordinator.
     * @throws Exception on failure.
     */
    public void testAT1_1()
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
     * testAT1_2 - 1.2 CompletionRollback
     * Participant creates and rolls back a transaction using the initiator's coordinator.
     * @throws Exception on failure.
     */
    public void testAT1_2()
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
    public void testAT2_1()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_COMMITTED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = ATInteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().commit(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = ATInteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
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
     * testAT2_2 - 2.2 Rollback
     * Participant registers for Durable2PC, initiator rolls back.
     * @throws Exception on failure.
     */
    public void testAT2_2()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_ABORTED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = ATInteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().rollback(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = ATInteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
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
     * testAT3_1 - 3.1 Phase2Rollback
     * Coordinator aborts the transaction due to an Aborted vote during the prepare phase. 
     * @throws Exception on failure.
     */
    public void testAT3_1()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_ABORTED, 2) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = ATInteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().phase2Rollback(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = ATInteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
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
     * testAT3_2 - 3.2 Readonly
     * Tests coordinator committing a transaction with a read only participant. 
     * @throws Exception on failure.
     */
    public void testAT3_2()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_COMMITTED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = ATInteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().readonly(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = ATInteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
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
     * testAT3_3 - 3.3 VolatileAndDurable
     * Tests registration during the volatile prepare phase. 
     * @throws Exception on failure.
     */
    public void testAT3_3()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_COMMITTED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = ATInteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().volatileAndDurable(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = ATInteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
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
     * testAT4_1 - 4.1 EarlyReadonly
     * Tests the case of a participant initiated ReadOnly message occurring prior to the prepare phase. 
     * @throws Exception on failure.
     */
    public void testAT4_1()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_COMMITTED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = ATInteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().earlyReadonly(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = ATInteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
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
     * testAT4_2 - 4.2 EarlyAborted
     * Tests the case of a participant initiated Aborted message occurring prior to the prepare phase. 
     * @throws Exception on failure.
     */
    public void testAT4_2()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_ABORTED, 2) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = ATInteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().earlyAborted(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = ATInteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
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
     * testAT5_1 - 5.1 ReplayCommit
     * Participant registers for Durable2PC, initator initiates commit, participant fails after prepared, recovers and resends prepared. Transaction commits normally.
     * @throws Exception on failure.
     */
    public void testAT5_1()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_COMMITTED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = ATInteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().replayCommit(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = ATInteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
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
     * testAT5_2 - 5.2 RetryPreparedCommit
     * Tests recovery from a communication failure during the prepare phase. Transaction commits normally.
     * @throws Exception on failure.
     */
    public void testAT5_2()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final ATInteropRetryPreparedCommitState state = new ATInteropRetryPreparedCommitState() ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = ATInteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().retryPreparedCommit(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = ATInteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
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
     * testAT5_3 - 5.3 RetryPreparedAbort
     * Tests recovery from a communication failure during the prepare phase. Transaction times out.
     * @throws Exception on failure.
     */
    public void testAT5_3()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final ATInteropRetryPreparedAbortState state = new ATInteropRetryPreparedAbortState() ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = ATInteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().retryPreparedAbort(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = ATInteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
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
     * testAT5_4 - 5.4 RetryCommit
     * Tests recovery from a communication failure during the commit phase. Transaction commits normally.
     * @throws Exception on failure.
     */
    public void testAT5_4()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_COMMITTED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = ATInteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().retryCommit(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = ATInteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
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
     * testAT5_5 - 5.5 PreparedAfterTimeout
     * Tests recovery from a communication failure during the prepare phase. Transaction times out. Two participants, volatile and durable.
     * @throws Exception on failure.
     */
    public void testAT5_5()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final ATInteropPreparedAfterTimeoutState state = new ATInteropPreparedAfterTimeoutState() ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = ATInteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().preparedAfterTimeout(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = ATInteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
            participant.commit() ;
            fail("Transaction rollback expected") ;
        }
        catch (final TransactionRolledBackException tre)
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
     * testAT5_6 - 5.6 LostCommitted
     * Tests the effect of a lost Committed message.
     * @throws Exception on failure.
     */
    public void testAT5_6()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(AtomicTransactionConstants.WSAT_ACTION_COMMITTED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = ATInteropUtil.createCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().lostCommitted(serviceURI, context) ;
            
            final CompletionCoordinatorParticipant participant = ATInteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
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
        SOURCE_PARTICIPANT_URI = serviceRegistry.getServiceURI(ATInteropConstants.SERVICE_PARTICIPANT) ;
        SOURCE_COORDINATOR_URI = serviceRegistry.getServiceURI(CoordinationConstants.ACTIVATION_SERVICE_NAME) ;
    }
}
