/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.util;

import com.arjuna.webservices.logging.WSCLogger;

import jakarta.xml.ws.Service;
import java.security.PrivilegedAction;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public final class ServiceAction<T extends Service> implements PrivilegedAction<T> {

    private final Class<T> serviceClass;

    public ServiceAction(Class<T> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public static <T extends Service> ServiceAction<T> getInstance(final Class<T> serviceClass) {
        return new ServiceAction<>(serviceClass);
    }

    @Override
    public T run() {
        try {
            return serviceClass.newInstance();
        } catch (final InstantiationException | IllegalAccessException e) {
            WSCLogger.i18NLogger.warn_cannot_create_service_instance(serviceClass, e);
            throw new RuntimeException(e);
        }
    }
}