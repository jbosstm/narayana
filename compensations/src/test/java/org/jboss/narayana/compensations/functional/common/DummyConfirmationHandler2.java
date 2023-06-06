/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.functional.common;

import org.jboss.narayana.compensations.api.ConfirmationHandler;

/**
 * @author paul.robinson@redhat.com 22/04/2013
 */
public class DummyConfirmationHandler2 implements ConfirmationHandler {

    private static boolean called = false;

    @Override
    public void confirm() {

        called = true;
    }

    public static Boolean getCalled() {

        return called;
    }

    public static void reset() {

        called = false;
    }
}