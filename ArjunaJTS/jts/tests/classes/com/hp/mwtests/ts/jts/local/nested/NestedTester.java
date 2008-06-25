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
 * $Id: NestedTester.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.local.nested;

import com.hp.mwtests.ts.jts.resources.*;
import com.hp.mwtests.ts.jts.orbspecific.resources.*;
import com.hp.mwtests.ts.jts.TestModule.*;
import com.hp.mwtests.ts.jts.utils.ResourceTrace;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.extensions.*;

import com.arjuna.ats.jts.OTSManager;

import com.arjuna.ats.internal.jts.ORBManager;
import org.jboss.dtf.testframework.unittest.Test;

import org.omg.CosTransactions.*;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CosTransactions.Unavailable;
import org.omg.CosTransactions.SubtransactionsUnavailable;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.HeuristicRollback;
import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicHazard;

public class NestedTester extends Test
{

    public void run(String[] args)
    {
	boolean registerSubtran = false;
	boolean doAbort = false;

	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-help") == 0)
	    {
		System.out.println("Usage: NestedTester [-subtran] [-abort] [-help]");
		assertFailure();
	    }
	    if (args[i].compareTo("-subtran") == 0)
		registerSubtran = true;
	    if (args[i].compareTo("-abort") == 0)
		doAbort = true;
	}

	DemoResource r = null;
	DemoSubTranResource sr = null;
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

	    org.omg.CosTransactions.Current current = OTSManager.get_current();

	    r = new DemoResource();
	    sr = new DemoSubTranResource();

	    current.begin();
	    current.begin();
	    current.begin();

	    sr.registerResource(registerSubtran);
	    r.registerResource();

	    System.out.println("committing first nested transaction");
	    current.commit(true);

	    System.out.println("committing second nested transaction");
	    current.commit(true);

	    if (!doAbort)
	    {
		System.out.println("committing top-level transaction");
		current.commit(true);
	    }
	    else
	    {
		System.out.println("aborting top-level transaction");
		current.rollback();
	    }

	    System.out.println("Test completed successfully.");

            if ( (!doAbort) && (!registerSubtran) &&
	         (sr.getNumberOfSubtransactionsRolledBack() == 0) &&
	         (sr.getNumberOfSubtransactionsCommitted() == 1) &&
	         (sr.getResourceTrace().getTrace() == ResourceTrace.ResourceTracePrepareCommit) &&
	         (r.getResourceTrace().getTrace() == ResourceTrace.ResourceTracePrepareCommit) )
	    {
	    	assertSuccess();
	    }
	    else
	    {
	    	if ( (doAbort) && (!registerSubtran) &&
	             (sr.getNumberOfSubtransactionsRolledBack()==0) &&
	             (sr.getNumberOfSubtransactionsCommitted()==1) &&
	             (sr.getResourceTrace().getTrace() == ResourceTrace.ResourceTraceRollback) &&
	             (r.getResourceTrace().getTrace() == ResourceTrace.ResourceTraceRollback) )
		{
		    assertSuccess();
		}
		else
		{
	    	    if ( (!doAbort) && (registerSubtran) &&
	                 (sr.getNumberOfSubtransactionsRolledBack()==0) &&
	                 (sr.getNumberOfSubtransactionsCommitted()==1) &&
	                 (sr.getResourceTrace().getTrace() == ResourceTrace.ResourceTraceNone) &&
	                 (r.getResourceTrace().getTrace() == ResourceTrace.ResourceTraceCommitOnePhase) )
	            {
	            	assertSuccess();
	            }
	            else
	            {
			if ( (doAbort) && (registerSubtran) &&
	                     (sr.getNumberOfSubtransactionsRolledBack()==0) &&
	                     (sr.getNumberOfSubtransactionsCommitted()==1) &&
	                     (sr.getResourceTrace().getTrace() == ResourceTrace.ResourceTraceNone) &&
	                     (r.getResourceTrace().getTrace() == ResourceTrace.ResourceTraceRollback) )
			{
			    assertSuccess();
			}
			else
			    assertFailure();
		    }
		}
            }
	}
	catch (UserException e)
	{
	    System.err.println("Caught UserException: "+e);
	    e.printStackTrace(System.err);
            assertFailure();
	}
	catch (SystemException e)
	{
	    System.err.println("Caught SystemException: "+e);
	    e.printStackTrace(System.err);
            assertFailure();
	}

	myOA.shutdownObject(r);
	myOA.shutdownObject(sr);

	myOA.destroy();
	myORB.shutdown();
    }

}

