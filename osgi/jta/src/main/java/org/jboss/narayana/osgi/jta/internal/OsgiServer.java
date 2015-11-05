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

import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import com.arjuna.ats.jbossatx.jta.TransactionManagerService;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.common.util.propertyservice.PropertiesFactory;
import org.jboss.tm.XAResourceRecovery;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class OsgiServer implements ServiceTrackerCustomizer<XAResourceRecovery, XAResourceRecovery> {

    private final BundleContext bundleContext;
    private final Dictionary<String, ?> configuration;

    List<ServiceRegistration> registrations;
    ServiceTracker<XAResourceRecovery, XAResourceRecovery> resourceRecoveryTracker;
    TransactionManagerService transactionManagerService;
    RecoveryManagerService recoveryManagerService;

    public OsgiServer(BundleContext bundleContext, Dictionary<String, ?> configuration) {
        this.bundleContext = bundleContext;
        this.configuration = configuration;
    }

    public void start() throws Exception {
        ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            doStart();
        } finally {
            Thread.currentThread().setContextClassLoader(ctxLoader);
        }
    }

    public void doStart() throws Exception {
        Properties properties = PropertiesFactory.getDefaultProperties();
        if (configuration != null) {
            for (Enumeration<String> keyEnum = configuration.keys(); keyEnum.hasMoreElements(); ) {
                String key = keyEnum.nextElement();
                String val = configuration.get(key).toString();
                properties.put(key, val);
            }
        }

        OsgiTransactionManager transactionManager = new OsgiTransactionManager();
        JTAEnvironmentBean jtaEnvironmentBean = jtaPropertyManager.getJTAEnvironmentBean();
        jtaEnvironmentBean.setTransactionManager(transactionManager);
        jtaEnvironmentBean.setUserTransaction(transactionManager);

        RecoveryManagerService rmSvc = new RecoveryManagerService();
        rmSvc.create();
        recoveryManagerService = rmSvc;

        resourceRecoveryTracker = new ServiceTracker<>(bundleContext, XAResourceRecovery.class, this);

        TransactionManagerService tmSvc = new TransactionManagerService();
        tmSvc.setTransactionSynchronizationRegistry(jtaEnvironmentBean.getTransactionSynchronizationRegistry());
        tmSvc.create();
        transactionManagerService = tmSvc;

        resourceRecoveryTracker.open();
        transactionManagerService.start();
        recoveryManagerService.start();

        register(TransactionManager.class, transactionManagerService.getTransactionManager());
        register(TransactionSynchronizationRegistry.class, transactionManagerService.getTransactionSynchronizationRegistry());
        register(UserTransaction.class, transactionManagerService.getUserTransaction());

    }

    public void stop() {
        ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            doStop();
        } finally {
            Thread.currentThread().setContextClassLoader(ctxLoader);
        }
    }

    protected void doStop() {
        if (registrations != null) {
            for (ServiceRegistration reg : registrations) {
                try {
                    reg.unregister();
                } catch (Throwable t) {
                    warn("Error unregistering service", t);
                }
            }
            registrations = null;
        }
        if (transactionManagerService != null) {
            try {
                try {
                    transactionManagerService.stop();
                } finally {
                    transactionManagerService.destroy();
                }
            } catch (Throwable t) {
                warn("Error stopping transaction manager service", t);
            } finally {
                transactionManagerService = null;
            }
        }
        if (recoveryManagerService != null) {
            try {
                try {
                    recoveryManagerService.stop();
                } finally {
                    recoveryManagerService.destroy();
                }
            } catch (Throwable t) {
                warn("Error stopping recovery manager service", t);
            } finally {
                recoveryManagerService = null;
            }
        }
        if (resourceRecoveryTracker != null) {
            try {
                resourceRecoveryTracker.close();
            } catch (Throwable t) {
                warn("Error stopping resource recovery tracker", t);
            } finally {
                resourceRecoveryTracker = null;
            }
        }
        TransactionReaper.terminate(false);
        TxControl.disable(true);
        StoreManager.shutdown();
    }

    protected <T> void register(Class<T> clazz, T service) {
        ServiceRegistration<T> registration = bundleContext.registerService(clazz, service, null);
        if (registrations == null) {
            registrations = new ArrayList<>();
        }
        registrations.add(registration);
    }

    protected void warn(String message, Throwable t) {
        ServiceReference<LogService> ref = bundleContext.getServiceReference(LogService.class);
        if (ref != null) {
            LogService svc = bundleContext.getService(ref);
            svc.log(LogService.LOG_WARNING, message, t);
            bundleContext.ungetService(ref);
        }
    }

    @Override
    public XAResourceRecovery addingService(ServiceReference<XAResourceRecovery> reference) {
        final XAResourceRecovery resourceRecovery = bundleContext.getService(reference);
        recoveryManagerService.addXAResourceRecovery(resourceRecovery);
        return resourceRecovery;
    }

    @Override
    public void modifiedService(ServiceReference<XAResourceRecovery> reference, XAResourceRecovery service) {
    }

    @Override
    public void removedService(ServiceReference<XAResourceRecovery> reference, XAResourceRecovery resourceRecovery) {
        recoveryManagerService.removeXAResourceRecovery(resourceRecovery);
        bundleContext.ungetService(reference);
    }

}
