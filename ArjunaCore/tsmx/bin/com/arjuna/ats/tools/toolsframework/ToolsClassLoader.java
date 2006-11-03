/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 * 
 * $Id: ToolsClassLoader.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.toolsframework;

import com.arjuna.ats.tools.toolsframework.plugin.ToolPluginInformation;
import com.arjuna.ats.tsmx.logging.tsmxLogger;

import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;

public class ToolsClassLoader implements FilenameFilter
{
	private final static String JAR_FILENAME_SUFFIX = ".jar";

	private	URLClassLoader	_urlLoader;
	private ArrayList 		_toolJars = new ArrayList();

	/**
	 * @message com.arjuna.ats.tools.toolsframework.ToolsClassLoader.invalidurl The URL is invalid: {0}
	 * @param toolsLibDirectory
	 */
	public ToolsClassLoader(File toolsLibDirectory)
	{
		/** Find all JAR files than contain a META-INF/tools.properties **/
		File[] jarFiles = toolsLibDirectory.listFiles(this);

		if ( jarFiles != null )
		{
			for (int count=0;count<jarFiles.length;count++)
			{
				try
				{
					ToolPluginInformation toolInfo = ToolPluginInformation.getToolPluginInformation(jarFiles[count]);

					/** Only add the tool info if the JAR is a tool JAR **/
					if ( toolInfo != null )
					{
						_toolJars.add( toolInfo );
					}
				}
				catch (Exception e)
				{
					// Ignore as JAR may not be a tool
				}
			}
		}

		URL[] urls = new URL[ _toolJars.size() + 1 ];

		for (int count=0;count<_toolJars.size();count++)
		{
			ToolPluginInformation info = ((ToolPluginInformation)_toolJars.get(count));
			try
			{
				urls[count] = info.getFilename().toURL();
			}
			catch (Exception e)
			{
				if ( tsmxLogger.loggerI18N.isErrorEnabled() )
				{
					tsmxLogger.loggerI18N.error("com.arjuna.ats.tools.toolsframework.ToolsClassLoader.invalidurl", new Object[] { info.getFilename() } );
				}
			}
		}

		try
		{
			urls[_toolJars.size()] = toolsLibDirectory.toURL();
		}
		catch (Exception e)
		{
			if ( tsmxLogger.loggerI18N.isErrorEnabled() )
			{
				tsmxLogger.loggerI18N.error("com.arjuna.ats.tools.toolsframework.ToolsClassLoader.invalidurl", new Object[] { toolsLibDirectory.toString() } );
			}
		}

		_urlLoader = new URLClassLoader(urls);
	}

	public ToolPluginInformation[] getToolsInformation()
	{
		ToolPluginInformation[] tools = new ToolPluginInformation[_toolJars.size()];
		_toolJars.toArray(tools);

		return tools;
	}

	public URL getResource(String name)
	{
		return _urlLoader.getResource(name);
	}

	public InputStream getResourceAsStream(String name)
	{
		return _urlLoader.getResourceAsStream(name);
	}

	public Class loadClass(String name) throws ClassNotFoundException
	{
		return _urlLoader.loadClass(name);
	}

	/**
	 * Tests if a specified file should be included in a file list.
	 *
	 * @param   dir    the directory in which the file was found.
	 * @param   name   the name of the file.
	 * @return  <code>true</code> if and only if the name should be
	 * included in the file list; <code>false</code> otherwise.
	 */
	public boolean accept(File dir, String name)
	{
		return name.endsWith(JAR_FILENAME_SUFFIX);
	}
}
