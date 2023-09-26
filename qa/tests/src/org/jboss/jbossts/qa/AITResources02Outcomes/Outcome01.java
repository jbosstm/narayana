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

public class Outcome01
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);

			int expectedValue = Integer.parseInt(args[args.length - 2]);

			String counterIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			Counter counter = CounterHelper.narrow(ORBInterface.orb().string_to_object(counterIOR));

			IntHolder value = new IntHolder();
			counter.get(value, null);

			if (value.value == expectedValue)
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
			System.err.println("Outcome01.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Outcome01.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}