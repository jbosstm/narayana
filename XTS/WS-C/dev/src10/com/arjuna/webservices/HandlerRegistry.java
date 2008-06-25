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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import com.arjuna.webservices.util.InsertionOrderSet;

/**
 * The handler registry for a service.
 * @author kevin
 */
public class HandlerRegistry
{
    /**
     * The header handlers for the service.
     */
    private final Map headerHandlers = new HashMap() ;
    /**
     * The unmodifiable version of the header handlers.
     */
    private final Map unmodifiableHeaderHandlers = Collections.unmodifiableMap(headerHandlers) ;
    /**
     * The body handlers for the service.
     */
    private final Map bodyHandlers = new HashMap() ;
    /**
     * The handler for SOAP faults.
     */
    private BodyHandler faultHandler ;
    /**
     * The interceptor handlers for the service.
     */
    private final Set interceptorHandlers = new InsertionOrderSet() ;
    /**
     * The unmodifiable version of the interceptor handlers.
     */
    private final Set unmodifiableInterceptorHandlers = Collections.unmodifiableSet(interceptorHandlers) ;
    
    /**
     * Register the header handler against the specified qualified name.
     * @param headerName The qualified name of the header.
     * @param handler The header handler.
     */
    public void registerHeaderHandler(final QName headerName, final HeaderHandler handler)
    {
        headerHandlers.put(headerName, handler) ;
    }

    /**
     * Remove the header handler for the specified qualified name.
     * @param headerName The qualified name of the header.
     */
    public void removeHeaderHandler(final QName headerName)
    {
        headerHandlers.remove(headerName) ;
    }

    /**
     * Get the header handler associated with the specified header name.
     * @param headerName The name of the header.
     * @return The header handler or null if not recognised.
     */
    public HeaderHandler getHeaderHandler(final QName headerName)
    {
        return (HeaderHandler)headerHandlers.get(headerName) ;
    }
    
    /**
     * Get the header handler map.
     * @return The header handler map.
     */
    public Map getHeaderHandlers()
    {
        return unmodifiableHeaderHandlers ;
    }
    
    /**
     * Register the body handler against the specified qualified name.
     * @param bodyName The qualified name of the body.
     * @param handler The body handler.
     */
    public void registerBodyHandler(final QName bodyName, final BodyHandler handler)
    {
        bodyHandlers.put(bodyName, handler) ;
    }
    
    /**
     * Remove the body handler for the specified qualified name.
     * @param bodyName The qualified name of the body.
     */
    public void removeBodyHandler(final QName bodyName)
    {
        bodyHandlers.remove(bodyName) ;
    }

    /**
     * Get the body handler associated with the specified body name.
     * @param bodyName The name of the body.
     * @return The body handler or null if not recognised.
     */
    public BodyHandler getBodyHandler(final QName bodyName)
    {
        return (BodyHandler)bodyHandlers.get(bodyName) ;
    }
    
    /**
     * Register a handler to process SOAP faults.
     * @param faultHandler The fault handler.
     */
    public void registerFaultHandler(final BodyHandler faultHandler)
    {
        this.faultHandler = faultHandler ;
    }
    
    /**
     * Get the fault handler.
     * @return The fault handler.
     */
    public BodyHandler getFaultHandler()
    {
        return faultHandler ;
    }
    
    /**
     * Register the specified interceptor handler
     * @param interceptorHandler The interceptor handler.
     */
    public void registerInterceptorHandler(final InterceptorHandler interceptorHandler)
    {
        interceptorHandlers.add(interceptorHandler) ;
    }
    
    /**
     * Remove the specified interceptor handler
     * @param interceptorHandler The interceptor handler.
     */
    public void removeInterceptorHandler(final InterceptorHandler interceptorHandler)
    {
        interceptorHandlers.remove(interceptorHandler) ;
    }
    
    /**
     * Get the interceptor handlers.
     * @return the interceptor handlers.
     */
    public Set getInterceptorHandlers()
    {
        return unmodifiableInterceptorHandlers ;
    }
}
