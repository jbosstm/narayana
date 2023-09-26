/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.common.tests.propertyservice;

import org.junit.Assert;

import java.util.Properties;

public final class PropertiesFactoryUtil {
    static final String PROPERTIES_FILE_NAME = "properties-factory-test.xml";

    static Properties getExpectedProperties() {
        // setup XML file that the expected properties are defined at
        System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile", PROPERTIES_FILE_NAME);

        Properties expectedProperties = new Properties();
        expectedProperties.put("CoordinatorEnvironmentBean.commitOnePhase", "YES");
        expectedProperties.put("ObjectStoreEnvironmentBean.objectStoreDir", "PutObjectStoreDirHere");
        expectedProperties.put("CoreEnvironmentBean.socketProcessIdPort", "0");
        expectedProperties.put("RecoveryEnvironmentBean.recoveryModuleClassNames", "com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule\n" +
                "        com.arjuna.ats.internal.txoj.recovery.TORecoveryModule");
        expectedProperties.put("RecoveryEnvironmentBean.expiryScannerClassNames", "com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner");
        expectedProperties.put("RecoveryEnvironmentBean.recoveryPort", "4712");
        expectedProperties.put("RecoveryEnvironmentBean.recoveryAddress", "");
        return expectedProperties;
    }

    static void assertProperties(final Properties expectedProperties, final Properties actualProperties) {
        for (Object key : expectedProperties.keySet()) {
            if (key instanceof String) {
                String expectedValue = expectedProperties.getProperty((String) key);
                String actualValue = actualProperties.getProperty((String) key);
                Assert.assertEquals("Testing value with key=" + key, expectedValue, actualValue);
            }
        }
    }
}