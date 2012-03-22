/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
 *
 * (C) 2010
 * @author JBoss Inc.
 */
package org.jboss.jbossts.star.test;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.jboss.jbossts.star.util.TxSupport;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CoordinatorTest extends BaseTest {
    @BeforeClass
    public static void beforeClass() throws Exception {
        startContainer(TXN_MGR_URL);
    }

    /*
      * Note: TxSupport methods throw exceptions if unexpected status codes are returned.
      * use the TxSupport.httpRequest(...) if you want to specify other status codes
      */

    // list transactions
    @Test
    public void testListTransactions() throws IOException
    {
        TxSupport[] txns = {new TxSupport(), new TxSupport()};
        int txnCount = new TxSupport().txCount();

        for (TxSupport txn : txns)
            txn.startTx();

        // there should be txns.length more transactions
        Assert.assertEquals(txnCount + txns.length, txns[0].txCount());

        for (TxSupport txn : txns)
            txn.commitTx();

        // the number of transactions should be back to the original number
        Assert.assertEquals(txnCount, txns[0].txCount());

    }

    // 1PC commit abort
    @Test
    public void test1PCAbort() throws Exception
    {
        TxSupport txn = new TxSupport();
        String pUrl = PURL;
        String pid = null;
        String pVal;

        pid = modifyResource(txn, pUrl, pid, "p1", "v1");
        pVal = getResourceProperty(txn, pUrl, pid, "p1");
        Assert.assertEquals(pVal, "v1");

        txn.startTx();
        pid = enlistResource(txn, pUrl + "?pId=" + pid);

        modifyResource(txn, pUrl, pid, "p1", "v2");
        pVal = getResourceProperty(txn, pUrl, pid, "p1");
        Assert.assertEquals(pVal, "v2");

        txn.rollbackTx();

        pVal = getResourceProperty(txn, pUrl, pid, "p1");
        Assert.assertEquals(pVal, "v1");
    }

    // 1PC commit
    @Test
    public void test1PCCommit() throws Exception
    {
        TxSupport txn = new TxSupport();
        String pUrl = PURL;
        String pid = null;
        String pVal;

        pid = modifyResource(txn, pUrl, pid, "p1", "v1");
        pVal = getResourceProperty(txn, pUrl, pid, "p1");
        Assert.assertEquals(pVal, "v1");

        txn.startTx();
        pid = enlistResource(txn, pUrl + "?pId=" + pid);

        modifyResource(txn, pUrl, pid, "p1", "v2");
        pVal = getResourceProperty(txn, pUrl, pid, "p1");
        Assert.assertEquals(pVal, "v2");

        txn.commitTx();

        pVal = getResourceProperty(txn, pUrl, pid, "p1");
        Assert.assertEquals(pVal, "v2");
    }

    // 2PC commit
    @Test
    public void test2PC() throws Exception
    {
        TxSupport txn = new TxSupport();
        String pUrl = PURL;
        String[] pid = new String[2];
        String[] pVal = new String[2];

        for (int i = 0; i < pid.length; i++) {
            pid[i] = modifyResource(txn, pUrl, null, "p1", "v1");
            pVal[i] = getResourceProperty(txn, pUrl, pid[i], "p1");

            Assert.assertEquals(pVal[i], "v1");
        }

        txn.startTx();

        for (int i = 0; i < pid.length; i++) {
            enlistResource(txn, pUrl + "?pId=" + pid[i]);

            modifyResource(txn, pUrl, pid[i], "p1", "v2");
            pVal[i] = getResourceProperty(txn, pUrl, pid[i], "p1");

            Assert.assertEquals(pVal[i], "v2");
        }

        txn.rollbackTx();

        for (int i = 0; i < pid.length; i++) {
            pVal[i] = getResourceProperty(txn, pUrl, pid[i], "p1");
            Assert.assertEquals(pVal[i], "v1");
        }
    }

    // commit an invalid transaction
    @Test
    public void testCommitInvalidTx() throws IOException
    {
        TxSupport txn = new TxSupport();

        txn.startTx();

        String terminator = txn.txTerminatorUrl();
        // we know that the terminator URL ends in terminate (by reading the code for the provider) - modify to produce an invalid one
        terminator = terminator.replace("/terminate", "_dead/terminate");
        // an attempt to commit on this URL should fail:
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_NOT_FOUND}, terminator, "PUT", TxSupport.STATUS_MEDIA_TYPE, TxSupport.DO_COMMIT, null);
        // commit it properly
        txn.commitTx();
    }
    
    @Test
    public void testTimeoutCleanup() throws InterruptedException {
        TxSupport txn = new TxSupport();
        int txnCount = txn.txCount();
        txn.startTx(1000);
        txn.enlist(PURL);

        // Let the txn timeout
        Thread.sleep(2000);
        
        Assert.assertEquals(txnCount, txn.txCount());
    }
}
