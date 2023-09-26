/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.Hammer02Servers;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.Hammer02.*;
import org.jboss.jbossts.qa.Hammer02Impls.AITMatrixImpl02;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Server02
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			AITMatrixImpl02 aitMatrixImpl = new AITMatrixImpl02(16, 16);
			MatrixPOATie servant = new MatrixPOATie(aitMatrixImpl);

			OAInterface.objectIsReady(servant);
			Matrix aitMatrix = MatrixHelper.narrow(OAInterface.corbaReference(servant));

			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(aitMatrix));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("Server02.main: " + exception);
		}
	}
}