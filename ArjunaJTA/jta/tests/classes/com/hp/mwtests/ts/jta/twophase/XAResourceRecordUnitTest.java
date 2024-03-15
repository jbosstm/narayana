/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.twophase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.common.DummyRecoverableXAConnection;
import com.hp.mwtests.ts.jta.common.DummyXA;
import com.hp.mwtests.ts.jta.common.FailureXAResource;
import com.hp.mwtests.ts.jta.common.FailureXAResource.FailLocation;
import com.hp.mwtests.ts.jta.common.FailureXAResource.FailType;
import com.hp.mwtests.ts.jta.common.TestResource;

public class XAResourceRecordUnitTest {
    @Test
    public void test() throws Exception {
        XAResourceRecord xares = new XAResourceRecord();
        Object obj = new Object();

        xares.setValue(obj);

        assertTrue(xares.value() != obj);

        DummyRecoverableXAConnection rc = new DummyRecoverableXAConnection();
        Object[] params = new Object[1];

        params[XAResourceRecord.XACONNECTION] = rc;

        xares = new XAResourceRecord(new TransactionImple(0), new DummyXA(false), new XidImple(new Uid()), params);

        assertTrue(xares.type() != null);

        xares.merge(xares);
        xares.replace(xares);

        assertTrue(xares.toString() != null);
    }

    @Test
    public void testPackUnpack() throws Exception {
        XAResourceRecord xares;
        DummyRecoverableXAConnection rc = new DummyRecoverableXAConnection();
        Object[] params = new Object[1];

        params[XAResourceRecord.XACONNECTION] = rc;

        xares = new XAResourceRecord(new TransactionImple(0), new DummyXA(false), new XidImple(new Uid()), params);

        OutputObjectState os = new OutputObjectState();

        assertTrue(xares.save_state(os, ObjectType.ANDPERSISTENT));

        InputObjectState is = new InputObjectState(os);

        assertTrue(xares.restore_state(is, ObjectType.ANDPERSISTENT));

        xares = new XAResourceRecord(new TransactionImple(0), new DummyXA(false), new XidImple(new Uid()), null);

        os = new OutputObjectState();

        assertTrue(xares.save_state(os, ObjectType.ANDPERSISTENT));

        is = new InputObjectState(os);

        assertTrue(xares.restore_state(is, ObjectType.ANDPERSISTENT));
    }

