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

import io.narayana.lra.arquillian.resource.JaxRsApplication;
import io.narayana.lra.arquillian.resource.NonRootLRAParticipant;
import io.narayana.lra.arquillian.resource.RootResource;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URL;

@RunWith(Arquillian.class)
public class NonRootLRAParticipantIT {

    @ArquillianResource
    private URL baseURL;

    @Deployment
    public static WebArchive deploy() {
        File[] files = Maven.resolver().loadPomFromFile("pom.xml")
            .importRuntimeDependencies().resolve().withTransitivity().asFile();

        return ShrinkWrap.create(WebArchive.class, NonRootLRAParticipantIT.class.getSimpleName() + ".war")
            .addClasses(NonRootLRAParticipant.class, RootResource.class, JaxRsApplication.class)
            .addPackages(true, Compensate.class.getPackage())
            .addPackages(true, "io.narayana.lra")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    @Ignore
    public void testNonRootLRAParticipantEnlist() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(baseURL.toExternalForm() + "/root/participant/lra").request().get();

        Assert.assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());

        response = client.target(baseURL.toExternalForm() + "/root/participant/counter").request().get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        int counterValue = response.readEntity(Integer.class);
        Assert.assertEquals("Non root JAX-RS participant should have been enlisted and invoked",
            1, counterValue);
    }
}
