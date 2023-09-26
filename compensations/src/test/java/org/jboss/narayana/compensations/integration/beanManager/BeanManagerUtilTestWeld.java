/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.compensations.integration.beanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.compensations.internal.BeanManagerUtil;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Arquillian.class)
public class BeanManagerUtilTestWeld extends BeanManagerTest {

    @Inject
    private BeanManager beanManager;

    @Deployment
    public static WebArchive getDeployment() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class)
                .addClasses(BeanManagerUtil.class, BeanManagerTest.class, DummyBean.class)
                .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\"></beans>"), "beans.xml");

        System.out.println("Test archive: " + archive.toString(true));

        return archive;
    }

    @Override
    protected BeanManager getBeanManager() {
        return beanManager;
    }
}