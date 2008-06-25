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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ArjunaNestingTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.local.arjuna;

import com.hp.mwtests.ts.jts.resources.*;
import com.hp.mwtests.ts.jts.orbspecific.resources.*;
import com.hp.mwtests.ts.jts.TestModule.*;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.extensions.*;
import com.arjuna.ats.jts.OTSManager;

import com.arjuna.ats.internal.jts.ORBManager;
import org.jboss.dtf.testframework.unittest.Test;
import org.jboss.dtf.testframework.unittest.LocalHarness;

import org.omg.CosTransactions.*;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CORBA.INVALID_TRANSACTION;

public class ArjunaNestingTest extends Test
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

	boolean doAbort = false;
	boolean registerSubtran = false;
	org.omg.CosTransactions.Current current = OTSManager.get_current();
	DemoArjunaResource sr = new DemoArjunaResource();

	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-help") == 0)
	    {
		logInformation("Usage: ArjunaNestingTest [-abort] [-subtran] [-help]");
		assertFailure();
	    }
	    if (args[i].compareTo("-abort") == 0)
		doAbort = true;
	    if (args[i].compareTo("-subtran") == 0)
		registerSubtran = true;
	}

	try
	{
	    current.begin();
	    current.begin();
	    current.begin();
	}
	catch (SystemException sysEx)
	{
	    System.err.println("Unexpected system exception:" +sysEx);
            sysEx.printStackTrace(System.err);
	    assertFailure();
	}
	catch (UserException se)
	{
	    System.err.println("Unexpected user exception:" +se);
            se.printStackTrace(System.err);
	    assertFailure();
	}

	try
	{
	    sr.registerResource(registerSubtran);
	}
	catch (SystemException ex1)
	{
	    System.err.println("Unexpected system exception: "+ex1);
            ex1.printStackTrace(System.err);
	    assertFailure();
	}
	catch (Exception e)
	{
	    System.err.println("call to registerSubtran failed: "+e);
            e.printStackTrace(System.err);
	    assertFailure();
	}

	try
	{
	    logInformation("committing first nested transaction");
	    current.commit(true);

	    logInformation("committing second nested transaction");
	    current.commit(true);

	    if (!doAbort)
	    {
		logInformation("committing top-level transaction");
		current.commit(true);
	    }
	    else
	    {
		logInformation("aborting top-level transaction");
		current.rollback();
	    }
	}
	catch (Exception ex)
	{
	    System.err.println("Caught unexpected exception: "+ex);
            ex.printStackTrace(System.err);
	    assertFailure();
	}

	logInformation("Test completed successfully.");
        assertSuccess();

	myOA.shutdownObject(sr);

	myOA.destroy();
	myORB.shutdown();
    }

	public static void main(String[] args)
	{
		ArjunaNestingTest test = new ArjunaNestingTest();
		test.initialise(null, null, args, new LocalHarness());
		test.runTest();
	}
}

