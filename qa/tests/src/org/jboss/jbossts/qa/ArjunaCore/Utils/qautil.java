/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.Utils;

import com.arjuna.ats.arjuna.common.Uid;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

import java.io.File;

public class qautil
{
	public static void storeUid(String objectName, Uid objectUid)
			throws Exception
	{
		ServerIORStore.storeIOR(objectName, objectUid.toString());
	}

	public static void clearUid(String objectName)
			throws Exception
	{
		ServerIORStore.removeIOR(objectName);
	}

	public static Uid loadUid(String objectName)
			throws Exception
	{
		Uid objectUid = new Uid(ServerIORStore.loadIOR(objectName));

		return objectUid;
	}

	public static void remove()
	{
		try
		{
			File file = new File("ObjectUids");

			file.delete();
		}
		catch (Exception exception)
		{
			System.err.println("Failed to remove \"ObjectUids\": " + exception);
		}
	}

	public static void debug(String s)
	{
		debug(s, null);
	}

	public static void debug(String s, Exception e)
	{
		if (s != null)
		{
			if (e != null)
			{
				System.err.println(s + " " + e);
				e.printStackTrace();
			}
			else
			{
				System.err.println(s);
			}
		}
	}

	/**
	 * Simple static method used for debug output that can be turned on if any errors occur.
	 * <p/>
	 * This can be turned on then recompiled or the -Dqa.debug=true can be used
	 * at runtime.
	 */

	public static void qadebug(String s)
	{
		qadebug(s, true);
	}

	public static void qadebug(String s, boolean newln)
	{
		if (debug)
		{
			if (newln)
			{
				System.err.println(s);
			}
			else
			{
				System.err.print(s);
			}
		}
	}

	public static boolean debug;

	static
	{
		String isDebugOn = System.getProperty("qa.debug");

		debug = isDebugOn != null ? isDebugOn.equals("true") : false;
	}
}