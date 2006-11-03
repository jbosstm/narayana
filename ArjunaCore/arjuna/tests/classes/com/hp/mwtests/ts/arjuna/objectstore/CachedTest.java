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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: CachedTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.objectstore;

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.common.*;

import java.io.*;

import java.util.*;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

import com.arjuna.mwlabs.testframework.unittest.Test;
import com.arjuna.mwlabs.testframework.unittest.LocalHarness;

class ThreadWriter extends Thread
{
    private static final String TYPE = "test";
    
    public ThreadWriter (ObjectStore theStore)
    {
	store = theStore;
    }
    
    public void run ()
    {
	byte[] data = new byte[1024];
	OutputObjectState state = new OutputObjectState(new Uid(), "type");
	Uid u = new Uid();

	try
	{
	    state.packBytes(data);

	    if (store.write_committed(u, TYPE, state))
	    {
		Thread.yield();
		
		InputObjectState s = store.read_committed(u, TYPE);

		Thread.yield();
	    
		if (s != null)
		{
		    if (store.remove_committed(u, TYPE))
			passed = true;
		    else
			System.err.println("Could not remove state.");
		}
		else
		    System.err.println("Could not read state.");
	    }
	    else
		System.err.println("Could not write state.");
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }

    public boolean passed = false;

    private ObjectStore store = null;
    
}


public class CachedTest
{
    public static void main (String[] args)
    {
	boolean passed = true;
	String cacheSize = "2048";
	int threads = 100;
	
	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].equals("-size"))
		cacheSize = args[i+1];
	    if (args[i].equals("-threads"))
	    {
		try
		{
		    threads = Integer.parseInt(args[i+1]);
		}
		catch (NumberFormatException e)
		{
		}
	    }
	    if (args[i].equals("-help"))
	    {
		System.err.println("Usage: [-size <cache size>] [-threads <number>]");

		System.exit(0);
	    }
	}

	Thread[] t = new Thread[threads];

	System.setProperty("com.arjuna.ats.internal.arjuna.objectstore.cacheStore.size", cacheSize);

	ObjectStore store = new ObjectStore(ArjunaNames.Implementation_ObjectStore_CacheStore());

	long stime = Calendar.getInstance().getTime().getTime();
	
	for (int i = 0; (i < threads) && passed; i++)
	{
	    try
	    {
		t[i] = new ThreadWriter(store);
		
		t[i].start();
	    }
	    catch (Exception ex)
	    {
		ex.printStackTrace();
		
		passed = false;
	    }
	}

	for (int j = 0; j < threads; j++)
	{
	    try
	    {
		t[j].join();

		passed = passed && ((ThreadWriter) t[j]).passed;
	    }
	    catch (Exception ex)
	    {
	    }
	}
	
	long ftime = Calendar.getInstance().getTime().getTime();
	long timeTaken = ftime - stime;

	try
	{
	    store.sync();
	}
	catch (Exception ex)
	{
	}

	System.out.println("time for "+threads+" users is "+timeTaken);
	
	if (passed)
	    System.out.println("Passed.");
	else
	    System.out.println("Failed.");
    }

}
