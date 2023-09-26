/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.RawResources01Clients1;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.RawResources01.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CosTransactions.HeuristicHazard;

public class Client003
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String serviceIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			Service service = ServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR));

			ResourceBehavior[] resourceBehaviors = new ResourceBehavior[1];
			resourceBehaviors[0] = new ResourceBehavior();
			resourceBehaviors[0].prepare_behavior = PrepareBehavior.PrepareBehaviorReturnVoteCommit;
			resourceBehaviors[0].rollback_behavior = RollbackBehavior.RollbackBehaviorReturn;
			resourceBehaviors[0].commit_behavior = CommitBehavior.CommitBehaviorReturn;
			resourceBehaviors[0].commitonephase_behavior = CommitOnePhaseBehavior.CommitOnePhaseBehaviorRaiseHeuristicHazard;

			boolean correct = true;

			OTS.current().begin();

			service.oper(resourceBehaviors);

			try
			{
				OTS.current().commit(true);
				System.err.println("Commit succeeded when it shouldn't");
				correct = false;
			}
			catch (HeuristicHazard heuristicHazard)
			{
			}

			correct = correct && service.is_correct();
			if (!correct)
			{
				System.err.println("service.is_correct() returned false");
			}

			correct = correct && (service.get_resource_trace(0) == ResourceTrace.ResourceTraceCommitOnePhaseForget);

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
			System.err.println("Client003.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client003.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}