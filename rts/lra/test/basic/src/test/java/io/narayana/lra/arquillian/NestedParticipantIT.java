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
import org.eclipse.microprofile.lra.annotation.LRAStatus;
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
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URL;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

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

        URI parentLRA = narayanaLRAClient.startLRA(NestedParticipantIT.class.getName() + "#nestedParticipantAfterLRACallTest");

        URI nestedLRA = null;

        try {
            response = client.target(UriBuilder.fromUri(baseURL.toExternalForm())
                .path(NestedParticipant.ROOT_PATH)
                .path(NestedParticipant.ENLIST_PATH))
                .request()
                .header(LRA.LRA_HTTP_CONTEXT_HEADER, parentLRA)
                .get();

            assertEquals(200, response.getStatus());
            Assert.assertTrue(response.hasEntity());

            nestedLRA = URI.create(response.readEntity(String.class));
            assertNotEquals(parentLRA, nestedLRA);
            assertEquals(1, lraMetricService.getMetric(LRAMetricType.Nested, parentLRA, NestedParticipant.class));
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

        assertEquals(1, lraMetricService.getMetric(LRAMetricType.Completed, nestedLRA, NestedParticipant.class));
        assertEquals(2, lraMetricService.getMetric(LRAMetricType.Nested, parentLRA, NestedParticipant.class));
        assertEquals(0, lraMetricService.getMetric(LRAMetricType.AfterLRA, nestedLRA, NestedParticipant.class));

        narayanaLRAClient.closeLRA(parentLRA);
        narayanaLRARecovery.waitForEndPhaseReplay(nestedLRA);

        assertEquals("After LRA method for nested LRA enlist should have been called",
            1, lraMetricService.getMetric(LRAMetricType.AfterLRA, nestedLRA, NestedParticipant.class));

    }

    // verify that starting and closing an LRA with no participants works
    @Test
    public void isSane() {
        URI lra = narayanaLRAClient.startLRA("NestedParticipantIT#isSane");
        narayanaLRAClient.closeLRA(lra);

        assertClosed("LRA should be closed", lra);
    }

    // test that closing the top level of a transaction hierarchy leaves no LRAs associated with the calling thread
    @Test
    public void testGrandparentContext() {
        assertNull("testGrandparentContext: current thread should not be associated with any LRAs",
                narayanaLRAClient.getCurrent());

        // start a hierarchy of three LRAs
        URI grandParent = narayanaLRAClient.startLRA("NestedParticipantIT#testGrandparentContext grandparent");
        URI parent = narayanaLRAClient.startLRA("NestedParticipantIT#testGrandparentContext parent"); // child of grandParent
        URI child = narayanaLRAClient.startLRA("NestedParticipantIT#testGrandparentContext child"); // child of parent

        narayanaLRAClient.closeLRA(grandParent); // should close everything in the hierarchy

        // nothing should be associated with the calling thread
        assertNull("testGrandparentContext: current thread should not be associated with any LRAs",
                narayanaLRAClient.getCurrent());

        // and verify they are all closed - narayanaLRAClient.getStatus(grandParent)
        assertClosed("grandparent", grandParent);
        assertClosed("parent", parent);
        assertClosed("child", child);
    }

    // test that the parent context header is propagated on JAX-RS invocations
    @Test
    public void testParentContext() {
        // start a top level LRA
        URI parent = narayanaLRAClient.startLRA(NestedParticipantIT.class.getName() + "#testParentContext parent");
        // and nest another one under it
        URI child = narayanaLRAClient.startLRA(NestedParticipantIT.class.getName() + "#testParentContext child");

        Response response = null;

        try {
            response = client.target(UriBuilder.fromUri(baseURL.toExternalForm())
                    .path(NestedParticipant.ROOT_PATH)
                    .path(NestedParticipant.PATH))
                    .request()
                    .get();

            assertEquals("parent context was not propagated correctly: " + response.readEntity(String.class),
                    200, response.getStatus());
        } finally {
            if (response != null) {
                response.close();
            }

            narayanaLRAClient.closeLRA(parent);
            assertNull("testParentContext: close LRA is still on the current thread", narayanaLRAClient.getCurrent());
        }

        // verify that the child is closed:
        try {
            // the status of child should be NOT_FOUND or finishing
            assertNotEquals("child should no longer be active", LRAStatus.Active, narayanaLRAClient.getStatus(child));
        } catch (WebApplicationException e) {
            assertEquals("child should be finished", NOT_FOUND.getStatusCode(), e.getResponse().getStatus());
        }
    }

    // verify that an LRA is closed
    private void assertClosed(String msg, URI lra) {
        try {
            assertEquals(msg, LRAStatus.Closed, narayanaLRAClient.getStatus(lra));
        } catch (NotFoundException ignore) {
        }
    }
}
