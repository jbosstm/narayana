/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
//
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
//
// Arjuna Technologies Ltd.,
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//

package org.jboss.jbossts.qa.PerfProfileIOClients;

import org.jboss.jbossts.qa.Utils.PerformanceProfileStore;

import java.io.File;
import java.io.FileDescriptor;
import java.io.RandomAccessFile;
import java.util.Date;

public class WriteSync
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
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
			FileDescriptor fileDescriptor = randomAccessFile.getFD();
			byte[] block = new byte[blockSize];

			Date start = new Date();

			for (int index = 0; index < numberOfCalls; index++)
			{
				randomAccessFile.write(block);
				fileDescriptor.sync();
			}

			Date end = new Date();

			randomAccessFile.close();
			file.delete();

			float operationDuration = ((float) (end.getTime() - start.getTime())) / ((float) numberOfCalls);

			System.out.println("Operation duration       : " + operationDuration + "ms");
			System.out.println("Test duration            : " + (end.getTime() - start.getTime()) + "ms");

			correct = PerformanceProfileStore.checkPerformance(prefix + "_WriteSync", operationDuration);

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
			System.err.println("WriteSync.main: " + exception);
			System.out.println("Failed");
		}
	}
}
