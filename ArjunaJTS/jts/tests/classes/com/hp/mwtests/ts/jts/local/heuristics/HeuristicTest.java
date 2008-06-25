/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: HeuristicTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.local.heuristics;

import com.hp.mwtests.ts.jts.resources.*;
import com.hp.mwtests.ts.jts.orbspecific.resources.*;
import com.hp.mwtests.ts.jts.TestModule.*;
import com.hp.mwtests.ts.jts.utils.ResourceTrace;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.OTSManager;

import com.arjuna.ats.internal.jts.ORBManager;
import org.jboss.dtf.testframework.unittest.Test;

import com.arjuna.ats.jts.utils.Utility;

import org.omg.CosTransactions.*;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

public class HeuristicTest extends Test
{

    public void run(String[] args)
    {
	boolean shouldCommit = true;
	boolean heuristicPrepare = false;

	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-abort") == 0)
		shouldCommit = false;
	    if (args[i].compareTo("-prepare") == 0)
		heuristicPrepare = true;
	    if (args[i].compareTo("-help") == 0)
	    {
		System.out.println("Usage: HeuristicTest [-abort] [-prepare]");
		assertFailure();
	    }
	}

	Coordinator coord = null;

	ORB myORB = null;
	RootOA myOA = null;
        heuristic hImpl = null;

	try
	{
	    myORB = ORB.getInstance("test");
	    myOA = OA.getRootOA(myORB);

	    myORB.initORB(args, null);
	    myOA.initOA();

	    ORBManager.setORB(myORB);
	    ORBManager.setPOA(myOA);

	    Current current = OTSManager.get_current();
	    hImpl = new heuristic(heuristicPrepare);
	    Resource heuristicObject = hImpl.getReference();
	    AtomicResource aImpl = new AtomicResource(shouldCommit);
	    Resource atomicObject = aImpl.getReference();

	    System.out.println("beginning top-level transaction.");

	    current.begin();

	    Control myControl = current.get_control();

	    if (myControl == null)
	    {
		System.err.println("Error - myControl is nil");
		assertFailure();
	    }

	    System.out.println("getting coordinator");

	    coord = myControl.get_coordinator();

	    myControl = null;

	    System.out.println("registering resources.");

	    try
	    {
		coord.register_resource(heuristicObject);
		coord.register_resource(atomicObject);
	    }
	    catch (Exception ex)
	    {
		System.out.println("Failed to register resources: "+ex);
		ex.printStackTrace(System.err);

		assertFailure();
	    }

	    System.out.println("committing top-level transaction.");

	    current.commit(true);
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
            e4.printStackTrace(System.err);
            assertFailure();
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
            {
		System.out.println("\nCould not determine action status.");
                assertFailure();
            }
	}
	catch (SystemException ex1)
	{
	    // assume invalid reference - tx may have been garbage collected
	}
	catch (Exception e5)
	{
	    System.out.println("Caught unexpected exception:" +e5);
            e5.printStackTrace(System.err);
            assertFailure();
	}

	System.out.println("\nFinal action status: "+Utility.stringStatus(status));

	System.out.println("Test completed successfully.");

        ResourceTrace trace = hImpl.getTrace();

	if ( (!heuristicPrepare) && (shouldCommit) && (trace.getTrace() == ResourceTrace.ResourceTracePrepareCommitHeurisiticRollbackForget) )
	{
	    assertSuccess();
	}
	else
	{
	    if ( (!heuristicPrepare) && (!shouldCommit) && (trace.getTrace() == ResourceTrace.ResourceTracePrepareRollback) )
	    {
	    	assertSuccess();
	    }
	    else
	    {
	    	if ( (heuristicPrepare) && (shouldCommit) && (trace.getTrace() == ResourceTrace.ResourceTracePrepareHeuristicHazardForget) )
	    	{
	    	    assertSuccess();
	    	}
	    	else
	    	{
		    assertFailure();
		}
	    }
	}

	myOA.destroy();
	myORB.shutdown();
    }

}

