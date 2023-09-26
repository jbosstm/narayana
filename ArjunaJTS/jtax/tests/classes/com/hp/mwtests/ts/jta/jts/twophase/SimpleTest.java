/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.twophase;

import jakarta.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.junit.Test;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.hp.mwtests.ts.jta.jts.common.TestResource;

public class SimpleTest
{
    @Test
    public void test() throws Exception
    {
        ORB myORB = null;
        RootOA myOA = null;

        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);

        jtaPropertyManager.getJTAEnvironmentBean().setTransactionManagerClassName(com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple.class.getName());
        jtaPropertyManager.getJTAEnvironmentBean().setUserTransactionClassName(com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple.class.getName());

        jakarta.transaction.TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();

        transactionManager.begin();

        Transaction currentTrans = transactionManager.getTransaction();

        TestResource res1, res2;
        currentTrans.enlistResource( res1 = new TestResource() );
        currentTrans.enlistResource( res2 = new TestResource() );

        currentTrans.delistResource( res2, XAResource.TMSUCCESS );
        currentTrans.delistResource( res1, XAResource.TMSUCCESS );

        transactionManager.commit();

        myOA.destroy();
        myORB.shutdown();
    }
}