    @Test
    public void testReadonly() throws Exception {
        XAResourceRecord xares;

        DummyRecoverableXAConnection rc = new DummyRecoverableXAConnection();
        Object[] params = new Object[1];

        params[XAResourceRecord.XACONNECTION] = rc;

        xares = new XAResourceRecord(new TransactionImple(0), new TestResource(true), new XidImple(new Uid()), params);

        assertEquals(xares.topLevelCommit(), TwoPhaseOutcome.NOT_PREPARED);
        assertEquals(xares.topLevelPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
    }

    @Test
    public void testCommitFailure() throws Exception {
        FailureXAResource fxa = new FailureXAResource(FailureXAResource.FailLocation.commit);
        TransactionImple tx = new TransactionImple(0);
        XAResourceRecord xares = new XAResourceRecord(tx, fxa, tx.getTxId(), null);

        assertEquals(xares.topLevelPrepare(), TwoPhaseOutcome.PREPARE_OK);
        assertEquals(xares.topLevelCommit(), TwoPhaseOutcome.HEURISTIC_MIXED);
        assertTrue(xares.forgetHeuristic());
    }

    @Test
    public void testRollbackFailure() throws Exception {
        FailureXAResource fxa = new FailureXAResource(FailureXAResource.FailLocation.rollback);
        TransactionImple tx = new TransactionImple(0);
        XAResourceRecord xares = new XAResourceRecord(tx, fxa, tx.getTxId(), null);

        assertEquals(xares.topLevelPrepare(), TwoPhaseOutcome.PREPARE_OK);
        assertEquals(xares.topLevelAbort(), TwoPhaseOutcome.HEURISTIC_MIXED);
        assertTrue(xares.forgetHeuristic());
    }

    @Test
    public void testValid2PC() throws Exception {
        TransactionImple tx = new TransactionImple(0);
        DummyXA res = new DummyXA(false);
        XAResourceRecord xares = new XAResourceRecord(tx, res, tx.getTxId(), null);

        assertEquals(xares.topLevelPrepare(), TwoPhaseOutcome.PREPARE_OK);
        assertEquals(xares.topLevelCommit(), TwoPhaseOutcome.FINISH_OK);
    }

    @Test
    public void testValid1PC() throws Exception {
        TransactionImple tx = new TransactionImple(0);
        DummyXA res = new DummyXA(false);
        XAResourceRecord xares = new XAResourceRecord(tx, res, tx.getTxId(), null);

        assertEquals(xares.topLevelOnePhaseCommit(), TwoPhaseOutcome.FINISH_OK);

        xares = new XAResourceRecord(tx, new FailureXAResource(FailLocation.end, FailType.normal), tx.getTxId(), null);

        assertEquals(xares.topLevelOnePhaseCommit(), TwoPhaseOutcome.ONE_PHASE_ERROR);

        xares = new XAResourceRecord(tx, new FailureXAResource(FailLocation.end, FailType.timeout), tx.getTxId(), null);

        assertEquals(xares.topLevelOnePhaseCommit(), TwoPhaseOutcome.ONE_PHASE_ERROR);

        xares = new XAResourceRecord(tx, new FailureXAResource(FailLocation.end, FailType.XA_RBCOMMFAIL), tx.getTxId(), null);

        assertEquals(xares.topLevelOnePhaseCommit(), TwoPhaseOutcome.ONE_PHASE_ERROR);

        xares = new XAResourceRecord(tx, new FailureXAResource(FailLocation.commit, FailType.heurcom), tx.getTxId(), null);

        assertEquals(xares.topLevelOnePhaseCommit(), TwoPhaseOutcome.FINISH_OK);

        xares = new XAResourceRecord(tx, new FailureXAResource(FailLocation.commit, FailType.timeout), tx.getTxId(), null);

        assertEquals(xares.topLevelOnePhaseCommit(), TwoPhaseOutcome.ONE_PHASE_ERROR);

        xares = new XAResourceRecord(tx, new FailureXAResource(FailLocation.commit, FailType.nota), tx.getTxId(), null);

        assertEquals(xares.topLevelOnePhaseCommit(), TwoPhaseOutcome.HEURISTIC_HAZARD);

        xares = new XAResourceRecord(tx, new FailureXAResource(FailLocation.commit, FailType.inval), tx.getTxId(), null);

        assertEquals(xares.topLevelOnePhaseCommit(), TwoPhaseOutcome.HEURISTIC_HAZARD);

        xares = new XAResourceRecord(tx, new FailureXAResource(FailLocation.commit, FailType.proto), tx.getTxId(), null);

        assertEquals(xares.topLevelOnePhaseCommit(), TwoPhaseOutcome.ONE_PHASE_ERROR);

        xares = new XAResourceRecord(tx, new FailureXAResource(FailLocation.commit, FailType.rmfail), tx.getTxId(), null);

        assertEquals(xares.topLevelOnePhaseCommit(), TwoPhaseOutcome.HEURISTIC_HAZARD);
    }

    @Test
    public void testInvalid() throws Exception {
        XAResourceRecord xares = new XAResourceRecord();

        assertEquals(xares.getXid(), null);
        assertEquals(xares.value(), null);
        assertEquals(xares.topLevelOnePhaseCommit(), TwoPhaseOutcome.ONE_PHASE_ERROR);
        assertEquals(xares.topLevelPrepare(), TwoPhaseOutcome.PREPARE_NOTOK);
        assertEquals(xares.topLevelAbort(), TwoPhaseOutcome.FINISH_ERROR);
        assertEquals(xares.topLevelCommit(), TwoPhaseOutcome.FINISH_ERROR);
    }

    @Test
    public void testNested() throws Exception {
        XAResourceRecord xares = new XAResourceRecord();

        assertEquals(xares.nestedOnePhaseCommit(), TwoPhaseOutcome.FINISH_ERROR);
        assertEquals(xares.nestedPrepare(), TwoPhaseOutcome.PREPARE_OK);
        assertEquals(xares.nestedCommit(), TwoPhaseOutcome.FINISH_OK);
        assertEquals(xares.nestedAbort(), TwoPhaseOutcome.FINISH_OK);
    }

    @Test
    public void testPrepareFailureWithRollback() {
        DummyXA res = new DummyXA(false);
        FailureXAResource fxa = new FailureXAResource(FailLocation.prepare_and_rollback, FailType.XA_RBINTEGRITY);
        TransactionImple tx = new TransactionImple(0);
        XAResourceRecord xares = new XAResourceRecord(tx, fxa, tx.getTxId(), null);

        assertEquals(xares.topLevelPrepare(), TwoPhaseOutcome.PREPARE_NOTOK);
        // If FailureXAResource's rollback is invoked, XAException.XA_HEURHAZ is thrown
        assertEquals(xares.topLevelAbort(), TwoPhaseOutcome.FINISH_OK);

        // Validates JBTM-3843 by checking if FailureXAResource committed and/or rolled back
        assertFalse(fxa.isRolledBack());
        assertFalse(fxa.isCommitted());
    }
}