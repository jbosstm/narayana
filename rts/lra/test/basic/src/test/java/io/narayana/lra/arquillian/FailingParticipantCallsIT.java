/*
 * SPDX short identifier: Apache-2.0
 */


package io.narayana.lra.arquillian;

import io.narayana.lra.arquillian.resource.FailingAfterLRAListener;
import io.narayana.lra.arquillian.spi.NarayanaLRARecovery;
import org.eclipse.microprofile.lra.tck.service.spi.LRACallbackException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URL;

public class FailingParticipantCallsIT extends TestBase {

    private static final Logger log = Logger.getLogger(FailingParticipantCallsIT.class);

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
        return Deployer.deploy(FailingParticipantCallsIT.class.getSimpleName(), FailingAfterLRAListener.class);
    }

    @Test
    public void testFailingAfterLRA() throws LRACallbackException {
        Client client = ClientBuilder.newClient();
        Response response = null;
        URI lra = null;

        try {
            response = client.target(UriBuilder.fromUri(baseURL.toExternalForm())
                .path(FailingAfterLRAListener.ROOT_PATH)
                .path(FailingAfterLRAListener.ACTION_PATH).build())
                .request()
                .get();

            Assert.assertEquals(200, response.getStatus());
            Assert.assertTrue(response.hasEntity());

            lra = URI.create(response.readEntity(String.class));
            lrasToAfterFinish.add(lra);
        } finally {
            if (response != null) {
                response.close();
            }
        }

        new NarayanaLRARecovery().waitForRecovery(lra);

        try {
            response = client.target(UriBuilder.fromUri(baseURL.toExternalForm())
                .path(FailingAfterLRAListener.ROOT_PATH).path("counter").build())
                .request().get();

            Assert.assertEquals(2, Integer.parseInt(response.readEntity(String.class)));
        } finally {
            if (response != null) {
                response.close();
            }
        }

    }
}