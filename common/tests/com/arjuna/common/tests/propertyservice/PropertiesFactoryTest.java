/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.common.tests.propertyservice;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.arjuna.common.util.propertyservice.AbstractPropertiesFactory;
import com.arjuna.common.util.propertyservice.PropertiesFactorySax;
import com.arjuna.common.util.propertyservice.PropertiesFactoryStax;

import static com.arjuna.common.tests.propertyservice.PropertiesFactoryUtil.assertProperties;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class PropertiesFactoryTest {
    private Properties expectedProperties;

    @Before
    public void setUp() {
        expectedProperties = PropertiesFactoryUtil.getExpectedProperties();

        // property not defined in XML file but defined as system property should be found as well
        System.setProperty("CoordinatorEnvironmentBean.dynamic1PC", "false");
        expectedProperties.put("CoordinatorEnvironmentBean.dynamic1PC", "false");
    }

    @Test
    public void testGetDefaultPropertiesWithStax() {
        final AbstractPropertiesFactory propertiesFactory = new PropertiesFactoryStax();
        final Properties properties = propertiesFactory.getDefaultProperties();

        assertProperties(expectedProperties, properties);
    }

    @Test
    public void testGetDefaultPropertiesWithSax() {
        final AbstractPropertiesFactory propertiesFactory = new PropertiesFactorySax();
        final Properties properties = propertiesFactory.getDefaultProperties();

        assertProperties(expectedProperties, properties);
    }

}