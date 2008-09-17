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
 * $Id: PluginClassloader.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.tools.objectstorebrowser;

import java.io.File;
import java.io.FilenameFilter;
import java.util.jar.JarFile;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.ArrayList;
import java.net.URLClassLoader;
import java.net.URL;

/**
 * This is a classloader which loads classes from a given directory from
 * JARs with a given prefix.
 *
 * @version $Id: PluginClassloader.java 2342 2006-03-30 13:06:17Z  $
 * @author Richard Adam Begg (richard.begg@arjuna.com)
 */
public class PluginClassloader implements FilenameFilter
{
    private final static String JAR_FILENAME_SUFFIX = ".jar";
    private final static String JAR_MANIFEST_PROPERTY_NAME = "plugin-classname-";

    private String          _pluginPrefix = null;
    private String          _pluginSuffix = null;
    private ArrayList       _plugins = new ArrayList();
    private URLClassLoader  _urlClassloader = null;

    public PluginClassloader(String pluginPrefix, String pluginSuffix, String manifestSectionName, File pluginDir)
    {
        _pluginPrefix = pluginPrefix;
        _pluginSuffix = pluginSuffix;

        File[] files = pluginDir.listFiles(this);
	if (files == null)	/* Directory not found */
		return;
        ArrayList urls = new ArrayList();
        ArrayList<String> plugins = new ArrayList<String> ();

        for (int count=0;count<files.length;count++)
        {
            try
            {
                JarFile jarFile = new JarFile(files[count]);

                Manifest jarManifest = jarFile.getManifest();

                if ( jarManifest != null )
                {
                    Attributes jarAttrs = jarManifest.getAttributes(manifestSectionName);

                    if ( jarAttrs != null )
                    {
                        int index = 1;
                        String classname;

                        /** Get property.1, property.2, property.x .... **/
                        while ( ( classname = jarAttrs.getValue(JAR_MANIFEST_PROPERTY_NAME + (index++)) ) != null )
                        {
                            /** Add the URL of the file to urls list **/
                            urls.add(files[count].toURL());
                            /** Add the classname to the list - which will later be replaced with the actual class **/
                            plugins.add(classname);
                        }
                    }
                }
            }
            catch (java.io.IOException e)
            {
                System.err.println("An error occurred while trying to load plugin: "+files[count]);
                e.printStackTrace(System.err);
            }

        }

        /** Convert arraylist into array **/
        URL[] jarUrls = new URL[urls.size()];
        urls.toArray(jarUrls);

        /** Create class loader with URLs array **/
        _urlClassloader = new URLClassLoader(jarUrls, this.getClass().getClassLoader());

        /** Go through list of classnames and convert into object instances **/
        for (String obj : plugins)
        {
            /** if it's a string then we need to convert it into a instance of that class **/
                try
                {
                    Object instance = _urlClassloader.loadClass((String)obj).newInstance();

                    /** Override the classname with the instance of that class **/
                    _plugins.add(instance);
                }
                catch (ClassNotFoundException e)
                {
                    System.err.println("Warning: The class '"+obj+"' cannot be found");
                    // Ignore - invalid configuration
                }
                catch (NoClassDefFoundError e)
                {
                    System.err.println("Warning: The class '"+obj+"' is not defined");
                }
                catch (Exception e)
                {
                    System.err.println("Warning: cCould not instantiate the class '"+obj+"' - " + e.getMessage());
                }
        }
    }

    public Class loadClass(String classname) throws ClassNotFoundException
    {
        return _urlClassloader.loadClass(classname);
    }

    public Object[] getPlugins()
    {
        return _plugins.toArray();
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
        /** Return true if the file starts with pluginPrefix (if not null) and ends with pluginSuffx (if not null) **/
        return ( _pluginPrefix != null ? name.startsWith(_pluginPrefix) : true ) && ( _pluginSuffix != null ? name.endsWith(_pluginSuffix + JAR_FILENAME_SUFFIX) : true );
    }
}
