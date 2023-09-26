/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.RawSubtransactionAwareResources02Servers;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.RawSubtransactionAwareResources02.*;
import org.jboss.jbossts.qa.RawSubtransactionAwareResources02Impls.ServiceImpl01;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Server03
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			ServiceImpl01 serviceImpl1 = new ServiceImpl01(0);
			ServiceImpl01 serviceImpl2 = new ServiceImpl01(1);
			ServiceImpl01 serviceImpl3 = new ServiceImpl01(2);

			ServicePOATie servant1 = new ServicePOATie(serviceImpl1);
			ServicePOATie servant2 = new ServicePOATie(serviceImpl2);
			ServicePOATie servant3 = new ServicePOATie(serviceImpl3);

			OAInterface.objectIsReady(servant1);
			OAInterface.objectIsReady(servant2);
			OAInterface.objectIsReady(servant3);
			Service service1 = ServiceHelper.narrow(OAInterface.corbaReference(servant1));
			Service service2 = ServiceHelper.narrow(OAInterface.corbaReference(servant2));
			Service service3 = ServiceHelper.narrow(OAInterface.corbaReference(servant3));

			ServerIORStore.storeIOR(args[args.length - 3], ORBInterface.orb().object_to_string(service1));
			ServerIORStore.storeIOR(args[args.length - 2], ORBInterface.orb().object_to_string(service2));
			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(service3));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("Server03.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}