/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11;

import java.util.Map;
import java.util.TreeMap;

/**
 * The registry of WS service providers for the WS-C and WS-T services.
 * @author kevin
 */
public class ServiceRegistry
{
    /**
     * The registry singleton.
     */
    private static final ServiceRegistry REGISTRY = new ServiceRegistry() ;

    /**
     * The SOAP service provider registry.
     */
    private final Map<String, String> serviceProviderRegistry = new TreeMap<String, String>() ;

    /**
     * The secure SOAP service provider registry.
     */
    private final Map<String, String> secureServiceProviderRegistry = new TreeMap<String, String>() ;

    /**
     * Get the service registry.
     * @return The service registry.
     */
    public static ServiceRegistry getRegistry()
    {
        // Check if the caller has a permission to use this method
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission(ServiceRegistry.class.getName() + ".getRegistry"));
        }

        return REGISTRY ;
    }

    /**
     * Register the specified service.
     * @param serviceName The service name.
     * @param url The service url.
     */
    public void registerServiceProvider(final String serviceName, final String url)
    {
        synchronized(serviceProviderRegistry)
        {
            serviceProviderRegistry.put(serviceName, url) ;
        }
    }

    /**
     * Register the specified secure service.
     * @param serviceName The secure service name.
     * @param url The service url.
     */
    public void registerSecureServiceProvider(final String serviceName, final String url)
    {
        synchronized(secureServiceProviderRegistry)
        {
            secureServiceProviderRegistry.put(serviceName, url) ;
        }
    }

    /**
     * Remove the specified service.
     * @param serviceName The service name.
     */
    public void removeServiceProvider(final String serviceName)
    {
        synchronized(serviceProviderRegistry)
        {
            serviceProviderRegistry.remove(serviceName) ;
        }
    }

    /**
     * Remove the specified secure service.
     * @param serviceName The secure service name.
     */
    public void removeSecureServiceProvider(final String serviceName)
    {
        synchronized(secureServiceProviderRegistry)
        {
            secureServiceProviderRegistry.remove(serviceName) ;
        }
    }

    /**
     * Get the service URI.
     * @param serviceName The service name.
     * @return The service URI or null if not registered.
     */
    public String getServiceURI(final String serviceName)
    {
        String uri;

        synchronized(serviceProviderRegistry)
        {
            uri = serviceProviderRegistry.get(serviceName) ;
        }

        return uri;
    }

    /**
     * Get the secure service URI.
     * @param serviceName The secure service name.
     * @return The secure service URI or null if not registered.
     */
    public String getSecureServiceURI(final String serviceName)
    {
        String uri;

        synchronized(secureServiceProviderRegistry)
        {
            uri = secureServiceProviderRegistry.get(serviceName) ;
        }

        return uri;
    }

    /**
     * Get the service URI.
     * @param serviceName The service name.
     * @param isSecure true if the secure service URL is required false if the normal service URL is required
     * @return The service URI or null if not registered.
     */
    public String getServiceURI(final String serviceName, boolean isSecure)
    {
        if (isSecure) {
            return getSecureServiceURI(serviceName);
        } else {
            return getServiceURI(serviceName);
        }
    }
}