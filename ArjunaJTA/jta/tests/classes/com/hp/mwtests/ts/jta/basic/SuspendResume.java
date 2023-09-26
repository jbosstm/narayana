/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.basic;


import org.junit.Test;

public class SuspendResume
{
    @Test
    public void test() throws Exception
    {
        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        tm.commit();

        tm.resume(theTransaction);
    }
}