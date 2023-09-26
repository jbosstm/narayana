/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.CrashRecovery09Servers;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import com.arjuna.ats.arjuna.common.Uid;
import org.jboss.jbossts.qa.CrashRecovery09.*;
import org.jboss.jbossts.qa.CrashRecovery09Impls.AITServiceImpl01;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ObjectUidStore;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class AITServer04
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			Uid uid1 = ObjectUidStore.loadUid(args[args.length - 4]);
			Uid uid2 = ObjectUidStore.loadUid(args[args.length - 3]);

			AITServiceImpl01 aitServiceImpl1 = new AITServiceImpl01(uid1);
			AITServiceImpl01 aitServiceImpl2 = new AITServiceImpl01(uid2);

			ServicePOATie servant1 = new ServicePOATie(aitServiceImpl1);
			ServicePOATie servant2 = new ServicePOATie(aitServiceImpl2);

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
			System.err.println("AITServer04.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}