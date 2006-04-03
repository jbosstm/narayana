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
package com.hp.mwtests.ts.txoj.basic;

/*
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: BasicActionTest.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.txoj.common.*;
import com.arjuna.ats.arjuna.common.*;

import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;
import com.arjuna.mwlabs.testframework.unittest.Test;
import com.arjuna.mwlabs.testframework.unittest.LocalHarness;

public class BasicActionTest extends Test
{
    
public void run(String[] args)
    {
	AtomicObject foo = null;
	Uid u = null;
	
	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-uid") == 0)
	    {
		u = new Uid(args[i+1]);

		if (!u.valid())
		{
		    System.out.println("Invalid uid.");
		    System.exit(1);
		}
	    }
	    if (args[i].compareTo("-help") == 0)
	    {
		System.out.println("Usage: [-uid <uid>] [-help]");
		assertFailure();
	    }
	}

	if (u == null)
	    foo = new AtomicObject();
	else
	    foo = new AtomicObject(u);

	AtomicAction A = new AtomicAction();

	try
	{
	    A.begin();
	
	    try
	    {
		logInformation("current object value is "+foo.get());
	    }
	    catch (Exception e)
	    {
	    }

	    try
	    {
		logInformation("setting value");
		
		foo.set(foo.get()+2);
	    }
	    catch (Exception e)
	    {
	    }

	    try
	    {
		logInformation("object value is now "+foo.get());
	    }
	    catch (Exception e)
	    {
	    }

	    A.commit();

	    try
	    {
		logInformation("final object value is now "+foo.get());
	    }
	    catch (Exception e)
	    {
	    }
	}
	catch (Exception e)
	{
	    logInformation("AtomicObject exception raised.");
	    A.abort();
	}

	logInformation("\nWill now try some erroneous conditions.\n");

	try
	{
	    AtomicAction B = new AtomicAction();
	    
	    u = new Uid();
	    foo = new AtomicObject(u);
	    
	    B.begin();

	    try
	    {
		logInformation("attempting to get value from non-existent object: "+foo.get());
	    }
	    catch (Exception e)
	    {
	    }
	    
	    logInformation("trying to set value to 5");

	    try
	    {
		foo.set(5);
	    }
	    catch (Exception e)
	    {
	    }

	    try
	    {
		logInformation("attempting to get value again: "+foo.get());
	    }
	    catch (Exception e)
	    {
	    }

	    B.commit();
	}
	catch (Exception e)
	{
            logInformation("Unexpected Exception - "+e);
	    e.printStackTrace(System.err);
            assertFailure();
	}

        assertSuccess();
    }

public static void main(String[] args)
    {
        BasicActionTest test = new BasicActionTest();
        test.initialise( null, null, args, new LocalHarness() );
        test.runTest();
    }
}
