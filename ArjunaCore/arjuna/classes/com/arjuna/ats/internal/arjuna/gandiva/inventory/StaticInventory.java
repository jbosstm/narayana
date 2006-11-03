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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: StaticInventory.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.gandiva.inventory;

import com.arjuna.ats.arjuna.logging.tsLogger;

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.ats.arjuna.gandiva.inventory.*;
import com.arjuna.ats.arjuna.gandiva.*;
import com.arjuna.ats.internal.arjuna.Implementations;
import java.io.PrintStream;
import java.util.*;

/**
 * The StaticInventory required each implementation type to be registered
 * with it explicitly. This can happen in an application specific manner,
 * or (more typically) through an "Implementations" class. Each module
 * may specific one or more Implementations classes which are responsible
 * for registering one or more implementations with the inventory. How
 * these Implementations are instantiated and made to do this can either
 * be application specific, or dynamically by specifying each Implementation
 * as a Java property. Any property that starts with
 * com.arjuna.ats.internal.arjuna.implementation.inventory.staticInventoryImple
 * will be assumed to have a value which is a valid Java class. An instance
 * of this class will be created when the StaticInventory is created and
 * it should then register any implementations with the inventory.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: StaticInventory.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class StaticInventory extends InventoryImple
{

public StaticInventory ()
    {
    }
    
public synchronized Object createVoid (ClassName typeName)
    {
	InventoryElement creator = find(typeName);

	if (creator != null)
	    return creator.createVoid();
	else
	    return null;
    }

public synchronized Object createClassName (ClassName typeName, ClassName paramClassName)
    {
	InventoryElement creator = find(typeName);

	if (creator != null)
	    return creator.createClassName(paramClassName);
	else
	    return null;
    }
    
public synchronized Object createObjectName (ClassName typeName, ObjectName paramObjectName)
    {
	InventoryElement creator = find(typeName);

	if (creator != null)
	    return creator.createObjectName(paramObjectName);
	else
	    return null;
    }

public synchronized Object createResources (ClassName typeName, Object[] paramResources)
    {
	InventoryElement creator = find(typeName);

	if (creator != null)
	    return creator.createResources(paramResources);
	else
	    return null;
    }

public synchronized Object createClassNameResources (ClassName typeName, ClassName paramClassName,
						     Object[] paramResources)
    {
	InventoryElement creator = find(typeName);

	if (creator != null)
	    return creator.createClassNameResources(paramClassName, paramResources);
	else
	    return null;
    }

public synchronized Object createObjectNameResources (ClassName typeName,
						      ObjectName paramObjectName,
						      Object[] paramResources)
    {
	InventoryElement creator = find(typeName);

	if (creator != null)
	    return creator.createObjectNameResources(paramObjectName, paramResources);
	else
	    return null;
    }

public synchronized void addToList (InventoryElement creator)
    {
	if ((creator != null) && (creator.className() != null))
	{
	    if (headOfList == null)
		headOfList = new InventoryList(creator);
	    else
	    {
		boolean found = false;
		InventoryList marker = headOfList, trail = null;
		
		while ((!found) && (marker != null))
		{
		    if (!creator.className().equals(marker._instance.className()))
		    {
			trail = marker;
			marker = marker._next;
		    }
		    else
			found = true;
		}

		if (!found)
		    trail._next = new InventoryList(creator);
	    }
	}
    }
    
public synchronized void printList (PrintStream toUse)
    {
	InventoryList marker = headOfList;

	toUse.println("StaticInventory contains:");
    
	while (marker != null)
	{
	    toUse.println(marker._instance.className());
	    marker = marker._next;
	}

	toUse.println("End of list.");
    }

public ClassName className ()
    {
	return ArjunaNames.Implementation_Inventory_StaticInventory();
    }

public static ClassName type ()
    {
	return ArjunaNames.Implementation_Inventory_StaticInventory();
    }
    
    /**
     * No need to synchronize since it can only be called
     * from other synchronized methods.
     *
     * @message com.arjuna.ats.internal.arjuna.gandiva.inventory.StaticInventory_1 [com.arjuna.ats.internal.arjuna.gandiva.inventory.StaticInventory_1] -  cannot find {0} implementation.
     */
    
protected InventoryElement find (ClassName className)
    {
	if (!StaticInventory.initialised)
	    initialise();
	    
	if (headOfList != null)
	{    
	    InventoryList marker = headOfList;
    
	    while (marker != null)
	    {
		if ((marker._instance.className() != null) &&
		    (marker._instance.className().equals(className)))
		{
		    return marker._instance;
		}
		else
		{
		    marker = marker._next;
		}
	    }
	}

	if (tsLogger.arjLoggerI18N.isWarnEnabled())
	{
	    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.gandiva.inventory.StaticInventory_1", 
					new Object[]{className});
	}
	
	return null;
    }

    /**
     * Scan the properties for any Implementations that may be
     * added dynamically. If we find them, instantiate them and
     * hope that works!
     */

private synchronized final void initialise ()
    {
	Implementations.initialise();

	Enumeration e = arjPropertyManager.propertyManager.propertyNames();
	
	if (e != null)
	{
	    while (e.hasMoreElements())
	    {
		String name = (String) e.nextElement();
		
		if (name.startsWith(com.arjuna.ats.arjuna.common.Environment.STATIC_INVENTORY_IMPLE))
		{
		    String className = arjPropertyManager.propertyManager.getProperty(name);

		    try
		    {
			Class c = Thread.currentThread().getContextClassLoader().loadClass(className);
			Object o = c.newInstance();
		    }
		    catch (Exception ex)
		    {
			ex.printStackTrace();
		    }
		}
	    }
	}

	initialised = true;
    }
    
private static InventoryList headOfList = null;
private static boolean       initialised = false;


}
