/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.provider;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps an HttpResponseException to the HTTP code embedded in the exception
 */
@Provider
public class HttpResponseMapper implements ExceptionMapper<HttpResponseException> {

   public Response toResponse(HttpResponseException exception) {

      return Response.status(exception.getActualResponse()).build();
   }
}