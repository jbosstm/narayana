/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.local.transactions;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.omg.CosTransactions.Control;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class TransactionTest5
{
    @Test
    public void test() throws Exception
    {
        ORB myORB = null;
        RootOA myOA = null;

        try
        {
            myORB = ORB.getInstance("test");
            myOA = OA.getRootOA(myORB);

            myORB.initORB(new String[] {}, null);
            myOA.initOA();

            ORBManager.setORB(myORB);
            ORBManager.setPOA(myOA);
        }
        catch (Exception e)
        {
            System.err.println("Initialisation failed: "+e);
        }

        try
        {
            OTSManager.get_current().begin();

            Control cont = OTSManager.get_current().get_control();

            OTSManager.get_current().commit(true);

            OTSManager.get_current().resume(cont);

            OTSManager.get_current().rollback_only();

            System.out.println("\nFailed.");
        }
        catch (org.omg.CosTransactions.NoTransaction ex)
        {
            System.out.println("\nPassed.");
        }
        catch (Throwable e)
        {
            fail("caught: "+e);

            e.printStackTrace();
        }

        myOA.destroy();
        myORB.shutdown();
    }
}