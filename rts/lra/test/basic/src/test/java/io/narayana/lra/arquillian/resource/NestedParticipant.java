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

import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.lra.tck.service.LRAMetricService;
import org.eclipse.microprofile.lra.tck.service.LRAMetricType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path(NestedParticipant.ROOT_PATH)
@ApplicationScoped
public class NestedParticipant {

    public static final String ROOT_PATH = "/nested";
    public static final String ENLIST_PATH = "/enlist";

    @Inject
    LRAMetricService lraMetricService;

    @LRA(value = LRA.Type.NESTED, end = false)
    @GET
    @Path(NestedParticipant.ENLIST_PATH)
    @Produces(MediaType.TEXT_PLAIN)
    public Response enlist(@HeaderParam(LRA.LRA_HTTP_CONTEXT_HEADER) URI lraId,
                           @HeaderParam(LRA.LRA_HTTP_PARENT_CONTEXT_HEADER) URI parentId) {
        lraMetricService.incrementMetric(LRAMetricType.Nested, parentId, NestedParticipant.class);
        return Response.ok(lraId).build();
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
