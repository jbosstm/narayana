package com.arjuna.wst11.tests;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import javax.xml.namespace.QName;

import com.arjuna.webservices11.ServiceRegistry;

import com.arjuna.wst.Participant;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;

import com.arjuna.webservices11.wsat.ParticipantInboundEvents;
import com.arjuna.webservices11.wsba.ParticipantCompletionParticipantInboundEvents;
import com.arjuna.webservices11.wsba.CoordinatorCompletionParticipantInboundEvents;
import com.arjuna.webservices11.wsat.processors.CompletionCoordinatorProcessor;
import com.arjuna.webservices11.wsat.processors.ParticipantProcessor;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.wsarjtx.processors.TerminationCoordinatorProcessor;
import com.arjuna.webservices11.wsarjtx.ArjunaTX11Constants;
import com.arjuna.webservices11.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.webservices11.wsba.processors.CoordinatorCompletionParticipantProcessor;
import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;

import com.arjuna.wst11.messaging.engines.ParticipantEngine;
import com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine;
import com.arjuna.wst11.messaging.engines.CoordinatorCompletionParticipantEngine;

import com.arjuna.wst11.CompletionCoordinatorParticipant;
import com.arjuna.wst11.BAParticipantManager;
import com.arjuna.wst11.BusinessActivityTerminator;


import com.arjuna.wst.tests.common.TestAbortedVoteParticipant;
import com.arjuna.wst.tests.common.TestFaultedExceptionBusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.tests.common.TestFaultedExceptionBusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.tests.common.TestNoExceptionBusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.tests.common.TestNoExceptionBusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.tests.common.TestNoExceptionParticipant;
import com.arjuna.wst.tests.common.TestPreparedVoteParticipant;
import com.arjuna.wst.tests.common.TestReadOnlyVoteParticipant;
import com.arjuna.wst.tests.common.TestTransactionRolledBackExceptionParticipant;
import com.arjuna.wst.tests.common.TestWrongStateExceptionParticipant;
import com.arjuna.wst.tests.common.TestSystemExceptionParticipant;

import com.arjuna.wst.tests.common.TestSystemExceptionBusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.tests.common.TestWrongStateExceptionBusinessAgreementWithParticipantCompletionParticipant;

import com.arjuna.wst.tests.common.TestSystemExceptionBusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.tests.common.TestWrongStateExceptionBusinessAgreementWithCoordinatorCompletionParticipant;
//import com.arjuna.wst.tests.common.*;

/**
 * listener class to set up WSTX 1.1 tests
 */

public class TestInitialisation  implements ServletContextListener
{
    private final CompletionCoordinatorParticipant testNoExceptionCompletionCoordinatorParticipant = new TestNoExceptionCompletionCoordinatorParticipant(getCoordinatorCompletionParticipantEndpoint(TestUtil.NOEXCEPTION_TRANSACTION_IDENTIFIER));
    private final CompletionCoordinatorParticipant testTransactionRolledExceptionCompletionCoordinatorParticipant  = new TestTransactionRolledBackExceptionCompletionCoordinatorParticipant(getCoordinatorCompletionParticipantEndpoint(TestUtil.TRANSACTIONROLLEDBACKEXCEPTION_TRANSACTION_IDENTIFIER));
    private final CompletionCoordinatorParticipant testUnknownTransactionExceptionCompletionCoordinatorParticipant = new TestUnknownTransactionExceptionCompletionCoordinatorParticipant(getCoordinatorCompletionParticipantEndpoint(TestUtil.UNKNOWNTRANSACTIONEXCEPTION_TRANSACTION_IDENTIFIER));
    private final CompletionCoordinatorParticipant testSystemExceptionCompletionCoordinatorParticipant             = new TestSystemExceptionCompletionCoordinatorParticipant(getCoordinatorCompletionParticipantEndpoint(TestUtil.SYSTEMEXCEPTION_TRANSACTION_IDENTIFIER));

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

