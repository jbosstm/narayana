/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.CrashRecovery11Clients;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery11.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CosTransactions.HeuristicHazard;

public class Client04b
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String serviceIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			BeforeCrashService service = BeforeCrashServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR));

			boolean correct = true;

			OTS.current().begin();

			service.set(0);

			OTS.current().commit(true);

			OTS.current().begin();

			service.set(1);
			service.setStartCrashAbstractRecordAction(CrashBehavior.CrashBehaviorCrashInPrepare);

			correct = correct && service.is_correct(); // checks basic action was added correctly

			try
			{
				OTS.current().commit(true);
				correct = false;
			}
			catch (HeuristicHazard heuristicHazard)
			{
				//System.err.println ("Client04b.main : caught expected HeuristicHazard");
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
			System.err.println("Client04b.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client04b.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}