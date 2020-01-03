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
import io.narayana.lra.arquillian.resource.TestBase;
import io.narayana.lra.arquillian.spi.NarayanaLRARecovery;
import org.eclipse.microprofile.lra.tck.service.LRAMetricService;
import org.eclipse.microprofile.lra.tck.service.LRAMetricType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URL;

@RunWith(Arquillian.class)
public class FailingParticipantCallsIT {

    @Inject
    private LRAMetricService lraMetricService;

    @ArquillianResource
    private URL baseURL;

    @Deployment
    public static WebArchive deploy() {
        return TestBase.deploy(FailingParticipantCallsIT.class.getSimpleName() + ".war");
    }

    @Before
    public void before() {
        lraMetricService.clear();
    }

    @Test
    public void testFailingAfterLRA() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(UriBuilder.fromUri(baseURL.toExternalForm())
            .path(FailingAfterLRAListener.ROOT_PATH)
            .path(FailingAfterLRAListener.ACTION_PATH).build())
            .request()
            .get();

        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue(response.hasEntity());

        URI lra = URI.create(response.readEntity(String.class));

        new NarayanaLRARecovery().waitForEndPhaseReplay(lra);

        Assert.assertEquals(3,
            lraMetricService.getMetric(LRAMetricType.AfterLRA, lra, FailingAfterLRAListener.class.getName()));
    }
}
