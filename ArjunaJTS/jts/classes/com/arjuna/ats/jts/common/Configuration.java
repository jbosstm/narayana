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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Configuration.javatmpl 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Build-time configuration information for the module.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: Configuration.javatmpl 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class Configuration
{
    
    /**
     * @return the version of the module.
     */

public static final String version ()
    {
	return getBuildTimeProperty("JTS_VERSION") ;
    }

    /**
     * @return the name of the module properties file to use.
     */



    /**
     * Get a build time property.
     * @param name The name of the build time property.
     * @return The build time property value.
     */
    private static String getBuildTimeProperty(final String name)
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
        final InputStream is = Configuration.class.getResourceAsStream("/jts.properties") ;
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

private static String _propFile = getBuildTimeProperty("PROPERTIES_FILE") ;

}
