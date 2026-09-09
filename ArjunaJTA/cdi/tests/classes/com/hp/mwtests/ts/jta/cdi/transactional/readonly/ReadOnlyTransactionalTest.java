/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.cdi.transactional.readonly;

import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.Transactional;
import jakarta.transaction.TransactionalException;
import jakarta.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.hp.mwtests.ts.jta.cdi.transactional.stereotype.extension.AnnotatedTypeWrapper;

/**
 * Behaviour of a bean whose Transactional annotation is read-only (added by
 * extension, since the attribute cannot be written in source against
 * jakarta.transaction-api 2.0.1).
 *
 * Expectations are keyed off the transaction API available at runtime in the
 * container: with the read-only API (2.0.2+) a read-only method's transaction
 * can only roll back and a propagation mismatch is rejected; without it the
 * read-only marker is invisible and behaviour is exactly that of a plain
 * REQUIRED method. The same tests therefore stay valid before and after the
 * EE11 API bump.
 */
@RunWith(Arquillian.class)
public class ReadOnlyTransactionalTest {

    @Inject
    ReadOnlyBean bean;

    @Inject
    UserTransaction userTransaction;

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class, "readonly-test.war")
                .addPackage(ReadOnlyTransactionalTest.class.getPackage())
                .addClass(AnnotatedTypeWrapper.class)
                .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\"></beans>"), "beans.xml")
                .addAsServiceProvider(jakarta.enterprise.inject.spi.Extension.class, AddReadOnlyTransactionalExtension.class);
    }

    @After
    public void tearDown() {
        ReadOnlyBean.reset();
        try {
            userTransaction.rollback();
        } catch (Exception e) {
            // do nothing
        }
    }

    @Test
    public void testReadOnlyMethodCompletesWithRollback() throws Exception {
        ReadOnlyBean.reset();

        bean.run();

        // a read-only transaction can only roll back; without the read-only
        // API the marker is invisible and the transaction commits
        int expected = readOnlyApiPresent() ? Status.STATUS_ROLLEDBACK : Status.STATUS_COMMITTED;

        Assert.assertEquals(expected, ReadOnlyBean.completionStatus);

        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, userTransaction.getStatus());
    }

    @Test
    public void testReadOnlyMismatchWithCallerTransaction() throws Exception {
        userTransaction.begin();

        try {
            if (readOnlyApiPresent()) {
                try {
                    bean.runWithoutSynchronization();

                    Assert.fail("a read-only method joining a read-write transaction must throw TransactionalException");
                } catch (TransactionalException e) {
                    // expected
                }
            } else {
                // without the read-only API both flags read false, so the
                // method simply joins the caller's transaction
                bean.runWithoutSynchronization();
            }

            Assert.assertEquals(Status.STATUS_ACTIVE, userTransaction.getStatus());
        } finally {
            userTransaction.rollback();
        }
    }

    private static boolean readOnlyApiPresent() {
        try {
            Transactional.class.getMethod("isReadOnly");

            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
