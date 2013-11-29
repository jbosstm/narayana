/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
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
