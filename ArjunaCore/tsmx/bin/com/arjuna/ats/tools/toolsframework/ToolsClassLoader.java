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
import java.net.MalformedURLException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Collection;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

/**
 * Tool specific class loader that knows how to load classes from a tool plugin
 * distributed as a sar or as a directory hierarchy
 */
public class ToolsClassLoader extends URLClassLoader implements FilenameFilter
{
	private final static String JAR_FILENAME_SUFFIX = ".jar";
    private final static String JBOSS_TMP_DIR_PROP = "jboss.server.temp.dir";
    private final static String TMP_DIR = "jboss_tools_tmp";
    private final static String TOOLS_DIR = "tools/";
    private final static String DEFAULT_LIB_DIRECTORY = "tools";

    private	URLClassLoader	_urlLoader;
	private ArrayList<ToolPluginInformation> _toolJars = new ArrayList<ToolPluginInformation> ();
    private URL toolsDir;

    public ToolsClassLoader(URL[] urls)
    {
        super(urls, Thread.currentThread().getContextClassLoader());

        init();
    }

    public ToolsClassLoader()
    {
        this(new URL[0]);
    }

    private void init()
    {
        String libDir = System.getProperty("com.arjuna.mw.ArjunaToolsFramework.lib");
        String sarPath;

        if (libDir != null)
        {
            processDir(libDir);
        }
        else if ((sarPath = getSarPath()) != null)
        {
            processSar(sarPath);
        }
        else
        {
            URL url = Thread.currentThread().getContextClassLoader().getResource(DEFAULT_LIB_DIRECTORY);

            if (url != null && new File(url.getFile()).exists())
                processDir(url.getFile());
            else
                throw new RuntimeException("Unable to locate any TM tools");
        }

        _urlLoader = this;
    }

    private void processSar(String sarFileName)
    {
        try {
            ZipFile zf = new ZipFile(sarFileName);
            Enumeration<? extends ZipEntry> entries = zf.entries();
            String tmpDir = getTmpDir();
            Collection<URL> urls = new ArrayList<URL> ();

            while (entries.hasMoreElements())
            {
                ZipEntry ze = entries.nextElement();
                String fname = tmpDir + '/' + ze.getName();

                if (ze.isDirectory())
                {
                    new File(fname).mkdirs();
                }
                else
                {
                    File f = ToolPluginInformation.externalizeFile(fname, zf.getInputStream(ze));

                    if (accept(ze.getName()))
                        addURL(ToolPluginInformation.getToolPluginInformation(_toolJars, f));
//                        urls.add(ToolPluginInformation.getToolPluginInformation(_toolJars, f));
                }
            }

            addURL(new File(getTmpDir()).toURL());
//            urls.add(new File(getTmpDir()).toURL());
            toolsDir = new File(getTmpDir() + DEFAULT_LIB_DIRECTORY).toURL();

//            setClassLoader(urls, getTmpDir() + DEFAULT_LIB_DIRECTORY);

            zf.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unable to unpack tools sar: " + e.getMessage());
        }
    }

