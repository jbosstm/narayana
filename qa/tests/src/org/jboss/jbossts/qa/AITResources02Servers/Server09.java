/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.AITResources02Servers;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.AITResources02.*;
import org.jboss.jbossts.qa.AITResources02Impls.AITCounterImpl03;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Server09
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			AITCounterImpl03 aitCounterImpl = new AITCounterImpl03();
			CounterPOATie servant = new CounterPOATie(aitCounterImpl);

			OAInterface.objectIsReady(servant);
			Counter aitCounter = CounterHelper.narrow(OAInterface.corbaReference(servant));

			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(aitCounter));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("Server09.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}