/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.arquillian;

import io.narayana.lra.arquillian.filter.LinkCapturingFilter;
import io.narayana.lra.arquillian.resource.SimpleLRAParticipant;
import jakarta.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.net.URL;

import static io.narayana.lra.arquillian.resource.SimpleLRAParticipant.SIMPLE_PARTICIPANT_RESOURCE_PATH;
import static io.narayana.lra.arquillian.resource.SimpleLRAParticipant.START_LRA_PATH;
import static org.junit.Assert.assertTrue;

/**
 * Test that the Link header sent to coordinator can be changed with the custom base URI.
 */
public class LRACustomBaseURIIT extends TestBase {
    private static final Logger log = Logger.getLogger(LRACustomBaseURIIT.class);

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
        return Deployer.deploy(LRACustomBaseURIIT.class.getSimpleName(), SimpleLRAParticipant.class, LinkCapturingFilter.class)
            .addAsManifestResource(
                new StringAsset("narayana.lra.base-uri=http://example.com"), "microprofile-config.properties");
    }

    @Test
    public void noLRATest() throws Exception {
        try(Response response = client.target(baseURL.toURI())
            .path(SIMPLE_PARTICIPANT_RESOURCE_PATH)
            .path(START_LRA_PATH)
            .request()
            .get()) {

            System.out.println(response.getHeaderString("Link"));
            assertTrue("The base URI was not overridden by the configuration",
                response.getHeaderString("Link").contains("http://example.com/compensate?method=jakarta.ws.rs.PUT"));
        }
    }
}