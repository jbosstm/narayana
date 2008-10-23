/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 * (C) 2008,
 * @author Red Hat Middleware LLC.
 */
package com.arjuna.ats.internal.arjuna.objectstore;

import com.arjuna.ats.arjuna.gandiva.inventory.InventoryElement;
import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.gandiva.ObjectName;
import com.arjuna.ats.arjuna.ArjunaNames;

/**
 * Setup code for the in-memory ObjectStore impl.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2008-10
 */
public class VolatileStoreSetup implements InventoryElement
{
    /**
     * Create an implementation using a default constructor.
     */

    public Object createVoid()
    {
        return new VolatileStore();
    }

    /**
     * Create an implementation and pass the constructor the ClassName parameter.
     */

    public Object createClassName(ClassName className)
    {
        return new VolatileStore();
    }

    /**
     * Create an implementation and pass the constructor the ObjectName parameter.
     */

    public Object createObjectName(ObjectName objectName)
    {
        return null;
    }

    /**
     * Create an implementation and pass the constructor the array of Objects.
     */

    public Object createResources(Object[] resources)
    {
        return null;
    }

    /**
     * Create an implementation and pass the constructor the ClassName
     * and array of Objects.
     */

    public Object createClassNameResources(ClassName className, Object[] resources)
    {
        return null;
    }

    /**
     * Create an implementation and pass the constructor the ObjectName and
     * array of Objects.
     */

    public Object createObjectNameResources(ObjectName objectName, Object[] resources)
    {
        return null;
    }

    /**
     * Return the ClassName of the implementation to be created.
     */

    public ClassName className()
    {
        return ArjunaNames.Implementation_ObjectStore_VolatileStore();
    }
}
