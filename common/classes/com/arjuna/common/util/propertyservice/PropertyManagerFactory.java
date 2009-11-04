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
import com.arjuna.common.util.ConfigurationInfo;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
 * This is the property manager factory used to produce properties.
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 */
public class PropertyManagerFactory
{
    private static ConcurrentMap<String, Properties> propertiesByModuleName = new ConcurrentHashMap<String, Properties>();
    private static ConcurrentMap<String, Properties> propertiesByCanonicalFileName = new ConcurrentHashMap<String, Properties>();

    public static Properties getDefaultProperties() {
        // TODO: pick and document new standard for global config file name property. For now use 'common' module value.        
       return getPropertiesForModule("common", "com.arjuna.ats.arjuna.common.propertiesFile");
    }

    /**
     * Return a Properties object for the given module. If no such Properties exists, create one using
     * the property file whose name is found by resolving the given key property.
     *
     * @param moduleName The symbolic name of the application module e.g. 'arjuna', 'txoj'.
     * @param fileNamePropertyKey The name of the property whose value is the config file name.
     * @return a Properties object.
     */
    private static Properties getPropertiesForModule(String moduleName, String fileNamePropertyKey)
    {
        // once loaded, Properties for each module are cached here.
        // Clients (usually BeanPopulator classes in each app module) should not cache the returned
        // Properties object, so that we can flush config just be clearing this one cache.
        if(propertiesByModuleName.containsKey(moduleName)) {
            return propertiesByModuleName.get(moduleName);
        }

        // first time we have been asked for this module's properties - try to load them.
        return createPropertiesForModule(moduleName, fileNamePropertyKey);
    }

    public static Properties getPropertiesFromFile(String propertiesFileName) {
        return getPropertiesFromFile(propertiesFileName, false);
    }

    private static Properties getPropertiesFromFile(String propertyFileName, boolean withCaching)
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
        Properties properties = null;

        if(withCaching) {
            properties = propertiesByCanonicalFileName.get(filepath);
        }

        // We have not loaded this file before. Do so now.
        if(properties == null) {
            PropertyManager propertyManager = new PropertyManagerImpl(propertyFileName);
            try {
                propertyManager.load(XMLFilePlugin.class.getName(), filepath);
                properties = propertyManager.getProperties();
                if(withCaching) {
                    Properties existingProperties = propertiesByCanonicalFileName.putIfAbsent(filepath, properties);
                    if(existingProperties != null) {
                        properties = existingProperties;
                    }
                }
            } catch(Exception e) {
                throw new RuntimeException("unable to load properties from file "+filepath, e);
            }
        }
        
        return properties;
    }


    private static synchronized Properties createPropertiesForModule(String moduleName, String fileNamePropertyKey)
    {
        if(propertiesByModuleName.containsKey(moduleName)) {
            return propertiesByModuleName.get(moduleName);
        }

        // This is where the properties loading takes place. The algorithm is as follows:

        // If the specified fileNamePropertyKey exists as a key is the system properties, take the value of that property as
        // the location of the module's properties file. This allows file location to be overriden easily.
        String propertyFileName = System.getProperty(fileNamePropertyKey);

        // If the system property is not set, try to load the build time properties. Build time properties
        // are not the module properties! These are optional and so loading may fail. That's not considered an error.
        // If the properties file name is defined by the build time properties, use that.
        // (In JBossTS it mostly does exist - the build scripts put build time properties into the .jars manifest file.)
        if (propertyFileName == null) {
            propertyFileName = ConfigurationInfo.getPropertiesFile();
        }

        // Bail out if it has not been possible to get a file name by either of these method.
        if(propertyFileName == null) {
            throw new RuntimeException("Unable to resolve property file name for module "+moduleName);
        }

        Properties properties = getPropertiesFromFile(propertyFileName, true);

        propertiesByModuleName.put(moduleName, properties);

        return properties;
    }
}
