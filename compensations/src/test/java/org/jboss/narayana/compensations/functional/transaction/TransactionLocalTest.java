/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.compensations.functional.transaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.Transaction;
import org.jboss.narayana.compensations.functional.common.DummyData;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Arquillian.class)
public class TransactionLocalTest {

    @Inject
    private Transaction transaction;

    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(DummyData.class) // Deployment need at least one class with Compensations API annotations to get dependencies
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return archive;
    }

    @Test
    public void successfulScenario() {
        final Counter actionsCounter = new Counter();
        final Counter compensationsCounter = new Counter();
        final Counter confirmationsCounter = new Counter();

        transaction
                .doAction(actionsCounter::increment)
                .doAction(actionsCounter::increment,
                        compensationsCounter::increment,
                        confirmationsCounter::increment)
                .doAction(actionsCounter::increment,
                        (CompensationHandler) compensationsCounter::increment)
                .doAction(actionsCounter::increment,
                        (ConfirmationHandler) confirmationsCounter::increment)
                .execute();

        assertEquals(4, actionsCounter.get());
        assertEquals(2, confirmationsCounter.get());
        assertEquals(0, compensationsCounter.get());
    }

    @Test
    public void exceptionScenario() {
        final Counter actionsCounter = new Counter();
        final Counter compensationsCounter = new Counter();
        final Counter confirmationsCounter = new Counter();

        try {
            transaction
                    .doAction(actionsCounter::increment)
                    .doAction(actionsCounter::increment,
                            compensationsCounter::increment,
                            confirmationsCounter::increment)
                    .doAction(actionsCounter::increment,
                            (CompensationHandler) compensationsCounter::increment)
                    .doAction(actionsCounter::increment,
                            (ConfirmationHandler) confirmationsCounter::increment)
                    .doAction(() -> {
                                throw new RuntimeException("Test");
                            },
                            compensationsCounter::increment,
                            confirmationsCounter::increment)
                    .execute();
            fail("RuntimeException expected");
        } catch (RuntimeException e) {
            // Expected
        }

        assertEquals(4, actionsCounter.get());
        assertEquals(2, compensationsCounter.get());
        assertEquals(0, confirmationsCounter.get());
    }

    private class Counter {

        private int value;

        public int get() {
            return value;
        }

        public void increment() {
            value++;
        }

    }

}
