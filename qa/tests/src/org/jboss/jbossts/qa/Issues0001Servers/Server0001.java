/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.Issues0001Servers;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.Issues0001.*;
import org.jboss.jbossts.qa.Issues0001Impls.CounterImpl0001;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Server0001
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			CounterImpl0001 counterImpl = new CounterImpl0001();
			CounterPOATie servant = new CounterPOATie(counterImpl);

			OAInterface.objectIsReady(servant);
			Counter counter = CounterHelper.narrow(OAInterface.corbaReference(servant));

			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(counter));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("Server0001.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}