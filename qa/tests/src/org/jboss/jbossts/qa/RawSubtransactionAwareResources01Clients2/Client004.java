/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.RawSubtransactionAwareResources01Clients2;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.RawSubtransactionAwareResources01.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

public class Client004
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

			boolean correct = true;

			OTS.current().begin();

			OTS.current().begin();

			service1.oper(1);
			service2.oper(1);

			OTS.current().rollback_only();

			try
			{
				OTS.current().commit(true);
				correct = false;
			}
			catch (TRANSACTION_ROLLEDBACK transactionRolledBack)
			{
			}

			OTS.current().commit(true);

			correct = correct && service1.is_correct();
			correct = correct && service2.is_correct();

			correct = correct && (service1.get_subtransaction_aware_resource_trace(0) == SubtransactionAwareResourceTrace.SubtransactionAwareResourceTraceRollbackSubtransaction);
			correct = correct && (service2.get_subtransaction_aware_resource_trace(0) == SubtransactionAwareResourceTrace.SubtransactionAwareResourceTraceRollbackSubtransaction);

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
			System.err.println("Client004.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client004.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}