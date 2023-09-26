/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.orbportability.internal.utils;

import java.util.Map;

import com.arjuna.common.internal.util.ClassloadingUtility;
import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.logging.opLogger;
import com.arjuna.orbportability.utils.InitClassInterface;

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
        opLogger.logger.debugf("Loading %s class - %s", initName, className);

        Class c = ClassloadingUtility.loadClass(className);
        if(c == null) {
            return;
        }
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
    }
    }

private String initName;
private String propertyName;
private Object initObj;

}