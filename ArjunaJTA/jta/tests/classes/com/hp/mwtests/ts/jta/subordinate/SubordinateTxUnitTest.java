/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.subordinate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.SubordinateAtomicAction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.TransactionImple;
import com.arjuna.ats.jta.exceptions.InvalidTerminationStateException;

class DummyTransactionImple extends TransactionImple
{
    public DummyTransactionImple()
    {
        super(0);
    }
    
    protected void commitAndDisassociate () throws jakarta.transaction.RollbackException, jakarta.transaction.HeuristicMixedException, jakarta.transaction.HeuristicRollbackException, java.lang.SecurityException, jakarta.transaction.SystemException, java.lang.IllegalStateException
    {
        super.commitAndDisassociate();
    }

    protected void rollbackAndDisassociate () throws java.lang.IllegalStateException, java.lang.SecurityException, jakarta.transaction.SystemException
    {
        super.rollbackAndDisassociate();
    }
}

public class SubordinateTxUnitTest
{
    @Test
    public void testTransactionImple () throws Exception
    {
        TransactionImple tx = new TransactionImple(0);
        TransactionImple dummy = new TransactionImple(0);
        
        assertFalse(tx.equals(dummy));
        
        try
        {
            tx.commit();
            
            fail();
        }
        catch (final IllegalStateException ex)
        {
        }
        
        try
        {
            tx.rollback();
            
            fail();
        }
        catch (InvalidTerminationStateException ex)
        {
        }
        
        assertEquals(tx.doPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
        
        tx.doCommit();
        
        dummy.doRollback();
        
        tx = new TransactionImple(10);
        
        tx.doOnePhaseCommit();
        tx.doForget();
        
        tx.doBeforeCompletion();
        
        assertTrue(tx.toString() != null);
        assertTrue(tx.activated());
    }
    
    @Test
    public void testAtomicAction () throws Exception
    {
        SubordinateAtomicAction saa = new SubordinateAtomicAction();
        AtomicAction A = new AtomicAction();
        
        assertEquals(saa.commit(), ActionStatus.INVALID);
        assertEquals(saa.abort(), ActionStatus.INVALID);
        
        assertTrue(saa.type() != A.type());
        
        assertTrue(saa.activated());
        
        saa.doForget();
    }
    
    @Test
    public void testError () throws Exception
    {
        DummyTransactionImple dti = new DummyTransactionImple();
        
        try
        {
            dti.commitAndDisassociate();
            
            fail();
        }
        catch (final InvalidTerminationStateException ex)
        {   
        }
        
        try
        {
            dti.rollbackAndDisassociate();
            
            fail();
        }
        catch (final InvalidTerminationStateException ex)
        {
        }
    }
}