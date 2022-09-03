package com.hp.mwtests.ts.jta.stats;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;

import static com.hp.mwtests.ts.jta.stats.DummyXAResource.faultType.RESOURCE_ROLLBACK;
import static com.hp.mwtests.ts.jta.stats.DummyXAResource.faultType.TIMEOUT_COMMIT;
import static com.hp.mwtests.ts.jta.stats.DummyXAResource.faultType.XA_HEURRB;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.arjuna.ats.arjuna.coordinator.TxStats;

public class StatSuccessUnitTest {
    private static TransactionManager tm;
    private StatHolder stats;
    private static long reaperCheckPeriod = 2000; // ms

    private class StatHolder {
        long abortedTransactions;
        long applicationRollbacks;
        long systemRollbacks; // NB already tested by TxStatsSystemErrorUnitTest
        long committedTransactions;
        long heuristics;
        long inflightTransactions;
        long nestedTransactions; // NB already tested by TxStatsSystemErrorUnitTest
        long resourceRollbacks;
        long timedOutTransactions;
        long transactions;
    }

    private StatHolder getStats() {
        StatHolder stats = new StatHolder();

        stats.abortedTransactions = TxStats.getInstance().getNumberOfAbortedTransactions();
        stats.applicationRollbacks = TxStats.getInstance().getNumberOfApplicationRollbacks();
        stats.systemRollbacks = TxStats.getInstance().getNumberOfSystemRollbacks();
        stats.committedTransactions = TxStats.getInstance().getNumberOfCommittedTransactions();
        stats.heuristics = TxStats.getInstance().getNumberOfHeuristics();
        stats.inflightTransactions = TxStats.getInstance().getNumberOfInflightTransactions();
        stats.nestedTransactions = TxStats.getInstance().getNumberOfNestedTransactions();
        stats.resourceRollbacks = TxStats.getInstance().getNumberOfResourceRollbacks();
        stats.timedOutTransactions = TxStats.getInstance().getNumberOfTimedOutTransactions();
        stats.transactions = TxStats.getInstance().getNumberOfTransactions();

        return stats;
    }

    private StatHolder getDiff(StatHolder prev) {
        StatHolder curr = getStats();

        curr.abortedTransactions -= prev.abortedTransactions;
        curr.applicationRollbacks -= prev.applicationRollbacks;
        curr.systemRollbacks -= prev.systemRollbacks;
        curr.committedTransactions -= prev.committedTransactions;
        curr.heuristics -= prev.heuristics;
        curr.inflightTransactions -= prev.inflightTransactions;
        curr.nestedTransactions -= prev.nestedTransactions;
        curr.resourceRollbacks -= prev.resourceRollbacks;
        curr.timedOutTransactions -= prev.timedOutTransactions;
        curr.transactions -= prev.transactions;

        return curr;
    }

    @BeforeClass
    public static void setup() throws SystemException {
        arjPropertyManager.getCoordinatorEnvironmentBean().setTxReaperTimeout(reaperCheckPeriod);
        arjPropertyManager.getCoordinatorEnvironmentBean().setEnableStatistics(true);

        tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        tm.setTransactionTimeout(1);
    }

    @Before
    public void beforeTest() {
        stats = getStats();
    }

    @Test
    public void abortTest() throws Exception {
        tm.begin();

        tm.getTransaction().enlistResource(new DummyXAResource());
        tm.getTransaction().enlistResource(new DummyXAResource());

        tm.rollback();

        stats = getDiff(stats);

        assertEquals(1, stats.abortedTransactions);
        assertEquals(1, stats.applicationRollbacks);
        assertEquals(0, stats.systemRollbacks);
        assertEquals(0, stats.committedTransactions);
        assertEquals(0, stats.heuristics);
        assertEquals(0, stats.inflightTransactions);
        assertEquals(0, stats.nestedTransactions);
        assertEquals(0, stats.resourceRollbacks);
        assertEquals(0, stats.timedOutTransactions);
        assertEquals(1, stats.transactions);
    }

