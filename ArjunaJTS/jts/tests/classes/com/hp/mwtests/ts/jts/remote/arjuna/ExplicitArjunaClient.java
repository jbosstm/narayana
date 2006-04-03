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
 * $Id: ExplicitArjunaClient.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.arjuna;

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

public class ExplicitArjunaClient extends Test
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
	String refFile = "/tmp/explicitstack.ref";
	String serverName = "ExplicitStack";
	int value = 1;
	Control cont = null;

	if (System.getProperty("os.name").startsWith("Windows"))
	{
	    refFile = "C:\\temp\\explicitstack.ref";
	}
	
	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-reffile") == 0)
		refFile = args[i+1];
	    if (args[i].compareTo("-value") == 0)
	    {
		try
		{
		    Integer val = new Integer(args[i+1]);
		    value = val.intValue();
		}
		catch (Exception e)
		{
		    System.err.println(e);
		    assertFailure();
		}
	    }
	    if (args[i].compareTo("-help") == 0)
	    {
		System.out.println("Usage: ExplicitArjunaClient [-reffile <file>] [-value <number>] [-help]");
		assertFailure();
	    }
	}

	try
	{
	    System.out.println("Starting initialising top-level transaction.");
	
	    current.begin();

	    System.out.println("Initialising transaction name: "+current.get_transaction_name());
	}
	catch (Exception e)
	{
	    e.printStackTrace(System.err);
	    assertFailure();
	}

	ExplicitStack stackVar = null;   // pointer the grid object that will be used.
	
	try
	{
	    stackVar = ExplicitStackHelper.narrow(myORB.orb().string_to_object(getService(refFile)));
	}
	catch (Exception e)
	{
	    System.err.println("Bind error: "+e);
	    assertFailure();
	}

	try
	{
	    System.out.println("pushing "+value+" onto stack");

	    cont = current.get_control();
	    stackVar.push(value, cont);

	    System.out.println("\npushing "+(value+1)+" onto stack");

	    stackVar.push(value+1, cont);

	    cont = null;
	}
	catch (Exception e)
	{
	    e.printStackTrace(System.err);
	    assertFailure();
	}

	try
	{
	    current.commit(true);

	    System.out.println("Committed top-level transaction");
	    System.out.println("\nBeginning top-level transaction");
    
	    current.begin();

	    System.out.println("Top-level name: "+current.get_transaction_name());
    
	    IntHolder val = new IntHolder(-1);
    
	    cont = current.get_control();
    
	    if (stackVar.pop(val, cont) == 0)
	    {
		System.out.println("popped top of stack "+val.value);
		System.out.println("\nbeginning nested transaction");

		current.begin();

		System.out.println("nested name: "+current.get_transaction_name());

		cont = null;
		cont = current.get_control();    
		stackVar.push(value+2, cont);

		System.out.println("pushed "+(value+2)+" onto stack. Aborting nested action.");

		cont = null;  // current will destroy this control!
		current.rollback();
		cont = current.get_control();
	
		System.out.println("current transaction name: "+current.get_transaction_name());
		System.out.println("rolledback nested transaction");

		stackVar.pop(val, cont);

		System.out.println("\npopped top of stack is "+val.value);

		System.out.println("\nTrying to print stack contents - should fail.");
	
		stackVar.printStack();
	
		cont = null;
		current.commit(true);

		System.out.println("\nCommitted top-level transaction");
	
		if (current.get_transaction_name() == null)
		    System.out.println("current transaction name: null");
		else
		    System.out.println("Error - current transaction name: "
				       +current.get_transaction_name());
	
		if (val.value == value)
		{
		    System.out.println("\nThis is correct.");
		    assertSuccess();
		}
		else
		{
		    System.out.println("\nThis is incorrect. Value should be "+value);
		    assertFailure();
		}
	    }
	    else
	    {
		System.out.println("Error getting stack value.");
	
		current.rollback();

		System.out.println("\nRolledback top-level transaction.");
	    }

	    try
	    {
		System.out.println("\nPrinting stack contents (should be empty).");
	
		stackVar.printStack();
	    }
	    catch (Exception e)
	    {
		System.out.println("\nError - could not print.");
		assertFailure();
	    }
	}
	catch (Exception e)
	{
	    System.err.println("Caught unexpected exception: "+e);
	    assertFailure();
	}

	myOA.destroy();
	myORB.shutdown();
    }

    public static void main(String[] args)
    {
	ExplicitArjunaClient eac = new ExplicitArjunaClient();
	eac.initialise(null, null, args, new LocalHarness());
	eac.runTest();
    }
}

