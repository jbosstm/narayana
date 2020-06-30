/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020, Red Hat, Inc., and individual contributors
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

package com.arjuna.common.tests.propertyservice;

import com.arjuna.common.tests.propertyservice.PropertiesFactoryUtil;
import com.arjuna.common.util.propertyservice.AbstractPropertiesFactory;
import com.arjuna.common.util.propertyservice.PropertiesFactorySax;
import com.arjuna.common.util.propertyservice.PropertiesFactoryStax;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static com.arjuna.common.tests.propertyservice.PropertiesFactoryUtil.assertProperties;

/**
 * Verification of the property processing when the system property contains an empty property.
 */
public class EmptyPropertiesFactoryTest {
    private Properties expectedProperties;

    @Before
    public void setUp() {
        Properties props = new Properties();
        // setup the empty system property
        props.setProperty("", "");
        props.putAll(System.getProperties()); // preserve all JDK properties
        System.setProperties(props);

        expectedProperties = PropertiesFactoryUtil.getExpectedProperties();
    }

    @Test
    public void testGetWithEmptyPropertyWithStax() {
        final AbstractPropertiesFactory propertiesFactory = new PropertiesFactoryStax();
        final Properties properties = propertiesFactory.getDefaultProperties();

        assertProperties(expectedProperties, properties);
    }

    @Test
    public void testGetWithEmptyPropertyWithSax() {
        final AbstractPropertiesFactory propertiesFactory = new PropertiesFactorySax();
        final Properties properties = propertiesFactory.getDefaultProperties();

        assertProperties(expectedProperties, properties);
    }
}
