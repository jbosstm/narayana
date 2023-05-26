/*
 * SPDX short identifier: Apache-2.0
 */


package io.narayana.lra.arquillian.resource;

import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.lra.tck.service.LRAMetricService;
import org.eclipse.microprofile.lra.tck.service.LRAMetricType;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

@Path(NestedParticipant.ROOT_PATH)
@ApplicationScoped
public class NestedParticipant {

    public static final String ROOT_PATH = "/nested";
    public static final String ENLIST_PATH = "/enlist";
    public static final String GET_COUNTER = "/counter";
    public static final String RESET_COUNTER = "/reset-counter";
    public static final String PATH = "path";

    @Inject
    LRAMetricService lraMetricService;

    @LRA(end = false)
    @GET
    @Path(PATH)
    @Produces({MediaType.TEXT_PLAIN})
    public Response runWithNestedContext(@HeaderParam(LRA.LRA_HTTP_CONTEXT_HEADER) URI lraId,
                           @HeaderParam(LRA.LRA_HTTP_PARENT_CONTEXT_HEADER) URI parentId) {
        if (parentId == null || lraId == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.PRECONDITION_FAILED)
                            .entity(parentId + " or " + lraId + " cannot be null").build());
        }

        return Response.ok(lraId).build();
    }

    @LRA(value = LRA.Type.NESTED, end = false)
    @GET
    @Path(NestedParticipant.ENLIST_PATH)
    @Produces(MediaType.TEXT_PLAIN)
    public Response enlist(@HeaderParam(LRA.LRA_HTTP_CONTEXT_HEADER) URI lraId,
                           @HeaderParam(LRA.LRA_HTTP_PARENT_CONTEXT_HEADER) URI parentId) {
        lraMetricService.incrementMetric(LRAMetricType.Nested, parentId, NestedParticipant.class);
        return Response.ok(lraId).build();
    }

    @GET
    @Path(NestedParticipant.GET_COUNTER)
    @Produces(MediaType.TEXT_PLAIN)
    public Response counter(
            @QueryParam("lraId") String lraId,
            @QueryParam("type") String type) throws URISyntaxException {

        return Response.ok(lraMetricService.getMetric(
                    LRAMetricType.valueOf(type), new URI(lraId)))
                    .build();
    }

    @GET
    @Path(NestedParticipant.RESET_COUNTER)
    public Response counter() {
        lraMetricService.clear();
        return Response.ok().build();
    }

    @Complete
    @PUT
    @Path("/complete")
    @Produces(MediaType.TEXT_PLAIN)
    public Response complete(@HeaderParam(LRA.LRA_HTTP_CONTEXT_HEADER) URI lraId,
                             @HeaderParam(LRA.LRA_HTTP_PARENT_CONTEXT_HEADER) URI parentId) {
        lraMetricService.incrementMetric(LRAMetricType.Nested, parentId, NestedParticipant.class);
        lraMetricService.incrementMetric(LRAMetricType.Completed, lraId, NestedParticipant.class);
        return Response.ok(ParticipantStatus.Completed).build();
    }

    @Compensate
    @PUT
    @Path("/compensate")
    @Produces(MediaType.TEXT_PLAIN)
    public Response compensate() {
        // required for the enlistment
        return Response.ok(ParticipantStatus.Compensated).build();
    }

    @AfterLRA
    @PUT
    @Path("/after")
    @Produces(MediaType.TEXT_PLAIN)
    public Response afterLRA(@HeaderParam(LRA.LRA_HTTP_ENDED_CONTEXT_HEADER) URI endedLraId,
                             @HeaderParam(LRA.LRA_HTTP_PARENT_CONTEXT_HEADER) URI parentId) {
        lraMetricService.incrementMetric(LRAMetricType.Nested, parentId, NestedParticipant.class);
        lraMetricService.incrementMetric(LRAMetricType.AfterLRA, endedLraId, NestedParticipant.class);
        return Response.ok(endedLraId).build();
    }
}