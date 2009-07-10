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
package com.arjuna.common.util.propertyservice;

import com.arjuna.common.internal.util.propertyservice.PropertyManagerImpl;
import com.arjuna.common.internal.util.propertyservice.plugins.io.XMLFilePlugin;
import com.arjuna.common.util.FileLocator;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PropertyManagerFactory.java 2342 2006-03-30 13:06:17Z  $
 */

/**
 * This is the property manager factory used to produce property managers.
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 */
public class PropertyManagerFactory
{
    private static ConcurrentMap<String, PropertyManager> propertyManagersByModuleName = new ConcurrentHashMap<String, PropertyManager>();
    private static ConcurrentMap<String, PropertyManager> propertyManagersByCanonicalFileName = new ConcurrentHashMap<String, PropertyManager>();

    /**
     * Return a PropertyManager for the given module. If no such PropertyManager exists, create one using
     * the property file whose name is found by resolving the given key property.
     *
     * @param moduleName The symbolic name of the application module e.g. 'arjuna', 'txoj'.
     * @param fileNamePropertyKey The name of the property whose value is the config file name.
     * @return a PropertyManager.
     */
    public static PropertyManager getPropertyManagerForModule(String moduleName, String fileNamePropertyKey)
    {
        // once loaded, Property managers for each module are cached here.
        // Clients (usually the xxxPropertyManager classes in each app module) should not cache the returned
        // propertyManager object, so that we can flush config just be clearing this one cache.
        if(propertyManagersByModuleName.containsKey(moduleName)) {
            return propertyManagersByModuleName.get(moduleName);
        }

        // first time we have been asked for this module's properties - try to load them.
        return createPropertyManagerForModule(moduleName, fileNamePropertyKey);
    }

    public static PropertyManager getPropertyManagerForFile(String propertyFileName, boolean withCaching)
    {
        String filepath = null;
        try
        {
            // Convert the possibly relative path into a canonical path, using FileLocator.
            // This is the point where the search path is applied - user.dir (pwd), user.home, java.home, classpath
            filepath = FileLocator.locateFile(propertyFileName);
            File propertyFile = new File(filepath);
            if(!propertyFile.exists() || !propertyFile.isFile()) {
                throw new RuntimeException("invalid property file "+filepath);
            }
            filepath = propertyFile.getCanonicalPath();
        }
        catch(FileNotFoundException fileNotFoundException)
        {
            // try falling back to a default file built into the .jar
            // Note the default- prefix on the name, to avoid finding it from the .jar at the previous stage
            // in cases where the .jar comes before the etc dir on the classpath.
            URL url = PropertyManagerFactory.class.getResource("/default-"+propertyFileName);
            if(url == null) {
                throw new RuntimeException("missing property file "+propertyFileName);
            } else {
                filepath = url.toString();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("invalid property file "+filepath, e);
        }

        // We have a candidate file. Check if we have loaded it already. If so, associate it to the module cache and return it.
        PropertyManager propertyManager = null;

        if(withCaching) {
            propertyManager = propertyManagersByCanonicalFileName.get(filepath);
        }

        // We have not loaded this file before. Do so now.
        if(propertyManager == null) {
            propertyManager = new PropertyManagerImpl(propertyFileName);
            try {
                propertyManager.load(XMLFilePlugin.class.getName(), filepath);
                if(withCaching) {
                    PropertyManager existingPropertyManager = propertyManagersByCanonicalFileName.putIfAbsent(filepath, propertyManager);
                    if(existingPropertyManager != null) {
                        propertyManager = existingPropertyManager;
                    }
                }
            } catch(Exception e) {
                throw new RuntimeException("unable to load properties from file "+filepath, e);
            }
        }
        
        return propertyManager;
    }


    private static synchronized PropertyManager createPropertyManagerForModule(String moduleName, String fileNamePropertyKey)
    {
        if(propertyManagersByModuleName.containsKey(moduleName)) {
            return propertyManagersByModuleName.get(moduleName);
        }

        // This is where the properties loading takes place. The algorithm is as follows:

        // If the specified fileNamePropertyKey exists as a key is the system properties, take the value of that property as
        // the location of the module's properties file. This allows file location to be overriden easily.
        String propertyFileName = System.getProperty(fileNamePropertyKey);

        // If the system property is not set, try to load the build time properties for the module. Build time properties
        // are not the module properties! These are optional and so loading may fail. That's not considered an error.
        // If the build time property key PROPERTIES_FILE exists, take its value as the module's property file location.
        // (In JBossTS it does exist for most modules - the build scripts put build time properties files for the modules
        // into the product .jar)
        if (propertyFileName == null)
            propertyFileName = getFileNameFromBuildTimeProperties(moduleName);

        // Bail out if it has not been possible to get a file name by either of these method.
        if(propertyFileName == null) {
            throw new RuntimeException("Unable to resolve property file name for module "+moduleName);
        }

        PropertyManager propertyManager = getPropertyManagerForFile(propertyFileName, true);

        propertyManagersByModuleName.put(moduleName, propertyManager);

        return propertyManager;
    }

    private static String getFileNameFromBuildTimeProperties(String moduleName) {
        Properties buildTimeProperties = new Properties();
        final InputStream is = PropertyManagerFactory.class.getResourceAsStream("/"+moduleName+".properties") ;
        if (is != null)
        {
            try {
                buildTimeProperties.load(is);
            } catch(IOException e) {
                try {
                    is.close();
                } catch(IOException e2) {}
            }
        }
        return buildTimeProperties.getProperty("PROPERTIES_FILE");
    }
}
