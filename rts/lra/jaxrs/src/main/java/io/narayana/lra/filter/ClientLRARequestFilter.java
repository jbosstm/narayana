/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
