/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.arquillian.client;

import io.narayana.lra.arquillian.Deployer;
import io.narayana.lra.arquillian.TestBase;
import io.narayana.lra.arquillian.resource.LRAParticipant;
import io.narayana.lra.client.NarayanaLRAClient;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URL;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LRAIT extends TestBase {

    private static final Logger log = Logger.getLogger(LRAIT.class);
    private static final String SHOULD_NOT_BE_ASSOCIATED =
            "The narayana implementation (of the MP-LRA specification) still thinks that there is "
                + "an active LRA associated with the current thread even though all LRAs should now be finished";

    @ArquillianResource
    public URL baseURL;

    @Rule
    public TestName testName = new TestName();

    @Override
    public void before() {
        super.before();
        log.info("Running test " + testName.getMethodName());
    }

    @Deployment
    public static WebArchive deploy() {
        return Deployer.deploy(LRAIT.class.getSimpleName(), LRAParticipant.class);
    }

    /**
     * Invoke a resource method which in turn invokes other resources.
     * The various resource invocations are called in new or existing LRAs.
     * Various tests are performed to verify that the correct LRAs are used
     * and the LRAs have the expected status (see the resource method for
     * the detail).
     */
    @Test
    public void testChainOfInvocations() {
        NarayanaLRAClient lraClient = new NarayanaLRAClient();

        // Invoke a method which starts a transaction
        // (note that the method LRAParticipant.CREATE_OR_CONTINUE_LRA also invokes other resource methods)
        URI lra1 = invokeInTransaction(null, LRAParticipant.CREATE_OR_CONTINUE_LRA);
        lrasToAfterFinish.add(lra1);
        assertEquals("LRA should still be active. The identifier of the LRA was " + lra1,
                LRAStatus.Active, lraClient.getStatus(lra1));

        // end the LRA
        invokeInTransaction(lra1, LRAParticipant.END_EXISTING_LRA);

        assertNull(SHOULD_NOT_BE_ASSOCIATED, lraClient.getCurrent());
    }

    /**
     * test behaviour when multiple LRAs are active
     */
    @Test
    public void testNoCurrent() {
        NarayanaLRAClient lraClient = new NarayanaLRAClient();

        URI lra1 = invokeInTransaction(null, LRAParticipant.START_NEW_LRA);
        lrasToAfterFinish.add(lra1);
        assertNull(SHOULD_NOT_BE_ASSOCIATED, lraClient.getCurrent());
        URI lra2 = invokeInTransaction(null, LRAParticipant.START_NEW_LRA);
        lrasToAfterFinish.add(lra2);
        assertNull(SHOULD_NOT_BE_ASSOCIATED, lraClient.getCurrent());

        invokeInTransaction(lra1, LRAParticipant.END_EXISTING_LRA);
        assertNull(SHOULD_NOT_BE_ASSOCIATED, lraClient.getCurrent());
        invokeInTransaction(lra2, LRAParticipant.END_EXISTING_LRA);
        assertNull(SHOULD_NOT_BE_ASSOCIATED, lraClient.getCurrent());
    }

    /**
     * Test that remote invocations can start and stop LRAs using the proprietary client API
     */
    @Test
    public void testRemoteCurrent() {
        NarayanaLRAClient lraClient = new NarayanaLRAClient();

        URI lra1 = invokeInTransaction(null, LRAParticipant.CREATE_OR_CONTINUE_LRA2);
        lrasToAfterFinish.add(lra1);
        invokeInTransaction(lra1, LRAParticipant.END_EXISTING_LRA);
        assertNull(SHOULD_NOT_BE_ASSOCIATED, lraClient.getCurrent());
    }

    /**
     * JBTM-3223 Validate LRA behaviour when multiple LRA's are active on the same thread
     * <p>
     * when multiple LRA's are active on the same thread that as one LRA finishes that:
     * a) the next one is handled correctly
     * b) creating another one still works fine
     */
    @Test
    public void testCurrent() {
        NarayanaLRAClient lraClient = new NarayanaLRAClient();

        // start two LRAs on the current thread
        URI lra1 = lraClient.startLRA("lra1");
        lrasToAfterFinish.add(lra1);
        assertEquals("lra1 is not associated with the current thread",
                lra1, lraClient.getCurrent());
        URI lra2 = lraClient.startLRA("lra2");
        lrasToAfterFinish.add(lra2);
        assertEquals("lra2 is not associated with the current thread",
                lra2, lraClient.getCurrent());

        // a) closing the current one should make the previous one active
        lraClient.closeLRA(lra2);
        assertEquals("closing the current LRA should have made the previous one active",
                lra1, lraClient.getCurrent());

        // b) verify that creating another LRA still works fine
        URI lra3 = lraClient.startLRA("lra3");
        lrasToAfterFinish.add(lra3);
        assertEquals("lra3 is not associated with the current thread",
                lra3, lraClient.getCurrent());

        lraClient.closeLRA(lra3);
        assertEquals("closing the current LRA (lra3) should have made the previous one (lra1) active",
                lra1, lraClient.getCurrent());

        // check that closing the last LRA will leave none active
        lraClient.closeLRA(lra1);
        assertNull("all LRAs are closed so none should be associated with the calling thread",
                lraClient.getCurrent());

        lraClient.close();
    }

    private URI invokeInTransaction(URI lra, String resourcePath) {
        Response response = null;

        try {
            Invocation.Builder builder = client.target(UriBuilder.fromUri(baseURL.toExternalForm())
                    .path(LRAParticipant.RESOURCE_PATH)
                    .path(resourcePath).build())
                    .request();

            if (lra != null) {
                builder.header(LRA_HTTP_CONTEXT_HEADER, lra.toASCIIString());
            }

            response = builder.get();

            assertTrue("This test expects that the invoked resource returns the identifier of the LRA " +
                    "that was active during the invocation or an error message.",
                    response.hasEntity());

            String responseMessage = response.readEntity(String.class);

            assertEquals(responseMessage, 200, response.getStatus());

            return URI.create(responseMessage);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}