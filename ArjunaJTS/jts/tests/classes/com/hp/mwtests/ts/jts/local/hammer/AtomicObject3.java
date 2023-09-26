/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.local.hammer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.logging.Logger;
import org.junit.Test;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.hp.mwtests.ts.jts.orbspecific.resources.AtomicObject;
import com.hp.mwtests.ts.jts.orbspecific.resources.AtomicWorker3;
import com.hp.mwtests.ts.jts.orbspecific.resources.ThreadObject3b;

public class AtomicObject3
{
    public static final Logger logger = Logger.getLogger("AtomicObject3");

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


        AtomicWorker3.init();

        AtomicWorker3.atomicObject_1 = new AtomicObject();
        AtomicWorker3.atomicObject_2 = new AtomicObject();

        logger.info(AtomicWorker3.atomicObject_1.get_uid());
        logger.info(AtomicWorker3.atomicObject_2.get_uid());

        assertTrue( AtomicWorker3.atomicObject_1.set(START_VALUE_1) );

        assertTrue( AtomicWorker3.atomicObject_2.set(START_VALUE_2) );

        Thread thr1 = new ThreadObject3b(1);
        Thread thr2 = new ThreadObject3b(2);

        thr1.start();
        thr2.start();

        try
        {
            thr1.join();
            thr2.join();
        }
        catch (InterruptedException e)
        {
            fail(e.toString());
        }

        AtomicWorker3.get12(0, 0);
        AtomicWorker3.get21(0, 0);

        try
        {
            int value1 = AtomicWorker3.get1();
            int value2 = AtomicWorker3.get2();

            assertEquals(EXPECTED_VALUE, (value1 + value2) );
        }
        catch (Exception e)
        {
            logger.warn(e.getMessage(), e);
            fail();
        }

        myOA.destroy();
        myORB.shutdown();
    }
}