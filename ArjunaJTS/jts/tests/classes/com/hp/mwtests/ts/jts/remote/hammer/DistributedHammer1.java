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
 * $Id: DistributedHammer1.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.hammer;

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
import com.arjuna.mwlabs.testframework.unittest.LocalHarness;

import org.omg.CosTransactions.*;

import org.omg.CORBA.IntHolder;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CORBA.INVALID_TRANSACTION;

public class DistributedHammer1 extends Test
{
    private final static int   START_VALUE_1 = 10;
    private final static int   START_VALUE_2 = 101;

    private final static int   EXPECTED_RESULT = START_VALUE_1 + START_VALUE_2;

    public void run (String[] args)
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
	}
	
	String server1 = "/tmp/hammer1.ref";
	String server2 = "/tmp/hammer2.ref";

	if (System.getProperty("os.name").startsWith("Windows"))
	{
	    server1 = "C:\\temp\\hammer1.ref";
	    server2 = "C:\\temp\\hammer2.ref";
	}

	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-reffiles") == 0)
            {
                server1 = args[i+1];
                server2 = args[i+2];
            }
	    if (args[i].compareTo("-help") == 0)
	    {
		System.out.println("Usage: DistributedHammer1 [-reffiles <file1> <file2>] [-help]");
		System.exit(0);
	    }
	}
	
	try
	{
	    Services serv = new Services(myORB);
	    
	    DistributedHammerWorker1.hammerObject_1 = HammerHelper.narrow(myORB.orb().string_to_object(getService(server1)));
	    DistributedHammerWorker1.hammerObject_2 = HammerHelper.narrow(myORB.orb().string_to_object(getService(server2)));

	    if (! DistributedHammerWorker1.hammerObject_1.set(START_VALUE_1, null))
            {
		System.out.println("m set1 : failed");
                assertFailure();
            }
	    if (! DistributedHammerWorker1.hammerObject_2.set(START_VALUE_2, null))
            {
		System.out.println("m set2 : failed");
                assertFailure();
            }

	    DistributedHammerWorker1.get12('m', 0);
	    DistributedHammerWorker1.get21('m', 0);
	}
	catch (Exception e)
	{
	    System.err.println("DistributedHammer1: "+e);
            e.printStackTrace(System.err);

	    assertFailure();
	}
    
	for (int i = 0; i < 100; i++)
	    DistributedHammerWorker1.randomOperation('1', 0);
	
	DistributedHammerWorker1.get12('m', 0);
	DistributedHammerWorker1.get21('m', 0);

        IntHolder value1 = new IntHolder(0);
	IntHolder value2 = new IntHolder(0);

        boolean res = DistributedHammerWorker1.get1(value1) | DistributedHammerWorker1.get2(value2);

        if ( (res) && ( (value1.value + value2.value) == EXPECTED_RESULT ) )
	    assertSuccess();
	else
	    assertFailure();

	myOA.destroy();
	myORB.shutdown();
    }


    public static void main(String[] args)
    {
        DistributedHammer1 client = new DistributedHammer1();
        client.initialise(null, null, args, new LocalHarness());
        client.runTest();
    }
}

