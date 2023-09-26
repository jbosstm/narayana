/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.nested;

import jakarta.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.junit.Test;

import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.hp.mwtests.ts.jta.common.TestResource;

public class SimpleNestedTest
{
    @Test
    public void testEnabled () throws Exception
    {
        jtaPropertyManager.getJTAEnvironmentBean().setSupportSubtransactions(true);

        jakarta.transaction.TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();

        transactionManager.begin();

        transactionManager.begin();

        Transaction currentTrans = transactionManager.getTransaction();
        TestResource res1, res2;
        currentTrans.enlistResource(res1 = new TestResource());
        currentTrans.enlistResource(res2 = new TestResource());

        currentTrans.delistResource(res2, XAResource.TMSUCCESS);
        currentTrans.delistResource(res1, XAResource.TMSUCCESS);

        transactionManager.commit();

        transactionManager.commit();
    }

    // testDisabled moved to its own class, as it needs separate jvm to allow different property value in static init.
}