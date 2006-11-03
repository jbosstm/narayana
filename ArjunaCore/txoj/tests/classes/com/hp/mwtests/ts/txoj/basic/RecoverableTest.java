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
 * $Id: RecoverableTest.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.txoj.common.*;

import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.RecoverableObject;
import com.arjuna.mwlabs.testframework.unittest.Test;
import com.arjuna.mwlabs.testframework.unittest.LocalHarness;

public class RecoverableTest extends Test
{
    
public void run(String[] args)
    {
	boolean passed = false;
	RecoverableObject foo = new RecoverableObject();
	int value = 0;

	AtomicAction A = new AtomicAction();

	A.begin();
	
	logInformation("value is "+foo.get());

	foo.set(2);

	logInformation("value is "+foo.get());

	A.abort();

	value = foo.get();

	logInformation("value is now "+value);

	if (value == 0)
	{
	    AtomicAction B = new AtomicAction();
	
	    B.begin();
	
	    logInformation("value is "+foo.get());

	    foo.set(4);

	    logInformation("value is "+foo.get());

	    B.commit();

	    value = foo.get();

	    logInformation("value is now "+value);

	    if (value == 4)
		passed = true;
	}

	if (passed)
        {
	    logInformation("Test passed");
            assertSuccess();
        }
	else
        {
	    logInformation("Test failed");
            assertFailure();
        }
    }

public static void main(String[] args)
    {
        RecoverableTest test = new RecoverableTest();
        test.initialise(null, null, args, new LocalHarness());
        test.runTest();
    }
}

