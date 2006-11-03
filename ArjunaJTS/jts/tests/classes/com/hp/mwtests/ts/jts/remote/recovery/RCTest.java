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
 * $Id: RCTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.recovery;

import com.hp.mwtests.ts.jts.resources.*;
import com.hp.mwtests.ts.jts.orbspecific.resources.*;
import com.hp.mwtests.ts.jts.TestModule.*;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.extensions.*;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;

import org.omg.CosTransactions.*;

import org.omg.CORBA.IntHolder;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

public class RCTest
{
    
    public static void main (String[] args)
    {
	boolean shouldCommit = true;

	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-abort") == 0)
		shouldCommit = false;
	    if (args[i].compareTo("-help") == 0)
	    {
		System.out.println("Usage: RCTest [-abort]");
		System.exit(0);
	    }
	}

	boolean passed = false;
	Coordinator coord = null;
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
	    AtomicResource aImpl = new AtomicResource(shouldCommit);
	    Resource atomicObject = aImpl.getReference();

	    System.out.println("beginning top-level transaction.");
	    
	    current.begin();

	    Control myControl = current.get_control();

	    if (myControl == null)
	    {
		System.err.println("Error - myControl is nil");
		System.exit(0);
	    }
	    
	    System.out.println("getting coordinator");
	    
	    coord = myControl.get_coordinator();

	    myControl = null;
	    
	    System.out.println("registering resources.");

	    RecoveryCoordinator rc = null;
	    
	    try
	    {
		rc = coord.register_resource(atomicObject);
	    }
	    catch (Exception ex)
	    {
		System.out.println("Failed to register resources: "+ex);
		ex.printStackTrace();
		
		System.exit(0);
	    }

	    if (rc == null)
		System.out.println("No recovery coordinator reference.");
	    else
	    {
		Status s = Status.StatusUnknown;
		
		try
		{
		    System.out.println("Attempting to use recovery coordinator.");
		    
		    s = rc.replay_completion(atomicObject);
		}
		catch (NotPrepared e)
		{
		    s = Status.StatusActive;
		}
		catch (Exception ex)
		{
		    System.err.println("Caught: "+ex);
		    
		    ex.printStackTrace();
		    
		    System.exit(0);
		}

		System.out.println("Got: "+com.arjuna.ats.jts.utils.Utility.stringStatus(s));

		if (s == Status.StatusActive)
		    passed = true;
	    }
	    
	    System.out.println("committing top-level transaction.");

	    if (shouldCommit)
		current.commit(true);
	    else
		current.rollback();

	    if (rc == null)
		System.out.println("No recovery coordinator reference.");
	    else
	    {
		Status s = Status.StatusUnknown;
		
		try
		{
		    System.out.println("Attempting to use recovery coordinator.");
		
		    s = rc.replay_completion(atomicObject);
		}
		catch (NotPrepared e)
		{
		    s = Status.StatusActive;
		}
		catch (Exception ex)
		{
		    System.err.println("Caught: "+ex);
		    
		    System.exit(0);
		}

		System.out.println("Got: "+com.arjuna.ats.jts.utils.Utility.stringStatus(s));

		if (passed && (s == Status.StatusRolledBack))
		    passed = true;
		else
		    passed = false;
	    }
	}
	catch (TRANSACTION_ROLLEDBACK  e1)
	{
	    System.out.println("\nTransaction RolledBack exception");
	}
	catch (HeuristicMixed e2)
	{
	    System.out.println("\nTransaction HeuristicMixed exception");
	}
	catch (HeuristicHazard e3)
	{
	    System.out.println("\nTransaction HeuristicHazard exception");
	}	
	catch (Exception e4)
	{
	    System.out.println("Caught unexpected exception: "+e4);
	}

	System.out.println("Trying to determing final transaction outcome.");

	org.omg.CosTransactions.Status status = Status.StatusUnknown;

	try
	{
	    if (coord != null)
	    {
		status = coord.get_status();

		coord = null;
	    }
	    else
		System.out.println("\nCould not determine action status.");
	}
	catch (SystemException ex1)
	{
	    // assume invalid reference - tx may have been garbage collected
	}
	catch (Exception e5)
	{
	    System.out.println("Caught unexpected exception:" +e5);
	}

	System.out.println("\nFinal action status: "+com.arjuna.ats.jts.utils.Utility.stringStatus(status));

	if (passed)
	    System.out.println("Passed");
	else
	    System.out.println("Failed");

	myOA.destroy();
	myORB.shutdown();
    }

}
