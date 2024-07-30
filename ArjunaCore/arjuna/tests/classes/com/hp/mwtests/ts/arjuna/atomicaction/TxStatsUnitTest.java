/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.atomicaction;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.coordinator.TxStats;
import com.hp.mwtests.ts.arjuna.resources.BasicRecord;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TxStatsUnitTest {
    @Before
    public void before() {
        Assertions.assertTrue(TxStats.enabled());
    }

    @Test
    public void test() throws Exception {
        arjPropertyManager.getCoordinatorEnvironmentBean().setEnableStatistics(true);

        for (int i = 0; i < 100; i++) {
            AtomicAction A = new AtomicAction();
            AtomicAction B = new AtomicAction();

            A.begin();
            B.begin();

            Assertions.assertEquals(2, TxStats.getInstance().getNumberOfInflightTransactions());

            B.commit();
            A.commit();

            Assertions.assertEquals(0, TxStats.getInstance().getNumberOfInflightTransactions());
        }

        for (int i = 0; i < 100; i++) {
            AtomicAction A = new AtomicAction();

            A.begin();

            Assertions.assertEquals(1, TxStats.getInstance().getNumberOfInflightTransactions());

            A.abort();

            Assertions.assertEquals(0, TxStats.getInstance().getNumberOfInflightTransactions());
        }

        Assertions.assertEquals(100, TxStats.getInstance().getNumberOfAbortedTransactions());
        Assertions.assertEquals(100, TxStats.getInstance().getNumberOfApplicationRollbacks());
        Assertions.assertEquals(200, TxStats.getInstance().getNumberOfCommittedTransactions());
        Assertions.assertEquals(0, TxStats.getInstance().getNumberOfHeuristics());
        Assertions.assertEquals(100, TxStats.getInstance().getNumberOfNestedTransactions());
        Assertions.assertEquals(0, TxStats.getInstance().getNumberOfResourceRollbacks());
        Assertions.assertEquals(0, TxStats.getInstance().getNumberOfTimedOutTransactions());
        Assertions.assertEquals(300, TxStats.getInstance().getNumberOfTransactions());

        PrintWriter pw = new PrintWriter(new StringWriter());

        TxStats.getInstance().printStatus(pw);

        for (int i = 0; i < 100; i++) {
            final AtomicAction D = new AtomicAction();
            D.begin();

            D.add(new StatsResource());
            D.add(new StatsResource());

            Assertions.assertEquals(1, TxStats.getInstance().getNumberOfInflightTransactions());

            Thread t1 = new Thread() {
                @Override
                public void run() {
                    D.cancel();
                }
            };
            Thread t2 = new Thread() {
                @Override
                public void run() {
                    D.abort();
                }
            };
            t1.start();
            t2.start();
            t1.join();
            t2.join();
            D.removeThread();
        }

        Assertions.assertEquals(200, TxStats.getInstance().getNumberOfAbortedTransactions());
        Assertions.assertEquals(200, TxStats.getInstance().getNumberOfApplicationRollbacks());
        Assertions.assertEquals(400, TxStats.getInstance().getNumberOfTransactions());

        for (int i = 0; i < 100; i++) {
            AtomicAction A = new AtomicAction();

            A.begin();

            // Let's add some resources
            A.add(new StatsResource());
            A.add(new StatsResource());

            Assertions.assertEquals(1, TxStats.getInstance().getNumberOfInflightTransactions());

            A.abort();

            Assertions.assertEquals(0, TxStats.getInstance().getNumberOfInflightTransactions());
        }

        Assertions.assertEquals(300, TxStats.getInstance().getNumberOfAbortedTransactions());
        Assertions.assertEquals(300, TxStats.getInstance().getNumberOfApplicationRollbacks());
        Assertions.assertEquals(500, TxStats.getInstance().getNumberOfTransactions());

        for (int i = 0; i < 100; i++) {
            AtomicAction A = new AtomicAction();

            A.begin();

            A.add(new StatsResource());
            A.add(new StatsResource());

            Assertions.assertEquals(1, TxStats.getInstance().getNumberOfInflightTransactions());

            A.preventCommit();
            A.commit();

            Assertions.assertEquals(0, TxStats.getInstance().getNumberOfInflightTransactions());
        }

        Assertions.assertEquals(400, TxStats.getInstance().getNumberOfAbortedTransactions());
        Assertions.assertEquals(300, TxStats.getInstance().getNumberOfApplicationRollbacks());
        Assertions.assertEquals(600, TxStats.getInstance().getNumberOfTransactions());
    }

    @Test
    public void testRollbackOnlyStat() {
        AtomicAction A = new AtomicAction();
        A.begin();
        Assertions.assertEquals(1, TxStats.getInstance().getNumberOfInflightTransactions());
        A.preventCommit();
        Assertions.assertEquals(0, TxStats.getInstance().getNumberOfInflightTransactions());
        A.commit();
    }

    private static class StatsResource extends BasicRecord {

        @Override
        public int topLevelPrepare() {
            // TxStats.getNumberOfInflightTransactions() shouldn't count pending transactions 
            Assertions.assertEquals(0, TxStats.getInstance().getNumberOfInflightTransactions());
            return TwoPhaseOutcome.PREPARE_OK;
        }

        @Override
        public int topLevelAbort() {
            // TxStats.getNumberOfInflightTransactions() shouldn't count aborting transactions 
            Assertions.assertEquals(0, TxStats.getInstance().getNumberOfInflightTransactions());
            return TwoPhaseOutcome.FINISH_OK;
        }
    }
}