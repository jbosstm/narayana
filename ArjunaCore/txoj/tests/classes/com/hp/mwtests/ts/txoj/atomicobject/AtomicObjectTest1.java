/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.txoj.atomicobject;



import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.internal.txoj.lockstore.BasicPersistentLockStore;
import com.arjuna.ats.txoj.common.txojPropertyManager;
import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;

public class AtomicObjectTest1
{
    public static final int START_VALUE_1 = 10;
    public static final int START_VALUE_2 = 101;
    public static final int EXPECTED_RESULT = START_VALUE_1 + START_VALUE_2;

    @Test
    public void test() throws TestException
    {
        txojPropertyManager.getTxojEnvironmentBean().setLockStoreType(BasicPersistentLockStore.class.getName());
        
	rand = new Random();

	atomicObject1 = new AtomicObject(ObjectModel.MULTIPLE);
	atomicObject2 = new AtomicObject(ObjectModel.MULTIPLE);

	System.out.println(atomicObject1.get_uid());
	System.out.println(atomicObject2.get_uid());

    atomicObject1.set(START_VALUE_1);

    atomicObject2.set(START_VALUE_2);

	get12('m', 0);
	get21('m', 0);

	for (int i = 0; i < 100; i++)
	    randomOperation('1', 0);

	get12('m', 0);
	get21('m', 0);

        assertEquals(EXPECTED_RESULT, (getValue1()+getValue2()));

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

private static AtomicObject atomicObject1 = null;
private static AtomicObject atomicObject2 = null;
private static Random rand = null;

}