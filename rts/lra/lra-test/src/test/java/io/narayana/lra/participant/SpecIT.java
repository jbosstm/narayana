/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or(at your option) any later version.
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

package io.narayana.lra.participant;

import static io.narayana.lra.participant.api.StandardController.ACTIVITIES_PATH3;
import static io.narayana.lra.participant.api.StandardController.NON_TRANSACTIONAL_WORK;
import static org.eclipse.microprofile.lra.client.LRAClient.LRA_COORDINATOR_HOST_KEY;
import static org.eclipse.microprofile.lra.client.LRAClient.LRA_COORDINATOR_PATH_KEY;
import static org.eclipse.microprofile.lra.client.LRAClient.LRA_COORDINATOR_PORT_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import io.narayana.lra.client.LRAInfoImpl;
import io.narayana.lra.participant.api.ActivityController;
import io.narayana.lra.participant.api.TimedParticipant;
import org.eclipse.microprofile.lra.annotation.CompensatorStatus;
import org.eclipse.microprofile.lra.client.LRAClient;
import org.eclipse.microprofile.lra.client.LRAInfo;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import io.narayana.lra.client.Current;
import io.narayana.lra.client.NarayanaLRAClient;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

import static io.narayana.lra.client.NarayanaLRAClient.RECOVERY_COORDINATOR_PATH_NAME;
import static io.narayana.lra.participant.api.ActivityController.ACTIVITIES_PATH;
import static io.narayana.lra.participant.api.TimedParticipant.ACTIVITIES_PATH2;

@RunWith(Arquillian.class)
@RunAsClient
public class SpecIT {
    private static final Long LRA_TIMEOUT_MILLIS = 50000L;
    private static URL MICRSERVICE_BASE_URL;
    private static URL RC_BASE_URL;

    private static final int COORDINATOR_SWARM_PORT = 8082;
    private static final int TEST_SWARM_PORT = 8081;

    private static LRAClient lraClient;
    private static Client msClient, rcClient;

    private WebTarget msTarget;
    private WebTarget recoveryTarget;

    private static List<LRAInfo> oldLRAs;

    private enum CompletionType {
        complete, compensate, mixed
    }

    @Rule
    public TestName testName = new TestName();

