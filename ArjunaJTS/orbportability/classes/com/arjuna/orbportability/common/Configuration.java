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
import com.arjuna.common.util.ConfigurationInfo;

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
    private static synchronized final String propertiesDir ()
    {
        String propDir = ".";

        try
        {
            propDir = FileLocator.locateFile( ConfigurationInfo.getPropertiesFile() );

			if ( propDir != null )
			{
				propDir = stripDirectoryFromFilename( propDir );
			}
        }
        catch (java.io.FileNotFoundException e)
        {
            if (opLogger.loggerI18N.isWarnEnabled())
            {
                opLogger.loggerI18N.warn("com.arjuna.orbportability.common.Configuration.cannotfindproperties", new Object[] { "Configuration.propertiesDir()", ConfigurationInfo.getPropertiesFile() } );
            }
        }

	return propDir;
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
     * @return the default bind mechanism.
     * @message com.arjuna.orbportability.common.Configuration.bindDefault.invalidbind {0} - invalid bind mechanism in properties file
     */
    public static synchronized final int bindDefault ()
    {
	    if (_bindMethod == -1)
	    {
                if (opLogger.loggerI18N.isWarnEnabled())
                {
                    opLogger.loggerI18N.warn("com.arjuna.orbportability.common.Configuration.bindDefault.invalidbind", new Object[] { "com.arjuna.orbportability.common.Configuration.bindDefault()" } );
                }
	    }

    	return _bindMethod;
    }
    

private static String 	_configFile = "CosServices.cfg";
private static String 	_configFileRoot = null;

    private static final int _bindMethod = Services.bindValue(opPropertyManager.getOrbPortabilityEnvironmentBean().getBindMechanism());
}
