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
package com.hp.mwtests.ts.txoj.performance;

/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PerformanceTest4.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.txoj.common.*;

import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import java.lang.NumberFormatException;

public class PerformanceTest4
{
    
public static void main (String[] args)
    {
	long iters = 1000;

	for (int i = 0; i < args.length; i++)
	{
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

	AtomicAction A = new AtomicAction();
	A.begin();
	A.commit();
	A = null;
	
	long t1 = System.currentTimeMillis();
	
	for (int c = 0; c < iters; c++)
	{
	    A = new AtomicAction();
	
	    A.begin();
	    
	    A.commit();

	    A = null;
	}

	long t2 = System.currentTimeMillis();

	System.out.println(t1+" "+t2);
	
	System.out.print("Time taken to create/commit "+iters+" atomic actions: ");

	System.out.println(t2-t1+" milliseconds");

	AtomicAction B = null;
	t1 = System.currentTimeMillis();

	for (int c = 0; c < iters; c++)
	{
	    A = new AtomicAction();
	    
	    A.begin();

	    B = new AtomicAction();

	    B.begin();

	    B.commit();
	    
	    A.commit();

	    B = null;
	    A = null;
	}

	t2 = System.currentTimeMillis();

	System.out.println(t1+" "+t2);
	
	System.out.print("Time taken to create/commit "+iters+" nested atomic actions: ");

	System.out.println(t2-t1+" milliseconds.");
    }
    
};
