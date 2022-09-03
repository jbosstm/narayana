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

import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxStatusMediaType;
import org.jboss.jbossts.star.util.TxSupport;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
    public void testListTransactions() throws IOException {
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
    public void test1PCAbort() throws Exception {
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
    public void test1PCCommit() throws Exception {
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

    @Test
    public void test1PCCommitUnaware() throws Exception {
        TxSupport txn = new TxSupport();
        String pUrl = PURL;
        String pid = null;
        String pVal;

        pid = modifyResource(txn, pUrl, pid, "p1", "v1");
        pVal = getResourceProperty(txn, pUrl, pid, "p1");
        Assert.assertEquals("intial value for key " + pid + " was set now checking it" , pVal, "v1");

        txn.startTx();
        pid = enlistResource(txn, pUrl + "?pId=" + pid + "&twoPhaseAware=false");

        modifyResource(txn, pUrl, pid, "p1", "v2");
        pVal = getResourceProperty(txn, pUrl, pid, "p1");
        Assert.assertEquals("transaction in run for key " + pid + " we should see already a new value", pVal, "v2");

        txn.commitTx();

        pVal = getResourceProperty(txn, pUrl, pid, "p1");
        Assert.assertEquals("transaction wrote for key " + pid + " but read value is different", pVal, "v2");

        String commitCount = getResourceProperty(txn, pUrl, pid, "commitCnt");
        Assert.assertEquals("one phase executed thus two-phase commit count expected being 0", "0", commitCount);
        String onePhaseCommitCount = getResourceProperty(txn, pUrl, pid, "commitOnePhaseCnt");
        Assert.assertEquals("one phase executed thus one-phase commit count expected being 1", "1", onePhaseCommitCount);
    }


    @Test
    public void test1PCCommitUnawareWithoutOnePhase() throws Exception {
        TxSupport txn = new TxSupport();
        String pUrl = PURL;
        String pid = null;
        String pVal;

        pid = modifyResource(txn, pUrl, pid, "p1", "v1");
        pVal = getResourceProperty(txn, pUrl, pid, "p1");
        Assert.assertEquals("intial value for key " + pid + " was set now checking it" , pVal, "v1");

        txn.startTx();
        pid = enlistResource(txn, pUrl + "?pId=" + pid + "&twoPhaseAware=false&isUnawareTwoPhaseParticipantOnePhase=false");

        modifyResource(txn, pUrl, pid, "p1", "v2");
        pVal = getResourceProperty(txn, pUrl, pid, "p1");
        Assert.assertEquals("transaction in run for key " + pid + " we should see already a new value", pVal, "v2");

        txn.commitTx();

        pVal = getResourceProperty(txn, pUrl, pid, "p1");
        Assert.assertEquals("transaction wrote for key " + pid + " but read value is different", pVal, "v2");

        String commitCount = getResourceProperty(txn, pUrl, pid, "commitCnt");
        Assert.assertEquals("one phase executed without 1pc support thus two-phase commit count expected being 1", "1", commitCount);
        String onePhaseCommitCount = getResourceProperty(txn, pUrl, pid, "commitOnePhaseCnt");
        Assert.assertEquals("one phase executed without 1pc support thus one-phase commit count expected being 0", "0", onePhaseCommitCount);
    }

    // 2PC commit
    @Test
    public void test2PC() throws Exception {
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

    @Test
    public void test2PCCommitWithoutResponse() throws Exception {
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
            enlistResource(txn, PURL_NO_RESPONSE + "?pId=" + pid[i]);

            modifyResource(txn, pUrl, pid[i], "p1", "v2");
            pVal[i] = getResourceProperty(txn, pUrl, pid[i], "p1");

            Assert.assertEquals(pVal[i], "v2");
        }

        String status = txn.commitTx();

        for (int i = 0; i < pid.length; i++) {
            pVal[i] = getResourceProperty(txn, pUrl, pid[i], "p1");
            Assert.assertEquals(pVal[i], "v2");
        }
    }

    @Test
    public void test2PCRollbackWithoutResponse() throws Exception {
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
            enlistResource(txn, PURL_NO_RESPONSE + "?pId=" + pid[i]);

            modifyResource(txn, pUrl, pid[i], "p1", "v2");
            pVal[i] = getResourceProperty(txn, pUrl, pid[i], "p1");

            Assert.assertEquals(pVal[i], "v2");
        }

        String status = txn.rollbackTx();

        for (int i = 0; i < pid.length; i++) {
            pVal[i] = getResourceProperty(txn, pUrl, pid[i], "p1");
            Assert.assertEquals(pVal[i], "v1");
        }
    }

    // commit an invalid transaction
    @Test
    public void testCommitInvalidTx() throws IOException {
        // start a transaction
        TxSupport txn = new TxSupport().startTx();

        String terminator = txn.getTerminatorURI();
        // mangle the terminator URI
        //terminator = terminator.replace("/terminate", "_dead/terminate");
        terminator += "/_dead";
        // an attempt to commit on this URI should fail:
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_NOT_FOUND}, terminator, "PUT", TxMediaType.TX_STATUS_MEDIA_TYPE, TxStatusMediaType.TX_COMMITTED);
        // commit it properly
        txn.commitTx();
    }

    @Test
    public void testTimeoutCleanup() throws InterruptedException {
        TxSupport txn = new TxSupport();
        int txnCount = txn.txCount();
        txn.startTx(1000);
        txn.enlistTestResource(PURL, false);

        // Let the txn timeout
        Thread.sleep(2000);

        Assert.assertEquals(txnCount, txn.txCount());
    }

    @Test
    public void testClientAPI() throws Exception {
        Client client = ClientBuilder.newClient();

        WebTarget resource = client.target(TXN_MGR_URL);
        Response response = resource.request(MediaType.APPLICATION_FORM_URLENCODED).post(Entity.entity(new Form(), MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        int status = response.getStatus();

        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, status);

        response.close();

        Invocation inv = resource.request(MediaType.APPLICATION_FORM_URLENCODED).buildPost(Entity.entity(new Form(), MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        response = inv.invoke();

        response.close();

        String r1 = resource.request("application/txlist").get(String.class);
        Assert.assertTrue("response should have contained 2 transaction urls",r1.length() != 0);

        String r2 = resource.request().get(String.class);
        Assert.assertTrue("xml response should have contained 2 transaction urls",r2.length() != 0);
    }

    @Test
    public void testFailureInSecondParticipantDuringCommit() {
        final TxSupport txn = new TxSupport();
        final int originalTxCount = txn.txCount();
        final String pUrl = PURL;
        final String[] pid = new String[2];
        pid[0] = modifyResource(txn, pUrl, null, "p1", "v1");
        pid[1] = modifyResource(txn, pUrl, null, "p1", "v1");

        txn.startTx();

        enlistResource(txn, pUrl + "?pId=" + pid[0]);
        enlistResource(txn, pUrl + "?pId=" + pid[1] + "&fault=CRUNTIME");

        modifyResource(txn, pUrl, pid[0], "p1", "v2");
        modifyResource(txn, pUrl, pid[1], "p1", "v2");

        Assert.assertEquals("v2", getResourceProperty(txn, pUrl, pid[0], "p1"));
        Assert.assertEquals("v2", getResourceProperty(txn, pUrl, pid[1], "p1"));

        txn.commitTx();

        Assert.assertEquals("v2", getResourceProperty(txn, pUrl, pid[0], "p1"));
        Assert.assertEquals("v1", getResourceProperty(txn, pUrl, pid[1], "p1"));

        // Transaction should not be removed because of the failure
        Assert.assertEquals(originalTxCount + 1, txn.txCount());
    }

}
