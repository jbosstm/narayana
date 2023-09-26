/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.PerfProfile01Clients;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.PerfProfile01.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.PerformanceProfileStore;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

import java.util.Date;

public class Client_ExplicitObject_NoTran_TranRollbackReadLock
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String prefix = args[args.length - 3];
			int numberOfCalls = Integer.parseInt(args[args.length - 2]);
			String explicitObjectIOR = ServerIORStore.loadIOR(args[args.length - 1]);

			ExplicitObject explicitObject = ExplicitObjectHelper.narrow(ORBInterface.orb().string_to_object(explicitObjectIOR));

			boolean correct = true;

			Date start = new Date();

			for (int index = 0; index < numberOfCalls; index++)
			{
				explicitObject.tran_rollback_readlock(null);
			}

			Date end = new Date();

			float operationDuration = ((float) (end.getTime() - start.getTime())) / ((float) numberOfCalls);

			System.err.println("Operation duration       : " + operationDuration + "ms");
			System.err.println("Test duration            : " + (end.getTime() - start.getTime()) + "ms");

			correct = PerformanceProfileStore.checkPerformance(prefix + "_ExplicitObject_NoTran_TranRollbackReadLock", operationDuration);

			if (correct)
			{
				System.out.println("Passed");
			}
			else
			{
				System.out.println("Failed");
			}
		}
		catch (Exception exception)
		{
			System.out.println("Failed");
			System.err.println("Client_ExplicitObject_NoTran_TranRollbackReadLock.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client_ExplicitObject_NoTran_TranRollbackReadLock.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}