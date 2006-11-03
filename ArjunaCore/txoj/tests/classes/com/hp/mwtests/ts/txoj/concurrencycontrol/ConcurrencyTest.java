/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
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
package com.hp.mwtests.ts.txoj.concurrencycontrol;

/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ConcurrencyTest.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.txoj.common.*;
import com.arjuna.ats.arjuna.common.*;

import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;
import com.arjuna.mwlabs.testframework.unittest.Test;
import com.arjuna.mwlabs.testframework.unittest.LocalHarness;

public class ConcurrencyTest extends Test
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
		    logInformation("Invalid uid.");
		    assertFailure();
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

	logInformation("Starting top-level action.");

	AtomicAction A = new AtomicAction();

	try
	{
	    A.begin();

	    logInformation("Current atomic object state: " + foo.get());
	
	    foo.set(7);

	    if (u == null)
	    {
		logInformation("Now waiting for 20 seconds.");

		try
		{
		    Thread.sleep(20000);
		}
		catch (InterruptedException e)
		{
		}
	    }

	    logInformation("\nCommitting top-level action.");

	    if (A.commit() != ActionStatus.COMMITTED)
            {
		logInformation("Error when committing action.");
                assertFailure();
            }
	    else
            {
		logInformation("Action committed.");
            }
	}
	catch (TestException e)
	{
	    logInformation("Could not set state. Aborting action.");
            assertFailure();
	    A.abort();
	}

        assertSuccess();
    }

public static void main(String[] args)
    {
        ConcurrencyTest test = new ConcurrencyTest();
        test.initialise( null, null, args, new LocalHarness() );
        test.runTest();
    }
};
