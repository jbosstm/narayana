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
 * $Id: ExplicitInterClient.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.explicitinterposition;

import com.hp.mwtests.ts.jts.resources.*;
import com.hp.mwtests.ts.jts.orbspecific.resources.*;
import com.hp.mwtests.ts.jts.TestModule.*;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.extensions.*;
import com.arjuna.ats.jts.ExplicitInterposition;

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

public class ExplicitInterClient extends Test
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
	Control theControl = null;
	String objectReference = "/tmp/object.ref";
	String serverName = "SetGet";

	if (System.getProperty("os.name").startsWith("Windows"))
	{
	    objectReference = "C:\\temp\\object.ref";
	}
	
	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-object") == 0)
		objectReference = args[i+1];
	    if (args[i].compareTo("-help") == 0)
	    {
		System.out.println("Usage: ExplicitInterClient [-object <reference>] [-help]");
		assertFailure();
	    }
	}

	SetGet SetGetVar = null;
	short h = 0;

	try
	{
	    current.begin();
	    current.begin();
	    current.begin();
	}
	catch (Exception e)
	{
	    System.err.println("Caught exception during begin: "+e);
	    e.printStackTrace(System.err);
	    assertFailure();
	}

	try
	{
	    Services serv = new Services(myORB);
	    
	    SetGetVar = SetGetHelper.narrow(myORB.orb().string_to_object(getService(objectReference)));
	}
	catch (Exception ex)
	{
	    System.err.println("Failed to bind to setget server: "+ex);
	    ex.printStackTrace(System.err);
	    assertFailure();
	}

	try
	{
	    theControl = current.get_control();
	
	    SetGetVar.set((short) 2, theControl);
	    //	    SetGetVar.set((short) 2, theControl);	    

	    theControl = null;
	    
	    System.out.println("Set value.");
	}
	catch (Exception ex1)
	{
	    System.err.println("Unexpected system exception during set: "+ex1);
	    ex1.printStackTrace(System.err);
	    assertFailure();
	}

	try
	{
	    System.out.println("committing first nested action");
    
	    current.commit(true);

	    //	    SetGetVar.set((short) 4, current.get_control());
	    
	    System.out.println("committing second nested action");
    
	    current.commit(true);
	}
	catch (Exception sysEx)
	{
	    System.err.println("Caught unexpected exception during commit: "+sysEx);
	    sysEx.printStackTrace(System.err);
	    assertFailure();
	}
    
	try
	{
	    theControl = current.get_control();
	
	    h = SetGetVar.get(theControl);

	    theControl = null;
	
	    System.out.println("Got value.");
	}
	catch (Exception ex2)
	{
	    System.err.println("Unexpected system exception during get: "+ex2);
	    ex2.printStackTrace(System.err);
	    assertFailure();
	}

	try
	{
	    current.commit(true);

	    System.out.println("committed top-level action");

	    assertSuccess();
	}
	catch (Exception ep)
	{
	    System.err.println("Caught commit exception for top-level action: "+ep);
	    ep.printStackTrace(System.err);
	    assertFailure();
	}

	myOA.destroy();
	myORB.shutdown();
    }

    public static void main(String[] args)
    {
	ExplicitInterClient eic = new ExplicitInterClient();
	eic.initialise(null, null, args, new LocalHarness());
	eic.runTest();
    }
}
