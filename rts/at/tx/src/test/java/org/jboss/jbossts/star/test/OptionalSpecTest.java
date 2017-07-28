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

import org.jboss.jbossts.star.provider.HttpResponseException;
import org.jboss.jbossts.star.util.*;
import org.jboss.jbossts.star.util.media.txstatusext.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.util.*;

/*
 * The comments that are preceded by line numbers refer to text in version 8 of the specification
 */
public class OptionalSpecTest extends BaseTest {
    @BeforeClass
    public static void beforeClass() throws Exception {
        startContainer(TXN_MGR_URL);
    }

    @Test
    public void testStatusWithoutParticipants() throws Exception {
        TxSupport txn = new TxSupport();

        txn.startTx();

        /*
        288Additional information about the transaction, such as the number of participants and their
        289individual URIs, MAY be returned if the client specifies the application/txstatusext+xml and the
        290implementation supports that type.
        */
        CoordinatorElement elem = txn.getTransactionInfo();

        if (txn.getStatus() != HttpURLConnection.HTTP_UNSUPPORTED_TYPE) {
            // the server supports extended status information
            Assert.assertNotNull(elem);

            Assert.assertEquals(elem.getStatus(), TransactionStatusElement.TransactionActive);
            Assert.assertEquals(elem.getTxnURI(), txn.getTxnUri());
            //TODO             Assert.assertEquals(elem.getTerminatorURI(), txn.getTerminatorURI());
            Assert.assertEquals(elem.getDurableParticipantEnlistmentURI(), txn.getDurableParticipantEnlistmentURI());
            Assert.assertEquals(elem.getVolatileParticipantEnlistmentURI(), txn.getVolatileParticipantEnlistmentURI());
        } else {
            log.warn("Not testing extended transaction information (reason not supported)");
        }
    }

    @Test
    public void testStatusWithParticipants() throws Exception {
        TxSupport txn = new TxSupport();

        txn.startTx();

        // enlist two Transactional Participants with the transaction
        for (int i = 0; i < 2; i++)
            txn.enlistTestResource(PURL, false);

        /*
        288Additional information about the transaction, such as the number of participants and their
        289individual URIs, MAY be returned if the client specifies the application/txstatusext+xml and the
        290implementation supports that type.
        */
        CoordinatorElement elem = txn.getTransactionInfo();

        if (txn.getStatus() != HttpURLConnection.HTTP_UNSUPPORTED_TYPE) {
            // the server supports extended status information
            Assert.assertNotNull(elem);

            Assert.assertEquals(elem.getStatus(), TransactionStatusElement.TransactionActive);
            Assert.assertEquals(elem.getTxnURI(), txn.getTxnUri());
//TODO            Assert.assertEquals(elem.getTerminatorURI(), txn.getTerminatorURI());
            Assert.assertEquals(elem.getDurableParticipantEnlistmentURI(), txn.getDurableParticipantEnlistmentURI());

            List<TwoPhaseAwareParticipantElement> participants = elem.getTwoPhaseAware();

            Assert.assertEquals(participants.size(), 2);
            TwoPhaseAwareParticipantElement p1 = participants.get(0);
            TwoPhaseAwareParticipantElement p2 = participants.get(1);

            Assert.assertFalse(p1.getTerminatorURI().equals(p2.getTerminatorURI()));
            Assert.assertFalse(p1.getRecoveryURI().equals(p2.getRecoveryURI()));
            Assert.assertFalse(p1.getResourceURI().equals(p2.getResourceURI()));
        } else {
            log.warn("Not testing extended transaction information (reason not supported)");
        }
    }

