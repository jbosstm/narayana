/*
 * SPDX short identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.PerfProfile01Servers;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.PerfProfile01.*;
import org.jboss.jbossts.qa.PerfProfile01Impls.AITExplicitObjectImpl01;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Server_AIT01_ExplicitObject
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			AITExplicitObjectImpl01 aitExplicitObjectImpl = new AITExplicitObjectImpl01();
			ExplicitObjectPOATie servant = new ExplicitObjectPOATie(aitExplicitObjectImpl);

			OAInterface.objectIsReady(servant);
			ExplicitObject aitExplicitObject = ExplicitObjectHelper.narrow(OAInterface.corbaReference(servant));

			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(aitExplicitObject));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("Server_AIT01_ExplicitObject.main: " + exception);
		}
	}
}