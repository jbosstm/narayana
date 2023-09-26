/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.remote.current;

import org.omg.CosTransactions.Control;

import com.hp.mwtests.ts.jts.utils.ServerORB;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.Services;
import com.hp.mwtests.ts.jts.TestModule.grid;
import com.hp.mwtests.ts.jts.TestModule.gridHelper;
import com.hp.mwtests.ts.jts.resources.TestUtility;

public class CurrentTest
{
    public static void main(String[] args) throws Exception
    {
        ServerORB orb = new ServerORB();
        ORB myORB = orb.getORB();
        RootOA myOA = orb.getOA();

        CurrentImple current = OTSImpleManager.current();
        Control myControl = null;

        String gridReference = args[0];

        grid gridVar = null;  // pointer the grid object that will be used.
        int h = -1, w = -1, v = -1;

        System.out.println("Beginning transaction.");

        try
        {
            current.begin();

            myControl = current.get_control();

            TestUtility.assertTrue(myControl != null);
        }
        catch (Exception sysEx)
        {
            sysEx.printStackTrace(System.err);
            TestUtility.fail(sysEx.toString());
        }

        try
        {
            Services serv = new Services(myORB);

            gridVar = gridHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(gridReference)));
        }
        catch (Exception sysEx)
        {
            TestUtility.fail("failed to bind to grid: "+sysEx);
            sysEx.printStackTrace(System.err);
        }

        try
        {
            h = gridVar.height();
            w = gridVar.width();
        }
        catch (Exception sysEx)
        {
            TestUtility.fail("grid height/width failed: "+sysEx);
            sysEx.printStackTrace(System.err);
        }

        System.out.println("height is "+h);
        System.out.println("width  is "+w);

        try
        {
            gridVar.set(2, 4, 123, myControl);
            v = gridVar.get(2, 4, myControl);
        }
        catch (Exception sysEx)
        {
            TestUtility.fail("grid set/get failed: "+sysEx);
            sysEx.printStackTrace(System.err);
        }

        // no problem setting and getting the element:
        System.out.println("grid[2,4] is "+v);

        // sanity check: make sure we got the value 123 back:
        if (v != 123)
        {
            TestUtility.fail("something went seriously wrong");

            try
            {
                current.rollback();
            }
            catch (Exception e)
            {
                TestUtility.fail("rollback error: "+e);
                e.printStackTrace(System.err);
            }
        }
        else
        {
            System.out.println("Committing transaction.");

            try
            {
                current.commit(true);
            }
            catch (Exception e)
            {
                TestUtility.fail("commit error: "+e);
                e.printStackTrace(System.err);
            }

            myOA.destroy();
            myORB.shutdown();

            System.out.println("Passed");
        }
    }
}