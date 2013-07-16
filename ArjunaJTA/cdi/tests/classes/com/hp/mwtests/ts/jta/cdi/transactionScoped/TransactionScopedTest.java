/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package com.hp.mwtests.ts.jta.cdi.transactionScoped;

import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * @author paul.robinson@redhat.com 01/05/2013
 */
@RunWith(Arquillian.class)
public class TransactionScopedTest {

    @Inject
    UserTransaction userTransaction;

    TransactionManager transactionManager;

    @Inject
    TestCDITransactionScopeBean testTxAssociationChangeBean;

    @Deployment
    public static JavaArchive createTestArchive() {

        return  ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addClass(TestCDITransactionScopeBean.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @After
    public void tearDown() {

        try {
            userTransaction.rollback();
        } catch (Exception e) {
            // do nothing
        }
    }

    //Based on test case from JTA 1.2 spec
    @Test
    public void testTxAssociationChange() throws Exception {

        transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();

        userTransaction.begin(); //tx1 begun
        testTxAssociationChangeBean.setValue(1);
        Transaction transaction = transactionManager.suspend();

        assertContextUnavailable();

        userTransaction.begin(); //tx2 begun
        Assert.assertEquals(0, testTxAssociationChangeBean.getValue());
        testTxAssociationChangeBean.setValue(2);
        userTransaction.commit();

        assertContextUnavailable();

        transactionManager.resume(transaction);
        Assert.assertEquals(1, testTxAssociationChangeBean.getValue());
        userTransaction.commit();

        assertContextUnavailable();
    }

    private void assertContextUnavailable() {

        try {
            testTxAssociationChangeBean.getValue();
            Assert.fail("Accessing bean should have thrown a ContextNotActiveException as it should not be available");
        } catch (ContextNotActiveException e) {
            //Expected
        }
    }


}