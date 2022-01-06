/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2021, Red Hat, Inc., and individual contributors
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
package io.narayana.lra.coordinator.domain.model;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import io.narayana.lra.client.NarayanaLRAClient;
import io.narayana.lra.coordinator.api.Coordinator;
import io.narayana.lra.coordinator.domain.service.LRAService;
import io.narayana.lra.coordinator.internal.LRARecoveryModule;
import io.narayana.lra.filter.ServerLRAFilter;
import io.narayana.lra.logging.LRALogger;
import io.narayana.lra.provider.ParticipantStatusOctetStreamProvider;
import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static io.narayana.lra.LRAConstants.COORDINATOR_PATH_NAME;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_PARENT_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_RECOVERY_HEADER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LRATest {
    private static UndertowJaxrsServer server;
    private static LRAService service;

    static final AtomicInteger compensateCount = new AtomicInteger(0);
    static final AtomicInteger completeCount = new AtomicInteger(0);
    static final AtomicInteger forgetCount = new AtomicInteger(0);

    static final long LRA_SHORT_TIMELIMIT = 10L;

    private static LRAStatus status = LRAStatus.Active;
    private static final AtomicInteger acceptCount = new AtomicInteger(0);

    private NarayanaLRAClient lraClient;
    private Client client;
    private String coordinatorPath;

    @Rule
    public TestName testName = new TestName();

    @Path("/test")
    public static class Participant {
        private Response getResult(boolean cancel, URI lraId) {
            Response.Status status = cancel ? Response.Status.INTERNAL_SERVER_ERROR : Response.Status.OK;

            return Response.status(status).entity(lraId.toASCIIString()).build();
        }

        @GET
        @Path("start-end")
        @LRA(value = LRA.Type.REQUIRED)
        public Response doInLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI contextId,
                                @DefaultValue("0") @QueryParam("accept") Integer acceptCount,
                                @DefaultValue("false") @QueryParam("cancel") Boolean cancel) {
            LRATest.acceptCount.set(acceptCount);

            return getResult(cancel, contextId);
        }

        @GET
        @Path("start")
        @LRA(value = LRA.Type.REQUIRED, end = false)
        public Response startInLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI contextId,
                                   @HeaderParam(LRA_HTTP_PARENT_CONTEXT_HEADER) URI parentLRA,
                                   @DefaultValue("0") @QueryParam("accept") Integer acceptCount,
                                   @DefaultValue("false") @QueryParam("cancel") Boolean cancel) {
            LRATest.acceptCount.set(acceptCount);

            return getResult(cancel, contextId);
        }

        @PUT
        @Path("end")
        @LRA(value = LRA.Type.MANDATORY,
                cancelOnFamily = Response.Status.Family.SERVER_ERROR)
        public Response endLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI contextId,
                               @HeaderParam(LRA_HTTP_PARENT_CONTEXT_HEADER) URI parentLRA,
                               @DefaultValue("0") @QueryParam("accept") Integer acceptCount,
                               @DefaultValue("false") @QueryParam("cancel") Boolean cancel) {
            LRATest.acceptCount.set(acceptCount);

            return getResult(cancel, contextId);
        }

        @GET
        @Path("time-limit")
        @Produces(MediaType.APPLICATION_JSON)
        @LRA(value = LRA.Type.REQUIRED, timeLimit = 500, timeUnit = ChronoUnit.MILLIS)
        public Response timeLimit(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
            try {
                // sleep for longer than specified in the attribute 'timeLimit'
                // (go large, ie 2 seconds, to avoid time issues on slower systems)
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                LRALogger.logger.debugf("Interrupted because time limit elapsed", e);
            }
            return Response.status(Response.Status.OK).entity(lraId.toASCIIString()).build();
        }

        @GET
        @Path("timed-action")
        @LRA(value = LRA.Type.REQUIRED, end = false, timeLimit = LRA_SHORT_TIMELIMIT) // the default unit is SECONDS
        public Response actionWithLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI contextId,
                                      @DefaultValue("false") @QueryParam("cancel") Boolean cancel) {
            status = LRAStatus.Active;

            server.stop(); //simulate a server crash

            return getResult(cancel, contextId);
        }

        @LRA(value = LRA.Type.NESTED, end = false)
        @PUT
        @Path("nested")
        public Response nestedLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI contextId,
                                  @HeaderParam(LRA_HTTP_PARENT_CONTEXT_HEADER) URI parentLRA,
                                  @DefaultValue("false") @QueryParam("cancel") Boolean cancel) {
            return getResult(cancel, contextId);
        }

        @LRA(value = LRA.Type.NESTED)
        @PUT
        @Path("nested-with-close")
        public Response nestedLRAWithClose(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI contextId,
                                           @HeaderParam(LRA_HTTP_PARENT_CONTEXT_HEADER) URI parentId,
                                           @DefaultValue("false") @QueryParam("cancel") Boolean cancel) {
            return getResult(cancel, contextId);
        }

        @PUT
        @Path("multiLevelNestedActivity")
        @LRA(value = LRA.Type.MANDATORY, end = false)
        public Response multiLevelNestedActivity(
                @HeaderParam(LRA_HTTP_RECOVERY_HEADER) URI recoveryId,
                @HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI nestedLRAId,
                @QueryParam("nestedCnt") @DefaultValue("1") Integer nestedCnt) {
            // invoke resources that enlist nested LRAs
            String[] lras = new String[nestedCnt + 1];
            lras[0] = nestedLRAId.toASCIIString();
            IntStream.range(1, lras.length).forEach(i -> lras[i] = restPutInvocation(nestedLRAId,"nestedActivity", ""));

            return Response.ok(String.join(",", lras)).build();
        }

        @PUT
        @Path("nestedActivity")
        @LRA(value = LRA.Type.NESTED, end = true)
        public Response nestedActivity(@HeaderParam(LRA_HTTP_RECOVERY_HEADER) URI recoveryId,
                                       @HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI nestedLRAId) {
            return Response.ok(nestedLRAId.toASCIIString()).build();
        }

        @GET
        @Path("status")
        public Response getStatus() {
            return Response.ok(status.name()).build();
        }

        @PUT
        @Path("/complete")
        @Complete
        public Response complete(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI contextLRA,
                                 @HeaderParam(LRA_HTTP_PARENT_CONTEXT_HEADER) URI parentLRA) {
            if (acceptCount.getAndDecrement() <= 0) {
                completeCount.incrementAndGet();
                acceptCount.set(0);
                return Response.status(Response.Status.OK).entity(ParticipantStatus.Completed).build();
            }

            return Response.status(Response.Status.ACCEPTED).entity(ParticipantStatus.Completing).build();
        }

        @PUT
        @Path("/compensate")
        @Compensate
        public Response compensate(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI contextLRA,
                                   @HeaderParam(LRA_HTTP_PARENT_CONTEXT_HEADER) URI parentLRA) {
            if (acceptCount.getAndDecrement() <= 0) {
                compensateCount.incrementAndGet();
                acceptCount.set(0);
                return Response.status(Response.Status.OK).entity(ParticipantStatus.Compensated).build();
            }

            return Response.status(Response.Status.ACCEPTED).entity(ParticipantStatus.Compensating).build();
        }

        @PUT
        @Path("after")
        @AfterLRA
        public Response lraEndStatus(LRAStatus endStatus) {
            status = endStatus;

            return Response.ok().build();
        }

        @DELETE
        @Path("/forget")
        @Produces(MediaType.APPLICATION_JSON)
        @Forget
        public Response forgetWork(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId,
                                   @HeaderParam(LRA_HTTP_RECOVERY_HEADER) URI recoveryId) {
            forgetCount.incrementAndGet();

            return Response.ok().build();
        }

        @GET
        @Path("forget-count")
        public int getForgetCount() {
            return forgetCount.get();
        }

        @PUT
        @Path("reset-accepted")
        public Response reset() {
            LRATest.acceptCount.set(0);

            return Response.ok("").build();
        }

        private String restPutInvocation(URI lraURI, String path, String bodyText) {
            String id = "";
            Client client = ClientBuilder.newClient();
            try {
                try (Response response = client
                        .target(TestPortProvider.generateURL("/base/test"))
                        .path(path)
                        .request()
                        .header(LRA_HTTP_CONTEXT_HEADER, lraURI)
                        .put(Entity.text(bodyText))) {
                    if (response.hasEntity()) { // read the entity (to force close on the response)
                        id = response.readEntity(String.class);
                    }
                    if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                        throw new WebApplicationException(id + ": error on REST PUT for LRA '" + lraURI
                                + "' at path '" + path + "' and body '" + bodyText + "'", response);
                    }
                }

                return id;
            } finally {
                client.close();
            }
        }
    }

    @ApplicationPath("base")
    public static class LRAParticipant extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            HashSet<Class<?>> classes = new HashSet<>();
            classes.add(Participant.class);
            classes.add(ServerLRAFilter.class);
            classes.add(ParticipantStatusOctetStreamProvider.class);
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

        clearObjectStore();
        lraClient = new NarayanaLRAClient();

        compensateCount.set(0);
        completeCount.set(0);
        forgetCount.set(0);

        client = ClientBuilder.newClient();
        coordinatorPath = TestPortProvider.generateURL('/' + COORDINATOR_PATH_NAME);
        server.deploy(Coordinator.class);
        server.deployOldStyle(LRAParticipant.class);

        service = LRARecoveryModule.getService();
    }

    @After
    public void after() {
        LRALogger.logger.debugf("Finished test %s", testName);
        lraClient.close();
        client.close();
        clearObjectStore();
        server.stop();
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
            fail("testReplay: LRA should still have been completing");
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

    private void clearObjectStore() {
        final String objectStorePath = arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreDir();
        final File objectStoreDirectory = new File(objectStorePath);

        clearDirectory(objectStoreDirectory);
    }

    private void clearDirectory(final File directory) {
        final File[] files = directory.listFiles();

        if (files != null) {
            for (final File file : Objects.requireNonNull(directory.listFiles())) {
                if (file.isDirectory()) {
                    clearDirectory(file);
                }

                if (!file.delete()) {
                    LRALogger.logger.infof("%s: unable to delete file %s", testName, file.getName());
                }
            }
        }
    }
}
