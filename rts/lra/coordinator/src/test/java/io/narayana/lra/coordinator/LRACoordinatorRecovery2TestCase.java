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
import io.narayana.lra.LRAData;
import io.narayana.lra.coordinator.domain.model.LongRunningAction;
import io.narayana.lra.coordinator.domain.service.LRAService;
import io.narayana.lra.coordinator.internal.LRARecoveryModule;
import io.narayana.lra.filter.ServerLRAFilter;
import io.narayana.lra.logging.LRALogger;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
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
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.temporal.ChronoUnit;

import static io.narayana.lra.coordinator.LRAListener.LRA_LISTENER_KILL;
import static io.narayana.lra.coordinator.LRAListener.LRA_LISTENER_STATUS;
import static io.narayana.lra.coordinator.LRAListener.LRA_LISTENER_UNTIMED_ACTION;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test that check that LRA deadlines are respected during crash recovery
 */
@RunWith(Arquillian.class)
@RunAsClient
public class LRACoordinatorRecovery2TestCase extends TestBase {
    private static final Long LONG_TIMEOUT = TimeoutValueAdjuster.adjustTimeout(600000L); // 10 minutes
    private static final Long SHORT_TIMEOUT = 10000L; // 10 seconds

    private static final Package[] coordinatorPackages = {
            RecoveryModule.class.getPackage(),
            Coordinator.class.getPackage(),
            LRAData.class.getPackage(),
            LRAStatus.class.getPackage(),
            LRALogger.class.getPackage(),
            NarayanaLRAClient.class.getPackage(),
            Current.class.getPackage(),
            LRAService.class.getPackage(),
            LRARecoveryModule.class.getPackage(),
            LongRunningAction.class.getPackage()
    };

    private static final Package[] participantPackages = {
            LRAListener.class.getPackage(),
            LRA.class.getPackage(),
            ServerLRAFilter.class.getPackage(),
            LRAParticipantRegistry.class.getPackage()
    };

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
                .addAsManifestResource(new StringAsset(ManifestMF), "MANIFEST.MF")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Before
    public void before() throws MalformedURLException, URISyntaxException {
        super.before();

        client = ClientBuilder.newClient();
        startContainer(null);
    }

    @After
    public void after() {
        client.close();
        stopContainer();
        super.after();
    }

    /**
     * Test that an LRA which times out while there is no running coordinator is cancelled
     * when a coordinator is restarted.
     *
     * Test that an LRA which times out after a coordinator is restarted after a crash is still active
     */
    @Test
    public void testRecovery2(@ArquillianResource @OperateOnDeployment(COORDINATOR_DEPLOYMENT) URL deploymentUrl) throws URISyntaxException, InterruptedException {
        URI lraListenerURI = UriBuilder.fromUri(deploymentUrl.toURI()).path(LRAListener.LRA_LISTENER_PATH).build();

        // start an LRA with a long timeout to validate that timed LRAs do not finish early during recovery
        URI longLRA = lraClient.startLRA(null, "Long Timeout Recovery Test", LONG_TIMEOUT, ChronoUnit.MILLIS);
        // start an LRA with a short timeout to validate that timed LRAs that time out when the coordinator is unavailable are cancelled
        URI shortLRA = lraClient.startLRA(null, "Short Timeout Recovery Test", SHORT_TIMEOUT, ChronoUnit.MILLIS);

        // invoke a method that will trigger a byteman rule to kill the JVM
        try (Response ignore = client.target(lraListenerURI).path(LRA_LISTENER_KILL)
                .request()
                .get()) {

            fail(testName + ": the container should have halted");
        } catch (RuntimeException e) {
            LRALogger.logger.infof("%s: the container halted", testName);
        }

        // waiting for the short LRA timeout really expires
        Thread.sleep(TimeoutValueAdjuster.adjustTimeout(SHORT_TIMEOUT));

        // restart the container
        restartContainer();

        // check that on restart an LRA whose deadline has expired are cancelled
        int sc = recover();

        if (sc != 0) {
            recover();
        }

        LRAStatus longStatus = getStatus(longLRA);
        LRAStatus shortStatus = getStatus(shortLRA);

        Assert.assertEquals("LRA with long timeout should still be active",
                LRAStatus.Active.name(), longStatus.name());
        Assert.assertTrue("LRA with short timeout should not be active",
                shortStatus == null ||
                        LRAStatus.Cancelled.equals(shortStatus) || LRAStatus.Cancelling.equals(shortStatus));

        // verify that it is still possible to join in with the LRA
        try (Response response = client.target(lraListenerURI).path(LRA_LISTENER_UNTIMED_ACTION)
                .request()
                .header(LRA_HTTP_CONTEXT_HEADER, longLRA)
                .put(Entity.text(""))) {

            Assert.assertEquals("LRA participant action", 200, response.getStatus());
        }

        // closing the LRA and clearing the active thread of the launched LRAs
        lraClient.closeLRA(longLRA);
        lraClient.closeLRA(shortLRA);

        // check that the participant was notified that the LRA has closed
        String listenerStatus = getStatusFromListener(lraListenerURI);

        assertEquals("LRA listener should have been told that the final state of the LRA was closed",
                LRAStatus.Closed.name(), listenerStatus);
    }

    /**
     * Ask {@link LRAListener} if it has been notified of the final outcome of the LRA
     * @return the listeners view of the LRA status
     */
    private String getStatusFromListener(URI lraListenerURI) {
        try (Response response = client.target(lraListenerURI).path(LRA_LISTENER_STATUS)
                .request()
                .get()) {

            Assert.assertEquals("LRA participant HTTP status", 200, response.getStatus());

            return response.readEntity(String.class);
        }
    }
}
