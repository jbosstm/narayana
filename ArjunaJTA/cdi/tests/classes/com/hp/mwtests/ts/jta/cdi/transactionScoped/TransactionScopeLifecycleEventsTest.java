/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019, Red Hat, Inc., and individual contributors
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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;

import javax.enterprise.context.spi.Context;

import javax.enterprise.event.Observes;

import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.BeanManager;

import javax.inject.Inject;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.Transactional;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionScoped;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;

import org.jboss.arquillian.junit.Arquillian;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;

import org.jboss.shrinkwrap.api.asset.EmptyAsset;

import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
@RunWith(Arquillian.class)
@ApplicationScoped
public class TransactionScopeLifecycleEventsTest {
    private static final Logger log = Logger.getLogger(TransactionScopeLifecycleEventsTest.class);

    private static boolean initializedObserved;
    private static boolean beforeDestroyedObserved;
    private static boolean destroyedObserved;
    private static int finalStatus;

    @Inject
    private TransactionManager transactionManager;

    @Inject
    private TransactionScopeLifecycleEventsTest self;

    @Inject
    TransactionSynchronizationRegistry registry;

    @Inject
    UserTransaction userTransaction;

    @Deployment
    public static JavaArchive createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class, "test.jar")
            .addClass(TransactionScopeLifecycleEventsTest.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Before
    public void init() {
        initializedObserved = false;
        beforeDestroyedObserved = false;
        destroyedObserved = false;
        finalStatus = Status.STATUS_UNKNOWN;
    }

    @Transactional
    void doSomethingTransactional() {
        registry.registerInterposedSynchronization(new TestSync());
    }

    void transactionScopeActivated(@Observes @Initialized(TransactionScoped.class) final Object initializedEvent,
                                   final BeanManager beanManager)
        throws SystemException {
        assertNotNull(initializedEvent);
        assertTrue(initializedEvent instanceof Transaction);
        assertNotNull(beanManager);
        assertNotNull(this.transactionManager);
        final Transaction transaction = this.transactionManager.getTransaction();
        assertNotNull(transaction);
        assertEquals(Status.STATUS_ACTIVE, transaction.getStatus());
        final Context transactionContext = beanManager.getContext(TransactionScoped.class);
        assertNotNull(transactionContext);
        assertTrue(transactionContext.isActive());
        initializedObserved = true;
    }

    void transactionScopeDectivated(@Observes @Destroyed(TransactionScoped.class) final Object destroyedEvent,
                                    final BeanManager beanManager) throws SystemException {
        assertNotNull(destroyedEvent);
        assertNotNull(beanManager);
        assertNotNull(this.transactionManager);
        assertNull(this.transactionManager.getTransaction());
        try {
            beanManager.getContext(TransactionScoped.class);
            fail();
        } catch (final ContextNotActiveException expected) {

        }
        destroyedObserved = true;
    }

    void transactionScopeBeforeDectivated(@Observes @BeforeDestroyed(TransactionScoped.class) final Object beforeDestroyedEvent,
                                    final BeanManager beanManager) throws SystemException {
        assertNotNull(beforeDestroyedEvent);
        assertTrue(beforeDestroyedEvent instanceof Transaction);
        assertNotNull(beanManager);
        assertNotNull(this.transactionManager);
        assertNotNull(this.transactionManager.getTransaction());
        final Context transactionContext = beanManager.getContext(TransactionScoped.class);
        assertNotNull(transactionContext);
        assertTrue(transactionContext.isActive());
        assertTrue(beforeDestroyedEvent instanceof Transaction);
        beforeDestroyedObserved = true;
    }

    void transactionScopeBeforeCompletion(@Observes(during = TransactionPhase.BEFORE_COMPLETION) Object ev) {
        try {
            // sleep for longer than the transaction timeout
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            if (log.isDebugEnabled()) {
                log.debug("transactionScopeBeforeCompletion: interrupted Thread.sleep");
            }
        }
    }

    @Test
    public void testEffects() throws SystemException {
        userTransaction.setTransactionTimeout(60); // because testRollbackDuringSynchronization sets it to 1 to force timeout
        self.doSomethingTransactional();
        assertTrue("Expected observed @Initialized(TransactionScoped.class)", initializedObserved);
        assertTrue("Expected observed @BeforeDestroyed(TransactionScoped.class)", beforeDestroyedObserved);
        assertTrue("Expected observed @Destroyed(TransactionScoped.class)", destroyedObserved);
    }

    @Test
    public void testRollbackDuringSynchronization() throws SystemException {
        finalStatus = Status.STATUS_UNKNOWN;
        // set the timeout to less than the sleep period in the TransactionPhase.BEFORE_COMPLETION CDI notification
        userTransaction.setTransactionTimeout(1);

        try {
            self.doSomethingTransactional();
            fail("testRollbackDuringSynchronization: expected a rollback exception");
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debugf("testRollback: exception in transactional method: %s", e.getMessage());
            }

            // the reason should be due to the transaction rolling back
            assertTrue("testRollbackDuringSynchronization: should have failed with a RollbackException, not " + e, e instanceof RollbackException);
        }

        assertEquals("Expected rollback", Status.STATUS_ROLLEDBACK, finalStatus);
    }

    public static class TestSync implements javax.transaction.Synchronization {
        @Override
        public void beforeCompletion() {
            if (log.isDebugEnabled()) {
                log.debug("Synchronization beforeCompletion");
            }
        }

        @Override
        public void afterCompletion(int status) {
            if (log.isDebugEnabled()) {
                log.debugf("Synchronization afterCompletion: transaction status = %d%n", status);
            }

            finalStatus = status;
        }
    }
}
