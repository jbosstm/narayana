/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: DistributedHammerWorker3.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.orbspecific.resources;

import com.hp.mwtests.ts.jts.TestModule.*;
import com.hp.mwtests.ts.jts.utils.Util;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;

import com.arjuna.orbportability.*;

import org.omg.CosTransactions.*;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;

import org.omg.CORBA.IntHolder;

public class DistributedHammerWorker3
{
    
public static void incr12 (int thr, int level)
    {
	boolean res  = false;
	boolean res1 = false;
	boolean res2 = false;
    
	int ran = 0;

	try
	{
	    OTSImpleManager.current().begin();
	    Control control = OTSImpleManager.current().get_control();
    
	    Util.indent(thr, level);
	    System.out.println("begin   incr12");

	    ran = Util.rand.nextInt() % 16;

	    res1 = hammerObject_1.incr(ran, control);
	    res  = res1;

	    Util.indent(thr, level);
	    System.out.println("part1   incr12 : "+res1);

	    Util.lowProbYield();

	    if (res)
	    {
		res2 = hammerObject_2.incr(-ran, control);
		res  = res2;

		Util.indent(thr, level);
		System.out.println("part2   incr12 : "+res2);
	    }

	    Util.lowProbYield();

	    control = null;
    
	    Util.indent(thr, level);
	    if (res)
	    {
		System.out.print("end ");
		OTSImpleManager.current().commit(true);
	    }
	    else
	    {
		System.out.print("abort  ");
		OTSImpleManager.current().rollback();
	    }
	}
	catch (Exception e)
	{
	    System.err.println("DistributedHammerWorker3.incr12: "+e);
	    res1 = res2 = res = false;
	}

	Util.indent(thr, level);

	System.out.println(" incr12 : "+res1+" : "+res2+" : "+res
			   +" : "+ran);
    }

public static void incr21 (int thr, int level)
    {
	boolean res  = false;
	boolean res1 = false;
	boolean res2 = false;
    
	int ran = 0;

	try
	{
	    OTSImpleManager.current().begin();
	    Control control = OTSImpleManager.current().get_control();
    
	    Util.indent(thr, level);
	    System.out.println("begin   incr21");

	    ran = Util.rand.nextInt() % 16;

	    res1 = hammerObject_2.incr(ran, control);
	    res  = res1;
    
	    Util.indent(thr, level);
	    System.out.println("part1   incr21 : "+res1);

	    Util.lowProbYield();

	    if (res)
	    {
		res2 = hammerObject_1.incr(-ran, control);
		res  = res2;
	
		Util.indent(thr, level);
		System.out.println("part2   incr21 : "+res2);
	    }

	    Util.lowProbYield();

	    control = null;
	    
	    Util.indent(thr, level);
	    if (res)
	    {
		System.out.print("end ");
		OTSImpleManager.current().commit(true);
	    }
	    else
	    {
		System.out.print("abort  ");
		OTSImpleManager.current().rollback();
	    }
	}
	catch (Exception e)
	{
	    System.err.println("DistributedHammerWorker3.incr21: "+e);
	    res1 = res2 = res = false;
	}

	Util.indent(thr, level);
	
	System.out.println(" incr21 : "+res1+" : "+res2+" : "+res
			   +" : "+ran);
    }

public static void get12 (int thr, int level)
    {
	boolean res  = false;
	boolean res1 = false;
	boolean res2 = false;
    
	IntHolder value1 = new IntHolder(0);
	IntHolder value2 = new IntHolder(0);

	try
	{
	    OTSImpleManager.current().begin();
	    Control control = OTSImpleManager.current().get_control();
    
	    Util.indent(thr, level);
	    System.out.println("begin   get12");

	    res1 = hammerObject_1.get(value1, control);
	    res  = res1;
    
	    Util.indent(thr, level);
	    System.out.println("part1   get12  : "+res1);

	    Util.lowProbYield();

	    if (res)
	    {
		res2 = hammerObject_2.get(value2, control);
		res  = res2;
	
		Util.indent(thr, level);
		System.out.println("part2   get12  : "+res2);
	    }

	    Util.lowProbYield();

	    control = null;
	    
	    Util.indent(thr, level);
	    if (res)
	    {
		System.out.print("end ");
		OTSImpleManager.current().commit(true);
	    }
	    else
	    {
		System.out.print("abort  ");
		OTSImpleManager.current().rollback();
	    }
	}
	catch (Exception e)
	{
	    System.err.println("DistributedHammerWorker3.get12: "+e);
	    res1 = res2 = res = false;
	}

	Util.indent(thr, level);
	
	System.out.println(" get12  : "+res1+" : "+res2+" : "+res
			   +" : "+value1.value+" : "+value2.value);
    }

public static void get21 (int thr, int level)
    {
	boolean res  = false;
	boolean res1 = false;
	boolean res2 = false;
    
	IntHolder value1 = new IntHolder(0);
	IntHolder value2 = new IntHolder(0);

	try
	{
	    OTSImpleManager.current().begin();
	    Control control = OTSImpleManager.current().get_control();
    
	    Util.indent(thr, level);
	    System.out.println("begin   get21");

	    res1 = hammerObject_2.get(value1, control);
	    res  = res1;
    
	    Util.indent(thr, level);
	    System.out.println("part1   get21  : "+res1);

	    Util.lowProbYield();

	    if (res)
	    {
		res2 = hammerObject_1.get(value2, control);
		res  = res2;
    
		Util.indent(thr, level);
		System.out.println("part2   get21  : "+res2);
	    }

	    Util.lowProbYield();

	    control = null;
	
	    Util.indent(thr, level);
	    if (res)
	    {
		System.out.print("end ");
		OTSImpleManager.current().commit(true);
	    }
	    else
	    {
		System.out.print("abort  ");
		OTSImpleManager.current().rollback();
	    }
	}
	catch (Exception e)
	{
	    System.err.println("DistributedHammerWorker3.get21: "+e);
	    res1 = res2 = res = false;
	}

	Util.indent(thr, level);
	
	System.out.println(" get21  : "+res1+" : "+res2+" : "+res
			   +" : "+value1.value+" : "+value2.value);
    }

public static void randomOperation(int thr, int level)
    {
	switch (Util.rand.nextInt() % 23)
	{
        case 0:
        case 1:
        case 2:
        case 3:
             incr12(thr, level);
            break;
        case 4:
        case 5:
        case 6:
        case 7:
           incr21(thr, level);
            break;
        case 8:
        case 9:
        case 10:
        case 11:
            get12(thr, level);
            break;
        case 12:
        case 13:
        case 14:
        case 15:
            get21(thr, level);
            break;
        case 16:
        case 17:
            {
		try
		{
		    OTSImpleManager.current().begin();

		    Util.indent(thr, level);
		    System.out.println("begin");

		    randomOperation(thr, level + 1);
		    randomOperation(thr, level + 1);

		    OTSImpleManager.current().commit(true);

		    Util.indent(thr, level);
		    System.out.println("end");
		}
		catch (Exception e)
		{
		    System.err.println("DistributedHammerWorker3.randomOperation: "+e);
		}
	    }
            break;
        case 18:
        case 19:
            {
		try
		{
		    OTSImpleManager.current().begin();

		    Util.indent(thr, level);
		    System.out.println("begin");

		    randomOperation(thr, level + 1);
		    randomOperation(thr, level + 1);

		    OTSImpleManager.current().rollback();

		    Util.indent(thr, level);
		    System.out.println("abort");
		}
		catch (Exception e)
		{
		    System.err.println("DistributedHammerWorker3.randomOperation: "+e);
		}		
	    }
            break;
         case 20:
            {
                Thread thr1 = null;
                Thread thr2 = null;

                Util.indent(thr, level);
                System.out.println("fork");

		thr1 = new DHThreadObject3a(false);
		thr2 = new DHThreadObject3a(false);

		thr1.start();
		thr2.start();
		
		try
		{
		    thr1.join();
		    thr2.join();
		}
		catch (InterruptedException e)
		{
		    System.err.println("join exception: "+e);
		}

                Util.indent(thr, level);
                System.out.println("join");
	    }
            break;
         case 21:
            {
                Thread thr1 = null;
                Thread thr2 = null;

                Util.indent(thr, level);
                System.out.println("fork");

		thr1 = new DHThreadObject3a(true);
		thr2 = new DHThreadObject3a(false);

		thr1.start();
		thr2.start();

		try
		{
		    thr1.join();
		    thr2.join();
		}
		catch (InterruptedException e)
		{
		    System.err.println("join exception: "+e);
		}

                Util.indent(thr, level);
                System.out.println("join");
	    }
            break;
         case 22:
            {
                Thread thr1 = null;
                Thread thr2 = null;

                Util.indent(thr, level);
                System.out.println("fork");

		thr1 = new DHThreadObject3a(true);
		thr2 = new DHThreadObject3a(true);

		thr1.start();
		thr2.start();

		try
		{
		    thr1.join();
		    thr2.join();
		}
		catch (InterruptedException e)
		{
		    System.err.println("join exception: "+e);
		}

                Util.indent(thr, level);
                System.out.println("join");
	    }
	 break;
	}
    }

	public static boolean get1(IntHolder value)
	{
		boolean res  = false;

		try
		{
			OTSImpleManager.current().begin();
			Control control = OTSImpleManager.current().get_control();

			res = hammerObject_1.get(value, control);
			control = null;

			if (res)
			{
				OTSImpleManager.current().commit(true);
			}
			else
			{
				OTSImpleManager.current().rollback();
			}
		}
		catch (Exception e)
		{
			System.err.println("DistributedHammerWorker1.get1: "+e);
			res = false;
		}

		return(res);
	}

	public static boolean get2(IntHolder value)
	{
		boolean res  = false;

		try
		{
			OTSImpleManager.current().begin();
			Control control = OTSImpleManager.current().get_control();

			res = hammerObject_2.get(value, control);
			control = null;

			if (res)
			{
				OTSImpleManager.current().commit(true);
			}
			else
			{
				OTSImpleManager.current().rollback();
			}
		}
		catch (Exception e)
		{
			System.err.println("DistributedHammerWorker1.get2: "+e);
			res = false;
		}

		return(res);
	}


public static Hammer hammerObject_1 = null;
public static Hammer hammerObject_2 = null;

};

