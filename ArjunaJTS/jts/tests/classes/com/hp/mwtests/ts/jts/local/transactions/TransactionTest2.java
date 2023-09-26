/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.local.transactions;

import org.junit.Test;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class TransactionTest2
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


        int count = 0;

        System.out.println("Testing memory allocation.");
        System.out.println("Creating as many transactions as possible.\n");

        try
        {
            for (;;)
            {
                OTSManager.get_current().begin();
                count++;
            }
        }
        catch (Exception e)
        {
            System.err.println("begin caught: "+e);

            System.gc();
        }
        catch (Error e)
        {
            System.err.println("begin caught: "+e);
            e.printStackTrace();

            System.gc();
        }

        System.out.println("\nbegan: "+count);

        try
        {
            int created = count;

            System.out.println("\nNow rolling back transactions.");

            for (int i = 0; i < created; i++)
            {
                try
                {
                    System.out.println(""+count);
                    OTSManager.get_current().rollback();
                    count--;
                }
                catch (OutOfMemoryError em)
                {
                    em.printStackTrace();

                    System.gc();
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("rollback caught: "+e);

            System.gc();
        }
        catch (Error e)
        {
            System.err.println("rollback caught: "+e);
            e.printStackTrace();

            System.gc();
        }

        System.out.println("\nStill to rollback: "+count);

        myOA.destroy();
        myORB.shutdown();
    }

}