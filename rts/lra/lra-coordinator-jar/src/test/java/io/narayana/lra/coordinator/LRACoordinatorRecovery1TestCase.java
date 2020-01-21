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

import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import io.narayana.lra.Current;
import io.narayana.lra.client.NarayanaLRAClient;
import io.narayana.lra.client.internal.proxy.nonjaxrs.LRAParticipantRegistry;
import io.narayana.lra.coordinator.api.Coordinator;
import io.narayana.lra.coordinator.domain.model.LRAData;
import io.narayana.lra.coordinator.domain.service.LRAService;
import io.narayana.lra.coordinator.internal.LRARecoveryModule;
import io.narayana.lra.filter.ServerLRAFilter;
import io.narayana.lra.logging.LRALogger;
import org.apache.http.HttpConnection;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static io.narayana.lra.coordinator.LRAListener.LRA_LISTENER_ACTION;
import static io.narayana.lra.coordinator.LRAListener.LRA_LISTENER_STATUS;
import static io.narayana.lra.coordinator.LRAListener.LRA_SHORT_TIMELIMIT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test that check that LRA deadlines are respected during crash recovery
 */
@RunWith(Arquillian.class)
public class LRACoordinatorRecovery1TestCase extends TestBase {
    private static Package[] coordinatorPackages = {
            RecoveryModule.class.getPackage(),
            Coordinator.class.getPackage(),
            LRAData.class.getPackage(),
            LRAStatus.class.getPackage(),
            LRALogger.class.getPackage(),
            NarayanaLRAClient.class.getPackage(),
            Current.class.getPackage(),
            LRAService.class.getPackage(),
            LRARecoveryModule.class.getPackage()
    };

    private static Package[] participantPackages = {
            LRAListener.class.getPackage(),
            LRA.class.getPackage(),
            ServerLRAFilter.class.getPackage(),
            LRAParticipantRegistry.class.getPackage()
    };

    private String lraListenerURL;

    private Client client;

    @Deployment(name = COORDINATOR_DEPLOYMENT, testable = false, managed = false)
    public static WebArchive createDeployment() {
        // LRA uses ArjunaCore so pull in the jts module to get them on the classpath
        // (maybe in the future we can add a WFLY LRA subsystem)
        final String ManifestMF = "Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.jts, org.jboss.logging\n";
        return ShrinkWrap.create(WebArchive.class, COORDINATOR_DEPLOYMENT + ".war")
                .addPackages(false, coordinatorPackages)
                .addPackages(false, participantPackages)
                .addPackages(true, HttpConnection.class.getPackage())
                .addAsManifestResource(new StringAsset(ManifestMF), "MANIFEST.MF")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Before
    public void before() throws MalformedURLException, URISyntaxException {
        super.before();

        lraListenerURL = String.format("%s/%s", getDeploymentUrl(), LRAListener.LRA_LISTENER_PATH);
        client = ClientBuilder.newClient();
    }

    @After
    public void after() {
        client.close();
        stopContainer();
        super.after();
    }

    /**
     * Test that an LRA which times out while there is no running coordinator is cancelled
     * when a coordinator is restarted
     * @throws URISyntaxException if the LRA or recovery URIs are invalid (should never happen)
     */
    @Test
    public void testRecovery() throws URISyntaxException, InterruptedException {
        startContainer("participant-byteman-rules");

        String lraId;

        // start an LRA with a short time limit by invoking a resource annotated with @LRA
        try (Response response = client.target(lraListenerURL).path(LRA_LISTENER_ACTION)
                .request()
                .put(null)) {

            Assert.assertEquals("LRA participant action", 200, response.getStatus());

            lraId = response.readEntity(String.class);
            fail(testName + ": byteman should have killed the container");
        } catch (RuntimeException e) {
            LRALogger.logger.infof("%s: byteman killed the container", testName);
            // we could have started the LRA via lraClient (which we do in the next test) but it is useful to test the filters
            lraId = getFirstLRA();
            assertNotNull("LRA should have been added to the object store before byteman killed the JVM", lraId);
            lraId = String.format("%s/%s", getCoordinatorUrl(), lraId);
        }

        // the byteman script should have killed the JVM
        // wait for a period longer than the timeout before restarting the coordinator
        doWait(LRA_SHORT_TIMELIMIT * 1000);

        restartContainer();

        // check recovery
        LRAStatus status = getStatus(new URI(lraId));

        LRALogger.logger.infof("%s: Status after restart is %s%n", status == null ? "GONE" : status.name());

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

        // verify that the resource was notified that the LRA finished
        String listenerStatus = getStatusFromListener();

        assertEquals("LRA listener should have been told that the final state of the LRA was cancelled",
                LRAStatus.Cancelled.name(), listenerStatus);
    }

    /**
     * Ask {@link LRAListener} if it has been notified of the final outcome of the LRA
     * @return the listeners view of the LRA status
     */
    private String getStatusFromListener() {
        try (Response response = client.target(lraListenerURL).path(LRA_LISTENER_STATUS)
                .request()
                .get()) {

            Assert.assertEquals("LRA participant HTTP status", 200, response.getStatus());

            return response.readEntity(String.class);
        }
    }
}
