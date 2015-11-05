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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogService;

public class Activator implements BundleActivator, ManagedService, Runnable {

    public static final String PID = "org.jboss.narayana";
    public static final String INTERN_PACKAGE = "org.jboss.narayana.osgi.jta.internal";
    public static final String SERVER_CLASS = INTERN_PACKAGE + ".OsgiServer";

    protected BundleContext bundleContext;

    protected ExecutorService executor = new ThreadPoolExecutor(0, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    private AtomicBoolean scheduled = new AtomicBoolean();

    private long schedulerStopTimeout = TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);

    private ServiceRegistration managedServiceRegistration;
    private Dictionary<String, ?> configuration;
    private Object service;

    @Override
    public void start(BundleContext context) throws Exception {
        bundleContext = context;
        scheduled.set(true);
        Hashtable<String, Object> props = new Hashtable<>();
        props.put(Constants.SERVICE_PID, PID);
        managedServiceRegistration = bundleContext.registerService(ManagedService.class, this, props);
        scheduled.set(false);
        reconfigure();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        scheduled.set(true);
        if (managedServiceRegistration != null) {
            managedServiceRegistration.unregister();
        }
        executor.shutdown();
        executor.awaitTermination(schedulerStopTimeout, TimeUnit.MILLISECONDS);
        doStop();
    }

    ClassLoader createClassLoader() {
        List<URL> urls = new ArrayList<>();
        // Find our base url
        String name = SERVER_CLASS.replace('.', '/') + ".class";
        URL url = bundleContext.getBundle().getResource(name);
        String strUrl = url.toExternalForm();
        if (!strUrl.endsWith(name)) {
            throw new IllegalStateException();
        }
        strUrl = strUrl.substring(0, strUrl.length() - name.length());
        try {
            urls.add(new URL(strUrl));
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
        // Find all embedded jars
        Collection<String> resources = bundleContext.getBundle().adapt(BundleWiring.class)
                .listResources("/", "*.jar", BundleWiring.LISTRESOURCES_LOCAL);
        for (String resource : resources) {
            urls.add(bundleContext.getBundle().getResource(resource));
        }
        // Create the classloader
        return new URLClassLoader(urls.toArray(new URL[urls.size()]),
                new ClassLoader(getClass().getClassLoader()) {
                    @Override
                    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                        // We forbid to load the server from the parent
                        if (name.startsWith(INTERN_PACKAGE)) {
                            throw new ClassNotFoundException(name);
                        }
                        return super.loadClass(name, resolve);
                    }
                });
    }

    protected void doStart() throws Exception {
        ClassLoader classLoader = createClassLoader();
        Class<?> osgiServerClass = classLoader.loadClass(SERVER_CLASS);
        service = osgiServerClass.getConstructor(BundleContext.class, Dictionary.class)
                .newInstance(bundleContext, configuration);
        service.getClass().getMethod("start").invoke(service);
    }

    protected void doStop() {
        if (service != null) {
            try {
                service.getClass().getMethod("stop").invoke(service);
            } catch (Throwable t) {
                warn("Error stopping service", t);
            } finally {
                service = null;
            }
        }
    }

    public void updated(Dictionary<String, ?> properties) {
        this.configuration = properties;
        reconfigure();
    }

    protected void reconfigure() {
        if (scheduled.compareAndSet(false, true)) {
            executor.submit(this);
        }
    }

    @Override
    public void run() {
        scheduled.set(false);
        doStop();
        try {
            doStart();
        } catch (Exception e) {
            warn("Error starting service", e);
            doStop();
        }
    }

    protected void warn(String message, Throwable t) {
        ServiceReference<LogService> ref = bundleContext.getServiceReference(LogService.class);
        if (ref != null) {
            LogService svc = bundleContext.getService(ref);
            svc.log(LogService.LOG_WARNING, message, t);
            bundleContext.ungetService(ref);
        }
    }

}
