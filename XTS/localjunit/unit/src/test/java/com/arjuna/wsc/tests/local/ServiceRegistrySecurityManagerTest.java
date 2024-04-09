/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.wsc.tests.local;

import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arjuna.webservices11.ServiceRegistry;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 *         Remove this unit test class once JDK21 is the minimum JDK
 *
 *         JDK21 throws UnsupportedOperationException when
 *         System.setSecurityManager is called, see https://openjdk.org/jeps/411
 */
public class ServiceRegistrySecurityManagerTest {

    private static final String SERVICE_REGISTRY_PERMISSION_NAME = ServiceRegistry.class.getName() + ".getRegistry";
    private final Logger logger = Logger.getLogger(ServiceRegistrySecurityManagerTest.class);
    @SuppressWarnings("removal")
    private SecurityManager oldSecurityManager;
    @SuppressWarnings("removal")
    @Before
    public void before() {
        oldSecurityManager = System.getSecurityManager();
    }

    @SuppressWarnings("removal")
    @After
    public void after() {
        try {
            System.setSecurityManager(oldSecurityManager);
        }
        catch (UnsupportedOperationException e) {
            logger.warn("setting the SecurityManager is no longer supported", e);
        }
    }

    @SuppressWarnings("removal")
    @Test
    public void testWithoutSecurityManager() {
        try {
            System.setSecurityManager(null);
            Assert.assertNotNull(ServiceRegistry.getRegistry());
        }
        catch (UnsupportedOperationException e) {
            logger.warn("setting the SecurityManager is no longer supported", e);
        }
    }

    @SuppressWarnings("removal")
    @Test
    public void testWithSecurityManager() {
        final SinglePermissionSecurityManager testSecurityManager = new SinglePermissionSecurityManager(
                SERVICE_REGISTRY_PERMISSION_NAME);
        try {
            System.setSecurityManager(testSecurityManager);
            Assert.assertNotNull(ServiceRegistry.getRegistry());
            Assert.assertTrue(testSecurityManager.wasCalled());
        }
        catch (UnsupportedOperationException e) {
            logger.warn("setting the SecurityManager is no longer supported", e);
        }
    }
}