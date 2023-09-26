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

public class Server07
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			AITPingPongImpl01 aitPingPongImpl = new AITPingPongImpl01();
			PingPongPOATie servant = new PingPongPOATie(aitPingPongImpl);

			OAInterface.objectIsReady(servant);
			PingPong aitPingPong = PingPongHelper.narrow(OAInterface.corbaReference(servant));

			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(aitPingPong));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("Server07.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}