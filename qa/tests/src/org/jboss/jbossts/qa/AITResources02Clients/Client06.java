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

public class Client06
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String pingerIOR = ServerIORStore.loadIOR(args[args.length - 2]);
			PingPong pinger = PingPongHelper.narrow(ORBInterface.orb().string_to_object(pingerIOR));

			String pongerIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			PingPong ponger = PingPongHelper.narrow(ORBInterface.orb().string_to_object(pongerIOR));

			int numberOfCalls = 10;

			for (int index = 0; index < numberOfCalls; index++)
			{
				pinger.hit(index, ponger, pinger, null);
			}

			IntHolder pingerValue = new IntHolder();
			pinger.get(pingerValue, null);

			IntHolder pongerValue = new IntHolder();
			ponger.get(pongerValue, null);

			if ((pingerValue.value == (numberOfCalls / 2)) && (pongerValue.value == (numberOfCalls / 2)))
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
			System.err.println("Client06.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client06.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}