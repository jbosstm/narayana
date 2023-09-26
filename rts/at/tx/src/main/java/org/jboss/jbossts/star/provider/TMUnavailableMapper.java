/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.provider;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.HttpResponseCodes;

/**
 * Map service unavailable exceptions
 */
@Provider
public class TMUnavailableMapper implements ExceptionMapper<TMUnavailableException> {
   public Response toResponse(TMUnavailableException exception) {
      return Response.status(HttpResponseCodes.SC_SERVICE_UNAVAILABLE).build();
   }
}