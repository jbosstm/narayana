/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.twophase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.startsWith;

import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.Transaction;

import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple;
import com.hp.mwtests.ts.jta.jts.common.TestBase;


@RunWith(BMUnitRunner.class)
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

    @Test
    @BMRule(name = "Throw InvalidControl from resumeWrapper",
            targetClass = "com.arjuna.ats.internal.jts.orbspecific.CurrentImple",
            targetMethod = "resumeWrapper",
            targetLocation = "AT ENTRY",
            action = "throw new org.omg.CosTransactions.InvalidControl()")
    public void testResumeInvalidControlThrowsInvalidTransactionException () throws Exception
    {
        TransactionManagerImple tmi = new TransactionManagerImple();

        tmi.begin();
        Transaction tx = tmi.getTransaction();
        tmi.suspend();

        try
        {
            // InvalidControl is thrown naturally when resuming a remote
            // transaction whose coordinator is no longer reachable (e.g. the
            // remote ORB crashed). Reproducing that requires a multi-node
            // setup, so Byteman injects the fault directly into resumeWrapper.
            tmi.resume(tx);
            fail("Expected InvalidTransactionException");
        }
        catch (InvalidTransactionException e)
        {
            // JBTM-4013: before the fix, initCause on InvalidTransactionException
            // (which extends RemoteException) threw IllegalStateException here
            // instead of propagating as InvalidTransactionException
            // RemoteException.getMessage() appends nested exception when detail is set,
            // so we check that the message starts with the repo ID
            assertThat(e.getMessage(), startsWith("IDL:omg.org/CosTransactions/InvalidControl:1.0"));
            // Full message includes the nested exception detail:
            // "IDL:omg.org/CosTransactions/InvalidControl:1.0; nested exception is: \n\torg.omg.CosTransactions.InvalidControl: IDL:omg.org/CosTransactions/InvalidControl:1.0"
            assertTrue("message should contain nested exception",
                e.getMessage().contains("; nested exception is:"));
            assertNotNull("cause should be preserved via detail", e.getCause());
            assertTrue(e.getCause() instanceof org.omg.CosTransactions.InvalidControl);
        }
    }

    @Test
    public void testResumeWithForeignTransactionThrowsInvalidTransactionException () throws Exception
    {
        TransactionManagerImple tmi = new TransactionManagerImple();

        Transaction foreignTransaction = new Transaction() {
            public void commit() {}
            public boolean delistResource(javax.transaction.xa.XAResource xaRes, int flag) { return false; }
            public boolean enlistResource(javax.transaction.xa.XAResource xaRes) { return false; }
            public int getStatus() { return jakarta.transaction.Status.STATUS_ACTIVE; }
            public void registerSynchronization(jakarta.transaction.Synchronization sync) {}
            public void rollback() {}
            public void setRollbackOnly() {}
        };

        try
        {
            tmi.resume(foreignTransaction);
            fail("Expected InvalidTransactionException");
        }
        catch (InvalidTransactionException e)
        {
            // resume must reject transaction types it does not recognise
        }
    }

}