    @Deployment(testable = false)
    public static Archive<?> createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, "lra-smoke-it.war");
        deployment.addPackages(true, "io/narayana/lra/participant");

        File[] libs = Maven.resolver()
            .loadPomFromFile("pom.xml")
            .resolve("org.jboss.narayana.rts:lra-filters")
            .withTransitivity().as(File.class);

        deployment.addAsLibraries(libs);
        return deployment;
    }

    @BeforeClass
    public static void setupClass() throws Exception {
        if (Boolean.valueOf(System.getProperty("enablePause", "true"))) {
            System.out.println("Getting ready to connect - expecting swarm lra coordinator is already up...");
            Thread.sleep(1000);
        }

        int servicePort = Integer.getInteger("service.http.port", TEST_SWARM_PORT);
        String rcHost = System.getProperty(LRA_COORDINATOR_HOST_KEY, "localhost");
        int rcPort = Integer.getInteger(LRA_COORDINATOR_PORT_KEY, COORDINATOR_SWARM_PORT);
        String coordinatorPath = System.getProperty(LRA_COORDINATOR_PATH_KEY, NarayanaLRAClient.COORDINATOR_PATH_NAME);

        MICRSERVICE_BASE_URL = new URL(String.format("http://localhost:%d", servicePort));
        RC_BASE_URL = new URL(String.format("http://%s:%d", rcHost, rcPort));

        // setting up the client
        lraClient = (LRAClient) Class.forName("io.narayana.lra.client.NarayanaLRAClient").getDeclaredConstructor().newInstance();
        lraClient.setCoordinatorURI(new URI(String.format("http://%s:%d/%s", rcHost, rcPort, coordinatorPath)));
        msClient = ClientBuilder.newClient();
        rcClient = ClientBuilder.newClient();

        oldLRAs = new ArrayList<>();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        oldLRAs.clear();
        lraClient.close();
        msClient.close();
        rcClient.close();
    }

    @Before
    public void setupTest() throws Exception {
        msTarget = msClient.target(URI.create(new URL(MICRSERVICE_BASE_URL, "/").toExternalForm()));
        recoveryTarget = rcClient.target(URI.create(new URL(RC_BASE_URL, "/").toExternalForm()));
    }

    @After
    public void finishTest() throws Exception {
        List<LRAInfo> activeLRAs = lraClient.getActiveLRAs();

        System.out.printf("TEST %s finished with %d active LRAs%n", testName.getMethodName(), activeLRAs.size());

        if (activeLRAs.size() != 0) {
            activeLRAs.forEach(lra -> {
                try {
                    if (!oldLRAs.contains(lra)) {
                        System.out.printf("%s: WARNING: test did not close %s%n", testName.getMethodName(), lra.getLraId());
                        oldLRAs.add(lra);
                        tryEndLRA(new URL(lra.getLraId()), false);
                    }
                } catch (WebApplicationException | MalformedURLException e) {
                    System.out.printf("After Test: exception %s closing %s%n", e.getMessage(), lra.getLraId());
                }
            });
        }
        Current.popAll();
    }

    private String tryEndLRA(URL lraId, boolean failTest) {
        return tryEndLRA(false, lraId, 10000, true);
    }

    private String tryCancelLRA(URL lraId, boolean failTest) {
        return tryEndLRA(true, lraId, 10000, true);
    }

    private String tryEndLRA(boolean cancel, URL lraId, long maxMsecWait, boolean failTest) {
        if (maxMsecWait > 0) {
            ExecutorService executor = Executors.newCachedThreadPool();
            Callable<String> task = () -> cancel ? lraClient.cancelLRA(lraId) : lraClient.closeLRA(lraId);
            Future<String> future = executor.submit(task);

            try {
                return future.get(maxMsecWait, TimeUnit.MILLISECONDS);
            } catch (TimeoutException | InterruptedException | ExecutionException e) {
                if (failTest) {
                    fail("delayEndLRA received unexpected exception: " + e.getMessage());
                }
            } finally {
                future.cancel(true);
            }
        } else {
            return cancel ? lraClient.cancelLRA(lraId) : lraClient.closeLRA(lraId);
        }

        return null;
    }

    @Test
    public void startLRA() throws WebApplicationException {
        URL lra = lraClient.startLRA("SpecTest#startLRA", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        tryEndLRA(lra, false);
    }

    @Test
    public void cancelLRA() throws WebApplicationException {
        URL lra = lraClient.startLRA("SpecTest#cancelLRA", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        tryCancelLRA(lra, false);

        List<LRAInfo> lras = lraClient.getAllLRAs();

        assertFalse(lras.contains(lra));
    }

    @Test
    public void closeLRA() throws WebApplicationException {
        URL lra = lraClient.startLRA("SpecTest#closelLRA", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        tryEndLRA(lra, false);

        List<LRAInfo> lras = lraClient.getAllLRAs();

        assertFalse(lras.contains(toLRAImpl(lra)));
    }

    @Test
    // test service A -> service B -> service C where A starts a LRA, service C starts a nested LRA and B is not LRA aware
    public void noLRATest() throws WebApplicationException {
        URL lra = lraClient.startLRA(null, "SpecTest#noLRATest", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        WebTarget resourcePath = msTarget.path(ACTIVITIES_PATH3).path(NON_TRANSACTIONAL_WORK);

        Response response = resourcePath.request().header(LRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), true, resourcePath);
        lraClient.cancelLRA(lra);
    }

    @Test
    public void closeLRAWaitForRecovery() throws WebApplicationException {
        delayEndLRA(-1, "wait", "recovery");
    }

    @Test
    public void closeLRAWaitIndefinitely() throws WebApplicationException {
        delayEndLRA(1000,"wait", "-1");
    }

    private void delayEndLRA(long maxMsecWait, String how, String arg) throws WebApplicationException {
        int[] cnt1 = {completedCount(ACTIVITIES_PATH, true), completedCount(ACTIVITIES_PATH, false)};

        List<LRAInfo> lras = lraClient.getActiveLRAs();
        int count = lras.size();
        URL lra = lraClient.startLRA(null, "SpecTest#delayEndLRA", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        WebTarget resourcePath = msTarget.path(ACTIVITIES_PATH).path(ActivityController.WORK_RESOURCE_METHOD)
                .queryParam("how", how)
                .queryParam("arg", arg);
        Response response = resourcePath
                .request().header(LRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        tryEndLRA(false, lra, maxMsecWait, false);

        // check that participant was told to complete
        int[] cnt2 = {completedCount(ACTIVITIES_PATH, true), completedCount(ACTIVITIES_PATH, false)};

        assertEquals("delayEndLRA: wrong completion count", cnt1[0] + 1, cnt2[0]);
        assertEquals("delayEndLRA: wrong compensation count", cnt1[1], cnt2[1]);

        // the delay will cause responsibility for ending the LRA to pass to the recovery system so run a scan:
        waitForRecovery(1, lra);

        lras = lraClient.getActiveLRAs();
        System.out.printf("delayEndLRA ok %d versus %d lras%n", count, lras.size());
        assertEquals("delayEndLRA: wrong LRA count", count, lras.size());
    }

    @Test
    public void connectionHangup() throws WebApplicationException {
        int[] cnt1 = {completedCount(ACTIVITIES_PATH, true), completedCount(ACTIVITIES_PATH, false)};

        List<LRAInfo> lras = lraClient.getActiveLRAs();
        int count = lras.size();
        URL lra = lraClient.startLRA(null, "SpecTest#connectionHangup", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        // tell the resource to generate a proccessing exception when asked to finish the LRA(simulates a connection hang)
        WebTarget resourcePath = msTarget.path(ACTIVITIES_PATH).path(ActivityController.WORK_RESOURCE_METHOD)
                .queryParam("how", "exception")
                .queryParam("arg", "javax.ws.rs.ProcessingException");
        Response response = resourcePath
                .request().header(LRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        String status = tryCancelLRA(lra, false);

        assertEquals("connectionHangup: canceled LRA should be compensating but is " + status,
                CompensatorStatus.Compensating.name(), status);

        // check that participant was told to compensate
        int[] cnt2 = {completedCount(ACTIVITIES_PATH, true), completedCount(ACTIVITIES_PATH, false)};

        assertEquals("connectionHangup: wrong completion count", cnt1[0], cnt2[0]);
        assertEquals("connectionHangup: wrong compensation count", cnt1[1] + 1, cnt2[1]);

        // the coordinator should have received an exception so run recovery to force it to retry
        assertTrue("Still waiting for recovery after 3 attempts", waitForRecovery(3, lra));

        int[] cnt3 = {completedCount(ACTIVITIES_PATH, true), completedCount(ACTIVITIES_PATH, false)};

        // there should have been a second compensate call(from the recovery coordinator)
        assertEquals("connectionHangup: wrong completion count after recovery", cnt1[0], cnt3[0]);
        assertEquals("connectionHangup: wrong compensation count after recovery", cnt1[1] + 2, cnt3[1]);

        lras = lraClient.getActiveLRAs();
        System.out.printf("join ok %d versus %d lras%n", count, lras.size());
        assertEquals("join: wrong LRA count", count, lras.size());
    }

    @Test
    public void getActiveLRAs() throws WebApplicationException {
        URL lra = lraClient.startLRA("SpecTest#getActiveLRAs", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        List<LRAInfo> lras = lraClient.getActiveLRAs();

        assertTrue(lras.contains(toLRAImpl(lra))); //new LRAInfoImpl(lra)));

        tryEndLRA(lra, false);
    }

    @Test
    public void getAllLRAs() throws WebApplicationException {
        URL lra = lraClient.startLRA("SpecTest#getAllLRAs", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        List<LRAInfo> lras = lraClient.getAllLRAs();

        assertTrue(lras.contains(toLRAImpl(lra)));

        tryEndLRA(lra, false);
    }

    //@Test
    public void getRecoveringLRAs() throws WebApplicationException {
        // TODO
    }

    @Test
    public void isActiveLRA() throws WebApplicationException {
        URL lra = lraClient.startLRA("SpecTest#isActiveLRA", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        assertTrue(lraClient.isActiveLRA(lra));

        tryEndLRA(lra, false);
    }

    // @Test
    // the coordinator cleans up when canceled
    public void isCompensatedLRA() throws WebApplicationException {
        URL lra = lraClient.startLRA("SpecTest#isCompensatedLRA", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        tryCancelLRA(lra, false);

        assertTrue(lraClient.isCompensatedLRA(lra));
    }

    // @Test
    // the coordinator cleans up when completed
    public void isCompletedLRA() throws WebApplicationException {
        URL lra = lraClient.startLRA("SpecTest#isCompletedLRA", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        tryEndLRA(lra, false);

        assertTrue(lraClient.isCompletedLRA(lra));
    }

    @Test
    public void joinLRAViaBody() throws WebApplicationException {
        Response response = msTarget.path(ACTIVITIES_PATH).path(ActivityController.WORK_RESOURCE_METHOD).request().put(Entity.text(""));

        String lra = checkStatusAndClose(response, Response.Status.OK.getStatusCode(), true);

        // validate that the LRA coordinator no longer knows about lraId
        List<LRAInfo> lras = lraClient.getActiveLRAs();

        // the resource /activities/work is annotated with Type.REQUIRED so the container should have ended it
        assertFalse(lras.contains(toLRAImpl(lra)));
    }

    private LRAInfo toLRAImpl(String lra) {
        return new LRAInfoImpl(lra, null, null,
                false, false, false,
                false,false,
                0L, 0L);
    }

    private LRAInfo toLRAImpl(URL lra) {
        return new LRAInfoImpl(lra.toExternalForm(), null, null,
                false, false, false,
                false,false,
                0L, 0L);
    }

    @Test
    public void nestedActivity() throws WebApplicationException {
        URL lra = lraClient.startLRA("SpecTest#nestedActivity", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        Response response = msTarget
                .path(ACTIVITIES_PATH).path(ActivityController.NESTED_ACTIVITY_RESOURCE_METHOD)
                .request()
                .header(NarayanaLRAClient.LRA_HTTP_HEADER, lra)
                .put(Entity.text(""));

        Object parentId = response.getHeaders().getFirst(NarayanaLRAClient.LRA_HTTP_HEADER);

        assertNotNull(parentId);
        assertEquals(lra.toExternalForm(), parentId);

        String nestedLraId = checkStatusAndClose(response, Response.Status.OK.getStatusCode(), true);

        List<LRAInfo> lras = lraClient.getActiveLRAs();

        // close the LRA
        tryEndLRA(lra, false);

        // validate that the nested LRA was closed
        lras = lraClient.getActiveLRAs();

        // the resource /activities/work is annotated with Type.REQUIRED so the container should have ended it
        assertFalse(lras.contains(toLRAImpl(nestedLraId)));
    }

    @Test
    public void completeMultiLevelNestedActivity() throws WebApplicationException {
        multiLevelNestedActivity(CompletionType.complete, 1);
    }

    @Test
    public void compensateMultiLevelNestedActivity() throws WebApplicationException {
        multiLevelNestedActivity(CompletionType.compensate, 1);
    }

    @Test
    public void mixedMultiLevelNestedActivity() throws WebApplicationException {
        multiLevelNestedActivity(CompletionType.mixed, 2);
    }

    @Test
    public void joinLRAViaHeader() throws WebApplicationException {
        int cnt1 = completedCount(ACTIVITIES_PATH, true);

        URL lra = lraClient.startLRA("SpecTest#joinLRAViaBody", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        Response response = msTarget.path(ACTIVITIES_PATH).path("work")
                .request().header(NarayanaLRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        // validate that the LRA coordinator still knows about lraId
        List<LRAInfo> lras = lraClient.getActiveLRAs();
        assertTrue(lras.contains(toLRAImpl(lra)));

        // close the LRA
        tryEndLRA(lra, false);

        // check that LRA coordinator no longer knows about lraId
        lras = lraClient.getActiveLRAs();
        assertFalse(lras.contains(toLRAImpl(lra)));

        // check that participant was told to complete
        int cnt2 = completedCount(ACTIVITIES_PATH, true);
        assertEquals(cnt1 + 1, cnt2);
    }

    @Test
    public void join() throws WebApplicationException {
        List<LRAInfo> lras = lraClient.getActiveLRAs();
        int count = lras.size();
        URL lra = lraClient.startLRA("SpecTest#join", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        Response response = msTarget.path(ACTIVITIES_PATH).path(ActivityController.WORK_RESOURCE_METHOD)
                .request().header(NarayanaLRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);
        tryEndLRA(lra, false);

        lras = lraClient.getActiveLRAs();
        System.out.printf("join ok %d versus %d lras%n", count, lras.size());
        assertEquals(count, lras.size());
    }

    @Test
    public void leaveLRA() throws WebApplicationException {
        int cnt1 = completedCount(ACTIVITIES_PATH, true);
        URL lra = lraClient.startLRA("SpecTest#leaveLRA", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        Response response = msTarget.path(ACTIVITIES_PATH).path(ActivityController.WORK_RESOURCE_METHOD)
                .request().header(NarayanaLRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        // perform a second request to the same method in the same LRA context to validate that multiple participants are not registered
        response = msTarget.path(ACTIVITIES_PATH).path(ActivityController.WORK_RESOURCE_METHOD)
                .request().header(NarayanaLRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        // call a method annotated with @Leave(should remove the participant from the LRA)
        response = msTarget.path(ACTIVITIES_PATH).path(ActivityController.LEAVE_RESOURCE_METHOD)
                .request().header(NarayanaLRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        // lraClient.leaveLRA(lra, "some participant"); // ask the MS for the participant url so we can test LRAClient

        tryEndLRA(lra, false);

        // check that participant was not told to complete
        int cnt2 = completedCount(ACTIVITIES_PATH, true);

        assertEquals(cnt1, cnt2);
    }

    @Test
    public void leaveLRAViaAPI() throws WebApplicationException {
        int cnt1 = completedCount(ACTIVITIES_PATH, true);
        URL lra = lraClient.startLRA("SpecTest#leaveLRA", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        Response response = msTarget.path(ACTIVITIES_PATH).path(ActivityController.WORK_RESOURCE_METHOD)
                .request().header(NarayanaLRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        // perform a second request to the same method in the same LRA context to validate that multiple participants are not registered
        response = msTarget.path(ACTIVITIES_PATH).path(ActivityController.WORK_RESOURCE_METHOD)
                .request().header(NarayanaLRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        // call a method annotated with @Leave(should remove the participant from the LRA)
        try {
            response = msTarget.path(ACTIVITIES_PATH).path(ActivityController.LEAVE_RESOURCE_METHOD)
                    .path(URLEncoder.encode(lra.toString(), "UTF-8"))
                    .request().header(NarayanaLRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        } catch (UnsupportedEncodingException e) {
            throw new WebApplicationException(
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Entity.text(e.getMessage())).build());
        }
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        // lraClient.leaveLRA(lra, "some participant"); // ask the MS for the participant url so we can test LRAClient

        tryEndLRA(lra, false);

        // check that participant was not told to complete
        int cnt2 = completedCount(ACTIVITIES_PATH, true);

        assertEquals(cnt1, cnt2);
    }

    @Test
    public void dependentLRA() throws WebApplicationException, MalformedURLException {
        // call a method annotated with NOT_SUPPORTED but one which programatically starts an LRA and returns it via a header
        Response response = msTarget.path(ACTIVITIES_PATH).path(ActivityController.START_VIA_API_RESOURCE_METHOD)
                .request().put(Entity.text(""));
        // check that the method started an LRA
        Object lraHeader = response.getHeaders().getFirst(NarayanaLRAClient.LRA_HTTP_HEADER);

        String id = checkStatusAndClose(response, Response.Status.OK.getStatusCode(), true);

        // the value returned via the header and body should be equal

        assertNotNull(lraHeader);

        assertEquals(id, lraHeader.toString());

        tryEndLRA(new URL(lraHeader.toString()), false);
    }

    @Test
    public void cancelOn() {
        cancelCheck(ActivityController.CANCEL_ON_RESOURCE_METHOD);
    }

    @Test
    public void cancelOnFamily() {
        cancelCheck(ActivityController.CANCEL_ON_FAMILY_RESOURCE_METHOD);
    }

    @Test
    public void timeLimitRequiredLRA() {
        int[] cnt1 = {completedCount(ACTIVITIES_PATH, true), completedCount(ACTIVITIES_PATH, false)};
        Response response = null;

        try {
            response = msTarget.path(ACTIVITIES_PATH)
                    .path(ActivityController.TIME_LIMIT_RESOURCE_METHOD)
                    .request()
                    .get();

            checkStatusAndClose(response, -1, true);

            // check that participant was invoked
            int[] cnt2 = {completedCount(ACTIVITIES_PATH, true), completedCount(ACTIVITIES_PATH, false)};

            /*
             * The call to activities/timeLimitRequiredLRA should have started an LRA whch should have timed out
             *(because the called resource method sleeps for long than the @TimeLimit annotation specifies).
             * Therefore the it should have compensated:
             */
            assertEquals("timeLimitRequiredLRA: complete was called instead of compensate",
                    cnt1[0], cnt2[0]);
            assertEquals("timeLimitRequiredLRA: compensate should have been called",
                    cnt1[1] + 1, cnt2[1]);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Test
    public void participantTimeLimitSupportsLRA() {
        // start an LRA with a timeout longer than the participant timeout
        // the expectation is that when the partipant timeout expires the LRA will be cancelled
        URL lra = lraClient.startLRA(null, "SpecTest#timeLimitSupportsLRA", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        int[] cnt1 = {completedCount(ACTIVITIES_PATH2, true),
                completedCount(ACTIVITIES_PATH2, false)};
        Response response = null;

        try {
            WebTarget resourcePath = msTarget.path(ACTIVITIES_PATH2).path(TimedParticipant.TIMELIMIT_SUPPRTS_RESOURCE_METHOD);
            response = resourcePath
                    .request()
                    .get();

            checkStatusAndClose(response, -1, true, resourcePath);

            try {
                // sleep for longer than specified in the @TimeLimit annotation on
                // the compensation method
                // {@link io.narayana.lra.participant.api.TimedParticipant#timeLimitSupportsLRA(String)}
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int[] cnt2 = {completedCount(ACTIVITIES_PATH2, true),
                    completedCount(ACTIVITIES_PATH2, false)};

            // the timeout on the participant should have canceled the LRA so check that
            // the compensation ran
            assertEquals("timeLimitSupportsLRA: complete was called instead of compensate", cnt1[0], cnt2[0]);
            assertEquals("timeLimitSupportsLRA: compensate should have been called", cnt1[1] + 1, cnt2[1]);

            // now validate that he LRA was cancelled
            List<LRAInfo> lras = lraClient.getAllLRAs();

            assertEquals("timeLimitSupportsLRA via client: lra still active",
                    null, getLra(lras, lra.toExternalForm()));
        } finally {

            if (response != null) {
                response.close();
            }
        }
    }

    /*
     * Participants can pass data during enlistment and this data will be returned during
     * the complete/compensate callbacks
     */
    // @Test // this test passes when ran under surefire but fails as an Arquilian test - TODO debug it
    public void testUserData() {
        List<LRAInfo> lras = lraClient.getActiveLRAs();
        int count = lras.size();
        String testData = "test participant data";

        Response response = msTarget.path(ACTIVITIES_PATH).path("testUserData")
                .request().put(Entity.text(testData));

        String activityId = response.readEntity(String.class);
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        lras = lraClient.getActiveLRAs();
        System.out.printf("join ok %d versus %d lras%n", count, lras.size());
        assertEquals(count, lras.size());

        response = msTarget.path(ACTIVITIES_PATH).path("getActivity")
                .queryParam("activityId", activityId)
                .request()
                .get();

        String activity = response.readEntity(String.class);

        // validate that the service received the correct data during the complete call
        assertTrue(activity.contains("userData='" + testData));
        assertTrue(activity.contains("endData='" + testData));
    }

    @Test
    public void acceptTest() throws WebApplicationException {
        joinAndEnd(true, true, ACTIVITIES_PATH, ActivityController.ACCEPT_WORK_RESOURCE_METHOD);
    }

    private void joinAndEnd(boolean waitForRecovery, boolean close, String path, String path2) throws WebApplicationException {
        int countBefore = lraClient.getActiveLRAs().size();
        URL lra = lraClient.startLRA("SpecTest#join", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        Response response = msTarget.path(path).path(path2)
                .request().header(NarayanaLRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));

        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        if (close) {
            tryEndLRA(lra, false);
        } else {
            tryCancelLRA(lra, false);
        }

        if (waitForRecovery) {
            // trigger a recovery scan which trigger a replay attempt on any participants
            // that have responded to complete/compensate requests with Response.Status.ACCEPTED
            assertTrue("Still waiting for recovery after 3 attempts", waitForRecovery(3, lra));
        }

        int countAfter = lraClient.getActiveLRAs().size();

        assertEquals(countBefore, countAfter);
    }

    @Test
    @Ignore
    public void renewTimeLimit() {
        int[] cnt1 = {completedCount(ACTIVITIES_PATH, true), completedCount(ACTIVITIES_PATH, false)};
        Response response = null;

        try {
            response = msTarget.path(ACTIVITIES_PATH)
                    .path(ActivityController.RENEW_TIME_LIMIT_RESOURCE_METHOD)
                    .request()
                    .get();

            checkStatusAndClose(response, -1, true);

            // check that participant was invoked
            int[] cnt2 = {completedCount(ACTIVITIES_PATH, true), completedCount(ACTIVITIES_PATH, false)};

            /*
             * The call to activities/timeLimit should have started an LRA whch should not have timed out
             *(because the called resource method renews the timeLimit before sleeping for longer than
              * the @TimeLimit annotation specifies).
             * Therefore the it should not have compensated:
             */
            assertEquals("compensate was called instead of complete", cnt1[0] + 1, cnt2[0]);
            assertEquals("compensate should not have been called", cnt1[1], cnt2[1]);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private String checkStatusAndClose(Response response, int expected, boolean readEntity) {
        try {
            if (expected != -1 && response.getStatus() != expected) {
                throw new WebApplicationException(response);
            }

            if (readEntity) {
                return response.readEntity(String.class);
            }
        } finally {
            response.close();
        }

        return null;
    }

    private String checkStatusAndClose(Response response, int expected, boolean readEntity, WebTarget webTarget) {
        try {
            if (expected != -1 && response.getStatus() != expected) {
                if (webTarget != null) {
                    throw new WebApplicationException(
                            String.format("%s: expected status %d got %d",
                                    webTarget.getUri().toString(), expected, response.getStatus()), response);
                }

                throw new WebApplicationException(response);
            }

            if (readEntity) {
                return response.readEntity(String.class);
            }
        } finally {
            response.close();
        }

        return null;
    }

    private int completedCount(String basePath, boolean completed) {
        Response response = null;
        String path = completed ? ActivityController.COMPLETED_COUNT_RESOURCE_METHOD
                : ActivityController.COMPENSATED_COUNT_RESOURCE_METHOD;

        try {
            response = msTarget.path(basePath).path(path).request().get();

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            return Integer.parseInt(response.readEntity(String.class));
        } finally {
            if (response != null) {
                response.close();
            }
        }

    }

    private void multiLevelNestedActivity(CompletionType how, int nestedCnt) throws WebApplicationException {

        int[] cnt1 = {completedCount(ACTIVITIES_PATH, true), completedCount(ACTIVITIES_PATH, false)};

        if (how == CompletionType.mixed && nestedCnt <= 1) {
            how = CompletionType.complete;
        }

        URL lra = lraClient.startLRA("SpecTest#multiLevelNestedActivity", LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        String lraId = lra.toString();

        Response response = msTarget
                .path(ACTIVITIES_PATH).path(ActivityController.MULTI_LEVEL_NESTED_ACTIVITY_RESOURCE_METHOD)
                .queryParam("nestedCnt", nestedCnt)
                .request()
                .header(NarayanaLRAClient.LRA_HTTP_HEADER, lra)
                .put(Entity.text(""));

        String lraStr = checkStatusAndClose(response, Response.Status.OK.getStatusCode(), true);
        String[] lraArray = lraStr.split(",");
        final List<LRAInfo> lras = lraClient.getActiveLRAs();
        URL[] urls = new URL[lraArray.length];

        IntStream.range(0, urls.length).forEach(i -> {
            try {
                urls[i] = new URL(lraArray[i]);
            } catch (MalformedURLException e) {
                fail("the test resource multiLevelNestedActivity return an invalid URL: " + e.getMessage());
            }
        });
        // check that the multiLevelNestedActivity method returned the mandatory LRA followed by two nested LRAs
        assertEquals(nestedCnt + 1, lraArray.length);
        assertEquals(lraId, lraArray[0]); // first element should be the mandatory LRA

        // check that the coordinator knows about the two nested LRAs started by the multiLevelNestedActivity method
        // NB even though they should have completed they are held in memory pending the enclosing LRA finishing
        IntStream.rangeClosed(1, nestedCnt).forEach(i -> assertTrue(lras.contains(toLRAImpl(lraArray[i]))));

        // and the mandatory lra seen by the multiLevelNestedActivity method
        assertTrue(lras.contains(toLRAImpl(lraArray[0])));

        int[] cnt2 = {completedCount(ACTIVITIES_PATH, true), completedCount(ACTIVITIES_PATH, false)};

        // check that both nested activities were told to complete
        assertEquals(cnt1[0] + nestedCnt, cnt2[0]);
        // and that neither were told to compensate
        assertEquals(cnt1[1], cnt2[1]);

        // close the LRA
        if (how == CompletionType.compensate) {
            tryCancelLRA(lra, false);
        } else if (how == CompletionType.complete) {
            tryEndLRA(lra, false);
        } else {
            /*
             * The test is calling for a mixed uutcome(a top level LRA L! and nestedCnt nested LRAs(L2, L3, ...)::
             * L1 the mandatory call(PUT "activities/multiLevelNestedActivity") registers participant C1
             *   the resource makes nestedCnt calls to "activities/nestedActivity" each of which create nested LRAs
             * L2, L3, ... each of which enlists a participant(C2, C3, ...) which are completed when the call returns
             * L2 is canceled  which causes C2 to compensate
             * L1 is closed which triggers the completion of C1
             *
             * To summarise:
             *
             * - C1 is completed
             * - C2 is completed and then compensated
             * - C3, ... are completed
             */
            tryCancelLRA(urls[1], false); // compensate the first nested LRA
            tryEndLRA(lra, false); // should not complete any nested LRAs(since they have already completed via the interceptor)
        }

        // validate that the top level and nested LRAs are gone
        final List<LRAInfo> lras2 = lraClient.getActiveLRAs();

        IntStream.rangeClosed(0, nestedCnt).forEach(i -> assertFalse(lras2.contains(toLRAImpl(lraArray[i]))));

        int[] cnt3 = {completedCount(ACTIVITIES_PATH, true), completedCount(ACTIVITIES_PATH, false)};

        if (how == CompletionType.complete) {
            // make sure that all nested activities were not told to complete or cancel a second time
            assertEquals(cnt2[0] + nestedCnt, cnt3[0]);
            // and that neither were still not told to compensate
            assertEquals(cnt1[1], cnt3[1]);

        } else if (how == CompletionType.compensate) {
            /*
             * the test starts LRA1 calls a @Mandatory method multiLevelNestedActivity which enlists in LRA1
             * multiLevelNestedActivity then calls an @Nested method which starts L2 and enlists another participant
             *   when the method returns the nested participant is completed(ie completed count is incremented)
             * Canceling L1 should then compensate the L1 enlistement(ie compensate count is incrememted)
             * which will then tell L2 to compenstate(ie the compensate count is incrememted again)
             */
            // each nested participant should have completed(the +nestedCnt)
            assertEquals(cnt1[0] + nestedCnt, cnt3[0]);
            // each nested participant should have compensated. The top level enlistement should have compensated(the +1)
            assertEquals(cnt2[1] + 1 + nestedCnt, cnt3[1]);
        } else {
            /*
             * The test is calling for a mixed uutcome:
             * - the top level LRA was closed
             * - one of the nested LRAs was compensated the rest should have been completed
             */
            assertEquals(1, cnt3[1] - cnt1[1]); // there should be just 1 compensation(the first nested LRA)
            /*
             * Expect nestedCnt + 1 completions, 1 for the top level and one for each nested LRA
             *(NB the first nested LRA is completed and compensated)
             * Note that the top level complete should not call complete again on the nested LRA
             */
            assertEquals(nestedCnt + 1, cnt3[0] - cnt1[0]); //
        }

        // this test leaves something left to recover so run a scan to clear the logs for the next test
        assertTrue("Still waiting for recovery after 3 attempts", waitForRecovery(3, lra));
    }

    private static LRAInfo getLra(List<LRAInfo> lras, String lraId) {
        for (LRAInfo lraInfo : lras) {
            if (lraInfo.getLraId().equals(lraId)) {
                return lraInfo;
            }
        }

        return null;
    }

    private void cancelCheck(String path) {
        int[] cnt1 = {completedCount(ACTIVITIES_PATH, true), completedCount(ACTIVITIES_PATH, false)};
        URL lra = lraClient.startLRA("SpecTest#" + path, LRA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        Response response = null;

        try {
            response = msTarget.path(ACTIVITIES_PATH)
                    .path(path)
                    .request()
                    .header(NarayanaLRAClient.LRA_HTTP_HEADER, lra)
                    .get();

            checkStatusAndClose(response, Response.Status.BAD_REQUEST.getStatusCode(), true);

            // check that participant was invoked
            int[] cnt2 = {completedCount(ACTIVITIES_PATH, true), completedCount(ACTIVITIES_PATH, false)};

            // check that complete was not called and that compensate was
            assertEquals("complete was called instead of compensate", cnt1[0], cnt2[0]);
            assertEquals("compensate should have been called", cnt1[1] + 1, cnt2[1]);

            try {
                assertTrue("cancelCheck: LRA should have been cancelled", !lraClient.isActiveLRA(lra));
            } catch (NotFoundException ignore) {
                // means the LRA has gone
            }
        } finally {
            if (response != null) {
                response.close();
            }

        }
    }

    private boolean waitForRecovery(int noOfPasses, URL... lras) {
        for (int i = 0; i < noOfPasses; i++) {
            // trigger a recovery scan to force a replay attempt on any pending participants
            Response response = recoveryTarget.path(RECOVERY_COORDINATOR_PATH_NAME).path("recovery")
                    .request().get();

            String recoveringLRAs = checkStatusAndClose(response, Response.Status.OK.getStatusCode(), true);
            int recoveredCnt = 0;

            for (URL lra : lras) {
                if (!recoveringLRAs.contains(lra.toExternalForm())) {
                    recoveredCnt += 1;
                }
            }

            if (recoveredCnt == lras.length) {
                return true;
            }
        }

        return false;
    }
}
