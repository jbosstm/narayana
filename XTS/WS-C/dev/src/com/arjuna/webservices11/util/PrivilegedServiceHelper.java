/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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