/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.PerfProfile01Servers;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.PerfProfile01.*;
import org.jboss.jbossts.qa.PerfProfile01Impls.AITImplicitObjectImpl01;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Server_AIT01_ImplicitObject
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			AITImplicitObjectImpl01 aitImplicitObjectImpl = new AITImplicitObjectImpl01();
			ImplicitObjectPOATie servant = new ImplicitObjectPOATie(aitImplicitObjectImpl);

			OAInterface.objectIsReady(servant);
			ImplicitObject aitImplicitObject = ImplicitObjectHelper.narrow(OAInterface.corbaReference(servant));

			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(aitImplicitObject));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("Server_AIT01_ImplicitObject.main: " + exception);
		}
	}
}