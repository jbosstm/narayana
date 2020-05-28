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
package org.jboss.narayana.rest.bridge.inbound;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Priority;
import javax.ejb.Stateless;
import javax.ejb.Stateful;
import javax.transaction.Transactional;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

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
