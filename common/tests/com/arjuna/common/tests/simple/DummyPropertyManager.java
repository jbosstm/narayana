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
 * @author JBoss, a division of Red Hat.
 */
package com.arjuna.common.tests.simple;

import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.common.util.propertyservice.plugins.PropertyManagementPlugin;
import com.arjuna.common.util.exceptions.LoadPropertiesException;
import com.arjuna.common.util.exceptions.SavePropertiesException;
import com.arjuna.common.util.exceptions.ManagementPluginException;
import com.arjuna.common.internal.util.propertyservice.ConcatenationPrefix;

import java.util.*;
import java.lang.reflect.Field;
import java.io.IOException;

/**
 * Basic PropertyManager impl for BeanPopulator for test purposes
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class DummyPropertyManager implements PropertyManager
{
    public static Set<String> extractKeys(Class environment) throws IllegalAccessException {

        Set<String> keys = new HashSet<String>();

        for(Field field : environment.getDeclaredFields()) {
            String key = (String)field.get(null);

            if(field.isAnnotationPresent(Deprecated.class)) {
                continue;
            }

            keys.add(key);
        }

        return keys;
    }

    public void addConcatenationKeys(Class environmentBean) {

        for(Field field : environmentBean.getDeclaredFields()) {
            if(field.isAnnotationPresent(ConcatenationPrefix.class)) {
                String prefix = field.getAnnotation(ConcatenationPrefix.class).prefix();
                concatenationKeys.add(prefix);
                System.out.println("addConcat : "+prefix);
            }
        }
    }

    public Set<String> usedKeys = new HashSet<String>();
    Properties properties = null;
    public Set<String> concatenationKeys = new HashSet<String>();

    public DummyPropertyManager(Properties properties) {
        this.properties = properties;
    }

    public String getProperty(String key)
    {
        for(String prefix : concatenationKeys) {
            if(key.startsWith(prefix) && !usedKeys.contains(prefix)) {
                usedKeys.add(prefix);
                break;
            }
        }

        usedKeys.add(key);

        if(properties != null) {
            return properties.getProperty(key);
        } else {
            return null;
        }
    }

    public String getProperty(String s, String s1)
    {
        throw new RuntimeException("this is not expected to be called during the test");
    }

    public String setProperty(String s, String s1, boolean b)
    {
        throw new RuntimeException("this is not expected to be called during the test");
    }

    public String setProperty(String s, String s1)
    {
        throw new RuntimeException("this is not expected to be called during the test");
    }

    public String removeProperty(String s)
    {
        throw new RuntimeException("this is not expected to be called during the test");
    }

    public Properties getProperties()
    {
        throw new RuntimeException("this is not expected to be called during the test");
    }

    public Enumeration propertyNames()
    {
        Vector<String> names = new Vector<String>();
        for(String prefix : concatenationKeys) {
            names.add(prefix+"_one");
            names.add(prefix+"_two");
        }
        return names.elements();
    }

    public void load(String s, String s1) throws IOException, ClassNotFoundException, LoadPropertiesException
    {
        throw new RuntimeException("this is not expected to be called during the test");
    }

    public void save(String s, String s1) throws IOException, ClassNotFoundException, SavePropertiesException
    {
        throw new RuntimeException("this is not expected to be called during the test");
    }

    public void addManagementPlugin(PropertyManagementPlugin propertyManagementPlugin) throws IOException, ManagementPluginException
    {
        throw new RuntimeException("this is not expected to be called during the test");
    }

    public boolean verbose()
    {
        throw new RuntimeException("this is not expected to be called during the test");
    }
}