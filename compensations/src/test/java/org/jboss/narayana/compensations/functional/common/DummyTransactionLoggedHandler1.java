/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.functional.common;

import org.jboss.narayana.compensations.api.TransactionLoggedHandler;

/**
 * @author paul.robinson@redhat.com 22/04/2013
 */
public class DummyTransactionLoggedHandler1 implements TransactionLoggedHandler {

    private static Boolean called = false;
    private static Boolean success = null;

    @Override
    public void transactionLogged(boolean success) {

        called = true;
        this.success = success;
    }

    public static Boolean getCalled() {

        return called;
    }

    public static Boolean getSuccess() {

        return success;
    }

    public static void reset() {

        called = false;
        success = null;
    }
}