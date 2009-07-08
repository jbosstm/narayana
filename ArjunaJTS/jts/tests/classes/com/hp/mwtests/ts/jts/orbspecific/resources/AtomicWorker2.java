/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: AtomicWorker2.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.orbspecific.resources;

import com.arjuna.ats.internal.jts.OTSImpleManager;

import com.hp.mwtests.ts.jts.utils.Util;
import com.hp.mwtests.ts.jts.exceptions.TestException;

import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;

public class AtomicWorker2
{

public static void randomOperation (char thr, int level)
    {
	switch (Util.rand.nextInt() % 6)
	{
        case 0:
            incr12(thr, level);
            break;
        case 1:
            incr21(thr, level);
            break;
        case 2:
            get12(thr, level);
            break;
        case 3:
            get21(thr, level);
            break;
        case 4:
            {
		try
		{
		    current.begin();

		    Util.indent(thr, level);
		    System.out.println("begin");

		    randomOperation(thr, level + 1);
		    randomOperation(thr, level + 1);

		    current.commit(false);
		    
		    Util.indent(thr, level);
		    System.out.println("end");
		}
		catch (Exception e)
		{
		    e.printStackTrace();
		    
		    System.err.println(e);
		}
	    }
	break;
        case 5:
            {
		try
		{
		    current.begin();

		    Util.indent(thr, level);
		    System.out.println("begin");

		    randomOperation(thr, level + 1);
		    randomOperation(thr, level + 1);

		    current.rollback();

		    Util.indent(thr, level);
		    System.out.println("abort");
		}
		catch (Exception e)
		{
		    e.printStackTrace();

		    System.err.println(e);
		}
	    }
	break;
	}
    }
    
public static void incr12 (char thr, int level)
    {
	boolean res  = false;
	boolean res1 = false;
	boolean res2 = false;

	int ran;

	try
	{
	    current.begin();

	    Util.indent(thr, level);
	    System.out.println("begin   incr12");

	    ran = Util.rand.nextInt() % 16;

	    res1 = atomicObject_1.incr(ran);
	    res  = res1;

	    Util.indent(thr, level);
	    System.out.println("part1   incr12 : "+res1);

	    Util.lowProbYield();
	
	    if (res)
	    {
		res2 = atomicObject_2.incr(-ran);
		res  = res2;

		Util.indent(thr, level);
		System.out.println("part2   incr12 : "+res2);
	    }

	    Util.lowProbYield();

	    Util.indent(thr, level);
	    if (res)
	    {
		System.out.print("end ");
		current.commit(false);
	    }
	    else
	    {
		System.out.print("abort  ");
		current.rollback();
	    }

	    System.out.println(" incr12 : "+res1+" : "+res2+" : "+res
			       +" : "+ran);
	}
	catch (Exception e)
	{
	    e.printStackTrace();

	    System.err.println(e);
	}
    }

public static void incr21 (char thr, int level)
    {
	boolean res  = false;
	boolean res1 = false;
	boolean res2 = false;

	int ran;

	try
	{
	    current.begin();

	    Util.indent(thr, level);
	    System.out.println("begin   incr21");

	    ran = Util.rand.nextInt() % 16;

	    res1 = atomicObject_2.incr(ran);
	    res  = res1;

	    Util.indent(thr, level);
	    System.out.println("part1   incr21 : "+res1);

	    Util.lowProbYield();
	    
	    if (res)
	    {
		res2 = atomicObject_1.incr(-ran);
		res  = res2;

		Util.indent(thr, level);
		System.out.println("part2   incr21 : "+res2);
	    }

	    Util.lowProbYield();

	    Util.indent(thr, level);
	    if (res)
	    {
		System.out.print("end ");
		current.commit(false);
	    }
	    else
	    {
		System.out.print("abort  ");
		current.rollback();
	    }
	     
	    System.out.println(" incr21 : "+res1+" : "+res2+" : "+res
			       +" : "+ran);
	}
	catch (Exception e)
	{
	    e.printStackTrace();

	    System.err.println(e);
	}
    }

public static void get12 (char thr, int level)
    {
	boolean res  = false;
	boolean res1 = false;
	boolean res2 = false;

	int value1 = 0;
	int value2 = 0;

	try
	{
	    current.begin();

	    Util.indent(thr, level);
	    System.out.println("begin   get12");

	    res1 = true;

	    try
	    {
		value1 = atomicObject_1.get();
	    }
	    catch (TestException e)
	    {
		res1 = false;
	    }
	    
	    res  = res1;

	    Util.indent(thr, level);
	    System.out.println("part1   get12  : "+res1);

	    Util.lowProbYield();

	    if (res)
	    {
		res2 = true;

		try
		{
		    value2 = atomicObject_2.get();
		}
		catch (TestException e)
		{
		    res2 = false;
		}
		
		res  = res2;

		Util.indent(thr, level);
		System.out.println("part2   get12  : "+res2);
	    }

	    Util.lowProbYield();

	    Util.indent(thr, level);
	    if (res)
	    {
		System.out.print("end ");
		current.commit(false);
	    }
	    else
	    {
		System.out.print("abort  ");
		current.rollback();
	    }

	    System.out.println(" get12  : "+res1+" : "+res2+" : "+res
			       + " : " + value1 + " : " + value2);
	}
	catch (Exception e)
	{
	    e.printStackTrace();

	    System.err.println(e);
	}
    }

public static void get21 (char thr, int level)
    {
	boolean res  = false;
	boolean res1 = false;
	boolean res2 = false;

	int value1 = 0;
	int value2 = 0;

	try
	{
	    current.begin();

	    Util.indent(thr, level);
	    System.out.println("begin   get21");

	    res1 = true;

	    try
	    {
		value1 = atomicObject_2.get();
	    }
	    catch (TestException e)
	    {
		res1 = false;
	    }
	    
	    res  = res1;

	    Util.indent(thr, level);
	    System.out.println("part1   get21  : " + res1);
	
	    Util.lowProbYield();

	    if (res)
	    {
		res2 = true;

		try
		{
		    value2 = atomicObject_1.get();
		}
		catch (TestException e)
		{
		    res2 = false;
		}
		
		res  = res2;

		Util.indent(thr, level);
		System.out.println("part2   get21  : " + res2);
	    }
	
	    Util.lowProbYield();

	    Util.indent(thr, level);
	    if (res)
	    {
		System.out.print("end ");
		current.commit(false);
	    }
	    else
	    {
		System.out.print("abort  ");
		current.rollback();
	    }

	    System.out.println(" get21  : " + res1 + " : " + res2 + " : " + res
			       + " : " + value1 + " : " + value2);
	}
	catch (Exception e)
	{
	    e.printStackTrace();

	    System.err.println(e);
	}
    }

    public static int get1() throws Exception
    {
        boolean res = false;
        int returnValue = -1;

        try
        {
            current.begin();

            try
            {
                returnValue = atomicObject_1.get();
                res = true;
            }
            catch (TestException e)
            {
            }

            if (res)
                current.commit(false);
            else
                current.rollback();
        }
        catch (Exception e)
        {
            System.err.println(e);
            throw e;
        }

        if (!res)
            throw new Exception("Get1: Failed to retrieve value");

        return(returnValue);
    }

    public static int get2() throws Exception
    {
        boolean res = false;
        int returnValue = -1;

        try
        {
            current.begin();

            try
            {
                returnValue = atomicObject_2.get();
                res = true;
            }
            catch (TestException e)
            {
            }

            if (res)
                current.commit(false);
            else
                current.rollback();
        }
        catch (Exception e)
        {
            System.err.println(e);
            throw e;
        }

        if (!res)
            throw new Exception("Get2: Failed to retrieve value");

        return(returnValue);
    }

    public static void init ()
    {
	AtomicWorker2.current = OTSImpleManager.current();
    }

    public static AtomicObject atomicObject_1 = null;
    public static AtomicObject atomicObject_2 = null;
    public static CurrentImple current = null;
    
}

