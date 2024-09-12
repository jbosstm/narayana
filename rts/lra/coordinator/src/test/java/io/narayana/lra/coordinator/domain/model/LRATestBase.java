/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.coordinator.domain.model;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_PARENT_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_RECOVERY_HEADER;

import java.io.File;
import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.rules.TestName;

import com.arjuna.ats.arjuna.common.arjPropertyManager;

import io.narayana.lra.logging.LRALogger;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class LRATestBase {

    protected static UndertowJaxrsServer server;
    static final AtomicInteger compensateCount = new AtomicInteger(0);
    static final AtomicInteger completeCount = new AtomicInteger(0);
    static final AtomicInteger forgetCount = new AtomicInteger(0);
    static final long LRA_SHORT_TIMELIMIT = 10L;
    private static LRAStatus status = LRAStatus.Active;
    private static final AtomicInteger acceptCount = new AtomicInteger(0);

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
            LRATestBase.acceptCount.set(acceptCount);

            return getResult(cancel, contextId);
        }

        @GET
        @Path("start")
        @LRA(value = LRA.Type.REQUIRED, end = false)
        public Response startInLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI contextId,
                                   @HeaderParam(LRA_HTTP_PARENT_CONTEXT_HEADER) URI parentLRA,
                                   @DefaultValue("0") @QueryParam("accept") Integer acceptCount,
                                   @DefaultValue("false") @QueryParam("cancel") Boolean cancel) {
            LRATestBase.acceptCount.set(acceptCount);

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
            LRATestBase.acceptCount.set(acceptCount);

            return getResult(cancel, contextId);
        }

        @GET
        @Path("time-limit")
        @Produces(MediaType.APPLICATION_JSON)
        @LRA(value = LRA.Type.REQUIRED, timeLimit = 1000, timeUnit = ChronoUnit.MILLIS)
        public Response timeLimit(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
            try {
                // sleep for longer than specified in the attribute 'timeLimit'
                // (go large, ie 2 seconds, to avoid time issues on slower systems)
                Thread.sleep(8000);
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
            LRATestBase.acceptCount.set(0);

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

    protected void clearObjectStore(TestName testName) {
        final String objectStorePath = arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreDir();
        final File objectStoreDirectory = new File(objectStorePath);

        clearDirectory(objectStoreDirectory, testName);
    }

    protected void clearDirectory(final File directory, TestName testName) {
        final File[] files = directory.listFiles();

        if (files != null) {
            for (final File file : Objects.requireNonNull(directory.listFiles())) {
                if (file.isDirectory()) {
                    clearDirectory(file, testName);
                }

                if (!file.delete()) {
                    LRALogger.logger.infof("%s: unable to delete file %s", testName, file.getName());
                }
            }
        }
    }
}