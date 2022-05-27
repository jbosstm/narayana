/*
 *
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 */

package io.narayana.lra.arquillian;

import io.narayana.lra.arquillian.resource.LRAParticipantAfterLRA;
import org.eclipse.microprofile.lra.tck.service.spi.LRACallbackException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URL;

public class LRAParticipantAfterLRAIT extends TestBase {

    private static final Logger log = Logger.getLogger(LRAParticipantAfterLRAIT.class);

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
        return Deployer.deploy(LRAParticipantAfterLRAIT.class.getSimpleName(), LRAParticipantAfterLRA.class);
    }

    @Test
    public void testAfterLRACount() throws LRACallbackException {
        Client client = ClientBuilder.newClient();
        Response response = null;
        try {
            response = client.target(UriBuilder.fromUri(baseURL.toExternalForm())
                    .path(LRAParticipantAfterLRA.SIMPLE_PARTICIPANT_RESOURCE_PATH)
                    .path(LRAParticipantAfterLRA.DO_LRA_PATH).build()).request().put(null);

            Assert.assertEquals(200, response.getStatus());
            Assert.assertTrue(response.hasEntity());

        } finally {
            if (response != null) {
                response.close();
            }
        }

        try {
            response = client.target(UriBuilder.fromUri(baseURL.toExternalForm())
                    .path(LRAParticipantAfterLRA.SIMPLE_PARTICIPANT_RESOURCE_PATH)
                    .path(LRAParticipantAfterLRA.COUNTER_LRA_PATH).build()).request().get();

            Assert.assertEquals(2, Integer.parseInt(response.readEntity(String.class)));
        } finally {
            if (response != null) {
                response.close();
            }
        }

    }

}
