/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.twophase;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple;
import com.hp.mwtests.ts.jta.jts.common.TestBase;


public class TransactionManagerImpleUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        TransactionManagerImple tmi = new TransactionManagerImple();
        
        assertEquals(tmi.getTransaction(), null);
        
        assertEquals(tmi.getObjectInstance(null, null, null, null), tmi);
        
        tmi.setTransactionTimeout(10);
        
        assertEquals(tmi.getTimeout(), 10);
    }
    
    @Test
    public void testNested () throws Exception
    {
        TransactionManagerImple tmi = new TransactionManagerImple();
        
        tmi.begin();
        
        try
        {
            tmi.begin();
        }
        catch (final Throwable ex)
        {
        }
        
        tmi.rollback();
    }
}