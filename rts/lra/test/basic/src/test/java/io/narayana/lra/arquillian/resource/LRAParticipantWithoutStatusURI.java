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

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

@ApplicationScoped
@Path(LRAParticipantWithoutStatusURI.LRA_PARTICIPANT_PATH)
public class LRAParticipantWithoutStatusURI {
    public static final String LRA_PARTICIPANT_PATH = "participant-without-status-reporting";
    public static final String TRANSACTIONAL_CLOSE_PATH = "close-work";
    public static final String TRANSACTIONAL_CANCEL_PATH = "cancel-work";
    public static final String FORGET_COUNT_PATH = "forget-count";

    private static final AtomicInteger forgetCount = new AtomicInteger(0);

    @GET
    @Path(TRANSACTIONAL_CLOSE_PATH)
    @LRA
    public Response closeLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        return Response.ok(lraId.toASCIIString()).build();
    }

    @GET
    @Path(TRANSACTIONAL_CANCEL_PATH)
    @LRA(cancelOn = Response.Status.INTERNAL_SERVER_ERROR)
    public Response abortLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(lraId.toASCIIString()).build();
    }

    @GET
    @Path(FORGET_COUNT_PATH)
    public int getForgetCount() {
        return forgetCount.get();
    }

    @PUT
    @Path("compensate")
    @Produces(MediaType.APPLICATION_JSON)
    @Compensate
    public Response compensateWork(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId)
            throws NotFoundException {
        return Response.status(500).entity(ParticipantStatus.FailedToCompensate.name()).build();
    }

    @PUT
    @Path("complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Complete
    public Response completeWork(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId)
            throws NotFoundException {
        return Response.status(500).entity(ParticipantStatus.FailedToComplete.name()).build();
    }

    @DELETE
    @Path("delete")
    @Forget
    public Response foget() {
        forgetCount.incrementAndGet();

        return Response.ok().build();
    }
}
