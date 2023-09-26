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
import org.jboss.jbossts.qa.AITResources01Impls.AITPingPongImpl01;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Server08
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			AITPingPongImpl01 aitPingPongImpl1 = new AITPingPongImpl01();
			AITPingPongImpl01 aitPingPongImpl2 = new AITPingPongImpl01();

			PingPongPOATie servant1 = new PingPongPOATie(aitPingPongImpl1);
			PingPongPOATie servant2 = new PingPongPOATie(aitPingPongImpl2);

			OAInterface.objectIsReady(servant1);
			OAInterface.objectIsReady(servant2);
			PingPong aitPingPong1 = PingPongHelper.narrow(OAInterface.corbaReference(servant1));
			PingPong aitPingPong2 = PingPongHelper.narrow(OAInterface.corbaReference(servant2));

			ServerIORStore.storeIOR(args[args.length - 2], ORBInterface.orb().object_to_string(aitPingPong1));
			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(aitPingPong2));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("Server08.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}