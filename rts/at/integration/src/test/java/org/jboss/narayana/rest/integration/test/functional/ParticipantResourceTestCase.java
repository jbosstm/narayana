package org.jboss.narayana.rest.integration.test.functional;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.jbossts.star.util.TxLinkNames;
import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.narayana.rest.integration.ParticipantInformation;
import org.jboss.narayana.rest.integration.ParticipantResource;
import org.jboss.narayana.rest.integration.ParticipantsContainer;
import org.jboss.narayana.rest.integration.api.Aborted;
import org.jboss.narayana.rest.integration.api.Participant;
import org.jboss.narayana.rest.integration.api.Prepared;
import org.jboss.narayana.rest.integration.api.ReadOnly;
import org.jboss.narayana.rest.integration.test.common.LoggingParticipant;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;
import org.jboss.resteasy.spi.Link;
import org.jboss.resteasy.spi.LinkHeader;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class ParticipantResourceTestCase {

    private static final String APPLICATION_ID = "org.jboss.narayana.rest.integration.test.functional.ParticipantResourceTestCase";

    private static final String BASE_URL = "http://localhost:" + TestPortProvider.getPort();

    private static final String PARTICIPANT_URL = BASE_URL + "/" + ParticipantResource.BASE_PATH_SEGMENT;

    private static NettyJaxrsServer NETTY;

    private String participantId;

    @BeforeClass
    public static void beforeClass() {
        List<String> resourceClasses = new ArrayList<String>();
        resourceClasses.add("org.jboss.narayana.rest.integration.ParticipantResource");

        ResteasyDeployment resteasyDeployment = new ResteasyDeployment();
        resteasyDeployment.setResourceClasses(resourceClasses);

        NETTY = new NettyJaxrsServer();
        NETTY.setDeployment(resteasyDeployment);
        NETTY.setPort(TestPortProvider.getPort());
        NETTY.start();
    }

    @AfterClass
    public static void afterClass() {
        NETTY.stop();
    }

    @Before
    public void before() {
        participantId = new Uid().toString();
        ParticipantsContainer.getInstance().clear();
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testRequestsToNotRegisteredParticipant() throws Exception {
        ClientResponse simpleResponse = getParticipantTerminator(participantId);
        Assert.assertEquals(404, simpleResponse.getStatus());

        ClientResponse<String> stringResponse = getParticipantStatus(participantId);
        Assert.assertEquals(404, stringResponse.getStatus());

        stringResponse = prepareParticipant(participantId);
        Assert.assertEquals(404, stringResponse.getStatus());

        simpleResponse = forgetParticipantHeuristic(participantId);
        Assert.assertEquals(404, simpleResponse.getStatus());
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testGetTerminator() throws Exception {
        registerParticipant(participantId, new LoggingParticipant(new ReadOnly()));

        ClientResponse response = getParticipantTerminator(participantId);
        Assert.assertEquals(200, response.getStatus());

        LinkHeader linkHeader = response.getLinkHeader();
        Link link = linkHeader.getLinkByRelationship(TxLinkNames.TERMINATOR);
        Assert.assertNotNull(link);
        Assert.assertEquals(PARTICIPANT_URL + "/" + participantId, link.getHref());
    }

    @Test
    public void testGetStatus() throws Exception {
        registerParticipant(participantId, new LoggingParticipant(new ReadOnly()));

        ParticipantInformation participantInformation = ParticipantsContainer.getInstance().getParticipantInformation(
                participantId);
        participantInformation.setStatus(TxStatus.TransactionPrepared.name());

        ClientResponse<String> response = getParticipantStatus(participantId);

        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue(TxStatus.isPrepare(TxSupport.getStatus(response.getEntity())));

        participantInformation.setStatus(TxStatus.TransactionCommitted.name());
        response = new ClientRequest(PARTICIPANT_URL + "/" + participantId).get(String.class);
        Assert.assertTrue(TxStatus.isCommit(TxSupport.getStatus(response.getEntity())));
    }

    @Test
    public void testPrepareOutcomePrepared() throws Exception {
        final LoggingParticipant participant = new LoggingParticipant(new Prepared());
        registerParticipant(participantId, participant);

        ClientResponse<String> stringResponse = prepareParticipant(participantId);

        ParticipantInformation participantInformation = ParticipantsContainer.getInstance().getParticipantInformation(
                participantId);

        Assert.assertEquals(200, stringResponse.getStatus());
        Assert.assertEquals(TxStatus.TransactionPrepared.name(), TxSupport.getStatus(stringResponse.getEntity()));
        Assert.assertEquals(TxStatus.TransactionPrepared.name(), participantInformation.getStatus());
        Assert.assertEquals(Arrays.asList(new String[] { "prepare" }), participant.getInvocations());
    }

    @Test
    public void testPrepareOutcomeAborted() throws Exception {
        final LoggingParticipant participant = new LoggingParticipant(new Aborted());
        registerParticipant(participantId, participant);

        ParticipantInformation participantInformation = ParticipantsContainer.getInstance().getParticipantInformation(
                participantId);

        ClientResponse<String> stringResponse = prepareParticipant(participantId);

        Assert.assertEquals(409, stringResponse.getStatus());
        Assert.assertEquals(TxStatus.TransactionRolledBack.name(), TxSupport.getStatus(stringResponse.getEntity()));
        Assert.assertEquals(TxStatus.TransactionRolledBack.name(), participantInformation.getStatus());
        Assert.assertEquals(Arrays.asList(new String[] { "prepare", "rollback" }), participant.getInvocations());
        Assert.assertNull(ParticipantsContainer.getInstance().getParticipantInformation(participantId));
    }

    @Test
    public void testPrepareOutcomeReadOnly() throws Exception {
        final LoggingParticipant participant = new LoggingParticipant(new ReadOnly());
        registerParticipant(participantId, participant);

        ParticipantInformation participantInformation = ParticipantsContainer.getInstance().getParticipantInformation(
                participantId);

        ClientResponse<String> stringResponse = prepareParticipant(participantId);

        Assert.assertEquals(200, stringResponse.getStatus());
        Assert.assertEquals(TxStatus.TransactionReadOnly.name(), TxSupport.getStatus(stringResponse.getEntity()));
        Assert.assertEquals(TxStatus.TransactionReadOnly.name(), participantInformation.getStatus());
        Assert.assertEquals(Arrays.asList(new String[] { "prepare" }), participant.getInvocations());
        Assert.assertNull(ParticipantsContainer.getInstance().getParticipantInformation(participantId));
    }

    @Test
    public void testPreparePreparedTransaction() throws Exception {
        final LoggingParticipant participant = new LoggingParticipant(new Prepared());
        registerParticipant(participantId, participant);

        ParticipantInformation participantInformation = ParticipantsContainer.getInstance().getParticipantInformation(
                participantId);
        participantInformation.setStatus(TxStatus.TransactionPrepared.name());

        ClientResponse<String> stringResponse = prepareParticipant(participantId);

        Assert.assertEquals(412, stringResponse.getStatus());
    }

    @Test
    public void testCommit() throws Exception {
        LoggingParticipant participant = new LoggingParticipant(new Prepared());
        registerParticipant(participantId, participant);
        ParticipantInformation participantInformation = ParticipantsContainer.getInstance().getParticipantInformation(
                participantId);

        participantInformation.setStatus(TxStatus.TransactionPrepared.name());

        ClientResponse<String> stringResponse = commitParticipant(participantInformation.getId());

        Assert.assertEquals(200, stringResponse.getStatus());
        Assert.assertEquals(TxStatus.TransactionCommitted.name(), participantInformation.getStatus());
        Assert.assertEquals(Arrays.asList(new String[] { "commit" }), participant.getInvocations());
        Assert.assertNull(ParticipantsContainer.getInstance().getParticipantInformation(participantId));
    }

    @Test
    public void testCommitWithoutPrepare() throws Exception {
        LoggingParticipant participant = new LoggingParticipant(new Prepared());
        registerParticipant(participantId, participant);
        ParticipantInformation participantInformation = ParticipantsContainer.getInstance().getParticipantInformation(
                participantId);

        ClientResponse<String> stringResponse = commitParticipant(participantInformation.getId());

        Assert.assertEquals(412, stringResponse.getStatus());
    }

    @Test
    public void testCommitOnePhase() throws Exception {
        LoggingParticipant participant = new LoggingParticipant(new Prepared());
        registerParticipant(participantId, participant);
        ParticipantInformation participantInformation = ParticipantsContainer.getInstance().getParticipantInformation(
                participantId);

        ClientResponse<String> stringResponse = commitParticipantInOnePhase(participantInformation.getId());

        Assert.assertEquals(200, stringResponse.getStatus());
        Assert.assertEquals(TxStatus.TransactionCommittedOnePhase.name(), participantInformation.getStatus());
        Assert.assertEquals(Arrays.asList(new String[] { "commitOnePhase" }), participant.getInvocations());
        Assert.assertNull(ParticipantsContainer.getInstance().getParticipantInformation(participantId));
    }

    @Test
    public void testCommitOnePhaseAfterPrepare() throws Exception {
        LoggingParticipant participant = new LoggingParticipant(new Prepared());
        registerParticipant(participantId, participant);
        ParticipantInformation participantInformation = ParticipantsContainer.getInstance().getParticipantInformation(
                participantId);

        participantInformation.setStatus(TxStatus.TransactionPrepared.name());

        ClientResponse<String> stringResponse = commitParticipantInOnePhase(participantInformation.getId());

        Assert.assertEquals(412, stringResponse.getStatus());
    }

    @Test
    public void testRollback() throws Exception {
        LoggingParticipant participant = new LoggingParticipant(new Prepared());
        registerParticipant(participantId, participant);
        ParticipantInformation participantInformation = ParticipantsContainer.getInstance().getParticipantInformation(
                participantId);

        ClientResponse<String> stringResponse = rollbackParticipant(participantInformation.getId());

        Assert.assertEquals(200, stringResponse.getStatus());
        Assert.assertEquals(TxStatus.TransactionRolledBack.name(), participantInformation.getStatus());
        Assert.assertEquals(Arrays.asList(new String[] { "rollback" }), participant.getInvocations());
        Assert.assertNull(ParticipantsContainer.getInstance().getParticipantInformation(participantId));
    }

    @Test
    public void testRollbackAfterPrepare() throws Exception {
        LoggingParticipant participant = new LoggingParticipant(new Prepared());
        registerParticipant(participantId, participant);
        ParticipantInformation participantInformation = ParticipantsContainer.getInstance().getParticipantInformation(
                participantId);

        participantInformation.setStatus(TxStatus.TransactionPrepared.name());

        ClientResponse<String> stringResponse = rollbackParticipant(participantInformation.getId());

        Assert.assertEquals(200, stringResponse.getStatus());
        Assert.assertEquals(TxStatus.TransactionRolledBack.name(), participantInformation.getStatus());
        Assert.assertEquals(Arrays.asList(new String[] { "rollback" }), participant.getInvocations());
        Assert.assertNull(ParticipantsContainer.getInstance().getParticipantInformation(participantId));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testForgetHeuristic() throws Exception {
        LoggingParticipant participant = new LoggingParticipant(new Prepared());
        registerParticipant(participantId, participant);
        ParticipantInformation participantInformation = ParticipantsContainer.getInstance().getParticipantInformation(
                participantId);

        participantInformation.setStatus(TxStatus.TransactionHeuristicRollback.name());

        ClientResponse simpleResponse = forgetParticipantHeuristic(participantId);

        Assert.assertEquals(200, simpleResponse.getStatus());
        Assert.assertNull(ParticipantsContainer.getInstance().getParticipantInformation(participantId));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testForgetHeuristicWithoutHeuristic() throws Exception {
        LoggingParticipant participant = new LoggingParticipant(new Prepared());
        registerParticipant(participantId, participant);

        ClientResponse simpleResponse = forgetParticipantHeuristic(participantId);

        ParticipantInformation participantInformation = ParticipantsContainer.getInstance().getParticipantInformation(
                participantId);

        Assert.assertEquals(412, simpleResponse.getStatus());
        Assert.assertEquals(TxStatus.TransactionActive.name(), participantInformation.getStatus());
    }

    private void registerParticipant(final String participantId, final Participant participant)
            throws MalformedURLException {

        ParticipantInformation participantInformation = new ParticipantInformation(participantId, APPLICATION_ID, "",
                participant);
        participantInformation.setStatus(TxStatus.TransactionActive.name());
        ParticipantsContainer.getInstance().addParticipantInformation(participantId, participantInformation);
    }

    @SuppressWarnings("rawtypes")
    private ClientResponse getParticipantTerminator(final String participantId) throws Exception {
        return new ClientRequest(PARTICIPANT_URL + "/" + participantId).head();
    }

    private ClientResponse<String> getParticipantStatus(final String participantId) throws Exception {
        return new ClientRequest(PARTICIPANT_URL + "/" + participantId).get(String.class);
    }

    private ClientResponse<String> prepareParticipant(final String participantId) throws Exception {
        return new ClientRequest(PARTICIPANT_URL + "/" + participantId).body(TxMediaType.TX_STATUS_MEDIA_TYPE,
                TxSupport.toStatusContent(TxStatus.TransactionPrepared.name())).put(String.class);
    }

    private ClientResponse<String> commitParticipant(final String participantId) throws Exception {
        return new ClientRequest(PARTICIPANT_URL + "/" + participantId).body(TxMediaType.TX_STATUS_MEDIA_TYPE,
                TxSupport.toStatusContent(TxStatus.TransactionCommitted.name())).put(String.class);
    }

    private ClientResponse<String> commitParticipantInOnePhase(final String participantId) throws Exception {
        return new ClientRequest(PARTICIPANT_URL + "/" + participantId).body(TxMediaType.TX_STATUS_MEDIA_TYPE,
                TxSupport.toStatusContent(TxStatus.TransactionCommittedOnePhase.name())).put(String.class);
    }

    private ClientResponse<String> rollbackParticipant(final String participantId) throws Exception {
        return new ClientRequest(PARTICIPANT_URL + "/" + participantId).body(TxMediaType.TX_STATUS_MEDIA_TYPE,
                TxSupport.toStatusContent(TxStatus.TransactionRolledBack.name())).put(String.class);
    }

    @SuppressWarnings("rawtypes")
    private ClientResponse forgetParticipantHeuristic(final String participantId) throws Exception {
        return new ClientRequest(PARTICIPANT_URL + "/" + participantId).delete();
    }

}
