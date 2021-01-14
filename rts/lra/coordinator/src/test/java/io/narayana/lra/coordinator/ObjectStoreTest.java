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
package io.narayana.lra.coordinator;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import io.narayana.lra.coordinator.api.Coordinator;
import io.narayana.lra.coordinator.domain.model.LRATest;
import io.narayana.lra.filter.ServerLRAFilter;
import io.narayana.lra.logging.LRALogger;
import io.narayana.lra.provider.ParticipantStatusOctetStreamProvider;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static io.narayana.lra.coordinator.LRAListener.LRA_SHORT_TIMELIMIT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test that check that LRA deadlines are respected during crash recovery
 */
public class ObjectStoreTest extends TestBase {

    private Client client;

    @Before
    public void before() {
        super.before();

        server.deploy(Coordinator.class);
        server.deploy(LRAParticipant.class);

        clearObjectStore();

        client = ClientBuilder.newClient();
    }

    @After
    public void after() {
        client.close();
        super.after();
    }

    /**
     * Test that an LRA which times out while there is no running coordinator is cancelled
     * when a coordinator is restarted
     * @throws URISyntaxException if the LRA or recovery URIs are invalid (should never happen)
     */
    @Test
    public void objectStoreTest() throws URISyntaxException, InterruptedException {

        LRALogger.logger.infof("Arjuna Properties File is set to: %s",
                System.getProperty("com.arjuna.ats.arjuna.common.propertiesFile"));
        LRALogger.logger.infof("The Arjuna Object Store is set to: %s",
                BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).getObjectStoreType());

        String lraId;

        // start an LRA with a short time limit by invoking a resource annotated with @LRA
        Response response = client.target(TestPortProvider.generateURL("/base/test/short"))
                .request().get();

        Assert.assertEquals("LRAParticipant's response",
                Response.Status.OK.getStatusCode(),
                response.getStatus());

        lraId = response.readEntity(String.class);
        assertNotNull("LRA should have been added to the object store", lraId);

        // wait for a period longer than the timeout before restarting the coordinator
        doWait(LRA_SHORT_TIMELIMIT * 1000);

        // check recovery
        LRAStatus status = getStatus(new URI(lraId));

        LRALogger.logger.infof("%s: Status after restart is %s%n", testName, status == null ? "GONE" : status.name());

        if (status == null || status == LRAStatus.Cancelling) {
            int sc = recover();

            if (sc != 0) {
                recover();
            }
        }

        // the LRA with the short timeout should have timed out and cancelled
        status = getStatus(new URI(lraId));

        Assert.assertTrue("LRA with short timeout should have cancelled",
                status == null || status == LRAStatus.Cancelled);

        try (Response responseListener = client.target(TestPortProvider.generateURL("/base/test/status"))
                .request()
                .get()) {

            assertEquals("LRA participant HTTP status", 200, responseListener.getStatus());

            String listenerStatus = responseListener.readEntity(String.class);

            assertEquals("LRA listener should have been told that the final state of the LRA was cancelled",
                    LRAStatus.Cancelled.name(), listenerStatus);
        }
    }

    @ApplicationPath("/base")
    public static class LRAParticipant extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            HashSet<Class<?>> classes = new HashSet<>();
            classes.add(LRATest.Participant.class);
            classes.add(Coordinator.class);
            classes.add(ServerLRAFilter.class);
            classes.add(ParticipantStatusOctetStreamProvider.class);
            return classes;
        }
    }
}
