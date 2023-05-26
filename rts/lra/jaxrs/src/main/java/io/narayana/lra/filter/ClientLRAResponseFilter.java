/*
 * SPDX short identifier: Apache-2.0
 */

package io.narayana.lra.filter;

import io.narayana.lra.Current;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

@Provider
public class ClientLRAResponseFilter implements ClientResponseFilter {
    @Context
    protected ResourceInfo resourceInfo;

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        Object callingContext = requestContext.getProperty(LRA_HTTP_CONTEXT_HEADER);

        if (callingContext != null) {
            Current.push((URI) callingContext);
        }
    }
}