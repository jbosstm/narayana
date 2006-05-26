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
package com.arjuna.wst.tests;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.arjuna.webservices.wsarjtx.processors.ParticipantManagerParticipantProcessor;
import com.arjuna.webservices.wsarjtx.processors.TerminatorParticipantProcessor;
import com.arjuna.webservices.wsat.Participant;
import com.arjuna.webservices.wsat.processors.CompletionCoordinatorProcessor;
import com.arjuna.webservices.wsat.processors.ParticipantProcessor;
import com.arjuna.webservices.wsba.processors.CoordinatorCompletionParticipantProcessor;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.wst.BAParticipantManager;
import com.arjuna.wst.BusinessActivityTerminator;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.CompletionCoordinatorParticipant;

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

    private final BusinessAgreementWithParticipantCompletionParticipant testSystemExceptionBusinessAgreementWithParticipantCompletionParticipant = new TestSystemExceptionBusinessAgreementWithParticipantCompletionParticipant();
    private final BusinessAgreementWithParticipantCompletionParticipant testWrongStateExceptionBusinessAgreementWithParticipantCompletionParticipant = new TestWrongStateExceptionBusinessAgreementWithParticipantCompletionParticipant();
    private final BusinessAgreementWithParticipantCompletionParticipant testNoExceptionBusinessAgreementWithParticipantCompletionParticipant = new TestNoExceptionBusinessAgreementWithParticipantCompletionParticipant();
    private final BusinessAgreementWithParticipantCompletionParticipant testFaultedExceptionBusinessAgreementWithParticipantCompletionParticipant = new TestFaultedExceptionBusinessAgreementWithParticipantCompletionParticipant();

    private final BusinessAgreementWithCoordinatorCompletionParticipant testSystemExceptionBusinessAgreementWithCoordinatorCompletionParticipant = new TestSystemExceptionBusinessAgreementWithCoordinatorCompletionParticipant();
    private final BusinessAgreementWithCoordinatorCompletionParticipant testWrongStateExceptionBusinessAgreementWithCoordinatorCompletionParticipant = new TestWrongStateExceptionBusinessAgreementWithCoordinatorCompletionParticipant();
    private final BusinessAgreementWithCoordinatorCompletionParticipant testNoExceptionBusinessAgreementWithCoordinatorCompletionParticipant = new TestNoExceptionBusinessAgreementWithCoordinatorCompletionParticipant();
    private final BusinessAgreementWithCoordinatorCompletionParticipant testFaultedExceptionBusinessAgreementWithCoordinatorCompletionParticipant = new TestFaultedExceptionBusinessAgreementWithCoordinatorCompletionParticipant();

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
        final CompletionCoordinatorProcessor completionCoordinatorProcessor = CompletionCoordinatorProcessor.getCoordinator() ;
        completionCoordinatorProcessor.activateParticipant(testNoExceptionCompletionCoordinatorParticipant, TestUtil.NOEXCEPTION_TRANSACTION_IDENTIFIER);
        completionCoordinatorProcessor.activateParticipant(testTransactionRolledExceptionCompletionCoordinatorParticipant, TestUtil.TRANSACTIONROLLEDBACKEXCEPTION_TRANSACTION_IDENTIFIER);
        completionCoordinatorProcessor.activateParticipant(testUnknownTransactionExceptionCompletionCoordinatorParticipant, TestUtil.UNKNOWNTRANSACTIONEXCEPTION_TRANSACTION_IDENTIFIER);
        completionCoordinatorProcessor.activateParticipant(testSystemExceptionCompletionCoordinatorParticipant, TestUtil.SYSTEMEXCEPTION_TRANSACTION_IDENTIFIER);

        final ParticipantProcessor participantProcessor = ParticipantProcessor.getParticipant() ;
        participantProcessor.activateParticipant(testPreparedVoteParticipant, TestUtil.PREPAREDVOTE_PARTICIPANT_IDENTIFIER);
        participantProcessor.activateParticipant(testAbortedVoteParticipant, TestUtil.ABORTEDVOTE_PARTICIPANT_IDENTIFIER);
        participantProcessor.activateParticipant(testReadOnlyParticipant, TestUtil.READONLYVOTE_PARTICIPANT_IDENTIFIER);
        participantProcessor.activateParticipant(testNoExceptionParticipant, TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantProcessor.activateParticipant(testTransactionRolledBackExceptionParticipant, TestUtil.TRANSACTIONROLLEDBACKEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantProcessor.activateParticipant(testWrongStateExceptionParticipant, TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantProcessor.activateParticipant(testSystemExceptionParticipant, TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER);

        final ParticipantCompletionParticipantProcessor participantCompletionParticipantProcessor = ParticipantCompletionParticipantProcessor.getParticipant() ;
        participantCompletionParticipantProcessor.activateParticipant(testSystemExceptionBusinessAgreementWithParticipantCompletionParticipant, TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantCompletionParticipantProcessor.activateParticipant(testWrongStateExceptionBusinessAgreementWithParticipantCompletionParticipant, TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantCompletionParticipantProcessor.activateParticipant(testNoExceptionBusinessAgreementWithParticipantCompletionParticipant, TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantCompletionParticipantProcessor.activateParticipant(testFaultedExceptionBusinessAgreementWithParticipantCompletionParticipant, TestUtil.FAULTEDEXCEPTION_PARTICIPANT_IDENTIFIER);

        final CoordinatorCompletionParticipantProcessor coordinatorCompletionParticipantProcessor = CoordinatorCompletionParticipantProcessor.getParticipant() ;
        coordinatorCompletionParticipantProcessor.activateParticipant(testSystemExceptionBusinessAgreementWithCoordinatorCompletionParticipant, TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER);
        coordinatorCompletionParticipantProcessor.activateParticipant(testWrongStateExceptionBusinessAgreementWithCoordinatorCompletionParticipant, TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER);
        coordinatorCompletionParticipantProcessor.activateParticipant(testNoExceptionBusinessAgreementWithCoordinatorCompletionParticipant, TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER);
        coordinatorCompletionParticipantProcessor.activateParticipant(testFaultedExceptionBusinessAgreementWithCoordinatorCompletionParticipant, TestUtil.FAULTEDEXCEPTION_PARTICIPANT_IDENTIFIER);

        final ParticipantManagerParticipantProcessor participantManagerParticipantProcessor = ParticipantManagerParticipantProcessor.getParticipant() ;
        participantManagerParticipantProcessor.activateParticipant(testNoExceptionBAPMParticipant, TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantManagerParticipantProcessor.activateParticipant(testWrongStateExceptionBAPMParticipant, TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER);
        participantManagerParticipantProcessor.activateParticipant(testSystemExceptionBAPMParticipant, TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER);

        final TerminatorParticipantProcessor terminatorParticipantProcessor = TerminatorParticipantProcessor.getParticipant() ;
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
        final CompletionCoordinatorProcessor completionCoordinatorProcessor = CompletionCoordinatorProcessor.getCoordinator() ;
        completionCoordinatorProcessor.deactivateParticipant(testNoExceptionCompletionCoordinatorParticipant);
        completionCoordinatorProcessor.deactivateParticipant(testTransactionRolledExceptionCompletionCoordinatorParticipant);
        completionCoordinatorProcessor.deactivateParticipant(testUnknownTransactionExceptionCompletionCoordinatorParticipant);
        completionCoordinatorProcessor.deactivateParticipant(testSystemExceptionCompletionCoordinatorParticipant);

        final ParticipantProcessor participantProcessor = ParticipantProcessor.getParticipant() ;
        participantProcessor.deactivateParticipant(testPreparedVoteParticipant);
        participantProcessor.deactivateParticipant(testAbortedVoteParticipant);
        participantProcessor.deactivateParticipant(testReadOnlyParticipant);
        participantProcessor.deactivateParticipant(testNoExceptionParticipant);
        participantProcessor.deactivateParticipant(testTransactionRolledBackExceptionParticipant);
        participantProcessor.deactivateParticipant(testWrongStateExceptionParticipant);
        participantProcessor.deactivateParticipant(testSystemExceptionParticipant);
        
        final ParticipantCompletionParticipantProcessor participantCompletionParticipantProcessor = ParticipantCompletionParticipantProcessor.getParticipant() ;
        participantCompletionParticipantProcessor.deactivateParticipant(testSystemExceptionBusinessAgreementWithParticipantCompletionParticipant);
        participantCompletionParticipantProcessor.deactivateParticipant(testWrongStateExceptionBusinessAgreementWithParticipantCompletionParticipant);
        participantCompletionParticipantProcessor.deactivateParticipant(testNoExceptionBusinessAgreementWithParticipantCompletionParticipant);
        participantCompletionParticipantProcessor.deactivateParticipant(testFaultedExceptionBusinessAgreementWithParticipantCompletionParticipant);

        final CoordinatorCompletionParticipantProcessor coordinatorCompletionParticipantProcessor = CoordinatorCompletionParticipantProcessor.getParticipant() ;
        coordinatorCompletionParticipantProcessor.deactivateParticipant(testSystemExceptionBusinessAgreementWithCoordinatorCompletionParticipant);
        coordinatorCompletionParticipantProcessor.deactivateParticipant(testWrongStateExceptionBusinessAgreementWithCoordinatorCompletionParticipant);
        coordinatorCompletionParticipantProcessor.deactivateParticipant(testNoExceptionBusinessAgreementWithCoordinatorCompletionParticipant);
        coordinatorCompletionParticipantProcessor.deactivateParticipant(testFaultedExceptionBusinessAgreementWithCoordinatorCompletionParticipant);

        final ParticipantManagerParticipantProcessor participantManagerParticipantProcessor = ParticipantManagerParticipantProcessor.getParticipant() ;
        participantManagerParticipantProcessor.deactivateParticipant(testNoExceptionBAPMParticipant);
        participantManagerParticipantProcessor.deactivateParticipant(testWrongStateExceptionBAPMParticipant);
        participantManagerParticipantProcessor.deactivateParticipant(testSystemExceptionBAPMParticipant);

        final TerminatorParticipantProcessor terminatorParticipantProcessor = TerminatorParticipantProcessor.getParticipant() ;
        terminatorParticipantProcessor.deactivateParticipant(testNoExceptionBusinessActivityTerminator);
        terminatorParticipantProcessor.deactivateParticipant(testTransactionRolledBackExceptionBusinessActivityTerminator);
        terminatorParticipantProcessor.deactivateParticipant(testUnknownTransactionExceptionBusinessActivityTerminator);
        terminatorParticipantProcessor.deactivateParticipant(testSystemExceptionBusinessActivityTerminator);
    }
}
