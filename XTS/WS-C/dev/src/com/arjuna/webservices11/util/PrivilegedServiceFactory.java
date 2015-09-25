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

import javax.xml.ws.Service;
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
