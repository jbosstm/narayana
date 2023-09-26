/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.AITResources02Clients;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import com.arjuna.ats.jts.extensions.AtomicTransaction;
import org.jboss.jbossts.qa.AITResources02.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CORBA.IntHolder;

/*
 * This class is used within the Arjuna Licence tests
 */

public class Client18
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			int expectedResult = Integer.parseInt(args[args.length - 1]);
			int numberOfCalls = Integer.parseInt(args[args.length - 2]);
			String counterIOR = ServerIORStore.loadIOR(args[args.length - 3]);
			Counter counter = CounterHelper.narrow(ORBInterface.orb().string_to_object(counterIOR));
			int index = 0;
			boolean expectingFailure;

			/* Remove 2 from the number of calls and the expected result
						 * two transactions occur other than in this loop
						 * one in the implementation within the server and one
						 * at the end to return the number from the object
						 */

			numberOfCalls -= 2;
			expectedResult -= 2;

			expectingFailure = (numberOfCalls != expectedResult);
			System.err.println("expectingFailure = " + expectingFailure);

			try
			{
				for (index = 0; index < numberOfCalls; index++)
				{
					AtomicTransaction atomicTransaction = new AtomicTransaction();

					atomicTransaction.begin();

					counter.increase(OTS.current().get_control());

					atomicTransaction.commit(true);
				}
			}
			catch (org.omg.CORBA.TRANSACTION_ROLLEDBACK e)
			{
				/*
								 * If the number of transactions created is equal to the
								 * expected result then we are not expecting this exception
								 * to be thrown therefore the test has failed
								 */
				System.err.println("Performed " + index + " calls when exception thrown");
				if (!expectingFailure)
				{
					System.err.println("Got unexpected org.omg.CORBA.TRANSACTION_ROLLEDBACK exception");
					throw e;
				}
				else
				{
					System.err.println("Got expected org.omg.CORBA.TRANSACTION_ROLLEDBACK exception");
				}

			}
			catch (Exception e)
			{
				System.err.println("Performed " + index + " calls when exception thrown");

				throw e;
			}

			AtomicTransaction atomicTransaction = new AtomicTransaction();

			atomicTransaction.begin();

			IntHolder value = new IntHolder();
			counter.get(value, OTS.current().get_control());

			try
			{
				atomicTransaction.commit(true);
			}
			catch (org.omg.CORBA.TRANSACTION_ROLLEDBACK e)
			{
				if (!expectingFailure)
				{
					System.err.println("Got unexpected org.omg.CORBA.TRANSACTION_ROLLEDBACK exception");
					throw e;
				}
				else
				{
					System.err.println("Got expected org.omg.CORBA.TRANSACTION_ROLLEDBACK exception");
				}
			}

			if (((!expectingFailure) && (value.value == expectedResult)) ||
					((expectingFailure) && (value.value != expectedResult)))
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
			System.err.println("Client18.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client18.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}