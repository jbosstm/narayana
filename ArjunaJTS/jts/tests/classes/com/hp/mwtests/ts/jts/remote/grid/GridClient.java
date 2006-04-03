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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: GridClient.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.grid;

import com.hp.mwtests.ts.jts.resources.*;
import com.hp.mwtests.ts.jts.orbspecific.resources.*;
import com.hp.mwtests.ts.jts.TestModule.*;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.extensions.*;
import com.arjuna.ats.jts.OTSManager;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.mwlabs.testframework.unittest.Test;

import org.omg.CosTransactions.*;

import org.omg.CORBA.IntHolder;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CORBA.INVALID_TRANSACTION;

public class GridClient extends Test
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
	
	TransactionFactory theOTS = null;
	Control myControl = null;
	grid gridVar = null;
	int h = -1, w = -1, v = -1;
	String gridReference = "/tmp/grid.ref";
	String serverName = "Grid";

	if (System.getProperty("os.name").startsWith("Windows"))
	{
	    gridReference = "C:\\temp\\grid.ref";
	}
	
	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-help") == 0)
	    {
		System.out.println("Usage: GridClient [-server <name>] [-reffile <file>] [-help]");
		System.exit(0);
	    }
	    if (args[i].compareTo("-server") == 0)
	    {
		System.out.println("Sorry, server name not supported by ORB.");
		assertFailure();
	    }
	    if (args[i].compareTo("-reffile") == 0)
		gridReference = args[i+1];
	}

	Services serv = new Services(myORB);

	try
	{
	    String[] params = new String[1];

	    params[0] = Services.otsKind;

	    org.omg.CORBA.Object obj = serv.getService(Services.transactionService, params);

	    params = null;
	    theOTS =  TransactionFactoryHelper.narrow(obj);
	}
	catch (Exception e)
	{
	    System.err.println("Unexpected bind exception: "+e);
	    System.exit(1);
	}

	System.out.println("Creating transaction.");
	
	try
	{
	    myControl = theOTS.create(0);
	}
	catch (Exception e)
	{
	    System.err.println("Create call failed: "+e);
	    e.printStackTrace();
	    System.exit(1);
	}
	
	try
	{
	    gridVar = gridHelper.narrow(serv.getService(gridReference, null, Services.FILE));
	}
	catch (Exception e)
	{
	    System.err.println("Grid bind failed: "+e);
	    System.exit(1);
	}

	try
	{
	    h = gridVar.height();
	    w = gridVar.width();
	}
	catch (Exception e)
	{
	    System.err.println("Grid invocation failed: "+e);
	    System.exit(1);
	}

	System.out.println("height is "+h);
	System.out.println("width  is "+w);

	try
	{
	    System.out.println("calling set");
	     
	    gridVar.set(2, 4, 123, myControl);

	    System.out.println("calling get");
	
	    v = gridVar.get(2, 4, myControl);
	}
	catch (Exception sysEx)
	{
	    System.err.println("Grid set/get failed: "+sysEx);
	    System.exit(1);
	}

	// no problem setting and getting the elememt:
	System.out.println("grid[2,4] is "+v);
	
	// sanity check: make sure we got the value 123 back:
	if (v != 123)
	{
	    // oops - we didn't:
	    System.err.println("something went seriously wrong");

	    try
	    {
		myControl.get_terminator().rollback();
	    }
	    catch (Exception e)
	    {
	    }
	
	    System.exit(1);
	}
	else
	{
	    System.out.println("Committing transaction.");
	    
	    try
	    {
		Terminator handle = myControl.get_terminator();
      
		handle.commit(true);
	    }
	    catch (Exception sysEx)
	    {
		System.err.println("Transaction commit error: "+sysEx);
		System.exit(1);
	    }
	}
	
	/*
	 * OTSArjuna specific call to tell the system
	 * that we are finished with this transaction.
	 */

	try
	{
	    OTSManager.destroyControl(myControl);
	}
	catch (Exception e)
	{
	    System.out.println("Caught destroy exception: "+e);
	    System.exit(0);
	}

	myOA.destroy();
	myORB.shutdown();

	System.out.println("Test completed successfully.");
    }

}

