/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.AITResources02Outcomes;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.AITResources02.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CORBA.IntHolder;

public class Outcome02
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);

			int expectedValue = Integer.parseInt(args[args.length - 3]);

			String counterIOR1 = ServerIORStore.loadIOR(args[args.length - 2]);
			Counter counter1 = CounterHelper.narrow(ORBInterface.orb().string_to_object(counterIOR1));

			String counterIOR2 = ServerIORStore.loadIOR(args[args.length - 1]);
			Counter counter2 = CounterHelper.narrow(ORBInterface.orb().string_to_object(counterIOR2));

			IntHolder value1 = new IntHolder();
			counter1.get(value1, null);

			IntHolder value2 = new IntHolder();
			counter2.get(value2, null);

			if ((value1.value == expectedValue) && (value2.value == expectedValue))
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
			System.err.println("Outcome02.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Outcome02.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}