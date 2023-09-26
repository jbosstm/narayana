/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.timeout;

import org.junit.Test;

public class SimpleTest
{
    @Test
    public void test() throws Exception
    {
        jakarta.transaction.TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();

        transactionManager.setTransactionTimeout(3);

        transactionManager.begin();

        Thread.currentThread().sleep(4000);

        try
        {
            transactionManager.commit();
        }
        catch (final jakarta.transaction.RollbackException ex)
        {
            // expected
        }
    }
}