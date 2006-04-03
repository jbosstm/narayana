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
 * $Id: PerformanceTest1.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.txoj.common.*;

import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;
import com.hp.mwtests.ts.txoj.common.resources.RecoverableObject;
import com.arjuna.mwlabs.testframework.unittest.Test;
import com.arjuna.mwlabs.testframework.utils.PerformanceLogger;

import java.lang.NumberFormatException;

public class PerformanceTest1 extends Test
{
    
    public void run(String[] args)
    {
	boolean persistent = true;
	long startIters = 1000;
        long endIters = 2000;
        long incrementIters = 250;

	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-recoverable") == 0)
		persistent = false;

	    if (args[i].compareTo("-start") == 0)
	    {
		try
		{
		    startIters = Long.parseLong(args[i+1]);
		}
		catch (NumberFormatException e)
		{
		}
	    }

            if (args[i].compareTo("-end") == 0)
	    {
		try
		{
		    endIters = Long.parseLong(args[i+1]);
		}
		catch (NumberFormatException e)
		{
		}
	    }

            if (args[i].compareTo("-inc") == 0)
	    {
		try
		{
		    incrementIters = Long.parseLong(args[i+1]);
		}
		catch (NumberFormatException e)
		{
		}
	    }
	}

        if ( ( ( ( startIters - endIters) % incrementIters ) != 0 ) || ( endIters < startIters ) )
        {
            System.out.println("Invalid iteration parameters");
        }
        else
        {
            long thisTime = 0;
            PerformanceLogger logger = new PerformanceLogger("PerformanceTest1");

            logInformation("Performing "+startIters+" - "+endIters+" (step "+incrementIters+") iterations");

            for (long iters=startIters;iters<=endIters;iters += incrementIters)
            {
                if (persistent)
                    thisTime = persistentTest(iters);
                else
                    thisTime = recoverableTest(iters);

                logInformation("Number of Iterations:"+ iters +", Time Taken:" +thisTime);

                logger.addData( iters, (double)((double)iters/((double)thisTime/1000)) );
            }

            try
            {
                logger.output(System.out);
                assertSuccess();
            }
            catch (Exception e)
            {
                System.err.println("Unexpected exception - "+e);
                assertFailure();
            }
        }
    }

public static long recoverableTest (long iters)
    {
	RecoverableObject foo = new RecoverableObject();
	AtomicAction A = new AtomicAction();
	long t1 = System.currentTimeMillis();

	A.begin();
	
	for (int c = 0; c < iters; c++)
	{
	    foo.set(2);
	}

	A.commit();	

	return System.currentTimeMillis() - t1;
    }

public static long persistentTest (long iters)
    {
	AtomicObject foo = new AtomicObject();
	AtomicAction A = new AtomicAction();
	long t1 = System.currentTimeMillis();

	A.begin();
	
	try
	{
	    for (int c = 0; c < iters; c++)
	    {
		foo.set(2);
	    }
	}
	catch (TestException e)
	{
	    System.out.println("AtomicObject exception raised.");
	}

	A.commit();
	
	return System.currentTimeMillis() - t1;
    }    
    
};
