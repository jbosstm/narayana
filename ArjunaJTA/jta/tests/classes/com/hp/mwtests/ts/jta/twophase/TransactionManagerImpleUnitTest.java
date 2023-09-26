/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.twophase;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;


public class TransactionManagerImpleUnitTest
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
}