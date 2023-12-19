/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.arquillian.resource;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_RECOVERY_HEADER;

@ApplicationScoped
@Path(SimpleLRAParticipant.SIMPLE_PARTICIPANT_RESOURCE_PATH)
public class SimpleLRAParticipant {
    public static final String SIMPLE_PARTICIPANT_RESOURCE_PATH = "simple-lra-participant";
    public static final String START_LRA_PATH = "start-lra";
    public static final String RESET_ACCEPTED_PATH = "reset-accepted";

    private static final AtomicBoolean accepted = new AtomicBoolean(false);

    @GET
    @Path(START_LRA_PATH)
    @LRA(value = LRA.Type.REQUIRED)
    public Response doInLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId, @HeaderParam(LRA_HTTP_RECOVERY_HEADER) URI recoveryUrl) {
        accepted.set(true);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .header(LRA_HTTP_RECOVERY_HEADER, recoveryUrl.toASCIIString())
                .entity(lraId.toASCIIString())
                .build();
    }

    @GET
    @Path(RESET_ACCEPTED_PATH)
    public Response reset() {
        accepted.set(false);

        return Response.ok("").build(); // return some entity since one of the tests expects it
    }

    @PUT
    @Path("/compensate")
    @Compensate
    public Response compensate() {
        if (accepted.get()) {
            return Response.status(Response.Status.ACCEPTED).entity(ParticipantStatus.Compensating).build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ParticipantStatus.FailedToCompensate).build();
    }
}