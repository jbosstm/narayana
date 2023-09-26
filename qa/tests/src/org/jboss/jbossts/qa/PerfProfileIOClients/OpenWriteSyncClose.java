/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.PerfProfileIOClients;

import org.jboss.jbossts.qa.Utils.PerformanceProfileStore;

import java.io.File;
import java.io.FileDescriptor;
import java.io.RandomAccessFile;
import java.util.Date;

public class OpenWriteSyncClose
{
	public static void main(String[] args)
	{
		try
		{
			String prefix = args[args.length - 3];
			int numberOfCalls = Integer.parseInt(args[args.length - 2]);
			int blockSize = Integer.parseInt(args[args.length - 1]);

			boolean correct = true;

			File file = new File("test.tmp");
			byte[] block = new byte[blockSize];

			Date start = new Date();

			for (int index = 0; index < numberOfCalls; index++)
			{
				RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
				FileDescriptor fileDescriptor = randomAccessFile.getFD();

				randomAccessFile.write(block);
				fileDescriptor.sync();
				randomAccessFile.close();
			}

			Date end = new Date();

			file.delete();

			float operationDuration = ((float) (end.getTime() - start.getTime())) / ((float) numberOfCalls);

			System.out.println("Operation duration       : " + operationDuration + "ms");
			System.out.println("Test duration            : " + (end.getTime() - start.getTime()) + "ms");

			correct = PerformanceProfileStore.checkPerformance(prefix + "_OpenWriteSyncClose", operationDuration);

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
			System.err.println("OpenWriteSyncClose.main: " + exception);
			System.out.println("Failed");
		}
	}
}