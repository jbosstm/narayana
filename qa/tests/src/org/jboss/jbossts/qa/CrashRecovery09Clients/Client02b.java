/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.CrashRecovery09Clients;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import org.jboss.jbossts.qa.CrashRecovery09.*;
import org.jboss.jbossts.qa.CrashRecovery09Impls.EndCrashAbstractRecordImpl;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Client02b
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

			service.set(0);

			OTS.current().commit(true);

			OTS.current().begin();

			service.set(1);

			correct = (BasicAction.Current().add(new EndCrashAbstractRecordImpl(EndCrashAbstractRecordImpl.CRASH_IN_PREPARE)) == AddOutcome.AR_ADDED);

			if (correct)
			{
				OTS.current().commit(true);
			}

			System.out.println("Failed");
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