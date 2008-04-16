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

public class FileServerIORStore implements ServerIORStorePlugin
{
	public void initialise() throws Exception
	{
		// Ignore
	}

	public void storeIOR(String serverName, String serverIOR) throws Exception
	{
		Properties serverIORs = new Properties();

		try
		{
			FileInputStream serverIORsFileInputStream = new FileInputStream("ServerIORs");
			serverIORs.load(serverIORsFileInputStream);
			serverIORsFileInputStream.close();
		}
		catch (Exception exception)
		{
		}

		serverIORs.put(serverName, serverIOR);

		FileOutputStream serverIORsFileOutputStream = new FileOutputStream("ServerIORs");
		serverIORs.store(serverIORsFileOutputStream, "Server IORs");
		serverIORsFileOutputStream.close();
	}

	public void removeIOR(String serverName) throws Exception
	{
		Properties serverIORs = new Properties();

		FileInputStream serverIORsFileInputStream = new FileInputStream("ServerIORs");
		serverIORs.load(serverIORsFileInputStream);
		serverIORsFileInputStream.close();

		serverIORs.remove(serverName);

		FileOutputStream serverIORsFileOutputStream = new FileOutputStream("ServerIORs");
		serverIORs.store(serverIORsFileOutputStream, "Server IORs");
		serverIORsFileOutputStream.close();
	}

	public String loadIOR(String serverName) throws Exception
	{
		String serverIOR = null;

		Properties serverIORs = new Properties();

		FileInputStream serverIORsFileInputStream = new FileInputStream("ServerIORs");
		serverIORs.load(serverIORsFileInputStream);
		serverIORsFileInputStream.close();

		serverIOR = (String) serverIORs.get(serverName);

		return serverIOR;
	}

	public void remove()
	{
		try
		{
			File file = new File("ServerIORs");

			file.delete();
		}
		catch (Exception exception)
		{
			System.err.println("Failed to remove \"ServerIORs\": " + exception);
		}
	}
}
