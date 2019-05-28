/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
