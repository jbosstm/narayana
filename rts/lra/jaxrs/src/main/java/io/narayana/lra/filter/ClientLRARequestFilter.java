/*
 * SPDX short identifier: Apache-2.0
 */

package io.narayana.lra.filter;

import io.narayana.lra.Current;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.ConfigProvider;

import java.net.URI;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

@Provider
public class ClientLRARequestFilter implements ClientRequestFilter {

    private boolean canPropagate;

    public ClientLRARequestFilter() {
        canPropagate = ConfigProvider.getConfig()
            .getOptionalValue("mp.lra.propagation.active", Boolean.class).orElse(true);
    }

    @Override
    public void filter(ClientRequestContext context) {
        MultivaluedMap<String, Object> headers = context.getHeaders();

        if (headers.containsKey(LRA_HTTP_CONTEXT_HEADER)) {
            // LRA context is explicitly set
            return;
        }

        URI lraId = Current.peek();

        if (lraId != null) {
            if (canPropagate) {
                headers.putSingle(LRA_HTTP_CONTEXT_HEADER, lraId);
                context.setProperty(LRA_HTTP_CONTEXT_HEADER, lraId);
            }
        } else {
            Object lraContext = context.getProperty(LRA_HTTP_CONTEXT_HEADER);

            if (lraContext != null) {
                if (canPropagate) {
                    headers.putSingle(LRA_HTTP_CONTEXT_HEADER, lraContext);
                }
            } else {
                headers.remove(LRA_HTTP_CONTEXT_HEADER);
            }
        }
    }
}