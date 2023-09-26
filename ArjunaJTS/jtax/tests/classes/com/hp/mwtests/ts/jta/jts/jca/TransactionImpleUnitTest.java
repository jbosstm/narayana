/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.jca;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.TransactionImple;
import com.hp.mwtests.ts.jta.jts.common.TestBase;

public class TransactionImpleUnitTest extends TestBase
{   
    @Test
    public void test () throws Exception
    {
        TransactionImple tx = new TransactionImple(0);
        
        tx.recordTransaction();
        
        assertFalse(tx.equals(null));
        assertTrue(tx.equals(tx));
        assertFalse(tx.equals(new TransactionImple(0)));
        
        assertTrue(tx.toString() != null);
        assertTrue(tx.baseXid() != null);
        
        tx.recover();
        
        assertTrue(tx.activated());  
    }
}