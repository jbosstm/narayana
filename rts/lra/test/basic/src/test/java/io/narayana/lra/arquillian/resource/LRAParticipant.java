/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.arquillian.resource;

import io.narayana.lra.client.internal.NarayanaLRAClient;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

@ApplicationScoped
@Path(LRAParticipant.RESOURCE_PATH)
public class LRAParticipant {
    public static final String RESOURCE_PATH = "participant2";

    public static final String CREATE_OR_CONTINUE_LRA = "start-lra";
    public static final String CREATE_OR_CONTINUE_LRA2 = "start-lra-before-entry-and-end-it-after-exit";
    public static final String END_EXISTING_LRA = "end-lra";
    public static final String CONTINUE_LRA = "continue-lra";
    public static final String START_NEW_LRA = "start-lra2";

    @Context
    UriInfo uriInfo;

    private final NarayanaLRAClient lraClient = new NarayanaLRAClient();

    @GET
    @Path(CREATE_OR_CONTINUE_LRA)
    @LRA(value = LRA.Type.REQUIRED, end = false)
    public Response beginLRAWithRemoteCalls(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lra1) {
        validateLRAIsActive("lra1 should be active", lraClient.getStatus(lra1));

        // start a new LRA
        URI lra2 = remoteInvocation(lra1, START_NEW_LRA);

        validateLRAIsActive("lra1 should still be active", lraClient.getStatus(lra1));
        validateLRAIsActive("lra2 should be active", lraClient.getStatus(lra2));

        // lra1 should be the current context for remote invocations even though lra2 is active
        if (!lra1.equals(lraClient.getCurrent())) {
            throw new WebApplicationException(
                    Response.status(Response.Status.PRECONDITION_FAILED).entity("lra1 should be current").build());
        }

        URI lra3 = remoteInvocation(lra2, CONTINUE_LRA); // lra2 is still active, use it for the next invocation

        validateLRAIsActive("lra2 should still be active", lraClient.getStatus(lra2));
        if (!lra2.equals(lra3)) { // lra3 was a continuation of lra2
            throw new WebApplicationException(
                    Response.status(Response.Status.PRECONDITION_FAILED).entity("lra2 should equal lra3").build());
        }

        lraClient.closeLRA(lra2); // use the (proprietary) client API to close the LRA started above (START_NEW_LRA)

        // the status of lra2 should be NOT_FOUND or not active
        try {
            // verify that the LRA is no longer active
            validateLRAIsNotActive(lraClient.getStatus(lra2));
        } catch (NotFoundException ignore) {
            // LRA is not active
        }

        // the original LRA (lra1) will still be active (because of the end = false attribute)
        return Response.status(Response.Status.OK).entity(lra1.toASCIIString()).build();
    }

    @GET
    @Path(CREATE_OR_CONTINUE_LRA2)
    @LRA(value = LRA.Type.REQUIRED, end = false)
    public Response beginLRAWithRemoteCalls2(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lra1) {
        validateLRAIsActive("lra1 should be active", lraClient.getStatus(lra1));

        URI lra2 = lraClient.startLRA("lra");
        lraClient.closeLRA(lra2); // use the (proprietary) client API to close the LRA started above (START_NEW_LRA)

        validateLRAIsNotActive(lraClient.getStatus(lra2));

        // the original LRA (lra1) will still be active (because of the end = false attribute)
        return Response.status(Response.Status.OK).entity(lra1.toASCIIString()).build();
    }

    @GET
    @Path(CONTINUE_LRA)
    @LRA(value = LRA.Type.MANDATORY, end = false)
    public Response continueLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        return Response.status(Response.Status.OK).entity(lraId.toASCIIString()).build();
    }

    @GET
    @Path(END_EXISTING_LRA)
    @LRA(value = LRA.Type.MANDATORY)
    public Response endLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        return Response.status(Response.Status.OK).entity(lraId.toASCIIString()).build();
    }

    @GET
    @Path(START_NEW_LRA)
    @LRA(value = LRA.Type.REQUIRES_NEW, end = false)
    public Response beginLRA2(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        return Response.status(Response.Status.OK).entity(lraId.toASCIIString()).build();
    }

    @PUT
    @Path("/compensate")
    @Compensate
    public Response compensate() {
        return Response.status(Response.Status.OK).build();
    }

    private URI remoteInvocation(URI lra, String resourcePath) {
        Response response = null;
        Client client = ClientBuilder.newClient();

        try {
            Invocation.Builder builder = client.target(UriBuilder.fromUri(uriInfo.getBaseUri()) // protocol and domain
                    .path(uriInfo.getPathSegments().get(0).getPath()) // RESOURCE_PATH
                    .path(resourcePath).build()) // the sub-path of the resource method
                    .request();

            if (lra != null) {
                builder.header(LRA_HTTP_CONTEXT_HEADER, lra.toASCIIString());
            }

            response = builder.get();

            if (response.hasEntity()) {
                return URI.create(response.readEntity(String.class));
            }

            throw new WebApplicationException(
                    Response.status(Response.Status.PRECONDITION_FAILED).entity("Missing LRA is response").build());
        } finally {
            if (response != null) {
                response.close();
            }

            client.close();
        }
    }

    private void validateLRAIsActive(String message, LRAStatus status) {
        if (!status.equals(LRAStatus.Active)) {
            throw new WebApplicationException(Response.status(Response.Status.PRECONDITION_FAILED).entity(message).build());
        }
    }

    private void validateLRAIsNotActive(LRAStatus status) {
        if (status.equals(LRAStatus.Active)) {
            throw new WebApplicationException(
                    Response.status(Response.Status.PRECONDITION_FAILED).entity(
                            "lra2 should no longer be active").build());
        }
    }
}