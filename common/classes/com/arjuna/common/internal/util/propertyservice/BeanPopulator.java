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
package com.arjuna.common.internal.util.propertyservice;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.arjuna.common.util.propertyservice.PropertiesFactory;

/**
 * Utility class that configures *EnvironmentBean objects using a PropertyManager, which is usually
 * backed by a -properties.xml file.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class BeanPopulator
{
    private static final ConcurrentMap<String, Object> beanInstances = new ConcurrentHashMap<String, Object>();

    public static <T> T getDefaultInstance(Class<T> beanClass) throws RuntimeException {
        T instance = (T) beanInstances.get(beanClass.getName());

        if (instance != null)
           return instance;

        return getNamedInstance(beanClass, null, null);
    }

    @Deprecated
    public static <T> T getDefaultInstance(Class<T> beanClass, Properties properties) throws RuntimeException {
       T instance = (T) beanInstances.get(beanClass.getName());

        if (instance != null)
           return instance;

        return getNamedInstance(beanClass, null, properties);
    }

    public static <T> T getNamedInstance(Class<T> beanClass, String name) throws RuntimeException {
        return getNamedInstance(beanClass, name, null);
    }

    private static <T> T getNamedInstance(Class<T> beanClass, String name, Properties properties) throws RuntimeException {
        StringBuilder sb = new StringBuilder().append(beanClass.getName());

        if (name != null)
           sb.append(":").append(name);

        String key = sb.toString();

        // we don't mind sometimes instantiating the bean multiple times,
        // as long as the duplicates never escape into the outside world.
        if(!beanInstances.containsKey(key)) {
            T bean = null;
            try {
                bean = beanClass.newInstance();
                if (properties != null) {
                    configureFromProperties(bean, name, properties);
                } else {
                    Properties defaultProperties = PropertiesFactory.getDefaultProperties();
                    configureFromProperties(bean, name, defaultProperties);
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            beanInstances.putIfAbsent(key, bean);
        }

        return (T) beanInstances.get(key);
    }

    /**
     * @Deprecated Only used in tests
     */
    public static void configureFromProperties(Object bean, Properties properties) throws Exception {
        configureFromProperties(bean, null, properties);
    }

    /**
     * Examine the properties of the provided bean and update them to match the values of the corresponding
     * properties in the Properties.
     * This will normally be used at startup to configure a freshly created default bean to match
     * the configuration read from a properties file.
     *
     * The algorithm is as follows: for each field in the bean, which must have a getter and setter method
     * matching according to the JavaBeans naming conventions, determine the corresponding property key.
     *
     * Several key names are tried, with the first match being used:
     * For scalar properties: The FQN of the bean class, optionally followed by the bean name, followed by the field name,
     *  e.g. com.arjuna.FooBean.theField or com.arjuna.FooBean.theName.theField
     *   the short class name of the bean, optionally followed by the bean name, followed by the field name,
     *  e.g. FooBean.theField or FooBean.theName.theField
     *  and finally the bean classes' PropertyPrefix annotation
     *   value followed by the name of the field, the last being except in cases where the field has a FullPropertyName
     *   annotation, in which case its value is used instead.
     * For vector (in the math sense - the type is actually normally List/Map) properties, a single property key matched
     *   according to the prior rules will be treated as having a compound value which will be tokenized into
     *   elements based on whitespace and inserted into the list in token order or further tokenized on = for Map key/value.
     * If no such key is found, the value of the field's ConcatenationPrefix annotation
     * is treated as a name prefix and the list elements are assembled from the values of any properties having
     * key values starting with the prefix. These are inserted into the list in order of the key's name sort position.
     *
     * This allows for the convention that all properties in a given bean will share
     * the same prefix e.g. com.arjuna.ats.arjuna.foo. whilst still allowing for changing of the property
     * name in cases where this makes for more readable code.
     *
     * Obtain the value of the property from the Properties and if it's not null, type convert it to match
     * the bean's property field type.  Obtain the value of the property from the bean and if it's different
     * from the value read from the Properties, use the setter to update the bean.
     *
     * @param bean a JavaBean, the target of the property updates and source for defaults.
     * @param instanceName the (optional, use null for default) name for the bean instance.
     * @param properties a Properties object, the source of the configuration overrides.
     * @throws Exception if the configuration of the bean fails.
     */
    public static void configureFromProperties(Object bean, String instanceName, Properties properties) throws Exception {

        for(Field field : bean.getClass().getDeclaredFields()) {
            Class type = field.getType();

            String setterMethodName = "set"+capitalizeFirstLetter(field.getName());
            Method setter;
            try {
                setter = bean.getClass().getMethod(setterMethodName, new Class[] {field.getType()});
            } catch(NoSuchMethodException e) {
                continue; // emma code coverage tool adds fields to instrumented classes - ignore them.
            }

            String getterMethodName;
            Method getter = null;
            if(field.getType().equals(Boolean.TYPE)) {
                getterMethodName = "is"+capitalizeFirstLetter(field.getName());
                try {
                    getter = bean.getClass().getMethod(getterMethodName, new Class[] {});
                } catch (NoSuchMethodException e) {}
            }

            if(getter == null) {
                getterMethodName = "get"+capitalizeFirstLetter(field.getName());
                getter = bean.getClass().getMethod(getterMethodName, new Class[] {});
            }

            if(field.isAnnotationPresent(ConcatenationPrefix.class) || field.getType().getName().startsWith("java.util")) {
                handleGroupProperty(bean, instanceName, properties, field, setter, getter);
            } else {
                handleSimpleProperty(bean, instanceName, properties, field, setter, getter);
            }
        }
    }

    public static String printBean(Object bean) {
        StringBuffer buffer = new StringBuffer();
        printBean(bean, buffer);
        return buffer.toString();
    }

    /**
     * Render the state of the known bean instances as text.
     */
    public static String printState() {
        StringBuffer buffer = new StringBuffer();
        for(Object bean : beanInstances.values()) {
            printBean(bean, buffer);
        }
        return buffer.toString();
    }

    private static void handleGroupProperty(Object bean, String instanceName, Properties properties, Field field, Method setter, Method getter)
        throws Exception
    {
        List<String> lines = new LinkedList<String>();

        String valueFromProperties = getValueFromProperties(bean, instanceName, properties, field, bean.getClass().getSimpleName());

        if(valueFromProperties != null)
        {
            // it's a single value which needs parsing

            String[] tokens = valueFromProperties.split("\\s+");

            // for lists, the order we want them in is the order they appear in the string, so we can just add in sequence:
            for(String token : tokens) {
                lines.add(token);
            }
        }
        else
        {
            // it's set of values that need gathering together
            // for lists, the order we want them in is the lex sort order of the keys, so we need to buffer and sort them:

            List<String> listOfMatchingPropertyNames = new LinkedList<String>();
            Enumeration propertyNamesEnumeration = properties.propertyNames();

            ConcatenationPrefix concatenationPrefix = field.getAnnotation(ConcatenationPrefix.class);
            if (propertyNamesEnumeration != null && concatenationPrefix != null)
            {
                String prefix = concatenationPrefix.prefix();

                while (propertyNamesEnumeration.hasMoreElements())
                {
                    String name = (String) propertyNamesEnumeration.nextElement();

                    if (name.startsWith(prefix))
                    {
                        listOfMatchingPropertyNames.add(name);
                    }
                }
            }

            Collections.sort(listOfMatchingPropertyNames);

            for(String name : listOfMatchingPropertyNames) {
                String value = properties.getProperty(name);
                lines.add(value);
            }

            if(lines.size() == 0) {
                return; // no relevant value in properties file, so leave bean defaults alone.
            }
        }

        Object replacementValue = null;

        if(java.util.Map.class.isAssignableFrom(field.getType())) {
            // we have a list but need a map. split each element into key/value pair.
            Map<String, String> map = new HashMap<String, String>();
            for(String element : lines) {
                String[] tokens = element.split("=");
                map.put(tokens[0], tokens[1]);
            }
            replacementValue = map;
        } else {
             // it stays as a list.
            replacementValue = lines;
        }

        Object valueFromBean = getter.invoke(bean, new Object[] {});

        if(!valueFromBean.equals(replacementValue)) {
            setter.invoke(bean, new Object[] {replacementValue});
        }
    }

    private static void handleSimpleProperty(Object bean, String instanceName, Properties properties, Field field, Method setter, Method getter)
            throws Exception
    {
        String prefix = null;
        if(bean.getClass().isAnnotationPresent(PropertyPrefix.class)) {
            PropertyPrefix prefixAnnotation = bean.getClass().getAnnotation(PropertyPrefix.class);
            prefix = prefixAnnotation.prefix();
        }

        String valueFromProperties = getValueFromProperties(bean, instanceName, properties, field, prefix);

        if(valueFromProperties != null) {

            Object valueFromBean = getter.invoke(bean, new Object[] {});

            if(field.getType().equals(Boolean.TYPE)) {

                if(!((Boolean)valueFromBean).booleanValue() && isPositive(valueFromProperties)) {
                    setter.invoke(bean, new Object[]{ Boolean.TRUE });
                }

                if(((Boolean)valueFromBean).booleanValue() && isNegative(valueFromProperties)) {
                    setter.invoke(bean, new Object[] { Boolean.FALSE});
                }

            } else if(field.getType().equals(String.class)) {

                if(!valueFromProperties.equals(valueFromBean)) {
                    setter.invoke(bean, new Object[] {valueFromProperties});
                }

            } else if(field.getType().equals(Long.TYPE)) {

                Long longValue = Long.valueOf(valueFromProperties);
                if(!longValue.equals(valueFromBean)) {
                    setter.invoke(bean, new Object[] {longValue});
                }

            } else if(field.getType().equals(Integer.TYPE)) {

                Integer intValue = Integer.valueOf(valueFromProperties);
                if(!intValue.equals(valueFromBean)) {
                    setter.invoke(bean, new Object[] {intValue});
                }

            } else {

                throw new Exception("unknown field type "+field.getType());

            }
        }
    }

    private static String getValueFromProperties(Object bean, String instanceName, Properties properties, Field field, String prefix)
    {
        String propertyFileKey;
        String valueFromProperties = null;

        if(valueFromProperties == null) {

            if(instanceName == null) {
                propertyFileKey = bean.getClass().getName()+"."+field.getName();
                valueFromProperties = properties.getProperty(propertyFileKey);
            }

            if(valueFromProperties == null) {
                propertyFileKey = bean.getClass().getName()+"."+instanceName+"."+field.getName();
                valueFromProperties = properties.getProperty(propertyFileKey);
            }
        }

        if(valueFromProperties == null) {

            if(instanceName == null) {
                propertyFileKey = bean.getClass().getSimpleName()+"."+field.getName();
                valueFromProperties = properties.getProperty(propertyFileKey);
            }

            if(valueFromProperties == null) {
                propertyFileKey = bean.getClass().getSimpleName()+"."+instanceName+"."+field.getName();
                valueFromProperties = properties.getProperty(propertyFileKey);
            }
        }

        if (valueFromProperties == null) {

            if(field.isAnnotationPresent(FullPropertyName.class)) {
                FullPropertyName fullPropertyName = field.getAnnotation(FullPropertyName.class);
                propertyFileKey = fullPropertyName.name();
            } else if(prefix != null) {
                propertyFileKey = prefix+field.getName();
            } else {
                propertyFileKey = field.getName();
            }

            valueFromProperties = properties.getProperty(propertyFileKey);
        }

        return valueFromProperties;
    }

    private static void printBean(Object bean, StringBuffer buffer)
    {
        String lineSeparator = System.getProperty("line.separator");
        buffer.append("Bean class: ");
        buffer.append(bean.getClass().getName());
        buffer.append(lineSeparator);

        for(Field field : bean.getClass().getDeclaredFields()) {
            Class type = field.getType();

            String getterMethodName;
            Method getter = null;
            if(field.getType().equals(Boolean.TYPE)) {
                getterMethodName = "is"+capitalizeFirstLetter(field.getName());
                try {
                    getter = bean.getClass().getMethod(getterMethodName, new Class[] {});
                } catch (NoSuchMethodException e) {}
            }

            try {
                if(getter == null) {
                    getterMethodName = "get"+capitalizeFirstLetter(field.getName());
                    getter = bean.getClass().getMethod(getterMethodName, new Class[] {});
                }

                Object valueFromBean = getter.invoke(bean, new Object[] {});

                buffer.append("  ");
                buffer.append(field.getName());
                buffer.append(": ");
                buffer.append(valueFromBean);
            } catch(Exception e) {
                buffer.append("failed to read property ");
                buffer.append(field.getName());
            }
            buffer.append(lineSeparator);
        }
    }

    private static String capitalizeFirstLetter(String string) {
        return (string.length()>0) ? (Character.toUpperCase(string.charAt(0))+string.substring(1)) : string;
    }

    private static boolean isPositive(String value) {
        return "YES".equalsIgnoreCase(value) || "ON".equalsIgnoreCase(value) || "TRUE".equalsIgnoreCase(value);
    }

    private static boolean isNegative(String value) {
        return "NO".equalsIgnoreCase(value) || "OFF".equalsIgnoreCase(value) || "FALSE".equalsIgnoreCase(value);
    }
}
