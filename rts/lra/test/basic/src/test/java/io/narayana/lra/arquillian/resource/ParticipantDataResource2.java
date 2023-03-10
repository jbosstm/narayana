/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.arquillian.resource;

import io.narayana.lra.client.LRAParticipantData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import java.net.URI;

import static io.narayana.lra.LRAConstants.NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_RECOVERY_HEADER;

@ApplicationScoped
@Path(ParticipantDataResource2.DATA_PARTICIPANT_RESOURCE_PATH)
public class ParticipantDataResource2 {
    public static final String DATA_PARTICIPANT_RESOURCE_PATH = "lra-participant-with-data";
    public static final String START_LRA_PATH = "start-lra";
    public static final String END_LRA_PATH = "end-lra";
    public static final String START_DATA = "context for begin";
    public static final String END_DATA = "context for end";

    @Inject
    LRAParticipantData data;

    @GET
    @Path(START_LRA_PATH)
    @LRA(value = LRA.Type.REQUIRES_NEW, end = false)
    public Response doStartLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId,
                               @HeaderParam(LRA_HTTP_RECOVERY_HEADER) URI recoveryUrl) {
        data.setData(START_DATA);

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
        String prevData = data.getData();

        data.setData(END_DATA);

        return Response.status(Response.Status.OK)
                .header(LRA_HTTP_RECOVERY_HEADER, recoveryUrl.toASCIIString())
                .entity(prevData)
                .build();
    }

    @PUT
    @Path("/compensate")
    @Compensate
    public Response compensate(@HeaderParam(NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME) String pData) {
        return checkBean(true, END_DATA, pData);
    }

    @PUT
    @Path("/complete")
    @Compensate
    public Response complete(@HeaderParam(NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME) String pData) {
        return checkBean(false, END_DATA, pData);
    }

    private Response checkBean(boolean compensate, String expectedData, String actualData) {
        if (expectedData.equals(actualData)) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.ok(compensate ? ParticipantStatus.FailedToCompensate : ParticipantStatus.FailedToComplete).build();
        }
    }
}
