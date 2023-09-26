/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jdbc.drivers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.sql.XADataSource;

/**
 * A simple wrapper class that uses reflection to load and configure an XADataSource.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2009-05
 */
public class XADataSourceReflectionWrapper
{
    private XADataSource xaDataSource;

    XADataSourceReflectionWrapper(String classname)
    {
        try
        {
            xaDataSource = (XADataSource)Class.forName(classname).newInstance();
        }
        catch(Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public void setProperty(String name, String value)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        name = "set"+name.substring(0,1).toUpperCase()+name.substring(1);

        Method[] methods = xaDataSource.getClass().getMethods();
        Method matchingMethod = null;
        for(Method method : methods) {
            if(method.getName().equals(name) && method.getParameterCount() == 1) {
                // ignores overloading, just takes the first match it finds.
                matchingMethod = method;
                break;
            }
        }

        if(matchingMethod == null) {
            throw new NoSuchMethodException("Could not match "+name);
        }

        Class type = matchingMethod.getParameterTypes()[0];
        Object argument = value;

        if(type == Integer.TYPE) {
            argument = Integer.valueOf(value);
        }
        if(type == Boolean.TYPE) {
            argument = Boolean.valueOf(value);
        }

        matchingMethod.invoke(xaDataSource, argument);
    }

    public XADataSource getWrappedXADataSource()
    {
        return xaDataSource;
    }
}