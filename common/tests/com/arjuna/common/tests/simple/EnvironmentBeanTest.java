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

import org.junit.Test;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * EnvironmentBean tests
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class EnvironmentBeanTest
{
    @Test
    public void beanPopulatorTestWithDummies() throws Exception {

        // check that a bean is populated correctly by the BeanPopulator

        DummyEnvironmentBean testBean = new DummyEnvironmentBean();
        DummyProperties testProperties = new DummyProperties(testBean.getProperties());
        testProperties.addConcatenationKeys(DummyEnvironmentBean.class);
        BeanPopulator.configureFromProperties(testBean, testProperties);

        Set<Object> expectedKeys = testBean.getProperties().keySet();

        assertTrue( testProperties.usedKeys.containsAll(expectedKeys) );
    }

    ///////////////////////////////////////////////

    public static void testBeanByReflection(Object environmentBean) throws Exception {

        for(Field field : environmentBean.getClass().getDeclaredFields()) {
            Class type = field.getType();

            String setterMethodName = "set"+capitalizeFirstLetter(field.getName());
            Method setter;
            try {
                setter = environmentBean.getClass().getMethod(setterMethodName, new Class[] {field.getType()});
            } catch(NoSuchMethodException e) {
                continue; // emma code coverage tool adds fields to instrumented classes - ignore them.
            }

            String getterMethodName;
            Method getter = null;
            if(field.getType().equals(Boolean.TYPE)) {
                getterMethodName = "is"+capitalizeFirstLetter(field.getName());
                try {
                    getter = environmentBean.getClass().getMethod(getterMethodName, new Class[] {});
                } catch (NoSuchMethodException e) {}
            }

            if(getter == null) {
                getterMethodName = "get"+capitalizeFirstLetter(field.getName());
                getter = environmentBean.getClass().getMethod(getterMethodName, new Class[] {});
            }

            if(field.getType().getName().startsWith("java.util")) {
                handleGroupProperty(environmentBean, field, setter, getter);
            } else {
                handleSimpleProperty(environmentBean, field, setter, getter);
            }
        }

    }

    private static void handleGroupProperty(Object bean, Field field, Method setter, Method getter)
        throws Exception
    {
        Object inputValue = null;

        if(java.util.Map.class.isAssignableFrom(field.getType())) {

            inputValue = new HashMap<String,String>();
            ((Map)inputValue).put("testKey", "testValue");

        } else {

            inputValue = new ArrayList<String>();
            ((List)inputValue).add("1");
            
        }

        setter.invoke(bean, new Object[] {inputValue});

        Object outputValue = getter.invoke(bean, new Object[] {});

        assertEquals(inputValue, outputValue);
        assertNotSame(inputValue, outputValue);

        setter.invoke(bean, new Object[] {null});
        outputValue = getter.invoke(bean, new Object[] {});
        assertNotNull(outputValue);

        if(outputValue instanceof Collection) {
            assertTrue(((Collection)outputValue).isEmpty());
        } else {
            assertTrue(((Map)outputValue).isEmpty());
        }

        // TODO if collection type is an interface (but how to know?) test matched Instance|ClassNames field sync. 

    }

    private static void handleSimpleProperty(Object bean, Field field, Method setter, Method getter)
            throws Exception
    {
        Object inputValue = null;

        if(field.getType().equals(Boolean.TYPE)) {

            inputValue = Boolean.TRUE;
            setter.invoke(bean, new Object[]{ inputValue });

        } else if(field.getType().equals(String.class)) {

            inputValue = "inputValue";
            setter.invoke(bean, new Object[] {inputValue});

        } else if(field.getType().equals(Long.TYPE)) {

            inputValue = new Long(1001);
            setter.invoke(bean, new Object[] {inputValue});

        } else if(field.getType().equals(Integer.TYPE)) {

            inputValue = new Integer(1001);
            setter.invoke(bean, new Object[] {inputValue});

        } else if(field.getType().toString().startsWith("interface ")) {

            handleInterfaceField(bean, field, setter, getter);
            return;

        } else if(field.getType().toString().equals("class java.lang.Class")) {
            // ignore for now, no easy way to test
        } else {
            throw new Exception("unknown field type "+field.getType());
        }

        Object outputValue = getter.invoke(bean, new Object[] {});

        assertEquals(inputValue, outputValue);
    }

    private static String capitalizeFirstLetter(String string) {
        return (string.length()>0) ? (Character.toUpperCase(string.charAt(0))+string.substring(1)) : string;
    }

    private static void handleInterfaceField(Object bean, Field field, Method setter, Method getter)
            throws Exception
    {
        Class interfaceType = field.getType();

        String setterMethodName = setter.getName()+"ClassName";
        Method classNameSetter = bean.getClass().getMethod(setterMethodName, new Class[] {String.class});

        String getterMethodName = getter.getName()+"ClassName";
        Method classNameGetter = bean.getClass().getMethod(getterMethodName, new Class[] {});

        ///////

        InvocationHandler invocationHandler = new DummyInvocationhandler();
        Object proxy = Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class[] { interfaceType }, invocationHandler);

        setter.invoke(bean, new Object[] { proxy }); // setFoo()
        assertSame(getter.invoke(bean, new Object[] {}), proxy); // getFoo

        setter.invoke(bean, new Object[] { null }); // setFoo()
        assertNull(getter.invoke(bean, new Object[] {})); // getFoo
        assertNull(classNameGetter.invoke(bean, new Object[] {})); // getFooClassName

        String bogusClassName = "bogusClassName";
        classNameSetter.invoke(bean, new Object[] { bogusClassName });
        assertNull(getter.invoke(bean, new Object[] {}));
        assertEquals(bogusClassName, classNameGetter.invoke(bean, new Object[] {}));
    }

    private static class DummyInvocationhandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            return null;
        }
    }
}