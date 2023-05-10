/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2023, Red Hat, Inc., and individual contributors
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

import io.narayana.lra.arquillian.resource.LRAUnawareResource;
import io.narayana.lra.arquillian.resource.SimpleLRAParticipant;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.net.URI;
import java.net.URL;

import static org.junit.Assert.assertNotEquals;

public class LRAPropagationIT extends TestBase {
    private static final Logger log = Logger.getLogger(LRAPropagationIT.class);

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
        return Deployer.deploy(LRAPropagationIT.class.getSimpleName(), LRAUnawareResource.class, SimpleLRAParticipant.class)
            .addAsManifestResource(
                new StringAsset("mp.lra.propagation.active=false"), "microprofile-config.properties");
    }

    @Test
    public void noLRATest() throws WebApplicationException {
        URI lraId = lraClient.startLRA(LRAPropagationIT.class.getName());

        URI returnedLraId = invokeInTransaction(baseURL, LRAUnawareResource.ROOT_PATH,
            LRAUnawareResource.RESOURCE_PATH, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        assertNotEquals("While calling non-LRA method the resource should not propagate the LRA id when mp.lra.propagation.active=false",
            lraId, returnedLraId);

        lraClient.closeLRA(lraId);
    }
}
