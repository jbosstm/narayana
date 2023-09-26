/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.compensations.integration.beanManager;

import org.jboss.narayana.compensations.internal.BeanManagerUtil;
import org.junit.Assert;
import org.junit.Test;

import jakarta.enterprise.inject.spi.BeanManager;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public abstract class BeanManagerTest {

    @Test
    public void shouldGetBeanManager() {
        Assert.assertNotNull(BeanManagerUtil.getBeanManager());
    }

    @Test
    public void shouldCreateBean() {
        Assert.assertNotNull(BeanManagerUtil.createBeanInstance(DummyBean.class, getBeanManager()));
    }

    protected abstract BeanManager getBeanManager();
}