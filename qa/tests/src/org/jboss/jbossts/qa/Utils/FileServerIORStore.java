/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


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