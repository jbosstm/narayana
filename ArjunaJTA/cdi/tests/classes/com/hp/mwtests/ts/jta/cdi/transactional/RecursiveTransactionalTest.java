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

import java.util.concurrent.ExecutorService;
import jakarta.annotation.Resource;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <p>
 * Implements various scenarios reproducing bug
 * <a href="https://issues.jboss.org/browse/JBTM-2350">JBTM-2350</a>.
 * </p>
 * <p>
 * Test weather transaction management type(user transaction availability) is correctly restored when method
 * annotated {@link Transactional @Transactional} is invoked recursively.
 * </p>
 *
 * @author <a href="mailto:Tomasz%20Krakowiak%20%3ctomasz.krakowiak@efish.pl%3c">Tomasz Krakowiak
 *         &lt;tomasz.krakowiak@efish.pl&gt;</a>
 */
@RunWith(Arquillian.class)
public class RecursiveTransactionalTest {

    public static final Runnable DO_NOTHING = new Runnable() {
        @Override
        public void run() {
        }
    };

    @Inject
    TestTransactionalInvokerHelper helper;

    @Resource(name = "java:comp/DefaultManagedExecutorService")
    ExecutorService executorService;

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage("com.hp.mwtests.ts.jta.cdi.transactional")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testRecursiveTxRequired() {
        doTest(TxType.REQUIRED);
    }

    @Test
    public void testRecursiveTxRequiresNew() {
        doTest(TxType.REQUIRES_NEW);
    }

    @Test
    public void testRecursiveTxMandatory() {
        doTest(TxType.MANDATORY);
    }

    @Test
    public void testRecursiveTxSupports() {
        doTest(TxType.SUPPORTS);
    }

    @Test
    public void testRecursiveTxNotSupported() {
        doTest(TxType.NOT_SUPPORTED);
    }

    @Test
    public void testRecursiveTxNever() {
        doTest(TxType.NEVER);
    }

    private void doTest(TxType txType) {
        TransactionManagementType oppositeTransactionManagementType = oppositeTransactionManagementType(txType);
        boolean startTransaction = txType == TxType.MANDATORY;
        boolean expectedAvailable = oppositeTransactionManagementType == TransactionManagementType.BEAN;
        Runnable runnable = helper.runWithTransactionManagement(oppositeTransactionManagementType, startTransaction,
                helper.runAndCheckUserTransactionAvailability(
                        helper.runInTxType(txType,
                                helper.runInTxType(txType, DO_NOTHING)
                        ),
                        expectedAvailable
                )
        );
        runnable.run();
    }

    private TransactionManagementType oppositeTransactionManagementType(TxType txType) {
        switch (txType) {
            case REQUIRED:
            case REQUIRES_NEW:
            case MANDATORY:
            case SUPPORTS:
                return TransactionManagementType.BEAN;
            case NOT_SUPPORTED:
            case NEVER:
                return TransactionManagementType.CONTAINER;
            default:
                throw new RuntimeException("Unexpected tx type " + txType);
        }
    }
}
