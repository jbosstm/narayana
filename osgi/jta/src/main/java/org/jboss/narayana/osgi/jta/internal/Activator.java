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
package org.jboss.narayana.osgi.jta.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.transaction.TransactionManager;

public class Activator implements BundleActivator {
    private ServiceRegistration transactionManagerReg;


    public void start(BundleContext context) {
        ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            // Register the TransactionManager service if is not already available
            if (context.getServiceReference(TransactionManager.class.getName()) == null)
            {
                TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
                transactionManagerReg = context.registerService(TransactionManager.class.getName(), tm, null);
            }

        } finally {
            Thread.currentThread().setContextClassLoader(ctxLoader);
        }
    }


    public void stop(BundleContext context) {
        if (transactionManagerReg != null) {
            transactionManagerReg.unregister();
            transactionManagerReg = null;
        }

    }

}
