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

import java.util.HashMap;
import java.util.Map;

/**
 * The context of a SOAP message.
 * @author kevin
 */
public class MessageContext
{
    /**
     * The properties associated with the current context.
     */
    private Map properties ;
    /**
     * The message scheme.
     */
    private String scheme ;
    
    /**
     * The default constructor.
     */
    public MessageContext()
    {
    }
    
    /**
     * Set the property in the message context. 
     * @param key The property key.
     * @param value The property value.
     */
    public void setProperty(final Object key, final Object value)
    {
        getPropertyMap().put(key, value) ;
    }
    
    /**
     * Get the property from the message context
     * @param key The property key.
     * @return The property value or null if not set.
     */
    public Object getProperty(final Object key)
    {
        return (properties == null ? null : properties.get(key)) ;
    }
    
    /**
     * Get the message scheme.
     * @return The message scheme.
     */
    public String getScheme()
    {
        return scheme ;
    }
    
    /**
     * Set the message scheme.
     * @param scheme The message scheme.
     */
    public void setScheme(final String scheme)
    {
        this.scheme = scheme ;
    }
    
    /**
     * Get the property map.
     * @return The property map.
     */
    private Map getPropertyMap()
    {
        if (properties == null)
        {
            properties = new HashMap() ;
        }
        return properties ;
    }
}
