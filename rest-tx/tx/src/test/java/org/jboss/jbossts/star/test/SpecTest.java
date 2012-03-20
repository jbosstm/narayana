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

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.jboss.jbossts.star.provider.HttpResponseException;
import org.jboss.jbossts.star.util.LinkHolder;
import org.jboss.jbossts.star.util.TxSupport;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * The comments that are preceded by line numbers refer to text in version 4
 * of the specification
 */
public class SpecTest extends BaseTest {
    // jax-rs does not support TRACE, CONNECT and PATCH
    private static enum HTTP_METHOD {HEAD, GET, POST, PUT, DELETE, OPTIONS}

    @BeforeClass
    public static void beforeClass() throws Exception {
        startContainer(TXN_MGR_URL);
    }

    @Test
    public void testTransactionUrls() throws Exception {
        TxSupport txn = new TxSupport();
        Map<String, String> links = new HashMap<String, String>();
        int txnCount = txn.txCount();

        /*
        156 Performing a POST on /transaction-manager with content as shown below will start a new
        157 transaction with a default timeout. A successful invocation will return 201 and the Location header
        158 MUST contain the URI of the newly created transaction resource, which we will refer to as
        159 transaction-coordinator in the rest of this specification. Two related URLs MUST also be returned,
        160 one for the terminator of the transaction to use (typically referred to as the client) and one used
        161 for registering durable participation in the transaction (typically referred to as the server).
        162 Although uniform URL structures are used in the examples, these linked URLs can be of arbitrary
        163 format.
        */
        txn.startTx();

        Assert.assertNotNull("Missing location header", txn.txUrl());
        Assert.assertNotNull("Missing durable participant header", txn.enlistUrl());
        Assert.assertNotNull("Missing terminator header", txn.txTerminatorUrl());

        /*
        179 Performing a HEAD on location URL MUST return the same link information.
        */
        txn.refreshLinkHeaders(links);

        Assert.assertTrue("Missing terminator link header", links.containsKey(TxSupport.TERMINATOR_LINK));
        Assert.assertTrue("Missing durable-participant link header", links.containsKey(TxSupport.PARTICIPANT_LINK));

        Assert.assertEquals(links.get(TxSupport.PARTICIPANT_LINK), txn.enlistUrl());
        Assert.assertEquals(links.get(TxSupport.TERMINATOR_LINK), txn.txTerminatorUrl());

        /*
        206 Performing a GET on the transaction-manager returns a list of all transaction URIs
        207 known to the coordinator (active and in recovery).
        */
        int tcnt1 = txn.txCount();
        int tcnt2 = txn.getTransactions().size();
        log.info("Comparing (" + txn.txUrl() + "): " + tcnt1 + " vrs " + tcnt2 + " vrs " + (txnCount + 1));
        Assert.assertEquals(tcnt1, tcnt2);
        Assert.assertEquals(tcnt1, txnCount + 1);

        /*
        209 Performing a GET on /transaction-coordinator/1234 returns the current status of the transaction,
        210 as described later.
        */
        Assert.assertEquals(TxSupport.TX_ACTIVE, txn.txStatus());

        /*
        223 Performing a DELETE on any of the /transaction-coordinator URIs will return a 403.
        */
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_FORBIDDEN}, txn.txUrl(), "DELETE", null, null, null);

        /*
        225 The client can PUT on the terminator URL in order to
        226 control the outcome of the transaction; anything else MUST return a 400.
        */
        for (HTTP_METHOD method : HTTP_METHOD.values()) {
            if (method != HTTP_METHOD.PUT) {
                txn.httpRequest(new int[] {HttpURLConnection.HTTP_BAD_REQUEST}, txn.txTerminatorUrl(), method.name(), null, null, null);
            }
        }

        /*
        226 ... Performing a PUT as
        227 shown below will trigger the commit of the transaction. Upon termination, the resource and all
        228 associated resources are implicitly deleted. For any subsequent invocation then an
        229 implementation MAY return 410 if the implementation records information about transactions that
        230 have rolled back, (not necessary for presumed rollback semantics) but at a minimum MUST
        231 return 401. The invoker can assume this was a rollback. In order for an interested party to know
        232 for sure the outcome of a transaction then it MUST be registered as a participant with the
        233 transaction coordinator.
        */
        Assert.assertEquals(TxSupport.TX_COMMITTED, txn.commitTx());

        txn.httpRequest(new int[] {HttpURLConnection.HTTP_NOT_FOUND}, txn.txUrl(), "GET", null, null, null);

        log.info("Spec test passed");
    }

    @Test
    public void testTransactionTimeout() throws Exception {
        TxSupport txn = new TxSupport();

        /*
        190 Performing a POST on transaction uri with content "timeout=1000" will start a new transaction with a
        120000 millisecond timeout
        */
        txn.startTx(1000);

        // sleep for longer than the transaction timeout period
        Thread.sleep(2000);

        /*
        200 If the transaction is terminated because of a timeout, the resources representing the created
        201 transaction are deleted. All further invocations on the transaction-coordinator or any of its related
        202 URIs MAY return 410 if the implementation records information about transactions that have
        203 rolled back, (not necessary for presumed rollback semantics) but at a minimum MUST return 401.
        204 The invoker can assume this was a rollback.
        */
        /*
        242 If the transaction no longer exists then an implementation MAY return 410 if the implementation
        243 records information about transactions that have rolled back, (not necessary for presumed
        244 rollback semantics) but at a minimum MUST return 404.
        */

        try {
            Assert.assertEquals(txn.commitTx(), TxSupport.TX_ABORTED);
        } catch (HttpResponseException e) {
            Assert.assertTrue(e.getActualResponse() == HttpURLConnection.HTTP_GONE || e.getActualResponse() == HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    @Test
    public void testRollback() throws Exception {
        TxSupport txn = new TxSupport();

        txn.startTx();

        /*
        253 The transaction may be told to rollback with the following PUT request:
        */
        Assert.assertEquals(TxSupport.TX_ABORTED, txn.rollbackTx());

        /*
        228 associated resources are implicitly deleted. For any subsequent invocation then an
        229 implementation MAY return 410 if the implementation records information about transactions that
        230 have rolled back, (not necessary for presumed rollback semantics) but at a minimum MUST
        231 return 404. The invoker can assume this was a rollback. In order for an interested party to know
        232 for sure the outcome of a transaction then it MUST be registered as a participant with the
        233 transaction coordinator.
        */
        try {
            txn.rollbackTx();
        } catch (HttpResponseException e) {
            Assert.assertTrue(e.getActualResponse() == HttpURLConnection.HTTP_GONE ||
                e.getActualResponse() == HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    @Test
    public void testEnlistResource() throws Exception {
        TxSupport txn = new TxSupport();

        txn.startTx();

        // enlist two Transactional Participants with the transaction
        for (int i = 0; i < 2; i++)
            txn.enlist(PURL);

        /*
        225 The client can PUT one of the following to /transaction-coordinator/1234/terminator in order to
        226 control the outcome of the transaction; anything else MUST return a 400. Performing a PUT as
            ...
        */
        try {
            txn.httpRequest(new int[] {HttpURLConnection.HTTP_BAD_REQUEST},
                    txn.txTerminatorUrl(), "POST", TxSupport.STATUS_MEDIA_TYPE, TxSupport.DO_COMMIT, null);
        } catch (HttpResponseException e) {
            Assert.fail("Should have thrown 400: " + e);
        }
        /*
        246 The state of the transaction MUST be Active for this operation to succeed. If the transaction is in
        247 an invalid state for the operation then the implementation MUST 403. Otherwise the
        248 implementation MAY return 200 or 202. In the latter case the Location header SHOULD contain a
        249 URI upon which a GET may be performed to obtain the transaction outcome. It is implementation
        250 dependent as to how long this URI will remain valid. Once removed by an implementation then
        251 410 MUST be returned.
        */
        try {
            txn.httpRequest(new int[] {HttpURLConnection.HTTP_BAD_REQUEST, HttpURLConnection.HTTP_NOT_FOUND},
                    txn.txTerminatorUrl() + "/garbage", "PUT", TxSupport.STATUS_MEDIA_TYPE, TxSupport.DO_COMMIT, null);
        } catch (HttpResponseException e) {
            Assert.fail("Should have thrown 400 or 404");
        }
        // and finally commit it correctly
        txn.commitTx();
    }

    @Test
    public void testHeuristic() throws Exception {
        TxSupport txn = new TxSupport();
        /*
        cause a heuristic so that we can test:
        246 The state of the transaction MUST be Active for this operation to succeed. If the transaction is in
        247 an invalid state for the operation then the implementation MUST 403. Otherwise the
         */
        String[] pUrl = {
                PURL,
                PURL + "?fault=H_ROLLBACK",
                PURL,
        };
        String[] work = new String[pUrl.length];

        // start a transaction
        txn.startTx();

        // enlist participants (one of which will rollback when asked to commit)
        for (int i = 0; i < pUrl.length; i++)
            work[i] = txn.enlist(pUrl[i]);

        // the commit request should produce a heuristic
        String content = txn.commitTx();
        Assert.assertEquals(TxSupport.TX_H_MIXED, content);

        // ask the dummy TransactionalResource for the terminator and participant urls:
        content = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                PURL + "?pId=" + work[1], "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);
        LinkHolder pUrls = new LinkHolder(content);
        String pTerminator = pUrls.get(TxSupport.TERMINATOR_LINK);
        String pParticipant = pUrls.get(TxSupport.PARTICIPANT_LINK);
        Map<String, String> links2 = new HashMap<String, String>();

        /*
        287 Performing a HEAD on a registered participant URI MUST return the terminator reference, as
        */
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, pParticipant, "HEAD", TxSupport.PLAIN_MEDIA_TYPE, null, links2);
        Assert.assertEquals(links2.get(TxSupport.TERMINATOR_LINK), pTerminator);

        // manually tell the TransactionalResource to forget the heuristic
        // (see the test testHeuristicWithForget for how to get the Transaction Manager to
        // tell the participant to forget it)
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NO_CONTENT},
                pParticipant, "DELETE", null, null, null);

        // the terminator should have gone
        pTerminator = txn.httpRequest(new int[] {HttpURLConnection.HTTP_NOT_FOUND},
                PURL + "?pId=" + work[1], "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);

        Assert.assertEquals(pTerminator, "");
    }

    @Test
    public void testHeuristicWithForget() throws Exception {
        TxSupport txn = new TxSupport();
        String[] pUrl = {
                PURL + "?fault=H_ROLLBACK",
                PURL,
        };
        String[] work = new String[pUrl.length];

        // start a transaction
        txn.startTx();

        // enlist participants (one of which will rollback when asked to commit)
        for (int i = 0; i < pUrl.length; i++)
            work[i] = txn.enlist(pUrl[i]);

        /*
         * the commit request should produce a heuristic but since the first participant
         * generates a heuristic rollback the transaction manager (TM) can decide to rollback
         * the second participant. Hence both participants will be consistent and therefore
         * the TM will tell the first participant that it is OK to forget the heuristic
         */

        String content = txn.commitTx();
        Assert.assertEquals(TxSupport.TX_H_ROLLBACK, content);

        // the participant terminator should have gone even though it got a heuristic
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_NOT_FOUND},
                PURL + "?pId=" + work[0], "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);
    }

    @Test
    public void testSpec6() throws Exception {
        TxSupport txn = new TxSupport();

        String[] pUrl = {
                PURL,
                PURL + "?fault=CDELAY",
                PURL,
        };

        // start another transaction
        txn.startTx();

        // enlist transactional participants (one of which will delay during commit)
        for (String url : pUrl)
            txn.enlist(url);

        AsynchronousCommit async = new AsynchronousCommit(txn);

        new Thread(async).start();

        Thread.sleep(4000);

        Assert.assertEquals(async.status, TxSupport.TX_COMMITTED);

    }

    @Test
    public void testParticipantStatus() throws Exception {
        TxSupport txn = new TxSupport();
        String[] pUrls = {
                PURL + "?fault=CDELAY",
                PURL,
        };
        String[] work = new String[pUrls.length];
        AsynchronousCommit[] async = new AsynchronousCommit[pUrls.length];

        // start a transaction
        txn.startTx();

        for (int i = 0; i < pUrls.length; i++) {
            work[i] = txn.enlist(pUrls[i]);
            async[i] = new AsynchronousCommit(txn);
        }

        for (int i = 0; i < async.length; i++)
            new Thread(async[i]).start();

        /*
        347 Performing a GET on the /participant-resource URL MUST return the current status of the
        348 participant
        */

        // ask the TransactionalResource for the participant url:
        String content = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                PURL + "?pId=" + work[0], "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);
        LinkHolder pLinks = new LinkHolder(content);
        String pParticipant = pLinks.get(TxSupport.PARTICIPANT_LINK);

        // wait long enough for the prepare
        Thread.sleep(1000);

        /*
        347 Performing a GET on the /participant-resource URL MUST return the current status of the
        348 participant
        */

        // the commit on the first participant is delayed so asking for its status should return prepared:
        content = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, pParticipant, "GET", TxSupport.STATUS_MEDIA_TYPE, null, null);
        Assert.assertEquals(content, TxSupport.TX_PREPARED);
    }

    @Test
    public void testCannotEnlistDuring2PC() throws Exception {
        TxSupport txn = new TxSupport();

        /*
        297 If the transaction is not Active then the implementation MUST return 403. If the implementation
        298 has seen this participant URI before then it MUST return 400. Otherwise the operation is
        */
        String[] pUrls = {
                PURL + "?fault=PDELAY",
                PURL,
        };
        String[] work = new String[pUrls.length];

        // start a transaction
        txn.startTx();

        // enlist participants
        for (int i = 0; i < pUrls.length; i++)
            work[i] = txn.enlist(pUrls[i]);

        AsynchronousCommit async = new AsynchronousCommit(txn);

        // ask the TransactionalResource for the participant urls:
        String enlistUrls = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                PURL + "?pId=" + work[0], "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);
        // and test that enlisting a participant a second time generates a 400 code:
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_BAD_REQUEST},
                txn.enlistUrl(), "POST", TxSupport.POST_MEDIA_TYPE, enlistUrls, null);

        // commit the transaction
        new Thread(async).start();

        // allow time for the prepare to be called
        Thread.sleep(1000);
        // the transaction should now be prepared so it should be too late to enlist in the transaction:
        try {
            String er = txn.enlist(PURL);
            Assert.fail("Should not be able to enlist a resource after 2PC has started");
        } catch (HttpResponseException e) {
            Assert.assertEquals(e.getActualResponse(), HttpURLConnection.HTTP_FORBIDDEN);
        }
    }

    // recovery
    public void recovery(boolean notifyRecovery) throws Exception {
        TxSupport txn = new TxSupport();
        String pUrl = PURL;

        // start a transaction
        txn.startTx();

        // enlist two participants
        String pId = pUrl + "?pId=" + txn.enlist(pUrl);
        String pId2 = pUrl + "?pId=" + txn.enlist(pUrl);

        // ask the TransactionalResource to move the participant to a new url:
        String notifyQuery = notifyRecovery ? "true" : "false";
        String newPid = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                pId + "&query=move&arg=101&register=" + notifyQuery, "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);

        newPid = pUrl + "?pId=" + newPid;
        // make sure the TransactionalResource has forgotten about the original pId
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_NOT_FOUND}, pId, "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);
        // and is listening on the new one
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, newPid, "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);

        // commit the transaction
        String txStatus = txn.commitTx();

        if (notifyRecovery) {
            // the participant that moved should have had commit called
            String status = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                    pId2 + "&query=status", "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);

            Assert.assertEquals(TxSupport.COMMITTED, status);
        } else {
            // the participant that did not move should have aborted
            String status = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                    pId2 + "&query=status", "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);

            // the participant that moved should not have had prepare or commit called on it so expect 204
            txn.httpRequest(new int[] {HttpURLConnection.HTTP_NO_CONTENT},
                    newPid + "&query=status", "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);

            Assert.assertEquals(TxSupport.ABORTED, status);
        }
    }

    @Test // recovery
    public void testRecoveryURL() throws Exception {
       recovery(true);
       recovery(false);
    }

    static class AsynchronousCommit implements Runnable {
        String status;
        TxSupport txn;
        public AsynchronousCommit(TxSupport txn) { this.txn = txn; }
        public void run() {
            try {
                status = txn.commitTx();
            } catch (HttpResponseException e) {
                status = "";
            }
        }
    }

    public static void main(String[] args) throws Exception {
        TxSupport[] transactions = {new TxSupport(), new TxSupport()};
        int txnCnt = new TxSupport().txCount();

        // start 2 transactions
        for (TxSupport txn : transactions)
            txn.startTx();

        // make sure that there are 2 more transactions
        if (transactions[0].txCount() != txnCnt + 2)
            System.out.println("Some transactions failed to start");

        // terminate the transactions
        for (TxSupport txn : transactions)
            txn.commitTx();

        // make sure that there are 2 less transactions
        if (transactions[0].txCount() != txnCnt)
            System.out.println("Some transactions failed to terminate");
    }
}
