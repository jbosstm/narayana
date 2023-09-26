/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.basic;

import org.junit.Test;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class Performance
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

        /*
       * We should have a reference to a factory object (see JTA
       * specification). However, for simplicity we will ignore this.
       */

        long stime = System.currentTimeMillis();

        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        for (int i = 0; i < 1000; i++)
        {
            tm.begin();

            tm.commit();
        }

        long ftime = System.currentTimeMillis();
        double elapsedTime = (ftime - stime)/1000.0;
        double tps = 1000.0/elapsedTime;

        System.err.println("TPS: "+tps);

        myOA.destroy();
        myORB.shutdown();
    }
}