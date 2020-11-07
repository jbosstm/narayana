/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020, Red Hat, Inc., and individual contributors
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

package io.narayana.lra.arquillian.resource;

import io.narayana.lra.client.NarayanaLRAClient;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

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

    @Inject
    private NarayanaLRAClient lraClient;

    @GET
    @Path(CREATE_OR_CONTINUE_LRA)
    @LRA(value = LRA.Type.REQUIRED, end = false)
    public Response beginLRAWithRemoteCalls(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lra1) {
        valididateLRAIsActive("lra1 should be active", lraClient.getStatus(lra1));

        // start a new LRA
        URI lra2 = remoteInvocation(lra1, START_NEW_LRA);

        valididateLRAIsActive("lra1 should still be active", lraClient.getStatus(lra1));
        valididateLRAIsActive("lra2 should be active", lraClient.getStatus(lra2));

        // lra1 should be the current context for remote invocations even though lra2 is active
        if (!lra1.equals(lraClient.getCurrent())) {
            throw new WebApplicationException(
                    Response.status(Response.Status.PRECONDITION_FAILED).entity("lra1 should be current").build());
        }

        URI lra3 = remoteInvocation(lra2, CONTINUE_LRA); // lra2 is still active, use it for the next invocation

        valididateLRAIsActive("lra2 should still be active", lraClient.getStatus(lra2));
        if (!lra2.equals(lra3)) { // lra3 was a continuation of lra2
            throw new WebApplicationException(
                    Response.status(Response.Status.PRECONDITION_FAILED).entity("lra2 should equal lra3").build());
        }

        lraClient.closeLRA(lra2); // use the (proprietary) client API to close the LRA started above (START_NEW_LRA)

        // the status of lra2 should be NOT_FOUND or not active
        try {
            // verify that the LRA is no longer active
            valididateLRAIsNotActive("lra2 should no longer be active", lraClient.getStatus(lra2));
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
        valididateLRAIsActive("lra1 should be active", lraClient.getStatus(lra1));

        URI lra2 = lraClient.startLRA("lra");
        lraClient.closeLRA(lra2); // use the (proprietary) client API to close the LRA started above (START_NEW_LRA)

        valididateLRAIsNotActive("lra2 should no longer be active", lraClient.getStatus(lra2));

        // the original LRA (lra1) will still be active (because of the end = false attribute)
        return Response.status(Response.Status.OK).entity(lra1.toASCIIString()).build();
    }

    @GET
    @Path(CONTINUE_LRA)
    @LRA(value = LRA.Type.MANDATORY, end=false)
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

    private void valididateLRAIsActive(String message, LRAStatus status) {
        if (!status.equals(LRAStatus.Active)) {
            throw new WebApplicationException(Response.status(Response.Status.PRECONDITION_FAILED).entity(message).build());
        }
    }

    private void valididateLRAIsNotActive(String message, LRAStatus status) {
        if (status.equals(LRAStatus.Active)) {
            throw new WebApplicationException(Response.status(Response.Status.PRECONDITION_FAILED).entity(message).build());
        }
    }
}
