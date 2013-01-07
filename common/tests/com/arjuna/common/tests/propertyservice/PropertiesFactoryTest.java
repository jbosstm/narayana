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
package com.arjuna.common.tests.propertyservice;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arjuna.common.util.propertyservice.AbstractPropertiesFactory;
import com.arjuna.common.util.propertyservice.PropertiesFactorySax;
import com.arjuna.common.util.propertyservice.PropertiesFactoryStax;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class PropertiesFactoryTest {

    private static final String PROPERTIES_FILE_NAME = "properties-factory-test.xml";

    private Properties expectedProperties;

    @Before
    public void setUp() {
        expectedProperties = new Properties();
        expectedProperties.put("CoordinatorEnvironmentBean.commitOnePhase", "YES");
        expectedProperties.put("ObjectStoreEnvironmentBean.objectStoreDir", "PutObjectStoreDirHere");
        expectedProperties.put("CoreEnvironmentBean.socketProcessIdPort", "0");
        expectedProperties.put("RecoveryEnvironmentBean.recoveryModuleClassNames", "com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule\n" +
                "        com.arjuna.ats.internal.txoj.recovery.TORecoveryModule");
        expectedProperties.put("RecoveryEnvironmentBean.expiryScannerClassNames", "com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner");
        expectedProperties.put("RecoveryEnvironmentBean.recoveryPort", "4712");
        expectedProperties.put("RecoveryEnvironmentBean.recoveryAddress", "");

        System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile", PROPERTIES_FILE_NAME);
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

    private void assertProperties(final Properties expectedProperties, final Properties actualProperties) {
        for (Object key : expectedProperties.keySet()) {
            if (key instanceof String) {
                String expectedValue = expectedProperties.getProperty((String) key);
                String actualValue = actualProperties.getProperty((String) key);
                Assert.assertEquals("Testing value with key=" + key, expectedValue, actualValue);
            }
        }
    }

}
