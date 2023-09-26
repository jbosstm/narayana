/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.functional.compensatableAction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.narayana.compensations.api.CompensatableAction;
import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.EnlistException;
import org.jboss.narayana.compensations.functional.common.DummyData;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Arquillian.class)
public class CompensatableActionTestLocal {

    @Inject
    private CompensatableAction compensatableAction;

    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(DummyData.class, CompensatableActionTestLocal.class, ParticipantCompletionCoordinatorRules.class)
                .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\"></beans>"), "beans.xml");

        return archive;
    }

    @Test
    public void successfulScenario() throws EnlistException {
        ParticipantCompletionCoordinatorRules.setParticipantCount(4);

        final Counter workCounter = new Counter();
        final Counter compensationsCounter = new Counter();
        final Counter confirmationsCounter = new Counter();

        compensatableAction
                .addWork(workCounter::increment, compensationsCounter::increment, confirmationsCounter::increment)
                .addWork(workCounter::increment, (CompensationHandler) compensationsCounter::increment)
                .addWork(workCounter::increment, (ConfirmationHandler) confirmationsCounter::increment).execute();

        assertEquals(3, workCounter.get());
        assertEquals(2, confirmationsCounter.get());
        assertEquals(0, compensationsCounter.get());
    }

    @Test
    public void exceptionScenario() throws EnlistException {
        ParticipantCompletionCoordinatorRules.setParticipantCount(5);

        final Counter workCounter = new Counter();
        final Counter compensationsCounter = new Counter();
        final Counter confirmationsCounter = new Counter();

        try {
            compensatableAction
                    .addWork(workCounter::increment, compensationsCounter::increment, confirmationsCounter::increment)
                    .addWork(workCounter::increment, (CompensationHandler) compensationsCounter::increment)
                    .addWork(workCounter::increment, (ConfirmationHandler) confirmationsCounter::increment).addWork(() -> {
                throw new RuntimeException("Test");
            }, compensationsCounter::increment, confirmationsCounter::increment).execute();
            fail("RuntimeException expected");
        } catch (RuntimeException e) {
            // Expected
        }

        assertEquals(3, workCounter.get());
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