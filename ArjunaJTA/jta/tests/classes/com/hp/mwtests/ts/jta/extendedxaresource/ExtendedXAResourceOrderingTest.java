/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.extendedxaresource;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.xa.CommitPriority;
import jakarta.transaction.xa.PreparePriority;

import org.junit.Test;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for ExtendedXAResource prepare/commit ordering support (GitHub issue #3050).
 *
 * <p>Verifies that XAResourceRecord.order() correctly positions resources with
 * EARLY/EXCLUSIVE_LAST prepare priorities and EXCLUSIVE_FIRST/LATE commit priorities
 * ahead of or behind normal resources in the 2PC sequence.</p>
 */
public class ExtendedXAResourceOrderingTest {

    /**
     * A resource with PreparePriority.EARLY should be prepared before a NORMAL resource.
     */
    @Test
    public void testEarlyPreparePriorityFirst() throws Exception {
        List<String> log = new ArrayList<>();
        TransactionManager tm = new TransactionManagerImple();
        tm.begin();
        Transaction tx = tm.getTransaction();

        OrderingXAResource normal = new OrderingXAResource("normal",
                PreparePriority.NORMAL, CommitPriority.NORMAL, log);
        OrderingXAResource early = new OrderingXAResource("early",
                PreparePriority.EARLY, CommitPriority.NORMAL, log);

        // Enlist normal first, then early — early must still prepare first
        assertTrue(tx.enlistResource(normal));
        assertTrue(tx.enlistResource(early));

        tm.commit();

        int earlyPrepare = log.indexOf("early:prepare");
        int normalPrepare = log.indexOf("normal:prepare");
        assertTrue("EARLY resource should prepare before NORMAL",
                earlyPrepare < normalPrepare);
    }

    /**
     * A resource with PreparePriority.EXCLUSIVE_LAST should be prepared after all
     * NORMAL resources.
     */
    @Test
    public void testExclusiveLastPreparePriorityLast() throws Exception {
        List<String> log = new ArrayList<>();
        TransactionManager tm = new TransactionManagerImple();
        tm.begin();
        Transaction tx = tm.getTransaction();

        OrderingXAResource exclusiveLast = new OrderingXAResource("exclusiveLast",
                PreparePriority.EXCLUSIVE_LAST, CommitPriority.NORMAL, log);
        OrderingXAResource normal = new OrderingXAResource("normal",
                PreparePriority.NORMAL, CommitPriority.NORMAL, log);

        // Enlist exclusive-last first — it must still prepare last
        assertTrue(tx.enlistResource(exclusiveLast));
        assertTrue(tx.enlistResource(normal));

        tm.commit();

        int exclusivePrepare = log.indexOf("exclusiveLast:prepare");
        int normalPrepare = log.indexOf("normal:prepare");
        assertTrue("EXCLUSIVE_LAST resource should prepare after NORMAL",
                normalPrepare < exclusivePrepare);
    }

    /**
     * A resource with CommitPriority.EXCLUSIVE_FIRST should be committed before
     * NORMAL resources.
     */
    @Test
    public void testExclusiveFirstCommitPriorityFirst() throws Exception {
        List<String> log = new ArrayList<>();
        TransactionManager tm = new TransactionManagerImple();
        tm.begin();
        Transaction tx = tm.getTransaction();

        OrderingXAResource normal = new OrderingXAResource("normal",
                PreparePriority.NORMAL, CommitPriority.NORMAL, log);
        OrderingXAResource exclusiveFirst = new OrderingXAResource("exclusiveFirst",
                PreparePriority.NORMAL, CommitPriority.EXCLUSIVE_FIRST, log);

        assertTrue(tx.enlistResource(normal));
        assertTrue(tx.enlistResource(exclusiveFirst));

        tm.commit();

        int exclusiveCommit = log.indexOf("exclusiveFirst:commit");
        int normalCommit = log.indexOf("normal:commit");
        assertTrue("EXCLUSIVE_FIRST resource should commit before NORMAL",
                exclusiveCommit < normalCommit);
    }

    /**
     * A resource with CommitPriority.LATE should be committed after NORMAL resources.
     */
    @Test
    public void testLateCommitPriorityLast() throws Exception {
        List<String> log = new ArrayList<>();
        TransactionManager tm = new TransactionManagerImple();
        tm.begin();
        Transaction tx = tm.getTransaction();

        OrderingXAResource late = new OrderingXAResource("late",
                PreparePriority.NORMAL, CommitPriority.LATE, log);
        OrderingXAResource normal = new OrderingXAResource("normal",
                PreparePriority.NORMAL, CommitPriority.NORMAL, log);

        // Enlist late first — it must still commit last
        assertTrue(tx.enlistResource(late));
        assertTrue(tx.enlistResource(normal));

        tm.commit();

        int lateCommit = log.indexOf("late:commit");
        int normalCommit = log.indexOf("normal:commit");
        assertTrue("LATE resource should commit after NORMAL",
                normalCommit < lateCommit);
    }

    /**
     * Enlisting a second EXCLUSIVE_LAST resource must throw SystemException.
     */
    @Test
    public void testDuplicateExclusiveLastThrows() throws Exception {
        TransactionManager tm = new TransactionManagerImple();
        tm.begin();
        Transaction tx = tm.getTransaction();

        List<String> log = new ArrayList<>();
        OrderingXAResource first = new OrderingXAResource("first",
                PreparePriority.EXCLUSIVE_LAST, CommitPriority.NORMAL, log);
        OrderingXAResource second = new OrderingXAResource("second",
                PreparePriority.EXCLUSIVE_LAST, CommitPriority.NORMAL, log);

        assertTrue(tx.enlistResource(first));

        try {
            tx.enlistResource(second);
            fail("Expected SystemException when enlisting second EXCLUSIVE_LAST resource");
        } catch (SystemException e) {
            // expected
        } finally {
            tm.rollback();
        }
    }

    /**
     * Enlisting a second EXCLUSIVE_FIRST resource must throw SystemException.
     */
    @Test
    public void testDuplicateExclusiveFirstThrows() throws Exception {
        TransactionManager tm = new TransactionManagerImple();
        tm.begin();
        Transaction tx = tm.getTransaction();

        List<String> log = new ArrayList<>();
        OrderingXAResource first = new OrderingXAResource("first",
                PreparePriority.NORMAL, CommitPriority.EXCLUSIVE_FIRST, log);
        OrderingXAResource second = new OrderingXAResource("second",
                PreparePriority.NORMAL, CommitPriority.EXCLUSIVE_FIRST, log);

        assertTrue(tx.enlistResource(first));

        try {
            tx.enlistResource(second);
            fail("Expected SystemException when enlisting second EXCLUSIVE_FIRST resource");
        } catch (SystemException e) {
            // expected
        } finally {
            tm.rollback();
        }
    }
}
