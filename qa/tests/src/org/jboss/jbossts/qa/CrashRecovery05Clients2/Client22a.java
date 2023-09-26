/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.CrashRecovery05Clients2;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery05.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.jboss.jbossts.qa.Utils.CrashRecoveryDelays;

public class Client22a extends ClientBase
{
	public static void main(String[] args)
	{
		try
		{
			init(args, null);

			String serviceIOR1 = ServerIORStore.loadIOR(args[args.length - 2]);
			AfterCrashService service1 = AfterCrashServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR1));

			String serviceIOR2 = ServerIORStore.loadIOR(args[args.length - 1]);
			AfterCrashService service2 = AfterCrashServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR2));

			CheckBehavior[] checkBehaviors1 = new CheckBehavior[1];
			checkBehaviors1[0] = new CheckBehavior();
			checkBehaviors1[0].allow_done = false;
			checkBehaviors1[0].allow_returned_prepared = false;
			checkBehaviors1[0].allow_returned_committing = false;
			checkBehaviors1[0].allow_returned_committed = false;
			checkBehaviors1[0].allow_returned_rolledback = true;
			checkBehaviors1[0].allow_raised_not_prepared = false;

			CheckBehavior[] checkBehaviors2 = new CheckBehavior[1];
			checkBehaviors2[0] = new CheckBehavior();
			checkBehaviors2[0].allow_done = false;
			checkBehaviors2[0].allow_returned_prepared = false;
			checkBehaviors2[0].allow_returned_committing = false;
			checkBehaviors2[0].allow_returned_committed = false;
			checkBehaviors2[0].allow_returned_rolledback = true;
			checkBehaviors2[0].allow_raised_not_prepared = false;

			boolean correct = true;

			service1.setup_oper(1);
			service2.setup_oper(1);

			correct = correct && service1.check_oper(checkBehaviors1);
			correct = correct && service2.check_oper(checkBehaviors2);
			correct = correct && service1.is_correct();
			correct = correct && service2.is_correct();

			CrashRecoveryDelays.awaitReplayCompletionCR05();

			ResourceTrace resourceTrace1 = service1.get_resource_trace(0);
			ResourceTrace resourceTrace2 = service2.get_resource_trace(0);

			correct = correct && (resourceTrace1 == ResourceTrace.ResourceTraceRollback);
			correct = correct && (resourceTrace2 == ResourceTrace.ResourceTraceRollback);

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
			System.err.println("Client22a.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			fini();
		}
		catch (Exception exception)
		{
			System.err.println("Client22a.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}