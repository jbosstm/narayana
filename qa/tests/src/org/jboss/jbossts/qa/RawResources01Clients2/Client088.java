/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.RawResources01Clients2;



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

public class Client088
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String serviceIOR1 = ServerIORStore.loadIOR(args[args.length - 2]);
			Service service1 = ServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR1));

			String serviceIOR2 = ServerIORStore.loadIOR(args[args.length - 1]);
			Service service2 = ServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR2));

			ResourceBehavior[] resourceBehaviors1 = new ResourceBehavior[1];
			resourceBehaviors1[0] = new ResourceBehavior();
			resourceBehaviors1[0].prepare_behavior = PrepareBehavior.PrepareBehaviorReturnVoteReadOnly;
			resourceBehaviors1[0].rollback_behavior = RollbackBehavior.RollbackBehaviorReturn;
			resourceBehaviors1[0].commit_behavior = CommitBehavior.CommitBehaviorReturn;
			resourceBehaviors1[0].commitonephase_behavior = CommitOnePhaseBehavior.CommitOnePhaseBehaviorReturn;

			ResourceBehavior[] resourceBehaviors2 = new ResourceBehavior[1];
			resourceBehaviors2[0] = new ResourceBehavior();
			resourceBehaviors2[0].prepare_behavior = PrepareBehavior.PrepareBehaviorReturnVoteRollback;
			resourceBehaviors2[0].rollback_behavior = RollbackBehavior.RollbackBehaviorRaiseHeuristicCommit;
			resourceBehaviors2[0].commit_behavior = CommitBehavior.CommitBehaviorReturn;
			resourceBehaviors2[0].commitonephase_behavior = CommitOnePhaseBehavior.CommitOnePhaseBehaviorReturn;

			boolean correct = true;

			System.err.println("begin transaction");
			OTS.current().begin();

			service1.oper(resourceBehaviors1);

			service2.oper(resourceBehaviors2);

			ResourceTrace expectedResourceTrace1 = ResourceTrace.ResourceTracePrepare;

			try
			{
				System.err.println("commit transaction");
				OTS.current().commit(true);
				System.err.println("transaction committed, no HeuristicHazard exception thrown - test failed");
				System.err.println("Commit succeeded when it shouldn't");
				correct = false;
			}
			catch (HeuristicHazard heuristicHazard)
			{
				// No exception used to be thrown, but this has been changed to HeuristicHazard from TeT 2.2.2.
				// MKS 17/06/2002
				System.err.println("expected HeuristicHazard exception thrown");
			}

			correct = correct && service1.is_correct() && service2.is_correct();
			if (!correct)
			{
				System.err.println("service1.is_correct() or service2.is_correct() returned false");
			}

			ResourceTrace resourceTrace1 = service1.get_resource_trace(0);
			ResourceTrace resourceTrace2 = service2.get_resource_trace(0);

			correct = correct && (resourceTrace1 == expectedResourceTrace1);
			System.err.println("resourceTrace1 = " + resourceTrace1.value());

			correct = correct && (resourceTrace2 == ResourceTrace.ResourceTracePrepareRollbackForget);
			System.err.println("resourceTrace2 = " + resourceTrace2.value());

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
			System.err.println("Client088.main: " + exception);
			exception.printStackTrace(System.err);
			System.out.println("Failed");
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client088.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}