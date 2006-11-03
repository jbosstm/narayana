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
 * $Id: Implementations.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna;

import com.arjuna.ats.arjuna.gandiva.inventory.Inventory;

import com.arjuna.ats.internal.arjuna.objectstore.ShadowingStoreSetup;
import com.arjuna.ats.internal.arjuna.objectstore.ActionStoreSetup;
import com.arjuna.ats.internal.arjuna.objectstore.HashedActionStoreSetup;
import com.arjuna.ats.internal.arjuna.objectstore.ShadowNoFileLockStoreSetup;
import com.arjuna.ats.internal.arjuna.objectstore.HashedStoreSetup;
import com.arjuna.ats.internal.arjuna.objectstore.JDBCStoreSetup;
import com.arjuna.ats.internal.arjuna.objectstore.JDBCActionStoreSetup;
import com.arjuna.ats.internal.arjuna.objectstore.CacheStoreSetup;
import com.arjuna.ats.internal.arjuna.objectstore.CacheStoreSetup;

import com.arjuna.ats.internal.arjuna.gandiva.nameservice.JNSSetup;
import com.arjuna.ats.internal.arjuna.gandiva.nameservice.PNSSetup;

import com.arjuna.ats.internal.arjuna.PersistenceRecordSetup;
import com.arjuna.ats.internal.arjuna.CadaverRecordSetup;

/*
 * No naming or inventory implementations added yet.
 */

/**
 * Module specific class that is responsible for adding any implementations
 * to the inventory.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Implementations.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class Implementations
{

public static synchronized boolean added ()
    {
	return _added;
    }
    
public static synchronized void initialise ()
    {
	if (!_added)
	{
	    Inventory.inventory().addToList(new ShadowingStoreSetup());
	    Inventory.inventory().addToList(new ShadowNoFileLockStoreSetup());
	    Inventory.inventory().addToList(new ActionStoreSetup());
	    Inventory.inventory().addToList(new HashedActionStoreSetup());
	    Inventory.inventory().addToList(new HashedStoreSetup());
	    Inventory.inventory().addToList(new JDBCStoreSetup());
	    Inventory.inventory().addToList(new JDBCActionStoreSetup());
	    Inventory.inventory().addToList(new CacheStoreSetup());

	    Inventory.inventory().addToList(new JNSSetup());
	    Inventory.inventory().addToList(new PNSSetup());

	    /*
	     * Now add various abstract records which crash recovery needs.
	     */

	    Inventory.inventory().addToList(new PersistenceRecordSetup());
	    Inventory.inventory().addToList(new CadaverRecordSetup());
	    Inventory.inventory().addToList(new DisposeRecordSetup());

	    _added = true;
	}
    }

private Implementations ()
    {
    }

private static boolean _added = false;

    static
    {
	initialise();
    }
    
}
