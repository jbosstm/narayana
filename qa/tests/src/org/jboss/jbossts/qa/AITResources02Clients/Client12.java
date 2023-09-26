/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

///////////////////////////////////////////////////////////////////////////////////////////
//

//
// File        : Client12.javatmpl (AITResources02)
//
// Description : Memory Test version of Client10 (ping pong test with no client transaction).
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

public class Client12
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String pingPongIOR = ServerIORStore.loadIOR(args[args.length - 4]);
			PingPong pingPong = PingPongHelper.narrow(ORBInterface.orb().string_to_object(pingPongIOR));

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

			pingPong.bad_hit(0, 0, pingPong, pingPong, null);

			int clientMemory0 = (int) JVMStats.getMemory();
			int serverMemory0 = pingPong.getMemory();

			for (int index0 = 0; index0 < numberOfCalls; index0++)
			{
				for (int index1 = 0; index1 <= index0; index1++)
				{
					pingPong.bad_hit(index0, index1, pingPong, pingPong, null);
				}
			}

			int clientMemory1 = (int) JVMStats.getMemory();
			int serverMemory1 = pingPong.getMemory();

			float clientMemoryIncrease = ((float) (clientMemory1 - clientMemory0)) / ((float) clientMemory0);
			float serverMemoryIncrease = ((float) (serverMemory1 - serverMemory0)) / ((float) serverMemory0);

			System.err.println("Client memory increase threshold : " + (float) (100.0 * clientIncreaseThreshold) + "%");
			System.err.println("Server memory increase threshold : " + (float) (100.0 * serverIncreaseThreshold) + "%");

			System.err.println("Client percentage memory increase: " + (float) (100.0 * clientMemoryIncrease) + "%");
			System.err.println("Client memory increase per call  : " + (clientMemory1 - clientMemory0) / numberOfCalls);
			System.err.println("Server percentage memory increase: " + (float) (100.0 * serverMemoryIncrease) + "%");
			System.err.println("Server memory increase per call  : " + (serverMemory1 - serverMemory0) / numberOfCalls);

			if ((clientMemoryIncrease < clientIncreaseThreshold) && (serverMemoryIncrease < clientIncreaseThreshold))
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
			System.err.println("Client12.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client12.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}