/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.hammer;

import java.util.Calendar;

import javax.transaction.xa.XAResource;

import org.junit.Test;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.hp.mwtests.ts.jta.jts.common.XACreator;

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

		jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

		if (tm != null)
		{
		    tm.begin();
	    
		    jakarta.transaction.Transaction theTransaction = tm.getTransaction();

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
    @Test
    public void test() throws Exception
    {
	ORB myORB = null;
	RootOA myOA = null;

	    myORB = ORB.getInstance("test");
	    myOA = OA.getRootOA(myORB);
	    
	    myORB.initORB(new String[] {}, null);
	    myOA.initOA();

	    ORBManager.setORB(myORB);
	    ORBManager.setPOA(myOA);

	String xaResource = "com.hp.mwtests.ts.jta.jts.common.DummyCreator";
	String connectionString = null;
	int threads = 10;
	int work = 100;

        jtaPropertyManager.getJTAEnvironmentBean().setTransactionManagerClassName(com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple.class.getName());
        jtaPropertyManager.getJTAEnvironmentBean().setUserTransactionClassName(com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple.class.getName());

	/*
	 * We should have a reference to a factory object (see JTA
	 * specification). However, for simplicity we will ignore this.
	 */

	XACreator creator = (XACreator) Thread.currentThread().getContextClassLoader().loadClass(xaResource).newInstance();

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

	myOA.destroy();
	myORB.shutdown();
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