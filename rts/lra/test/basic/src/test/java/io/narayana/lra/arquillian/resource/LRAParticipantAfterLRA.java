/*
 *
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 */

package io.narayana.lra.arquillian.resource;

import org.eclipse.microprofile.lra.LRAResponse;
import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_ENDED_CONTEXT_HEADER;

@ApplicationScoped
@Path(LRAParticipantAfterLRA.SIMPLE_PARTICIPANT_RESOURCE_PATH)
@LRA(value = LRA.Type.REQUIRED)
public class LRAParticipantAfterLRA {
    public static final String SIMPLE_PARTICIPANT_RESOURCE_PATH = "lra-participant-type-required";
    public static final String START_LRA_PATH = "start-lra";
    public static final String DO_LRA_PATH = "/do";
    public static final String COMPLETE_LRA_PATH = "/complete";
    public static final String AFTER_LRA_PATH = "/after";
    public static final String COUNTER_LRA_PATH = "/counter";
    public static final String RESET_ACCEPTED_PATH = "reset-accepted";

    private static final AtomicInteger afterLRACounter = new AtomicInteger(0);
    private static Logger log = Logger.getLogger(LRAParticipantAfterLRA.class);

    @Context
    private UriInfo context;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello participant";
    }

    @PUT
    @Path(DO_LRA_PATH)
    @LRA(value = LRA.Type.REQUIRES_NEW)
    public Response doLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        log.debug("doing " + lraId);
        // pretend to do some work
        return Response.ok().entity(lraId + "\n").build();
    }

    @Complete
    @Path(COMPLETE_LRA_PATH)
    @PUT
    public Response complete(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        log.debug("completing " + lraId);
        return LRAResponse.completed();
    }

    @PUT
    @Path(AFTER_LRA_PATH)
    @AfterLRA
    public Response afterLRA(@HeaderParam(LRA_HTTP_ENDED_CONTEXT_HEADER) URI endedLRAId, LRAStatus status) {
        log.debug("after " + endedLRAId + " status " + status + " for path " + context.getPath());

        if (afterLRACounter.getAndIncrement() < 1) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok("ok").build();
    }

    @GET
    @Path(COUNTER_LRA_PATH)
    public Response getCounterValue() {
        return Response.ok(afterLRACounter.get()).build();
    }

}
