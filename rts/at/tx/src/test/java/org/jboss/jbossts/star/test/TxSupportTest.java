/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

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

    @Test
    public void testListTransactionsWhenNoActiveTxns() throws IOException {
        TxSupport txn = new TxSupport(TXN_MGR_URL, 60000);

        Collection<String> transactionsInProgress = txn.getTransactions();

        // There should be no txns in progress, since none were started in this test
        Assert.assertEquals(0, txn.txCount());
        Assert.assertEquals(0, transactionsInProgress.size());
    }

}