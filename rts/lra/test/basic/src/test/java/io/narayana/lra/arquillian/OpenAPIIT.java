/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2022, Red Hat, Inc., and individual contributors
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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import javax.ws.rs.core.Response;
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