    @Test
    public void commitTest() throws Exception {
        tm.begin();

        tm.getTransaction().enlistResource(new DummyXAResource());
        tm.getTransaction().enlistResource(new DummyXAResource());

        tm.commit();

        stats = getDiff(stats);

        assertEquals(0, stats.abortedTransactions);
        assertEquals(0, stats.applicationRollbacks);
        assertEquals(0, stats.systemRollbacks);
        assertEquals(1, stats.committedTransactions);
        assertEquals(0, stats.heuristics); // TODO
        assertEquals(0, stats.inflightTransactions);
        assertEquals(0, stats.nestedTransactions);
        assertEquals(0, stats.resourceRollbacks);
        assertEquals(0, stats.timedOutTransactions);
        assertEquals(1, stats.transactions);
    }

    @Test
    public void heuristicRollbackTest() throws Exception {
        tm.begin();

        tm.getTransaction().enlistResource(new DummyXAResource());
        tm.getTransaction().enlistResource(new DummyXAResource(XA_HEURRB));

        try {
            tm.commit();
        } catch (HeuristicMixedException ignore) {
            // expected
        } catch (Exception e) {
            fail(e.getMessage());
        }

        stats = getDiff(stats);

        assertEquals(0, stats.abortedTransactions);
        assertEquals(0, stats.applicationRollbacks);
        assertEquals(0, stats.systemRollbacks);
        assertEquals(1, stats.committedTransactions);
        assertEquals(1, stats.heuristics);
        assertEquals(0, stats.inflightTransactions);
        assertEquals(0, stats.nestedTransactions);
        assertEquals(0, stats.resourceRollbacks);
        assertEquals(0, stats.timedOutTransactions);
        assertEquals(1, stats.transactions);
    }

    @Test
    public void inFlightTest() throws Exception {
        tm.begin();

        tm.getTransaction().enlistResource(new DummyXAResource());
        tm.getTransaction().enlistResource(new DummyXAResource());

        stats = getDiff(stats);

        assertEquals(0, stats.abortedTransactions);
        assertEquals(0, stats.applicationRollbacks);
        assertEquals(0, stats.systemRollbacks);
        assertEquals(0, stats.committedTransactions);
        assertEquals(0, stats.heuristics); // TODO
        assertEquals(1, stats.inflightTransactions);
        assertEquals(0, stats.nestedTransactions);
        assertEquals(0, stats.resourceRollbacks);
        assertEquals(0, stats.timedOutTransactions);
        assertEquals(1, stats.transactions);

        tm.commit();
    }

    @Test
    public void resoureceRollbackTest() throws Exception {
        tm.begin();

        tm.getTransaction().enlistResource(new DummyXAResource());
        tm.getTransaction().enlistResource(new DummyXAResource(RESOURCE_ROLLBACK));

        try {
            tm.commit();
        } catch (Exception ignore) {
        } finally {
        }

        stats = getDiff(stats);

        assertEquals(1, stats.abortedTransactions);
        assertEquals(0, stats.applicationRollbacks);
        assertEquals(0, stats.systemRollbacks);
        assertEquals(0, stats.committedTransactions);
        assertEquals(0, stats.heuristics); // TODO
        assertEquals(0, stats.inflightTransactions);
        assertEquals(0, stats.nestedTransactions);
        assertEquals(1, stats.resourceRollbacks);
        assertEquals(0, stats.timedOutTransactions);
        assertEquals(1, stats.transactions);
    }

    @Test
    public void timeoutTest() throws Exception {
        tm.begin();

        tm.getTransaction().enlistResource(new DummyXAResource());
        tm.getTransaction().enlistResource(new DummyXAResource());

        try {
            Thread.sleep(reaperCheckPeriod + 100); // must be longer than the transaction timeout period
            tm.commit();
        } catch (Exception e) {
            ; // should have timedout
        } finally {
        }

        stats = getDiff(stats);

        assertEquals(1, stats.abortedTransactions);
        assertEquals(1, stats.applicationRollbacks);
        assertEquals(0, stats.systemRollbacks);
        assertEquals(0, stats.committedTransactions);
        assertEquals(0, stats.heuristics); // TODO
        assertEquals(0, stats.inflightTransactions);
        assertEquals(0, stats.nestedTransactions);
        assertEquals(0, stats.resourceRollbacks);
        assertEquals(1, stats.timedOutTransactions);
        assertEquals(1, stats.transactions);
    }
}
