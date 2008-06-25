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
package com.arjuna.wst.tests;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

// import com.arjuna.webservices.wsarjtx.processors.ParticipantManagerParticipantProcessor;
import com.arjuna.webservices.wsarjtx.processors.TerminationCoordinatorProcessor;
import com.arjuna.wst.Participant;
import com.arjuna.webservices.wsat.processors.CompletionCoordinatorProcessor;
import com.arjuna.webservices.wsat.processors.ParticipantProcessor;
import com.arjuna.webservices.wsat.ParticipantInboundEvents;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wsba.processors.CoordinatorCompletionParticipantProcessor;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.webservices.wsba.ParticipantCompletionParticipantInboundEvents;
import com.arjuna.webservices.wsba.BusinessActivityConstants;
import com.arjuna.webservices.wsba.CoordinatorCompletionParticipantInboundEvents;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.wst.BAParticipantManager;
import com.arjuna.wst.BusinessActivityTerminator;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.CompletionCoordinatorParticipant;
import com.arjuna.wst.messaging.engines.ParticipantEngine;
import com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine;
import com.arjuna.wst.messaging.engines.CoordinatorCompletionParticipantEngine;

/**
 * Initialise the test.
 * @author kevin
 */
public class TestInitialisation implements ServletContextListener
{
    private final CompletionCoordinatorParticipant testNoExceptionCompletionCoordinatorParticipant                 = new TestNoExceptionCompletionCoordinatorParticipant();
    private final CompletionCoordinatorParticipant testTransactionRolledExceptionCompletionCoordinatorParticipant  = new TestTransactionRolledBackExceptionCompletionCoordinatorParticipant();
    private final CompletionCoordinatorParticipant testUnknownTransactionExceptionCompletionCoordinatorParticipant = new TestUnknownTransactionExceptionCompletionCoordinatorParticipant();
    private final CompletionCoordinatorParticipant testSystemExceptionCompletionCoordinatorParticipant             = new TestSystemExceptionCompletionCoordinatorParticipant();

    private final Participant testPreparedVoteParticipant                   = new TestPreparedVoteParticipant();
    private final Participant testAbortedVoteParticipant                    = new TestAbortedVoteParticipant();
    private final Participant testReadOnlyParticipant                       = new TestReadOnlyVoteParticipant();
    private final Participant testNoExceptionParticipant                    = new TestNoExceptionParticipant();
    private final Participant testTransactionRolledBackExceptionParticipant = new TestTransactionRolledBackExceptionParticipant();
    private final Participant testWrongStateExceptionParticipant            = new TestWrongStateExceptionParticipant();
    private final Participant testSystemExceptionParticipant                = new TestSystemExceptionParticipant();

    private ParticipantInboundEvents testPreparedVoteParticipantEngine;
    private ParticipantInboundEvents testAbortedVoteParticipantEngine;
    private ParticipantInboundEvents testReadOnlyParticipantEngine;
    private ParticipantInboundEvents testNoExceptionParticipantEngine;
    private ParticipantInboundEvents testTransactionRolledBackExceptionParticipantEngine;
    private ParticipantInboundEvents testWrongStateExceptionParticipantEngine;
    private ParticipantInboundEvents testSystemExceptionParticipantEngine;

    private final BusinessAgreementWithParticipantCompletionParticipant testSystemExceptionBusinessAgreementWithParticipantCompletionParticipant = new TestSystemExceptionBusinessAgreementWithParticipantCompletionParticipant();
    private final BusinessAgreementWithParticipantCompletionParticipant testWrongStateExceptionBusinessAgreementWithParticipantCompletionParticipant = new TestWrongStateExceptionBusinessAgreementWithParticipantCompletionParticipant();
    private final BusinessAgreementWithParticipantCompletionParticipant testNoExceptionBusinessAgreementWithParticipantCompletionParticipant = new TestNoExceptionBusinessAgreementWithParticipantCompletionParticipant();
    private final BusinessAgreementWithParticipantCompletionParticipant testFaultedExceptionBusinessAgreementWithParticipantCompletionParticipant = new TestFaultedExceptionBusinessAgreementWithParticipantCompletionParticipant();

