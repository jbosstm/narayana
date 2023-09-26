/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.common.internal.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.arjuna.common.logging.commonLogger;

/**
 * Utility functions, used mainly by the EnvironmentBeans, for managing dynamic classloading.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-04
 */
public class ClassloadingUtility
{
    /**
     * Load a class. No instantiation.
     * 
     * In the event of error (ClassNotFound etc) this method will log the error and return null.
     *
     * @param className the name of the class to load and instantiate.
     * @return the specified Class, or null.
     */
    public static Class loadClass(String className) {

        // This should be pretty much the only point in the codebase that actually does classloading.
        // Once upon a time it used TCCL, but that does not play nice with AS and is fairly pointless
        // anyhow, so we changed it... JBTM-828 and JBTM-735

        Class clazz;
        try
        {
            //clazz = Thread.currentThread().getContextClassLoader().loadClass( className ) ;
            clazz = Class.forName( className );
        } catch(ClassNotFoundException e) {
            commonLogger.i18NLogger.warn_common_ClassloadingUtility_2(className, e);
            return null;
        }
        return clazz;
    }

    /**
     * Load and return the named class, which is expected to be an implementation of the specified interface.
     *
     * In the event of error (ClassNotFound, ClassCast, ...) this method will log the error and return null.
     *
     * @param iface the expected interface type.
     * @param className the name of the class to load and instantiate.
     * @return the specified class, or null.
     */
    public static <T> Class<? extends T> loadClass(Class<T> iface, String className)
    {
        if (commonLogger.logger.isTraceEnabled()) {
            commonLogger.logger.trace("Loading class " + className);
        }

        if (className == null) {
            commonLogger.i18NLogger.warn_common_ClassloadingUtility_1();
            return null;
        }

        Class<?> clazz = loadClass( className );
        if(clazz == null) {
            return null;
        }

        try {
            Class<? extends T> clazz2 = clazz.asSubclass(iface);
            return clazz2;
        } catch (ClassCastException e) {
            commonLogger.i18NLogger.warn_common_ClassloadingUtility_3(className, iface.getName(), e);
            return null;
        }
    }

    /**
     * Load, instantiate and return an instance of the named class, which is expected to be an implementation of
     * the specified interface.
     *
     * In the event of error (ClassNotFound, ClassCast, can't instantiate, ...) this method will log the error and return null.
     *
     *
     * @param iface the expected interface type.
     * @param className the name of the class to load and instantiate.
     * @param environmentBeanInstanceName When the class ctor requires a *EnvironmentBean instance, the name of the bean.
     *   null for default ctor or default bean instance..
     * @return an instance of the specified class, or null.
     */
    public static <T> T loadAndInstantiateClass(Class<T> iface, String className, String environmentBeanInstanceName)
    {
        T instance = null;

        try {
            Class<? extends T> clazz = loadClass(iface, className);
            if(clazz == null) {
                return null;
            }

            Constructor[] ctors = clazz.getConstructors();
            Class environmentBeanClass = null;
            for(Constructor constructor : ctors) {
                if(constructor.getParameterCount() == 1 &&
                        constructor.getParameterTypes()[0].getCanonicalName().endsWith("EnvironmentBean")) {
                    environmentBeanClass = constructor.getParameterTypes()[0];
                    Object envBean = BeanPopulator.getNamedInstance(environmentBeanClass, environmentBeanInstanceName);
                    instance = (T)constructor.newInstance(envBean);
                    break;
                }
            }
            if(environmentBeanClass == null && environmentBeanInstanceName == null) {
                // no bean ctor, try default ctor
                instance = clazz.newInstance();
            }

        } catch (InstantiationException e) {
            commonLogger.i18NLogger.warn_common_ClassloadingUtility_4(className, e);
        } catch (IllegalAccessException e) {
            commonLogger.i18NLogger.warn_common_ClassloadingUtility_5(className, e);
        } catch(InvocationTargetException e) {
            commonLogger.i18NLogger.warn_common_ClassloadingUtility_4(className, e);
        }

        return instance;
    }

    public static <T> List<T> loadAndInstantiateClasses(Class<T> iface, List<String> classNames)
    {
        List<T> instances = new ArrayList<T>();

        if(classNames != null) {
            for(String className : classNames)
            {
                T instance = loadAndInstantiateClass(iface, className, null);
                if(instance != null)
                {
                    instances.add(instance);
                }
            }
        }

        return instances;
    }


    public static <T> List<T> loadAndInstantiateClassesWithInit(Class<T> iface, List<String> classNamesWithOptionalInitParams)
    {
        List<T> instances = new ArrayList<T>();

        if(classNamesWithOptionalInitParams != null) {
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

                T instance = loadAndInstantiateClass(iface, theClass, null);


                if (theClass != null && theParameter != null)
                {
                    try {
                        Method method = instance.getClass().getMethod("initialise", new Class[] {String.class}); // yup, UK English spelling
                        method.invoke(instance, theParameter);
                    } catch(Exception e) {
                        commonLogger.i18NLogger.warn_common_ClassloadingUtility_6(theClassAndParameter, e);
                        continue;
                    }
                }

                if(instance != null)
                {
                    instances.add(instance);
                }
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