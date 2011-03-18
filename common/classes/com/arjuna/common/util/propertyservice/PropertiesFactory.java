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

import com.arjuna.common.util.ConfigurationInfo;

import java.util.Properties;
import java.util.Enumeration;
import java.io.*;
import java.net.URL;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 */

/**
 * This class loads properties according to the file location, substitution and override rules described in the docs.
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 */
public class PropertiesFactory
{
    private static volatile Properties defaultProperties = null;

    /**
     * Returns the systems default properties, as read from the configuration file.
     * @return the configuration Properties
     */
    public static Properties getDefaultProperties() {
        if(defaultProperties == null) {
            // TODO: pick and document new standard for global config file name property. For now use 'common' module value.
            initDefaultProperties("com.arjuna.ats.arjuna.common.propertiesFile");
        }

        return defaultProperties;
    }

    /**
     * Returns the config properties read from a specified location.
     *
     * @param propertyFileName the file name. If relative, this is located using the FileLocator algorithm.
     * @return the Properties loaded from the specified source.
     */
    public static Properties getPropertiesFromFile(String propertyFileName, ClassLoader classLoader)
    {
        String filepath = null;
        try
        {
            // Convert the possibly relative path into a canonical path, using FileLocator.
            // This is the point where the search path is applied - user.dir (pwd), user.home, java.home, classpath
            filepath = com.arjuna.common.util.propertyservice.FileLocator.locateFile(propertyFileName, classLoader);
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
            URL url = PropertiesFactory.class.getResource("/default-"+propertyFileName);
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

        Properties properties = null;

        try {
            properties = loadFromFile(filepath);
            properties = applySystemProperties(properties);

        } catch(Exception e) {
            throw new RuntimeException("unable to load properties from file "+filepath, e);
        }

        return properties;
    }

    /////////////////

    // system properties take precedence over ones from the file.
    private static Properties applySystemProperties(Properties inputProperties)
    {
        Properties outputProperties = new Properties(inputProperties);
        Enumeration enumeration = System.getProperties().propertyNames();
        while(enumeration.hasMoreElements()) {
            String key = (String)enumeration.nextElement();
            outputProperties.setProperty(key, System.getProperty(key));
        }
        return outputProperties;
    }

    // standard java.util.Properties xml format, with JBossAS style substitution post-processing.
    private static Properties loadFromFile(String uri) throws IOException
    {
        InputStream inputStream = null;
        Properties inputProperties = new Properties();
        Properties outputProperties = new Properties();

        if( new File(uri).exists() ) {
            inputStream = new FileInputStream(uri);
        } else {
            inputStream = new URL(uri).openStream();
        }

        try {
            inputProperties.loadFromXML(inputStream);
        } finally {
            inputStream.close();
        }

        Enumeration namesEnumeration = inputProperties.propertyNames();
        while(namesEnumeration.hasMoreElements()) {
            String propertyName = (String)namesEnumeration.nextElement();
            String propertyValue = inputProperties.getProperty(propertyName);

            propertyValue = propertyValue.trim();

            // perform JBossAS style property substitutions. JBTM-369
            propertyValue = StringPropertyReplacer.replaceProperties(propertyValue);

            outputProperties.setProperty(propertyName, propertyValue);
        }

        return outputProperties;
    }

    private static synchronized void initDefaultProperties(String fileNamePropertyKey)
    {
        if(defaultProperties != null) {
            return;
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
            throw new RuntimeException("Unable to resolve property file name");
        }

        defaultProperties = getPropertiesFromFile(propertyFileName, PropertiesFactory.class.getClassLoader());
    }
}