    private ParticipantCompletionParticipantInboundEvents testSystemExceptionBusinessAgreementWithParticipantCompletionParticipantEngine;
    private ParticipantCompletionParticipantInboundEvents testWrongStateExceptionBusinessAgreementWithParticipantCompletionParticipantEngine;
    private ParticipantCompletionParticipantInboundEvents testNoExceptionBusinessAgreementWithParticipantCompletionParticipantEngine;
    private ParticipantCompletionParticipantInboundEvents testFaultedExceptionBusinessAgreementWithParticipantCompletionParticipantEngine;

    private final BusinessAgreementWithCoordinatorCompletionParticipant testSystemExceptionBusinessAgreementWithCoordinatorCompletionParticipant = new TestSystemExceptionBusinessAgreementWithCoordinatorCompletionParticipant();
    private final BusinessAgreementWithCoordinatorCompletionParticipant testWrongStateExceptionBusinessAgreementWithCoordinatorCompletionParticipant = new TestWrongStateExceptionBusinessAgreementWithCoordinatorCompletionParticipant();
    private final BusinessAgreementWithCoordinatorCompletionParticipant testNoExceptionBusinessAgreementWithCoordinatorCompletionParticipant = new TestNoExceptionBusinessAgreementWithCoordinatorCompletionParticipant();
    private final BusinessAgreementWithCoordinatorCompletionParticipant testFaultedExceptionBusinessAgreementWithCoordinatorCompletionParticipant = new TestFaultedExceptionBusinessAgreementWithCoordinatorCompletionParticipant();

    private CoordinatorCompletionParticipantInboundEvents testSystemExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine;
    private CoordinatorCompletionParticipantInboundEvents testWrongStateExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine;
    private CoordinatorCompletionParticipantInboundEvents testNoExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine;
    private CoordinatorCompletionParticipantInboundEvents testFaultedExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine;

    private final BAParticipantManager testNoExceptionBAPMParticipant                    = new TestNoExceptionBAPMParticipant();
    private final BAParticipantManager testWrongStateExceptionBAPMParticipant            = new TestWrongStateExceptionBAPMParticipant();
    private final BAParticipantManager testSystemExceptionBAPMParticipant                = new TestSystemExceptionBAPMParticipant();

    private final BusinessActivityTerminator testNoExceptionBusinessActivityTerminator = new TestNoExceptionBusinessActivityTerminator();
    private final BusinessActivityTerminator testUnknownTransactionExceptionBusinessActivityTerminator = new TestUnknownTransactionExceptionBusinessActivityTerminator();
    private final BusinessActivityTerminator testTransactionRolledBackExceptionBusinessActivityTerminator = new TestTransactionRolledBackExceptionBusinessActivityTerminator();
    private final BusinessActivityTerminator testSystemExceptionBusinessActivityTerminator = new TestSystemExceptionBusinessActivityTerminator();

    /**
     * The context has been initialized.
     * @param servletContextEvent The servlet context event.
     */
    public void contextInitialized(final ServletContextEvent servletContextEvent)
    {
        final CompletionCoordinatorProcessor completionCoordinatorProcessor = CompletionCoordinatorProcessor.getProcessor() ;
        completionCoordinatorProcessor.activateParticipant(testNoExceptionCompletionCoordinatorParticipant, TestUtil.NOEXCEPTION_TRANSACTION_IDENTIFIER);
        completionCoordinatorProcessor.activateParticipant(testTransactionRolledExceptionCompletionCoordinatorParticipant, TestUtil.TRANSACTIONROLLEDBACKEXCEPTION_TRANSACTION_IDENTIFIER);
        completionCoordinatorProcessor.activateParticipant(testUnknownTransactionExceptionCompletionCoordinatorParticipant, TestUtil.UNKNOWNTRANSACTIONEXCEPTION_TRANSACTION_IDENTIFIER);
        completionCoordinatorProcessor.activateParticipant(testSystemExceptionCompletionCoordinatorParticipant, TestUtil.SYSTEMEXCEPTION_TRANSACTION_IDENTIFIER);

        final ParticipantProcessor participantProcessor = ParticipantProcessor.getProcessor() ;
        final AttributedURIType coordinatorURI = new AttributedURIType(SoapRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.SERVICE_COORDINATOR));

