/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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

package io.narayana.lra.participant;

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
import java.util.stream.IntStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import io.narayana.lra.participant.api.ActivityController;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import io.narayana.lra.client.Current;
import io.narayana.lra.client.LRAClient;
import io.narayana.lra.client.LRAStatus;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

import static io.narayana.lra.client.LRAClient.RECOVERY_COORDINATOR_PATH_NAME;
import static io.narayana.lra.participant.api.ActivityController.ACTIVITIES_PATH;

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

    private WebTarget msTarget, recoveryTarget;

    private static List<LRAStatus> oldLRAs;

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
            System.out.println("Getting ready to connect - waiting for coordinator to startup...");
            Thread.currentThread().sleep(20000);
        }

        int servicePort = Integer.getInteger("service.http.port", TEST_SWARM_PORT);
        String rcHost = System.getProperty("lra.http.host", "localhost");
        int rcPort = Integer.getInteger("lra.http.port", COORDINATOR_SWARM_PORT);

        MICRSERVICE_BASE_URL = new URL(String.format("http://localhost:%d", servicePort));
        RC_BASE_URL = new URL(String.format("http://%s:%d", rcHost, rcPort));

        // setting up the client
        lraClient = new LRAClient(rcHost, rcPort);
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
        List<LRAStatus> activeLRAs = lraClient.getActiveLRAs();

        System.out.printf("TEST %s finished with %d active LRAs%n", testName.getMethodName(), activeLRAs.size());

        if (activeLRAs.size() != 0) {
            activeLRAs.forEach(lra -> {
                try {
                    if (!oldLRAs.contains(lra)) {
                        System.out.printf("%s: WARNING: test did not close %s%n", testName.getMethodName(), lra.getLraId());
                        oldLRAs.add(lra);
                        lraClient.closeLRA(new URL(lra.getLraId()));
                    }
                } catch (WebApplicationException | MalformedURLException e) {
                    System.out.printf("After Test: exception %s closing %s%n", e.getMessage(), lra.getLraId());
                }
            });
        }
        Current.popAll();
    }

    // TODO add a test for a participant annotated with @TimeLimit

    @Test
    public void startLRA() throws WebApplicationException {
        URL lra = lraClient.startLRA("SpecTest#startLRA", LRA_TIMEOUT_MILLIS);

        lraClient.closeLRA(lra);
    }

    @Test
    public void cancelLRA() throws WebApplicationException {
        URL lra = lraClient.startLRA("SpecTest#cancelLRA", LRA_TIMEOUT_MILLIS);

        lraClient.cancelLRA(lra);

        List<LRAStatus> lras = lraClient.getAllLRAs();

        assertFalse(lras.contains(lra));
    }

    @Test
    public void closeLRA() throws WebApplicationException {
        URL lra = lraClient.startLRA("SpecTest#closelLRA", LRA_TIMEOUT_MILLIS);

        lraClient.closeLRA(lra);

        List<LRAStatus> lras = lraClient.getAllLRAs();

        assertFalse(lras.contains(new LRAStatus(lra)));
    }

    @Test
    public void getActiveLRAs() throws WebApplicationException {
        URL lra = lraClient.startLRA("SpecTest#getActiveLRAs", LRA_TIMEOUT_MILLIS);
        List<LRAStatus> lras = lraClient.getActiveLRAs();

        assertTrue(lras.contains(new LRAStatus(lra)));

        lraClient.closeLRA(lra);
    }

    @Test
    public void getAllLRAs() throws WebApplicationException {
        URL lra = lraClient.startLRA("SpecTest#getAllLRAs", LRA_TIMEOUT_MILLIS);
        List<LRAStatus> lras = lraClient.getAllLRAs();

        assertTrue(lras.contains(new LRAStatus(lra)));

        lraClient.closeLRA(lra);
    }

    @Test
    public void getRecoveringLRAs() throws WebApplicationException {
        // TODO
    }

    @Test
    public void isActiveLRA() throws WebApplicationException {
        URL lra = lraClient.startLRA("SpecTest#isActiveLRA", LRA_TIMEOUT_MILLIS);

        assertTrue(lraClient.isActiveLRA(lra));

        lraClient.closeLRA(lra);
    }

    // @Test
    // the coordinator cleans up when canceled
    public void isCompensatedLRA() throws WebApplicationException {
        URL lra = lraClient.startLRA("SpecTest#isCompensatedLRA", LRA_TIMEOUT_MILLIS);

        lraClient.cancelLRA(lra);

        assertTrue(lraClient.isCompensatedLRA(lra));
    }

    // @Test
    // the coordinator cleans up when completed
    public void isCompletedLRA() throws WebApplicationException {
        URL lra = lraClient.startLRA("SpecTest#isCompletedLRA", LRA_TIMEOUT_MILLIS);

        lraClient.closeLRA(lra);

        assertTrue(lraClient.isCompletedLRA(lra));
    }

    @Test
    public void joinLRAViaBody() throws WebApplicationException {
        Response response = msTarget.path("activities").path("work").request().put(Entity.text(""));

        String lra = checkStatusAndClose(response, Response.Status.OK.getStatusCode(), true);

        // validate that the LRA coordinator no longer knows about lraId
        List<LRAStatus> lras = lraClient.getActiveLRAs();

        // the resource /activities/work is annotated with Type.REQUIRED so the container should have ended it
        assertFalse(lras.contains(new LRAStatus(lra)));
    }

    @Test
    public void nestedActivity() throws WebApplicationException {
        URL lra = lraClient.startLRA("SpecTest#nestedActivity", LRA_TIMEOUT_MILLIS);

        Response response = msTarget
                .path("activities").path("nestedActivity")
                .request()
                .header(LRAClient.LRA_HTTP_HEADER, lra)
                .put(Entity.text(""));

        Object parentId = response.getHeaders().getFirst(LRAClient.LRA_HTTP_HEADER);

        assertNotNull(parentId);
        assertEquals(lra.toExternalForm(), parentId);

        String nestedLraId = checkStatusAndClose(response, Response.Status.OK.getStatusCode(), true);

        List<LRAStatus> lras = lraClient.getActiveLRAs();

        // close the LRA
        lraClient.closeLRA(lra);

        // validate that the nested LRA was closed
        lras = lraClient.getActiveLRAs();

        // the resource /activities/work is annotated with Type.REQUIRED so the container should have ended it
        assertFalse(lras.contains(new LRAStatus(nestedLraId)));
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
    public void joinLRAViaHeader () throws WebApplicationException {
        int cnt1 = completedCount(true);

        URL lra = lraClient.startLRA("SpecTest#joinLRAViaBody", LRA_TIMEOUT_MILLIS);

        Response response = msTarget.path("activities").path("work")
                .request().header(LRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        // validate that the LRA coordinator still knows about lraId
        List<LRAStatus> lras = lraClient.getActiveLRAs();
        assertTrue(lras.contains(new LRAStatus(lra)));

        // close the LRA
        lraClient.closeLRA(lra);

        // check that LRA coordinator no longer knows about lraId
        lras = lraClient.getActiveLRAs();
        assertFalse(lras.contains(new LRAStatus(lra)));

        // check that participant was told to complete
        int cnt2 = completedCount(true);
        assertEquals(cnt1 + 1, cnt2);
    }

    @Test
    public void join () throws WebApplicationException {
        List<LRAStatus> lras = lraClient.getActiveLRAs();
        int count = lras.size();
        URL lra = lraClient.startLRA("SpecTest#join", LRA_TIMEOUT_MILLIS);

        Response response = msTarget.path("activities").path("work")
                .request().header(LRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);
        lraClient.closeLRA(lra);

        lras = lraClient.getActiveLRAs();
        System.out.printf("join ok %d versus %d lras%n", count, lras.size());
        assertEquals(count, lras.size());
    }

    @Test
    public void leaveLRA() throws WebApplicationException {
        int cnt1 = completedCount(true);
        URL lra = lraClient.startLRA("SpecTest#leaveLRA", LRA_TIMEOUT_MILLIS);

        Response response = msTarget.path("activities").path("work").request().header(LRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        // perform a second request to the same method in the same LRA context to validate that multiple participants are not registered
        response = msTarget.path("activities").path("work").request().header(LRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        // call a method annotated with @Leave (should remove the participant from the LRA)
        response = msTarget.path("activities").path("leave").request().header(LRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        // lraClient.leaveLRA(lra, "some participant"); // ask the MS for the participant url so we can test LRAClient

        lraClient.closeLRA(lra);

        // check that participant was not told to complete
        int cnt2 = completedCount(true);

        assertEquals(cnt1, cnt2);
    }

    @Test
    public void leaveLRAViaAPI() throws WebApplicationException {
        int cnt1 = completedCount(true);
        URL lra = lraClient.startLRA("SpecTest#leaveLRA", LRA_TIMEOUT_MILLIS);

        Response response = msTarget.path("activities").path("work").request().header(LRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        // perform a second request to the same method in the same LRA context to validate that multiple participants are not registered
        response = msTarget.path("activities").path("work").request().header(LRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        // call a method annotated with @Leave (should remove the participant from the LRA)
        try {
            response = msTarget.path("activities").path("leave").path(URLEncoder.encode(lra.toString(), "UTF-8"))
                    .request().header(LRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));
        } catch (UnsupportedEncodingException e) {
            throw new WebApplicationException(
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Entity.text(e.getMessage())).build());
        }
        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        // lraClient.leaveLRA(lra, "some participant"); // ask the MS for the participant url so we can test LRAClient

        lraClient.closeLRA(lra);

        // check that participant was not told to complete
        int cnt2 = completedCount(true);

        assertEquals(cnt1, cnt2);
    }

    @Test
    public void dependentLRA() throws WebApplicationException, MalformedURLException {
        // call a method annotated with NOT_SUPPORTED but one which programatically starts an LRA and returns it via a header
        Response response = msTarget.path("activities").path("startViaApi").request().put(Entity.text(""));
        // check that the method started an LRA
        Object lraHeader = response.getHeaders().getFirst(LRAClient.LRA_HTTP_HEADER);

        String id = checkStatusAndClose(response, Response.Status.OK.getStatusCode(), true);

        // the value returned via the header and body should be equal

        assertNotNull(lraHeader);

        assertEquals(id, lraHeader.toString());

        lraClient.closeLRA(new URL(lraHeader.toString()));
    }

    @Test
    public void cancelOn() {
        cancelCheck("cancelOn");
    }

    @Test
    public void cancelOnFamily() {
        cancelCheck("cancelOnFamily");
    }

    @Test
    public void timeLimit() {
        int[] cnt1 = {completedCount(true), completedCount(false)};
        Response response = null;

        try {
            response = msTarget.path("activities")
                    .path("timeLimit")
                    .request()
                    .get();

            checkStatusAndClose(response, -1, true);

            // check that participant was invoked
            int[] cnt2 = {completedCount(true), completedCount(false)};

            /*
             * The call to activities/timeLimit should have started an LRA whch should have timed out
             * (because the called resource method sleeps for long than the @TimeLimit annotation specifies).
             * Therefore the it should have compensated:
             */
            assertEquals("complete was called instead of compensate", cnt1[0], cnt2[0]);
            assertEquals("compensate should have been called", cnt1[1] + 1, cnt2[1]);
        } finally {
            if (response != null)
                response.close();
        }
    }

    /*
     * Participants can pass data during enlistment and this data will be returned during
     * the complete/compensate callbacks
     */
    // @Test // this test passes when ran under surefire but fails as an Arquilian test - TODO debug it
    public void testUserData() {
        List<LRAStatus> lras = lraClient.getActiveLRAs();
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
        joinAndEnd(true, true, ACTIVITIES_PATH, ActivityController.ACCEPT_WORK);
    }

    private void joinAndEnd(boolean waitForRecovery, boolean close, String path, String path2) throws WebApplicationException {
        int countBefore = lraClient.getActiveLRAs().size();
        URL lra = lraClient.startLRA("SpecTest#join", LRA_TIMEOUT_MILLIS);

        Response response = msTarget.path(path).path(path2)
                .request().header(LRAClient.LRA_HTTP_HEADER, lra).put(Entity.text(""));

        checkStatusAndClose(response, Response.Status.OK.getStatusCode(), false);

        if (close)
            lraClient.closeLRA(lra);
        else
            lraClient.cancelLRA(lra);

        if (waitForRecovery) {
            // trigger a recovery scan which trigger a replay attempt on any participants
            // that have responded to complete/compensate requests with Response.Status.ACCEPTED
            Response response2 = recoveryTarget.path(RECOVERY_COORDINATOR_PATH_NAME).path("recovery")
                    .request().get();

            checkStatusAndClose(response2, Response.Status.OK.getStatusCode(), false);
        }

        int countAfter = lraClient.getActiveLRAs().size();

        assertEquals(countBefore, countAfter);
    }

    // @Test
    public void renewTimeLimit() {
        int[] cnt1 = {completedCount(true), completedCount(false)};
        Response response = null;

        try {
            response = msTarget.path("activities")
                    .path("renewTimeLimit")
                    .request()
                    .get();

            checkStatusAndClose(response, -1, true);

            // check that participant was invoked
            int[] cnt2 = {completedCount(true), completedCount(false)};

            /*
             * The call to activities/timeLimit should have started an LRA whch should not have timed out
             * (because the called resource method renews the timeLimit before sleeping for longer than
              * the @TimeLimit annotation specifies).
             * Therefore the it should not have compensated:
             */
            assertEquals("compensate was called instead of complete", cnt1[0] + 1, cnt2[0]);
            assertEquals("compensate should not have been called", cnt1[1], cnt2[1]);
        } finally {
            if (response != null)
                response.close();
        }
    }

    private String checkStatusAndClose(Response response, int expected, boolean readEntity) {
        try {
            if (expected != -1 && response.getStatus() != expected)
                throw new WebApplicationException(response);

            if (readEntity)
                return response.readEntity(String.class);
        } finally {
            response.close();
        }

        return null;
    }

    private int completedCount(boolean completed) {
        Response response = null;
        String path = completed ? "completedactivitycount" : "compensatedactivitycount";

        try {
            response = msTarget.path("activities").path(path).request().get();

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            return Integer.parseInt(response.readEntity(String.class));
        } finally {
            if (response != null)
                response.close();
        }

    }

    private void multiLevelNestedActivity(CompletionType how, int nestedCnt) throws WebApplicationException {

        int[] cnt1 = {completedCount(true), completedCount(false)};

        if (how == CompletionType.mixed && nestedCnt <= 1)
            how = CompletionType.complete;

        URL lra = lraClient.startLRA("SpecTest#multiLevelNestedActivity", LRA_TIMEOUT_MILLIS);
        String lraId = lra.toString();

        Response response = msTarget
                .path("activities").path("multiLevelNestedActivity")
                .queryParam("nestedCnt", nestedCnt)
                .request()
                .header(LRAClient.LRA_HTTP_HEADER, lra)
                .put(Entity.text(""));

        String lraStr = checkStatusAndClose(response, Response.Status.OK.getStatusCode(), true);
        String[] lraArray = lraStr.split(",");
        final List<LRAStatus> lras = lraClient.getActiveLRAs();
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
        IntStream.rangeClosed(1, nestedCnt).forEach(i -> assertTrue(lras.contains(new LRAStatus(lraArray[i]))));

        // and the mandatory lra seen by the multiLevelNestedActivity method
        assertTrue(lras.contains(new LRAStatus(lraArray[0])));

        int[] cnt2 = {completedCount(true), completedCount(false)};

        // check that both nested activities were told to complete
        assertEquals(cnt1[0] + nestedCnt, cnt2[0]);
        // and that neither were told to compensate
        assertEquals(cnt1[1], cnt2[1]);

        // close the LRA
        if (how == CompletionType.compensate) {
            lraClient.cancelLRA(lra);
        } else if (how == CompletionType.complete) {
            lraClient.closeLRA(lra);
        } else {
            /*
             * The test is calling for a mixed uutcome (a top level LRA L! and nestedCnt nested LRAs (L2, L3, ...)::
             * L1 the mandatory call (PUT "activities/multiLevelNestedActivity") registers participant C1
             *   the resource makes nestedCnt calls to "activities/nestedActivity" each of which create nested LRAs
             * L2, L3, ... each of which enlists a participant (C2, C3, ...) which are completed when the call returns
             * L2 is canceled  which causes C2 to compensate
             * L1 is closed which triggers the completion of C1
             *
             * To summarise:
             *
             * - C1 is completed
             * - C2 is completed and then compensated
             * - C3, ... are completed
             */
            lraClient.cancelLRA(urls[1]); // compensate the first nested LRA
            lraClient.closeLRA(lra); // should not complete any nested LRAs (since they have already completed via the interceptor)
        }

        // validate that the top level and nested LRAs are gone
        final List<LRAStatus> lras2 = lraClient.getActiveLRAs();

        IntStream.rangeClosed(0, nestedCnt).forEach(i -> assertFalse(lras2.contains(new LRAStatus(lraArray[i]))));

        int[] cnt3 = {completedCount(true), completedCount(false)};

        if (how == CompletionType.complete) {
            // make sure that all nested activities were not told to complete or cancel a second time
            assertEquals(cnt2[0] + nestedCnt, cnt3[0]);
            // and that neither were still not told to compensate
            assertEquals(cnt1[1], cnt3[1]);

        } else if (how == CompletionType.compensate) {
            /*
             * the test starts LRA1 calls a @Mandatory method multiLevelNestedActivity which enlists in LRA1
             * multiLevelNestedActivity then calls an @Nested method which starts L2 and enlists another participant
             *   when the method returns the nested participant is completed (ie completed count is incremented)
             * Canceling L1 should then compensate the L1 enlistement (ie compensate count is incrememted)
             * which will then tell L2 to compenstate (ie the compensate count is incrememted again)
             */
            // each nested participant should have completed (the +nestedCnt)
            assertEquals(cnt1[0] + nestedCnt, cnt3[0]);
            // each nested participant should have compensated. The top level enlistement should have compensated (the +1)
            assertEquals(cnt2[1] + 1 + nestedCnt, cnt3[1]);
        } else {
            /*
             * The test is calling for a mixed uutcome:
             * - the top level LRA was closed
             * - one of the nested LRAs was compensated the rest should have been completed
             */
            assertEquals(1, cnt3[1] - cnt1[1]); // there should be just 1 compensation (the first nested LRA)
            /*
             * Expect nestedCnt + 1 completions, 1 for the top level and one for each nested LRA
             * (NB the first nested LRA is completed and compensated)
             * Note that the top level complete should not call complete again on the nested LRA
             */
            assertEquals(nestedCnt + 1, cnt3[0] - cnt1[0]); //
        }
    }

    private void cancelCheck(String path) {
        int[] cnt1 = {completedCount(true), completedCount(false)};
        URL lra = lraClient.startLRA("SpecTest#" + path, LRA_TIMEOUT_MILLIS);
        Response response = null;

        try {
            response = msTarget.path("activities")
                    .path(path)
                    .request()
                    .header(LRAClient.LRA_HTTP_HEADER, lra)
                    .get();

            checkStatusAndClose(response, Response.Status.BAD_REQUEST.getStatusCode(), true);

            // check that participant was invoked
            int[] cnt2 = {completedCount(true), completedCount(false)};

            // check that complete was not called and that compensate was
            assertEquals("complete was called instead of compensate", cnt1[0], cnt2[0]);
            assertEquals("compensate should have been called", cnt1[1] + 1, cnt2[1]);
        } finally {
            if (response != null)
                response.close();

                assertFalse(lraClient.isActiveLRA(lra));
        }
    }
}
