/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.jca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.omg.CORBA.WrongTransaction;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.SubordinateAtomicTransaction;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.TransactionImple;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.jts.common.TestBase;

class DummySubordinateAtomicTransaction extends SubordinateAtomicTransaction
{
    public DummySubordinateAtomicTransaction ()
    {
        super(new Uid());
    }
    
    public boolean checkForCurrent ()
    {
        return super.checkForCurrent();
    }
}


public class SubordinateTxUnitTest extends TestBase
{
    @Test
    public void testTransactionImple () throws Exception
    {
        TransactionImple tx = new TransactionImple(new Uid());
        TransactionImple dummy = new TransactionImple(new Uid());
        
        tx.recordTransaction();
        
        assertFalse(tx.equals(dummy));
        
        assertTrue(tx.toString() != null);
        
        tx.recover();
    }
    
    @Test
    public void testAtomicTransaction () throws Exception
    {
        XidImple xid = new XidImple(new Uid());
        SubordinateAtomicTransaction saa1 = new SubordinateAtomicTransaction(new Uid());
        SubordinateAtomicTransaction saa2 = new SubordinateAtomicTransaction(new Uid(), xid, 0);
        
        assertEquals(saa2.getXid(), xid);
        
        try
        {
            saa2.end(true);
            
            fail();
        }
        catch (final WrongTransaction ex)
        {
        }
        
        try
        {
            saa2.abort();
            
            fail();
        }
        catch (final WrongTransaction ex)
        {
        }
        
        DummySubordinateAtomicTransaction dsat = new DummySubordinateAtomicTransaction();
        
        assertFalse(dsat.checkForCurrent());
    }
}