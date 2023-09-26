/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package io.narayana.lra.arquillian.resource;

import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

@Path(FailingAfterLRAListener.ROOT_PATH)
public class FailingAfterLRAListener {

    public static final String ROOT_PATH = "failing-after-lra-listener";
    public static final String ACTION_PATH = "action";

    private static final AtomicInteger afterLRACounter = new AtomicInteger(0);

    @GET
    @Path(ACTION_PATH)
    @LRA(LRA.Type.REQUIRED)
    public String doWorkInLRA(@HeaderParam(LRA.LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        return lraId.toASCIIString();
    }

    @PUT
    @Path("after-lra")
    @AfterLRA
    public Response afterLRA(@HeaderParam(LRA.LRA_HTTP_ENDED_CONTEXT_HEADER) URI endedLRAId) {
        if (afterLRACounter.getAndIncrement() < 1) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.ok().build();
    }

    @GET
    @Path("counter")
    public Response getCounterValue() {
        return Response.ok(afterLRACounter.get()).build();
    }
}