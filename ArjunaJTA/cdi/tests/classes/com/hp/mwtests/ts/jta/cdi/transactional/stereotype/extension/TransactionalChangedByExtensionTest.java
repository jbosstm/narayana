/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.cdi.transactional.stereotype.extension;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TransactionalChangedByExtensionTest {
    @Inject
    NoAnnotationBean bean;

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class, "transactional-test.war")
            .addPackage(TransactionalChangedByExtensionTest.class.getPackage())
            .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\"></beans>"), "beans.xml")
            .addAsServiceProvider(jakarta.enterprise.inject.spi.Extension.class, AddTransactionalAnnotationExtension.class);
    }


    @Test(expected = RuntimeException.class)
    public void transactionalAddedByExtensionAtBean() throws Exception {
        bean.process();
    }
}