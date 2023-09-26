/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.local.transactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.jts.extensions.AtomicTransaction;
import com.hp.mwtests.ts.jts.orbspecific.resources.DemoResource;
import com.hp.mwtests.ts.jts.orbspecific.resources.DemoSubTranResource;
import com.hp.mwtests.ts.jts.orbspecific.resources.demosync;
import com.hp.mwtests.ts.jts.resources.TestBase;


public class AtomicTransactionUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        AtomicTransaction A = new AtomicTransaction();
        
        A.begin();
        
        assertTrue(A.get_transaction_name() != null);
        
        A.set_timeout(10);
        
        assertEquals(A.get_timeout(), 10);
        
        assertTrue(A.getTimeout() != -1);
        assertTrue(A.get_txcontext() != null);
        assertTrue(A.get_uid().notEquals(Uid.nullUid()));
        assertTrue(A.control() != null);
        
        assertFalse(A.equals(null));
        assertTrue(A.equals(A));
        assertFalse(A.equals(new AtomicTransaction()));
        
        A.rollback();
    }
    
    @Test
    public void testCommit () throws Exception
    {
        AtomicTransaction A = new AtomicTransaction();
        
        A.begin();
        
        assertTrue(A.get_txcontext() != null);
        
        A.registerResource(new DemoResource().getResource());
        
        AtomicTransaction B = new AtomicTransaction();
        
        B.begin();
        
        B.registerSubtranAware(new DemoSubTranResource().getReference());
        
        B.commit(true);
        
        A.commit(true);
        
        A = new AtomicTransaction();
        
        A.begin();
        
        A.rollbackOnly();
        
        try
        {
            A.commit(true);
            
            fail();
        }
        catch (final TRANSACTION_ROLLEDBACK ex)
        {
        }
    }
    
    @Test
    public void testRollback () throws Exception
    {
        AtomicTransaction A = new AtomicTransaction();
        
        A.begin();
        
        A.registerSynchronization(new demosync(false).getReference());
        
        A.rollback();
    }
    
    @Test
    public void testSuspendResume () throws Exception
    {
        AtomicTransaction A = new AtomicTransaction();
        
        A.begin();
        
        assertTrue(A.control() != null);
        
        A.suspend();
        
        assertEquals(OTSImpleManager.current().get_control(), null);
        
        A.resume();
        
        assertTrue(OTSImpleManager.current() != null);
        
        A.rollback();
    }
}