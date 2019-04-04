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
package io.narayana.lra;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_HEADER;

public class Current {
    private static final ThreadLocal<Current> lraContexts = new ThreadLocal<>();

    private Stack<URI> stack;
    private Map<String, Object> state;

    private Current(URI url) {
        stack = new Stack<>();
        stack.push(url);
    }

    public static Object putState(String key, Object value) {
        Current current = lraContexts.get();

        if (current != null) {
            return current.updateState(key, value);
        }

        return null;
    }

    public static Object getState(String key) {
        Current current = lraContexts.get();

        if (current != null && current.state != null) {
            return current.state.get(key);
        }

        return null;
    }

    public Object updateState(String key, Object value) {
        if (state == null) {
            state = new HashMap<>();
        }

        return state.put(key, value);
    }

    private static void clearContext(Current current) {
        if (current.state != null) {
            current.state.clear();
        }

        lraContexts.set(null);
    }

    public static URI peek() {
        Current current = lraContexts.get();

        return current != null ? current.stack.peek() : null;
    }

    public static URI pop() {
        Current current = lraContexts.get();
        URI lraId = null;

        if (current != null) {
            lraId = current.stack.pop(); // there must be at least one

            if (current.stack.empty()) {
                clearContext(current);
            }
        }

        return lraId;
    }


    public static boolean pop(URI lra) {
        Current current = lraContexts.get();

        // NB URIs would have been preferable to URLs for testing equality
        if (current == null || !current.stack.contains(lra)) {
            return false;
        }

        current.stack.remove(lra);

        if (current.stack.empty()) {
            clearContext(current);
        }

        return true;
    }

    /**
     * push the current context onto the stack of contexts for this thread
     * @param lraId id of context to push (must not be null)
     */
    public static void push(URI lraId) {
        Current current = lraContexts.get();

        if (current == null) {
            lraContexts.set(new Current(lraId));
        } else {
            if (!current.stack.contains(lraId)) {
                current.stack.push(lraId);
            }
        }
    }

    public static List<Object> getContexts() {
        Current current = lraContexts.get();

        if (current == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(current.stack);
    }

    /**
     * If there is an LRA context on the calling thread then add it to the provided headers
     *
     * @param responseContext the header map to add the KRA context to
     */
    public static void updateLRAContext(ContainerResponseContext responseContext) {
        URI lraId = Current.peek();

        if (lraId != null) {
            responseContext.getHeaders().put(LRA_HTTP_HEADER, getContexts());
        } else {
            responseContext.getHeaders().remove(LRA_HTTP_HEADER);
        }
    }

    public static void updateLRAContext(URI lraId, MultivaluedMap<String, String> headers) {
        headers.putSingle(LRA_HTTP_HEADER, lraId.toString());
        push(lraId);
    }

    /**
     * If there is an LRA context on the calling thread then make it available as
     * a header on outgoing JAX-RS invocations
     *
     * @param context the context for the JAX-RS request
     */
    public static void updateLRAContext(ClientRequestContext context) {
        MultivaluedMap<String, Object> headers = context.getHeaders();

        if (headers.containsKey(LRA_HTTP_HEADER)) {
            // LRA context is explicitly set
            return;
        }

        URI lraId = Current.peek();

        if (lraId != null) {
            headers.putSingle(LRA_HTTP_HEADER, lraId);
        } else {
            Object lraContext = context.getProperty(LRA_HTTP_HEADER);

            if (lraContext != null) {
                headers.putSingle(LRA_HTTP_HEADER, lraContext);
            } else {
                headers.remove(LRA_HTTP_HEADER);
            }
        }
    }

    public static void popAll() {
        lraContexts.set(null);
    }

    public static void clearContext(MultivaluedMap<String, String> headers) {
        headers.remove(LRA_HTTP_HEADER);
        popAll();
    }

    public static <T> T getLast(List<T> objects) {
        return objects == null ? null : objects.stream().reduce((a, b) -> b).orElse(null);
    }
}
