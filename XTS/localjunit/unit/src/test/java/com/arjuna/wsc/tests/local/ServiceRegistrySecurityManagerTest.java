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
