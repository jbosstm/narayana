/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
 * $Id: TimeoutClient.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.timeout;

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
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

public class TimeoutClient extends Test
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
	    
	    CurrentImple current = OTSImpleManager.current();
	    Control theControl = null;
	    String objectReference = "/tmp/object.ref";
	    String serverName = "SetGet";

	    if (System.getProperty("os.name").startsWith("Windows"))
		objectReference = "C:\\temp\\object.ref";
	    
	    for (int i = 0; i < args.length; i++)
	    {
		if (args[i].compareTo("-reffile") == 0)
		    objectReference = args[i+1];
		if (args[i].compareTo("-help") == 0)
		{
		    System.out.println("Usage: TimeoutClient [-reffile <file>] [-help]");
		    assertFailure();
		}
	    }

	    SetGet SetGetVar = null;

	    System.out.println("Setting transaction timeout to 2 seconds.");
	
	    current.set_timeout(2);
	
	    current.begin();
	    current.begin();
    
	    try
	    {
		Services serv = new Services(myORB);
		
		SetGetVar = SetGetHelper.narrow(myORB.orb().string_to_object(getService(objectReference)));
	    }
	    catch (Exception e)
	    {
		System.err.println("Bind to object failed: "+e);
		e.printStackTrace(System.err);
		assertFailure();
	    }

	    try
	    {
		theControl = current.get_control();
	
		SetGetVar.set((short) 2, theControl);

		theControl = null;

		System.out.println("Set value.");
	    }
	    catch (Exception e)
	    {
		System.err.println("Call to set or get failed: "+e);
		e.printStackTrace(System.err);
		assertFailure();
	    }

	    try
	    {
		System.out.println("Now sleeping for 5 seconds.");
	
		Thread.sleep(5000);
	    }
	    catch (Exception e)
	    {
	    }

	    System.out.println("\ncommitting nested action.");

	    try
	    {
		current.commit(true);
		assertFailure();
	    }
	    catch (TRANSACTION_ROLLEDBACK  e1)
	    {
		System.out.println("Caught TransactionRolledBack");
	    }
	    catch (INVALID_TRANSACTION  e1)	/* For JacORB */
	    {
		System.out.println("Caught InvalidTransaction");
	    }

	    System.out.println("\ncommitting top-level action");

	    try
	    {
		current.commit(true);
		assertFailure();
	    }
	    catch (TRANSACTION_ROLLEDBACK  e2)
	    {
		System.out.println("Caught TransactionRolledBack");
	    }
	    catch (INVALID_TRANSACTION  e3)
	    {
		System.out.println("Caught InvalidTransaction");
	    }
	    catch (Exception e)
	    {
		System.out.println("Caught other exception: "+e);
		assertFailure();
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace(System.err);
	    assertFailure();
	}

	System.out.println("\nTest completed successfully.");

	myOA.destroy();
	myORB.shutdown();	
	assertSuccess();
    }

    public static void main(String[] args)
    {
	TimeoutClient tc = new TimeoutClient();
	tc.initialise(null, null, args, new LocalHarness());
	tc.runTest();
    }
}
