/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package io.narayana.lra.arquillian;

import io.narayana.lra.arquillian.resource.NestedParticipant;
import io.narayana.lra.arquillian.spi.NarayanaLRARecovery;
import io.narayana.lra.client.NarayanaLRAClient;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.lra.tck.service.LRAMetricService;
import org.eclipse.microprofile.lra.tck.service.LRAMetricType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URL;

@RunWith(Arquillian.class)
public class NestedParticipantIT {

    @ArquillianResource
    private URL baseURL;

    @Inject
    private LRAMetricService lraMetricService;

    @Inject
    private NarayanaLRAClient narayanaLRAClient;

    private NarayanaLRARecovery narayanaLRARecovery = new NarayanaLRARecovery();

    private Client client;

    @Deployment
    public static WebArchive deploy() {
        return Deployer.deploy(NestedParticipantIT.class.getSimpleName());
    }

    @Before
    public void before() {
        lraMetricService.clear();
        client = ClientBuilder.newClient();
    }

    @After
    public void after() {
        if (client != null) {
            client.close();
        }
    }

    /**
     * Verifies that the AfterLRA notification in nested participant is received correctly.
     */
    @Test
    public void nestedParticipantAfterLRACallTest() {
        Response response = null;

        URI parentLRA = narayanaLRAClient.startLRA(NestedParticipantIT.class.getName());

        URI nestedLRA = null;

        try {
            response = client.target(UriBuilder.fromUri(baseURL.toExternalForm())
                .path(NestedParticipant.ROOT_PATH)
                .path(NestedParticipant.ENLIST_PATH))
                .request()
                .header(LRA.LRA_HTTP_CONTEXT_HEADER, parentLRA)
                .get();

            Assert.assertEquals(200, response.getStatus());
            Assert.assertTrue(response.hasEntity());

            nestedLRA = URI.create(response.readEntity(String.class));
            Assert.assertNotEquals(parentLRA, nestedLRA);
            Assert.assertEquals(1, lraMetricService.getMetric(LRAMetricType.Nested, parentLRA));
        } finally {
            if (response != null) {
                response.close();
            }
        }

        // close nested LRA
        narayanaLRAClient.closeLRA(nestedLRA);
        // the nested LRA should be in Closed state, however, we keep it in Closing state
        // so we can't wait for the recovery of the nested LRA
        // https://issues.redhat.com/browse/JBTM-3330
        narayanaLRARecovery.waitForEndPhaseReplay(nestedLRA);

        Assert.assertEquals(1, lraMetricService.getMetric(LRAMetricType.Completed, nestedLRA));
        Assert.assertEquals(2, lraMetricService.getMetric(LRAMetricType.Nested, parentLRA));
        Assert.assertEquals(0, lraMetricService.getMetric(LRAMetricType.AfterLRA, nestedLRA));

        narayanaLRAClient.closeLRA(parentLRA);
        narayanaLRARecovery.waitForEndPhaseReplay(nestedLRA);

        Assert.assertEquals("After LRA method for nested LRA enlist should have been called",
            1, lraMetricService.getMetric(LRAMetricType.AfterLRA, nestedLRA));

    }
}
