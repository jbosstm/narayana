/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.remote.implicit;

import com.hp.mwtests.ts.jts.utils.ServerORB;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.Services;
import com.hp.mwtests.ts.jts.TestModule.TranGrid;
import com.hp.mwtests.ts.jts.TestModule.TranGridHelper;
import com.hp.mwtests.ts.jts.resources.TestUtility;

public class ImplicitClient
{
    public static void main(String[] args) throws Exception
    {
        ServerORB orb = new ServerORB();
        ORB myORB = orb.getORB();
        RootOA myOA = orb.getOA();

        String refFile = args[0];

        CurrentImple current = OTSImpleManager.current();

        TranGrid TranGridVar = null;   // pointer the grid object that will be used.
        short h = 0, w = 0, v = 0;

        try
        {
            current.begin();

            Services serv = new Services(myORB);
            TranGridVar = TranGridHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(refFile)));

            try
            {
                h = TranGridVar.height();
                w = TranGridVar.width();
            }
            catch (Exception e)
            {
                TestUtility.fail("Invocation failed: "+e);
            }

            System.out.println("height is "+h);
            System.out.println("width  is "+w);

            try
            {
                System.out.println("calling set");

                TranGridVar.set((short) 2, (short) 4, (short) 123);

                System.out.println("calling get");

                v = TranGridVar.get((short) 2, (short) 4);
            }
            catch (Exception sysEx)
            {
                TestUtility.fail("Grid set/get failed: "+sysEx);
                sysEx.printStackTrace(System.err);
            }

            // no problem setting and getting the element:

            System.out.println("trangrid[2,4] is "+v);

            // sanity check: make sure we got the value 123 back:

            if (v != 123)
            {
                // oops - we didn't:

                current.rollback();
                TestUtility.fail("Result not as expected");
            }
            else
            {
                current.commit(true);
            }
        }
        catch (Exception e)
        {
            TestUtility.fail("Caught exception: "+e);
            e.printStackTrace(System.err);
        }

        myOA.destroy();
        myORB.shutdown();

        System.out.println("Passed");
    }
}