/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.basic;

import javax.transaction.xa.XAResource;

import org.junit.Test;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.hp.mwtests.ts.jta.jts.common.FirstXAResource;
import com.hp.mwtests.ts.jta.jts.common.LastXAResource;
import com.hp.mwtests.ts.jta.jts.common.TestResource;

public class JTAOrder
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

        boolean passed = false;

        jtaPropertyManager.getJTAEnvironmentBean().setTransactionManagerClassName(com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple.class.getName());
        jtaPropertyManager.getJTAEnvironmentBean().setUserTransactionClassName(com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple.class.getName());

        try
        {
            jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

            XAResource theResource = new TestResource();
            FirstXAResource first = new FirstXAResource();
            LastXAResource last = new LastXAResource();

            System.out.println("Starting top-level transaction.");

            tm.begin();

            jakarta.transaction.Transaction theTransaction = tm.getTransaction();

            theTransaction.enlistResource(theResource);
            theTransaction.enlistResource(last);
            theTransaction.enlistResource(first);

            System.err.println("Committing transaction.");

            tm.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        myOA.destroy();
        myORB.shutdown();
    }
}