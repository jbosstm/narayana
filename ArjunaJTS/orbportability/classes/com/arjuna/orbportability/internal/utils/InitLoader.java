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
 * $Id: InitLoader.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.internal.utils;

import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.logging.opLogger;
import com.arjuna.orbportability.utils.InitClassInterface;

import java.util.Properties;
import java.util.Enumeration;
import java.util.Map;

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

    protected void initialise()
    {
        Map<String, String> properties = opPropertyManager.getOrbPortabilityEnvironmentBean().getOrbInitializationProperties();

        for(String attrName : properties.keySet())
        {
            if (attrName.indexOf(propertyName) != -1)
            {
                createInstance(attrName, properties.get(attrName));
            }
        }
    }

private void createInstance (String attrName, String className)
    {
	if (className == null)
	{
        opLogger.i18NLogger.warn_internal_utils_InitLoader_initfailed(initName, attrName);

	    return;
	}
	else
	{
	    try
	    {
            opLogger.i18NLogger.warn_internal_utils_InitLoader_loading(initName, className);

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
            opLogger.i18NLogger.warn_internal_utils_InitLoader_exception(initName, e1);
		}
		catch (InstantiationException e2)
		{
            opLogger.i18NLogger.warn_internal_utils_InitLoader_exception(initName, e2);
		}

		c = null;
	    }
	    catch (ClassNotFoundException e)
	    {
            opLogger.i18NLogger.warn_internal_utils_InitLoader_couldnotfindclass(initName, className);
	    }
	}
    }

private String initName;
private String propertyName;
private Object initObj;

}
