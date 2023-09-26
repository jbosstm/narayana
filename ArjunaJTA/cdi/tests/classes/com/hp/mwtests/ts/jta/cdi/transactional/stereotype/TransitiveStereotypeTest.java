/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.cdi.transactional.stereotype;

import jakarta.enterprise.inject.Stereotype;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TransitiveStereotypeTest {

    @Inject
    StereotypeBean stereotypeMethodBean;

    @Inject
    StereotypeTransitiveBean stereotypeTransitiveBean;

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class, "stereotype-test.war")
            .addClasses(StereotypeBean.class, StereotypeTransitiveBean.class, TestException.class,
                TransactionalNeverStereotype.class, TransactionalRequiredStereotype.class, TransitiveStereotypeTest.class,
                TransitiveToNeverNoTransactional.class, TransitiveToRequiredNoTransactional.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void neverStereotypeAtBean() throws Exception {
        stereotypeMethodBean.stereotypeRequiredAtBean();
    }

    @Test
    public void transactionalAtMethod() throws Exception {
        stereotypeMethodBean.transactionalAtMethod();
    }

    @Test(expected = TestException.class)
    public void transitiveAtBean() throws Exception {
        stereotypeTransitiveBean.process();
    }

    /**
     * <p>
     * This test is commented by intention as Weld does not define precise behavior
     * in state where {@link Stereotype} annoations are chained and the {@link Transactional}
     * annotation is defined transitively in several of them.
     * <p>
     * Currently in such case exception is thrown<br>
     * <code>WELD-000048: Conflicting interceptor bindings found on class</code>
     */
    // @Test(expected = TestException.class)
    public void transitiveAtBean2() throws Exception {
        // StereotypeWithTransactionalBean.process();
    }
}