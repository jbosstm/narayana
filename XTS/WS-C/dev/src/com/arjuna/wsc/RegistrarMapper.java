/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
/*
 * Copyright (c) 2002, 2003, Arjuna Technologies Limited.
 *
 * RegistrarMapper.java
 */

package com.arjuna.wsc;

import java.util.HashMap;
import java.util.Map;

/**
 * The registrar mapper.
 * @author kevin
 */
public class RegistrarMapper
{
    /**
     * The factory singleton.
     */
    private static final RegistrarMapper FACTORY = new RegistrarMapper() ;
    
    /**
     * The registrar map.
     */
    private final Map registrarMap = new HashMap() ;
    
    /**
     * Get the registrar mapper singleton.
     * @return The registrar mapper singleton.
     */
    public static RegistrarMapper getFactory()
    {
        return FACTORY ;
    }
    
    /**
     * Default constructor
     */
    protected RegistrarMapper()
    {
    }
    
    /**
     * Add a registrar for the specified protocol identifier.
     * @param protocolIdentifier The protocol identifier.
     * @param registrar The registrar.
     */
    public void addRegistrar(final String protocolIdentifier, final Registrar registrar)
    {
        synchronized(registrarMap)
        {
            registrarMap.put(protocolIdentifier, registrar) ;
        }
        registrar.install(protocolIdentifier) ;
    }

    /**
     * Get the registrar for the specified protocol identifier.
     * @param protocolIdentifier The protocol identifier.
     * @return The registrar.
     */
    public Registrar getRegistrar(final String protocolIdentifier)
    {
        final Object localRegistrar ;
        synchronized(registrarMap)
        {
            localRegistrar = registrarMap.get(protocolIdentifier) ;
        }
        return (Registrar)localRegistrar ;
    }

    /**
     * Remove the registrar for the specified protocol identifier.
     * @param protocolIdentifier The protocol identifier.
     */
    public void removeRegistrar(final String protocolIdentifier)
    {
        final Object localRegistrar ;
        synchronized(registrarMap)
        {
            localRegistrar = registrarMap.remove(protocolIdentifier) ;
        }
        if (localRegistrar != null)
        {
            ((Registrar)localRegistrar).uninstall(protocolIdentifier) ;
        }
    }
}
