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

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

public class NonRootLRAParticipant {

    private static final AtomicInteger counter = new AtomicInteger(0);
    private static String currentLraId;

    @GET
    @Path("/lra")
    @LRA(value = LRA.Type.REQUIRED, cancelOn = Response.Status.PRECONDITION_FAILED)
    public Response doInLRA(@HeaderParam(LRA.LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        currentLraId = lraId.toASCIIString();
        return Response.status(Response.Status.PRECONDITION_FAILED).build();
    }

    @PUT
    @Path("/compensate")
    @Compensate
    public void compensate(@HeaderParam(LRA.LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        if (lraId.toASCIIString().equals(currentLraId)) {
            counter.incrementAndGet();
        } else {
            throw new RuntimeException("Non root LRA compensated with invalid LRA id");
        }
    }

    @GET
    @Path("/counter")
    public int getCounterValue() {
        return counter.intValue();
    }

}
