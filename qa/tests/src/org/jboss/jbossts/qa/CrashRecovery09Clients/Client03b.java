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
import org.jboss.jbossts.qa.CrashRecovery09Impls.StartCrashAbstractRecordImpl;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Client03b
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

			service1.set(0);
			service2.set(0);

			OTS.current().commit(true);

			OTS.current().begin();

			service1.set(1);
			service2.set(1);

			correct = (BasicAction.Current().add(new StartCrashAbstractRecordImpl(StartCrashAbstractRecordImpl.CRASH_IN_COMMIT)) == AddOutcome.AR_ADDED);

			if (correct)
			{
				OTS.current().commit(true);
			}

			System.out.println("Failed");
		}
		catch (Exception exception)
		{
			System.out.println("Failed");
			System.err.println("Client03b.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client03b.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}