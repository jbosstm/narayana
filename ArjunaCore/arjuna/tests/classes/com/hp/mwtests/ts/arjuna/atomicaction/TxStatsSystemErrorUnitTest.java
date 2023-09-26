/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.atomicaction;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.coordinator.TxStats;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TxStatsSystemErrorUnitTest {
    private static final String storeClassName = com.arjuna.ats.internal.arjuna.objectstore.VolatileStore.class.getName();

    @BeforeClass
    public static void setupStore() throws Exception {
        String storeType = UnreliableTestStore.class.getName();
        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreType(storeType);
        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").setObjectStoreType(storeType);
    }

    @Test
    public void test() throws Exception
    {
        final int loopCnt = 100;
        final int sysErrCnt = loopCnt / 10;
        final int commitCnt = loopCnt * 2 - sysErrCnt; // first loops includes a nested transaction
        final int abortCnt = 100;
        final int txnCnt = loopCnt * 2 + abortCnt + 1;

        arjPropertyManager.getCoordinatorEnvironmentBean().setEnableStatistics(true);
        ParticipantStore pstore = StoreManager.getParticipantStore();
        UnreliableTestStore store = (UnreliableTestStore) pstore;

        long startTime = System.nanoTime();
        
        for (int i = 0; i < loopCnt; i++)
        {
            if (i % 10 == 0)
                store.setWriteError(true);

            AtomicAction A = new AtomicAction();
            AtomicAction B = new AtomicAction();
            
            A.begin();
            B.begin();

            A.add(new SimpleAbstractRecord());
            A.add(new SimpleAbstractRecord());

            B.commit();
            A.commit();

            if (i % 10 == 0)
                store.setWriteError(false);
        }

        long avgTxnTime = (System.nanoTime() - startTime) / commitCnt;

        for (int i = 0; i < abortCnt; i++)
        {
            AtomicAction A = new AtomicAction();

            A.begin();

            A.abort();
        }

        AtomicAction B = new AtomicAction();

        B.begin();
        
        assertTrue(TxStats.enabled());
        assertEquals(abortCnt + sysErrCnt, TxStats.getInstance().getNumberOfAbortedTransactions());
        assertEquals(abortCnt, TxStats.getInstance().getNumberOfApplicationRollbacks());
        assertEquals(sysErrCnt, TxStats.getInstance().getNumberOfSystemRollbacks());
        assertEquals(commitCnt, TxStats.getInstance().getNumberOfCommittedTransactions());
        assertEquals(0, TxStats.getInstance().getNumberOfHeuristics());
        assertEquals(1, TxStats.getInstance().getNumberOfInflightTransactions());
        assertEquals(loopCnt, TxStats.getInstance().getNumberOfNestedTransactions());
        assertEquals(0, TxStats.getInstance().getNumberOfResourceRollbacks());
        assertEquals(0, TxStats.getInstance().getNumberOfTimedOutTransactions());
        assertEquals(txnCnt, TxStats.getInstance().getNumberOfTransactions());
        assertTrue(TxStats.getInstance().getAverageCommitTime() < avgTxnTime);
        
        PrintWriter pw = new PrintWriter(new StringWriter());
        
        TxStats.getInstance().printStatus(pw);
    }


    private class SimpleAbstractRecord extends AbstractRecord {
        public SimpleAbstractRecord() {
            super(new Uid());

        }

        @Override
        public boolean doSave() {
            // force the intentions list to be written to disk
            return true;
        }

        @Override
        public Uid order() {
            return super.order();
        }

        @Override
        public int typeIs() {
            return RecordType.USER_DEF_FIRST0;
        }

        @Override
        public Object value() {
            return null;
        }

        @Override
        public void setValue(Object o) {
        }

        @Override
        public int nestedAbort() {
            return TwoPhaseOutcome.FINISH_OK;
        }

        @Override
        public int nestedCommit() {
            return TwoPhaseOutcome.FINISH_OK;
        }

        @Override
        public int nestedPrepare() {
            return TwoPhaseOutcome.PREPARE_OK;
        }

        @Override
        public int topLevelAbort() {
            return TwoPhaseOutcome.FINISH_OK;
        }

        @Override
        public int topLevelCommit() {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
            return TwoPhaseOutcome.FINISH_OK;
        }

        @Override
        public int topLevelPrepare() {
            return TwoPhaseOutcome.PREPARE_OK;
        }

        @Override
        public void merge(AbstractRecord a) {
        }

        @Override
        public void alter(AbstractRecord a) {
        }

        @Override
        public boolean shouldAdd(AbstractRecord a) {
            return false;
        }

        @Override
        public boolean shouldAlter(AbstractRecord a) {
            return false;
        }

        @Override
        public boolean shouldMerge(AbstractRecord a) {
            return false;
        }

        @Override
        public boolean shouldReplace(AbstractRecord a) {
            return false;
        }
    }
}