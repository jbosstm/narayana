/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.narayana.lra.LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME;
import static io.narayana.lra.LRAConstants.CURRENT_API_VERSION_STRING;
import static io.narayana.lra.LRAConstants.NARAYANA_LRA_API_SUPPORTED_VERSIONS;
import static jakarta.ws.rs.core.Response.Status.EXPECTATION_FAILED;
import static jakarta.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

@Provider
public class CoordinatorContainerFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        MultivaluedMap<String, String> headers = requestContext.getHeaders();
        URI lraId = null;

        verifyHighestSupportedVersion(requestContext);

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

    /**
     * Verification if the version in the header is the supported version.
     * Format and the string has to match of the list of the supported versions.
     */
    private void verifyHighestSupportedVersion(ContainerRequestContext requestContext) {
        if (!requestContext.getHeaders().containsKey(LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME)) {
            // no header specified, going with 'null' further into processing
            return;
        }

        String apiVersionString = requestContext.getHeaderString(LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME);

        if (requestContext.getHeaders().get(LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME).size() > 1 && LRALogger.logger.isDebugEnabled()) {
            LRALogger.logger.debugf("Multiple version headers for the request '%s', using version '%s'.",
                            dumpInputStreamToString(requestContext.getEntityStream()), apiVersionString);
        }

        if (!Arrays.stream(NARAYANA_LRA_API_SUPPORTED_VERSIONS).anyMatch(v -> v.equals(apiVersionString))) {
            String errorMsg = LRALogger.i18nLogger.get_wrongAPIVersionDemanded(
                    apiVersionString, NARAYANA_LRA_API_SUPPORTED_VERSIONS.toString());
            LRALogger.logger.debugf(errorMsg);
            throw new WebApplicationException(errorMsg,
                    Response.status(EXPECTATION_FAILED).entity(errorMsg)
                            .header(NARAYANA_LRA_API_VERSION_HEADER_NAME, CURRENT_API_VERSION_STRING).build());
        }
    }

    private String dumpInputStreamToString(InputStream is) {
        try (Stream<String> lines = new BufferedReader(new InputStreamReader(is)).lines()) {
            return lines.collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
