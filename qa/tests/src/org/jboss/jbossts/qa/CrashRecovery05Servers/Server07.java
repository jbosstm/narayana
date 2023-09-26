/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.CrashRecovery05Servers;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery05.*;
import org.jboss.jbossts.qa.CrashRecovery05Impls.BeforeCrashServiceImpl02;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Server07
{
	public static void main(String args[])
	{
		try
		{
			if (ORBInterface.getORB() == null) {
				ORBInterface.initORB(args, null);
				OAInterface.initOA();
			}

			BeforeCrashServiceImpl02 beforeCrashServiceImpl1 = new BeforeCrashServiceImpl02(args[args.length - 3].hashCode(), 0);
			BeforeCrashServiceImpl02 beforeCrashServiceImpl2 = new BeforeCrashServiceImpl02(args[args.length - 3].hashCode(), 1);

			BeforeCrashServicePOATie servant1 = new BeforeCrashServicePOATie(beforeCrashServiceImpl1);
			BeforeCrashServicePOATie servant2 = new BeforeCrashServicePOATie(beforeCrashServiceImpl2);

			OAInterface.objectIsReady(servant1);
			BeforeCrashService beforeCrashService1 = BeforeCrashServiceHelper.narrow(OAInterface.corbaReference(servant1));

			OAInterface.objectIsReady(servant2);
			BeforeCrashService beforeCrashService2 = BeforeCrashServiceHelper.narrow(OAInterface.corbaReference(servant2));

			ServerIORStore.storeIOR(args[args.length - 2], ORBInterface.orb().object_to_string(beforeCrashService1));
			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(beforeCrashService2));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("Server07.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}