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
package com.hp.mwtests.ts.txoj.performance;

/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PerformanceTest2.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.txoj.common.*;

import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;
import com.hp.mwtests.ts.txoj.common.resources.RecoverableObject;

import java.lang.NumberFormatException;

public class PerformanceTest2
{
    
public static void main (String[] args)
    {
	boolean persistent = true;
	long iters = 1000;
	
	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-recoverable") == 0)
		persistent = false;

	    if (args[i].compareTo("-iter") == 0)
	    {
		try
		{
		    iters = Long.parseLong(args[i+1]);
		}
		catch (NumberFormatException e)
		{
		}
	    }
	}

	long totalTime = 0;

	AtomicAction A = new AtomicAction();
	A.begin();
	A.commit();
	A = null;

	if (persistent)
	    totalTime = persistentTest(iters);
	else
	    totalTime = recoverableTest(iters);

	System.out.print("Time taken to perform "+iters+" iterations on a ");

	if (persistent)
	    System.out.print("persistent object: ");
	else
	    System.out.print("recoverable object: ");

	System.out.println(totalTime+" milliseconds");
    }

public static long recoverableTest (long iters)
    {
	RecoverableObject foo = new RecoverableObject();
	AtomicAction A = null;
	long t1 = System.currentTimeMillis();

	for (int c = 0; c < iters; c++)
	{
	    A = new AtomicAction();
	    
	    A.begin();
	    
	    foo.set(2);

	    A.commit();

	    A = null;
	}

	foo = null;
	
	return System.currentTimeMillis() - t1;
    }

public static long persistentTest (long iters)
    {
	AtomicObject foo = new AtomicObject();
	AtomicAction A = null;
	long t1 = System.currentTimeMillis();

	try
	{
	    for (int c = 0; c < iters; c++)
	    {
		A = new AtomicAction();
		
		A.begin();
		
		foo.set(2);

		A.commit();

		A = null;
	    }
	}
	catch (TestException e)
	{
	    System.out.println("AtomicObject exception raised.");

	    if (A != null)
		A.abort();
	}

	foo = null;
	
	return System.currentTimeMillis() - t1;
    }    
    
};
