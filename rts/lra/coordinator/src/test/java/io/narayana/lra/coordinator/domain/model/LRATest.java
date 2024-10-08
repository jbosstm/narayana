/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.coordinator.domain.model;

import static io.narayana.lra.LRAConstants.COORDINATOR_PATH_NAME;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_RECOVERY_HEADER;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import io.narayana.lra.LRAConstants;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.hamcrest.MatcherAssert;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import io.narayana.lra.LRAData;
import io.narayana.lra.client.NarayanaLRAClient;
import io.narayana.lra.coordinator.api.Coordinator;
import io.narayana.lra.coordinator.domain.service.LRAService;
import io.narayana.lra.coordinator.internal.LRARecoveryModule;
import io.narayana.lra.filter.ServerLRAFilter;
import io.narayana.lra.logging.LRALogger;
import io.narayana.lra.provider.ParticipantStatusOctetStreamProvider;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class LRATest extends LRATestBase {
    static final String LRA_API_VERSION_HEADER_NAME = "Narayana-LRA-API-version";
    static final String RECOVERY_HEADER_NAME = "Long-Running-Action-Recovery";
    private static LRAService service;

    private NarayanaLRAClient lraClient;
    private Client client;
    private String coordinatorPath;
    private String recoveryPath;

    @Rule
    public TestName testName = new TestName();

    @ApplicationPath("base")
    public static class LRAParticipant extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            HashSet<Class<?>> classes = new HashSet<>();
            classes.add(Participant.class);
            classes.add(Participant1.class);
            classes.add(Participant2.class);
            classes.add(AfterLRAListener.class);
            classes.add(ServerLRAFilter.class);
            classes.add(ParticipantStatusOctetStreamProvider.class);
            return classes;
        }
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

        compensateCount.set(0);
        completeCount.set(0);
        forgetCount.set(0);

        client = ClientBuilder.newClient();
        coordinatorPath = TestPortProvider.generateURL('/' + COORDINATOR_PATH_NAME);
        recoveryPath = coordinatorPath + "/recovery";
        server.deploy(LRACoordinator.class);
        server.deployOldStyle(LRAParticipant.class);

        service = LRARecoveryModule.getService();
    }

    @After
    public void after() {
        LRALogger.logger.debugf("Finished test %s", testName);
        lraClient.close();
        client.close();
        clearObjectStore(testName);
        server.stop();
    }

    @Test
    public void joinWithVersionTest() {
        URI lraId = lraClient.startLRA("joinLRAWithBody");
        String version = LRAConstants.API_VERSION_1_2;
        String encodedLraId = URLEncoder.encode(lraId.toString(), StandardCharsets.UTF_8); // must be valid

        try (Response response = client.target(coordinatorPath)
                .path(encodedLraId)
                .request()
                .header(LRA_API_VERSION_HEADER_NAME, version)
                // the request body should correspond to a valid compensator or be empty
                .put(Entity.text(""))) {
            Assert.assertEquals("Expected joining LRA succeeded, PUT/200 is expected.",
                    Response.Status.OK.getStatusCode(), response.getStatus());
            Assert.assertEquals("Expected API header to be returned with the version provided in request",
                    version, response.getHeaderString(LRA_API_VERSION_HEADER_NAME));
            String recoveryHeaderUrlMessage = response.getHeaderString(RECOVERY_HEADER_NAME);
            String recoveryUrlBody = response.readEntity(String.class);
            URI recoveryUrlLocation = response.getLocation();
            Assert.assertEquals("Expecting returned body and recovery header have got the same content",
                    recoveryUrlBody, recoveryHeaderUrlMessage);
            Assert.assertEquals("Expecting returned body and location have got the same content",
                    recoveryUrlBody, recoveryUrlLocation.toString());
            MatcherAssert.assertThat("Expected returned message contains the sub-path of LRA recovery URL",
                    recoveryUrlBody, containsString("lra-coordinator/recovery"));
            // the new format just contains the Uid of the LRA
            MatcherAssert.assertThat("Expected returned message contains the LRA id",
                    recoveryUrlBody, containsString(LRAConstants.getLRAUid(lraId)));
        } finally {
            lraClient.cancelLRA(lraId);
        }
    }

    @Test
    public void joinWithOldVersionTest() {
        URI lraId = lraClient.startLRA("joinLRAWithBody");
        String version = LRAConstants.API_VERSION_1_1;
        String encodedLraId = URLEncoder.encode(lraId.toString(), StandardCharsets.UTF_8); // must be valid

        try (Response response = client.target(coordinatorPath)
                .path(encodedLraId)
                .request()
                .header(LRA_API_VERSION_HEADER_NAME, version)
                // the request body should correspond to a valid compensator or be empty
                .put(Entity.text(""))) {
            Assert.assertEquals("Expected joining LRA succeeded, PUT/200 is expected.",
                    Response.Status.OK.getStatusCode(), response.getStatus());
            Assert.assertEquals("Expected API header to be returned with the version provided in request",
                    version, response.getHeaderString(LRA_API_VERSION_HEADER_NAME));
            String recoveryHeaderUrlMessage = response.getHeaderString(RECOVERY_HEADER_NAME);
            String recoveryUrlBody = response.readEntity(String.class);
            URI recoveryUrlLocation = response.getLocation();
            Assert.assertEquals("Expecting returned body and recovery header have got the same content",
                    recoveryUrlBody, recoveryHeaderUrlMessage);
            Assert.assertEquals("Expecting returned body and location have got the same content",
                    recoveryUrlBody, recoveryUrlLocation.toString());
            MatcherAssert.assertThat("Expected returned message contains the sub-path of LRA recovery URL",
                    recoveryUrlBody, containsString("lra-coordinator/recovery"));
            MatcherAssert.assertThat("Expected returned message contains the LRA id",
                    recoveryUrlBody, containsString(encodedLraId));
        } finally {
            lraClient.cancelLRA(lraId);
        }
    }

    /*
     * verify that participants are compensated in the reverse order from which they were enlisted with the LRA
     */
    @Test
    public void testParticipantCallbackOrderWithCancel() {
        participantCallbackOrder(true);
    }

    /*
     * verify that participants are completed in the reverse order from which they were enlisted with the LRA
     */
    @Test
    public void testParticipantCallbackOrderWithClose() {
        participantCallbackOrder(false);
    }

    void participantCallbackOrder(boolean cancel) {
        queue.clear(); // reset the queue which records the order in which participants are ended

        URI lraId = lraClient.startLRA("testParticipantCallbackOrderWith" + (cancel ? "Cancel" : "Close"));

        for (int i = 1; i <= 2; i++) { // pick out participants participant1 and participant2
            String businessMethodName = String.format("/base/participant%d/continue", i);

            // invoke a method that has LRA.Type.MANDATORY passing in the LRA that was just started
            // (this will cause the participant to be enlisted with the LRA)
            try (Response r = client.target(TestPortProvider.generateURL(businessMethodName)).request()
                    .header(LRA_HTTP_CONTEXT_HEADER, lraId).get()) {
                if (r.getStatus() != Response.Status.OK.getStatusCode()) {
                    try {
                        // clean up and fail
                        lraClient.cancelLRA(lraId);

                        fail("could not reach participant" + i + ", status code=" + r.getStatus());
                    } catch (WebApplicationException e) {
                        fail(String.format("could not reach participant%d, status code=%d and %s failed with %s",
                                i, r.getStatus(), cancel ? "cancel" : "close", e.getMessage()));
                    }
                }
            }
        }

        if (cancel) {
            lraClient.cancelLRA(lraId);
        } else {
            lraClient.closeLRA(lraId);
        }

        // verify that participants participant1 and participant2 were compensated/completed in reverse order,
        // ie the queue should be in the order {2, 1} because they were enlisted in the order {1, 2}
        assertEquals(String.format("second participant should have %s first", cancel ? "compensated" : "completed"),
                Integer.valueOf(2), queue.remove()); // removing the first item from the queue should give participant2

        queue.remove(); // clean up item from participant1 (the remaining integer on the queue)
    }

    /**
     * sanity check: test that a participant is notified when an LRA closes
     */
    @Test
    public void testLRAParticipant() {
        // lookup the status of a non-existent LRA
        Response r1 = client.target(coordinatorPath + "/xyz/status").request().get();
        assertEquals("LRA id xyz should not exist", Response.Status.NOT_FOUND.getStatusCode(), r1.getStatus());

        // start a new LRA
        Response r2 = client.target(coordinatorPath + "/start").request().post(null);
        assertEquals("Expected 201", Response.Status.CREATED.getStatusCode(), r2.getStatus());
        String lraId = r2.getHeaderString(LRA_HTTP_CONTEXT_HEADER);
        Assert.assertNotNull("missing context header", lraId);
        // RestEasy adds brackets and , to delimit multiple values for a particular header key
        lraId = new StringTokenizer(lraId, "[,]").nextToken();
        // close the LRA
        Response r3 = client.target(String.format("%s/close", lraId)).request().put(null);
        int status = r3.getStatus();
        assertTrue("Problem closing LRA: ",
                status == Response.Status.OK.getStatusCode() || status == Response.Status.NOT_FOUND.getStatusCode());

        // verify that the participant complete request is issued when a method annotated with @LRA returns
        int completions = completeCount.get();
        client.target(TestPortProvider.generateURL("/base/test/start-end")).request().get(String.class);
        assertEquals(completions + 1, completeCount.get());
    }

    /**
     * sanity check: test that an LRA that closes is reported as closed or absent
     */
    @Test
    public void testComplete() throws URISyntaxException {
        // verify that the participant complete request is issued when a method annotated with @LRA returns
        int completions = completeCount.get();
        String lraId = client.target(TestPortProvider.generateURL("/base/test/start-end")).request().get(String.class);
        assertEquals(completions + 1, completeCount.get());
        LRAStatus status = getStatus(new URI(lraId));
        assertTrue("LRA should have closed", status == null || status == LRAStatus.Closed);
    }

    /*
     * Participants can update their callbacks to facilitate recovery.
     * Test that the compensate endpoint can be changed:
     */
    @Test
    public void testReplaceCompensator() throws URISyntaxException {
        // verify that participants can change their callback endpoints
        int fallbackCompensations = fallbackCompensateCount.get();
        // call a participant method that starts an LRA and returns the lra and the recovery id in the response
        String urls = client.target(TestPortProvider.generateURL("/base/test/start-with-recovery")).request().get(String.class);
        String[] tokens = urls.split(",");
        assertTrue("response is missing components for the lraId and/or recoveryId",
                tokens.length >= 2);
        // the service method returns the lra and recovery ids in a comma separated response:
        String lraUrl = tokens[tokens.length - 2];
        String recoveryUrl = tokens[tokens.length - 1];

        // change the participant compensate endpoint (or change the resource completely to showcase migrating
        // responsibility for the participant to a different microservice
        String newCompensateCallback = TestPortProvider.generateURL("/base/test/fallback-compensate");
        // define the new link header for the new compensate endpoint
        String newCompensator = String.format("<%s>; rel=compensate", newCompensateCallback);

        // check that performing a GET on the recovery url returns the participant callbacks:
        try (Response r1 = client.target(recoveryUrl).request().get()) {
            int res = r1.getStatus();
            if (res != Response.Status.OK.getStatusCode()) {
                // clean up and fail
                fail("get recovery url failed: " + res);
            }

            String linkHeader = r1.readEntity(String.class);
            // the link header should be a standard link header corresponding to the participant callbacks,
            // just sanity check that the mandatory compensate rel type is present
            String compensateRelationType = "rel=\"compensate\"";

            MatcherAssert.assertThat("Compensator link header is missing the compensate rel type",
                    linkHeader, containsString(compensateRelationType));
        }

        // use the recovery url to ask the coordinator to compensate on a different endpoint
        try (Response r1 = client.target(recoveryUrl).request().put(Entity.text(newCompensator))) {
            int res = r1.getStatus();
            if (res != Response.Status.OK.getStatusCode()) {
                // clean up and fail
                try (Response r = client.target(String.format("%s/cancel", lraUrl)).request().put(null)) {
                    if (r.getStatus() != Response.Status.OK.getStatusCode()) {
                        fail("move and cancel failed");
                    }
                }
                fail("move failed");
            }
        }

        // cancel the LRA
        try (Response r2 = client.target(String.format("%s/cancel", lraUrl)).request().put(null)) {
            int res = r2.getStatus();
            if (res != Response.Status.OK.getStatusCode()) {
                fail("unable to cleanup: " + res);
            }
        }

        // verify that the participant was called on the new endpoint and that the LRA cancelled
        assertEquals(fallbackCompensations + 1, fallbackCompensateCount.get());
        LRAStatus status = getStatus(new URI(lraUrl));
        assertTrue("LRA should have cancelled", status == null || status == LRAStatus.Cancelled);
    }

    /**
     * Run a loop of LRAs so that a debugger can watch memory
     * @throws URISyntaxException
     */
    @Test
    public void testForLeaks() throws URISyntaxException {
        int txnCount = 10;
        // verify that the participant complete request is issued when a method annotated with @LRA returns
        int completions = completeCount.get();

        // start some LRAs
        for (int i = 0; i < txnCount; i++) {
            String lraId = client.target(TestPortProvider.generateURL("/base/test/start-end")).request().get(String.class);
            LRAStatus status = getStatus(new URI(lraId));
            assertTrue("LRA should have closed", status == null || status == LRAStatus.Closed);
        }

        // Remark: there should be no memory leaks in LRAService

        assertEquals(completions + txnCount, completeCount.get());
    }

    /**
     * test that participants that report LRAStatus.Closing are replayed
     */
    @Test
    public void testReplay() {
        int completions = completeCount.get();
        Response response = client.target(TestPortProvider.generateURL("/base/test/start-end"))
                .queryParam("accept", "1")
                .request()
                .get();
        String lra = response.readEntity(String.class);
        URI lraId = null;

        try {
            lraId = new URI(lra);
        } catch (URISyntaxException e) {
            fail(String.format("%s: service returned an invalid URI (%s). Reason: %s)", testName, lra, e.getMessage()));
        }

        try {
            service.getLRA(lraId);
        } catch (NotFoundException e) {
            fail("testReplay: LRA should still have been completing: " + e.getMessage());
        }

        // the LRA should still be finishing (ie there should be a log record)
        assertEquals(completions, completeCount.get());

        service.recover();
        assertTrue(testName + ": lra did not finish", isFinished(lraId));
    }

    /**
     * test nested LRA behaviour when the parent closes
     */
    @Test
    public void testNestedLRA() {
        testNestedLRA(false, false, false, false);
    }

    /**
     * test nested LRA behaviour when the child cancels early
     */
    @Test
    public void testNestedLRAChildCancelsEarly() {
        testNestedLRA(true, false, false, false);
    }

    /**
     * test nested LRA behaviour when the parent and child both cancel early
     */
    @Test
    public void testNestedLRAChildAndParentCancelsEarly() {
        testNestedLRA(true, true, false, false);
    }

    /**
     * test nested LRA behaviour when the parent cancels
     */
    @Test
    public void testNestedLRAParentCancels() {
        testNestedLRA(false, true, false, false);
    }

    private void testNestedLRA(boolean childCancelEarly, boolean parentCancelEarly,
                               boolean childCancelLate, boolean parentCancelLate) {
        // start a transaction (and cancel it if parentCancelEarly is true)
        Response parentResponse = client.target(TestPortProvider.generateURL("/base/test/start"))
                .queryParam("cancel", parentCancelEarly)
                .request().get();
        String parent = parentResponse.readEntity(String.class);

        if (parentCancelEarly) {
            assertEquals("invocation should have produced a 500 code", 500, parentResponse.getStatus());
            // and the parent and child should have compensated
            assertEquals("neither parent nor child should complete", 0, completeCount.get());
            assertEquals("parent and child should each have compensated", 1, compensateCount.get());
            assertStatus(parent, LRAStatus.Cancelled, true);

            return;
        }

        // start another transaction nested under parent (and cancel it if childCancelEarly is true)
        String child;

        try (Response childResponse = client.target(TestPortProvider.generateURL("/base/test/nested"))
                .queryParam("cancel", childCancelEarly)
                .request().header(LRA_HTTP_CONTEXT_HEADER, parent).put(Entity.text(parent))) {
            child = childResponse.readEntity(String.class);
        }

        assertNotNull("start child failed: ", child);

        if (childCancelEarly) {
            // the child is canceled and the parent is active
            assertEquals("neither parent nor child should complete", 0, completeCount.get());
            assertEquals("child should have compensated", 1, compensateCount.get());
            assertStatus(child, LRAStatus.Cancelled, true);
            assertStatus(parent, LRAStatus.Active, false);
        } else {
            assertEquals("nothing should be completed yet", 0, completeCount.get());
            assertEquals("nothing should be compensated yet", 0, compensateCount.get());
            assertStatus(parent, LRAStatus.Active, false);
            assertStatus(child, LRAStatus.Active, false);
        }

        // if the child was not cancelled then close it now
        if (childCancelEarly) {
            assertEquals("child should not have (provisionally) completed", 0, completeCount.get());
            assertEquals("child should have compensated", 1, compensateCount.get());
        } else {
            try (Response response = client.target(TestPortProvider.generateURL("/base/test/end"))
                    .queryParam("cancel", childCancelLate)
                    .request().header(LRA_HTTP_CONTEXT_HEADER, child).put(Entity.text(""))) {
                assertEquals("finish child: ", response.getStatus(), childCancelLate ? 500 : 200);
                assertEquals("child should have (provisionally) completed", 1, completeCount.get());
            }
        }

        // close the parent (remark: if parentCancelEarly then this code is not reached)
        try (Response response = client.target(TestPortProvider.generateURL("/base/test/end"))
                .queryParam("cancel", parentCancelLate)
                .request().header(LRA_HTTP_CONTEXT_HEADER, parent).put(Entity.text(""))) {
            assertEquals("finish parent", response.getStatus(), parentCancelLate ? 500 : 200);
        }

        try (Response response = client.target(TestPortProvider.generateURL("/base/test/forget-count"))
                .request()
                .get()) {

            assertEquals("LRA participant HTTP status", 200, response.getStatus());

            int forgetCount = response.readEntity(Integer.class);

            if (childCancelEarly) {
                assertEquals("A participant in a nested LRA that compensates should not be asked to forget",
                        0, forgetCount);
            } else {
                assertEquals("A participant in a nested LRA that completes should be asked to forget",
                        1, forgetCount);
            }
        }

        if (childCancelEarly) {
            assertEquals("parent should have completed and child should not have completed",
                    1, completeCount.get());
        } else {
            assertTrue("parent and child should have completed once each",
                    completeCount.get() >= 2);
        }
    }

    @Test
    public void completeMultiLevelNestedActivity() {
        try {
            multiLevelNestedActivity(CompletionType.complete, 1);
        } catch (URISyntaxException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void compensateMultiLevelNestedActivity() {
        try {
            multiLevelNestedActivity(CompletionType.compensate, 1);
        } catch (URISyntaxException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void mixedMultiLevelNestedActivity() {
        try {
            multiLevelNestedActivity(CompletionType.mixed, 2);
        } catch (URISyntaxException e) {
            fail(e.getMessage());
        }
    }

    private enum CompletionType {
        complete, compensate, mixed
    }

    private void multiLevelNestedActivity(CompletionType how, int nestedCnt) throws WebApplicationException, URISyntaxException {
        WebTarget resourcePath = client.target(TestPortProvider.generateURL("/base/test/multiLevelNestedActivity"));

        if (how == CompletionType.mixed && nestedCnt <= 1) {
            how = CompletionType.complete;
        }

        URI lra = new URI(client.target(TestPortProvider.generateURL("/base/test/start")).request().get(String.class));
        Response response = resourcePath
                .queryParam("nestedCnt", nestedCnt)
                .request()
                .header(LRA_HTTP_CONTEXT_HEADER, lra)
                .put(Entity.text(""));

        // the response is a comma separated list of URIs (parent, children)
        String lraStr = response.readEntity(String.class);
        assertNotNull("expecting a LRA string returned from " + resourcePath.getUri(), lraStr);

        URI[] uris = Arrays.stream(lraStr.split(",")).map(s -> {
            try {
                return new URI(s);
            } catch (URISyntaxException e) {
                fail(e.getMessage());
                return null;
            }
        }).toArray(URI[]::new);

        // check that the multiLevelNestedActivity method returned the mandatory LRA followed by any nested LRAs
        assertEquals("multiLevelNestedActivity: step 1 (the test call went to " + resourcePath.getUri() + ")",
                nestedCnt + 1, uris.length);
        // first element should be the mandatory LRA
        assertEquals("multiLevelNestedActivity: step 2 (the test call went to " + resourcePath.getUri() + ")",
                lra, uris[0]);

        // and the mandatory lra seen by the multiLevelNestedActivity method
        assertFalse("multiLevelNestedActivity: top level LRA should be active (path called " + resourcePath.getUri() + ")",
                isFinished(uris[0]));

        // check that all nested activities were told to complete
        assertEquals("multiLevelNestedActivity: step 3 (called test path " +
                resourcePath.getUri() + ")", nestedCnt, completeCount.get());
        assertEquals("multiLevelNestedActivity: step 4 (called test path " +
                resourcePath.getUri() + ")", 0, compensateCount.get());

        // close the LRA
        if (how == CompletionType.compensate) {
            lraClient.cancelLRA(lra);

            // validate that the top level and nested LRAs are gone
            assertAllFinished(uris);
            /*
             * the test starts LRA1 calls a @Mandatory method multiLevelNestedActivity which enlists in LRA1
             * multiLevelNestedActivity then calls an @Nested method which starts L2 and enlists another participant
             *   when the method returns the nested participant is completed (ie completed count is incremented)
             * Canceling L1 should then compensate the L1 enlistment (ie compensate count is incremented)
             * which will then tell L2 to compensate (ie the compensate count is incremented again)
             */
            // each nested participant should have completed (the +nestedCnt)
            assertEquals("multiLevelNestedActivity: step 7 (called test path " +
                    resourcePath.getUri() + ")", nestedCnt, completeCount.get());
            // each nested participant should have compensated. The top level enlistment should have compensated (the +1)
            assertEquals("multiLevelNestedActivity: step 8 (called test path " +
                    resourcePath.getUri() + ")", nestedCnt + 1, compensateCount.get());
        } else if (how == CompletionType.complete) {
            lraClient.closeLRA(lra);

            // validate that the top level and nested LRAs are gone
            assertAllFinished(uris);

            // each nested participant and the top level participant should have completed (nestedCnt + 1) at least once
            assertTrue("multiLevelNestedActivity: step 5a (called test path " +
                    resourcePath.getUri() + ")", completeCount.get() >= nestedCnt + 1);
            // each nested participant should have been told to forget
            assertEquals("multiLevelNestedActivity: step 5b (called test path " +
                    resourcePath.getUri() + ")", forgetCount.get(), nestedCnt);
            // and that neither were still not told to compensate
            assertEquals("multiLevelNestedActivity: step 6 (called test path " +
                    resourcePath.getUri() + ")", 0, compensateCount.get());
        } else {
            /*
             * The test is calling for a mixed outcome (a top level LRA L1 and nestedCnt nested LRAs (L2, L3, ...)::
             * L1 the mandatory call (PUT "lraresource/multiLevelNestedActivity") registers participant C1
             * the resource makes nestedCnt calls to "lraresource/nestedActivity" each of which create nested LRAs
             * L2, L3, ... each of which enlists a participant (C2, C3, ...) which are completed when the call returns
             * L2 is cancelled which causes C2 to compensate
             * L1 is closed which triggers the completion of C1
             *
             * To summarise:
             *
             * - C1 is completed
             * - C2 is completed and then compensated
             * - C3, ... are completed
             */

            // compensate the first nested LRA in the enlisted resource
            try (Response r = client.target(TestPortProvider.generateURL("/base/test/end"))
                    .queryParam("cancel", true)
                    .request()
                    .header(LRA_HTTP_CONTEXT_HEADER, uris[1])
                    .put(Entity.text(""))) {
                assertEquals("compensate the first nested LRA", 500, r.getStatus());
            }

            lraClient.closeLRA(lra); // should not complete any nested LRAs (since they have already completed via the interceptor)

            /*
             * Expect nestedCnt + 1 completions, 1 for the top level and one for each nested LRA
             * (NB the first nested LRA is completed and compensated)
             * Note that the top level complete should not call complete again on the nested LRA
             */
            assertEquals("multiLevelNestedActivity: step 10 (called test path " +
                    resourcePath.getUri() + ")", nestedCnt + 1, completeCount.get());
            /*
             * The test is calling for a mixed outcome:
             * - the top level LRA was closed
             * - one of the nested LRAs was compensated the rest should have been completed
             */
            // there should be just 1 compensation (the first nested LRA)
            assertEquals("multiLevelNestedActivity: step 9 (called test path " +
                    resourcePath.getUri() + ")",1, compensateCount.get());
        }

        // verify that the coordinator does not return any LRAs
        // ie assert lraClient.getAllLRAs().isEmpty() but for clarity check each one
        List<LRAData> lras = lraClient.getAllLRAs();
        LRAData parentData = new LRAData();
        parentData.setLraId(lra);
        assertFalse("parent LRA should not have been returned", lras.contains(parentData));

        for (URI uri : uris) {
            LRAData nestedData = new LRAData();
            nestedData.setLraId(uri);
            assertFalse("child LRA should not have been returned", lras.contains(nestedData));
        }
    }

    // validate that the top level and nested LRAs are gone
    private void assertAllFinished(URI[] uris) {
        assertTrue(uris.length != 0);
        IntStream.rangeClosed(0, uris.length - 1).forEach(i -> assertTrue(
                String.format("multiLevelNestedActivity: %s LRA still active",
                        (i == 0 ? "top level" : "nested")),
                isFinished(uris[i])));
    }

    @Test
    public void testGrandparentContext() {
        assertNull("testGrandparentContext: current thread should not be associated with any LRAs",
                lraClient.getCurrent());

        // start a hierarchy of three LRAs
        URI grandParent = lraClient.startLRA("NestedParticipantIT#testGrandparentContext grandparent");
        URI parent = lraClient.startLRA("NestedParticipantIT#testGrandparentContext parent"); // child of grandParent
        URI child = lraClient.startLRA("NestedParticipantIT#testGrandparentContext child"); // child of parent

        lraClient.closeLRA(grandParent); // should close everything in the hierarchy

        // nothing should be associated with the calling thread
        assertNull("testGrandparentContext: current thread should not be associated with any LRAs",
                lraClient.getCurrent());

        // and verify they are all closed
        assertStatus("grandparent", grandParent, null, LRAStatus.Closed);
        assertStatus("parent", parent, null, LRAStatus.Closed);
        assertStatus("child", child, null, LRAStatus.Closed);
    }

    @Test
    public void testClose() {
        runLRA(false);
    }

    @Test
    public void testCancel() {
        runLRA(true);
    }

    @Test
    public void testTimeout() throws URISyntaxException {
        int compensations = compensateCount.get();
        String lraId = client
                .target(TestPortProvider.generateURL("/base/test/time-limit"))
                .request()
                .get(String.class);
        assertEquals(compensations + 1, compensateCount.get());
        LRAStatus status = getStatus(new URI(lraId));
        assertTrue("LRA should have cancelled", status == null || status == LRAStatus.Cancelled);
    }

    @Test
    public void testTimeOutWithNoParticipants() {
        URI lraId = lraClient.startLRA(null, "testTimeLimit", 100L, ChronoUnit.MILLIS);

        // a) wait for the time limit to be reached
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // b) verify that the coordinator either removed it from its cache or it cancelled the LRA
        LRAStatus status = getStatus(lraId);
        assertTrue("LRA should have cancelled but it's in state " + status,
                status == null || status == LRAStatus.Cancelled);
        try {
            lraClient.cancelLRA(lraId);
            fail("should not be able to cancel a timed out LRA");
        } catch (WebApplicationException ignore) {
        }
    }

    @Test
    public void testLRAListener() throws InterruptedException, URISyntaxException {
        String businessMethodName = "/base/lra-listener/do-in-LRA"; // the one that creates the LRA
        String afterCheckMethodName = "/base/lra-listener/check-after"; // the one that reports the no of notifications
        URI lraId;

        // invoke a business method that should start and end an LRA
        try (Response r = client.target(TestPortProvider.generateURL(businessMethodName)).request().get()) {
            assertEquals("testLRAListener: business method call failed", 200, r.getStatus());
            lraId = new URI(r.readEntity(String.class)); // remember the LRA so that it's status can be verified
        }

        // verify that the AfterLRA annotated method ("/lra-listener/after") keeps getting called until it returns 200
        try (Response r = client.target(TestPortProvider.generateURL(afterCheckMethodName))
                .request().get()) {
            assertEquals("testLRAListener: check-after method call failed", 200, r.getStatus());

            Integer notificationsBeforeRecovery = r.readEntity(Integer.class);
            assertTrue("Expected at least one AfterLRA notifications", notificationsBeforeRecovery > 0);

            // verify that the coordinator still regards the LRA as finished even though there are still listeners
            LRAStatus status = lraClient.getStatus(lraId);
            assertEquals("LRA should be in the closed state, not " + status, LRAStatus.Closed, status);

            // trigger a recovery scan so that the coordinator redelivers the listener notification which can take
            // a few seconds (maybe put this in a routine so that other tests can use it)
            try (Response r2 = client.target(recoveryPath).request().get()) {
                assertEquals("testLRAListener: trigger recovery method call failed", 200, r.getStatus());
                r2.getEntity(); // read the response stream, ignore the result (but we could check to see if contains lraId)
            }

            // check that the listener was notified again during the recovery scan
            try (Response r3 = client.target(TestPortProvider.generateURL(afterCheckMethodName)).request().get()) {
                assertEquals("testLRAListener: check-after method call failed", 200, r3.getStatus());
                int notificationsAfterRecovery = r3.readEntity(Integer.class);
                assertTrue("Expected the recovery scan to produce extra AfterLRA listener notifications",
                        notificationsAfterRecovery > notificationsBeforeRecovery);
            }

            // the AfterLRA notification handler during recovery ("/lra-listener/after")
            // should have returned status 200, verify that the LRA is gone

            try {
                status = lraClient.getStatus(lraId);

                fail("LRA should have gone but it is in state " + status);
            } catch (NotFoundException ignore) {
                ; // success the LRA is gone as expected
            } catch (WebApplicationException e) {
                fail("status of LRA unavailable: " + e.getMessage());
            }
        }
    }

    private void runLRA(boolean cancel) {
        URI parentId = lraClient.startLRA("parent");
        URI childId = lraClient.startLRA(parentId, "child", 0L, ChronoUnit.SECONDS);

        // enlist a participant with the child
        enlistParticipant(childId.toASCIIString().split("\\?")[0]);
        // enlist a participant with the parent
        enlistParticipant(parentId.toASCIIString().split("\\?")[0]);

        if (cancel)
            lraClient.cancelLRA(parentId);
        else
            lraClient.closeLRA(parentId);

        assertEquals("parent and child should both have finished",
                2, cancel ? compensateCount.get() : completeCount.get());

        LRAStatus pStatus = getStatus(parentId);
        LRAStatus cStatus = getStatus(childId);

        assertTrue("parent LRA finished in wrong state",
                pStatus == null || pStatus == (cancel ? LRAStatus.Cancelled : LRAStatus.Closed));
        assertTrue("child LRA finished in wrong state",
                cStatus == null || cStatus == (cancel ? LRAStatus.Cancelled : LRAStatus.Closed));
    }

    private void enlistParticipant(String lraUid) {
        try (Response response = client.target(lraUid).request().put(Entity.text(getCompensatorLinkHeader()))) {
            assertEquals("Unexpected status: " + response.readEntity(String.class),
                    200, response.getStatus());
            String recoveryId = response.getHeaderString(LRA_HTTP_RECOVERY_HEADER);
            assertNotNull("recovery id was null", recoveryId);
        }
    }

    LRAStatus getStatus(URI lra) {
        try {
            return lraClient.getStatus(lra);
        } catch (NotFoundException ignore) {
            return null;
        }
    }

    private boolean isFinished(URI lra) {
        LRAStatus status = getStatus(lra);

        return status == null
                || status == LRAStatus.Closed || status == LRAStatus.Cancelled
                || status == LRAStatus.FailedToClose || status == LRAStatus.FailedToCancel;
    }

    private void assertStatus(String message, URI lraId, LRAStatus ... expectedValues) {
            LRAStatus status = getStatus(lraId);

            assertTrue(message + ": LRA status: " + status,
                    Arrays.stream(expectedValues).anyMatch(s -> s == status));
    }

    private void assertStatus(String lraId, LRAStatus expected, boolean nullValid) {
        try {
            LRAStatus status = getStatus(new URI(lraId));

            assertTrue("unexpected null LRA status", status != null || nullValid);

            assertTrue("Expected status " + expected + " but state was " + status,
                    status == null || status == expected);
        } catch (URISyntaxException e) {
            fail(String.format("%s: %s", testName.getMethodName(), e.getMessage()));
        }
    }

    private String getCompensatorLinkHeader() {
        String prefix = TestPortProvider.generateURL("/base/test");

        return String.join(",",
                makeLink(prefix, "forget"),
                makeLink(prefix, "after"),
                makeLink(prefix, "complete"),
                makeLink(prefix, "compensate")
        );
    }

    private static String makeLink(String uriPrefix, String key) {
        return Link.fromUri(String.format("%s/%s", uriPrefix, key))
                .title(key + " URI")
                .rel(key)
                .type(MediaType.TEXT_PLAIN)
                .build().toString();
    }

}