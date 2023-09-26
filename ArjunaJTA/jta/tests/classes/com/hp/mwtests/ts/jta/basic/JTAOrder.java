/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.basic;

import static org.junit.Assert.assertEquals;

import jakarta.transaction.Status;
import javax.transaction.xa.XAResource;

import org.junit.Test;

import com.hp.mwtests.ts.jta.common.FirstXAResource;
import com.hp.mwtests.ts.jta.common.LastXAResource;
import com.hp.mwtests.ts.jta.common.TestResource;

public class JTAOrder
{
    @Test
    public void test() throws Exception
    {
        XAResource theResource = new TestResource();
        FirstXAResource first = new FirstXAResource();
        LastXAResource last = new LastXAResource();

        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        System.out.println("Starting top-level transaction.");

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        theTransaction.enlistResource(theResource);
        theTransaction.enlistResource(last);
        theTransaction.enlistResource(first);

        System.out.println("Committing transaction.");

        tm.commit();

        assertEquals(Status.STATUS_COMMITTED, theTransaction.getStatus());
    }
}