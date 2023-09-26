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
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.jboss.jbossts.qa.Utils.CrashRecoveryDelays;

public class Client05a
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String serviceIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			AfterCrashService service = AfterCrashServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR));

			CheckBehavior[] checkBehaviors = new CheckBehavior[1];
			checkBehaviors[0] = new CheckBehavior();
			checkBehaviors[0].allow_done = false;
			checkBehaviors[0].allow_returned_prepared = false;
			checkBehaviors[0].allow_returned_committing = false;
			checkBehaviors[0].allow_returned_committed = false;
			checkBehaviors[0].allow_returned_rolledback = true;
			checkBehaviors[0].allow_raised_not_prepared = false;

			boolean correct = true;

			service.setup_oper(1);

			correct = service.check_oper(checkBehaviors) && service.is_correct();

			CrashRecoveryDelays.awaitReplayCompletionCR02();

			ResourceTrace resourceTrace = service.get_resource_trace(0);

			correct = correct && (resourceTrace == ResourceTrace.ResourceTraceRollback);

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
			System.err.println("Client05a.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client05a.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}