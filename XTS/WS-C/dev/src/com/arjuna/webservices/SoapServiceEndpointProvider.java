/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.webservices;

/**
 * The interface for a SOAP service endpoint provider.
 * @author kevin
 */
public interface SoapServiceEndpointProvider
{
    /**
     * Get the URI of a service supported by this endpoint provider.
     * @param messageContext The message context.
     * @param serviceName The service name.
     * @return The service URI or null if not known.
     */
    public String getServiceURI(final MessageContext messageContext,
        final String serviceName) ;
    
    /**
     * Get the URI of a service supported by this endpoint provider.
     * @param scheme The addressing scheme of the endpoint provider.
     * @param serviceName The service name.
     * @return The service URI or null if not known.
     */
    public String getServiceURI(final String scheme, final String serviceName) ;
}
