/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.arquillian;

import io.narayana.lra.arquillian.resource.ParticipantDataResource;
import io.narayana.lra.arquillian.resource.LRAParticipantWithStatusURI;
import io.narayana.lra.arquillian.resource.LRAParticipantWithoutStatusURI;
import io.narayana.lra.arquillian.resource.ParticipantDataResource2;
import io.narayana.lra.arquillian.spi.NarayanaLRARecovery;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.lra.tck.service.spi.LRACallbackException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

/**
 * There is a spec requirement to report failed LRAs but the spec only requires that a failure message is reported
 * (not how it is reported). Failure records are vital pieces of data needed to aid failure tracking and analysis.
 *
 * The Narayana implementation allows failed LRAs to be directly queried. The following tests validate that the
 * correct failure records are kept until explicitly removed.
 */
public class ParticipantDataIT extends TestBase {
    private static final Logger log = Logger.getLogger(ParticipantDataIT.class);

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
        return Deployer.deploy(ParticipantDataIT.class.getSimpleName(),
                LRAParticipantWithStatusURI.class,
                LRAParticipantWithoutStatusURI.class,
                ParticipantDataResource.class,
                ParticipantDataResource2.class);
    }

    @Test
    public void testSimple() throws LRACallbackException, URISyntaxException {
        URI lraId = new URI(invoke(null, ParticipantDataResource.SIMPLE_PARTICIPANT_RESOURCE_PATH,
                ParticipantDataResource.START_LRA_PATH, Response.Status.OK.getStatusCode()));

        lrasToAfterFinish.add(lraId);

        // wait for recovery to recover the LRA (should call @Status followed by @Forget)
        new NarayanaLRARecovery().waitForRecovery(lraId);

        String calls = invoke(null, ParticipantDataResource.SIMPLE_PARTICIPANT_RESOURCE_PATH,
                ParticipantDataResource.CALLS_PATH, Response.Status.OK.getStatusCode());

        Assert.assertTrue("missing Complete callback: " + calls, calls.contains("@Complete"));
        Assert.assertTrue("missing Status callback: " + calls, calls.contains("@Status"));
        Assert.assertTrue("missing Forget callback: " + calls, calls.contains("@Forget"));
        Assert.assertTrue("missing AfterLRA callback: " + calls, calls.contains("@AfterLRA"));
    }

    @Test
    public void testPropagateLRAData() throws URISyntaxException {
        // invoke a resource that starts an LRA
        URI lraId = new URI(invoke(null, ParticipantDataResource2.DATA_PARTICIPANT_RESOURCE_PATH,
                ParticipantDataResource2.START_LRA_PATH, Response.Status.OK.getStatusCode()));
        // invoke a second resource in the same context
        String data = invoke(lraId, ParticipantDataResource2.DATA_PARTICIPANT_RESOURCE_PATH,
                ParticipantDataResource2.END_LRA_PATH, Response.Status.OK.getStatusCode());

        lrasToAfterFinish.add(lraId);

        Assert.assertEquals("wrong data in bean on second call", ParticipantDataResource2.START_DATA, data);
    }

    private String invoke(URI lraId, String resourcePrefix, String resourcePath, int expectedStatus) {
        try (Response response = client.target(baseURL.toURI())
                .path(resourcePrefix)
                .path(resourcePath)
                .request()
                .header(LRA_HTTP_CONTEXT_HEADER, lraId)
                .get()) {

            Assert.assertEquals(
                    "Unexpected response status from " + resourcePrefix + "/" + resourcePath + " was ",
                    expectedStatus, response.getStatus());

            Assert.assertTrue("Expecting a non empty body in response from " + resourcePrefix + "/" + resourcePath,
                    response.hasEntity());

            return response.readEntity(String.class);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("response cannot be converted to URI: " + e.getMessage());
        }
    }
}
