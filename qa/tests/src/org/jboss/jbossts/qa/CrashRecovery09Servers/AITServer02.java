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

public class AITServer02
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			Uid uid = ObjectUidStore.loadUid(args[args.length - 2]);

			AITServiceImpl01 aitServiceImpl = new AITServiceImpl01(uid);
			ServicePOATie servant = new ServicePOATie(aitServiceImpl);

			OAInterface.objectIsReady(servant);
			Service service = ServiceHelper.narrow(OAInterface.corbaReference(servant));

			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(service));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("AITServer02.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}