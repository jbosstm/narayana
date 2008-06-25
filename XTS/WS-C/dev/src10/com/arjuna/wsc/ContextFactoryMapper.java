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
/*
 * Copyright (c) 2002, 2003, Arjuna Technologies Limited.
 *
 * ContextFactoryMapper.java
 */

package com.arjuna.wsc;

import java.util.HashMap;
import java.util.Map;

/**
 * The context factory mapper.
 * @author kevin
 */
public class ContextFactoryMapper
{
    /**
     * The factory singleton.
     */
    private static final ContextFactoryMapper FACTORY = new ContextFactoryMapper() ;
    
    /**
     * The context factory map.
     */
    private final Map contextFactoryMap = new HashMap() ;
    /**
     * The subordinate context factory mapper.
     */
    private ContextFactoryMapper subordinateContextFactoryMapper ;
    /**
     * The default context factory.
     */
    private ContextFactory defaultContextFactory ;
    
    /**
     * Get the context factory mapper singleton.
     * @return The context factory mapper singleton.
     */
    public static ContextFactoryMapper getFactory()
    {
        return FACTORY ;
    }
    
    /**
     * Default constructor
     */
    protected ContextFactoryMapper()
    {
    }
    
    /**
     * Add a context factory for the specified coordination type.
     * @param coordinationTypeURI The coordination type.
     * @param contextFactory The context factory.
     */
    public void addContextFactory(final String coordinationTypeURI, final ContextFactory contextFactory)
    {
        synchronized(contextFactoryMap)
        {
            contextFactoryMap.put(coordinationTypeURI, contextFactory) ;
        }
        contextFactory.install(coordinationTypeURI) ;
    }

    /**
     * Get the context factory for the specified coordination type.
     * @param coordinationTypeURI The coordination type.
     * @return The context factory.
     */
    public ContextFactory getContextFactory(final String coordinationTypeURI)
    {
        final Object localContextFactory ;
        synchronized(contextFactoryMap)
        {
            localContextFactory = contextFactoryMap.get(coordinationTypeURI) ;
        }
        if (localContextFactory != null)
        {
            return (ContextFactory)localContextFactory ;
        }
        if (subordinateContextFactoryMapper != null)
        {
            final ContextFactory subordinateContextFactory = subordinateContextFactoryMapper.getContextFactory(coordinationTypeURI) ;
            if (subordinateContextFactory != null)
            {
                return subordinateContextFactory ;
            }
        }
        return defaultContextFactory ;
    }

    /**
     * Remove the context factory for the specified coordination type.
     * @param coordinationTypeURI The coordination type.
     */
    public void removeContextFactory(final String coordinationTypeURI)
    {
        final Object localContextFactory ;
        synchronized(contextFactoryMap)
        {
            localContextFactory = contextFactoryMap.remove(coordinationTypeURI) ;
        }
        if (localContextFactory != null)
        {
            ((ContextFactory)localContextFactory).uninstall(coordinationTypeURI) ;
        }
    }

    /**
     * Get the subordinate coordination factory mapper.
     * @return The subordinate coordination factory mapper.
     */
    public ContextFactoryMapper getSubordinateContextFactoryMapper()
    {
        return subordinateContextFactoryMapper ;
    }

    /**
     * Set a subordinate coordination factory mapper.
     * @param subordinateContextFactoryMapper The subordinate coordination factory mapper.
     */
    public void setSubordinateContextFactoryMapper(final ContextFactoryMapper subordinateContextFactoryMapper)
    {
        this.subordinateContextFactoryMapper = subordinateContextFactoryMapper ;
    }

    /**
     * Get the default coordination factory.
     * @return The default coordination factory.
     */
    public ContextFactory getDefaultContextFactory()
    {
        return defaultContextFactory ;
    }

    /**
     * Set the default coordination factory.
     * @param defaultContextFactory The default coordination factory.
     */
    public void setDefaultContextFactory(final ContextFactory defaultContextFactory)
    {
        this.defaultContextFactory = defaultContextFactory ;
    }
}
