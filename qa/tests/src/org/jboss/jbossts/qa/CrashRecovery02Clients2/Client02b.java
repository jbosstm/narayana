/*
 * SPDX short identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.CrashRecovery02Clients2;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery02.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CosTransactions.HeuristicHazard;

public class Client02b
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String serviceIOR1 = ServerIORStore.loadIOR(args[args.length - 2]);
			BeforeCrashService service1 = BeforeCrashServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR1));

			String serviceIOR2 = ServerIORStore.loadIOR(args[args.length - 1]);
			BeforeCrashService service2 = BeforeCrashServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR2));

			ResourceBehavior[] resourceBehaviors1 = new ResourceBehavior[1];
			resourceBehaviors1[0] = new ResourceBehavior();
			resourceBehaviors1[0].crash_behavior = CrashBehavior.CrashBehaviorCrashInCommit;

			ResourceBehavior[] resourceBehaviors2 = new ResourceBehavior[1];
			resourceBehaviors2[0] = new ResourceBehavior();
			resourceBehaviors2[0].crash_behavior = CrashBehavior.CrashBehaviorNoCrash;

			boolean correct = true;

			OTS.current().begin();

			service1.setup_oper(resourceBehaviors1);
			service2.setup_oper(resourceBehaviors2);

			correct = correct && service1.is_correct();
			correct = correct && service2.is_correct();

			try
			{
				OTS.current().commit(true);
			}
			catch (HeuristicHazard heuristicHazard)
			{
			}

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
			System.err.println("Client02b.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client02b.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}