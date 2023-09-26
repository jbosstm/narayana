/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

///////////////////////////////////////////////////////////////////////////////////////////
//

//
// File        : Client03.javatmpl (AITResources02)
//
// Description : Memory Test version of Client01 (counter increase & check - no client trans).
//
//               Client performs a specified number of remote calls before
//               the memory growth is checked. If client or server memory growth
//               exceeds specified parameters then the test fails and "Failed" is output.
//               Otherwise "Passed" is output.
//
// Author      : Stewart Wheater
//
// History     : 1.0   25 Feb 2000  S Wheater       Creation.
//               1.1   07 Jul 2001  M Buckingham    Added facility to use client/server
//                                                  thresholds in config file
//                                                  MemoryTestProfile.
//
///////////////////////////////////////////////////////////////////////////////////////////

package org.jboss.jbossts.qa.AITResources02Clients;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.AITResources02.*;
import org.jboss.jbossts.qa.Utils.*;

public class Client03
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String counterIOR = ServerIORStore.loadIOR(args[args.length - 4]);
			Counter counter = CounterHelper.narrow(ORBInterface.orb().string_to_object(counterIOR));

			int numberOfCalls = Integer.parseInt(args[args.length - 3]);

			float clientIncreaseThreshold;
			float serverIncreaseThreshold;

			// If no threshold value then use default.
			if (MemoryTestProfileStore.getNoThresholdValue().equals(args[args.length - 2]))
			{
				clientIncreaseThreshold = Float.parseFloat(MemoryTestProfileStore.getDefaultClientIncreaseThreshold());
			}
			else // Use passed threshold
			{
				clientIncreaseThreshold = Float.parseFloat(args[args.length - 2]);
			}

			// If no threshold value then use default.
			if (MemoryTestProfileStore.getNoThresholdValue().equals(args[args.length - 1]))
			{
				serverIncreaseThreshold = Float.parseFloat(MemoryTestProfileStore.getDefaultServerIncreaseThreshold());
			}
			else // Use passed threshold
			{
				serverIncreaseThreshold = Float.parseFloat(args[args.length - 1]);
			}

			counter.increase(null);

			int clientMemory0 = (int) JVMStats.getMemory();
			int serverMemory0 = counter.getMemory();

			for (int index = 0; index < numberOfCalls; index++)
			{
				counter.increase(null);
			}

			int clientMemory1 = (int) JVMStats.getMemory();
			int serverMemory1 = counter.getMemory();

			float clientMemoryIncrease = ((float) (clientMemory1 - clientMemory0)) / ((float) clientMemory0);
			float serverMemoryIncrease = ((float) (serverMemory1 - serverMemory0)) / ((float) serverMemory0);

			System.err.println("Client memory increase threshold : " + (float) (100.0 * clientIncreaseThreshold) + "%");
			System.err.println("Server memory increase threshold : " + (float) (100.0 * serverIncreaseThreshold) + "%");

			System.err.println("Client percentage memory increase: " + (float) (100.0 * clientMemoryIncrease) + "%");
			System.err.println("Client memory increase per call  : " + (clientMemory1 - clientMemory0) / numberOfCalls);
			System.err.println("Server percentage memory increase: " + (float) (100.0 * serverMemoryIncrease) + "%");
			System.err.println("Server memory increase per call  : " + (serverMemory1 - serverMemory0) / numberOfCalls);

			if ((clientMemoryIncrease < clientIncreaseThreshold) && (serverMemoryIncrease < serverIncreaseThreshold))
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
			System.err.println("Client03.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client03.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}