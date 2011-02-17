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
package com.hp.mwtests.ts.txoj.atomicobject;

/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: AtomicObjectTest3.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.arjuna.coordinator.*;
import java.util.Random;
import java.lang.Math;

import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.InterruptedException;










public class AtomicObjectTest3
{
    public static final int START_VALUE_1 = 10;
    public static final int START_VALUE_2 = 101;
    public static final int EXPECTED_RESULT = START_VALUE_1 + START_VALUE_2;

    @Test
    public void test() throws TestException
    {
	rand = new Random();
	atomicObject1 = new AtomicObject();
	atomicObject2 = new AtomicObject();

	System.out.println(atomicObject1.get_uid());
	System.out.println(atomicObject2.get_uid());

	try
	{
	    atomicObject1.set(START_VALUE_1);
	}
	catch (TestException e)
	{
	    System.out.println("0 set1 : failed");
	}

	try
	{
	    atomicObject2.set(START_VALUE_2);
	}
	catch (TestException e)
	{
	    System.out.println("0 set2 : failed");
	}

	ThreadObject2 thr1 = new ThreadObject2(1);
	ThreadObject2 thr2 = new ThreadObject2(2);

	thr1.start();
	thr2.start();

	try
	{
	    thr1.join();
	    thr2.join();
	}
	catch (InterruptedException e)
	{
	}

	get12(0, 0);
	get21(0, 0);

        assertEquals(EXPECTED_RESULT, (getValue1()+getValue2()));
    }

public static void randomOperation (int thr, int level)
    {
	switch (Math.abs(rand.nextInt()) % 23)
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
                AtomicAction a = new AtomicAction();

                a.begin();

		indent(thr, level);
		System.out.println("begin");

		randomOperation(thr, level + 1);
		randomOperation(thr, level + 1);

                a.commit();

                indent(thr, level);
                System.out.println("commit");
	    }
            break;
        case 18:
        case 19:
            {
                AtomicAction a = new AtomicAction();

                a.begin();

		indent(thr, level);
		System.out.println("begin");

		randomOperation(thr, level + 1);
		randomOperation(thr, level + 1);

                a.abort();

                indent(thr, level);
                System.out.println("abort");
	    }
            break;
         case 20:
            {
		AbortObject thr1 = new AbortObject();
		AbortObject thr2 = new AbortObject();

                indent(thr, level);
                System.out.println("fork");

		thr1.start();
		thr2.start();

		try
		{
		    thr1.join();
		    thr2.join();
		}
		catch (InterruptedException e)
		{
		}

                indent(thr, level);
                System.out.println("join");
	    }
            break;
	case 21:
            {
		CommitObject thr1 = new CommitObject();
		CommitObject thr2 = new CommitObject();

                indent(thr, level);
                System.out.println("fork");

		thr1.start();
		thr2.start();

		try
		{
		    thr1.join();
		    thr2.join();
		}
		catch (InterruptedException e)
		{
		}

                indent(thr, level);
                System.out.println("join");
	    }
            break;
	case 22:
            {
		CommitObject thr1 = new CommitObject();
		AbortObject thr2 = new AbortObject();

                indent(thr, level);
                System.out.println("fork");

		thr1.start();
		thr2.start();

		try
		{
		    thr1.join();
		    thr2.join();
		}
		catch (InterruptedException e)
		{
		}

                indent(thr, level);
                System.out.println("join");
	    }
            break;
	}
    }

public static void lowProbYield()
    {
	while ((Math.abs(rand.nextInt()) % 2) != 0)
	    Thread.yield();
    }

public static void highProbYield()
    {
	while ((Math.abs(rand.nextInt()) % 4) != 0)
	    Thread.yield();
    }

public static void indent (int thr, int level)
    {
	System.out.print(thr+" ");
	for (int i = 0; i < level; i++)
	    System.out.print(" ");
    }

private static void incr12 (int thr, int level)
    {
	boolean res  = true;
	boolean res1 = true;
	boolean res2 = true;

	int ran;

	AtomicAction a = new AtomicAction();

	a.begin();

	indent(thr, level);
	System.out.println("begin   incr12");

	ran = Math.abs(rand.nextInt()) % 16;

	try
	{
	    atomicObject1.incr(ran);
	}
	catch (TestException e)
	{
	    res  = res1 = false;
	}

	indent(thr, level);
	System.out.println("part1   incr12 : "+res1+" "+res);

	lowProbYield();

	if (res)
	{
	    try
	    {
		atomicObject2.incr(-ran);
	    }
	    catch (TestException e)
	    {
		res  = res2 = false;
	    }

	    indent(thr, level);
	    System.out.println("part2   incr12 : "+res2);
	}
	else
	    res2 = false;

	lowProbYield();

	indent(thr, level);
	if (res)
	{
	    System.out.flush();
	    System.out.print("commit ");
	    System.out.flush();
	    res = (boolean) (a.commit() == ActionStatus.COMMITTED);
	}
	else
	{
	    System.out.flush();
	    System.out.print("abort  ");
	    System.out.flush();
	    a.abort();
	}

	System.out.println(" incr12 : "+res1+" : "+res2+" : "+res+" : "+ran);
    }

