/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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