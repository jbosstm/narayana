/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.Utils;

import com.arjuna.ats.arjuna.common.arjPropertyManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * This class acts as a main program which can be used to test
 * the state of the environment prior to running a test.
 * <p/>
 * At the moment this class tests:
 * <ul>
 * <li> The set of running processes
 * <li> That the ObjectStore is empty
 * </ul>
 */
public class AssertCleanEnvironment
{
	public static void main(String[] args)
	{
		try
		{
			checkRunningProcesses(args);
			checkObjectStore(args);
		}
		catch (Exception exception)
		{
			System.err.println("AssertCleanEnvironment.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}

	private static void checkRunningProcesses(String[] args)
			throws Exception
	{
		// Assume we are running in cygwin and have this command available
		Process p = Runtime.getRuntime().exec("ps -ef");
		BufferedReader br = new BufferedReader(
				new InputStreamReader(p.getInputStream()));
		String line = br.readLine();
		while (line != null)
		{
			System.err.println(line);
			line = br.readLine();
		}
	}

	private static void checkObjectStore(String[] args)
			throws Exception
	{
		String objectStoreDirName = null;
		try
		{
			// We have to init the orb etc to make sure we get the
			// latest version of the property and not some old value.
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			objectStoreDirName = arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreDir();
		}
		finally
		{
			// Try and tidy up
			try
			{
				OAInterface.shutdownOA();
				ORBInterface.shutdownORB();
			}
			catch (Exception exception)
			{
				// No need to report tidy up errors
			}
		}

		File objectStoreDir = null;
		if (objectStoreDirName != null)
		{
			objectStoreDir = new File(objectStoreDirName);
		}

		if ((objectStoreDir != null) &&
				objectStoreDir.isDirectory() &&
				(!objectStoreDir.getName().equals("")) &&
				(!objectStoreDir.getName().equals("/")) &&
				(!objectStoreDir.getName().equals("\\")) &&
				(!objectStoreDir.getName().equals(".")) &&
				(!objectStoreDir.getName().equals("..")))
		{
			File[] contents = objectStoreDir.listFiles();
			if (contents != null && contents.length != 0)
			{
				reportError("OBJECTSTORE is not empty");
			}
		}
		else
		{
			throw new IllegalStateException("ArjunaCoreEnvironment.OBJECTSTORE_DIR is invalid");
		}
	}

	private static void reportError(String text)
	{
		System.err.println("AssertCleanEnvironment:!!!! " + text);
	}
}