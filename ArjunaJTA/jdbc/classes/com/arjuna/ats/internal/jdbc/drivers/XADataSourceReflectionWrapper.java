/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author JBoss Inc.
 */
package com.arjuna.ats.internal.jdbc.drivers;

import javax.sql.XADataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
            if(method.getName().equals(name) && method.getParameterTypes().length == 1) {
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
            argument = new Integer(value);
        }
        if(type == Boolean.TYPE) {
            argument = new Boolean(value);
        }

        matchingMethod.invoke(xaDataSource, argument);
    }

    public XADataSource getWrappedXADataSource()
    {
        return xaDataSource;
    }
}
