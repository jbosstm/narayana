/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.arquillian;

import io.narayana.lra.arquillian.resource.NestedParticipant;
import io.narayana.lra.arquillian.spi.NarayanaLRARecovery;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.lra.tck.service.LRAMetricType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

public class NestedParticipantIT extends TestBase {
    private static final Logger log = Logger.getLogger(NestedParticipantIT.class);
    private final NarayanaLRARecovery narayanaLRARecovery = new NarayanaLRARecovery();

    @ArquillianResource
    public URL baseURL;

    @Rule
    public TestName testName = new TestName();

    @Deployment
    public static WebArchive deploy() {
        return Deployer.deploy(NestedParticipantIT.class.getSimpleName(), NestedParticipant.class);
    }

    @Override
    public void before() {
        super.before();
        log.info("Running test " + testName.getMethodName());

        Response response = client.target(UriBuilder.fromUri(baseURL.toExternalForm())
                .path(NestedParticipant.ROOT_PATH)
                .path(NestedParticipant.RESET_COUNTER))
                .request()
                .get();

        assertEquals(200, response.getStatus());
    }

    /**
     * Verifies that the AfterLRA notification in nested participant is received correctly.
     */
    @Test
    public void nestedParticipantAfterLRACallTest() {
        Response response = null;

        URI parentLRA = lraClient.startLRA(NestedParticipantIT.class.getName() + "#nestedParticipantAfterLRACallTest");
        lrasToAfterFinish.add(parentLRA);

        URI nestedLRA;

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

            response = client.target(UriBuilder.fromUri(baseURL.toExternalForm())
                    .path(NestedParticipant.ROOT_PATH)
                    .path(NestedParticipant.GET_COUNTER)
                    .queryParam("type", LRAMetricType.Nested.name())
                    .queryParam("lraId", URLEncoder.encode(parentLRA.toString(), StandardCharsets.UTF_8)))
                    .request()
                    .get();

            assertEquals(200, response.getStatus());
            Assert.assertTrue(response.hasEntity());

            assertEquals(Integer.valueOf(1), response.readEntity(Integer.class));

        } finally {
            if (response != null) {
                response.close();
            }
        }

        // close nested LRA
        lraClient.closeLRA(nestedLRA);
        // the nested LRA should be in Closed state, however, we keep it in Closing state,
        // so we can't wait for the recovery of the nested LRA
        // https://issues.redhat.com/browse/JBTM-3330
        narayanaLRARecovery.waitForEndPhaseReplay(nestedLRA);

        response = client.target(UriBuilder.fromUri(baseURL.toExternalForm())
                .path(NestedParticipant.ROOT_PATH)
                .path(NestedParticipant.GET_COUNTER)
                .queryParam("type", LRAMetricType.Completed.name())
                .queryParam("lraId", URLEncoder.encode(nestedLRA.toString(), StandardCharsets.UTF_8)))
                .request()
                .get();

        assertEquals(200, response.getStatus());
        Assert.assertTrue(response.hasEntity());

        assertEquals(Integer.valueOf(1), response.readEntity(Integer.class));

        response = client.target(UriBuilder.fromUri(baseURL.toExternalForm())
                .path(NestedParticipant.ROOT_PATH)
                .path(NestedParticipant.GET_COUNTER)
                .queryParam("type", LRAMetricType.Nested.name())
                .queryParam("lraId", URLEncoder.encode(parentLRA.toString(), StandardCharsets.UTF_8)))
                .request()
                .get();

        assertEquals(200, response.getStatus());
        Assert.assertTrue(response.hasEntity());

        assertEquals(Integer.valueOf(2), response.readEntity(Integer.class));

        response = client.target(UriBuilder.fromUri(baseURL.toExternalForm())
                .path(NestedParticipant.ROOT_PATH)
                .path(NestedParticipant.GET_COUNTER)
                .queryParam("type", LRAMetricType.AfterLRA.name())
                .queryParam("lraId", URLEncoder.encode(nestedLRA.toString(), StandardCharsets.UTF_8)))
                .request()
                .get();

