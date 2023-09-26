/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.jca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.resource.spi.work.Work;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.Transaction;

import org.junit.Test;

import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.arjuna.ats.internal.jta.transaction.jts.jca.TxWorkManager;
import com.arjuna.ats.internal.jta.transaction.jts.jca.WorkSynchronization;
import com.hp.mwtests.ts.jta.jts.common.TestBase;

public class WorkUnitTest extends TestBase
{
    class DummyWork implements Work
    {       
        public DummyWork ()
        {
        }
        
        public void release ()
        {
        }
        
        public void run ()
        {
        }
    }
    
    @Test
    public void testWorkManager () throws Exception
    {
        DummyWork work = new DummyWork();
        Transaction tx = new TransactionImple();
        
        TxWorkManager.addWork(work, tx);
        
        try
        {
            TxWorkManager.addWork(new DummyWork(), tx);
            
            fail();
        }
        catch (final Throwable ex)
        {
        }
        
        assertTrue(TxWorkManager.hasWork(tx));
        
        assertEquals(work, TxWorkManager.getWork(tx));       
        
        TxWorkManager.removeWork(work, tx);
        
        assertEquals(TxWorkManager.getWork(tx), null);
    }
    
    @Test
    public void testWorkSynchronization () throws Exception
    {
        Transaction tx = new TransactionImple();
        Synchronization ws = new WorkSynchronization(tx);
        DummyWork work = new DummyWork();
        
        TxWorkManager.addWork(work, tx);
        
        try
        {
            ws.beforeCompletion();
            
            fail();
        }
        catch (final IllegalStateException ex)
        {
        }
        
        ws.afterCompletion(Status.STATUS_COMMITTED);
    }
}