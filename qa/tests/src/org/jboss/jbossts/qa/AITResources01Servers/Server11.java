/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

/*
*
* This server object has been created to test jiterbug issue 264
*
* The class is a copy of server01 but the remote object registered with
* the ORB is impl04
*						# Author P.Craddock
*						# 09/08/01
*/
package org.jboss.jbossts.qa.AITResources01Servers;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.AITResources01.*;
import org.jboss.jbossts.qa.AITResources01Impls.AITCounterImpl04;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Server11
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			AITCounterImpl04 aitCounterImpl = new AITCounterImpl04();
			CounterPOATie servant = new CounterPOATie(aitCounterImpl);

			OAInterface.objectIsReady(servant);
			Counter aitCounter = CounterHelper.narrow(OAInterface.corbaReference(servant));

			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(aitCounter));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("Server01.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}