/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.arquillian.resource;

import io.narayana.lra.client.LRAParticipantData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.Status;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import java.net.URI;

import static io.narayana.lra.LRAConstants.NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_ENDED_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_RECOVERY_HEADER;

@ApplicationScoped
@Path(ParticipantDataResource.SIMPLE_PARTICIPANT_RESOURCE_PATH)
public class ParticipantDataResource {
    public static final String SIMPLE_PARTICIPANT_RESOURCE_PATH = "simple-lra-participant";
    public static final String START_LRA_PATH = "start-end-lra";
    public static final String BEGIN_LRA_PATH = "begin-lra";
    public static final String END_LRA_PATH = "end-lra";

    public static final String CALLS_PATH = "calls";

    private static final String CONTEXT_DATA = "context for doInLRA";
    private static final String BEGIN_DATA = "context for begin";
    private static ParticipantStatus status = ParticipantStatus.Active;
    private static final StringBuilder calls = new StringBuilder();

    @Inject
    LRAParticipantData data;

    @GET
    @Path(START_LRA_PATH)
    @LRA(value = LRA.Type.REQUIRED)
    public Response doInLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId,
                            @HeaderParam(LRA_HTTP_RECOVERY_HEADER) URI recoveryUrl) {
        data.setData(CONTEXT_DATA);
        calls.setLength(0);
        status = ParticipantStatus.Active;

        return Response.status(Response.Status.OK)
                .header(LRA_HTTP_RECOVERY_HEADER, recoveryUrl.toASCIIString())
                .entity(lraId.toASCIIString())
                .build();
    }

    @GET
    @Path(BEGIN_LRA_PATH)
    @LRA(value = LRA.Type.REQUIRES_NEW, end = false)
    public Response doStartLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId,
                               @HeaderParam(LRA_HTTP_RECOVERY_HEADER) URI recoveryUrl) {
        data.setData(BEGIN_DATA);
        calls.setLength(0);
        status = ParticipantStatus.Active;

        return Response.status(Response.Status.OK)
                .header(LRA_HTTP_RECOVERY_HEADER, recoveryUrl.toASCIIString())
                .entity(lraId.toASCIIString())
                .build();
    }

    @GET
    @Path(END_LRA_PATH)
    @LRA(value = LRA.Type.REQUIRED)
    public Response doEndLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId,
                             @HeaderParam(LRA_HTTP_RECOVERY_HEADER) URI recoveryUrl) {
        status = ParticipantStatus.Active;

        return Response.status(Response.Status.OK)
                .header(LRA_HTTP_RECOVERY_HEADER, recoveryUrl.toASCIIString())
                .entity(data.getData())
                .build();
    }

    @PUT
    @Path("/compensate")
    @Compensate
    public Response compensate(@HeaderParam(NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME) String pData) {
        status = ParticipantStatus.FailedToCompensate;

        if (CONTEXT_DATA.equals(pData)) {
            calls.append("@Compensate");
        } else {
            calls.append("XCompensate");
        }

        return Response.status(Response.Status.ACCEPTED).entity(ParticipantStatus.Compensating).build();
    }

    @PUT
    @Path("/complete")
    @Complete
    public Response complete(@HeaderParam(NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME) String pData) {
        status = ParticipantStatus.FailedToComplete;

        if (CONTEXT_DATA.equals(pData)) {
            calls.append("@Complete");
        } else {
            calls.append("XComplete");
        }

        return Response.status(Response.Status.ACCEPTED).entity(ParticipantStatus.Completing).build();
    }

    @DELETE
    @Path("delete")
    @Forget
    public Response forget(@HeaderParam(NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME) String pData) {
        if (CONTEXT_DATA.equals(pData)) {
            calls.append("@Forget");
        } else {
            calls.append("XForget");
        }

        return Response.ok().build();
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Status
    public Response status(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId,
                           @HeaderParam(LRA_HTTP_RECOVERY_HEADER) URI recoveryId,
                           @HeaderParam(NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME) String pData) {
        if (CONTEXT_DATA.equals(pData)) {
            calls.append("@Status");
        } else {
            calls.append("XStatus");
        }

        return Response.ok(status.name()).build();
    }

    @PUT
    @Path("/after")
    @AfterLRA
    public Response afterLRA(LRAStatus status,
                             @HeaderParam(LRA_HTTP_ENDED_CONTEXT_HEADER) URI endedLRAId,
                             @HeaderParam(NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME) String pData) {
        if (CONTEXT_DATA.equals(pData)) {
            calls.append("@AfterLRA");
        } else {
            calls.append("XAfterLRA");
        }

        return Response.ok("ok").build();
    }

    @GET
    @Path(CALLS_PATH)
    public Response getCalls() {
        return Response.ok(calls.toString()).build();
    }
}
