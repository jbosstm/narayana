/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.CrashRecovery09Servers;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery09.*;
import org.jboss.jbossts.qa.CrashRecovery09Impls.AITServiceImpl01;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ObjectUidStore;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class AITServer01
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			AITServiceImpl01 aitServiceImpl = new AITServiceImpl01();
			ServicePOATie servant = new ServicePOATie(aitServiceImpl);

			OAInterface.objectIsReady(servant);
			Service service = ServiceHelper.narrow(OAInterface.corbaReference(servant));

			ObjectUidStore.storeUid(args[args.length - 2], aitServiceImpl.get_uid());
			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(service));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("AITServer01.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}