    /**
	 * @message com.arjuna.ats.tools.toolsframework.ToolsClassLoader.invalidjar Error reading tool jar: {0}
	 * @param toolsDirectory
	 */
    private void processDir(String toolsDirectory)
	{
        File toolsLibDirectory = new File(toolsDirectory);

        if (!toolsLibDirectory.isDirectory())
            return;

        /** Find all JAR files that contain a META-INF/tools.properties **/
        Collection<URL> urls = new ArrayList<URL> ();

        for (File jar : toolsLibDirectory.listFiles(this))
        {
            try
            {
                addURL(ToolPluginInformation.getToolPluginInformation(_toolJars, jar));
//                urls.add(ToolPluginInformation.getToolPluginInformation(_toolJars, jar));
            }
            catch (IOException e)
            {
                if ( tsmxLogger.loggerI18N.isErrorEnabled() )
                    tsmxLogger.loggerI18N.error("com.arjuna.ats.tools.toolsframework.ToolsClassLoader.invalidjar", new Object[] {e.getMessage()});

                if ( tsmxLogger.loggerI18N.isDebugEnabled())
                    tsmxLogger.loggerI18N.debug("com.arjuna.ats.tools.toolsframework.ToolsClassLoader.invalidjar", e);
            }
        }

        try
        {
            addURL(toolsLibDirectory.toURL());
//            urls.add(toolsLibDirectory.toURL());
            toolsDir = toolsLibDirectory.toURL();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

//        return setClassLoader(urls, toolsLibDirectory.getAbsolutePath());
    }

    /**
	 * @message com.arjuna.ats.tools.toolsframework.ToolsClassLoader.invalidurl The URL is invalid: {0}
	 * @param toolsLibDirectory
	 */
    private boolean setClassLoader(Collection<URL> urls, String toolsLibDirectory)
    {
        // method 1 works
        URL[] xurls = new URL[ _toolJars.size() + 1 ];

        for (int count=0;count<_toolJars.size();count++) {
            ToolPluginInformation info = _toolJars.get(count);
            try {
                xurls[count] = info.getFileURL();
            } catch (Exception e) {
            }
        }

        try
        {
            toolsDir = new File(toolsLibDirectory).toURL();

            _urlLoader = new URLClassLoader(xurls);
            if (toolsDir != null)
                return true;
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            return false;
        }



        // method 2 doesn't
        URL[] urla = urls.toArray(new URL[urls.size() + 1]);

        try
        {
            // make sure the toolsDir has trailing slash for the benefit of the class loader
            toolsDir = new File(toolsLibDirectory).toURL();
        }
        catch (MalformedURLException e)
        {
            if ( tsmxLogger.loggerI18N.isErrorEnabled() )
                tsmxLogger.loggerI18N.error("com.arjuna.ats.tools.toolsframework.ToolsClassLoader.invalidurl", new Object[] { toolsLibDirectory } );

            if ( tsmxLogger.loggerI18N.isDebugEnabled())
                tsmxLogger.loggerI18N.debug("com.arjuna.ats.tools.toolsframework.ToolsClassLoader.invalidurl", e);

            return false;
        }

        urla[urls.size()] = toolsDir;

        _urlLoader = URLClassLoader.newInstance(urls.toArray(new URL[urla.length]),
                Thread.currentThread().getContextClassLoader());

        return true;
    }

    public ToolPluginInformation[] getToolsInformation()
	{
        return _toolJars.toArray(new ToolPluginInformation[_toolJars.size()]);
	}
/*
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
*/
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

	private boolean accept(String name)
	{
        if (!name.endsWith(JAR_FILENAME_SUFFIX))
            return false;

        if (name.indexOf('/') == -1)
            return true;    // jar in the top level directory

        if (name.startsWith(TOOLS_DIR) && name.indexOf('/', TOOLS_DIR.length()) == -1)
            return true;    // jar in the tools direcory

        return false;
	}

    private String getTmpDir()
    {
        String tmpDir = System.getProperty(JBOSS_TMP_DIR_PROP);

        if (tmpDir == null)
            tmpDir = TMP_DIR;
        else
            tmpDir += "/" + TMP_DIR;

        return (tmpDir.endsWith("/") ? tmpDir : tmpDir + '/');
    }

    private String getSarPath()
    {
        URL url = Thread.currentThread().getContextClassLoader().getResource(DEFAULT_LIB_DIRECTORY);
        String tDir = url.getFile();
        int ti = tDir.indexOf('/' + DEFAULT_LIB_DIRECTORY);
        String sarPath = ti != -1 ? tDir.substring(0, ti) : null;

        return (sarPath != null && new File(sarPath).exists() ? sarPath : null);
    }

    public URL getToolsDir()
    {
        return toolsDir;
    }
}
