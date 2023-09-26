/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.RawResources02Clients3;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.RawResources02.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

public class Client026
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String serviceIOR1 = ServerIORStore.loadIOR(args[args.length - 3]);
			Service service1 = ServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR1));

			String serviceIOR2 = ServerIORStore.loadIOR(args[args.length - 2]);
			Service service2 = ServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR2));

			String serviceIOR3 = ServerIORStore.loadIOR(args[args.length - 1]);
			Service service3 = ServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR3));

			ResourceBehavior[] resourceBehaviors1 = new ResourceBehavior[1];
			resourceBehaviors1[0] = new ResourceBehavior();
			resourceBehaviors1[0].prepare_behavior = PrepareBehavior.PrepareBehaviorReturnVoteRollback;
			resourceBehaviors1[0].rollback_behavior = RollbackBehavior.RollbackBehaviorReturn;
			resourceBehaviors1[0].commit_behavior = CommitBehavior.CommitBehaviorReturn;
			resourceBehaviors1[0].commitonephase_behavior = CommitOnePhaseBehavior.CommitOnePhaseBehaviorReturn;

			ResourceBehavior[] resourceBehaviors2 = new ResourceBehavior[1];
			resourceBehaviors2[0] = new ResourceBehavior();
			resourceBehaviors2[0].prepare_behavior = PrepareBehavior.PrepareBehaviorReturnVoteReadOnly;
			resourceBehaviors2[0].rollback_behavior = RollbackBehavior.RollbackBehaviorReturn;
			resourceBehaviors2[0].commit_behavior = CommitBehavior.CommitBehaviorReturn;
			resourceBehaviors2[0].commitonephase_behavior = CommitOnePhaseBehavior.CommitOnePhaseBehaviorReturn;

			ResourceBehavior[] resourceBehaviors3 = new ResourceBehavior[1];
			resourceBehaviors3[0] = new ResourceBehavior();
			resourceBehaviors3[0].prepare_behavior = PrepareBehavior.PrepareBehaviorReturnVoteReadOnly;
			resourceBehaviors3[0].rollback_behavior = RollbackBehavior.RollbackBehaviorReturn;
			resourceBehaviors3[0].commit_behavior = CommitBehavior.CommitBehaviorReturn;
			resourceBehaviors3[0].commitonephase_behavior = CommitOnePhaseBehavior.CommitOnePhaseBehaviorReturn;

			boolean correct = true;

			OTS.current().begin();

			service1.oper(resourceBehaviors1, OTS.current().get_control());

			service2.oper(resourceBehaviors2, OTS.current().get_control());

			service3.oper(resourceBehaviors3, OTS.current().get_control());

			try
			{
				OTS.current().commit(true);
				System.err.println("Commit succeeded when it shouldn't");
				correct = false;
			}
			catch (TRANSACTION_ROLLEDBACK transactionRolledback)
			{
			}

			correct = correct && service1.is_correct() && service2.is_correct() && service3.is_correct();

			ResourceTrace resourceTrace1 = service1.get_resource_trace(0);
			ResourceTrace resourceTrace2 = service2.get_resource_trace(0);
			ResourceTrace resourceTrace3 = service3.get_resource_trace(0);

			correct = correct && ((resourceTrace1 == ResourceTrace.ResourceTracePrepareRollback) || (resourceTrace1 == ResourceTrace.ResourceTraceRollback));
			correct = correct && ((resourceTrace2 == ResourceTrace.ResourceTracePrepare) || (resourceTrace2 == ResourceTrace.ResourceTraceRollback));
			correct = correct && ((resourceTrace3 == ResourceTrace.ResourceTracePrepare) || (resourceTrace3 == ResourceTrace.ResourceTraceRollback));

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
			System.err.println("Client026.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client026.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}