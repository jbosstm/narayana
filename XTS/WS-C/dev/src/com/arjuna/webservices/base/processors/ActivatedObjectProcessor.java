/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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