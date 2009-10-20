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

import com.arjuna.common.internal.util.propertyservice.ConcatenationPrefix;

import java.util.*;
import java.lang.reflect.Field;

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
