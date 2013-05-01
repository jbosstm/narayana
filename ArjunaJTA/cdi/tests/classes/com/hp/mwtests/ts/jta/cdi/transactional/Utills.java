package com.hp.mwtests.ts.jta.cdi.transactional;

import junit.framework.Assert;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * @author paul.robinson@redhat.com 08/05/2013
 */
public class Utills {


    public static void assertTransactionActive(boolean expectActive) throws Exception {

        final Transaction tx = getCurrentTransaction();

        if (expectActive) {
            if (tx == null) {
                Assert.fail("Transactions was expected to be active, but was not");
            }
        } else {
            if (tx != null) {
                Assert.fail("Transactions was expected to be inactive, but was active");
            }
        }
    }

    public static void assertSameTransaction(Transaction expectedTransaction) throws Exception {

        Assert.assertTrue("Expected transaction to be the same, but it wasn't", expectedTransaction == getCurrentTransaction());
    }

    public static void assertDifferentTransaction(Transaction expectedTransaction) throws Exception {

        Assert.assertFalse("Expected transaction to be different, but it wasn't", expectedTransaction == getCurrentTransaction());
    }

    public static TransactionManager getTransactionManager() {
        return com.arjuna.ats.jta.TransactionManager.transactionManager();
    }

    public static Transaction getCurrentTransaction() throws Exception {
        return getTransactionManager().getTransaction();
    }
}
