/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.CrashRecovery02Clients1;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery02.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Client03b
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String serviceIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			BeforeCrashService service = BeforeCrashServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR));

			ResourceBehavior[] resourceBehaviors = new ResourceBehavior[1];
			resourceBehaviors[0] = new ResourceBehavior();
			resourceBehaviors[0].crash_behavior = CrashBehavior.CrashBehaviorCrashInRollback;

			boolean correct = true;

			OTS.current().begin();

			service.setup_oper(resourceBehaviors);

			correct = service.is_correct();

			OTS.current().rollback();

			if (correct)
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
			System.err.println("Client03b.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client03b.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}