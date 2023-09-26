/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.common.tests.simple;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import com.arjuna.common.internal.util.propertyservice.ConcatenationPrefix;

/**
 * Dummy Properties handler for EnvironmentBean test purposes.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2009-10
 */
public class DummyProperties extends Properties
{
    public DummyProperties() {
        super();
    }

    public DummyProperties(Properties properties) {
        super(properties);
    }

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

    @Override
    public String getProperty(String key)
    {
        for(String prefix : concatenationKeys) {
            if(key.startsWith(prefix) && !usedKeys.contains(prefix)) {
                usedKeys.add(prefix);
                break;
            }
        }

        usedKeys.add(key);

        return super.getProperty(key);
    }

    @Override
    public Enumeration propertyNames()
    {
        Vector<String> names = new Vector<String>();
        for(String prefix : concatenationKeys) {
            names.add(prefix+"_one");
            names.add(prefix+"_two");
        }
        return names.elements();
    }

    public Set<String> usedKeys = new HashSet<String>();
    public Set<String> concatenationKeys = new HashSet<String>();
}