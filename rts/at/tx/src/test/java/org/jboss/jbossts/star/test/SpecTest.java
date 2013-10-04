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

import com.arjuna.ats.internal.jta.transaction.arjunacore.AtomicAction;
import junit.framework.Assert;
import org.jboss.jbossts.star.provider.HttpResponseException;
import org.jboss.jbossts.star.util.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/*
 * The tests are against draft 8 of the spec. Some comments refer to line numbers from draft 4 if they spec didn't
 * changed in draft 8, otherwise draft 8 lines numbers are used. Sorry if it's confusing but I shall update the
 * line numbers when the final version of the spec is out.
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

        /*
        196Performing a POST on the transaction-manager URI with header as shown
        197below will start a new transaction with a default timeout. A successful invocation will return 201
        198and the Location header MUST contain the URI of the newly created transaction resource, which
        199we will refer to as transaction-coordinator in the rest of this specification. At least two related
        200URLs MUST also be returned, one for the terminator of the transaction to use (typically referred
        201to as the client) and one used for registering durable participation in the transaction (typically
        202referred to as the server). These are referred to as the transaction-terminator and transaction-
        203enlistment URIs, respectively. Although uniform URL structures are used in the examples, these
        204linked URLs can be of arbitrary format.
        */

        txn.startTx();

        Assert.assertNotNull("Missing location header", txn.getTxnUri());
        Assert.assertNotNull("Missing durable participant header", txn.getDurableParticipantEnlistmentURI());
        Assert.assertNotNull("Missing terminator header", txn.getTerminatorURI());

        /*
        228Performing a HEAD on the transaction-coordinator URI MUST return the same link information.
        */
        txn.refreshTransactionHeaders(links);

        Assert.assertTrue("Missing terminator link header", links.containsKey(TxLinkNames.TERMINATOR));
        Assert.assertTrue("Missing durable-participant link header", links.containsKey(TxLinkNames.PARTICIPANT));

        Assert.assertEquals(links.get(TxLinkNames.PARTICIPANT), txn.getDurableParticipantEnlistmentURI());
        Assert.assertEquals(links.get(TxLinkNames.TERMINATOR), txn.getTerminatorURI());

        /*
        262Performing a GET on the /transaction-manager URI returns a list of all transaction -coordinator
        263URIs known to the coordinator (active and in recovery). The returned response MAY include a
        264link header with rel attribute "statistics" linking to a resource that contains statistical information
        265such as the number of transactions that have committed and aborted. The link MAY contain a
        266media type hint with value “application/txstatusext+xml”.
        */
        Collection<String> txns = txn.getTransactions(TxMediaType.TX_LIST_MEDIA_TYPE);

        // assert that the returned list of transactions contains the new transaction:
        Assert.assertTrue(txns.contains(txn.getTxnUri()));
        Assert.assertEquals(TxMediaType.TX_LIST_MEDIA_TYPE, txn.getContentType());

        /*
        271Performing a GET on the transaction-coordinator URI returns the
        272current status of the transaction, as described later.
        TODO Check which other status's we test for
        */
        Assert.assertEquals(TxStatusMediaType.TX_ACTIVE, txn.txStatus(TxMediaType.TX_STATUS_MEDIA_TYPE));
        Assert.assertEquals(TxMediaType.TX_STATUS_MEDIA_TYPE, txn.getContentType());

        /*
        285Performing a DELETE on any of the transaction-coordinator or transaction-enlistment URIs
        286/transaction-coordinator URIs will return a 403.
        */
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_FORBIDDEN}, txn.getTxnUri(), "DELETE", null);

        /*
        292The client can PUT one of the following to the transaction-terminator URI /transaction-
        293coordinator/1234/terminator in order to control the outcome of the transaction; anything else
        294MUST return a 400 (unless the terminator and transaction URLs are the same in which case GET
        295would return the transaction status as described previously).
        */
        boolean sameUrls = txn.getTerminatorURI().equals(txn.getTxnUri());
        for (HTTP_METHOD method : HTTP_METHOD.values()) {
            // if sameUrls then the terminator url will support both GET and PUT otherwise just PUT is supported
            // TODO test the case where sameUrls is true
            if (method != HTTP_METHOD.PUT && (!sameUrls || method != HTTP_METHOD.GET)) {
                txn.httpRequest(new int[] {HttpURLConnection.HTTP_BAD_REQUEST}, txn.getTerminatorURI(), method.name(),
                        null);
            }
        }

        /*
        295 ... Performing a PUT as shown below
        296will trigger the commit of the transaction. Upon termination, the resource and all associated
        297resources are implicitly deleted. For any subsequent PUT invocation, such as due to a
        298timeout/retry, then an implementation MAY return 410 if the implementation records information
        299about transactions that have rolled back, (not necessary for presumed rollback semantics) but at
        300a minimum MUST return 404. The invoker can assume this was a rollback. In order for an
        301interested party to know for sure the outcome of a transaction then it MUST be registered as a
        302participant with the transaction coordinator.
        */
        String txnStatus = txn.commitTx();

        txn.httpRequest(new int[] {HttpURLConnection.HTTP_NOT_FOUND}, txn.getTxnUri(), "GET", null);
        /*
        311The response body MAY contain the transaction outcome.
        */
        if (txnStatus != null && txnStatus.length() != 0)
            Assert.assertEquals(TxStatusMediaType.TX_COMMITTED, txnStatus);

        log.info("Spec test passed");
    }

    @Test
    public void testTransactionTimeout() throws Exception {
        TxSupport txn = new TxSupport();

        /*
        241Performing a POST on the transaction-manager URI as shown below will start a new transaction
        242with the specified timeout in milliseconds.
        */
        txn.startTx(1000);

        // sleep for longer than the transaction timeout period
        Thread.sleep(2000);

        /*
        295... Performing a PUT as shown below
        296will trigger the commit of the transaction. Upon termination, the resource and all associated
        297resources are implicitly deleted. For any subsequent PUT invocation, such as due to a
        298timeout/retry, then an implementation MAY return 410 if the implementation records information
        299about transactions that have rolled back, (not necessary for presumed rollback semantics) but at
        300a minimum MUST return 404. The invoker can assume this was a rollback. In order for an

        and

        311... If the transaction no longer exists then
        312an implementation MAY return 410 if the implementation records information about transactions
        313that have rolled back, (not necessary for presumed rollback semantics) but at a minimum MUST
        314return 404.
        */
        try {
            Assert.assertEquals(txn.commitTx(), TxStatusMediaType.TX_ROLLEDBACK);
        } catch (HttpResponseException e) {
            Assert.assertTrue(e.getActualResponse() == HttpURLConnection.HTTP_GONE || e.getActualResponse() == HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    /*
    TODO there is no test for the following
    316The state of the transaction MUST be TransactionActive for this operation to succeed. If the
    317transaction is in an invalid state for the operation then the implementation MUST return a 412
    318status code. Otherwise the implementation MAY return 200 or 202 codes. In the latter case the
    319Location header SHOULD contain a URI upon which a GET may be performed to obtain the
    320transaction outcome. It is implementation dependent as to how long this URI will remain valid.
    321Once removed by an implementation then 410 MUST be returned.
    */


    @Test
    public void testRollback() throws Exception {
        TxSupport txn = new TxSupport();

        txn.startTx();

        /*
        323The transaction may be told to rollback with the following PUT request:
        */
        Assert.assertEquals(TxStatusMediaType.TX_ROLLEDBACK, txn.rollbackTx());

        /*
        296... Upon termination, the resource and all associated
        297resources are implicitly deleted. For any subsequent PUT invocation, such as due to a
        298timeout/retry, then an implementation MAY return 410 if the implementation records information
        299about transactions that have rolled back, (not necessary for presumed rollback semantics) but at
        300a minimum MUST return 404. The invoker can assume this was a rollback.
        */
        try {
            Assert.assertEquals(TxStatusMediaType.TX_ROLLEDBACK, txn.rollbackTx());
        } catch (HttpResponseException e) {
            Assert.assertTrue(e.getActualResponse() == HttpURLConnection.HTTP_GONE ||
                    e.getActualResponse() == HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    @Test
    public void testRollbackOnly() throws Exception {
        TxSupport txn = new TxSupport();

        txn.startTx();

        Assert.assertEquals(TxStatusMediaType.TX_ROLLBACK_ONLY, txn.markTxRollbackOnly());
        Assert.assertEquals(TxStatusMediaType.TX_ROLLEDBACK, txn.rollbackTx());

        /*
        296... Upon termination, the resource and all associated
        297resources are implicitly deleted. For any subsequent PUT invocation, such as due to a
        298timeout/retry, then an implementation MAY return 410 if the implementation records information
        299about transactions that have rolled back, (not necessary for presumed rollback semantics) but at
        300a minimum MUST return 404. The invoker can assume this was a rollback.
        */
        try {
            Assert.assertEquals(TxStatusMediaType.TX_ROLLEDBACK, txn.rollbackTx());
        } catch (HttpResponseException e) {
            Assert.assertTrue(e.getActualResponse() == HttpURLConnection.HTTP_GONE ||
                    e.getActualResponse() == HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    @Test
    public void testRollbackOnlyWithCommit() throws Exception {
        TxSupport txn = new TxSupport();

        txn.startTx();

        Assert.assertEquals(TxStatusMediaType.TX_ROLLBACK_ONLY, txn.markTxRollbackOnly());

        /*
         * committing a transaction marked rollback only should automatically abort the transaction
         */
        Assert.assertEquals(TxStatusMediaType.TX_ROLLEDBACK, txn.commitTx());

        /*
        296... Upon termination, the resource and all associated
        297resources are implicitly deleted. For any subsequent PUT invocation, such as due to a
        298timeout/retry, then an implementation MAY return 410 if the implementation records information
        299about transactions that have rolled back, (not necessary for presumed rollback semantics) but at
        300a minimum MUST return 404. The invoker can assume this was a rollback.
        */
        try {
            Assert.assertEquals(TxStatusMediaType.TX_ROLLEDBACK, txn.rollbackTx());
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
            txn.enlistTestResource(PURL, false);

        /*
        make sure we can't POST to the terminator URL - we already tested this but not with enlisted participants
        292The client can PUT one of the following to the transaction-terminator URI /transaction-
        293coordinator/1234/terminator in order to control the outcome of the transaction; anything else
        294MUST return a 400 (unless the terminator and transaction URLs are the same in which case GET
        295would return the transaction status as described previously)
        */
        try {
            txn.httpRequest(new int[] {HttpURLConnection.HTTP_BAD_REQUEST},
                    txn.getTerminatorURI(), "POST", TxMediaType.TX_STATUS_MEDIA_TYPE, TxStatusMediaType.TX_COMMITTED);
        } catch (HttpResponseException e) {
            Assert.fail("Should have thrown 400: " + e);
        }

        // and finally commit it correctly
        txn.commitTx();
    }

    @Test
    public void testEnlistResourceWithRollbackOnly() throws Exception {
        TxSupport txn = new TxSupport();

        txn.startTx();

        Assert.assertEquals(TxStatusMediaType.TX_ROLLBACK_ONLY, txn.markTxRollbackOnly());

        // enlisting a participant into a transaction that is marked rollback only should fail:
        // 385If the transaction is not TransactionActive when registration is attempted, then the implementation
        // 386MUST return a 412 status code.
        try {
            for (int i = 0; i < 2; i++)
                txn.enlistTestResource(PURL, false);
            Assert.fail("Should have thrown 412");
        } catch (HttpResponseException e) {
            if (e.getActualResponse() != HttpURLConnection.HTTP_PRECON_FAILED)
                Assert.fail("Should have thrown 412 but actual response was " + e.getActualResponse());
        }

        Assert.assertEquals(TxStatusMediaType.TX_ROLLEDBACK, txn.rollbackTx());
    }

    /*
     * This test starts a transaction enlists a participant and then asks the participant
     * to tell the coordinator to terminate it on a different url. Arrange for the transaction to rollback
     * due to a timeout. This condition tests an edge case that could cause the coordinator not to delete
     * recovery urls after a transaction completes (causing a memory leak)
     */
    @Test
    public void testRecoveryUrlIsRemovedAfterCompletion() throws Exception {
        TxSupport txn = new TxSupport();
        String pUrl = PURL;

        txn.startTx(1000);

        String wid1 = txn.enlistTestResource(pUrl, false);
        String wid2 = txn.enlistTestResource(pUrl, false);

        String pId1 = PURL + "?pId=" + wid1;

        String recoveryUrl = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                pId1 + "&query=recoveryUrl",
                "GET", TxMediaType.PLAIN_MEDIA_TYPE);

        // ask the TransactionalResource to move the participant to a new url:
        String newPid = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                pId1 + "&query=move&arg=101&register=true&twoPhaseAware=false",
                "GET", TxMediaType.PLAIN_MEDIA_TYPE);

        newPid = PURL + "?pId=" + newPid;
        // make sure the TransactionalResource has forgotten about the original pId
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_NOT_FOUND}, pId1, "GET", TxMediaType.PLAIN_MEDIA_TYPE);
        // and is listening on the new one
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, newPid, "GET", TxMediaType.PLAIN_MEDIA_TYPE);

        // sleep for longer than the transaction timeout period
        Thread.sleep(2000);

        try {
            Assert.assertEquals(txn.commitTx(), TxStatusMediaType.TX_ROLLEDBACK);
        } catch (HttpResponseException e) {
            Assert.assertTrue(e.getActualResponse() == HttpURLConnection.HTTP_GONE || e.getActualResponse() == HttpURLConnection.HTTP_NOT_FOUND);
        }

        // ask the transaction links using the recovery URL. Since the txn has completed the
        // coordinator should no longer have any knowledge of the recovery url
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_NOT_FOUND}, recoveryUrl, "GET", null);
    }

    @Test
    public void testHeuristic() throws Exception {
        TxSupport txn = new TxSupport();
        /*
        cause a heuristic to ensure the Coordinator handles them correctly:
         */
        String[] pUrl = {
                PURL,
                PURL + "?fault=H_ROLLBACK", // this participant will produce a heuristic
                PURL,
        };
        String[] work = new String[pUrl.length];

        // start a transaction
        txn.startTx();

        // enlist participants (one of which will rollback when asked to commit)
        for (int i = 0; i < pUrl.length; i++)
            work[i] = txn.enlistTestResource(pUrl[i], false);

        // the commit request should produce a heuristic
        String content = txn.commitTx();
        Assert.assertEquals(TxStatusMediaType.TX_H_MIXED, content);

        // ask the dummy TransactionalResource for the terminator and participant urls:
        content = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                PURL + "?pId=" + work[1], "GET", TxMediaType.PLAIN_MEDIA_TYPE);

        Map<String, String> pUrls = TxSupport.decodeLinkHeader(content);

        String pTerminator = pUrls.get(TxLinkNames.PARTICIPANT_TERMINATOR);
        String pParticipant = pUrls.get(TxLinkNames.PARTICIPANT_RESOURCE);
        Map<String, String> links2 = new HashMap<String, String>();

        /*
        366Performing a HEAD on a participant-resource URI MUST return the
        367terminator reference,
        */
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, pParticipant, "HEAD", TxMediaType.PLAIN_MEDIA_TYPE, null,
                links2);
        Assert.assertEquals(links2.get(TxLinkNames.TERMINATOR), pTerminator);

        // manually tell the TransactionalResource to forget the heuristic
        // (see the test testHeuristicWithForget for how to get the Transaction Manager to
        // tell the participant to forget it)
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NO_CONTENT},
                pParticipant, "DELETE", null);

        // the terminator should have gone
        pTerminator = txn.httpRequest(new int[] {HttpURLConnection.HTTP_NOT_FOUND},
                PURL + "?pId=" + work[1], "GET", TxMediaType.PLAIN_MEDIA_TYPE);

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
            work[i] = txn.enlistTestResource(pUrl[i], false);

        /*
         * the commit request should produce a heuristic but since the first participant
         * generates a heuristic rollback the transaction manager (TM) can decide to rollback
         * the second participant. Hence both participants will be consistent and therefore
         * the TM will tell the first participant that it is OK to forget the heuristic
         */

        String content = txn.commitTx();
        Assert.assertEquals(TxStatusMediaType.TX_H_ROLLBACK, content);

        // the participant terminator should have gone even though it got a heuristic
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_NOT_FOUND},
                PURL + "?pId=" + work[0], "GET", TxMediaType.PLAIN_MEDIA_TYPE);
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
            txn.enlistTestResource(url, false);

        Future<String> future = submitJob((Callable<String>) new AsynchronousCommit(txn));

        Assert.assertEquals(future.get(), TxStatusMediaType.TX_COMMITTED);

    }

    @Test
    public void testParticipantStatus() throws Exception {
        TxSupport txn = new TxSupport();
        String[] pUrls = {
                PURL + "?fault=CDELAY",
                PURL,
        };
        String[] work = new String[pUrls.length];

        // start a transaction
        txn.startTx();

        for (int i = 0; i < pUrls.length; i++)
            work[i] = txn.enlistTestResource(pUrls[i], false);

        // ask the TransactionalResource for the participant url:
        String content = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                PURL + "?pId=" + work[0], "GET", TxMediaType.PLAIN_MEDIA_TYPE);
        Map<String, String> pLinks = TxSupport.decodeLinkHeader(content);
        String pParticipant = pLinks.get(TxLinkNames.PARTICIPANT_RESOURCE);

        // run the commit in the background
        submitJob((Runnable) new AsynchronousCommit(txn, 0L));

        /*
        427 Performing a GET on the /participant-resource URL MUST return the current status of the
        428 participant
        */

        /*
         * wait long enough for the background thread to commit the transaction. Note one of the
         * participants will delay during the commit phase. Wait long enough for the prepare to
         * be called on each participant but not too long that the participant commit calls will
         * complete @see TransactionalResource#terminate where the delay is 3000 ms
         */
        Thread.sleep(500);

        /*
        427 Performing a GET on the /participant-resource URL MUST return the current status of the
        428 participant
        */

        // the commit on the first participant is delayed so asking for its status should return prepared:
        content = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, pParticipant, "GET",
                TxMediaType.TX_STATUS_MEDIA_TYPE);
        Assert.assertEquals(TxStatusMediaType.TX_PREPARED, content);
    }

    @Test
    public void testCannotEnlistDuring2PC() throws Exception {
        TxSupport txn = new TxSupport();

        /*
        376If the transaction is not TransactionActive when registration is attempted, then the implementation
        377MUST return a 412 status code. If the implementation has seen this participant URI before then it
        378MUST return 400. Otherwise the operation is considered a success and the implementation
        379MUST return 201 and SHOULD use the Location header to give a participant specific URI that
        380the participant MAY use later during prepare or for recovery purposes.
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
            work[i] = txn.enlistTestResource(pUrls[i], false);

        // ask the TransactionalResource for the participant urls:
        String enlistUrls = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                PURL + "?pId=" + work[0], "GET", TxMediaType.PLAIN_MEDIA_TYPE);
        // and test that enlisting a participant a second time generates a 400 code:
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_BAD_REQUEST},
                txn.getDurableParticipantEnlistmentURI(), "POST", TxMediaType.POST_MEDIA_TYPE, enlistUrls);

        // commit the transaction
        submitJob((Runnable) new AsynchronousCommit(txn)) ;

        // allow time for the prepare to be called
        Thread.sleep(1000);
        // the transaction should now be prepared so it should be too late to enlist in the transaction:
        try {
            String er = txn.enlistTestResource(PURL, false);
            Assert.fail("Should not be able to enlist a resource after 2PC has started: " + er);
        } catch (HttpResponseException e) {
            Assert.assertEquals(e.getActualResponse(), HttpURLConnection.HTTP_PRECON_FAILED);
        }
    }

    // recovery

    /**
     * test recovery on a new URI
     * @param notifyRecovery the resource will tell the coordinator that it should be called on a different URI
     * @param twoPhaseAware if true the participant should be two Phase aware
     * @throws Exception
     */
    public void recovery(boolean notifyRecovery, boolean twoPhaseAware) throws Exception {
        TxSupport txn = new TxSupport(600000);
        String pUrl = PURL;

        // start a transaction
        txn.startTx();

        // enlist two participants
        if (!twoPhaseAware)
            pUrl += "?twoPhaseAware=false";

        String wid1 = txn.enlistTestResource(pUrl, false);
        String wid2 = txn.enlistTestResource(pUrl, false);

        String pId1 = PURL + "?pId=" + wid1;
        String pId2 = PURL + "?pId=" + wid2;

        // ask the TransactionalResource to move the participant to a new url:
        String notifyQuery = notifyRecovery ? "true" : "false";
        String newPid = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                pId1 + "&query=move&arg=101&register=" + notifyQuery + "&twoPhaseAware=" + twoPhaseAware,
                "GET", TxMediaType.PLAIN_MEDIA_TYPE);

        newPid = PURL + "?pId=" + newPid;
        // make sure the TransactionalResource has forgotten about the original pId
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_NOT_FOUND}, pId1, "GET", TxMediaType.PLAIN_MEDIA_TYPE);
        // and is listening on the new one
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, newPid, "GET", TxMediaType.PLAIN_MEDIA_TYPE);

        // commit the transaction
        String txStatus = txn.commitTx();

        if (notifyRecovery) {
            // the participant that moved should have had commit called
            String status = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                    pId2 + "&query=status", "GET", TxMediaType.PLAIN_MEDIA_TYPE);

            Assert.assertEquals(TxStatus.TransactionCommitted.name(), status);
            if (!twoPhaseAware) {
                // only 2 phase unaware participants maintain counts so the next 2 asserts validate that the
                // coordinator invoked the correct termination URIs
                Assert.assertEquals(getResourceProperty(txn, PURL, wid2, "commitCnt"), "1");
                Assert.assertEquals(getResourceProperty(txn, PURL, wid2, "rollbackCnt"), "0");
            }
        } else {
            // the participant that did not move should have aborted
            String status = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                    pId2 + "&query=status", "GET", TxMediaType.PLAIN_MEDIA_TYPE);

            // the participant that moved should not have had prepare or commit called on it so expect 204
            txn.httpRequest(new int[] {HttpURLConnection.HTTP_NO_CONTENT},
                    newPid + "&query=status", "GET", TxMediaType.PLAIN_MEDIA_TYPE);

            Assert.assertEquals(TxStatus.TransactionRolledBack.name(), status);
            if (!twoPhaseAware) {
                // only 2 phase unaware participants maintain counts
                Assert.assertEquals(getResourceProperty(txn, PURL, wid2, "commitCnt"), "0");
                Assert.assertEquals(getResourceProperty(txn, PURL, wid2, "rollbackCnt"), "1");
            }
        }
    }

    @Test // recovery of two phase aware participants without notifying the coordinator that a participant moved
    public void testRecoveryURLTwoPhaseAwareWithoutNotification() throws Exception {
        recovery(false, true);
    }

    @Test // recovery of two phase aware participants where the participant tells the coordinator it moved
    public void testRecoveryURLTwoPhaseAwareWithNotification() throws Exception {
        recovery(true, true);
    }

    @Test // recovery of two phase unaware participants without notifying the coordinator that a participant moved
    public void testRecoveryURLTwoPhaseUnawareWithoutNotification() throws Exception {
        recovery(false, false);
    }

    @Test // recovery of two phase unaware participants where the participant tells the coordinator it moved
    public void testRecoveryURLTwoPhaseUnawareWithNotification() throws Exception {
        recovery(true, false);
    }


    //@Test
    public void testRecoveringParticipant1() throws Exception {
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
            txn.enlistTestResource(url, false);

        Future<String> future = submitJob((Callable<String>) new AsynchronousCommit(txn));

        Thread.sleep(1000); // read committed
        OSRecordHolder recordHolder = readObjectStoreRecord(new AtomicAction().type());
        future.get();
        writeObjectStoreRecord(recordHolder);
    }

    static class AsynchronousCommit implements Callable<String>, Runnable {
        String status = "";
        TxSupport txn;
        Long delay;

        public AsynchronousCommit(TxSupport txn, Long delay) {
            this.txn = txn;
            this.delay = delay;
        }

        public AsynchronousCommit(TxSupport txn) {
            this(txn, 0L);
        }

        public void run() {
            try {
                if (delay > 0)
                    Thread.sleep(delay);
                status = txn.commitTx();
            } catch (HttpResponseException e) {
                // ignore
            } catch (InterruptedException e) {
                // ignore
            }
        }

        @Override
        public String call() throws Exception {
            try {
                return txn.commitTx();
            } catch (HttpResponseException e) {
                return "";
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
