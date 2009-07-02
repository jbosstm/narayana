/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Configuration.javatmpl 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.common;

import com.arjuna.orbportability.Services;

import com.arjuna.common.util.FileLocator;

import com.arjuna.orbportability.logging.*;

import java.io.File;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Module specific configuration object.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Configuration.javatmpl 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class Configuration
{

    /**
     * @return the name of the default orb configuration file.
     */
public static synchronized final String defaultORBConfiguration()
    {
        return _orbConfiguration;
    }

    /**
     * @return the name of the module properties file to use.
     */

public static synchronized final String propertiesFile ()
    {
	return _propFile;
    }

    /**
     * Set the name of the properties file.
     */

public static synchronized final void setPropertiesFile (String file)
    {
	_propFile = file;
    }

	/**
	 * Strip the directory from the given filename.
	 *
	 * @param filename The filename to strip the directory for.
	 * @return The directory the file exists in.
	 */
private static final String stripDirectoryFromFilename( String filename )
	{
		String dir = ".";
		/** Search the string for the last file separator char and the last slash **/
		int lastSeparator = filename.lastIndexOf(File.separatorChar);
		int lastSlash = filename.lastIndexOf('/');

		/** If the last character is a file separator and it exists **/
		if ( ( lastSeparator > lastSlash ) && ( lastSeparator != -1 ) )
		{
			/** Set the directory to the filename upto the last separator **/
			dir = filename.substring( 0, lastSeparator );
		}
		else
		{
			/** If the last slash is further towards the end and it exists **/
			if ( lastSlash != -1 )
			{
				dir = filename.substring( 0, lastSlash );
			}
		}

		return dir;
	}

    /**
     * @return the location of the module properties file to use.
     * @message com.arjuna.orbportability.common.Configuration.cannotfindproperties {0} - Cannot find properties file {1}
     */

public static synchronized final String propertiesDir ()
    {
        String propDir = ".";

        try
        {
            propDir = FileLocator.locateFile( propertiesFile() );

			if ( propDir != null )
			{
				propDir = stripDirectoryFromFilename( propDir );
			}
        }
        catch (java.io.FileNotFoundException e)
        {
            if (opLogger.loggerI18N.isWarnEnabled())
            {
                opLogger.loggerI18N.warn("com.arjuna.orbportability.common.Configuration.cannotfindproperties", new Object[] { "Configuration.propertiesDir()", propertiesFile() } );
            }
        }

	return propDir;
    }

    /**
     * @return the name of the file where <name, object IOR> may be stored.
     */

public static synchronized final String configFile ()
    {
	return _configFile;
    }

    /**
     * Set the name of the file where <name, object IOR> may be stored.
     */

public static synchronized void setConfigFile (String s)
    {
	_configFile = s;
    }

    /**
     * @return the location of the file where <name, object IOR> may be stored.
     */

public static synchronized final String configFileRoot ()
    {
	if (_configFileRoot == null)
	{
	    /*
	     * Search for an old file.
	     */

	    String oldConfigFile = null;

	    try
	    {
	    	oldConfigFile = FileLocator.locateFile(_configFile);
	    }
	    catch (FileNotFoundException e)
	    {
	    }

	    if (oldConfigFile != null)
	    {
		_configFileRoot = stripDirectoryFromFilename(oldConfigFile);
	    }
	    else
	    {
		_configFileRoot = com.arjuna.orbportability.common.Configuration.propertiesDir();
	    }
	}

	return _configFileRoot;
    }

    /**
     * Set the location of the file where <name, object IOR> may be stored.
     */

public static synchronized void setConfigFileRoot (String s)
    {
	_configFileRoot = s;
    }

    /**
     * @return the default bind mechanism.
     * @message com.arjuna.orbportability.common.Configuration.bindDefault.invalidbind {0} - invalid bind mechanism in properties file
     */

public static synchronized final int bindDefault ()
    {
        int bindMethod = _bindDefault;

        if (!_bindDefaultSet)
        {
            String configuredMechanism = opPropertyManager.getPropertyManager().getProperty(Environment.BIND_MECHANISM);

            if (configuredMechanism != null)
            {
                bindMethod = Services.bindValue(configuredMechanism);
            }

	    if (bindMethod == -1)
	    {
                if (opLogger.loggerI18N.isWarnEnabled())
                {
                    opLogger.loggerI18N.warn("com.arjuna.orbportability.common.Configuration.bindDefault.invalidbind", new Object[] { "com.arjuna.orbportability.common.Configuration.bindDefault()" } );
                }
	    }
	}

	return bindMethod;
    }

    /**
     * Set the default bind mechanism.
     * @message com.arjuna.orbportability.common.Configuration.setBindDefault.invaliddefaultvalue {0} - invalid value {1}
     */
public static synchronized final void setBindDefault (int i)
    {
	if ((i < Services.CONFIGURATION_FILE) || (i > Services.NAMED_CONNECT))
        {
            if (opLogger.loggerI18N.isWarnEnabled())
            {
                opLogger.loggerI18N.warn( "com.arjuna.orbportability.common.Configuration.setBindDefault.invaliddefaultvalue", new Object[] {"com.arjuna.orbportability.common.Configuration.setBindDefault", ""+i} );
            }
        }
	else
	{
	    _bindDefaultSet = true;
	    _bindDefault = i;
	}
    }

    /**
     * @return the version of the module.
     */

public static final String version ()
    {
	return getBuildTimeProperty("ORBPORTABILITY_VERSION") ;
    }
    /**
     * Get a build time property.
     * @param name The name of the build time property.
     * @return The build time property value.
     */
    public static String getBuildTimeProperty(final String name)
    {
        if (PROPS == null)
        {
            return "" ;
        }
        else
        {
            return PROPS.getProperty(name, "") ;
        }
    }
    
    private static final Properties PROPS ;
    
    static
    {
        final InputStream is = Configuration.class.getResourceAsStream("/arjuna.properties") ;
        if (is != null)
        {
            Properties props = new Properties() ;
            try
            {
                props.load(is) ;
            }
            catch (final IOException ioe)
            {
                props = null ;
            }
            PROPS = props ;
        }
        else
        {
            PROPS = null ;
        }
    }
    

private static String 	_configFile = "CosServices.cfg";
private static String 	_configFileRoot = null;
private static int    	_bindDefault = Services.CONFIGURATION_FILE;
private static boolean 	_bindDefaultSet = false;
private static String   _propFile = getBuildTimeProperty("PROPERTIES_FILE") ;
private static String   _orbConfiguration = getBuildTimeProperty("ORB_CONFIGURATION") ;

}
