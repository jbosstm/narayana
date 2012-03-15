package org.jboss.jbossts.star.test;

import java.io.IOException;
import java.util.Collection;
import org.jboss.jbossts.star.util.TxSupport;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TxSupportTest extends BaseTest {

    @BeforeClass
    public static void beforeClass() throws Exception {
        startContainer(TXN_MGR_URL);
    }

    // This test needs to be run in isolation so that previously running/incomplete txns don't affect the results
    // Needs to be considered before adding new tests here
    @Test
    public void testListTransactionsWhenNoActiveTxns() throws IOException {
        TxSupport txn = new TxSupport();

        // This should be a collection of size 0.
        Collection<String> transactionsInProgress = txn.getTransactions();

        Assert.assertEquals(0, transactionsInProgress.size());
    }

}
