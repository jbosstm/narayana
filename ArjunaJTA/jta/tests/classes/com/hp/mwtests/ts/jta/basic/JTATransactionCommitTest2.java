/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import jakarta.transaction.NotSupportedException;
import jakarta.transaction.Status;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import org.junit.Test;



public class JTATransactionCommitTest2
{
    @Test
    public void test() throws Exception
    {
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        Transaction tx = tm.getTransaction();

        tx.commit();

        assertEquals(Status.STATUS_COMMITTED, tm.getStatus());

        try {
            tm.begin();

            fail("Begin call completed successfully - this shouldn't have happened");
        }
        catch (NotSupportedException e)
        {
            System.out.println("NotSupportedException \""+e.getMessage()+"\" occurred this is expected and correct");
        }
    }
}