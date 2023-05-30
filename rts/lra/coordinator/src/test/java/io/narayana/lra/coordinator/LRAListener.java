/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package io.narayana.lra.coordinator;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import java.net.URI;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

@Path(LRAListener.LRA_LISTENER_PATH)
public class LRAListener {
    static final String LRA_LISTENER_PATH = "lra-listener";
    static final String LRA_LISTENER_ACTION = "action";
    static final String LRA_LISTENER_UNTIMED_ACTION = "untimed";
    static final String LRA_LISTENER_STATUS = "status";
    static final String LRA_LISTENER_KILL = "kill";

    static final long LRA_SHORT_TIMELIMIT = 10L;
    private static LRAStatus status = LRAStatus.Active;

    @PUT
    @Path(LRA_LISTENER_ACTION)
    @LRA(value = LRA.Type.REQUIRED, end = false, timeLimit = LRA_SHORT_TIMELIMIT) // the default unit is SECONDS
    public Response actionWithLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        status = LRAStatus.Active;

        return Response.ok(lraId.toASCIIString()).build();
    }

    @PUT
    @Path(LRA_LISTENER_UNTIMED_ACTION)
    @LRA(value = LRA.Type.REQUIRED, end = false)
    public Response untimedActionWithLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        status = LRAStatus.Active;

        return Response.ok(lraId.toASCIIString()).build();
    }

    @PUT
    @Path("after")
    @AfterLRA
    public Response lraEndStatus(LRAStatus endStatus) {
        status = endStatus;

        return Response.ok().build();
    }

    @GET
    @Path(LRA_LISTENER_STATUS)
    public Response getStatus() {
        return Response.ok(status.name()).build();
    }

    @GET
    @Path(LRA_LISTENER_KILL)
    public Response killJVM() {
        Runtime.getRuntime().halt(1);
        return Response.ok(status.name()).build();
    }
}