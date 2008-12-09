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
 * $Id: LockStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.txoj.lockstore;

import com.arjuna.ats.txoj.TxOJNames;
import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.txoj.common.*;
import com.arjuna.ats.internal.txoj.Implementations;
import com.arjuna.ats.arjuna.gandiva.inventory.Inventory;

import com.arjuna.ats.txoj.exceptions.LockStoreException;

/**
 * The lock store interface is the application's route to using
 * a specific lock store implementation. The interface dynamically
 * binds to an implementation of the right type.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: LockStore.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class LockStore
{

    /*
     * Default for single lock store type.
     */
    
public LockStore (String param)
    {
	Object[] resources = new Object[1];
	resources[0] = param;

	if (!singleCheck)
	{
	    singleLockStoreType = new ClassName(txojPropertyManager.propertyManager.getProperty(Environment.SINGLE_LOCKSTORE,
									    TxOJNames.Implementation_LockStore_defaultStore().stringForm()));
	    singleCheck = true;
	}

	Object ptr = Inventory.inventory().createResources(singleLockStoreType, resources);

	if (ptr instanceof LockStoreImple)
	    _imple = (LockStoreImple) ptr;
	else
	    _imple = null;
    }

public LockStore (ClassName typeName, String param)
    {
	if (typeName == null)
	{
	    if (!singleCheck)
	    {
		singleLockStoreType = new ClassName(txojPropertyManager.propertyManager.getProperty(Environment.SINGLE_LOCKSTORE,
										TxOJNames.Implementation_LockStore_defaultStore().stringForm()));
		singleCheck = true;
	    }

	    typeName = singleLockStoreType;
	}
	    
	Object[] resources = new Object[1];
	resources[0] = param;
	
	Object ptr = Inventory.inventory().createResources(typeName, resources);

	if (ptr instanceof LockStoreImple)
	    _imple = (LockStoreImple) ptr;
	else
	    _imple = null;
    }

public LockStore (Object[] param)
    {
	if (param.length == 3)
	{
	    ClassName typeName = (ClassName) param[0];
	    int modelType = ((Integer) param[1]).intValue();
	    Object[] resources = new Object[1];

	    resources[0] = param[2];

	    /*
	     * Assume anything in the property field overrides anything given
	     * by the application.
	     */

	    String type = null;
	    
	    if (modelType == ObjectModel.SINGLE)
	    {
		if (!singleCheck)
		{
		    type = txojPropertyManager.propertyManager.getProperty(Environment.SINGLE_LOCKSTORE);
		    singleCheck = true;

		    if (type != null)
			singleLockStoreType = new ClassName(type);
		}

		if (singleLockStoreType != null)
		    typeName = singleLockStoreType;
	    }
	    else
	    {
		if (!multipleCheck)
		{
		    type = txojPropertyManager.propertyManager.getProperty(Environment.MULTIPLE_LOCKSTORE, null);
		    multipleCheck = true;

		    if (type != null)
			multipleLockStoreType = new ClassName(type);
		}

		if (multipleLockStoreType != null)
		    typeName = multipleLockStoreType;
	    }

	    Object ptr = Inventory.inventory().createResources(typeName, resources);

	    if (ptr instanceof LockStoreImple)
		_imple = (LockStoreImple) ptr;
	    else
		_imple = null;
	    
	}
	else
	    _imple = null;
    }
	    
public InputObjectState read_state (Uid u, String tName) throws LockStoreException
    {
	return ((_imple != null) ? _imple.read_state(u, tName) : null);
    }
    
public boolean remove_state (Uid u, String tname)
    {
	return ((_imple != null) ? _imple.remove_state(u, tname) : false);
    }
    
public boolean write_committed (Uid u, String tName, OutputObjectState state)
    {
	return ((_imple != null) ? _imple.write_committed(u, tName, state) : false);
    }

public ClassName className ()
    {
	return ((_imple != null) ? _imple.className() : ClassName.invalid());
    }
    
private LockStoreImple _imple;

private static ClassName singleLockStoreType = null;
private static ClassName multipleLockStoreType = null;
private static boolean   singleCheck = false;
private static boolean   multipleCheck = false;
 
    static 
    {
	/*
	 * Make sure the possible implementations are in the
	 * inventory. Otherwise this is going to be a very short
	 * ride!
	 */

	if (!Implementations.added())
	    Implementations.initialise();
    }
    
}

