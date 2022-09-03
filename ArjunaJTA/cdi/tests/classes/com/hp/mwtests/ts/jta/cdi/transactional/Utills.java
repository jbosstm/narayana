package com.hp.mwtests.ts.jta.cdi.transactional;

import org.jboss.logging.Logger;
import org.junit.Assert;

import com.arjuna.ats.jta.common.jtaPropertyManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

/**
 * @author paul.robinson@redhat.com 08/05/2013
 */
public class Utills {
    private static final Logger LOGGER = Logger.getLogger(Utills.class);


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
        try {
            return com.arjuna.ats.jta.TransactionManager.transactionManager(new InitialContext());
        } catch (NamingException ne) {
            String errorMsg = String.format("failure to lookup transaction manager at '%s'",
                jtaPropertyManager.getJTAEnvironmentBean().getTransactionManagerJNDIContext());
            LOGGER.error(errorMsg, ne);
            throw new RuntimeException(errorMsg, ne);
        }
    }

    public static Transaction getCurrentTransaction() throws Exception {
        return getTransactionManager().getTransaction();
    }
}
