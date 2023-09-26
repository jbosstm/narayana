/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package io.narayana.lra.arquillian.resource;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
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