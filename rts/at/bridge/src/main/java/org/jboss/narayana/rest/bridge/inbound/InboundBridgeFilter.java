/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.rest.bridge.inbound;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Priority;
import jakarta.ejb.Stateless;
import jakarta.ejb.Stateful;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

import org.jboss.jbossts.star.util.TxLinkNames;
import org.jboss.logging.Logger;
import org.jboss.resteasy.core.ResourceMethodInvoker;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@Provider
@Priority(Priorities.USER - 100)
public final class InboundBridgeFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(InboundBridgeFilter.class);

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeFilter.filter(" + requestContext + ")");
        }

        if (isBridgeRequired(requestContext)) {
            final String enlistmentUrl = getEnlistmentUrl(requestContext);
            startBridge(enlistmentUrl);
        }
    }

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
            throws IOException {

        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeFilter.filter(" + requestContext + ", " + responseContext + ")");
        }

        if (isBridgeRequired(requestContext)) {
            final String enlistmentUrl = getEnlistmentUrl(requestContext);
            stopBridge(enlistmentUrl);
        }
    }

    private void startBridge(final String enlistmentUrl) {
        if (enlistmentUrl != null) {
            final InboundBridge inboundBridge = InboundBridgeManager.getInstance().createInboundBridge(enlistmentUrl);
            inboundBridge.start();
        }
    }

    private void stopBridge(final String enlistmentUrl) {
        if (enlistmentUrl != null) {
            final InboundBridge inboundBridge = InboundBridgeManager.getInstance().createInboundBridge(enlistmentUrl);
            inboundBridge.stop();
        }
    }

    private String getEnlistmentUrl(final ContainerRequestContext requestContext) {
        final Map<String, String> links = getLinksFromHeaders(requestContext.getHeaders());
        final String enlistmentUrl = getEnlistmentUrlFromLinks(links);

        return enlistmentUrl;
    }

    private String getEnlistmentUrlFromLinks(final Map<String, String> links) {
        String enlistmentUrl = null;

        if (links.get(TxLinkNames.PARTICIPANT) != null) {
            enlistmentUrl = links.get(TxLinkNames.PARTICIPANT);
        }

        return enlistmentUrl;
    }

    /**
     *
     * @param headers
     * @return
     */
    private Map<String, String> getLinksFromHeaders(final MultivaluedMap<String, String> headers) {
        final Collection<String> linkHeaders = extractLinkHeaders(headers);
        final Map<String, String> links = new HashMap<String, String>();

        for (final String linkString : linkHeaders) {
            Link link = Link.valueOf(linkString);
            links.put(link.getRel(), link.getUri().toASCIIString());
        }

        return links;
    }

    private Collection<String> extractLinkHeaders(final MultivaluedMap<String, String> headers) {
        Collection<String> linkHeaders = headers.get("Link");

        if (linkHeaders == null) {
            linkHeaders = headers.get("link");
        }

        if (linkHeaders == null) {
            linkHeaders = new ArrayList<String>();
        }

        return linkHeaders;
    }

    private boolean isBridgeRequired(final ContainerRequestContext requestContext) {
        final ResourceMethodInvoker methodInvoker = (ResourceMethodInvoker)
                requestContext.getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");

        if (methodInvoker == null) {
            return false;
        }

        Method method = methodInvoker.getMethod();

        // CDI transactional method is declared by annotation @Transactional, EJB class is transactional by default
        return method.isAnnotationPresent(Transactional.class) || method.getDeclaringClass().isAnnotationPresent(Transactional.class)
                || method.getDeclaringClass().isAnnotationPresent(Stateless.class)
                || method.getDeclaringClass().isAnnotationPresent(Stateful.class);
    }

}