/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.basic;

import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import org.junit.Test;



public class JTATransactionCommitTest
{
    @Test
    public void test() throws Exception
    {
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        Transaction tx = tm.suspend();

        tm.begin();

        tx.commit();

        tm.commit();
    }

}