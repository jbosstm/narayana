/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.integration.archive;

import org.jboss.narayana.compensations.api.ConfirmationHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TestConfirmationHandler implements ConfirmationHandler {

    private static final AtomicInteger INVOCATIONS_COUNTER = new AtomicInteger();

    public static int getInvocationsCounter() {
        return INVOCATIONS_COUNTER.get();
    }

    public void confirm() {
        System.out.println(
                TestConfirmationHandler.class.getSimpleName() + ".confirm counter=" + INVOCATIONS_COUNTER.incrementAndGet());
    }

}