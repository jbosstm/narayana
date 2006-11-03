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
 * $Id: InventoryElement.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.gandiva.inventory;

import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.gandiva.ObjectName;

/**
 * Implementations which are to be made available to the Inventory can
 * be created using one of the methods provided by implementations of
 * this interface. This enables the Inventory to not have to know about
 * implementation specific.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: InventoryElement.java 2342 2006-03-30 13:06:17Z  $
 * @since 1.0.
 */
 
public interface InventoryElement
{
    
    /**
     * Create an implementation using a default constructor.
     */

public abstract Object createVoid ();

/**
 * Create an implementation and pass the constructor the ClassName parameter.
 */
 
public abstract Object createClassName (ClassName className);

/**
 * Create an implementation and pass the constructor the ObjectName parameter.
 */
 
public abstract Object createObjectName (ObjectName objectName);

/**
 * Create an implementation and pass the constructor the array of Objects.
 */
 
public abstract Object createResources (Object[] resources);

/**
 * Create an implementation and pass the constructor the ClassName
 * and array of Objects.
 */
 
public abstract Object createClassNameResources (ClassName className, Object[] resources);

/**
 * Create an implementation and pass the constructor the ObjectName and
 * array of Objects.
 */
 
public abstract Object createObjectNameResources (ObjectName objectName, Object[] resources);

/**
 * Return the ClassName of the implementation to be created.
 */
 
public abstract ClassName className ();
    
}
