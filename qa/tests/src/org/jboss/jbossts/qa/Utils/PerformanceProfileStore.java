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

package org.jboss.jbossts.qa.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class PerformanceProfileStore
{
	private final static String BASE_DIRECTORY_PROPERTY = "performanceprofilestore.dir";

	public static boolean checkPerformance(String performanceName, float operationDuration)
	{
		boolean correct;

		try
		{
			float expectedOperationDuration = loadPerformance(performanceName);

			System.out.println("Operation duration change: " + ((float) (100.0 * (operationDuration - expectedOperationDuration) / expectedOperationDuration)) + "%");

			correct = (operationDuration < (1.1 * expectedOperationDuration));
		}
		catch (Exception exception1)
		{
			System.err.println("checkPerformance: " + exception1);
			exception1.printStackTrace(System.err);
			try
			{
				storePerformance(performanceName, operationDuration);

				correct = false;
			}
			catch (Exception exception2)
			{
				System.err.println("checkPerformance: " + exception2);
				exception2.printStackTrace(System.err);
				correct = false;
			}
		}

		return correct;
	}

	private static void storePerformance(String performanceName, float operationDuration)
			throws Exception
	{
		Properties performanceProfile = new Properties();

		try
		{
			FileInputStream performanceProfileFileInputStream = new FileInputStream(getBaseDir() + File.separator + "PerformanceProfiles");
			performanceProfile.load(performanceProfileFileInputStream);
			performanceProfileFileInputStream.close();
		}
		catch (Exception exception)
		{
			System.err.println("storePerformance: " + exception);
			exception.printStackTrace(System.err);
		}

		performanceProfile.put(performanceName, Float.toString(operationDuration));

		FileOutputStream performanceProfileFileOutputStream = new FileOutputStream(getBaseDir() + File.separator + "PerformanceProfiles");
		performanceProfile.store(performanceProfileFileOutputStream, "Performance profile (time in milli-seconds)");
		performanceProfileFileOutputStream.close();
	}

	private static float loadPerformance(String performanceName)
			throws Exception
	{
		float operationDuration = (float) 0.0;

		Properties performanceProfile = new Properties();

		FileInputStream performanceProfileFileInputStream = new FileInputStream(getBaseDir() + File.separator + "PerformanceProfiles");
		performanceProfile.load(performanceProfileFileInputStream);
		performanceProfileFileInputStream.close();

		operationDuration = Float.parseFloat((String) performanceProfile.get(performanceName));

		return operationDuration;
	}

	private static void remove()
	{
		try
		{
			File file = new File(getBaseDir() + File.separator + "PerformanceProfiles");

			file.delete();
		}
		catch (Exception exception)
		{
			System.err.println("Failed to remove \"perf_profile" + File.separator + "PerformanceProfiles\": " + exception);
			exception.printStackTrace(System.err);
		}
	}

	private static String getBaseDir() throws Exception
	{
		String baseDir = System.getProperty(BASE_DIRECTORY_PROPERTY);

		if (baseDir == null)
		{
			throw new Exception(BASE_DIRECTORY_PROPERTY + " property not set - cannot find performance test profiles!");
		}

		return baseDir;
	}
}
