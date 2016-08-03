package org.jboss.narayana.rest.integration.test.functional;

import com.arjuna.ats.arjuna.common.Uid;
import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.narayana.rest.integration.ParticipantsContainer;
import org.jboss.narayana.rest.integration.VolatileParticipantResource;
import org.jboss.narayana.rest.integration.api.VolatileParticipant;
import org.jboss.narayana.rest.integration.test.common.LoggingVolatileParticipant;

import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class VolatileParticipantResourceTestCase {

    private static final String BASE_URL = "http://localhost:" + TestPortProvider.getPort();

    private static final String VOLATILE_PARTICIPANT_URL = BASE_URL + "/" + VolatileParticipantResource.BASE_PATH_SEGMENT;

    private static NettyJaxrsServer NETTY;

    private String participantId;

    @BeforeClass
    public static void beforeClass() {
        List<String> resourceClasses = new ArrayList<String>();
        resourceClasses.add("org.jboss.narayana.rest.integration.VolatileParticipantResource");

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
    public void testRequestsToNotRegisteredParticipant() throws Exception {
        Response simpleResponse = beforeCompletion(participantId);
        Assert.assertEquals(404, simpleResponse.getStatus());

        simpleResponse = afterCompletion(participantId, TxStatus.TransactionCommitted);
        Assert.assertEquals(404, simpleResponse.getStatus());
    }

    @Test
    public void testBeforeCompletion() throws Exception {
        LoggingVolatileParticipant participant = new LoggingVolatileParticipant();
        registerParticipant(participantId, participant);

        Response response = beforeCompletion(participantId);

        Assert.assertEquals(200, response.getStatus());

        List<String> invocations = participant.getInvocations();
        Assert.assertEquals(1, invocations.size());
        Assert.assertEquals("beforeCompletion", invocations.get(0));
    }

    @Test
    public void testAfterCompletion() throws Exception {
        LoggingVolatileParticipant participant = new LoggingVolatileParticipant();
        registerParticipant(participantId, participant);

        Response response = afterCompletion(participantId, TxStatus.TransactionCommitted);

        Assert.assertEquals(200, response.getStatus());

        List<String> invocations = participant.getInvocations();
        Assert.assertEquals(1, invocations.size());
        Assert.assertEquals("afterCompletion", invocations.get(0));
        Assert.assertEquals(TxStatus.TransactionCommitted, participant.getTxStatus());

        response = beforeCompletion(participantId);
        Assert.assertEquals(404, response.getStatus());
    }

    private void registerParticipant(final String participantId, final VolatileParticipant volatileParticipant)
            throws MalformedURLException {

        ParticipantsContainer.getInstance().addVolatileParticipant(participantId, volatileParticipant);
    }

    private Response beforeCompletion(final String participantId) throws Exception {
        return ClientBuilder.newClient().target(VOLATILE_PARTICIPANT_URL + "/" + participantId).request().put(null);
    }

    private Response afterCompletion(final String participantId, final TxStatus txStatus) throws Exception {
        return ClientBuilder.newClient().target(VOLATILE_PARTICIPANT_URL + "/" + participantId).request()
                .put(Entity.entity(TxSupport.toStatusContent(txStatus.name()), TxMediaType.TX_STATUS_MEDIA_TYPE));
    }

}
