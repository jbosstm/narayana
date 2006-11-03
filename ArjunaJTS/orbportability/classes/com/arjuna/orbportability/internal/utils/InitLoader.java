/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
 * $Id: InitLoader.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.internal.utils;

import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.logging.opLogger;
import com.arjuna.orbportability.utils.InitClassInterface;

import java.util.Properties;
import java.util.Enumeration;
import java.lang.ClassLoader;

import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;

/*
 * This class allows the programmer (and us!) to register classes
 * which must be instantiated before/after ORB initialisation.
 */

abstract class InitLoader
{

protected InitLoader (String name, String attrName, Object obj)
    {
	initName = name;
	propertyName = attrName;
        initObj = obj;
    }

protected void initialise ()
    {
	Properties properties = opPropertyManager.propertyManager.getProperties();

	if (properties != null)
	{
	    Enumeration names = properties.propertyNames();
	    
	    while (names.hasMoreElements())
	    {
		String attrName = (String) names.nextElement();

		if (attrName.indexOf(propertyName) != -1)
		    createInstance(attrName, properties.getProperty(attrName));
	    }
	}
    }

    /**
     * @message com.arjuna.orbportability.internal.utils.InitLoader.initfailed {0} - attempt to initialise {1} with null class name!
     * @message com.arjuna.orbportability.internal.utils.InitLoader.couldnotfindclass {0} - could not find class {1}
     * @message com.arjuna.orbportability.internal.utils.InitLoader.loading Loading {0} class - {1}
     */
private void createInstance (String attrName, String className)
    {
	if (className == null)
	{
            if ( opLogger.loggerI18N.isWarnEnabled() )
            {
                opLogger.loggerI18N.warn( "com.arjuna.orbportability.internal.utils.InitLoader.initfailed",
                                            new Object[] { initName, attrName } );
            }

	    return;
	}
	else
	{
	    try
	    {
		if ( opLogger.loggerI18N.isInfoEnabled() )
		{
		    opLogger.loggerI18N.info( "com.arjuna.orbportability.internal.utils.InitLoader.loading",
									    new Object[] { initName, className } );
		}

		Class c = Thread.currentThread().getContextClassLoader().loadClass(className);

		try
		{
		    Object o = c.newInstance();

                    if ( o instanceof InitClassInterface )
                    {
                        ((InitClassInterface)o).invoke(initObj);
                    }

		    o = null;
		}
		catch (IllegalAccessException e1)
		{
                    if ( opLogger.loggerI18N.isWarnEnabled() )
                    {
                        opLogger.logger.warn( initName + " " +e1 );
                    }
		}
		catch (InstantiationException e2)
		{
                    if ( opLogger.loggerI18N.isWarnEnabled() )
                    {
                        opLogger.logger.warn( initName + " " +e2 );
                    }
		}

		c = null;
	    }
	    catch (ClassNotFoundException e)
	    {
                if ( opLogger.loggerI18N.isWarnEnabled() )
                {
                    opLogger.loggerI18N.warn( "com.arjuna.orbportability.internal.utils.InitLoader.couldnotfindclass",
                                                new Object[] { initName, className } );
                }
	    }
	}
    }

private String initName;
private String propertyName;
private Object initObj;

}
