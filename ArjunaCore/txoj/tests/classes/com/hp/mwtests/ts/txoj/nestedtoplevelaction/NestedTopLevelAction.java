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
package com.hp.mwtests.ts.txoj.nestedtoplevelaction;

/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: NestedTopLevelAction.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.txoj.common.*;

import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;

public class NestedTopLevelAction
{
    
public static void main (String[] args)
    {
	AtomicAction A = new AtomicAction();
	TopLevelAction B = new TopLevelAction();
	AtomicAction C = new AtomicAction();
	AtomicObject foo1 = new AtomicObject();
	AtomicObject foo2 = new AtomicObject();
	boolean passed = false;

	try
	{
	    System.out.println("\nStarting top-level action.\n");
	
	    A.begin();

	    System.out.println(A);

	    foo1.set(5);

	    System.out.println("Current atomic object 1 state: " + foo1.get());

	    System.out.println("\nStarting nested top-level action.");

	    B.begin();

	    System.out.println(B);

	    foo2.set(7);

	    System.out.println("Current atomic object 2 state: " + foo2.get());

	    System.out.println("\nCommitting nested top-level action.");
	
	    B.commit();

	    System.out.println("\nAborting top-level action.");

	    A.abort();

	    C.begin();

	    int val1 = foo1.get();
	    int val2 = foo2.get();
	    
	    System.out.println("\nFinal atomic object 1 state: " + val1);
	    
	    if (val1 == 0)
	    {
		System.out.println("This is correct.");

		passed = true;
	    }
	    else
		System.out.println("This is incorrect.");
	    
	    System.out.println("\nFinal atomic object 2 state: " + val2);

	    if (val2 == 7)
	    {
		System.out.println("This is correct.");

		passed = passed && true;
	    }
	    else
	    {
		System.out.println("This is incorrect.");

		passed = passed && true;
	    }
	    
	    C.commit();
	}
	catch (TestException e)
	{
	    System.out.println("AtomicObject exception raised.");

	    A.abort();
	    B.abort();
	    C.abort();
	}

	if (passed)
	    System.out.println("Test passed");
	else
	    System.out.println("Test failed");
    }
    
};
