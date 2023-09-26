/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.basic;

import static org.junit.Assert.assertEquals;

import jakarta.transaction.Status;

import org.junit.Test;

public class JTAAbort
{
    @Test
    public void test() throws Exception
    {
        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        assertEquals(Status.STATUS_ACTIVE, theTransaction.getStatus());

        theTransaction.rollback();

        assertEquals(Status.STATUS_ROLLEDBACK, theTransaction.getStatus());

        assertEquals(Status.STATUS_ROLLEDBACK, tm.getStatus());

        theTransaction = tm.suspend();

        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());

        tm.resume(theTransaction);

        assertEquals(Status.STATUS_ROLLEDBACK, tm.getStatus());

        tm.suspend();
    }
}