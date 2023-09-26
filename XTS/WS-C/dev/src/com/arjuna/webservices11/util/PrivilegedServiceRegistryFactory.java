/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.util;

import com.arjuna.webservices11.ServiceRegistry;

import java.security.AccessController;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class PrivilegedServiceRegistryFactory {

    private static final PrivilegedServiceRegistryFactory INSTANCE = new PrivilegedServiceRegistryFactory();

    private PrivilegedServiceRegistryFactory() {

    }

    public static PrivilegedServiceRegistryFactory getInstance() {
        return INSTANCE;
    }

    public ServiceRegistry getServiceRegistry() {
        final ServiceRegistryAction serviceRegistryAction = ServiceRegistryAction.getInstance();

        if (System.getSecurityManager() == null) {
            return serviceRegistryAction.run();
        }

        return AccessController.doPrivileged(serviceRegistryAction);
    }

}