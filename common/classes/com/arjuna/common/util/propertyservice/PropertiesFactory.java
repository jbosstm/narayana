/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.common.util.propertyservice;

import java.util.Properties;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class PropertiesFactory {

    private static AbstractPropertiesFactory delegatePropertiesFactory;

    /**
     * Allow the properties factory delegate to be supplied externally. This
     * is useful for containers like Quarkus that need to statically initialise
     * properties during a deployment phase.
     *
     * @param propertiesFactory a factory for providing default values for properties
     */
    public static void setDelegatePropertiesFactory(AbstractPropertiesFactory propertiesFactory) {
        delegatePropertiesFactory = propertiesFactory;
    }

    public static Properties getDefaultProperties() {
        initPropertiesFactory();
        return delegatePropertiesFactory.getDefaultProperties();
    }

    public static Properties getPropertiesFromFile(String propertyFileName, ClassLoader classLoader) {
        initPropertiesFactory();
        return delegatePropertiesFactory.getPropertiesFromFile(propertyFileName, classLoader);
    }

    private static void initPropertiesFactory() {
        if (delegatePropertiesFactory != null) {
            return;
        }

        synchronized (PropertiesFactory.class) {
            if (delegatePropertiesFactory == null) {
                if (isStaxAvailable()) {
                    delegatePropertiesFactory = new PropertiesFactoryStax();
                } else {
                    delegatePropertiesFactory = new PropertiesFactorySax();
                }
            }
        }
    }

    private static boolean isStaxAvailable() {
        try {
            Class.forName("javax.xml.stream.XMLInputFactory", false, PropertiesFactory.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            return false;
        }

        return true;
    }
}