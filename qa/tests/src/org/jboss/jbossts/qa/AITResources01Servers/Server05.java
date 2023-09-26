/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.AITResources01Servers;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.AITResources01.*;
import org.jboss.jbossts.qa.AITResources01Impls.AITCounterImpl01;
import org.jboss.jbossts.qa.AITResources01Impls.AITCounterImpl02;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Server05
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			AITCounterImpl01 aitCounterImpl1 = new AITCounterImpl01();
			AITCounterImpl02 aitCounterImpl2 = new AITCounterImpl02();

			CounterPOATie servant1 = new CounterPOATie(aitCounterImpl1);
			CounterPOATie servant2 = new CounterPOATie(aitCounterImpl2);

			OAInterface.objectIsReady(servant1);
			OAInterface.objectIsReady(servant2);
			Counter aitCounter1 = CounterHelper.narrow(OAInterface.corbaReference(servant1));
			Counter aitCounter2 = CounterHelper.narrow(OAInterface.corbaReference(servant2));

			ServerIORStore.storeIOR(args[args.length - 2], ORBInterface.orb().object_to_string(aitCounter1));
			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(aitCounter2));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("Server05.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}