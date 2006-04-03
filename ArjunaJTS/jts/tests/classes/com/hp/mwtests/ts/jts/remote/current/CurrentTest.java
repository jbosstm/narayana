/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
 * $Id: CurrentTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.current;

import com.hp.mwtests.ts.jts.resources.*;
import com.hp.mwtests.ts.jts.orbspecific.resources.*;
import com.hp.mwtests.ts.jts.TestModule.*;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.extensions.*;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.mwlabs.testframework.unittest.Test;
import com.arjuna.mwlabs.testframework.unittest.LocalHarness;

import org.omg.CosTransactions.*;

import org.omg.CORBA.IntHolder;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CORBA.INVALID_TRANSACTION;

public class CurrentTest extends Test
{
    
    public void run(String[] args)
    {
	ORB myORB = null;
	RootOA myOA = null;

	try
	{
	    myORB = ORB.getInstance("test");
	    myOA = OA.getRootOA(myORB);
	    
	    myORB.initORB(args, null);
	    myOA.initOA();

	    ORBManager.setORB(myORB);
	    ORBManager.setPOA(myOA);
	}
	catch (Exception e)
	{
	    System.err.println("Initialisation failed: "+e);
	    assertFailure();
	}
    
	CurrentImple current = OTSImpleManager.current();
	Control myControl = null;
	String gridReference = "/tmp/grid.ref";
	String serverName = "Grid";
	grid gridVar = null;  // pointer the grid object that will be used.
	int h = -1, w = -1, v = -1;

	if (System.getProperty("os.name").startsWith("Windows"))
	{
	    gridReference = "C:\\temp\\grid.ref";
	}
	
	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-help") == 0)
	    {
		System.out.println("Usage: CurrentTest [-reffile <file>] [-help]");
		assertFailure();
	    }
	    if (args[i].compareTo("-reffile") == 0)
		gridReference = args[i+1];
	}

	System.out.println("Beginning transaction.");
	
	try
	{
	    current.begin();
	    
	    myControl = current.get_control();

	    if (myControl == null)
	    {
		System.err.println("Error - control is null!");
		assertFailure();
	    }
	}
	catch (Exception sysEx)
	{
	    sysEx.printStackTrace(System.err);
	    assertFailure();
	}
  
	try
	{
	    Services serv = new Services(myORB);
	    
	    gridVar = gridHelper.narrow(myORB.orb().string_to_object(getService(gridReference)));
	}
	catch (Exception sysEx)
	{
	    System.err.println("failed to bind to grid: "+sysEx);
	    sysEx.printStackTrace(System.err);
	    assertFailure();
	}

	try
	{
	    h = gridVar.height();
	    w = gridVar.width();
	}
	catch (Exception sysEx)
	{
	    System.err.println("grid height/width failed: "+sysEx);
	    sysEx.printStackTrace(System.err);
	    assertFailure();
	}

	System.out.println("height is "+h);
	System.out.println("width  is "+w);

	try
	{
	    gridVar.set(2, 4, 123, myControl);
	    v = gridVar.get(2, 4, myControl);
	}
	catch (Exception sysEx)
	{
	    System.err.println("grid set/get failed: "+sysEx);
	    sysEx.printStackTrace(System.err);
	    assertFailure();
	}

	// no problem setting and getting the elememt:
	System.out.println("grid[2,4] is "+v);

	// sanity check: make sure we got the value 123 back:
	if (v != 123)
	{
	    assertFailure();
	    // oops - we didn't:
	    System.err.println("something went seriously wrong");

	    try
	    {
		current.rollback();
	    }
	    catch (Exception e)
	    {
		System.err.println("rollback error: "+e);
		e.printStackTrace(System.err);
		assertFailure();
	    }
	
	    assertFailure();
	}
	else
	{
	    System.out.println("Committing transaction.");
	    
	    try
	    {
		current.commit(true);

		assertSuccess();
	    }
	    catch (Exception e)
	    {
		System.err.println("commit error: "+e);
		e.printStackTrace(System.err);
		assertFailure();
	    }

	    myOA.destroy();
	    myORB.shutdown();

	    System.out.println("Test completed successfully.");
	}
    }

    public static void main(String[] args)
    {
	CurrentTest ct = new CurrentTest();
	ct.initialise(null, null, args, new LocalHarness());
	ct.runTest();
    }
}

