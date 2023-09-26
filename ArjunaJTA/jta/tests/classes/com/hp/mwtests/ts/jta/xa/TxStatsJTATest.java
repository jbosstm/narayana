/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.xa;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.TxStats;
import com.hp.mwtests.ts.jta.common.LastXAResource;
import com.hp.mwtests.ts.jta.common.TestResource;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.transaction.Status;
import javax.transaction.xa.XAResource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TxStatsJTATest {
    private static boolean enabledStatisticsInit;

    private long numberOfTransactions, numberOfCommittedTransactions, numberOfAbortedTransactions, numberOfApplicationRollbacks,
            numberOfResourceRollbacks, numberOfHeuristics, numberOfInflightTransactions, numberOfNestedTransactions, numberOfTimeoutedTransactions;

    @BeforeClass
    public static final void setUp() {
        enabledStatisticsInit = arjPropertyManager.getCoordinatorEnvironmentBean().isTransactionStatusManagerEnable();
        arjPropertyManager.getCoordinatorEnvironmentBean().setEnableStatistics(true);
    }

    @AfterClass
    public static final void tearDown() {
        arjPropertyManager.getCoordinatorEnvironmentBean().setEnableStatistics(enabledStatisticsInit);
    }

    @Before
    public final void initStatistics() {
        initStatsNumbers();
    }

    @Test
    public void readOnlyAndLrcoResourceCommitted() throws Exception {
        XAResource readOnlyResource = new TestResource(true);
        LastXAResource lrcoResource = new LastXAResource();

        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        theTransaction.enlistResource(readOnlyResource);
        theTransaction.enlistResource(lrcoResource);

        tm.commit();

        assertEquals(Status.STATUS_COMMITTED, theTransaction.getStatus());

        assertTrue("Transaction statistics are expected to be enabled", TxStats.enabled());
        TxStats statsInstance = TxStats.getInstance();
        assertEquals("Expected one more transaction was processed", numberOfTransactions + 1, statsInstance.getNumberOfTransactions());
        assertEquals("Expected one more transaction should be committed", numberOfCommittedTransactions + 1, statsInstance.getNumberOfCommittedTransactions());
        assertEquals("Expected no more transaction was rolled-back", numberOfAbortedTransactions, statsInstance.getNumberOfAbortedTransactions());
        assertEquals("Expected no more transaction was rolled-back", numberOfApplicationRollbacks, statsInstance.getNumberOfApplicationRollbacks());
        assertEquals("Expected no more transaction was rolled-back", numberOfResourceRollbacks, statsInstance.getNumberOfResourceRollbacks());
        assertEquals("Expected no more heuristic transaction was creted", numberOfHeuristics, statsInstance.getNumberOfHeuristics());
        assertEquals("Expected all transaction has finished in test", numberOfInflightTransactions, statsInstance.getNumberOfInflightTransactions());
        assertEquals("Expected no more nested transaction was created", numberOfNestedTransactions, statsInstance.getNumberOfNestedTransactions());
        assertEquals("Expected no more transaction was timed-out", numberOfTimeoutedTransactions, statsInstance.getNumberOfTimedOutTransactions());
    }

    private void initStatsNumbers() {
        TxStats statsInstance = TxStats.getInstance();
        numberOfTransactions = statsInstance.getNumberOfTransactions();
        numberOfCommittedTransactions = statsInstance.getNumberOfCommittedTransactions();
        numberOfAbortedTransactions = statsInstance.getNumberOfAbortedTransactions();
        numberOfApplicationRollbacks = statsInstance.getNumberOfApplicationRollbacks();
        numberOfResourceRollbacks = statsInstance.getNumberOfResourceRollbacks();
        numberOfHeuristics = statsInstance.getNumberOfHeuristics();
        numberOfInflightTransactions = statsInstance.getNumberOfInflightTransactions();
        numberOfNestedTransactions = statsInstance.getNumberOfNestedTransactions();
        numberOfTimeoutedTransactions = statsInstance.getNumberOfTimedOutTransactions();
    }
}