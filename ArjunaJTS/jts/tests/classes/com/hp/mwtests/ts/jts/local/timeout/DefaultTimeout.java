/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.local.timeout;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

import static org.junit.Assert.fail;

public class DefaultTimeout
{
    // Use a short timeout so the test completes quickly without real-time waits
    private static final int TEST_TIMEOUT_SECONDS = 1;

    private ORB myORB;
    private RootOA myOA;

    @Before
    public void setUp() throws Exception
    {
        myORB = ORB.getInstance("defaultTimeoutTest");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);
    }

    @After
    public void tearDown()
    {
        myOA.destroy();
        myORB.shutdown();
    }

    @Test
    public void test() throws Exception
    {
        // Override the default timeout with a short value so the test
        // does not rely on any externally configured (potentially long) timeout.
        arjPropertyManager.getCoordinatorEnvironmentBean().setDefaultTimeout(TEST_TIMEOUT_SECONDS);

        System.out.println("Thread " + Thread.currentThread() + " starting transaction.");

        OTSManager.get_current().begin();

        // Sleep for twice the timeout to guarantee the reaper fires.
        Thread.sleep(TEST_TIMEOUT_SECONDS * 1000 * 2);

        System.out.println("Thread " + Thread.currentThread() + " attempting commit (should fail — timeout should have fired).");

        try
        {
            OTSManager.get_current().commit(false);
            fail("Transaction committed after timeout — expected a rollback exception.");
        }
        catch (Exception e)
        {
            System.out.println("Caught expected exception: " + e);
            System.out.println("Timeout went off as expected. Test completed successfully.");
        }
    }
}