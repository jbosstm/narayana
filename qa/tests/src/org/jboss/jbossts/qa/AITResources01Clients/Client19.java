/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.AITResources01Clients;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import com.arjuna.ats.jts.extensions.AtomicTransaction;
import org.jboss.jbossts.qa.AITResources01.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CORBA.IntHolder;

public class Client19
{
	public static void main(String[] args)
	{

		boolean correct = true;
		int numberOfCalls = 10;
		Counter counter = null;

		System.err.println("Starting first init");

		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();
		}
		catch (Exception exception)
		{
			correct = false;
			System.err.println("exception in first start: " + exception);
			exception.printStackTrace(System.err);
		}


		System.err.println("Starting first block");

		try
		{
			String counterIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			counter = CounterHelper.narrow(ORBInterface.orb().string_to_object(counterIOR));

			for (int index = 0; index < numberOfCalls; index++)
			{
				AtomicTransaction atomicTransaction = new AtomicTransaction();

				atomicTransaction.begin();

				counter.increase();

				if ((index % 2) == 0)
				{
					atomicTransaction.commit(true);
				}
				else
				{
					atomicTransaction.rollback();
				}
			}

			AtomicTransaction atomicTransaction = new AtomicTransaction();

			atomicTransaction.begin();

			IntHolder value = new IntHolder();
			counter.get(value);

			atomicTransaction.commit(true);

			if (value.value == (numberOfCalls / 2) && correct)
			{
				correct = true;
			}
			else
			{
				correct = false;
			}
		}
		catch (Exception exception)
		{
			correct = false;
			System.err.println("exception in first block" + exception);
			exception.printStackTrace(System.err);
		}

		System.err.println("Starting first shutdown");

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			correct = false;
			System.err.println("exception in first shutdown" + exception);
			exception.printStackTrace(System.err);
		}

		System.err.println("----Starting second block -------");

		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();
		}
		catch (Exception exception)
		{
			correct = false;
			System.err.println("exception in second start " + exception);
			exception.printStackTrace(System.err);
		}

		System.err.println("init done starting second block");

		try
		{
			String counterIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			counter = CounterHelper.narrow(ORBInterface.orb().string_to_object(counterIOR));

			AtomicTransaction atomicTransaction = new AtomicTransaction();
			atomicTransaction.begin();
			counter.set(0);
			atomicTransaction.commit(true);
		}
		catch (Exception exception)
		{
			correct = false;
			System.err.println("exception in set operation " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			for (int index = 0; index < numberOfCalls; index++)
			{
				AtomicTransaction atomicTransaction = new AtomicTransaction();

				atomicTransaction.begin();

				counter.increase();

				if ((index % 2) == 0)
				{
					atomicTransaction.commit(true);
				}
				else
				{
					atomicTransaction.rollback();
				}
			}

		}
		catch (Exception exception)
		{
			correct = false;
			System.err.println("exception in second loop block " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			AtomicTransaction atomicTransaction = new AtomicTransaction();

			atomicTransaction.begin();

			IntHolder value = new IntHolder();
			counter.get(value);

			atomicTransaction.commit(true);

			if (value.value == (numberOfCalls / 2) && correct)
			{
				correct = true;
			}
			else
			{
				correct = false;
			}
		}
		catch (Exception exception)
		{
			correct = false;
			System.err.println("exception in second test " + exception);
			exception.printStackTrace(System.err);
		}

		System.err.println("Starting second shutdown");

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			correct = false;
			System.err.println("error in second shutdown" + exception);
			exception.printStackTrace(System.err);
		}

		System.err.println("testing result");
		if (correct)
		{
			System.out.println("Passed");
		}
		else
		{
			System.out.println("Failed");
		}
	}
}