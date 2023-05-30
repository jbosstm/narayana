/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */


package io.narayana.lra.arquillian;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import jakarta.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class OpenAPIIT extends TestBase {
    private static final Logger log = Logger.getLogger(OpenAPIIT.class);

    @Rule
    public TestName testName = new TestName();

    @Override
    public void before() {
        super.before();
        log.info("Running test " + testName.getMethodName());
    }

    @Deployment
    public static WebArchive deploy() {
        return Deployer.deploy(OpenAPIIT.class.getSimpleName());
    }

    @Test
    public void test() throws URISyntaxException, MalformedURLException {
        URL url = new URL("http://" + System.getProperty("lra.coordinator.host", "localhost")+":"+ System.getProperty("lra.coordinator.port", "8080")+"/openapi");
        Response response = client.target(url.toURI()).request().get();
        String output = response.readEntity(String.class);
        org.junit.Assert.assertTrue("WildFly OpenAPI document has paths at wrong location", !output.contains("/lra-coordinator/lra-coordinator:"));
        org.junit.Assert.assertTrue("WildFly OpenAPI document does not have paths for expected location", output.contains("/lra-coordinator:"));
        org.junit.Assert.assertTrue("WildFly OpenAPI document does not have server URL", output.contains("url: /lra-coordinator"));
    }
}