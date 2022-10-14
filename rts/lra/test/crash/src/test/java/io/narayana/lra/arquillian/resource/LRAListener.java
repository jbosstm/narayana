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
package io.narayana.lra.arquillian.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import java.net.URI;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

@Path(LRAListener.LRA_LISTENER_PATH)
public class LRAListener {
    public static final String LRA_LISTENER_PATH = "lra-listener";
    public static final String LRA_LISTENER_ACTION = "action";
    public static final String LRA_LISTENER_UNTIMED_ACTION = "untimedAction";
    public static final String LRA_LISTENER_STATUS = "status";
    public static final long LRA_SHORT_TIMELIMIT = 5L;

    private static LRAStatus status;

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
    @Produces("application/json")
    public Response getStatus() {
        return Response.ok(status != null ? status.name() : LRAStatus.FailedToCancel).build();
    }
}
