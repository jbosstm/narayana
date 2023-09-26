/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.basic;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class SuspendResume
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
        
        try
        {
            jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                    .transactionManager();

            System.out.println("Starting top-level transaction.");

            tm.begin();

            jakarta.transaction.Transaction theTransaction = tm
                    .getTransaction();

            if (theTransaction != null)
            {
                tm.commit();

                tm.resume(theTransaction);
            }
            else
            {
                tm.rollback();
                fail("Error - could not get transaction!");
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        myOA.destroy();
        myORB.shutdown();
    }
}