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

import com.hp.mwtests.ts.jta.cdi.transactional.stereotype.TransactionalRequiredStereotype;

@RunWith(Arquillian.class)
public class StereotypeChangedByExtensionTest {
    @Inject
    NoAnnotationBean bean;

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class, "stereotype-test.war")
            .addPackage(StereotypeChangedByExtensionTest.class.getPackage())
            .addClasses(TransactionalRequiredStereotype.class)
            .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\"></beans>"), "beans.xml")
            .addAsServiceProvider(jakarta.enterprise.inject.spi.Extension.class, AddStereotypeAnnotationExtension.class);
    }


    @Test(expected = RuntimeException.class)
    public void stereotypeAddedByExtensionAtBean() throws Exception {
        bean.process();
    }
}