    @Test
    public void testTransactionStatistics() throws Exception {
        TxSupport txn = new TxSupport();

        TransactionStatisticsElement statsBeforeCommit = txn.getTransactionStatistics();

        txn.startTx();

        /*
         From REST-AT spec:
          Performing a GET on the /transaction-manager URI returns a list of all transaction -coordinator
          URIs known to the coordinator (active and in recovery). The returned response MAY include a
          link header with rel attribute "statistics" linking to a resource that contains statistical information
          such as the number of transactions that have committed and aborted. The link MAY contain
          a media type hint with value “application/txstatusext+xml”.
        */
        TransactionStatisticsElement statsDuringCommit = txn.getTransactionStatistics();

        txn.commitTx();

        TransactionStatisticsElement statsAfterCommit = txn.getTransactionStatistics();

        // although stats are optional this implementation supports them
        if (statsDuringCommit != null) {
            Assert.assertEquals("there should be the same number of currently active txns before txn start and after it",
                statsBeforeCommit.getActive(), statsAfterCommit.getActive());
            Assert.assertEquals("rolled-back before and after has to match", statsDuringCommit.getRolledback(), statsAfterCommit.getRolledback());
            Assert.assertEquals("there should be one active transaction during txn processing and no-one after processing ends",
                statsDuringCommit.getActive(), statsAfterCommit.getActive() + 1);
            Assert.assertEquals("prepared before and after has to match",
                statsBeforeCommit.getPrepared(), statsAfterCommit.getPrepared());
            Assert.assertEquals("prepared during txn processing does not change (changes at commit time and changes back after commit)",
                statsDuringCommit.getPrepared(), statsAfterCommit.getPrepared());
            Assert.assertEquals("transaction got committed that should be recorded",
                statsDuringCommit.getCommitted() + 1, statsAfterCommit.getCommitted());
        } else {
            log.warn("Not testing transaction statistics (reason not supported)");
        }
    }

    @Test
    public void testTMExtMediaType() throws Exception {
        TxSupport txn1 = new TxSupport();
        TxSupport txn2 = new TxSupport();
        TransactionManagerElement tme1, tme2;
        TransactionStatisticsElement stats1, stats2;

        txn1.startTx();
        txn2.startTx();

        /*
        269Performing a GET on the transaction-manager URI with media type application/txstatusext+xml
        270returns extended  information about the transaction-manager resource such as how long it has been
        271up and all  transaction-coordinator URIs.
        */
        tme1 = txn1.getTransactionManagerInfo();
        if (txn1.getStatus() == HttpsURLConnection.HTTP_UNSUPPORTED_TYPE) {
            log.warn("Not testing extended transaction manager info (reason not supported)");
            return;
        }

        Assert.assertEquals(TxStatusMediaType.TX_COMMITTED, txn2.commitTx());
        Assert.assertEquals(TxStatusMediaType.TX_ROLLEDBACK, txn1.rollbackTx());

        tme2 = txn1.getTransactionManagerInfo();

        Date d1 = tme1.getCreated();
        Date d2 = new Date();

        // sanity check the creation time of the transaction manager
        Assert.assertNotNull(d1);
        Assert.assertEquals(d1, tme2.getCreated());
        Assert.assertTrue(d2.after(d1));

        stats1 = tme1.getStatistics();
        stats2 = tme2.getStatistics();

        // test that the number of active transactions decreased by 2
        Assert.assertEquals(stats1.getActive(), stats2.getActive() + 2);
        // test that the number of prepared transactions us zero
        Assert.assertEquals(stats1.getPrepared(), stats2.getPrepared());
        // test that the number of committed transactions increased by 1
        Assert.assertEquals(stats1.getCommitted(), stats2.getCommitted() - 1);
        // test that the number of aborted transactions increased by 1
        Assert.assertEquals(stats1.getRolledback(), stats2.getRolledback() - 1);

        List<String> txns1 = tme1.getCoordinatorURIs();
        List<String> txns2 = tme2.getCoordinatorURIs();

        // test that the number of transactions has decreased by 2
        Assert.assertEquals(txns1.size(), txns2.size() + 2);
        // test that the first list contains both transactions
        Assert.assertTrue(txns1.contains(txn1.getTxnUri()));
        Assert.assertTrue(txns1.contains(txn2.getTxnUri()));
    }

