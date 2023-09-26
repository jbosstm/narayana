/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.interposition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import org.junit.Test;
import org.omg.CosTransactions.Status;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;
import com.hp.mwtests.ts.jts.orbspecific.resources.demosync;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class ServerTransactionUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        ServerTransaction sc = new ServerTransaction(new Uid(), null);
        
        assertTrue(sc.type() != null);
        assertTrue(ServerTransaction.typeName() != null);
        assertTrue(sc.getSavingUid().notEquals(Uid.nullUid()));
        
        OutputObjectState os = new OutputObjectState();
        
        assertTrue(sc.save_state(os, ObjectType.ANDPERSISTENT));
        
        InputObjectState is = new InputObjectState(os);
        
        assertTrue(sc.restore_state(is, ObjectType.ANDPERSISTENT));
        
        sc.setRecoveryCoordinator(null);
    }
    
    @Test
    public void testPrepareCommit () throws Exception
    {
        ServerTransaction sc = new ServerTransaction(new Uid(), null);

        sc.register_synchronization(new demosync(false).getReference());
        
        sc.doBeforeCompletion();
        
        assertEquals(sc.doPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
        assertEquals(sc.doPhase2Commit(), TwoPhaseOutcome.FINISH_OK);
        
        sc.doAfterCompletion(Status.StatusCommitted);
    }
    
    @Test
    public void testPrepareRollback () throws Exception
    {
        ServerTransaction sc = new ServerTransaction(new Uid(), null);

        assertEquals(sc.doPrepare(), TwoPhaseOutcome.PREPARE_READONLY);  // readonly so we commit here
        assertEquals(sc.doPhase2Abort(), ActionStatus.ABORTED);  // Due to the readonly we allow the massage
    }
    
    @Test
    public void testOnePhaseCommit () throws Exception
    {
        ServerTransaction sc = new ServerTransaction(new Uid(), null);

        sc.doCommit(true);
    }
    
    @Test
    public void testRollback () throws Exception
    {
        ServerTransaction sc = new ServerTransaction(new Uid(), null);

        sc.rollback();
    }
}