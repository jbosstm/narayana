/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
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
package org.jboss.jbossts.star.test;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.grizzly.spdy.SpdyAddOn;
import org.glassfish.grizzly.spdy.SpdyMode;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.uri.UriComponent;

import jakarta.servlet.Servlet;
import jakarta.ws.rs.ProcessingException;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class SpdyEnabledHttpServer {
    public static HttpServer create(URI u, Map<String, String> initParams, String trustStoreFile, String trustStorePswd, int threadPoolSize, boolean enableSpdy) throws IOException {
        return create(u, ServletContainer.class, null, initParams, null, trustStoreFile, trustStorePswd, threadPoolSize, enableSpdy);
    }

    public static HttpServer create(URI u, Class<? extends Servlet> c, Servlet servlet,
                                     Map<String, String> initParams, Map<String, String> contextInitParams,
            String trustStoreFile, String trustStorePswd, int poolSize, boolean enableSpdy)
            throws IOException {
        if (u == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }

        String path = u.getPath();
        if (path == null) {
            throw new IllegalArgumentException("The URI path, of the URI " + u + ", must be non-null");
        } else if (path.isEmpty()) {
            throw new IllegalArgumentException("The URI path, of the URI " + u + ", must be present");
        } else if (path.charAt(0) != '/') {
            throw new IllegalArgumentException("The URI path, of the URI " + u + ". must start with a '/'");
        }

        path = String.format("/%s", UriComponent.decodePath(u.getPath(), true).get(1).toString());

        WebappContext context = new WebappContext("GrizzlyContext", path);
        ServletRegistration registration;
        if (c != null) {
            registration = context.addServlet(c.getName(), c);
        } else {
            registration = context.addServlet(servlet.getClass().getName(), servlet);
        }
        registration.addMapping("/*");

        if (contextInitParams != null) {
            for (Map.Entry<String, String> e : contextInitParams.entrySet()) {
                context.setInitParameter(e.getKey(), e.getValue());
            }
        }

        if (initParams == null) {
//            registration.setInitParameter(ClassPathResourceConfig.PROPERTY_CLASSPATH,
//                    System.getProperty("java.class.path").replace(File.pathSeparatorChar, ';'));
        } else {
            registration.setInitParameters(initParams);
        }

        HttpServer server = createHttpServer(u, null, true, getSSLConfig(trustStoreFile, trustStorePswd), poolSize, enableSpdy); //GrizzlyHttpServerFactory.createHttpServer(u);

        context.deploy(server);
        return server;
    }

    private static SSLEngineConfigurator getSSLConfig(String trustStoreFile, String trustStorePswd) {
        SSLContextConfigurator sslContext = new SSLContextConfigurator();
        sslContext.setKeyStoreFile(trustStoreFile);
        sslContext.setKeyStorePass(trustStorePswd);

        return new SSLEngineConfigurator(sslContext).setClientMode(false);
    }

    private static void addSpdy(HttpServer server) {
        NetworkListener listener = server.getListener("grizzly");
        SpdyAddOn spdyAddOn = new SpdyAddOn(SpdyMode.NPN);
        listener.registerAddOn(spdyAddOn);
    }

    private static HttpServer createHttpServer(final URI uri,
                                               final GrizzlyHttpContainer handler,
                                               final boolean secure,
                                               final SSLEngineConfigurator sslEngineConfigurator,
                                               final int poolSize,
                                               final boolean enableSpdy)
            throws ProcessingException {
        final String host = (uri.getHost() == null) ? NetworkListener.DEFAULT_NETWORK_HOST
                : uri.getHost();
        final int port = (uri.getPort() == -1) ? 80 : uri.getPort();
        final HttpServer server = new HttpServer();
        final NetworkListener listener = new NetworkListener("grizzly", host, port);

        listener.setSecure(secure);

        if (sslEngineConfigurator != null) {
            listener.setSSLEngineConfig(sslEngineConfigurator);
        }

        if (poolSize > 0) {
            TCPNIOTransport transport = listener.getTransport();
            transport.getKernelThreadPoolConfig().setMaxPoolSize(poolSize);
        }

        if (enableSpdy) {
            SpdyAddOn spdyAddOn = new SpdyAddOn(SpdyMode.NPN);
            System.out.printf("SPDY: max conc. streams: %d%n", spdyAddOn.getMaxConcurrentStreams());
            listener.registerAddOn(spdyAddOn);
        }

        listener.setSecure(secure);
        server.addListener(listener);

        // Map the path to the processor.
        final ServerConfiguration config = server.getServerConfiguration();
        if (handler != null) {
            config.addHttpHandler(handler, uri.getPath());
        }

        config.setPassTraceRequest(true);

        try {
            // Start the server.
            server.start();
        } catch (IOException ex) {
            throw new ProcessingException("IOException thrown when trying to start grizzly server", ex);
        }

        return server;
    }


}