    private final BusinessActivityTerminator testNoExceptionBusinessActivityTerminator = new TestNoExceptionBusinessActivityTerminator(getTerminationParticipantEndpoint(TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER));
    private final BusinessActivityTerminator testUnknownTransactionExceptionBusinessActivityTerminator = new TestUnknownTransactionExceptionBusinessActivityTerminator(getTerminationParticipantEndpoint(TestUtil.UNKNOWNTRANSACTIONEXCEPTION_TRANSACTION_IDENTIFIER));
    private final BusinessActivityTerminator testTransactionRolledBackExceptionBusinessActivityTerminator = new TestTransactionRolledBackExceptionBusinessActivityTerminator(getTerminationParticipantEndpoint(TestUtil.TRANSACTIONROLLEDBACKEXCEPTION_PARTICIPANT_IDENTIFIER));
    private final BusinessActivityTerminator testSystemExceptionBusinessActivityTerminator = new TestSystemExceptionBusinessActivityTerminator(getTerminationParticipantEndpoint(TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER));
    /**
     * * Notification that the web application initialization
     * * process is starting.
     * * All ServletContextListeners are notified of context
     * * initialization before any filter or servlet in the web
     * * application is initialized.
     */

public void contextInitialized(ServletContextEvent sce) {
        final CompletionCoordinatorProcessor completionCoordinatorProcessor = CompletionCoordinatorProcessor.getProcessor() ;
        completionCoordinatorProcessor.activateParticipant(testNoExceptionCompletionCoordinatorParticipant, TestUtil.NOEXCEPTION_TRANSACTION_IDENTIFIER);
        completionCoordinatorProcessor.activateParticipant(testTransactionRolledExceptionCompletionCoordinatorParticipant, TestUtil.TRANSACTIONROLLEDBACKEXCEPTION_TRANSACTION_IDENTIFIER);
        completionCoordinatorProcessor.activateParticipant(testUnknownTransactionExceptionCompletionCoordinatorParticipant, TestUtil.UNKNOWNTRANSACTIONEXCEPTION_TRANSACTION_IDENTIFIER);
        completionCoordinatorProcessor.activateParticipant(testSystemExceptionCompletionCoordinatorParticipant, TestUtil.SYSTEMEXCEPTION_TRANSACTION_IDENTIFIER);

        final ParticipantProcessor participantProcessor = ParticipantProcessor.getProcessor() ;

        testPreparedVoteParticipantEngine = new ParticipantEngine(testPreparedVoteParticipant, TestUtil.PREPAREDVOTE_PARTICIPANT_IDENTIFIER, getCoordinatorEndpoint(TestUtil.PREPAREDVOTE_PARTICIPANT_IDENTIFIER));
        testAbortedVoteParticipantEngine = new ParticipantEngine(testAbortedVoteParticipant, TestUtil.ABORTEDVOTE_PARTICIPANT_IDENTIFIER, getCoordinatorEndpoint(TestUtil.ABORTEDVOTE_PARTICIPANT_IDENTIFIER));
        testReadOnlyParticipantEngine = new ParticipantEngine(testReadOnlyParticipant, TestUtil.READONLYVOTE_PARTICIPANT_IDENTIFIER, getCoordinatorEndpoint(TestUtil.READONLYVOTE_PARTICIPANT_IDENTIFIER));
        testNoExceptionParticipantEngine = new ParticipantEngine(testNoExceptionParticipant, TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER, getCoordinatorEndpoint(TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER));
        testTransactionRolledBackExceptionParticipantEngine = new ParticipantEngine(testTransactionRolledBackExceptionParticipant, TestUtil.TRANSACTIONROLLEDBACKEXCEPTION_PARTICIPANT_IDENTIFIER, getCoordinatorEndpoint(TestUtil.TRANSACTIONROLLEDBACKEXCEPTION_PARTICIPANT_IDENTIFIER));
        testWrongStateExceptionParticipantEngine = new ParticipantEngine(testWrongStateExceptionParticipant, TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER, getCoordinatorEndpoint(TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER));
        testSystemExceptionParticipantEngine = new ParticipantEngine(testSystemExceptionParticipant, TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER, getCoordinatorEndpoint(TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER));

        participantProcessor.activateParticipant(testPreparedVoteParticipantEngine, TestUtil.PREPAREDVOTE_PARTICIPANT_IDENTIFIER);
        participantProcessor.activateParticipant(testAbortedVoteParticipantEngine, TestUtil.ABORTEDVOTE_PARTICIPANT_IDENTIFIER);
        participantProcessor.activateParticipant(testReadOnlyParticipantEngine, TestUtil.READONLYVOTE_PARTICIPANT_IDENTIFIER);
        participantProcessor.activateParticipant(testNoExceptionParticipantEngine, TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantProcessor.activateParticipant(testTransactionRolledBackExceptionParticipantEngine, TestUtil.TRANSACTIONROLLEDBACKEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantProcessor.activateParticipant(testWrongStateExceptionParticipantEngine, TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantProcessor.activateParticipant(testSystemExceptionParticipantEngine, TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER);

        final ParticipantCompletionParticipantProcessor participantCompletionParticipantProcessor = ParticipantCompletionParticipantProcessor.getProcessor() ;

        testSystemExceptionBusinessAgreementWithParticipantCompletionParticipantEngine = new ParticipantCompletionParticipantEngine(TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER, getParticipantCompletionCoordinatorEndpoint(TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER), testSystemExceptionBusinessAgreementWithParticipantCompletionParticipant);
        testWrongStateExceptionBusinessAgreementWithParticipantCompletionParticipantEngine = new ParticipantCompletionParticipantEngine(TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER, getParticipantCompletionCoordinatorEndpoint(TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER), testWrongStateExceptionBusinessAgreementWithParticipantCompletionParticipant);
        testNoExceptionBusinessAgreementWithParticipantCompletionParticipantEngine = new ParticipantCompletionParticipantEngine(TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER, getParticipantCompletionCoordinatorEndpoint(TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER), testNoExceptionBusinessAgreementWithParticipantCompletionParticipant);
        testFaultedExceptionBusinessAgreementWithParticipantCompletionParticipantEngine = new ParticipantCompletionParticipantEngine(TestUtil.FAULTEDEXCEPTION_PARTICIPANT_IDENTIFIER, getParticipantCompletionCoordinatorEndpoint(TestUtil.FAULTEDEXCEPTION_PARTICIPANT_IDENTIFIER), testFaultedExceptionBusinessAgreementWithParticipantCompletionParticipant);

        participantCompletionParticipantProcessor.activateParticipant(testSystemExceptionBusinessAgreementWithParticipantCompletionParticipantEngine, TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantCompletionParticipantProcessor.activateParticipant(testWrongStateExceptionBusinessAgreementWithParticipantCompletionParticipantEngine, TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantCompletionParticipantProcessor.activateParticipant(testNoExceptionBusinessAgreementWithParticipantCompletionParticipantEngine, TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantCompletionParticipantProcessor.activateParticipant(testFaultedExceptionBusinessAgreementWithParticipantCompletionParticipantEngine, TestUtil.FAULTEDEXCEPTION_PARTICIPANT_IDENTIFIER);

        final CoordinatorCompletionParticipantProcessor coordinatorCompletionParticipantProcessor = CoordinatorCompletionParticipantProcessor.getProcessor() ;

        testSystemExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine = new CoordinatorCompletionParticipantEngine(TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER, getCoordinatorCompletionCoordinatorEndpoint(TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER), testSystemExceptionBusinessAgreementWithCoordinatorCompletionParticipant);
        testWrongStateExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine = new CoordinatorCompletionParticipantEngine(TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER, getCoordinatorCompletionCoordinatorEndpoint(TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER), testWrongStateExceptionBusinessAgreementWithCoordinatorCompletionParticipant);
        testNoExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine = new CoordinatorCompletionParticipantEngine(TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER, getCoordinatorCompletionCoordinatorEndpoint(TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER), testNoExceptionBusinessAgreementWithCoordinatorCompletionParticipant);
        testFaultedExceptionBusinessAgreementWithCoordinatorCompletionParticipantEngine = new CoordinatorCompletionParticipantEngine(TestUtil.FAULTEDEXCEPTION_PARTICIPANT_IDENTIFIER, getCoordinatorCompletionCoordinatorEndpoint(TestUtil.FAULTEDEXCEPTION_PARTICIPANT_IDENTIFIER), testFaultedExceptionBusinessAgreementWithCoordinatorCompletionParticipant);


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
     * * Notification that the servlet context is about to be shut down.
     * * All servlets and filters have been destroy()ed before any
     * * ServletContextListeners are notified of context
     * * destruction.
     */
public void contextDestroyed(ServletContextEvent sce) {
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

    private static W3CEndpointReference getCoordinatorCompletionParticipantEndpoint(String id)
    {
        try {
            W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
            final QName serviceName = BusinessActivityConstants.COORDINATOR_COMPLETION_PARTICIPANT_SERVICE_QNAME;
            final QName endpointName = BusinessActivityConstants.COORDINATOR_COMPLETION_PARTICIPANT_PORT_QNAME;
            final String address = ServiceRegistry.getRegistry().getServiceURI(BusinessActivityConstants.COORDINATOR_COMPLETION_PARTICIPANT_SERVICE_NAME);
            builder.serviceName(serviceName);
            builder.endpointName(endpointName);
            builder.address(address);
            InstanceIdentifier.setEndpointInstanceIdentifier(builder, id);
            return builder.build();
        } catch (Exception e) {
            return null;
        }
    }

    private static W3CEndpointReference getTerminationParticipantEndpoint(String id)
    {
        try {
            W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
            final QName serviceName = ArjunaTX11Constants.TERMINATION_PARTICIPANT_SERVICE_QNAME;
            final QName endpointName = ArjunaTX11Constants.TERMINATION_PARTICIPANT_PORT_QNAME;
            final String address = ServiceRegistry.getRegistry().getServiceURI(ArjunaTX11Constants.TERMINATION_PARTICIPANT_SERVICE_NAME);
            builder.serviceName(serviceName);
            builder.endpointName(endpointName);
            builder.address(address);
            InstanceIdentifier.setEndpointInstanceIdentifier(builder, id);
            return builder.build();
        } catch (Exception e) {
            return null;
        }
    }

    private static W3CEndpointReference getCoordinatorEndpoint(String id)
    {
        try {
            W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
            final QName serviceName = AtomicTransactionConstants.COORDINATOR_SERVICE_QNAME;
            final QName endpointName = AtomicTransactionConstants.COORDINATOR_PORT_QNAME;
            final String address = ServiceRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.COORDINATOR_SERVICE_NAME);
            builder.serviceName(serviceName);
            builder.endpointName(endpointName);
            builder.address(address);
            InstanceIdentifier.setEndpointInstanceIdentifier(builder, id);
            return builder.build();
        } catch (Exception e) {
            return null;
        }
    }

    private static W3CEndpointReference getParticipantCompletionCoordinatorEndpoint(String id)
    {
        try {
            W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
            final QName serviceName = BusinessActivityConstants.PARTICIPANT_COMPLETION_COORDINATOR_SERVICE_QNAME;
            final QName endpointName = BusinessActivityConstants.PARTICIPANT_COMPLETION_COORDINATOR_PORT_QNAME;
            final String address = ServiceRegistry.getRegistry().getServiceURI(BusinessActivityConstants.PARTICIPANT_COMPLETION_COORDINATOR_SERVICE_NAME);
            builder.serviceName(serviceName);
            builder.endpointName(endpointName);
            builder.address(address);
            InstanceIdentifier.setEndpointInstanceIdentifier(builder, id);
            return builder.build();
        } catch (Exception e) {
            return null;
        }
    }

    private static W3CEndpointReference getCoordinatorCompletionCoordinatorEndpoint(String id)
    {
        try {
            W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
            final QName serviceName = BusinessActivityConstants.COORDINATOR_COMPLETION_COORDINATOR_SERVICE_QNAME;
            final QName endpointName = BusinessActivityConstants.COORDINATOR_COMPLETION_COORDINATOR_PORT_QNAME;
            final String address = ServiceRegistry.getRegistry().getServiceURI(BusinessActivityConstants.COORDINATOR_COMPLETION_COORDINATOR_SERVICE_NAME);
            builder.serviceName(serviceName);
            builder.endpointName(endpointName);
            builder.address(address);
            InstanceIdentifier.setEndpointInstanceIdentifier(builder, id);
            return builder.build();
        } catch (Exception e) {
            return null;
        }
    }
}
