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
