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
package com.hp.mwtests.ts.txoj.atomicobject;

/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: AtomicObjectTest1.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.*;
import com.arjuna.ats.arjuna.common.*;
import java.util.Random;
import java.lang.Math;

import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;
import com.arjuna.mwlabs.testframework.unittest.Test;
import com.arjuna.mwlabs.testframework.unittest.LocalHarness;

public class AtomicObjectTest1 extends Test
{
    public static final int START_VALUE_1 = 10;
    public static final int START_VALUE_2 = 101;
    public static final int EXPECTED_RESULT = START_VALUE_1 + START_VALUE_2;


public void run(String[] args)
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
	    System.out.println("m set1 : failed");
            logInformation("ERROR - "+e);
            e.printStackTrace(System.err);
            assertFailure();
	}

	try
	{
	    atomicObject2.set(START_VALUE_2);
	}
	catch (TestException e)
	{
	    System.out.println("m set2 : failed");
            logInformation("ERROR - "+e);
            e.printStackTrace(System.err);
            assertFailure();
	}

	get12('m', 0);
	get21('m', 0);

	for (int i = 0; i < 100; i++)
	    randomOperation('1', 0);

	get12('m', 0);
	get21('m', 0);

        try
        {
            if ( ( getValue1() + getValue2() ) != EXPECTED_RESULT )
            {
                assertFailure();
            }
        }
        catch (TestException e)
        {
            assertFailure();
            logInformation("Unexpected Failure: "+e);
            e.printStackTrace(System.err);
        }

        assertSuccess();
    }

private static void indent (char thr, int level)
    {
	System.out.print(thr+" ");

	for (int i = 0; i < level; i++)
	    System.out.print(" ");
    }

private static void incr12 (char thr, int level)
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
	    res = res1 = false;
	}

	indent(thr, level);
	System.out.println("part1   incr12 : "+res1);

	if (res)
	{
	    try
	    {
		atomicObject2.incr(-ran);
	    }
	    catch (TestException e)
	    {
		res = res2 = false;
	    }

	    indent(thr, level);
	    System.out.println("part2   incr12 : "+res2);
	}
	else
	    res2 = false;

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

	System.out.println(" incr12 : "+res1+" : "+res2+" : "+res+" : "+ran);
    }

private static void incr21 (char thr, int level)
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
	    res = res1 = false;
	}

	indent(thr, level);
	System.out.println("part1   incr21 : "+res1);

	if (res)
	{
	    try
	    {
		atomicObject1.incr(-ran);
	    }
	    catch (TestException e)
	    {
		res = res2 = false;
	    }

	    indent(thr, level);
	    System.out.println("part2   incr21 : "+res2);
	}
	else
	    res2 = false;

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

private static void get12 (char thr, int level)
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
	    res = res1 = false;
	}

	indent(thr, level);
	System.out.println("part1   get12  : "+res1);

	if (res)
	{
	    try
	    {
		value2 = atomicObject2.get();
	    }
	    catch (TestException e)
	    {
		res = res2 = false;
	    }

	    indent(thr, level);
	    System.out.println("part2   get12  : "+res2);
	}
	else
	    res2 = false;

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

private static void get21 (char thr, int level)
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
	    res = res1 = false;
	}

	indent(thr, level);
	System.out.println("part1   get21  : "+res1);

	if (res)
	{
	    try
	    {
		value2 = atomicObject1.get();
	    }
	    catch (TestException e)
	    {
		res = res2 = false;
	    }

	    indent(thr, level);
	    System.out.println("part2   get21  : "+res2);
	}
	else
	    res2 = false;

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

private static void randomOperation (char thr, int level)
    {
	switch (Math.abs(rand.nextInt()) % 6)
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
        case 5:
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
	}
    }

public static void main(String[] args)
    {
        AtomicObjectTest1 test = new AtomicObjectTest1();
        test.initialise( null, null, args, new LocalHarness() );
        test.runTest();
    }

private static AtomicObject atomicObject1 = null;
private static AtomicObject atomicObject2 = null;
private static Random rand = null;

}
