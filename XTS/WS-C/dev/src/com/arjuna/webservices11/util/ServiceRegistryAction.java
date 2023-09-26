/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.util;

import com.arjuna.webservices11.ServiceRegistry;

import java.security.PrivilegedAction;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ServiceRegistryAction implements PrivilegedAction<ServiceRegistry> {

    private static final ServiceRegistryAction INSTANCE = new ServiceRegistryAction();

    private ServiceRegistryAction() {

    }

    public static ServiceRegistryAction getInstance() {
        return INSTANCE;
    }

    @Override
    public ServiceRegistry run() {
        return ServiceRegistry.getRegistry();
    }

}