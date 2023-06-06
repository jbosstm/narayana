/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */
package org.jboss.narayana.rest.integration.test.functional;

import com.arjuna.ats.arjuna.common.Uid;
import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.narayana.rest.integration.ParticipantsContainer;
import org.jboss.narayana.rest.integration.VolatileParticipantResource;
import org.jboss.narayana.rest.integration.api.VolatileParticipant;
import org.jboss.narayana.rest.integration.test.common.LoggingVolatileParticipant;

import org.jboss.resteasy.core.ResteasyDeploymentImpl;
import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
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
        List<String> resourceClasses = new ArrayList<>();
        resourceClasses.add("org.jboss.narayana.rest.integration.VolatileParticipantResource");

        ResteasyDeployment resteasyDeployment = new ResteasyDeploymentImpl();
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
    public void testRequestsToNotRegisteredParticipant() {
        Response simpleResponse = beforeCompletion(participantId);
        Assert.assertEquals(404, simpleResponse.getStatus());

        simpleResponse = afterCompletion(participantId, TxStatus.TransactionCommitted);
        Assert.assertEquals(404, simpleResponse.getStatus());
    }

    @Test
    public void testBeforeCompletion() {
        LoggingVolatileParticipant participant = new LoggingVolatileParticipant();
        registerParticipant(participantId, participant);

        Response response = beforeCompletion(participantId);

        Assert.assertEquals(200, response.getStatus());

        List<String> invocations = participant.getInvocations();
        Assert.assertEquals(1, invocations.size());
        Assert.assertEquals("beforeCompletion", invocations.get(0));
    }

    @Test
    public void testAfterCompletion() {
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

    private void registerParticipant(final String participantId, final VolatileParticipant volatileParticipant) {
        ParticipantsContainer.getInstance().addVolatileParticipant(participantId, volatileParticipant);
    }

    private Response beforeCompletion(final String participantId) {
        return ClientBuilder.newClient().target(VOLATILE_PARTICIPANT_URL + "/" + participantId).request().put(null);
    }

    private Response afterCompletion(final String participantId, final TxStatus txStatus) {
        return ClientBuilder.newClient().target(VOLATILE_PARTICIPANT_URL + "/" + participantId).request()
                .put(Entity.entity(TxSupport.toStatusContent(txStatus.name()), TxMediaType.TX_STATUS_MEDIA_TYPE));
    }

}
