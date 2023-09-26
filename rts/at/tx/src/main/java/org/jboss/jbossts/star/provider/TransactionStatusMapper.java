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
 * map transaction status exceptions
 */
@Provider
public class TransactionStatusMapper implements ExceptionMapper<TransactionStatusException> {
   public Response toResponse(TransactionStatusException exception) {
      return Response.status(HttpResponseCodes.SC_CONFLICT).build();
   }
}