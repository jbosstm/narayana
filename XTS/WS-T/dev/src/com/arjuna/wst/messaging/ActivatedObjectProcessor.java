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
package com.arjuna.wst.messaging;

import java.util.HashMap;
import java.util.Map;

/**
 * The base class for processors acting on registered objects.
 */
class ActivatedObjectProcessor
{
    /**
     * Lock for guarding the maps.
     */
    private final byte[] lock = new byte[0] ;
    
    /**
     * The identifier to object map.
     */
    private Map objectMap = new HashMap() ;
    /**
     * The object to identifier map.
     */
    private Map identifierMap = new HashMap() ;

    /**
     * Activate the object.
     * @param object The object.
     * @param identifier The identifier.
     */
    void activateObject(final Object object, final String identifier)
    {
        synchronized(lock)
        {
            objectMap.put(identifier, object);
            identifierMap.put(object, identifier);
        }
    }

    /**
     * Deactivate the object.
     * @param object The object.
     */
    void deactivateObject(final Object object)
    {
        synchronized(lock)
        {
            String identifier = (String) identifierMap.remove(object) ;
            if (identifier != null)
            {
                objectMap.remove(identifier) ;
            }
        }
    }
    
    /**
     * Get the object with the specified identifier.
     * @param identifier The identifier.
     * @return The participant or null if not known.
     */
    Object getObject(final String identifier)
    {
        synchronized(lock)
        {
            return objectMap.get(identifier) ;
        }
    }
}
