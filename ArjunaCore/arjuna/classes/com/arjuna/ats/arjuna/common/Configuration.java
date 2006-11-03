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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Configuration.javatmpl 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.common;

import com.arjuna.common.Info;
import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.common.util.FileLocator;

import java.io.File;
import java.io.InputStream;

import com.arjuna.ats.arjuna.logging.tsLogger;

import java.io.IOException;
import java.util.Properties;

/*
 * When we have an installation utility (e.g., InstallShield) we can have
 * that compile the property file location in a separate file which this
 * class then uses. So we ship a small source file in the distribution which
 * then gets built on a per-installation basis. Could do that for other
 * things too.
 */

/**
 * This class contains various run-time configuration options. Default
 * values are provided at compile-time, and may be operating system
 * specific.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Configuration.javatmpl 2342 2006-03-30 13:06:17Z  $
 * @since 1.0.
 */

public class Configuration
{

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
     * @return the location of the module properties file to use.
     *
     * @message com.arjuna.ats.arjuna.common.Configuration_1 [com.arjuna.ats.arjuna.common.Configuration_1] Configuration.propertiesDir() - Cannot find properties file {0}
     */

public static synchronized final String propertiesDir ()
    {
        String propDir = ".";

        try
        {
            propDir = FileLocator.locateFile( propertiesFile() );
        }
        catch (java.io.FileNotFoundException e)
        {
            if (tsLogger.arjLoggerI18N.isWarnEnabled()) {
	      tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.common.Configuration_1", 
					   new Object[]{propertiesFile()});
            }
        }

	return propDir;
    }

    /**
     * @return the version of arjuna.
     */

public static final String version ()
    {
	return getBuildTimeProperty("ARJUNA_VERSION");
    }

    /**
     * Used to obtain the root of the object store.
     *
     * @return <code>path</code> to object store.
     */

public static synchronized final String objectStoreRoot ()
    {
	if (_objectStore == null)
	{
	    /* Set default location under current directory */
	    _objectStore = System.getProperty("user.dir") + File.separator + "ObjectStore";

	}

	return _objectStore;
    }

    /**
     * Used to set the root of the object store. Changes will
     * take effect the next time the root is queried. Existing
     * object store instances will not be effected.
     */

public static synchronized final void setObjectStoreRoot (String s)
    {
	_objectStore = s;
    }

    /**
     * Whether to use the alternative abstract record ordering.
     * At present this is not fully documented, so stay away!
     *
     * @return <code>true</code> if order abstract records on type first, or
     * <code>false</code> if order on Uid first.
     */

public static synchronized final boolean useAlternativeOrdering ()
    {
	return _useAltOrder;
    }

    /**
     * Set whether or not to use the alternative abstract record
     * ordering. Takes effect the next time ordering is required.
     */

public static synchronized final void setAlternativeOrdering (boolean b)
    {
	_useAltOrder = b;
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

private static String  _objectStore = null;
private static boolean _useAltOrder = false;
private static String  _propFile = getBuildTimeProperty("PROPERTIES_FILE");


}
