/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.internal.arjuna.common;

import com.arjuna.ats.arjuna.logging.tsLogger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility functions, used mainly by the EnvironmentBeans, for managing dynamic classloading.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-04
 */
public class ClassloadingUtility
{
    // this really belongs in common, but can't use logging from there at present.

    /**
     * Load, instantiate and return an instance of the named class, which is expected to be an implementation of
     * the specified interface.
     *
     * In the event of error (ClassNotFound, ClassCast, can't instantiate, ...) this method will log the error and return null.
     *
     *
     * @param iface the expected interface type.
     * @param className the name of the class to load and instantiate.
     * @param <T>
     * @return an instantiate of the specified class, or null.
     */
    public static <T> T loadAndInstantiateClass(Class<T> iface, String className)
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("Loading class " + className);
        }

        if (className == null) {
            tsLogger.i18NLogger.warn_common_ClassloadingUtility_1();
            return null;
        }

        Class<?> clazz;

        try
        {
            clazz = Thread.currentThread().getContextClassLoader().loadClass( className ) ;
        } catch(ClassNotFoundException e) {
            tsLogger.i18NLogger.warn_common_ClassloadingUtility_2(className, e);
            return null;
        }

        T instance = null;

        try {
            Class<? extends T> clazz2 = clazz.asSubclass(iface);
            instance = (T)clazz2.newInstance();
        } catch (ClassCastException e) {
            tsLogger.i18NLogger.warn_common_ClassloadingUtility_3(className, iface.getName(), e);
        }
        catch (InstantiationException e) {
            tsLogger.i18NLogger.warn_common_ClassloadingUtility_4(className, e);
        } catch (IllegalAccessException e) {
            tsLogger.i18NLogger.warn_common_ClassloadingUtility_5(className, e);
        }

        return instance;
    }

    public static <T> List<T> loadAndInstantiateClassesWithInit(Class<T> iface, List<String> classNamesWithOptionalInitParams)
    {
        List<T> instances = new ArrayList<T>();

        for(String theClassAndParameter : classNamesWithOptionalInitParams)
        {
            // see if there is a string parameter

            int breakPosition = theClassAndParameter.indexOf(BREAKCHARACTER);

            String theClass = null;
            String theParameter = null;

            if (breakPosition != -1)
            {
                theClass = theClassAndParameter.substring(0, breakPosition);
                theParameter = theClassAndParameter.substring(breakPosition + 1);
            }
            else
            {
                theClass = theClassAndParameter;
            }

            T instance = loadAndInstantiateClass(iface, theClass);


            if (theClass != null && theParameter != null)
            {
                try {
                    Method method = instance.getClass().getMethod("initialise", new Class[] {String.class}); // yup, UK English spelling
                    method.invoke(instance, theParameter);
                } catch(Exception e) {
                    tsLogger.i18NLogger.warn_common_ClassloadingUtility_6(theClassAndParameter, e);
                    continue;
                }
            }

            if(instance != null)
            {
                instances.add(instance);
            }
        }

        return instances;
    }

    /**
     * Reverse mapping - obtain the class name for a given Object.
     *
     * @param instance the Object of interest
     * @return the class name of the Object, or null.
     */
    public static String getNameForClass(Object instance)
    {
        if(instance == null) {
            return null;
        }

        return instance.getClass().getName();
    }

    /**
     * Reverse mapping - obtain the class names from a given set of Objects.
     *
     * If the input list is null a zero length list is returned.
     * If the input list contains nulls, these will not be present in the returned list.
     *
     * @param instances a list of Objects of interest.
     * @return a non-null list of zero or more elements, being class names of the Objects.
     */
    public static List<String> getNamesForClasses(List<? extends Object> instances)
    {
        List<String> names = new ArrayList<String>();

        if(instances != null)
        {
            for(Object instance : instances)
            {
                String name = getNameForClass(instance);
                if(name != null) {
                    names.add(name);
                }

            }
        }

        return names;
    }

    private static final char BREAKCHARACTER = ';';
}
