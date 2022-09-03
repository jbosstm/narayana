/*
 *
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 */

package com.hp.mwtests.ts.jta.cdi.transactional.stereotype.ejb;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;
import jakarta.transaction.Status;

/**
 * When using Weld then Narayana switches to use Weld API to access the list of the {@code @Transactional} annotations.
 * This is done via {@code InvocationContext.getContextData("org.jboss.weld.interceptor.bindings")}.
 * In some particular cases where EJB is involved it may happen the Weld is not able to provide the {@code @Transactional} annotations.
 *
 * This test aims to hit this corner case and check that when Weld does not provide the annotation then
 * Narayana will use a different way to get the annotation correctly and it intercepts the call.
 *
 * @see <a href="https://issues.redhat.com/browse/WFLY-14924">WFLY-14924</a>
 */
@RunWith(Arquillian.class)
public class CdiWeldFailureWithEjbTest {

    @Inject
    StatelessEjbCaller beanB;

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class, "weld-failure-wit-ejb.war")
                .addPackage(CdiWeldFailureWithEjbTest.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void workWithTestService() {
        Assert.assertNotNull("Expecting bean injection from arquillian works fine", beanB);
        Assert.assertEquals("Expecting the transactional EJB returns that the transaction is active",
                Status.STATUS_ACTIVE, beanB.doWork());
    }
}
