/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.tm;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.transaction.*;

/**
 * Transaction SPI integration tests
 */
@RunWith(Arquillian.class)
public class TestSPI {
    @Inject
    private javax.transaction.TransactionManager transactionManager;

    @Deployment
    public static WebArchive createTestArchive() {

        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage("org.jboss.tm")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void sanityCheck() {
        Assert.assertTrue(Boolean.TRUE);
    }

    @Test
    @org.junit.Ignore("j9 TODO Deployment Cannot deploy: test.war")
    public void testTimeouts() throws Exception {
        Transaction oldTxn = null;
        final int SLEEP = 5;
        final int timeout = 2;
        final boolean[] afterCompletionViaTMTimeoutThread = {false};

        try {
            oldTxn = transactionManager.suspend();
            transactionManager.setTransactionTimeout(timeout);
            transactionManager.begin();

            Transaction newTxn = transactionManager.getTransaction();

            newTxn.registerSynchronization(new Synchronization() {
                @Override
                public void beforeCompletion() {
                }

                @Override
                public void afterCompletion(int status) {
                    System.out.printf("afterCompletion callback. Txn Status=%d%n", status);
                    afterCompletionViaTMTimeoutThread[0] = TxUtils.isTransactionManagerTimeoutThread();
                    Assert.assertTrue("afterCompletion synchronization not called via TransactionManagerTimeoutThread",
                            afterCompletionViaTMTimeoutThread[0]);
                }
            });

            Thread.sleep(SLEEP * 1000); // sleep for longer than the transaction timeout period
            int status = transactionManager.getStatus();

            Assert.assertTrue("ERROR transaction should have timed out but its state is " + status,
                    status == Status.STATUS_ROLLEDBACK);

            transactionManager.commit();

            Assert.assertTrue("testTimeouts synchronization was not invoked", afterCompletionViaTMTimeoutThread[0]);
        } catch (RollbackException e) {
            // SUCCESS: rollback exception expected
            // any other exception is a failure
        } finally {
            try {
                if (transactionManager.getStatus() == Status.STATUS_ACTIVE)
                    transactionManager.rollback();
            } catch (SystemException e) {
                System.err.printf("testTimeouts: cleanup exception %s%n", e.getMessage());
            }
            if (oldTxn != null) {
                try {
                    transactionManager.resume(oldTxn);
                } catch (SystemException e) {
                    System.err.printf("testTimeouts: resume exception %s%n", e.getMessage());
                } catch (InvalidTransactionException e) {
                    System.err.printf("testTimeouts: resume exception %s%n", e.getMessage());
                }
            }
        }

    }
}
