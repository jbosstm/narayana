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

import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.ServiceRegistry;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import com.arjuna.wst.BusinessActivityTerminator;
import com.jboss.transaction.txinterop.interop.states.BAInteropDroppedParticipantCompletedState;
import com.jboss.transaction.txinterop.interop.states.BAInteropParticipantCompletedState;
import com.jboss.transaction.txinterop.interop.states.BAInteropUnsolicitedCompleteState;
import com.jboss.transaction.txinterop.interop.states.InteropWaitState;
import com.jboss.transaction.txinterop.proxy.ProxyConversation;
import com.jboss.transaction.txinterop.proxy.ProxyURIRewriting;
import com.jboss.transaction.txinterop.webservices.bainterop.*;

/**
 * The BA endpoint test case
 * @author kevin
 */
public class BATestCase extends InteropTestCase
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
    public BATestCase()
    {
        // Default the service to the local service.
        setParticipantURI(getSourceParticipantURI()) ;
        setTestTimeout(120000) ;
        setAsyncTest(true) ;
    }

    /**
     * testBA1_1 - 1.1 Cancel
     * The IA cancels the activity before the PA completes its work on behalf of the activity.
     * @throws Exception on failure.
     */
    public void testBA1_1()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(BusinessActivityConstants.WSBA_ACTION_CANCELLED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = BAInteropUtil.createAtomicOutcomeCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().cancel(serviceURI, context) ;
            
            final BusinessActivityTerminator terminator = BAInteropUtil.registerTerminator(context, context.getIdentifier().getValue()) ;
            terminator.cancel() ;
            
            state.waitForCompletion(0) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * testBA1_2 - 1.2 Exit
     * The PA exits without completing any work on behalf of the activity.
     * @throws Exception on failure.
     */
    public void testBA1_2()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(BusinessActivityConstants.WSBA_ACTION_EXITED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = BAInteropUtil.createAtomicOutcomeCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().exit(serviceURI, context) ;
            
            state.waitForCompletion(getTestTimeout()) ;
            
            final BusinessActivityTerminator terminator = BAInteropUtil.registerTerminator(context, context.getIdentifier().getValue()) ;
            terminator.cancel() ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * testBA1_3 - 1.3 Fail
     * The PA fails before completing its work on behalf of the activity.
     * @throws Exception on failure.
     */
    public void testBA1_3()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(BusinessActivityConstants.WSBA_ACTION_FAILED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = BAInteropUtil.createAtomicOutcomeCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().fail(serviceURI, context) ;
            
            state.waitForCompletion(getTestTimeout()) ;
            
            final BusinessActivityTerminator terminator = BAInteropUtil.registerTerminator(context, context.getIdentifier().getValue()) ;
            terminator.cancel() ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * testBA1_4 - 1.4 CannotComplete
     * The PA sends CannotComplete before completing its work on behalf of the activity.
     * @throws Exception on failure.
     */
    public void testBA1_4()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(BusinessActivityConstants.WSBA_ACTION_NOT_COMPLETED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = BAInteropUtil.createAtomicOutcomeCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().cannotComplete(serviceURI, context) ;
            
            state.waitForCompletion(getTestTimeout()) ;
            
            final BusinessActivityTerminator terminator = BAInteropUtil.registerTerminator(context, context.getIdentifier().getValue()) ;
            terminator.cancel() ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * testBA1_5 - 1.5 ParticipantCompleteClose
     * The PA successfully completes its work on behalf of the activity and the activity is closed.
     * @throws Exception on failure.
     */
    public void testBA1_5()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final BAInteropParticipantCompletedState state = new BAInteropParticipantCompletedState(BusinessActivityConstants.WSBA_ACTION_CLOSED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = BAInteropUtil.createAtomicOutcomeCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().participantCompleteClose(serviceURI, context) ;

            assertTrue("Participant did not issue Completed", state.waitForParticipantCompleted(getTestTimeout())) ;
            
            final BusinessActivityTerminator terminator = BAInteropUtil.registerTerminator(context, context.getIdentifier().getValue()) ;
            terminator.close() ;
            
            state.waitForCompletion(0) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * testBA1_6 - 1.6 CoordinatorCompleteClose
     * The PA successfully completes its work on behalf of the activity and the activity is closed.
     * @throws Exception on failure.
     */
    public void testBA1_6()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(BusinessActivityConstants.WSBA_ACTION_CLOSED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = BAInteropUtil.createAtomicOutcomeCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().coordinatorCompleteClose(serviceURI, context) ;
            
            final BusinessActivityTerminator terminator = BAInteropUtil.registerTerminator(context, context.getIdentifier().getValue()) ;
            terminator.complete() ;
            terminator.close() ;
            
            state.waitForCompletion(0) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * testBA1_7 - 1.7 UnsolicitedComplete
     * Tests a protocol error, participant send Completed for coordinator completion.  Throws an invalid state fault.
     * @throws Exception on failure.
     *
     * this test relies on rewriting the protocol at registration time so that the returned endpoint
     * is for the coordinator completion coordinator rather than the participant completion coordinator.
     * the participant engine tries to use the endpoint to send an unsolicited complete to the coordinator
     * completion coordinator. unfortunately, since the endpoint nwo contains service and port metadata the
     * send fails. so this test has had to be decommissioned.
     */
    
    /*
    public void testBA1_7()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final BAInteropUnsolicitedCompleteState state = new BAInteropUnsolicitedCompleteState() ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = BAInteropUtil.createAtomicOutcomeCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().unsolicitedComplete(serviceURI, context) ;
            
            state.waitForCompletion(getTestTimeout()) ;
            
            final BusinessActivityTerminator terminator = BAInteropUtil.registerTerminator(context, context.getIdentifier().getValue()) ;
            terminator.cancel() ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }
    */
//
    /**
     * testBA1_8 - 1.8 Compensate
     * The PA successfully completes its work on behalf of the activity and the activity is compensated.
     * @throws Exception on failure.
     */
    public void testBA1_8()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final BAInteropParticipantCompletedState state = new BAInteropParticipantCompletedState(BusinessActivityConstants.WSBA_ACTION_COMPENSATED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = BAInteropUtil.createAtomicOutcomeCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().participantCompleteClose(serviceURI, context) ;

            assertTrue("Participant did not issue Completed", state.waitForParticipantCompleted(getTestTimeout())) ;
            
            final BusinessActivityTerminator terminator = BAInteropUtil.registerTerminator(context, context.getIdentifier().getValue()) ;
            terminator.cancel() ;
            
            state.waitForCompletion(0) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * testBA1_9 - 1.9 CompensationFail
     * The PA successfully completes its work on behalf of the activity but compensation fails..
     * @throws Exception on failure.
     */
    public void testBA1_9()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final BAInteropParticipantCompletedState state = new BAInteropParticipantCompletedState(BusinessActivityConstants.WSBA_ACTION_FAILED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = BAInteropUtil.createAtomicOutcomeCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().compensationFail(serviceURI, context) ;

            assertTrue("Participant did not issue Completed", state.waitForParticipantCompleted(getTestTimeout())) ;
            
            final BusinessActivityTerminator terminator = BAInteropUtil.registerTerminator(context, context.getIdentifier().getValue()) ;
            terminator.cancel() ;
            
            state.waitForCompletion(getTestTimeout()) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * testBA1_10 - 1.10 ParticipantCancelCompletedRace
     * Simulate a race between cancel and completed.
     * @throws Exception on failure.
     */
    public void testBA1_10()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final BAInteropDroppedParticipantCompletedState state = new BAInteropDroppedParticipantCompletedState(BusinessActivityConstants.WSBA_ACTION_COMPENSATED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = BAInteropUtil.createAtomicOutcomeCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().participantCancelCompletedRace(serviceURI, context) ;
            
            assertTrue("Participant did not issue Completed", state.waitForParticipantCompleted(getTestTimeout())) ;
            
            final BusinessActivityTerminator terminator = BAInteropUtil.registerTerminator(context, context.getIdentifier().getValue()) ;
            terminator.cancel() ;
            
            state.waitForCompletion(getTestTimeout()) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

    /**
     * testBA1_11 - 1.11 MessageLossAndRecovery
     * The IA cancels the activity before the PA completes its work on behalf of the activity.
     * @throws Exception on failure.
     */
    public void testBA1_11()
        throws Exception
    {
        final String conversationId = getConversationId() ;
        final InteropWaitState state = new InteropWaitState(BusinessActivityConstants.WSBA_ACTION_COMPENSATED) ;
        ProxyConversation.setConversationState(conversationId, state) ;
        try
        {
            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
            final CoordinationContextType context = BAInteropUtil.createAtomicOutcomeCoordinationContext(getSourceCoordinatorURI()) ;
            getParticipantStub().messageLossAndRecovery(serviceURI, context) ;
            
            final BusinessActivityTerminator terminator = BAInteropUtil.registerTerminator(context, context.getIdentifier().getValue()) ;
            terminator.complete() ;
            terminator.cancel() ;
            
            state.waitForCompletion(0) ;
        }
        finally
        {
            ProxyConversation.clearConversationState(conversationId) ;
        }
        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
    }

//    /**
//     * testBA1_12 - 1.12 MixedOutcome
//     * The IA cancels the activity before the PA completes its work on behalf of the activity.
//     * @throws Exception on failure.
//     */
//    public void testBA1_12()
//        throws Exception
//    {
//        final String conversationId = getConversationId() ;
//        final ATInteropWaitState state = new ATInteropWaitState(BusinessActivityConstants.WSAT_ACTION_COMMITTED) ;
//        ProxyConversation.setConversationState(conversationId, state) ;
//        try
//        {
//            final String serviceURI = ProxyURIRewriting.rewriteURI(conversationId, getParticipantURI()) ;
//            final CoordinationContextType context = BAInteropUtil.createMixedOutcomeCoordinationContext(getSourceCoordinatorURI()) ;
//            getParticipantStub().commit(serviceURI, context) ;
//            
//            final CompletionCoordinatorParticipant participant = BAInteropUtil.registerCompletion(context, context.getIdentifier().getValue()) ;
//            participant.commit() ;
//            
//            state.waitForCompletion(getTestTimeout()) ;
//        }
//        finally
//        {
//            ProxyConversation.clearConversationState(conversationId) ;
//        }
//        assertTrue("Conversation did not complete successfully", state.isSuccessful()) ;
//    }

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
        SOURCE_PARTICIPANT_URI = serviceRegistry.getServiceURI(BAInteropConstants.SERVICE_PARTICIPANT) ;
        SOURCE_COORDINATOR_URI = serviceRegistry.getServiceURI(CoordinationConstants.ACTIVATION_SERVICE_NAME) ;
    }
}
