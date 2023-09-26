/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.client;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InvalidSRAExceptionMapper implements ExceptionMapper<InvalidSRAId> {
    @Override
    public Response toResponse(InvalidSRAId exception) {
        return Response.status(Response.Status.NOT_ACCEPTABLE)
                .entity(String.format("Invalid LRA id: %s", exception.getMessage())).build();
    }
}