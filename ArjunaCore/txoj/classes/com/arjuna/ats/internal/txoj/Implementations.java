/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
 * Copyright (C) 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: Implementations.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.txoj;

import com.arjuna.ats.arjuna.gandiva.inventory.Inventory;
import com.arjuna.ats.internal.txoj.lockstore.BasicLockStoreSetup;
import com.arjuna.ats.internal.txoj.lockstore.BasicPersistentLockStoreSetup;
import com.arjuna.ats.internal.txoj.semaphore.BasicSemaphoreSetup;

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
	    Inventory.inventory().addToList(new BasicLockStoreSetup());
	    Inventory.inventory().addToList(new BasicPersistentLockStoreSetup());
	    Inventory.inventory().addToList(new BasicSemaphoreSetup());	

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
