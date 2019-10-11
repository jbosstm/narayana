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

import io.narayana.lra.client.NarayanaLRAClient;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.eclipse.microprofile.lra.annotation.LRAStatus.Closing;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class NonRootLRAParticipantIT {

    @ArquillianResource
    private URL baseURL;

    @Deployment
    public static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class, NonRootLRAParticipantIT.class.getSimpleName() + ".war");
    }

    @Test
    public void testNonRootLRAParticipantEnlist() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(baseURL.toExternalForm() + "root/participant/lra").request().get();

        Assert.assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());

        response = client.target(baseURL.toExternalForm() + "root/participant/counter").request().get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        int counterValue = response.readEntity(Integer.class);
        Assert.assertEquals("Non root JAX-RS participant should have been enlisted and invoked",
            1, counterValue);
    }

    /*
     * Test that a participant can join an LRA using the Narayana specific client API
     * Test that the LRA has the correct status of Closing if a participant
     * returns 202 Accepted when asked to complete.
     * @throws URISyntaxException if arquillian URL is invalid
     */
    @Test
    public void testFinishLRA() throws URISyntaxException {
        NarayanaLRAClient client = new NarayanaLRAClient(); // the narayana client API for using LRAs
        URI lraId = client.startLRA("testFinishLRA");
        URI compensateURI = new URI(baseURL.toExternalForm() + "/participant/compensate");
        URI completeURI = new URI(baseURL.toExternalForm() + "/participant/complete");

        // enlist the participant with these compensate and complete URIs into the LRA
        client.joinLRA(lraId, 0L, compensateURI, completeURI, null, null, null, null, null);
        client.closeLRA(lraId);

        try {
            LRAStatus status = client.getStatus(lraId, false);

            Assert.assertEquals("wrong state", Closing, status);
        } catch (WebApplicationException e) {
            fail("testFinishLRA: received unexpected response code (" + e.getResponse().getStatus()
                    + ") getting LRA status " + e.getMessage());
        }

    }
}
