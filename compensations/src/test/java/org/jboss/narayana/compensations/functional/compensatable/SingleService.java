/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.compensations.functional.compensatable;

import org.jboss.narayana.compensations.api.TxCompensate;
import org.jboss.narayana.compensations.api.TxConfirm;
import org.jboss.narayana.compensations.api.TxLogged;
import org.jboss.narayana.compensations.functional.common.DummyCompensationHandler1;
import org.jboss.narayana.compensations.functional.common.DummyCompensationHandler2;
import org.jboss.narayana.compensations.functional.common.DummyConfirmationHandler1;
import org.jboss.narayana.compensations.functional.common.DummyConfirmationHandler2;
import org.jboss.narayana.compensations.functional.common.DummyTransactionLoggedHandler1;
import org.jboss.narayana.compensations.functional.common.DummyTransactionLoggedHandler2;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class SingleService {

    @TxCompensate(DummyCompensationHandler1.class)
    @TxConfirm(DummyConfirmationHandler1.class)
    @TxLogged(DummyTransactionLoggedHandler1.class)
    public void testSingle1(Throwable throwable) throws Throwable {

        if (throwable != null) {
            throw throwable;
        }
    }

    @TxCompensate(DummyCompensationHandler2.class)
    @TxConfirm(DummyConfirmationHandler2.class)
    @TxLogged(DummyTransactionLoggedHandler2.class)
    public void testSingle2(Throwable throwable) throws Throwable {

        if (throwable != null) {
            throw throwable;
        }
    }

}