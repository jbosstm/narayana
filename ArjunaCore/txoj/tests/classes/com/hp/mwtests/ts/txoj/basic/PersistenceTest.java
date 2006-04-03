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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PersistenceTest.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.txoj.common.*;
import java.io.*;

import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.mwlabs.testframework.unittest.Test;
import com.arjuna.mwlabs.testframework.unittest.LocalHarness;

public class PersistenceTest extends Test
{
    
public void run(String[] args)
    {
	boolean passed = false;

	try
	{
	    ObjectStore store = new ObjectStore(ArjunaNames.Implementation_ObjectStore_ShadowingStore());
	    OutputObjectState state = new OutputObjectState();
	    Uid u = new Uid();

	    logInformation("Uid is "+u);

	    if (store.write_committed(u, "/StateManager/LockManager/foo", state))
	    {
		logInformation("written ok");

		passed = true;
	    }
	    else
		logInformation("write error");

	    if (passed)
	    {
		passed = false;

		/*
		 * Now try to read.
		 */

		InputObjectState inputState = store.read_committed(u, "/StateManager/LockManager/foo");

		if (inputState != null)
		{
		    logInformation("read ok");

		    passed = true;
		}
		else
		    logInformation("read error");
	    }
	}
	catch (ObjectStoreException e)
	{
	    logInformation(e.getMessage());

	    passed = false;
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
        PersistenceTest test = new PersistenceTest();
        test.initialise(null, null, args, new LocalHarness());
        test.runTest();
    }
}
