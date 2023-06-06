/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.CrashRecovery10Clients;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery10.*;
import org.jboss.jbossts.qa.Utils.*;
import org.omg.CORBA.IntHolder;

public class Client03a
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

			CrashRecoveryDelays.awaitRecoveryCR10();

			boolean correct = true;

			OTS.current().begin();

			IntHolder valueHolder1 = new IntHolder();
			IntHolder valueHolder2 = new IntHolder();
			service1.get(OTS.get_current().get_control(), valueHolder1);
			service2.get(OTS.get_current().get_control(), valueHolder2);
			correct = correct && (valueHolder1.value == 1);
			correct = correct && (valueHolder2.value == 1);

			OTS.current().commit(true);

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
			System.err.println("Client03a.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client03a.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}