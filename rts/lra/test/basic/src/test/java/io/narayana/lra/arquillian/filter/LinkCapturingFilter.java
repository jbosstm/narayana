/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.arquillian.filter;

import io.narayana.lra.arquillian.LRACustomBaseURIIT;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

/**
 * JAX-RS filter that captures outgoing Link header and adds it to the server response filter.
 * Note that this is client request and server response.
 * @see LRACustomBaseURIIT
 */
@Provider
public class LinkCapturingFilter implements ClientRequestFilter, ContainerResponseFilter {

    private String linkHeader;

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String link = requestContext.getHeaderString("Link");
        if (link != null) {
            linkHeader = link;
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if (linkHeader != null) {
            responseContext.getHeaders().putSingle("Link", linkHeader);
        }
    }
}
