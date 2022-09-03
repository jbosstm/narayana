/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package com.arjuna.webservices11.util;

import javax.xml.namespace.QName;
import jakarta.xml.ws.EndpointReference;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceFeature;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class PrivilegedServiceHelper {

    private static final PrivilegedServiceHelper INSTANCE = new PrivilegedServiceHelper();

    private PrivilegedServiceHelper() {

    }

    public static PrivilegedServiceHelper getInstance() {
        return INSTANCE;
    }

    public <T> T getPort(final Service service, final QName portName, final Class<T> serviceEndpointInterface) {
        if (System.getSecurityManager() == null) {
            return service.getPort(portName, serviceEndpointInterface);
        }

        return AccessController.doPrivileged(new PrivilegedAction<T>() {
            @Override
            public T run() {
                return service.getPort(portName, serviceEndpointInterface);
            }
        });
    }

    public <T> T getPort(final Service service, final QName portName, final Class<T> serviceEndpointInterface,
            final WebServiceFeature... features) {

        if (System.getSecurityManager() == null) {
            return service.getPort(portName, serviceEndpointInterface, features);
        }

        return AccessController.doPrivileged(new PrivilegedAction<T>() {
            @Override
            public T run() {
                return service.getPort(portName, serviceEndpointInterface, features);
            }
        });
    }

    public <T> T getPort(final Service service, final EndpointReference endpointReference,
            final Class<T> serviceEndpointInterface, final WebServiceFeature... features) {

        if (System.getSecurityManager() == null) {
            return service.getPort(endpointReference, serviceEndpointInterface, features);
        }

        return AccessController.doPrivileged(new PrivilegedAction<T>() {
            @Override
            public T run() {
                return service.getPort(endpointReference, serviceEndpointInterface, features);
            }
        });
    }

    public <T> T getPort(final Service service, final Class<T> serviceEndpointInterface,
            final WebServiceFeature... features) {

        if (System.getSecurityManager() == null) {
            return service.getPort(serviceEndpointInterface, features);
        }

        return AccessController.doPrivileged(new PrivilegedAction<T>() {
            @Override
            public T run() {
                return service.getPort(serviceEndpointInterface, features);
            }
        });
    }

}