        assertEquals(200, response.getStatus());
        Assert.assertTrue(response.hasEntity());

        assertEquals(Integer.valueOf(0), response.readEntity(Integer.class));

        lraClient.closeLRA(parentLRA);
        narayanaLRARecovery.waitForEndPhaseReplay(nestedLRA);

        response = client.target(UriBuilder.fromUri(baseURL.toExternalForm())
                .path(NestedParticipant.ROOT_PATH)
                .path(NestedParticipant.GET_COUNTER)
                .queryParam("type", LRAMetricType.AfterLRA.name())
                .queryParam("lraId", URLEncoder.encode(nestedLRA.toString(), StandardCharsets.UTF_8)))
                .request()
                .get();

        assertEquals(200, response.getStatus());
        Assert.assertTrue(response.hasEntity());

        assertEquals(Integer.valueOf(1), response.readEntity(Integer.class));

    }

    // verify that starting and closing an LRA with no participants works
    @Test
    public void isSane() {
        URI lra = lraClient.startLRA("NestedParticipantIT#isSane");
        lrasToAfterFinish.add(lra);
        lraClient.closeLRA(lra);

        assertClosed("LRA should be closed", lra);
    }

    // test that closing the top level of a transaction hierarchy leaves no LRAs associated with the calling thread
    @Test
    public void testGrandparentContext() {
        assertNull("testGrandparentContext: current thread should not be associated with any LRAs",
                lraClient.getCurrent());

        // start a hierarchy of three LRAs
        URI grandParent = lraClient.startLRA("NestedParticipantIT#testGrandparentContext grandparent");
        lrasToAfterFinish.add(grandParent);
        URI parent = lraClient.startLRA("NestedParticipantIT#testGrandparentContext parent"); // child of grandParent
        URI child = lraClient.startLRA("NestedParticipantIT#testGrandparentContext child"); // child of parent

        lraClient.closeLRA(grandParent); // should close everything in the hierarchy

        // nothing should be associated with the calling thread
        assertNull("testGrandparentContext: current thread should not be associated with any LRAs",
                lraClient.getCurrent());

        // and verify they are all closed - narayanaLRAClient.getStatus(grandParent)
        assertClosed("grandparent", grandParent);
        assertClosed("parent", parent);
        assertClosed("child", child);
    }

    // test that the parent context header is propagated on JAX-RS invocations
    @Test
    public void testParentContext() {
        // start a top level LRA
        URI parent = lraClient.startLRA(NestedParticipantIT.class.getName() + "#testParentContext parent");
        lrasToAfterFinish.add(parent);
        // and nest another one under it
        URI child = lraClient.startLRA(NestedParticipantIT.class.getName() + "#testParentContext child");

        try (Response response = client.target(UriBuilder.fromUri(baseURL.toExternalForm())
                        .path(NestedParticipant.ROOT_PATH)
                        .path(NestedParticipant.PATH))
                .request()
                .get()) {

            assertEquals("parent context was not propagated correctly: " + response.readEntity(String.class),
                    200, response.getStatus());
        } finally {
            lraClient.closeLRA(parent);
            assertNull("testParentContext: close LRA is still on the current thread", lraClient.getCurrent());
        }

        // verify that the child is closed:
        try {
            // the status of child should be NOT_FOUND or finishing
            assertNotEquals("child should no longer be active", LRAStatus.Active, lraClient.getStatus(child));
        } catch (WebApplicationException e) {
            assertEquals("child should be finished", NOT_FOUND.getStatusCode(), e.getResponse().getStatus());
        }
    }

    // verify that an LRA is closed
    private void assertClosed(String msg, URI lra) {
        try {
            assertEquals(msg, LRAStatus.Closed, lraClient.getStatus(lra));
        } catch (NotFoundException ignore) {
        }
    }
}