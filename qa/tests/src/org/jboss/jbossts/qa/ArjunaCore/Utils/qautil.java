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