private static void incr21 (int thr, int level)
    {
	boolean res  = true;
	boolean res1 = true;
	boolean res2 = true;

	int ran;

	AtomicAction a = new AtomicAction();

	a.begin();

	indent(thr, level);
	System.out.println("begin   incr21");

	ran = Math.abs(rand.nextInt()) % 16;

	try
	{
	    atomicObject2.incr(ran);
	}
	catch (TestException e)
	{
	    res  = res1 = false;
	}

	indent(thr, level);
	System.out.println("part1   incr21 : "+res1);

	lowProbYield();

	if (res)
	{
	    try
	    {
		atomicObject1.incr(-ran);
	    }
	    catch (TestException e)
	    {
		res  = res2 = false;
	    }

	    indent(thr, level);
	    System.out.println("part2   incr21 : "+res2);
	}
	else
	    res2 = false;

	lowProbYield();

	indent(thr, level);
	if (res)
	{
	    System.out.flush();
	    System.out.print("commit ");
	    System.out.flush();
	    res = (boolean) (a.commit() == ActionStatus.COMMITTED);
	}
	else
	{
	    System.out.flush();
	    System.out.print("abort  ");
	    System.out.flush();
	    a.abort();
	}

	System.out.println(" incr21 : "+res1+" : "+res2+" : "+res+" : "+ran);
    }

public static int getValue1() throws TestException
    {
        return(atomicObject1.get());
    }

public static int getValue2() throws TestException
    {
        return(atomicObject2.get());
    }

private static void get12 (int thr, int level)
    {
	boolean res  = true;
	boolean res1 = true;
	boolean res2 = true;

	int value1 = 0;
	int value2 = 0;

	AtomicAction a = new AtomicAction();

	a.begin();

	indent(thr, level);
	System.out.println("begin   get12");

	try
	{
	    value1 = atomicObject1.get();
	}
	catch (TestException e)
	{
	    res  = res1 = false;
	}

	indent(thr, level);
	System.out.println("part1   get12  : "+res1);

	lowProbYield();

	if (res)
	{
	    try
	    {
		value2 = atomicObject2.get();
	    }
	    catch (TestException e)
	    {
		res  = res2 = false;
	    }

	    indent(thr, level);
	    System.out.println("part2   get12  : "+res2);
	}
	else
	    res2 = false;

	lowProbYield();

	indent(thr, level);
	if (res)
	{
	    System.out.print("commit ");
	    res = (boolean) (a.commit() == ActionStatus.COMMITTED);
	}
	else
	{
	    System.out.print("abort  ");
	    a.abort();
	}

	System.out.println(" get12  : "+res1+" : "+res2+" : "+res+" : "+value1+" : "+value2);
    }

private static void get21 (int thr, int level)
    {
	boolean res  = true;
	boolean res1 = true;
	boolean res2 = true;

	int value1 = 0;
	int value2 = 0;

	AtomicAction a = new AtomicAction();

	a.begin();

	indent(thr, level);
	System.out.println("begin   get21");

	try
	{
	    value1 = atomicObject2.get();
	}
	catch (TestException e)
	{
	    res  = res1 = false;
	}

	indent(thr, level);
	System.out.println("part1   get21  : "+res1);

	lowProbYield();

	if (res)
	{
	    try
	    {
		value2 = atomicObject1.get();
	    }
	    catch (TestException e)
	    {
		res  = res2 = false;
	    }

	    indent(thr, level);
	    System.out.println("part2   get21  : "+res2);
	}
	else
	    res2 = false;

	lowProbYield();

	indent(thr, level);
	if (res)
	{
	    System.out.print("commit ");
	    res = (boolean) (a.commit() == ActionStatus.COMMITTED);
	}
	else
	{
	    System.out.print("abort  ");
	    a.abort();
	}

	System.out.println(" get21  : "+res1+" : "+res2+" : "+res+" : "+value1+" : "+value2);
    }

private static AtomicObject atomicObject1 = null;
private static AtomicObject atomicObject2 = null;
private static Random rand = null;

}
