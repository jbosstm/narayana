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
package com.hp.mwtests.ts.txoj.objectmodeltest;

/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ObjectModelTest.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.gandiva.*;
import com.arjuna.ats.txoj.common.*;
import com.arjuna.ats.txoj.*;

import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;

public class ObjectModelTest
{
    
public static void main (String[] args)
    {
	ObjectName objName = null;
	boolean write = false;
	String uid = null;
	long objectModel = ObjectModel.SINGLE;
	
	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-single") == 0)
		objectModel = ObjectModel.SINGLE;
	    if (args[i].compareTo("-multiple") == 0)
		objectModel = ObjectModel.MULTIPLE;
	    if (args[i].compareTo("-write") == 0)
		write = true;
	    if (args[i].compareTo("-uid") == 0)
		uid = args[i+1];
	    if (args[i].compareTo("-help") == 0)
	    {
		System.out.println("Usage: com.hp.mwtests.ts.txoj.objectmodeltest.ObjectModelTest [-uid <uid>] [-single] [-multiple] [-write] [-help]");
		System.exit(0);
	    }
	}

	try
	{
	    objName = ObjectName.uniqueObjectName("PNS");	    
	    objName.setLongAttribute(ArjunaNames.StateManager_objectModel(), objectModel);

	    System.out.println("ObjectName: "+objName);
	}
	catch (Exception e)
	{
	    System.err.println(e);
	    System.exit(-1);
	}

	try
	{
	    AtomicObject obj = null;

	    if (uid != null)
	    {
		Uid u = new Uid(uid);
	
		obj = new AtomicObject(u, objName);
	    }
	    else
		obj = new AtomicObject(objName);

	    AtomicAction A = new AtomicAction();

	    A.begin();
	    
	    if (write)
	    {
		obj.set(1234);
		System.out.println("set state to 1234");
	    }
	    else
		System.out.println("got "+obj.get());

	    System.out.println("Thread sleeping");
	    
	    Thread.sleep(10000);

	    A.commit();
	    
	    obj = null;
	}
	catch (Exception e)
	{
	    System.err.println(e);
	    System.exit(-1);
	}
    }

}
