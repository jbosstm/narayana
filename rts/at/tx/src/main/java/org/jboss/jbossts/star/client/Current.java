/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.client;

import jakarta.ws.rs.core.MultivaluedMap;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Current {
    private static final ThreadLocal<Current> sraContexts = new ThreadLocal<>();

    private Stack<URL> stack;
    private Map<String, Object> state;

    private Current(URL url) {
        stack = new Stack<>();
        stack.push(url);
    }

    public static Object putState(String key, Object value) {
        Current current = sraContexts.get();

        if (current != null)
            return current.updateState(key, value);

        return null;
    }

    public static Object getState(String key) {
        Current current = sraContexts.get();

        if (current != null && current.state != null)
            return current.state.get(key);

        return null;
    }

    public static <T> T getLast(List<T> objects) {
        return objects == null ? null : objects.stream().reduce((a, b) -> b).orElse(null);
    }

    public Object updateState(String key, Object value) {
        if (state == null)
            state = new HashMap<>();

        return state.put(key, value);
    }

    private static void clearContext(Current current) {
        if (current.state != null)
            current.state.clear();

        sraContexts.set(null);
    }

    public static URL peek() {
        Current current = sraContexts.get();

        return current != null ? current.stack.peek() : null;
    }

    public static URL pop() {
        Current current = sraContexts.get();
        URL sraId = null;

        if (current != null) {
            sraId = current.stack.pop(); // there must be at least one

            if (current.stack.empty())
                clearContext(current);
        }

        return sraId;
    }


    public static boolean pop(URL sra) {
        Current current = sraContexts.get();

        // NB URIs would have been preferable to URLs for testing equality
        if (current == null || !current.stack.contains(sra))
            return false;

        current.stack.remove(sra);

        if (current.stack.empty())
            clearContext(current);

        return true;
    }

    /**
     * push the current context onto the stack of contexts for this thread
     * @param sraId id of context to push (must not be null)
     */
    public static void push(URL sraId) {
        Current current = sraContexts.get();

        if (current == null) {
            sraContexts.set(new Current(sraId));
        } else {
            if (!current.stack.peek().equals(sraId))
                current.stack.push(sraId);
        }
    }

    public static void updateSRAContext(MultivaluedMap<String, Object> headers) {
        URL sraId = Current.peek();

        if (sraId != null)
            headers.putSingle(SRAClient.RTS_HTTP_CONTEXT_HEADER, sraId);
        else
            headers.remove(SRAClient.RTS_HTTP_CONTEXT_HEADER);
    }

    public static void popAll() {
        sraContexts.set(null);
    }

    public static void clearContext(MultivaluedMap<String, String> headers) {
        headers.remove(SRAClient.RTS_HTTP_CONTEXT_HEADER);
        popAll();
    }

    public static void updateSRAContext(URL sraId, MultivaluedMap<String, String> headers) {
        headers.putSingle(SRAClient.RTS_HTTP_CONTEXT_HEADER, sraId.toString());
        push(sraId);
    }
}