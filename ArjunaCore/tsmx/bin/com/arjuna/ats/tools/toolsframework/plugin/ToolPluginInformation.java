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
 * $Id: ToolPluginInformation.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.toolsframework.plugin;

import java.io.*;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.*;
import java.util.zip.ZipEntry;
import java.net.URL;

public class ToolPluginInformation
{
	private final static String META_INFO_TOOLS_INFORMATION_FILENAME = "META-INF/tools.properties";
	private final static String TOOL_CLASSNAME_PROPERTY_NAME = "tool.classname";
	private final static String TOOL_ICON16_PROPERTY_NAME = "tool.icon.16";
	private final static String TOOL_ICON32_PROPERTY_NAME = "tool.icon.32";

	private String[]	_classnames = null;
	private Properties	_properties = null;
	private URL		    _fileURL = null;
    private String		_icon16 = null;
	private String		_icon32 = null;

	private ToolPluginInformation(URL fileURL, Properties toolInfo)
	{
		/** Parse the ; delimited classname list **/
		ArrayList classnames = new ArrayList();

		/** Create a copy of the tool properties **/
		Properties props = new Properties();
		props.putAll(toolInfo);

		/** Retrieve the tool classname from the properties **/
    	String classname = props.getProperty( TOOL_CLASSNAME_PROPERTY_NAME );

		/** Retrieve the icon names **/
        _icon16 = props.getProperty( TOOL_ICON16_PROPERTY_NAME );
		_icon32 = props.getProperty( TOOL_ICON32_PROPERTY_NAME );

        StringTokenizer st = new StringTokenizer(classname,";");
		while (st.hasMoreElements())
		{
			classnames.add( st.nextElement() );
		}

		_classnames = new String[classnames.size()];
		classnames.toArray(_classnames);

		/** Remove the mandatory properties **/
		props.remove( TOOL_CLASSNAME_PROPERTY_NAME );

		_properties = props;
		_fileURL = fileURL;
	}

	/**
	 * Get the name of the 16x16 icon
	 * @return
	 */
	public final String getIcon16()
	{
		return _icon16;
	}

	/**
	 * Get the name of the 32x32 icon
	 * @return
	 */
	public final String getIcon32()
	{
		return _icon32;
	}

	/**
	 * Get the classname of the tool.
	 * @return
	 */
	public final String[] getClassnames()
	{
		return _classnames;
	}

	/**
	 * Get the properties to be used by this tool.
	 * @return
	 */
	public final Properties getProperties()
	{
		return _properties;
	}

	/**
	 * Get the name of the tool's JAR file.
	 * @return
	 */
	public final URL getFileURL()
	{
		return _fileURL;
	}

	/**
	 * Searches the given JAR file for the tools information file and creates a tool plugin information
	 * wrapper for it.
	 *
     * @param plugins collection for returning the desired plugin wrapper for the tool jar file
	 * @param jar The JAR file to search.
	 * @return the url of the JAR file.
	 * @throws IOException
	 */
	public static URL getToolPluginInformation(Collection<ToolPluginInformation> plugins, File jar) throws IOException
	{
			JarFile jFile = new JarFile(jar);

			Enumeration entries = jFile.entries();

			while ( entries.hasMoreElements() )
			{
				JarEntry entry = (JarEntry)entries.nextElement();

				if ( entry.getName().equals( META_INFO_TOOLS_INFORMATION_FILENAME ) )
				{
					Properties toolProps = new Properties();
					toolProps.load( jFile.getInputStream(entry) );
                    plugins.add(new ToolPluginInformation(jar.toURL(), toolProps));

                    break;
				}
			}

			jFile.close();

            return jar.toURL();
	}
    
    public static URL toPlugin(Collection<ToolPluginInformation> plugins, String tmpDir, InputStream is, ZipEntry ze) throws IOException
    {
        return getToolPluginInformation(plugins, externalizeFile(tmpDir, is, ze));  // TODO clean up when done
    }

    public static File externalizeFile(String fname, InputStream is) throws IOException
    {
        File f = new File(fname);
        OutputStream out = new FileOutputStream(f);
        byte[] buf = new byte[1024];
        int len;

        while ((len = is.read(buf)) > 0)
            out.write(buf, 0, len);

        out.close();

        return f;
    }

    public static File externalizeFile(String tmpDir, InputStream is, ZipEntry ze) throws IOException
    {
        File f = new File(tmpDir + File.separatorChar + ze.getName());
        
        if (!ze.isDirectory())
        {
            int i = ze.getName().lastIndexOf(File.separatorChar);

            if (i != -1) {
				// this file name is part of a directory path - create the parent directories
                String path = f.getPath();
				String sp = path.substring(0, path.lastIndexOf(File.separatorChar));
				new File(sp).mkdirs();
			}

            OutputStream out = new FileOutputStream(f);
            byte[] buf = new byte[1024];
            int len;

            while ((len = is.read(buf)) > 0)
                out.write(buf, 0, len);

            out.close();
        }
        else
            f.mkdirs();

        return f;
    }
}
