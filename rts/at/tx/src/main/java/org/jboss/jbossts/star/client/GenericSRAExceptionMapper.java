/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.client;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GenericSRAExceptionMapper implements ExceptionMapper<GenericSRAException> {
    @Override
    public Response toResponse(GenericSRAException exception) {
        return Response.status(exception.getStatusCode())
                .entity(String.format("%s: %s", exception.getLraId() != null
                        ? exception.getLraId()
                        : "not present", exception.getMessage())).build();
    }
}