    @Test
    public void testEnlistVolatileParticipant() {
        String enlistUrl = PURL + "?isVolatile=true";
        String[] workIds = new String[1];
        TxSupport txn = new TxSupport(60000);
        txn.startTx();

        // enlist Transactional Participants and volatile participants with the transaction
        for (int i = 0; i < workIds.length; i++) {
            /*
             * the resource implementation will enlist in the volatile protocol twice:
             * - once directly using the coordinator volatile-participant registration link
             * - and then indirectly during participant enlistment by including a link header with
             *  value TxLinkRel.VOLATILE_PARTICIPANT
             * This will mean that two before and after synchronisations will be called resulting in a
             * total of 4 synchronisations
             */
            workIds[i] = txn.enlistTestResource(enlistUrl, true);
        }

        txn.commitTx();

        for (int i = 0; i < workIds.length; i++) {
            String syncCount = getResourceProperty(txn, PURL, workIds[i], "syncCount");
            // there should have been 2 before and 2 after synchronisation calls:
            Assert.assertEquals(syncCount, "4");
        }
    }

    @Test
    public void testEnlistVolatileParticipantWithRollbackOnly() {
        String enlistUrl = PURL + "?isVolatile=true";
        String[] workIds = new String[1];
        TxSupport txn = new TxSupport(60000);
        txn.startTx();

        Assert.assertEquals(TxStatusMediaType.TX_ROLLBACK_ONLY, txn.markTxRollbackOnly());

        // enlisting a participant into a transaction that is marked rollback only should fail:
        // 385If the transaction is not TransactionActive when registration is attempted, then the implementation
        // 386MUST return a 412 status code.
        for (int i = 0; i < workIds.length; i++) {
            /*
             * the resource implementation will enlist in the volatile protocol twice:
             * - once directly using the coordinator volatile-participant registration link
             * - and then indirectly during participant enlistment by including a link header with
             *  value TxLinkRel.VOLATILE_PARTICIPANT
             * This will mean that two before and after synchronisations will be called resulting in a
             * total of 4 synchronisations
             */
            try {
                workIds[i] = txn.enlistTestResource(enlistUrl, true);
                Assert.fail("Should have thrown 412");
            } catch (HttpResponseException e) {
                if (e.getActualResponse() != HttpURLConnection.HTTP_PRECON_FAILED)
                    Assert.fail("Should have thrown 412 but actual response was " + e.getActualResponse());
            }
        }

        Assert.assertEquals(TxStatusMediaType.TX_ROLLEDBACK, txn.rollbackTx());
    }

    @Test
    public void testVolatilePrepareFail() {
        String enlistUrl = PURL + "?isVolatile=true&fault=V_PREPARE";
        String[] workIds = new String[1];
        TxSupport txn = new TxSupport(60000);
        txn.startTx();

        // enlist Transactional Participants and volatile participants with the transaction
        for (int i = 0; i < workIds.length; i++) {
            /*
             * the resource implementation will enlist in the volatile protocol twice:
             * - once directly using the coordinator volatile-participant registration link
             * - and then indirectly during participant enlistment by including a link header with
             *  value TxLinkRel.VOLATILE_PARTICIPANT
             * This will mean that two before and after synchronisations will be called resulting in a
             * total of 4 synchronisations
             */
            workIds[i] = txn.enlistTestResource(enlistUrl, true);
        }
        /*
            555In this case the Volatile prepare phase executes prior to the Durable prepare where the
            556transaction-coordinator sends a PUT request to the registered volatile-participant: only if this
            557prepare succeeds will the Durable protocol be executed
         */
        // the volatile participants should have failed the volatile prepare phase (  "?fault=V_PREPARE" in the url)
        Assert.assertEquals(TxStatusMediaType.TX_ROLLEDBACK, txn.commitTx());

        for (int i = 0; i < workIds.length; i++) {
            String syncCount = getResourceProperty(txn, PURL, workIds[i], "syncCount");
            // there should have been 2 before and 2 after synchronisation calls:
            Assert.assertEquals(syncCount, "4");
        }
    }
}
