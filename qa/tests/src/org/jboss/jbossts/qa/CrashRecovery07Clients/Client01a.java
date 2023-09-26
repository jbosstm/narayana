/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.CrashRecovery07Clients;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery07.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.jboss.jbossts.qa.Utils.CrashRecoveryDelays;

public class Client01a
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			int numberOfResources = Integer.parseInt(args[args.length - 2]);

			String serviceIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			Service service = ServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR));

			boolean correct = true;

            CrashRecoveryDelays.awaitRecoveryCR07(Integer.parseInt(args[args.length - 3]));

			ResourceTrace resourceTrace = null;

			for (int index = 0; index < numberOfResources; index++)
			{
				resourceTrace = service.get_resource_trace(index);
				correct = correct && (resourceTrace == ResourceTrace.ResourceTracePrepareCommit);

				if (!correct)
				{
					System.out.println("Test will fail because we have just received value " + resourceTrace.value() + " for resource " + index);
				}
			}

			if (correct)
			{
				System.out.println("Passed");
			}
			else
			{
				System.out.println("Test has failed because we got " + resourceTrace.value() + " for " + numberOfResources);

				System.out.println("Failed");
			}
		}
		catch (Exception exception)
		{
			System.out.println("Failed");
			System.err.println("Client01a.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client01a.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}