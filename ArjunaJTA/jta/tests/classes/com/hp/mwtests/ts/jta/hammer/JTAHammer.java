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
 * Copyright (C) 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JTAHammer.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.hammer;

import com.hp.mwtests.ts.jta.common.*;

import com.arjuna.ats.jta.*;

import com.arjuna.ats.arjuna.common.*;

import javax.transaction.*;
import javax.transaction.xa.*;

import java.util.*;

import java.lang.IllegalAccessException;

class Worker extends Thread
{

    public Worker (XACreator c, String s, int iters)
    {
	_creator = c;
	_connectionString = s;
	_iters = iters;
    }
    
    public void run ()
    {
	for (int i = 0; i < _iters; i++)
	{
	    try
	    {
		XAResource theResource = _creator.create(_connectionString, false);

		if (theResource == null)
		{
		    System.err.println("Error - creator "+_creator+" returned null resource.");
		    System.exit(0);
		}

		javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

		if (tm != null)
		{
		    tm.begin();
	    
		    javax.transaction.Transaction theTransaction = tm.getTransaction();

		    if (theTransaction != null)
		    {
			if (!theTransaction.enlistResource(theResource))
			{
			    System.err.println("Error - could not enlist resource in transaction!");
			    tm.rollback();
			    
			    System.exit(0);
			}

			/*
			 * XA does not support subtransactions.
			 */
		    
			/*
			 * Do some work and decide whether to commit or
			 * rollback. (Assume commit for example.)
			 */

			tm.commit();
		    }
		    else
		    {
			System.err.println("Error - could not get transaction!");
			tm.rollback();
			System.exit(0);
		    }
		}
		else
		    System.err.println("Error - could not get transaction manager!");
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}

	JTAHammer.doSignal();
    }
    
    private XACreator _creator;
    private String _connectionString;
    private int _iters;

}

    
public class JTAHammer
{

    public static void main (String[] args)
    {
	String xaResource = "com.hp.mwtests.ts.jta.common.DummyCreator";
	String connectionString = null;
	int threads = 10;
	int work = 100;
	
	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-connect") == 0)
		connectionString = args[i+1];
	    if (args[i].compareTo("-creator") == 0)
		xaResource = args[i+1];
	    if (args[i].compareTo("-threads") == 0)
	    {
		try
		{
		    Integer v = new Integer(args[i+1]);
		    
		    threads = v.intValue();
		}
		catch (Exception e)
		{
		    System.err.println(e);
		}
	    }
	    if (args[i].compareTo("-work") == 0)
	    {
		try
		{
		    Integer v = new Integer(args[i+1]);
		    
		    work = v.intValue();
		}
		catch (Exception e)
		{
		    System.err.println(e);
		}
	    }
	    if (args[i].compareTo("-help") == 0)
	    {
		System.out.println("Usage: JTAHammer -creator <name> [-connect <string>] [-help] [-threads <number>] [-work <number>]");
		System.exit(0);
	    }
	}

	if (xaResource == null)
	{
	    System.err.println("Error - no resource creator specified.");
	    System.exit(0);
	}

	/*
	 * We should have a reference to a factory object (see JTA
	 * specification). However, for simplicity we will ignore this.
	 */

	XACreator creator = null;

	try
	{
	    creator = (XACreator) Thread.currentThread().getContextClassLoader().loadClass(xaResource).newInstance();
	}
	catch (Exception e)
	{
	    System.err.println(e);
	    
	    System.exit(0);
	}

	number = threads;

	int numberOfTransactions = threads * work;
	long stime = Calendar.getInstance().getTime().getTime();
	Worker[] workers = new Worker[threads];
	
	for (int i = 0; i < threads; i++)
	{
	    workers[i] = new Worker(creator, connectionString, work);
	    
	    workers[i].start();
	}

	JTAHammer.doWait();
	
	long ftime = Calendar.getInstance().getTime().getTime();
	long timeTaken = ftime - stime;
	
	System.out.println("time for "+numberOfTransactions+" write transactions is "+timeTaken);
	System.out.println("number of transactions: "+numberOfTransactions);
	System.out.println("throughput: "+(float) (numberOfTransactions/(timeTaken / 1000.0)));
    }

    public static void doWait ()
    {
	try
	{
	    synchronized (sync)
		{
		    if (number > 0)
			sync.wait();
		}
	}
	catch (Exception e)
	{
	}
    }

    public static void doSignal ()
    {
	synchronized (sync)
	    {
		if (--number == 0)
		    sync.notify();
	    }
    }
	
    private static Object sync = new Object();
    private static int number = 0;
    
}
