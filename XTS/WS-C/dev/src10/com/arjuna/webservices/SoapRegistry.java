/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.webservices;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * The registry of SOAP service providers, SOAP services and SOAP clients .
 * @author kevin
 */
public class SoapRegistry
{
    /**
     * The registry singleton.
     */
    private static final SoapRegistry REGISTRY = new SoapRegistry() ;
    
    /**
     * The SOAP service provider registry.
     */
    private final Map soapServiceProviderRegistry = new TreeMap() ;
    /**
     * The SOAP service registry.
     */
    private final Map soapServiceRegistry = new TreeMap() ;
    /**
     * The SOAP client registry.
     */
    private final Map soapClientRegistry = new TreeMap() ;
    
    /**
     * Get the service registry.
     * @return The service registry.
     */
    public static SoapRegistry getRegistry()
    {
        return REGISTRY ;
    }
    
    /**
     * Register the specified service.
     * @param scheme The service scheme.
     * @param provider The service provider.
     */
    public void registerSoapServiceProvider(final String scheme, final SoapServiceEndpointProvider provider)
    {
        synchronized(soapServiceProviderRegistry)
        {
            soapServiceProviderRegistry.put(scheme, provider) ;
        }
    }
    
    /**
     * Remove the specified service.
     * @param scheme The service scheme.
     */
    public void removeSoapServiceProvider(final String scheme)
    {
        synchronized(soapServiceProviderRegistry)
        {
            soapServiceProviderRegistry.remove(scheme) ;
        }
    }
    
    /**
     * Register the specified client.
     * @param scheme The client scheme.
     * @param client The client.
     */
    public void registerSoapClient(final String scheme, final SoapClient client)
    {
        synchronized(soapClientRegistry)
        {
            soapClientRegistry.put(scheme, client) ;
        }
    }

    /**
     * Get the SOAP client for the specified scheme.
     * @param scheme The addressing scheme.
     * @return The SOAP client or null if not known.
     */
    public SoapClient getSoapClient(final String scheme)
    {
        synchronized(soapClientRegistry)
        {
            return (SoapClient)soapClientRegistry.get(scheme) ;
        }
    }
    
    /**
     * Remove the specified client.
     * @param scheme The client scheme.
     */
    public void removeSoapClient(final String scheme)
    {
        synchronized(soapClientRegistry)
        {
            soapClientRegistry.remove(scheme) ;
        }
    }
    
    /**
     * Register the specified service.
     * @param serviceName The unique name of the service.
     * @param soapService The service instance.
     */
    public void registerSoapService(final String serviceName, final SoapService soapService)
    {
        synchronized(soapServiceRegistry)
        {
            soapServiceRegistry.put(serviceName, soapService) ;
        }
    }

    /**
     * Get the SOAP service for the specified service.
     * @param serviceName The name of the service.
     * @return The SOAP service or null if not known.
     */
    public SoapService getSoapService(final String serviceName)
    {
        synchronized(soapServiceRegistry)
        {
            return (SoapService)soapServiceRegistry.get(serviceName) ;
        }
    }
    
    /**
     * Remove the specified service.
     * @param serviceName The unique name of the service.
     */
    public void removeSoapService(final String serviceName)
    {
        synchronized(soapServiceRegistry)
        {
            soapServiceRegistry.remove(serviceName) ;
        }
    }
    
    /**
     * Get the service URI.
     * @param scheme The addressing scheme.
     * @param serviceName The service name.
     * @return The service URI or null if not registered.
     */
    public String getServiceURI(final String scheme, final String serviceName)
    {
        final SoapServiceEndpointProvider soapServiceProvider ;
        synchronized(soapServiceRegistry)
        {
            soapServiceProvider = (SoapServiceEndpointProvider)soapServiceProviderRegistry.get(scheme) ;
        }
        if (soapServiceProvider != null)
        {
            return soapServiceProvider.getServiceURI(scheme, serviceName) ;
        }
        return null ;
    }
    
    /**
     * Get the service URI.
     * @param serviceName The service name.
     * @return The service URI or null if not registered.
     */
    public String getServiceURI(final String serviceName)
    {
        synchronized(soapServiceRegistry)
        {
            final Iterator entryIter = soapServiceProviderRegistry.entrySet().iterator() ;
            while(entryIter.hasNext())
            {
                final Map.Entry entry = (Map.Entry)entryIter.next() ;
                final String entryScheme = (String)entry.getKey() ;
                final SoapServiceEndpointProvider soapServiceProvider = (SoapServiceEndpointProvider)entry.getValue() ;
                final String uri = soapServiceProvider.getServiceURI(entryScheme, serviceName) ;
                if (uri != null)
                {
                    return uri ;
                }
            }
        }
        return null ;
    }
    
    /**
     * Get the service URI.
     * @param messageContext The message context.
     * @param serviceName The service name.
     * @return The service URI or null if not registered.
     */
    public String getServiceURI(final MessageContext messageContext, final String serviceName)
    {
        final String scheme = messageContext.getScheme() ;
        final SoapServiceEndpointProvider soapServiceProvider ;
        synchronized(soapServiceRegistry)
        {
            soapServiceProvider = (SoapServiceEndpointProvider)soapServiceProviderRegistry.get(scheme) ;
        }
        if (soapServiceProvider != null)
        {
            return soapServiceProvider.getServiceURI(messageContext, serviceName) ;
        }
        else
        {
            return null ;
        }
    }
    
    /**
     * Get the scheme from a URL.
     * @param url The URL.
     * @return The scheme.
     */
    public String getScheme(final String url)
    {
        final int separatorIndex = url.indexOf(':') ;
        if (separatorIndex >= 0)
        {
            return url.substring(0, separatorIndex) ;
        }
        else
        {
            return url ;
        }
    }
}
