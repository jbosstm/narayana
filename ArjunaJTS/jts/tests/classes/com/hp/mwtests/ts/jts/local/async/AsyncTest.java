/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.local.async;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.omg.CosTransactions.Current;

import com.hp.mwtests.ts.jts.utils.ServerORB;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.hp.mwtests.ts.jts.orbspecific.resources.DemoResource;
import com.hp.mwtests.ts.jts.orbspecific.resources.heuristic;

public class AsyncTest
{
    @Test
    public void test() throws Exception
    {
        boolean errorp = false;
        boolean errorc = false;

        ServerORB orb = new ServerORB();
        ORB myORB = orb.getORB();
        RootOA myOA = orb.getOA();

        try {

            Current current = OTSManager.get_current();

            DemoResource.printThread = true;

            current.begin();

            for (int j = 0; j < 100; j++)
            {
                if ((j == 10) && (errorp || errorc))
                {
                    boolean heuristicPrepare = errorp;
                    heuristic h = new heuristic(heuristicPrepare);

                    current.get_control().get_coordinator().register_resource(h.getReference());

                    h = null;
                }

                DemoResource r = new DemoResource();

                r.registerResource();

                r = null;
            }

            System.out.println("committing top-level transaction");
            current.commit(false);

            System.out.println("Test completed.");
        }
        catch (org.omg.CORBA.TRANSACTION_ROLLEDBACK e)
        {
            System.out.println("Caught exception: "+e);

            assertTrue(errorp || errorc);

        }

        myOA.destroy();
        myORB.shutdown();
    }
    
    public static void main (String[] args)
    {
        AsyncTest obj = new AsyncTest();
        
        try
        {
            obj.test();
        }
        catch (final Exception ex)
        {
            ex.printStackTrace();
        }
    }
}