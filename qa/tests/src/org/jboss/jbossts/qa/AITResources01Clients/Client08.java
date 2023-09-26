/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

///////////////////////////////////////////////////////////////////////////////////////////
//

//
// File        : Client08.javatmpl (AITResources01)
//
// Description : Memory Test version of Client06 (ping pong test with no client transaction).
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

package org.jboss.jbossts.qa.AITResources01Clients;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.AITResources01.*;
import org.jboss.jbossts.qa.Utils.*;

public class Client08
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String pingerIOR = ServerIORStore.loadIOR(args[args.length - 5]);
			PingPong pinger = PingPongHelper.narrow(ORBInterface.orb().string_to_object(pingerIOR));

			String pongerIOR = ServerIORStore.loadIOR(args[args.length - 4]);
			PingPong ponger = PingPongHelper.narrow(ORBInterface.orb().string_to_object(pongerIOR));

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

			pinger.hit(numberOfCalls, pinger, pinger);
			ponger.hit(numberOfCalls, ponger, ponger);

			int clientMemory0 = (int) JVMStats.getMemory();
			int server1Memory0 = pinger.getMemory();
			int server2Memory0 = ponger.getMemory();

			for (int index = 0; index < numberOfCalls; index++)
			{
				pinger.hit(index, ponger, pinger);
			}

			int clientMemory1 = (int) JVMStats.getMemory();
			int server1Memory1 = pinger.getMemory();
			int server2Memory1 = ponger.getMemory();

			float clientMemoryIncrease = ((float) (clientMemory1 - clientMemory0)) / ((float) clientMemory0);
			float server1MemoryIncrease = ((float) (server1Memory1 - server1Memory0)) / ((float) server1Memory0);
			float server2MemoryIncrease = ((float) (server2Memory1 - server2Memory0)) / ((float) server2Memory0);

			System.err.println("Client memory increase threshold : " + (float) (100.0 * clientIncreaseThreshold) + "%");
			System.err.println("Server memory increase threshold : " + (float) (100.0 * serverIncreaseThreshold) + "%");

			System.err.println("Client   percentage memory increase: " + (float) (100.0 * clientMemoryIncrease) + "%");
			System.err.println("Client   memory increase per call  : " + (clientMemory1 - clientMemory0) / numberOfCalls);
			System.err.println("Server 1 percentage memory increase: " + (float) (100.0 * server1MemoryIncrease) + "%");
			System.err.println("Server 1 memory increase per call  : " + (server1Memory1 - server1Memory0) / numberOfCalls);
			System.err.println("Server 2 percentage memory increase: " + (float) (100.0 * server2MemoryIncrease) + "%");
			System.err.println("Server 2 memory increase per call  : " + (server2Memory1 - server2Memory0) / numberOfCalls);

			if ((clientMemoryIncrease < clientIncreaseThreshold) && (server1MemoryIncrease < serverIncreaseThreshold) && (server2MemoryIncrease < serverIncreaseThreshold))
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
			System.err.println("Client08.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client08.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}