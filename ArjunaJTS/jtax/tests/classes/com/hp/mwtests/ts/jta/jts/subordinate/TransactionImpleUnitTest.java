/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.subordinate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.arjuna.ats.internal.jta.transaction.jts.AtomicTransaction;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.SubordinateAtomicTransaction;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.TransactionImple;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.interposition.ServerControlWrapper;
import com.arjuna.ats.jta.exceptions.InactiveTransactionException;
import com.arjuna.ats.jta.exceptions.InvalidTerminationStateException;
import com.hp.mwtests.ts.jta.jts.common.TestBase;

class DummyTransactionImple extends TransactionImple
{
    public DummyTransactionImple(AtomicTransaction imported)
    {
        super(imported);
    }

    public void commitAndDisassociate () throws jakarta.transaction.RollbackException, jakarta.transaction.HeuristicMixedException, jakarta.transaction.HeuristicRollbackException, java.lang.SecurityException, jakarta.transaction.SystemException, java.lang.IllegalStateException
    {
        super.commitAndDisassociate();
    }

    public void rollbackAndDisassociate () throws java.lang.IllegalStateException, java.lang.SecurityException, jakarta.transaction.SystemException
    {
        super.rollbackAndDisassociate();
    }
}


public class TransactionImpleUnitTest extends TestBase
{   
    @Test
    public void test () throws Exception
    {
        OTSImpleManager.current().begin();
        
        TransactionImple tx = new TransactionImple(new SubordinateAtomicTransaction(new ServerControlWrapper(OTSImpleManager.current().get_control())));
        
        assertFalse(tx.equals(null));
        assertTrue(tx.equals(tx));
        assertFalse(tx.equals(new TransactionImple(new AtomicTransaction())));
        assertFalse(tx.equals(new Object()));
        assertTrue(tx.toString() != null);
        
        try
        {
            tx.commit();
            
            fail();
        }
        catch (final InvalidTerminationStateException ex)
        {
        }
        
        try
        {
            tx.rollback();
            
            fail();
        }
        catch (final InvalidTerminationStateException ex)
        {
        }
        
        tx.doBeforeCompletion();
        
        tx.doPrepare();
        
        try
        {
            tx.doCommit();
            
            fail();
        }
        catch (final Throwable ex)
        {
        }
        
        try
        {
            tx.doRollback();
            
            fail();
        }
        catch (final Throwable ex)
        {
        }
        
        try
        {
            tx.doOnePhaseCommit();
            
            fail();
        }
        catch (final Throwable ex)
        {
        }
        
        tx.doForget();
        
        OTSImpleManager.current().rollback();
        
        DummyTransactionImple dummy = new DummyTransactionImple(new AtomicTransaction());
        
        try
        {
            dummy.commitAndDisassociate();
        }
        catch (final InactiveTransactionException ex)
        {
        }
        
        try
        {
            dummy.rollbackAndDisassociate();
        }
        catch (final InactiveTransactionException ex)
        {
        }
    }
}