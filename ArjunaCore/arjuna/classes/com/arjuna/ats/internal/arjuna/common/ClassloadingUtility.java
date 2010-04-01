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
     *
     * @message com.arjuna.ats.internal.arjuna.common.ClassloadingUtility_1 [com.arjuna.ats.internal.arjuna.common.ClassloadingUtility_1] className is null
     * @message com.arjuna.ats.internal.arjuna.common.ClassloadingUtility_2 [com.arjuna.ats.internal.arjuna.common.ClassloadingUtility_2] attempt to load {0} threw ClassNotFound. Wrong classloader?
     * @message com.arjuna.ats.internal.arjuna.common.ClassloadingUtility_3 [com.arjuna.ats.internal.arjuna.common.ClassloadingUtility_3] class {0} does not implement {1}
     * @message com.arjuna.ats.internal.arjuna.common.ClassloadingUtility_4 [com.arjuna.ats.internal.arjuna.common.ClassloadingUtility_4] can't create new instance of {0}
     * @message com.arjuna.ats.internal.arjuna.common.ClassloadingUtility_5 [com.arjuna.ats.internal.arjuna.common.ClassloadingUtility_5] can't access {0}
     */
    public static <T> T loadAndInstantiateClass(Class<T> iface, String className) {

        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("Loading class " + className);
        }

        if (className == null)
        {
            tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.common.ClassloadingUtility_1");
            return null;
        }

        Class<?> clazz;

        try
        {
            clazz = Thread.currentThread().getContextClassLoader().loadClass( className ) ;
        } catch(ClassNotFoundException e) {
            tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.common.ClassloadingUtility_2",  new Object[]{className}, e);
            return null;
        }

        T instance = null;

        try {
            Class<? extends T> clazz2 = clazz.asSubclass(iface);
            instance = (T)clazz2.newInstance();
        } catch (ClassCastException e) {
            tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.common.ClassloadingUtility_3", new Object[]{className, iface.getName()}, e);
        }
        catch (InstantiationException e) {
            tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.common.ClassloadingUtility_4", new Object[]{className}, e);
        } catch (IllegalAccessException e) {
            tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.common.ClassloadingUtility_5", new Object[]{className}, e);
        }

        return instance;
    }

    /**
     * Reverse mapping - obtain the class name for a given object.
     *
     * @param instance the object of interest
     * @return the class name of the object, or null.
     */
    public static String getNameForClass(Object instance) {
        if(instance == null) {
            return null;
        }

        return instance.getClass().getName();
    }
}
