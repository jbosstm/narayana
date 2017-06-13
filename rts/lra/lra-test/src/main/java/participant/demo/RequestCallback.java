/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package participant.demo;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class RequestCallback<T> implements InvocationCallback<Response> {

    private final Class<T> responseType;
    private final CompletableFuture<T> future = new CompletableFuture<>();
    private final Collection<Integer> acceptableStatusCodes;

    RequestCallback(Class<T> responseType) {
        this(responseType, Response.Status.OK.getStatusCode());
    }

    RequestCallback(Class<T> responseType, Integer ... acceptableStatusCodes) {
        this.responseType = responseType;
        this.acceptableStatusCodes = Arrays.asList(acceptableStatusCodes);
    }

    CompletableFuture<T> getCompletableFuture() {
        return future;
    }

    @Override
    public void completed(Response response) {
        try {
            if (acceptableStatusCodes.contains(response.getStatus())) {
                T t = response.readEntity(responseType);
                future.complete(t);
            } else {
                failed(new WebApplicationException(response));
            }
        } catch (Throwable t) {
            failed(t);
//        } finally {
//            response.close();
        }
    }

    @Override
    public void failed(Throwable t) {
        future.completeExceptionally(t);
    }
}

