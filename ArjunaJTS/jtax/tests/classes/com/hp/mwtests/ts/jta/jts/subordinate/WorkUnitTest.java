/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.subordinate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.resource.spi.work.Work;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.Transaction;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.arjuna.ats.internal.jta.transaction.jts.jca.TxWorkManager;
import com.arjuna.ats.internal.jta.transaction.jts.jca.WorkSynchronization;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class WorkUnitTest
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
    
    @Before
    public void setUp () throws Exception
    {
        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);
    }
    
    @After
    public void tearDown () throws Exception
    {
        myOA.destroy();
        myORB.shutdown();
    }
    
    private ORB myORB = null;
    private RootOA myOA = null;
}