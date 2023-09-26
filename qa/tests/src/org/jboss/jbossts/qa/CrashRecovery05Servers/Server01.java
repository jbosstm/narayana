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
import org.jboss.jbossts.qa.CrashRecovery05Impls.BeforeCrashServiceImpl01;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Server01
{
	public static void main(String args[])
	{
		try
		{
			if (ORBInterface.getORB() == null) {
				ORBInterface.initORB(args, null);
				OAInterface.initOA();
			}

			BeforeCrashServiceImpl01 beforeCrashServiceImpl = new BeforeCrashServiceImpl01(args[args.length - 2].hashCode(), 0);
			BeforeCrashServicePOATie servant = new BeforeCrashServicePOATie(beforeCrashServiceImpl);

			OAInterface.objectIsReady(servant);
			BeforeCrashService beforeCrashService = BeforeCrashServiceHelper.narrow(OAInterface.corbaReference(servant));

			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(beforeCrashService));

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