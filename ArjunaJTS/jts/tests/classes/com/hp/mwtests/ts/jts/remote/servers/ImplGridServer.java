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
 * $Id: ImplGridServer.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.servers;

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

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CORBA.INVALID_TRANSACTION;

public class ImplGridServer extends Test
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

	String refFile = "/tmp/trangrid.ref";
	String serverName = "ImplGrid";

	if (System.getProperty("os.name").startsWith("Windows"))
	{
	    refFile = "C:\\temp\\trangrid.ref";
	}
	
	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-reffile") == 0)
		refFile = args[i+1];
	    if (args[i].compareTo("-help") == 0)
	    {
		System.out.println("Usage: ImplGridServer [-reffile <file>] [-help]");
		assertFailure();
	    }
	}

	trangrid_i gridI = new trangrid_i((short) 100, (short) 100);
	Services serv = new Services(myORB);
	
	try
	{
	    registerService(refFile, myORB.orb().object_to_string(gridI.getReference()));

	    System.out.println("**ImplGrid server started**");

	    assertReady();
	    assertSuccess();

	    myOA.run();
	}
	catch (Exception e)
	{
	    System.err.println("ImplGrid server caught exception: "+e);
	    assertFailure();
	}

	myOA.shutdownObject(gridI);
    
	System.out.println("**ImplGrid server exiting**");
    }

    public static void main(String[] args)
    {
	ImplGridServer igs = new ImplGridServer();
	igs.initialise(null, null, args, new LocalHarness());
	igs.runTest();
    }
}