        testPreparedVoteParticipantEngine = new ParticipantEngine(testPreparedVoteParticipant, TestUtil.PREPAREDVOTE_PARTICIPANT_IDENTIFIER, new EndpointReferenceType(coordinatorURI));
        testAbortedVoteParticipantEngine = new ParticipantEngine(testAbortedVoteParticipant, TestUtil.ABORTEDVOTE_PARTICIPANT_IDENTIFIER, new EndpointReferenceType(coordinatorURI));
        testReadOnlyParticipantEngine = new ParticipantEngine(testReadOnlyParticipant, TestUtil.READONLYVOTE_PARTICIPANT_IDENTIFIER, new EndpointReferenceType(coordinatorURI));
        testNoExceptionParticipantEngine = new ParticipantEngine(testNoExceptionParticipant, TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER, new EndpointReferenceType(coordinatorURI));
        testTransactionRolledBackExceptionParticipantEngine = new ParticipantEngine(testTransactionRolledBackExceptionParticipant, TestUtil.TRANSACTIONROLLEDBACKEXCEPTION_PARTICIPANT_IDENTIFIER, new EndpointReferenceType(coordinatorURI));
        testWrongStateExceptionParticipantEngine = new ParticipantEngine(testWrongStateExceptionParticipant, TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER, new EndpointReferenceType(coordinatorURI));
        testSystemExceptionParticipantEngine = new ParticipantEngine(testSystemExceptionParticipant, TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER, new EndpointReferenceType(coordinatorURI));

