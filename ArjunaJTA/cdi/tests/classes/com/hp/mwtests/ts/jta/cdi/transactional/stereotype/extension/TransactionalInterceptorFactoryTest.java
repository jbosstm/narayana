/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.cdi.transactional.stereotype.extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

@RunWith(Arquillian.class)
public class TransactionalInterceptorFactoryTest {
    @Inject
    TransactionalTestServiceInterceptorFactory.TestService testService;

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class, "transactional-interceptor-factory-test.war")
                .addPackage(TransactionalChangedByExtensionTest.class.getPackage())
                .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\"></beans>"), "beans.xml");
    }

    @Test
    public void workWithTestService() throws Exception {
        Assert.assertNotNull("Expecting the producer provides the test service bean", testService);
        Assert.assertEquals("The test service is expected to return status of a no active transaction",
                0, testService.doTransactional());
    }
}