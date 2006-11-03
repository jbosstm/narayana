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
package com.hp.mwtests.ts.arjuna.objectstore;

/*
 * Copyright (C) 2001,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JDBCInventory.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.common.Environment;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreImple;
import com.arjuna.ats.arjuna.gandiva.inventory.Inventory;
import com.arjuna.mwlabs.testframework.unittest.Test;

public class JDBCInventory extends Test
{

public void run(String[] args)
    {
	arjPropertyManager.propertyManager.setProperty(Environment.STATIC_INVENTORY_IMPLE+"1", "com.arjuna.ats.internal.arjuna.objectstore.JDBCStoreSetup");
	arjPropertyManager.propertyManager.setProperty(Environment.JDBC_USER_DB_ACCESS, "com.hp.mwtests.ts.arjuna.objectstore.MyAccess");

	ObjectStoreImple os = (ObjectStoreImple) Inventory.inventory().createVoid(ArjunaNames.Implementation_ObjectStore_JDBCStore());

	if (os != null)
        {
	    logInformation("\nPassed.");
            assertSuccess();
        }
	else
	{
	    Inventory.inventory().printList(System.out);

	    logInformation("\nFailed.");
            assertFailure();
	}
    }

public static void main(String[] args)
    {
        JDBCInventory test = new JDBCInventory();
        test.initialise(null, null, args, new com.arjuna.mwlabs.testframework.unittest.LocalHarness());
        test.run(args);
    }
}
