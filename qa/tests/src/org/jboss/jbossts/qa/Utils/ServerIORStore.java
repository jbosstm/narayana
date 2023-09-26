/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.Utils;

public class ServerIORStore
{
	private final static String IOR_STORE_PLUGIN_CLASSNAME = "org.jboss.jbossts.qa.Utils.ServerIORStore.plugin";
	private final static String DEFAULT_IOR_STORE_PLUGIN_CLASSNAME = FileServerIORStore.class.getName();

	private static ServerIORStorePlugin _iorStore = null;

	public static void storeIOR(String serverName, String serverIOR) throws Exception
	{
		_iorStore.storeIOR(serverName, serverIOR);
	}

	public static void removeIOR(String serverName)
			throws Exception
	{
		_iorStore.removeIOR(serverName);
	}

	public static String loadIOR(String serverName)
			throws Exception
	{
		return _iorStore.loadIOR(serverName);
	}

	public static void remove()
	{
		_iorStore.remove();
	}

	static
	{
		try
		{
			String iorStoreClassname = System.getProperty(IOR_STORE_PLUGIN_CLASSNAME, DEFAULT_IOR_STORE_PLUGIN_CLASSNAME);

			_iorStore = (ServerIORStorePlugin) Class.forName(iorStoreClassname).getDeclaredConstructor().newInstance();

			_iorStore.initialise();
		}
		catch (Exception e)
		{
			throw new ExceptionInInitializerError("Failed to initialise IOR store plugin: " + e);
		}
	}
}