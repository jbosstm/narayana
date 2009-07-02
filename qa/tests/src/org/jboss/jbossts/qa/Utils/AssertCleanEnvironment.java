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

import com.arjuna.ats.arjuna.common.Environment;
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

			objectStoreDirName = arjPropertyManager.getPropertyManager().getProperty(
					Environment.OBJECTSTORE_DIR,
					com.arjuna.ats.arjuna.common.Configuration.objectStoreRoot());
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
