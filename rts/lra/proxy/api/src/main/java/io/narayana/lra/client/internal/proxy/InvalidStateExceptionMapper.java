/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.client.internal.proxy;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

public class InvalidStateExceptionMapper implements ExceptionMapper<InvalidLRAStateException> {
    @Override
    public Response toResponse(InvalidLRAStateException exception) {
        return Response.status(Response.Status.PRECONDITION_FAILED)
                .entity(exception.getMessage()).build();
    }
}