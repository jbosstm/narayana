package org.jboss.narayana.rest.integration.test.functional;

import java.net.MalformedURLException;

import junit.framework.Assert;

import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.narayana.rest.integration.ParticipantInformation;
import org.jboss.narayana.rest.integration.ParticipantsContainer;
import org.jboss.narayana.rest.integration.api.HeuristicType;
import org.jboss.narayana.rest.integration.api.Participant;
import org.jboss.narayana.rest.integration.api.ParticipantsManager;
import org.jboss.narayana.rest.integration.api.ParticipantsManagerFactory;
import org.jboss.narayana.rest.integration.api.Prepared;
import org.jboss.narayana.rest.integration.test.common.LoggingParticipant;
import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;

public final class ParticipantsManagerTestCase {

    private static final String APPLICATION_ID = "org.jboss.narayana.rest.integration.test.functional.ParticipantResourceTestCase";

    private ParticipantsManager participantsManager;

    @Before
    public void before() {
        participantsManager = ParticipantsManagerFactory.getInstance();
        ParticipantsContainer.getInstance().clear();
    }

    @Test
    public void testSetBaseUrl() {
        final String url1 = "http://example1.com";
        final String url2 = "http://example2.com";
        final ParticipantsManager participantsManager = ParticipantsManagerFactory.getInstance();

        participantsManager.setBaseUrl(url1);
        Assert.assertEquals(url1, participantsManager.getBaseUrl());

        participantsManager.setBaseUrl(url2);
        Assert.assertEquals(url2, participantsManager.getBaseUrl());
    }

    @Test
    public void testReportHeuristic() throws MalformedURLException {
        final String participantId = new Uid().toString();
        registerParticipant(participantId, new LoggingParticipant(new Prepared()));
        final ParticipantInformation participantInformation = ParticipantsContainer.getInstance().getParticipantInformation(participantId);
        participantInformation.setStatus(TxStatus.TransactionPrepared.name());

        participantsManager.reportHeuristic(participantId, HeuristicType.HEURISTIC_ROLLBACK);

        Assert.assertEquals(TxStatus.TransactionHeuristicRollback.name(), participantInformation.getStatus());
    }

    private void registerParticipant(final String participantId, final Participant participant) throws MalformedURLException {
        ParticipantInformation participantInformation = new ParticipantInformation(participantId, APPLICATION_ID, "", participant);
        participantInformation.setStatus(TxStatus.TransactionActive.name());
        ParticipantsContainer.getInstance().addParticipantInformation(participantId, participantInformation);
    }

}
