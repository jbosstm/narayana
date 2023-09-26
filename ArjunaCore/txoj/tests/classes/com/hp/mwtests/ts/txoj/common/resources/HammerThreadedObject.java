/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.txoj.common.resources;

import java.util.Random;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.hp.mwtests.ts.txoj.common.exceptions.TestException;

public class HammerThreadedObject extends Thread
{
    
public HammerThreadedObject (int value)
    {
	_uid = new Uid();
	_value = value;
    }

public void run ()
    {
	for (int i = 0; i < HammerThreadedObject.iter; i++)
	{
	    AtomicAction A = new AtomicAction();
	    float f = HammerThreadedObject.rand.nextFloat();

	    try
	    {
		Thread.yield();
		
		A.begin();

		int v = HammerThreadedObject.object.get();

		if (f > 0.25)
		    System.out.println(_uid+": atomic object value: "+v);
		else
		{
		    int nv = v+_value;
		
		    HammerThreadedObject.object.set(nv);
		    
		    System.out.println(_uid+": atomic object value set to : "+nv);
		}
	    
		A.commit();
		
		try
		{
		    Thread.sleep((int) HammerThreadedObject.rand.nextFloat()*1000);
		}
		catch (InterruptedException e)
		{
		}
	    }
	    catch (TestException e)
	    {
		System.out.println(_uid+": AtomicObject exception raised: "+e);
		A.abort();

		Thread.yield();
	    }
	}
    }

private Uid _uid;
private int _value;

public static AtomicObject object = null;
public static int iter = 100;
public static Random rand = new Random();
    
}