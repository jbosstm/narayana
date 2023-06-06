/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.Issues0001Clients;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import com.arjuna.ats.jts.extensions.AtomicTransaction;
import org.jboss.jbossts.qa.Issues0001.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CORBA.IntHolder;

public class Client0001
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String counterIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			Counter counter = CounterHelper.narrow(ORBInterface.orb().string_to_object(counterIOR));

			int numberOfCalls = 1000;

			for (int index = 0; index < numberOfCalls; index++)
			{
				AtomicTransaction atomicTransaction = new AtomicTransaction();

				atomicTransaction.begin();

				try
				{
					counter.increase();
				}
				catch (InvocationException invocationException)
				{
				}

				atomicTransaction.commit(true);
			}

			AtomicTransaction atomicTransaction = new AtomicTransaction();

			atomicTransaction.begin();

			IntHolder value = new IntHolder();

			counter.get(value);

			atomicTransaction.commit(true);

			if (value.value == numberOfCalls)
			{
				System.out.println("Passed");
			}
			else
			{
				System.out.println("Failed");
			}
		}
		catch (Exception exception)
		{
			System.out.println("Failed");
			System.err.println("Client0001.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client0001.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}