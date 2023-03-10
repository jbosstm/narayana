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

import io.narayana.lra.arquillian.resource.NonRootLRAParticipant;
import io.narayana.lra.arquillian.resource.RootResource;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.eclipse.microprofile.lra.annotation.LRAStatus.Closing;
import static org.junit.Assert.fail;

public class NonRootLRAParticipantIT extends TestBase {
    private static final Logger log = Logger.getLogger(NonRootLRAParticipantIT.class);

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
        return Deployer.deploy(NonRootLRAParticipantIT.class.getSimpleName(), RootResource.class, NonRootLRAParticipant.class);
    }

    @Test
    public void testNonRootLRAParticipantEnlist() {

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

        URI lraId = lraClient.startLRA("testFinishLRA");
        lrasToAfterFinish.add(lraId);
        URI compensateURI = new URI(baseURL.toExternalForm() + "/participant/compensate");
        URI completeURI = new URI(baseURL.toExternalForm() + "/participant/complete");

        // enlist the participant with these compensate and complete URIs into the LRA
        lraClient.joinLRA(lraId, 0L, compensateURI, completeURI, null, null, null, null, (StringBuilder) null);
        lraClient.closeLRA(lraId);

        try {
            LRAStatus status = lraClient.getStatus(lraId);

            Assert.assertEquals("wrong state", Closing, status);
        } catch (WebApplicationException e) {
            fail("testFinishLRA: received unexpected response code (" + e.getResponse().getStatus()
                    + ") getting LRA status " + e.getMessage());
        }

    }
}
