/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package io.narayana.lra.coordinator.domain.model;

import static io.narayana.lra.LRAConstants.COORDINATOR_PATH_NAME;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_PARENT_CONTEXT_HEADER;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import io.narayana.lra.client.NarayanaLRAClient;
import io.narayana.lra.coordinator.api.Coordinator;
import io.narayana.lra.filter.ServerLRAFilter;
import io.narayana.lra.logging.LRALogger;
import io.narayana.lra.provider.ParticipantStatusOctetStreamProvider;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

public class LRAWithParticipantsTest extends LRATestBase {

    @Rule
    public TestName testName = new TestName();
    private UndertowJaxrsServer server;
    private NarayanaLRAClient lraClient;
    private static ReentrantLock lock = new ReentrantLock();
    private static boolean joinAttempted;
    private static boolean compensateCalled;
    @Path("/test")
    public static class ParticipantExtended extends Participant {

        @PUT
        @Path("/compensate")
        @Compensate
        public Response compensate(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI contextLRA,
                @HeaderParam(LRA_HTTP_PARENT_CONTEXT_HEADER) URI parentLRA) {
            synchronized (lock) {
                compensateCalled = true;
                lock.notify();
            }
            synchronized (lock) {
                while (!joinAttempted) {
                    try {
                        lock.wait();
                    }
                    catch (InterruptedException e) {
                        fail("Could not wait");
                    }
                }
            }
            return Response.status(Response.Status.ACCEPTED).entity(ParticipantStatus.Compensating).build();
        }
    }
    @ApplicationPath("service2")
    public static class Service2 extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            HashSet<Class<?>> classes = new HashSet<>();
            classes.add(ParticipantExtended.class);
            classes.add(ServerLRAFilter.class);
            classes.add(ParticipantStatusOctetStreamProvider.class);
            return classes;
        }
    }
    @ApplicationPath("service3")
    public static class Service3 extends Service2 {
    }
    @ApplicationPath("service4")
    public static class Service4 extends Service2 {
    }
    @ApplicationPath("/")
    public static class LRACoordinator extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            HashSet<Class<?>> classes = new HashSet<>();
            classes.add(Coordinator.class);
            return classes;
        }
    }
    @BeforeClass
    public static void start() {
        System.setProperty("lra.coordinator.url", TestPortProvider.generateURL('/' + COORDINATOR_PATH_NAME));
    }

    @Before
    public void before() {
        LRALogger.logger.debugf("Starting test %s", testName);
        server = new UndertowJaxrsServer().start();
        clearObjectStore(testName);
        lraClient = new NarayanaLRAClient();
        server.deploy(LRACoordinator.class);
        server.deployOldStyle(Service2.class);
        server.deployOldStyle(Service3.class);
        server.deployOldStyle(Service4.class);
    }

    @After
    public void after() {
        LRALogger.logger.debugf("Finished test %s", testName);
        lraClient.close();
        clearObjectStore(testName);
        server.stop();
    }

    @Test
    public void testJoinAfterTimeout() {
        // lraClient calls POST /lra-coordinator/start to start a Saga.
        // this simulates the service 1 from the JBTM-3908
        URI lraId = lraClient.startLRA(null, "testTimeLimit", 1000L, ChronoUnit.MILLIS);
        // Service 2 calls PUT /lra-coordinator/{LraId} to join the Saga.
        lraClient.joinLRA(lraId, null, URI.create("http://localhost:8081/service2/test"), null);
        // Service 3 calls PUT /lra-coordinator/{LraId} to join the same Saga.
        lraClient.joinLRA(lraId, null, URI.create("http://localhost:8081/service3/test"), null);
        // A timeout exception occurs in Service 1, leading it to call PUT
        // /lra-coordinator/{LraId}/cancel to cancel the Saga.
        // The LRA Coordinator calls the compensation API /saga/compensate registered
        // by Service 2 and Service 3.
        try {
            TimeUnit.SECONDS.sleep(1);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        synchronized (lock) {
            while (!compensateCalled) {
                try {
                    lock.wait();
                }
                catch (InterruptedException e) {
                    fail("Could not wait");
                }
            }
            // Service 2 receives the /saga/compensate call and begins compensating.
            // Before compensate call is finished, Service 4 calls PUT
            // /lra-coordinator/{LraId} to attempt to join the Saga.
            // Exception is thrown because a timed-out lra cannot be joined
            assertThrows(WebApplicationException.class, () -> {
                lraClient.joinLRA(lraId, null, URI.create("http://localhost:8081/service4/test"), null);
            });
            joinAttempted = true;
            lock.notify();
        }
    }
}