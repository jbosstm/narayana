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
import com.hp.mwtests.ts.jts.orbspecific.resources.DistributedHammerWorker1;
import com.hp.mwtests.ts.jts.resources.TestUtility;

public class DistributedHammer1
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

            DistributedHammerWorker1.hammerObject_1 = HammerHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(server1)));
            DistributedHammerWorker1.hammerObject_2 = HammerHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(server2)));

            TestUtility.assertTrue( DistributedHammerWorker1.hammerObject_1.set(START_VALUE_1, null) );

            TestUtility.assertTrue( DistributedHammerWorker1.hammerObject_2.set(START_VALUE_2, null) );

            DistributedHammerWorker1.get12('m', 0);
            DistributedHammerWorker1.get21('m', 0);
        }
        catch (Exception e)
        {
            TestUtility.fail("DistributedHammer1: "+e);
            e.printStackTrace(System.err);
        }

        for (int i = 0; i < 100; i++)
            DistributedHammerWorker1.randomOperation('1', 0);

        DistributedHammerWorker1.get12('m', 0);
        DistributedHammerWorker1.get21('m', 0);

        IntHolder value1 = new IntHolder(0);
        IntHolder value2 = new IntHolder(0);

        TestUtility.assertTrue( DistributedHammerWorker1.get1(value1) | DistributedHammerWorker1.get2(value2) );

        TestUtility.assertEquals(EXPECTED_RESULT, (value1.value + value2.value) );

        myOA.destroy();
        myORB.shutdown();

        System.out.println("Passed");
    }
}