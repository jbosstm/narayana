/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

///////////////////////////////////////////////////////////////////////////////////////////
//

//
// File        : Client15.javatmpl (AITResources02)
//
// Description : Memory Test version of Client14.
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

public class Client15
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String counterIOR = ServerIORStore.loadIOR(args[args.length - 5]);
			Counter counter = CounterHelper.narrow(ORBInterface.orb().string_to_object(counterIOR));

			int numberOfWorkers = Integer.parseInt(args[args.length - 4]);
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

			Worker[] workers = new Worker[numberOfWorkers];

			for (int index = 0; index < workers.length; index++)
			{
				workers[index] = new Worker(numberOfCalls, counter);
			}

			for (int index = 0; index < workers.length; index++)
			{
				workers[index].start();
			}

			boolean correct = true;

			for (int index = 0; index < workers.length; index++)
			{
				workers[index].join();
				correct = correct && workers[index].isCorrect();
				workers[index] = null;
			}
			workers = null;

			int clientMemory1 = (int) JVMStats.getMemory();
			int serverMemory1 = counter.getMemory();

			float clientMemoryIncrease = ((float) (clientMemory1 - clientMemory0)) / ((float) clientMemory0);
			float serverMemoryIncrease = ((float) (serverMemory1 - serverMemory0)) / ((float) serverMemory0);

			System.err.println("Client memory increase threshold : " + (float) (100.0 * clientIncreaseThreshold) + "%");
			System.err.println("Server memory increase threshold : " + (float) (100.0 * serverIncreaseThreshold) + "%");

			System.err.println("Client percentage memory increase: " + (float) (100.0 * clientMemoryIncrease) + "%");
			System.err.println("Client memory increase per call  : " + (clientMemory1 - clientMemory0) / (numberOfCalls * numberOfWorkers));
			System.err.println("Server percentage memory increase: " + (float) (100.0 * serverMemoryIncrease) + "%");
			System.err.println("Server memory increase per call  : " + (serverMemory1 - serverMemory0) / (numberOfCalls * numberOfWorkers));

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
			System.err.println("Client15.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client15.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}

	private static class Worker extends Thread
	{
		public Worker(int numberOfCalls, Counter counter)
		{
			_numberOfCalls = numberOfCalls;
			_counter = counter;
		}

		public void run()
		{
			try
			{
				int index = 0;
				while (index < _numberOfCalls)
				{
					try
					{
						_counter.increase(null);
						index++;
					}
					catch (InvocationException invocationException)
					{
					}
				}
			}
			catch (Exception exception)
			{
				System.err.println("Client15.Worker.run: " + exception);
				exception.printStackTrace(System.err);
				_correct = false;
			}
		}

		public boolean isCorrect()
		{
			return _correct;
		}

		private boolean _correct = true;
		private int _numberOfCalls;
		private Counter _counter = null;
	}
}