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

import io.narayana.lra.client.Current;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;
import java.net.URL;

import static io.narayana.lra.client.NarayanaLRAClient.LRA_HTTP_HEADER;

public class ClientLRAResponseFilter implements ClientResponseFilter {
    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        Object incomingLRA = responseContext.getHeaders().getFirst(LRA_HTTP_HEADER);

        if (incomingLRA == null) {
            incomingLRA = requestContext.getProperty(LRA_HTTP_HEADER);
        }

        /*
         * if the incoming response contains a context make it the current one
         * (note we never popped the context in the request filter so we don't need to push outgoingLRA
         */
        if (incomingLRA != null)
            Current.push(new URL(incomingLRA.toString()));
    }
}
