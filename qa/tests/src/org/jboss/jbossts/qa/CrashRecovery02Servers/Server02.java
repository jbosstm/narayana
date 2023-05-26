/*
 * SPDX short identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.CrashRecovery02Servers;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery02.*;
import org.jboss.jbossts.qa.CrashRecovery02Impls.AfterCrashServiceImpl01;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Server02
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			AfterCrashServiceImpl01 afterCrashServiceImpl = new AfterCrashServiceImpl01(args[args.length - 2].hashCode(), 0);
			AfterCrashServicePOATie servant = new AfterCrashServicePOATie(afterCrashServiceImpl);

			OAInterface.objectIsReady(servant);
			AfterCrashService afterCrashService = AfterCrashServiceHelper.narrow(OAInterface.corbaReference(servant));

			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(afterCrashService));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("Server02.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}