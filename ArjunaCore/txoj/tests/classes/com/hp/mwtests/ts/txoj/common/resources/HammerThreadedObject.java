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
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: HammerThreadedObject.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.txoj.common.resources;

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.arjuna.common.*;
import java.lang.Thread;
import java.util.Random;

import com.hp.mwtests.ts.txoj.common.exceptions.TestException;

import java.lang.InterruptedException;

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
