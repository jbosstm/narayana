/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.CrashRecovery04Clients;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery04.*;
import org.jboss.jbossts.qa.Utils.*;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

public class Client12
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String serviceIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			Service service = ServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR));

			boolean correct = true;

			OTS.current().begin();

			service.setup_oper(OTS.current().get_control(), 2);

			OTS.current().rollback_only();

			try
			{
				OTS.current().commit(false);
				correct = false;
			}
			catch (TRANSACTION_ROLLEDBACK transactionRolledBack)
			{
			}

//  code changed to cope with recovery manager fix
// 	that makes reply_completion cause resource to rollback even though
//	transaction has completed
			ResourceTrace resourceTrace1 = service.get_resource_trace(0);
			ResourceTrace resourceTrace2 = service.get_resource_trace(1);

//  trace should be rollback
			correct = correct && (resourceTrace1 == ResourceTrace.ResourceTraceRollback);
			correct = correct && (resourceTrace2 == ResourceTrace.ResourceTraceRollback);

//  check_oper will invoke reply_completion and check the state of the transaction
			correct = correct && service.check_oper();

// now sleep to let reply completion do its job 1 second should be more than enough
			CrashRecoveryDelays.awaitReplayCompletionCR04();

			correct = correct && service.is_correct();
//  after reply_completion is called the resource will have rollback called on
//	it again, changing the ResourceTrace to ResourceTrace.ResourceTraceUnknown
			resourceTrace1 = service.get_resource_trace(0);
			resourceTrace2 = service.get_resource_trace(1);

			correct = correct && (resourceTrace1 == ResourceTrace.ResourceTraceUnknown);
			correct = correct && (resourceTrace2 == ResourceTrace.ResourceTraceUnknown);


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
			System.err.println("Client12.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client12.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}