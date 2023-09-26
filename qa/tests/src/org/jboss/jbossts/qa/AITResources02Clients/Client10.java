/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.AITResources02Clients;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.AITResources02.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CORBA.IntHolder;

public class Client10
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String pingPongIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			PingPong pingPong = PingPongHelper.narrow(ORBInterface.orb().string_to_object(pingPongIOR));

			int numberOfCalls = 10;

			for (int index0 = 0; index0 < numberOfCalls; index0++)
			{
				for (int index1 = 0; index1 <= index0; index1++)
				{
					pingPong.bad_hit(index0, index1, pingPong, pingPong, null);
				}
			}

			IntHolder pingPongValue = new IntHolder();
			pingPong.get(pingPongValue, null);

			if (pingPongValue.value == 0)
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
			System.err.println("Client10.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client10.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}