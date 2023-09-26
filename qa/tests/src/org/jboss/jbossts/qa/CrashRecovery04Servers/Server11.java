/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.CrashRecovery04Servers;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery04.*;
import org.jboss.jbossts.qa.CrashRecovery04Impls.ServiceImpl06;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Server11
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			ServiceImpl06 serviceImpl1 = new ServiceImpl06(0);
			ServiceImpl06 serviceImpl2 = new ServiceImpl06(1);

			ServicePOATie servant1 = new ServicePOATie(serviceImpl1);
			ServicePOATie servant2 = new ServicePOATie(serviceImpl2);

			OAInterface.objectIsReady(servant1);
			Service service1 = ServiceHelper.narrow(OAInterface.corbaReference(servant1));

			OAInterface.objectIsReady(servant2);
			Service service2 = ServiceHelper.narrow(OAInterface.corbaReference(servant2));

			ServerIORStore.storeIOR(args[args.length - 2], ORBInterface.orb().object_to_string(service1));
			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(service2));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("Server11.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}