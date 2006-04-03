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
 * $Id: AsyncTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.local.async;

import com.hp.mwtests.ts.jts.resources.*;
import com.hp.mwtests.ts.jts.orbspecific.resources.*;
import com.hp.mwtests.ts.jts.TestModule.*;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.OTSManager;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.mwlabs.testframework.unittest.Test;

import org.omg.CosTransactions.*;

import org.omg.CosTransactions.Unavailable;
import org.omg.CosTransactions.SubtransactionsUnavailable;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.HeuristicRollback;
import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CORBA.INVALID_TRANSACTION;

public class AsyncTest extends Test
{
    
    public void run(String[] args)
    {
	boolean errorp = false;
	boolean errorc = false;
	
	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-help") == 0)
	    {
		logInformation("Usage: AsyncTest [-errorp] [-errorc] [-help]");
		assertFailure();
	    }
	    if (args[i].compareTo("-errorp") == 0)
		errorp = true;
	    if (args[i].compareTo("-errorc") == 0)
		errorc = true;
	}

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

	    Current current = OTSManager.get_current();

	    DemoResource.printThread = true;

	    current.begin();
	    
	    for (int j = 0; j < 100; j++)
	    {
		if ((j == 10) && (errorp || errorc))
		{
		    boolean heuristicPrepare = errorp;
		    heuristic h = new heuristic(heuristicPrepare);

		    current.get_control().get_coordinator().register_resource(h.getReference());

		    h = null;
		}
		    
		DemoResource r = new DemoResource();
    
		r.registerResource();

		r = null;
	    }

	    logInformation("committing top-level transaction");
	    current.commit(false);

	    logInformation("Test completed.");
            assertSuccess();
	}
        catch (org.omg.CORBA.TRANSACTION_ROLLEDBACK e)
	{
	    logInformation("Caught exception: "+e);
	    if ((!errorp)&&(!errorc))
	    {
	    	assertFailure();
	    }
	    else
	    {
		assertSuccess();
	    }
	}
	catch (Exception e)
	{
	    System.err.println("Caught exception: "+e);
            e.printStackTrace(System.err);
            assertFailure();
	}

	myOA.destroy();
	myORB.shutdown();
    }
    
}

