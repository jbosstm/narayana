/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.remote.hammer;

import org.omg.CORBA.IntHolder;

import com.hp.mwtests.ts.jts.utils.ServerORB;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.Services;
import com.hp.mwtests.ts.jts.TestModule.HammerHelper;
import com.hp.mwtests.ts.jts.orbspecific.resources.DHThreadObject3b;
import com.hp.mwtests.ts.jts.orbspecific.resources.DistributedHammerWorker3;
import com.hp.mwtests.ts.jts.resources.TestUtility;

public class DistributedHammer3
{
    private final static int   START_VALUE_1 = 10;
    private final static int   START_VALUE_2 = 101;

    private final static int   EXPECTED_RESULT = START_VALUE_1 + START_VALUE_2;

    public static void main(String[] args) throws Exception
    {
        ServerORB orb = new ServerORB();
        ORB myORB = orb.getORB();
        RootOA myOA = orb.getOA();

        String server1 = args[0];
        String server2 = args[1];

        try
        {
            Services serv = new Services(myORB);

            DistributedHammerWorker3.hammerObject_1 = HammerHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(server1)));
            DistributedHammerWorker3.hammerObject_2 = HammerHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(server2)));

            TestUtility.assertTrue( DistributedHammerWorker3.hammerObject_1.set(START_VALUE_1, null) );

            TestUtility.assertTrue( DistributedHammerWorker3.hammerObject_2.set(START_VALUE_2, null) );

            DistributedHammerWorker3.get12(0, 0);
            DistributedHammerWorker3.get21(0, 0);
        }
        catch (Exception e)
        {
            TestUtility.fail("DistributedHammer3: "+e);
            e.printStackTrace(System.err);
        }

        DHThreadObject3b thr1 = new DHThreadObject3b(1);
        DHThreadObject3b thr2 = new DHThreadObject3b(2);

        thr1.start();
        thr2.start();

        try
        {
            thr1.join();
            thr2.join();
        }
        catch (InterruptedException e)
        {
            System.err.println(e);
        }

        DistributedHammerWorker3.get12(0, 0);
        DistributedHammerWorker3.get21(0, 0);

        IntHolder value1 = new IntHolder(0);
        IntHolder value2 = new IntHolder(0);

        TestUtility.assertTrue( DistributedHammerWorker3.get1(value1) | DistributedHammerWorker3.get2(value2) );

        TestUtility.assertEquals(EXPECTED_RESULT, (value1.value + value2.value));

        myOA.destroy();
        myORB.shutdown();

        System.out.println("Passed");
    }
}