        participantProcessor.activateParticipant(testPreparedVoteParticipantEngine, TestUtil.PREPAREDVOTE_PARTICIPANT_IDENTIFIER);
        participantProcessor.activateParticipant(testAbortedVoteParticipantEngine, TestUtil.ABORTEDVOTE_PARTICIPANT_IDENTIFIER);
        participantProcessor.activateParticipant(testReadOnlyParticipantEngine, TestUtil.READONLYVOTE_PARTICIPANT_IDENTIFIER);
        participantProcessor.activateParticipant(testNoExceptionParticipantEngine, TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantProcessor.activateParticipant(testTransactionRolledBackExceptionParticipantEngine, TestUtil.TRANSACTIONROLLEDBACKEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantProcessor.activateParticipant(testWrongStateExceptionParticipantEngine, TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantProcessor.activateParticipant(testSystemExceptionParticipantEngine, TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER);

        final ParticipantCompletionParticipantProcessor participantCompletionParticipantProcessor = ParticipantCompletionParticipantProcessor.getProcessor() ;
        final AttributedURIType participantCompletionCoordinatorURI = new AttributedURIType(SoapRegistry.getRegistry().getServiceURI(BusinessActivityConstants.SERVICE_PARTICIPANT_COMPLETION_COORDINATOR));

        testSystemExceptionBusinessAgreementWithParticipantCompletionParticipantEngine = new ParticipantCompletionParticipantEngine(TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER, new EndpointReferenceType(participantCompletionCoordinatorURI), testSystemExceptionBusinessAgreementWithParticipantCompletionParticipant);
        testWrongStateExceptionBusinessAgreementWithParticipantCompletionParticipantEngine = new ParticipantCompletionParticipantEngine(TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER, new EndpointReferenceType(participantCompletionCoordinatorURI), testWrongStateExceptionBusinessAgreementWithParticipantCompletionParticipant);
        testNoExceptionBusinessAgreementWithParticipantCompletionParticipantEngine = new ParticipantCompletionParticipantEngine(TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER, new EndpointReferenceType(participantCompletionCoordinatorURI), testNoExceptionBusinessAgreementWithParticipantCompletionParticipant);
        testFaultedExceptionBusinessAgreementWithParticipantCompletionParticipantEngine = new ParticipantCompletionParticipantEngine(TestUtil.FAULTEDEXCEPTION_PARTICIPANT_IDENTIFIER, new EndpointReferenceType(participantCompletionCoordinatorURI), testFaultedExceptionBusinessAgreementWithParticipantCompletionParticipant);

        participantCompletionParticipantProcessor.activateParticipant(testSystemExceptionBusinessAgreementWithParticipantCompletionParticipantEngine, TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantCompletionParticipantProcessor.activateParticipant(testWrongStateExceptionBusinessAgreementWithParticipantCompletionParticipantEngine, TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantCompletionParticipantProcessor.activateParticipant(testNoExceptionBusinessAgreementWithParticipantCompletionParticipantEngine, TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantCompletionParticipantProcessor.activateParticipant(testFaultedExceptionBusinessAgreementWithParticipantCompletionParticipantEngine, TestUtil.FAULTEDEXCEPTION_PARTICIPANT_IDENTIFIER);

        final CoordinatorCompletionParticipantProcessor coordinatorCompletionParticipantProcessor = CoordinatorCompletionParticipantProcessor.getProcessor() ;
        // !!! is this not supposed to be BusinessActivityConstants.SERVICE_COORDINATOR_COMPLETION_COORDINATOR
        final AttributedURIType coordinatorCompletionCoordinatorURI = new AttributedURIType(SoapRegistry.getRegistry().getServiceURI(BusinessActivityConstants.SERVICE_PARTICIPANT_COMPLETION_COORDINATOR));

        testSystemExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine = new CoordinatorCompletionParticipantEngine(TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER, new EndpointReferenceType(coordinatorCompletionCoordinatorURI), testSystemExceptionBusinessAgreementWithCoordinatorCompletionParticipant);
        testWrongStateExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine = new CoordinatorCompletionParticipantEngine(TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER, new EndpointReferenceType(coordinatorCompletionCoordinatorURI), testWrongStateExceptionBusinessAgreementWithCoordinatorCompletionParticipant);
        testNoExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine = new CoordinatorCompletionParticipantEngine(TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER, new EndpointReferenceType(coordinatorCompletionCoordinatorURI), testNoExceptionBusinessAgreementWithCoordinatorCompletionParticipant);
        testFaultedExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine = new CoordinatorCompletionParticipantEngine(TestUtil.FAULTEDEXCEPTION_PARTICIPANT_IDENTIFIER, new EndpointReferenceType(coordinatorCompletionCoordinatorURI), testFaultedExceptionBusinessAgreementWithCoordinatorCompletionParticipant);


        coordinatorCompletionParticipantProcessor.activateParticipant(testSystemExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine, TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER);
        coordinatorCompletionParticipantProcessor.activateParticipant(testWrongStateExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine, TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER);
        coordinatorCompletionParticipantProcessor.activateParticipant(testNoExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine, TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER);
        coordinatorCompletionParticipantProcessor.activateParticipant(testFaultedExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine, TestUtil.FAULTEDEXCEPTION_PARTICIPANT_IDENTIFIER);

        // final ParticipantManagerParticipantProcessor participantManagerParticipantProcessor = ParticipantManagerParticipantProcessor.getParticipant() ;
        // participantManagerParticipantProcessor.activateParticipant(testNoExceptionBAPMParticipant, TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER);
        // participantManagerParticipantProcessor.activateParticipant(testWrongStateExceptionBAPMParticipant, TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER);
        // participantManagerParticipantProcessor.activateParticipant(testSystemExceptionBAPMParticipant, TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER);

        final TerminationCoordinatorProcessor terminatorParticipantProcessor = TerminationCoordinatorProcessor.getProcessor() ;
        terminatorParticipantProcessor.activateParticipant(testNoExceptionBusinessActivityTerminator, TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER);
        terminatorParticipantProcessor.activateParticipant(testTransactionRolledBackExceptionBusinessActivityTerminator, TestUtil.TRANSACTIONROLLEDBACKEXCEPTION_TRANSACTION_IDENTIFIER);
        terminatorParticipantProcessor.activateParticipant(testUnknownTransactionExceptionBusinessActivityTerminator, TestUtil.UNKNOWNTRANSACTIONEXCEPTION_TRANSACTION_IDENTIFIER);
        terminatorParticipantProcessor.activateParticipant(testSystemExceptionBusinessActivityTerminator, TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER);
    }

    /**
     * The context is about to be destroyed.
     * @param servletContextEvent The servlet context event.
     */
    public void contextDestroyed(final ServletContextEvent servletContextEvent)
    {
        final CompletionCoordinatorProcessor completionCoordinatorProcessor = CompletionCoordinatorProcessor.getProcessor() ;
        completionCoordinatorProcessor.deactivateParticipant(testNoExceptionCompletionCoordinatorParticipant);
        completionCoordinatorProcessor.deactivateParticipant(testTransactionRolledExceptionCompletionCoordinatorParticipant);
        completionCoordinatorProcessor.deactivateParticipant(testUnknownTransactionExceptionCompletionCoordinatorParticipant);
        completionCoordinatorProcessor.deactivateParticipant(testSystemExceptionCompletionCoordinatorParticipant);

        final ParticipantProcessor participantProcessor = ParticipantProcessor.getProcessor() ;
        participantProcessor.deactivateParticipant(testPreparedVoteParticipantEngine);
        participantProcessor.deactivateParticipant(testAbortedVoteParticipantEngine);
        participantProcessor.deactivateParticipant(testReadOnlyParticipantEngine);
        participantProcessor.deactivateParticipant(testNoExceptionParticipantEngine);
        participantProcessor.deactivateParticipant(testTransactionRolledBackExceptionParticipantEngine);
        participantProcessor.deactivateParticipant(testWrongStateExceptionParticipantEngine);
        participantProcessor.deactivateParticipant(testSystemExceptionParticipantEngine);
        
        final ParticipantCompletionParticipantProcessor participantCompletionParticipantProcessor = ParticipantCompletionParticipantProcessor.getProcessor() ;
        participantCompletionParticipantProcessor.deactivateParticipant(testSystemExceptionBusinessAgreementWithParticipantCompletionParticipantEngine);
        participantCompletionParticipantProcessor.deactivateParticipant(testWrongStateExceptionBusinessAgreementWithParticipantCompletionParticipantEngine);
        participantCompletionParticipantProcessor.deactivateParticipant(testNoExceptionBusinessAgreementWithParticipantCompletionParticipantEngine);
        participantCompletionParticipantProcessor.deactivateParticipant(testFaultedExceptionBusinessAgreementWithParticipantCompletionParticipantEngine);

        final CoordinatorCompletionParticipantProcessor coordinatorCompletionParticipantProcessor = CoordinatorCompletionParticipantProcessor.getProcessor() ;
        coordinatorCompletionParticipantProcessor.deactivateParticipant(testSystemExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine);
        coordinatorCompletionParticipantProcessor.deactivateParticipant(testWrongStateExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine);
        coordinatorCompletionParticipantProcessor.deactivateParticipant(testNoExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine);
        coordinatorCompletionParticipantProcessor.deactivateParticipant(testFaultedExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine);

        // final ParticipantManagerParticipantProcessor participantManagerParticipantProcessor = ParticipantManagerParticipantProcessor.getParticipant() ;
        // participantManagerParticipantProcessor.deactivateParticipant(testNoExceptionBAPMParticipant);
        // participantManagerParticipantProcessor.deactivateParticipant(testWrongStateExceptionBAPMParticipant);
        // participantManagerParticipantProcessor.deactivateParticipant(testSystemExceptionBAPMParticipant);

        final TerminationCoordinatorProcessor terminatorParticipantProcessor = TerminationCoordinatorProcessor.getProcessor() ;
        terminatorParticipantProcessor.deactivateParticipant(testNoExceptionBusinessActivityTerminator);
        terminatorParticipantProcessor.deactivateParticipant(testTransactionRolledBackExceptionBusinessActivityTerminator);
        terminatorParticipantProcessor.deactivateParticipant(testUnknownTransactionExceptionBusinessActivityTerminator);
        terminatorParticipantProcessor.deactivateParticipant(testSystemExceptionBusinessActivityTerminator);
    }
}
