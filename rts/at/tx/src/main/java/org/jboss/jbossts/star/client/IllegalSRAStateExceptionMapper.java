/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.client;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class IllegalSRAStateExceptionMapper implements ExceptionMapper<IllegalSRAStateException> {
    @Override
    public Response toResponse(IllegalSRAStateException exception) {
        return Response.status(Response.Status.PRECONDITION_FAILED)
                .entity(String.format("LRA is in the wrong state for this operation: %s", exception.getMessage())).build();
    }
}