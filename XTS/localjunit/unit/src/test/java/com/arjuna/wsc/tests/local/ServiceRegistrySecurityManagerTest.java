/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.wsc.tests.local;

import com.arjuna.webservices11.ServiceRegistry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ServiceRegistrySecurityManagerTest {

    private static final String SERVICE_REGISTRY_PERMISSION_NAME = ServiceRegistry.class.getName() + ".getRegistry";

    private SecurityManager oldSecurityManager;

    @Before
    public void before() {
        oldSecurityManager = System.getSecurityManager();
    }

    @After
    public void after() {
        System.setSecurityManager(oldSecurityManager);
    }

    @Test
    public void testWithoutSecurityManager() {
        System.setSecurityManager(null);
        Assert.assertNotNull(ServiceRegistry.getRegistry());
    }

    @Test
    public void testWithSecurityManager() {
        final SinglePermissionSecurityManager testSecurityManager = new SinglePermissionSecurityManager(SERVICE_REGISTRY_PERMISSION_NAME);
        System.setSecurityManager(testSecurityManager);

        Assert.assertNotNull(ServiceRegistry.getRegistry());
        Assert.assertTrue(testSecurityManager.wasCalled());
    }

}