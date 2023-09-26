/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.timeout;

import org.junit.Test;

public class RollbackTest
{
    @Test
    public void test() throws Exception
    {
        jakarta.transaction.TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();

        transactionManager.setTransactionTimeout(3);

        transactionManager.begin();

        Thread.currentThread().sleep(4000);

        transactionManager.rollback();
    }
}