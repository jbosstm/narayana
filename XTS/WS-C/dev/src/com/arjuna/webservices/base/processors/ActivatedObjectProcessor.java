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
package com.arjuna.webservices.base.processors;

import java.util.HashMap;
import java.util.Map;

/**
 * This class manages the association between an object and its identifier.
 */
public class ActivatedObjectProcessor
{
    /**
     * The identifier to object map.
     */
    protected Map objectMap = new HashMap() ;
    /**
     * The object to identifier map.
     */
    protected Map identifierMap = new HashMap() ;

    /**
     * Activate the object.
     * @param object The object.
     * @param identifier The identifier.
     */
    public synchronized void activateObject(final Object object, final String identifier)
    {
        objectMap.put(identifier, object);
        identifierMap.put(object, identifier);
    }

    /**
     * Deactivate the object.
     * @param object The object.
     */
    public synchronized void deactivateObject(final Object object)
    {
        String identifier = (String) identifierMap.remove(object) ;
        if (identifier != null)
        {
            objectMap.remove(identifier) ;
        }
    }
    
    /**
     * Get the object with the specified identifier.
     * @param identifier The identifier.
     * @return The participant or null if not known.
     */
    public synchronized Object getObject(final String identifier)
    {
        return objectMap.get(identifier) ;
    }
    
    /**
     * Get the number of active objects.
     * @return The number of active objects.
     */
    public synchronized int count()
    {
        return objectMap.size() ;
    }
}
