/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.util;

import jakarta.xml.ws.Service;
import java.security.AccessController;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class PrivilegedServiceFactory<T extends Service> {

    private final Class<T> serviceClass;

    public PrivilegedServiceFactory(final Class<T> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public static <T extends Service> PrivilegedServiceFactory<T> getInstance(final Class<T> serviceClass) {
        return new PrivilegedServiceFactory<>(serviceClass);
    }

    public T getService() {
        final ServiceAction<T> serviceAction = ServiceAction.getInstance(serviceClass);

        if (System.getSecurityManager() == null) {
            return serviceAction.run();
        }

        return AccessController.doPrivileged(serviceAction);
    }

}