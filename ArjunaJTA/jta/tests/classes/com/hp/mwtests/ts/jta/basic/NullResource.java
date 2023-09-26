/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.basic;

import org.junit.Test;

public class NullResource
{
    @Test
    public void test() throws Exception
    {
        for (int i = 0; i < 1000; i++)
        {
            jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

            tm.begin();

            tm.getTransaction().rollback();

            tm.suspend();
        }
    }
}