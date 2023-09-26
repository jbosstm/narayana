/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.remote.current;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.omg.CosTransactions.Control;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class SuspendResume
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


        CurrentImple current = OTSImpleManager.current();
        Control myControl = null;

        System.out.println("Beginning transaction.");

        try
        {
            current.begin();

            myControl = current.get_control();

            assertNotNull( myControl );
        }
        catch (Exception sysEx)
        {
            sysEx.printStackTrace(System.err);
            fail();
        }

        System.out.println("Committing transaction.");

        try
        {
            current.commit(true);

            current.resume(myControl);
        }
        catch (Exception e)
        {
            fail("commit error: " + e);
            e.printStackTrace(System.err);
        }

        myOA.destroy();
        myORB.shutdown();

        System.out.println("Test completed successfully.");
    }
}