/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.CrashRecovery08Clients;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import org.jboss.jbossts.qa.CrashRecovery08.*;
import org.jboss.jbossts.qa.CrashRecovery08Impls.CrashAbstractRecordImpl;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Client01b
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			int numberOfResources = Integer.parseInt(args[args.length - 2]);

			String serviceIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			Service service = ServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR));

			boolean correct = true;

			OTS.current().begin();

			service.setup_oper(OTS.current().get_control(), numberOfResources);

			correct = (BasicAction.Current().add(new CrashAbstractRecordImpl()) == AddOutcome.AR_ADDED);

			correct = correct && service.is_correct();

			if (correct)
			{
				OTS.current().commit(true);

				System.out.println("jjh: "+service.get_resource_trace(0));

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
			System.err.println("Client01b.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client01b.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}