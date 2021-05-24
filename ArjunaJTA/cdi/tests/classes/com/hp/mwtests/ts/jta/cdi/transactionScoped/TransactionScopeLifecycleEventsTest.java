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
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;

import javax.enterprise.context.spi.Context;

import javax.enterprise.event.Observes;

import javax.enterprise.inject.spi.BeanManager;

import javax.inject.Inject;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.Transactional;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionScoped;

import org.jboss.arquillian.container.test.api.Deployment;

import org.jboss.arquillian.junit.Arquillian;

import org.jboss.shrinkwrap.api.ShrinkWrap;

import org.jboss.shrinkwrap.api.asset.EmptyAsset;

import org.jboss.shrinkwrap.api.spec.JavaArchive;

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

    private static boolean initializedObserved;

    private static boolean destroyedObserved;

    @Inject
    private TransactionManager transactionManager;

    @Inject
    private TransactionScopeLifecycleEventsTest self;

    @Deployment
    public static JavaArchive createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class, "test.jar")
            .addClass(TransactionScopeLifecycleEventsTest.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Transactional
    void doSomethingTransactional() {

    }

    void transactionScopeActivated(@Observes @Initialized(TransactionScoped.class) final Object event,
                                   final BeanManager beanManager)
        throws SystemException {
        assertNotNull(event);
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

    void transactionScopeDectivated(@Observes @Destroyed(TransactionScoped.class) final Object event,
                                    final BeanManager beanManager) throws SystemException {
        assertNotNull(event);
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

    @Test
    public void testEffects() {
        self.doSomethingTransactional();
        assertTrue(initializedObserved);
        assertTrue(destroyedObserved);
    }

}
