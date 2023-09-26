/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.compensations.functional.compensatable;

import org.jboss.narayana.compensations.internal.BAControllerFactory;
import org.junit.Assert;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class Utills {

    public static void assertTransactionActive(final boolean expectActive) throws Exception {

        final Object txContext = BAControllerFactory.getInstance().getCurrentTransaction();

        if (expectActive) {
            if (txContext == null) {
                Assert.fail("Transactions was expected to be active, but was not");
            }
        } else {
            if (txContext != null) {
                Assert.fail("Transactions was expected to be inactive, but was active");
            }
        }
    }

    public static void assertSameTransaction(final Object txContext) throws Exception {

        Object currentTx = BAControllerFactory.getInstance().getCurrentTransaction();
        if (txContext == null && currentTx == null) {
            return;
        }

        if (txContext == null || currentTx == null) {
            Assert.fail();
        }
        Assert.assertTrue("Expected transaction to be the same, but it wasn't. '" + txContext + "' != '" + currentTx + "'",
                txContext.equals(currentTx));
    }

    public static void assertDifferentTransaction(final Object txContext) throws Exception {

        Object currentTx = BAControllerFactory.getInstance().getCurrentTransaction();
        if (txContext == null || currentTx == null) {
            return;
        }
        Assert.assertTrue("Expected transaction to be different, but it wasn't. '" + txContext + "' == '" + currentTx + "'",
                !txContext.equals(currentTx));
    }

}