/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.twophase;

import jakarta.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.junit.Test;

import com.hp.mwtests.ts.jta.common.TestResource;

public class SimpleTest
{
    @Test
    public void test() throws Exception
    {
        jakarta.transaction.TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();

        transactionManager.begin();

        Transaction currentTrans = transactionManager.getTransaction();

        TestResource res1, res2;
        currentTrans.enlistResource( res1 = new TestResource() );
        currentTrans.enlistResource( res2 = new TestResource() );

        currentTrans.delistResource( res2, XAResource.TMSUCCESS );
        currentTrans.delistResource( res1, XAResource.TMSUCCESS );

        transactionManager.commit();
    }
}