/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.local.hammer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.hp.mwtests.ts.jts.orbspecific.resources.AtomicObject;
import com.hp.mwtests.ts.jts.orbspecific.resources.AtomicWorker2;
import com.hp.mwtests.ts.jts.orbspecific.resources.ThreadObject2;

public class AtomicObject2
{
    private final static int START_VALUE_1 = 10;
    private final static int START_VALUE_2 = 101;
    private final static int EXPECTED_VALUE = START_VALUE_1 + START_VALUE_2;

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

        AtomicWorker2.init();

        AtomicWorker2.atomicObject_1 = new AtomicObject();
        AtomicWorker2.atomicObject_2 = new AtomicObject();

        System.out.println(AtomicWorker2.atomicObject_1.get_uid());
        System.out.println(AtomicWorker2.atomicObject_2.get_uid());

        assertTrue( AtomicWorker2.atomicObject_1.set(START_VALUE_1) );

        assertTrue( AtomicWorker2.atomicObject_2.set(START_VALUE_2) );

        ThreadObject2 thr1 = new ThreadObject2('1');
        ThreadObject2 thr2 = new ThreadObject2('2');

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

        AtomicWorker2.get12('m', 0);
        AtomicWorker2.get21('m', 0);

        try
        {
            int value1 = AtomicWorker2.get1();
            int value2 = AtomicWorker2.get2();

            assertEquals(EXPECTED_VALUE, (value1 + value2) );
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }

        myOA.destroy();
        myORB.shutdown();
    }
}