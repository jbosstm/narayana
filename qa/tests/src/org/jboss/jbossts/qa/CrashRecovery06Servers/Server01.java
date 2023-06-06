/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.CrashRecovery06Servers;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery06.*;
import org.jboss.jbossts.qa.CrashRecovery06Impls.ServiceImpl01;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Server01
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			ServiceImpl01 serviceImpl = new ServiceImpl01();
			ServicePOATie servant = new ServicePOATie(serviceImpl);

			OAInterface.objectIsReady(servant);
			Service service = ServiceHelper.narrow(OAInterface.corbaReference(servant));

			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(service));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("Server01.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}