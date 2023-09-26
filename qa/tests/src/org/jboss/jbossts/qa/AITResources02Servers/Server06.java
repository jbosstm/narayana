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
import org.jboss.jbossts.qa.AITResources02Impls.AITCounterImpl01;
import org.jboss.jbossts.qa.AITResources02Impls.AITCounterImpl02;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Server06
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			AITCounterImpl01 aitCounterImpl1 = new AITCounterImpl01();
			AITCounterImpl02 aitCounterImpl2 = new AITCounterImpl02();
			AITCounterImpl01 aitCounterImpl3 = new AITCounterImpl01();
			AITCounterImpl02 aitCounterImpl4 = new AITCounterImpl02();

			CounterPOATie servant1 = new CounterPOATie(aitCounterImpl1);
			CounterPOATie servant2 = new CounterPOATie(aitCounterImpl2);
			CounterPOATie servant3 = new CounterPOATie(aitCounterImpl3);
			CounterPOATie servant4 = new CounterPOATie(aitCounterImpl4);

			OAInterface.objectIsReady(servant1);
			OAInterface.objectIsReady(servant2);
			OAInterface.objectIsReady(servant3);
			OAInterface.objectIsReady(servant4);
			Counter aitCounter1 = CounterHelper.narrow(OAInterface.corbaReference(servant1));
			Counter aitCounter2 = CounterHelper.narrow(OAInterface.corbaReference(servant2));
			Counter aitCounter3 = CounterHelper.narrow(OAInterface.corbaReference(servant3));
			Counter aitCounter4 = CounterHelper.narrow(OAInterface.corbaReference(servant4));

			ServerIORStore.storeIOR(args[args.length - 4], ORBInterface.orb().object_to_string(aitCounter1));
			ServerIORStore.storeIOR(args[args.length - 3], ORBInterface.orb().object_to_string(aitCounter2));
			ServerIORStore.storeIOR(args[args.length - 2], ORBInterface.orb().object_to_string(aitCounter3));
			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(aitCounter4));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("Server06.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}