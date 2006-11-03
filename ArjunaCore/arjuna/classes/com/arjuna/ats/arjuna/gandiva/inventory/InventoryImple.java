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
 * $Id: InventoryImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.gandiva.inventory;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.gandiva.*;
import com.arjuna.ats.internal.arjuna.Implementations;

import java.io.PrintStream;

/**
 * Inventory implementations must inherit from this base class
 * and provide implementations of the abstract methods.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: InventoryImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public abstract class InventoryImple
{

    /**
     * Create a new implementation of the specified type using a default
     * constructor.
     */

public abstract Object createVoid (ClassName typeName);

    /**
     * Create a new implementation of the specified type, and pass its
     * constructor the ClassName parameter.
     */

public abstract Object createClassName (ClassName typeName, ClassName paramClassName);
    
    /**
     * Create a new implementation of the specified type, and pass its
     * constructor the ObjectName parameter.
     */

public abstract Object createObjectName (ClassName typeName, ObjectName paramObjectName);

    /**
     * Create a new implementation of the specified type, and pass its
     * constructor the array of Objects as the parameter.
     */

public abstract Object createResources (ClassName typeName, Object[] paramResources);

    /**
     * Create a new implementation of the specified type, and pass its
     * constructor the ClassName and array of Objects as the parameter.
     */

public abstract Object createClassNameResources (ClassName typeName, ClassName paramClassName,
						 Object[] paramResources);

    /**
     * Create a new implementation of the specified type, and pass its
     * constructor the ObjectName and array of Objects as the parameter.
     */

public abstract Object createObjectNameResources (ClassName typeName,
						  ObjectName paramObjectName,
						  Object[] paramResources);
    
public abstract void addToList (InventoryElement creator);
public abstract void printList (PrintStream toUse);

public ClassName className ()
    {
	return InventoryImple._className;
    }

public static ClassName type ()
    {
	return InventoryImple._className;
    }
    
private static final ClassName _className = new ClassName("InventoryImple");
    
}
