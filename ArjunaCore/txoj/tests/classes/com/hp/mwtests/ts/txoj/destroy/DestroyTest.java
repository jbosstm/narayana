/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
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
package com.hp.mwtests.ts.txoj.destroy;

/*
 * Copyright (C) 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DestroyTest.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.txoj.common.*;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;

import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;
import com.arjuna.mwlabs.testframework.unittest.Test;
import com.arjuna.mwlabs.testframework.unittest.LocalHarness;

public class DestroyTest extends Test
{

public void run(String[] args)
    {
	AtomicObject atomicObject = new AtomicObject();
	Uid u = atomicObject.get_uid();
	AtomicAction a = new AtomicAction();
	
	a.begin();
	
	try
	{
	    atomicObject.set(10);
	}
	catch (TestException e)
	{
	    logInformation("set : failed");
            logInformation("Unexpected exception - "+e);
            e.printStackTrace(System.err);
	    assertFailure();
	}
	
	logInformation("set : ok");

	if (!atomicObject.destroy())
	{
	    logInformation("destroy : failed");

	    a.abort();
	    
	    assertFailure();
	}

	a.commit();
	
	atomicObject = new AtomicObject(u);
	
	boolean passed = false;

	try
	{
	    int val = atomicObject.get();
	    
	    if (val != -1)
	    {
		logInformation("got : "+val);

		logInformation("destroy did not work!");
	    }
	    else
	    {
		System.out.println("object destroyed!");

		passed = true;
	    }
	}
	catch (TestException e)
	{
	    System.out.println("object destroyed!");

	    passed = true;
	}

	if (passed)
	    System.out.println("Test passed");
	else
        {
	    System.out.println("Test failed");

            assertFailure();
        }

        assertSuccess();
    }

public static void main(String[] args)
    {
        DestroyTest test = new DestroyTest();
        test.initialise(null, null, args, new LocalHarness());
        test.runTest();
    }
}
