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

package com.hp.mwtests.ts.jta.cdi.transactional;

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

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionalException;
import javax.transaction.UserTransaction;

/**
 * @author paul.robinson@redhat.com 01/05/2013
 */
@RunWith(Arquillian.class)
public class TransactionalImplTest {

    @Inject
    UserTransaction userTransaction;

    @Inject
    TestTransactionalBean testTransactionalBean;

    @Deployment
    public static JavaArchive createTestArchive() {

        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackage("com.hp.mwtests.ts.jta.cdi.transactional")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

        String ManifestMF = "Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.jts export services\n";

        archive.setManifest(new StringAsset(ManifestMF));

        return archive;
    }

    @After
    public void tearDown() {

        AssertionParticipant.reset();
        try {
            userTransaction.rollback();
        } catch (Exception e) {
            // do nothing
        }
    }

    @Test
    public void testDefaultNoExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithDefault();
        Utills.assertTransactionActive(false);

        AssertionParticipant.assertCommitted();
    }

    @Test
    public void testDefaultExistingTX() throws Exception {

        TransactionManager tm = Utills.getTransactionManager();

        Utills.assertTransactionActive(false);
        tm.begin();
        Transaction tx = Utills.getCurrentTransaction();
        Utills.assertTransactionActive(true);

        testTransactionalBean.invokeWithDefault(Utills.getCurrentTransaction());
        Utills.assertSameTransaction(tx);

        Utills.assertTransactionActive(true);
        tm.commit();
        Utills.assertTransactionActive(false);

        AssertionParticipant.assertCommitted();
    }

    @Test
    public void testDefaultThrowRuntimeException() throws Throwable {

        Utills.assertTransactionActive(false);

        try {
            testTransactionalBean.invokeWithDefault(RuntimeException.class);
            Assert.fail("Expected RuntimeException to be thrown, but it was not");
        } catch (RuntimeException e) {
            //expected
        }

        Utills.assertTransactionActive(false);

        AssertionParticipant.assertRolledBack();
    }

    @Test
    public void testDefaultThrowException() throws Throwable {

        Utills.assertTransactionActive(false);

        try {
            testTransactionalBean.invokeWithDefault(Exception.class);
            Assert.fail("Expected Exception to be thrown, but it was not");
        } catch (Exception e) {
            //expected
        }

        Utills.assertTransactionActive(false);

        AssertionParticipant.assertCommitted();
    }

    @Test
    public void testDefaultWithRollbackOn() throws Throwable {

        Utills.assertTransactionActive(false);

        try {
            testTransactionalBean.invokeWithDefaultAndRollbackOn(TestException.class);
            Assert.fail("Expected Exception to be thrown, but it was not");
        } catch (TestException e) {
            //expected
        }

        Utills.assertTransactionActive(false);

        AssertionParticipant.assertRolledBack();
    }


    @Test
    public void testDefaultWithDontRollbackOn() throws Throwable {

        Utills.assertTransactionActive(false);

        try {
            testTransactionalBean.invokeWithDefaultAndDontRollbackOn(TestRuntimeException.class);
            Assert.fail("Expected Exception to be thrown, but it was not");
        } catch (TestRuntimeException e) {
            //expected
        }

        Utills.assertTransactionActive(false);

        AssertionParticipant.assertCommitted();
    }


    @Test
    public void testDefaultWithDoAndDontRollbackOn() throws Throwable {

        Utills.assertTransactionActive(false);

        try {
            testTransactionalBean.invokeWithDefaultAndDoAndDontRollbackOn(TestException.class);
            Assert.fail("Expected Exception to be thrown, but it was not");
        } catch (TestException e) {
            //expected
        }

        Utills.assertTransactionActive(false);

        AssertionParticipant.assertCommitted();
    }


    @Test
    public void testRequiresNewNoExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithRequiresNew(null);
        Utills.assertTransactionActive(false);

        AssertionParticipant.assertCommitted();
    }


    @Test
    public void testRequiresNewExistingTX() throws Exception {

        TransactionManager tm = Utills.getTransactionManager();

        Utills.assertTransactionActive(false);
        tm.begin();
        Transaction tx = Utills.getCurrentTransaction();

        Utills.assertTransactionActive(true);
        testTransactionalBean.invokeWithRequiresNew(tx);
        Utills.assertTransactionActive(true);

        tm.commit();
        Utills.assertTransactionActive(false);

        AssertionParticipant.assertCommitted();
    }

    @Test
    public void testMandatoryExistingTX() throws Exception {

        TransactionManager tm = Utills.getTransactionManager();

        Utills.assertTransactionActive(false);
        tm.begin();
        Transaction tx = Utills.getCurrentTransaction();

        Utills.assertTransactionActive(true);
        testTransactionalBean.invokeWithMandatory(tx);
        Utills.assertTransactionActive(true);

        tm.commit();
        Utills.assertTransactionActive(false);

        AssertionParticipant.assertCommitted();
    }

    @Test
    public void testMandatoryNoExistingTX() throws Exception {


        Utills.assertTransactionActive(false);
        try {
            testTransactionalBean.invokeWithMandatory(null);
            Assert.fail("Expected Exception to be thrown, but it was not");
        } catch (TransactionalException e) {
            //expected
        }
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testSupportsExistingTX() throws Exception {

        TransactionManager tm = Utills.getTransactionManager();

        Utills.assertTransactionActive(false);
        tm.begin();
        Transaction tx = Utills.getCurrentTransaction();

        Utills.assertTransactionActive(true);
        testTransactionalBean.invokeWithSupports(tx);
        Utills.assertTransactionActive(true);

        tm.commit();
        Utills.assertTransactionActive(false);

        AssertionParticipant.assertCommitted();
    }

    @Test
    public void testSupportsNoExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithSupports(null);
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testNotSupportedExistingTX() throws Exception {

        TransactionManager tm = Utills.getTransactionManager();

        Utills.assertTransactionActive(false);
        tm.begin();
        Utills.assertTransactionActive(true);

        testTransactionalBean.invokeWithNotSupported();

        Utills.assertTransactionActive(true);
        tm.commit();
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testNotSupportedNoExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithNotSupported();
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testNeverExistingTX() throws Exception {

        TransactionManager tm = Utills.getTransactionManager();

        Utills.assertTransactionActive(false);
        tm.begin();
        Utills.assertTransactionActive(true);

        try {
            testTransactionalBean.invokeWithNever();
            Assert.fail("Expected Exception to be thrown, but it was not");
        } catch (TransactionalException e) {
            //expected
        }

        Utills.assertTransactionActive(true);
        tm.commit();
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testNeverNoExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithNever();
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testClassLevelDefaultNeverExistingTX() throws Exception {

        TransactionManager tm = Utills.getTransactionManager();

        Utills.assertTransactionActive(false);
        tm.begin();
        Utills.assertTransactionActive(true);

        try {
            testTransactionalBean.invokeWithCLassLevelDefault();
            Assert.fail("Expected Exception to be thrown, but it was not");
        } catch (TransactionalException e) {
            //expected
        }

        Utills.assertTransactionActive(true);
        tm.commit();
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testClassLevelDefaultNeverNoExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithCLassLevelDefault();
        Utills.assertTransactionActive(false);
    }

    @Test(expected = IllegalStateException.class)
    public void testUseUserTransactionInRequired() throws Exception {

        testTransactionalBean.invokeWithRequiredUseUserTransaction();
    }

    @Test
    public void testUseUserTransactionInNever() throws Exception {

        testTransactionalBean.invokeWithNeverUseUserTransaction();
    }

    @Test(expected = IllegalStateException.class)
    public void testUseUserTransactionInRequiresNew() throws Exception {

        testTransactionalBean.invokeWithRequiresNewUseUserTransaction();
    }

    @Test(expected = IllegalStateException.class)
    public void testUseUserTransactionInMandatory() throws Exception {

        TransactionManager tm = Utills.getTransactionManager();
        tm.begin();

        testTransactionalBean.invokeWithMandatoryUseUserTransaction();
    }

    @Test(expected = IllegalStateException.class)
    public void testUseUserTransactionInSupports() throws Exception {

        TransactionManager tm = Utills.getTransactionManager();
        tm.begin();

        testTransactionalBean.invokeWithSupportsUseUserTransaction();
    }

    @Test
    public void testUseUserTransactionInNotSupported() throws Exception {

        testTransactionalBean.invokeWithNotSupportedUseUserTransaction();
    }

    @Test
    public void testEJB() throws Exception {

        testTransactionalBean.invokeEJB();
    }
}