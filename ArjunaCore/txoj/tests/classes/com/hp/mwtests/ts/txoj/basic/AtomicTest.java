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
package com.hp.mwtests.ts.txoj.basic;

/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: AtomicTest.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.txoj.common.*;
import com.arjuna.ats.arjuna.common.*;

import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;
import org.jboss.dtf.testframework.unittest.Test;
import org.jboss.dtf.testframework.unittest.LocalHarness;

public class AtomicTest extends Test
{

public void run(String[] args)
    {
	AtomicObject foo = new AtomicObject();
	Uid u = foo.get_uid();
	int v;

	AtomicAction A = new AtomicAction();

	try
	{
	    A.begin();

	    logInformation("current object value is "+foo.get());

	    foo.set(2);

	    logInformation("object value is now "+foo.get());

	    A.commit();

	    int finalVal = foo.get();

	    logInformation("final object value is now "+finalVal);

	    if (finalVal == 2)
		logInformation("This is correct.");
	    else
	    {
		logInformation("This is incorrect.");
		assertFailure();
	    }

	    logInformation("\nStarting new action.");

	    foo = new AtomicObject(u);

	    A = new AtomicAction();

	    A.begin();

	    logInformation("current object value is "+foo.get());

	    foo.set(4);

	    A.commit();

	    finalVal = foo.get();

	    logInformation("final object value is "+finalVal);

	    if (finalVal == 4)
		logInformation("This is correct.");
	    else
            {
		logInformation("This is incorrect.");
                assertFailure();
            }
	}
	catch (TestException e)
	{
	    logInformation("AtomicObject exception raised.");
            e.printStackTrace(System.err);
            assertFailure();

	    A.abort();
	}

        assertSuccess();
    }

public static void main(String[] args)
    {
        AtomicTest test = new AtomicTest();
        test.initialise( null, null, args, new LocalHarness() );
        test.runTest();
    }
};
