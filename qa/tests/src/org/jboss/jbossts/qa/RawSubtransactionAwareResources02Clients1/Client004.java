/*
 * SPDX short identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.RawSubtransactionAwareResources02Clients1;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.RawSubtransactionAwareResources02.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Client004
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

			OTS.current().begin();

			service.oper(1, OTS.current().get_control());

			OTS.current().rollback();

			OTS.current().commit(true);

			correct = correct && service.is_correct();

			correct = correct && (service.get_subtransaction_aware_resource_trace(0) == SubtransactionAwareResourceTrace.SubtransactionAwareResourceTraceRollbackSubtransaction);

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