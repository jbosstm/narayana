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
import org.eclipse.microprofile.lra.tck.service.spi.LRARecoveryService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URL;

@RunWith(Arquillian.class)
public class FailingParticipantCallsIT {

    @ArquillianResource
    private URL baseURL;

    private Client client;

    @Deployment
    public static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class, FailingParticipantCallsIT.class.getSimpleName() + ".war")
            .addClasses(LRARecoveryService.class, LRACallbackException.class);
    }

    @Before
    public void before() {
        client = ClientBuilder.newClient();
    }

    @After
    public void after() {
        if (client != null) {
            client.close();
        }
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
