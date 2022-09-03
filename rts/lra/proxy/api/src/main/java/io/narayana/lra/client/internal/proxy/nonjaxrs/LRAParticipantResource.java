/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019, Red Hat, Inc., and individual contributors
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
package io.narayana.lra.client.internal.proxy.nonjaxrs;

import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.Status;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;

import static io.narayana.lra.LRAConstants.AFTER;
import static io.narayana.lra.LRAConstants.COMPENSATE;
import static io.narayana.lra.LRAConstants.COMPLETE;
import static io.narayana.lra.LRAConstants.FORGET;
import static io.narayana.lra.LRAConstants.STATUS;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_ENDED_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_PARENT_CONTEXT_HEADER;

@ApplicationScoped
@Path(LRAParticipantResource.RESOURCE_PATH)
public class LRAParticipantResource {

    static final String RESOURCE_PATH = "lra-participant-proxy";

    @Inject
    private LRAParticipantRegistry lraParticipantRegistry;

    @PUT
    @Path("{participantId}/" + COMPENSATE)
    @Produces(MediaType.TEXT_PLAIN)
    @Compensate
    public Response compensate(@PathParam("participantId") String participantId,
                               @HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId,
                               @HeaderParam(LRA_HTTP_PARENT_CONTEXT_HEADER) String parentId) {
        return getParticipant(participantId).compensate(createURI(lraId), createURI(parentId));
    }

    @PUT
    @Path("{participantId}/" + COMPLETE)
    @Produces(MediaType.TEXT_PLAIN)
    @Complete
    public Response complete(@PathParam("participantId") String participantId,
                             @HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId,
                             @HeaderParam(LRA_HTTP_PARENT_CONTEXT_HEADER) String parentId) {
        return getParticipant(participantId).complete(createURI(lraId), createURI(parentId));
    }

    @GET
    @Path("{participantId}/" + STATUS)
    @Produces(MediaType.TEXT_PLAIN)
    @Status
    public Response status(@PathParam("participantId") String participantId,
                           @HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId,
                           @HeaderParam(LRA_HTTP_PARENT_CONTEXT_HEADER) String parentId) {
        return getParticipant(participantId).status(createURI(lraId), createURI(parentId));
    }

    @DELETE
    @Path("{participantId}/" + FORGET)
    @Produces(MediaType.TEXT_PLAIN)
    @Forget
    public Response forget(@PathParam("participantId") String participantId,
                           @HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId,
                           @HeaderParam(LRA_HTTP_PARENT_CONTEXT_HEADER) String parentId) {
        return getParticipant(participantId).forget(createURI(lraId), createURI(parentId));
    }

    @PUT
    @Path("{participantId}/" + AFTER)
    @AfterLRA
    public Response afterLRA(@PathParam("participantId") String participantId,
                         @HeaderParam(LRA_HTTP_ENDED_CONTEXT_HEADER) URI lraId,
                         LRAStatus lraStatus) {
        return getParticipant(participantId).afterLRA(lraId, lraStatus);
    }

    private LRAParticipant getParticipant(String participantId) {
        LRAParticipant participant = lraParticipantRegistry.getParticipant(participantId);
        if (participant == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).
                    entity(participantId + ": Cannot find participant in LRA registry").build());
        }
        return participant;
    }

    private URI createURI(String value) {
        return value != null ? URI.create(value) : null;
    }
}
