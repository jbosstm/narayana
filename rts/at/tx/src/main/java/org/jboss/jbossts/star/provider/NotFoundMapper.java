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
 * 404 mapper
 */
@Provider
public class NotFoundMapper implements ExceptionMapper<ResourceNotFoundException> {
   public Response toResponse(ResourceNotFoundException exception) {
      return Response.status(HttpResponseCodes.SC_NOT_FOUND).build();
   }
}