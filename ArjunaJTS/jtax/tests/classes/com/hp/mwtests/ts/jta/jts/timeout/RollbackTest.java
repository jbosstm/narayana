/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.timeout;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class RollbackTest
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

        jakarta.transaction.TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();
        boolean passed = false;

        transactionManager.setTransactionTimeout(3);

        transactionManager.begin();

        try
        {
            Thread.currentThread().sleep(4000);
        }
        catch (Exception ex)
        {
        }

        try
        {
            transactionManager.rollback();

            passed = true;
        }
        catch (IllegalStateException ex)
        {
            passed = false;
        }
        catch (Exception ex)
        {
            passed = false;
        }

        assertTrue(passed);

    }
}