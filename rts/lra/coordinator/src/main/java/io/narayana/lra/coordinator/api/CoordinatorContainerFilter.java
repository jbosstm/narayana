/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package io.narayana.lra.coordinator.api;

import io.narayana.lra.Current;
import io.narayana.lra.LRAConstants;
import io.narayana.lra.logging.LRALogger;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.narayana.lra.LRAConstants.CURRENT_API_VERSION_STRING;
import static jakarta.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

@Provider
public class CoordinatorContainerFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        MultivaluedMap<String, String> headers = requestContext.getHeaders();
        URI lraId = null;

        if (headers.containsKey(LRA_HTTP_CONTEXT_HEADER)) {
            try {
                lraId = new URI(Current.getLast(headers.get(LRA_HTTP_CONTEXT_HEADER)));
            } catch (URISyntaxException e) {
                String errMsg = String.format("header %s contains an invalid URL %s: %s",
                        LRA_HTTP_CONTEXT_HEADER, Current.getLast(headers.get(LRA_HTTP_CONTEXT_HEADER)), e.getMessage());
                LRALogger.logger.debugf(errMsg);
                throw new WebApplicationException(errMsg, e,
                        Response.status(PRECONDITION_FAILED.getStatusCode()).entity(errMsg).build());
            }
        }

        if (!headers.containsKey(LRA_HTTP_CONTEXT_HEADER)) {
            Object lraContext = requestContext.getProperty(LRA_HTTP_CONTEXT_HEADER);

            if (lraContext != null) {
                lraId = (URI) lraContext;
            }
        }

        if (lraId != null) {
            Current.updateLRAContext(lraId, headers);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if (!responseContext.getHeaders().containsKey(LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME)) {
            // when app code did not provide version to header to be returned back
            // then using the api version which came within request; when provided neither then the current version of the api
            String responseVersion = requestContext.getHeaders().containsKey(LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) ?
                    requestContext.getHeaderString(LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) : CURRENT_API_VERSION_STRING;
            responseContext.getHeaders().putSingle(LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME, responseVersion);
        }

        Current.updateLRAContext(responseContext);
    }

    private String dumpInputStreamToString(InputStream is) {
        try (Stream<String> lines = new BufferedReader(new InputStreamReader(is)).lines()) {
            return lines.collect(Collectors.joining(System.lineSeparator()));
        }
    }
}