/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.resources;

import org.omg.CORBA.IntHolder;
import org.omg.CosTransactions.Control;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.hp.mwtests.ts.jts.TestModule.Hammer;
import com.hp.mwtests.ts.jts.utils.Util;

public class DistributedHammerWorker1
{
    
public static void incr12 (char thr, int level)
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

	    if (res)
	    {
		res2 = hammerObject_2.incr(-ran, control);
		res  = res2;

		Util.indent(thr, level);
		System.out.println("part2   incr12 : "+res2);
	    }

	    Util.indent(thr, level);

	    control = null;
    
	    if (res)
	    {
		System.out.print("end ");

		OTSImpleManager.current().commit(true);
		res = true;
	    }
	    else
	    {
		System.out.print("abort  ");
		OTSImpleManager.current().rollback();
	    }
	}
	catch (Exception e)
	{
	    System.err.println("DistributedHammerWorker1.incr12: "+e);
	    res1 = res2 = res = false;
	}

	Util.indent(thr, level);

	System.out.println(" incr12 : "+res1+" : "+res2+" : "+res
			   +" : "+ran);
    }

public static void incr21 (char thr, int level)
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
	    
	    if (res)
	    {
		res2 = hammerObject_1.incr(-ran, control);
		res  = res2;

		Util.indent(thr, level);
		System.out.println("part2   incr21 : "+res2);
	    }

	    control = null;
    
	    Util.indent(thr, level);
	    if (res)
	    {
		System.out.print("end ");
		OTSImpleManager.current().commit(true);
		res = true;
	    }
	    else
	    {
		System.out.print("abort  ");
		OTSImpleManager.current().rollback();
	    }
	}
	catch (Exception e)
	{
	    System.err.println("DistributedHammerWorker1.incr21: "+e);
	    res1 = res2 = res = false;
	}

	Util.indent(thr, level);

	System.out.println(" incr21 : "+res1+" : "+res2+" : "+res
			   +" : "+ran);
    }

public static void get12 (char thr, int level)
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

	    if (res)
	    {
		res2 = hammerObject_2.get(value2, control);
		res  = res2;

		Util.indent(thr, level);
		System.out.println("part2   get12  : "+res2);
	    }

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
	    System.err.println("DistributedHammerWorker1.get12: "+e);
	    res1 = res2 = res = false;
	}

	Util.indent(thr, level);

	System.out.println(" get12  : "+res1+" : "+res2+" : "+res
			   +" : "+value1.value+" : "+value2.value);
    }

public static void get21 (char thr, int level)
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

	    if (res)
	    {
		res2 = hammerObject_1.get(value2, control);
		res  = res2;

		Util.indent(thr, level);
		System.out.println("part2   get21  : "+res2);
	    }

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
	    System.err.println("DistributedHammerWorker1.get21: "+e);
	    res1 = res2 = res = false;
	}

	Util.indent(thr, level);

	System.out.println(" get21  : "+res1+" : "+res2+" : "+res
			   +" : "+value1.value+" : "+value2.value);
    }

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
		    System.err.println("DistributedHammerWorker1.randomOperation: "+e);
		}
	    }
	break;
        case 5:
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
		    System.err.println("DistributedHammerWorker1.randomOperation: "+e);
		}
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
}