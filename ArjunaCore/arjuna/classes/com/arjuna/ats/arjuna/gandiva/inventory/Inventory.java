/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: Inventory.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.gandiva.inventory;

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.logging.tsLogger;

import com.arjuna.ats.arjuna.gandiva.*;
import com.arjuna.ats.internal.arjuna.gandiva.inventory.StaticInventory;
import java.io.PrintStream;

/**
 * Inventory implementations are accessed via instances of this
 * class.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Inventory.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class Inventory
{

    /**
     * Create a new instance using the specified implementation.
     * Users should not use this to create an Inventory, but
     * should use the inventory() method.
     */

public Inventory (InventoryImple imple)
    {
	_imple = imple;
    }

    /**
     * Return the current Inventory interface.
     */

public static Inventory inventory ()
    {
	if (Inventory._inventory == null)
	    Inventory._inventory = new Inventory(new StaticInventory());
	
	return Inventory._inventory;
    }
    
    /**
     * Create a new implementation of the specified type using a default
     * constructor.
     */

public synchronized Object createVoid (ClassName typeName)
    {
	return ((_imple == null) ? null : _imple.createVoid(typeName));
    }

    /**
     * Create a new implementation of the specified type, and pass its
     * constructor the ClassName parameter.
     */

public synchronized Object createClassName (ClassName typeName, ClassName paramClassName)
    {
	return ((_imple == null) ? null : _imple.createClassName(typeName, paramClassName));
    }
    
    /**
     * Create a new implementation of the specified type, and pass its
     * constructor the ObjectName parameter.
     */

public synchronized Object createObjectName (ClassName typeName, ObjectName paramObjectName)
    {
	return ((_imple == null) ? null : _imple.createObjectName(typeName, paramObjectName));
    }

    /**
     * Create a new implementation of the specified type, and pass its
     * constructor the array of Objects as the parameter.
     */

public synchronized Object createResources (ClassName typeName, Object[] paramResources)
    {
	return ((_imple == null) ? null : _imple.createResources(typeName, paramResources));
    }

    /**
     * Create a new implementation of the specified type, and pass its
     * constructor the ClassName and array of Objects as the parameter.
     */

public synchronized Object createClassNameResources (ClassName typeName, ClassName paramClassName,
						     Object[] paramResources)
    {
	return ((_imple == null) ? null : _imple.createClassNameResources(typeName, paramClassName, paramResources));
    }

    /**
     * Create a new implementation of the specified type, and pass its
     * constructor the ObjectName and array of Objects as the parameter.
     */

public synchronized Object createObjectNameResources (ClassName typeName,
						      ObjectName paramObjectName,
						      Object[] paramResources)
    {
	return ((_imple == null) ? null : _imple.createObjectNameResources(typeName, paramObjectName, paramResources));
    }
    

    /**
     * @message com.arjuna.ats.arjuna.gandiva.inventory.Inventory_1 [com.arjuna.ats.arjuna.gandiva.inventory.Inventory_1] - Inventory.addToList error - no implementation!
     */
public synchronized void addToList (InventoryElement creator)
    {
	if (_imple == null)
	{
	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.gandiva.inventory.Inventory_1");
	}
	else
	    _imple.addToList(creator);
    }
    
public synchronized void printList (PrintStream toUse)
    {
	if (_imple == null)
	    toUse.println("Inventory implementation null.");
	else
	    _imple.printList(toUse);
    }

public static ClassName name ()
    {
	return ArjunaNames.Interface_Inventory();
    }

public ClassName className ()
    {
	return null;
    }

public ClassName impleClassName ()
    {
	if (_imple != null)
	    return _imple.className();
	else
	    return ClassName.invalid();
    }

public Inventory castup (ClassName theType)
    {
	if (theType.equals(className()))
	    return this;
	else
	    return null;
    }

private InventoryImple _imple;
    
private static Inventory _inventory = null;
 
}
