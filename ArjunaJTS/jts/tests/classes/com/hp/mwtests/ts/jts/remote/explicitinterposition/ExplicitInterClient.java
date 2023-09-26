/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.remote.explicitinterposition;

import org.omg.CosTransactions.Control;

import com.hp.mwtests.ts.jts.utils.ServerORB;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.Services;
import com.hp.mwtests.ts.jts.TestModule.SetGet;
import com.hp.mwtests.ts.jts.TestModule.SetGetHelper;
import com.hp.mwtests.ts.jts.resources.TestUtility;

public class ExplicitInterClient
{
    public static void main(String[] args) throws Exception
    {
        ServerORB orb = new ServerORB();
        ORB myORB = orb.getORB();
        RootOA myOA = orb.getOA();

        CurrentImple current = OTSImpleManager.current();
        Control theControl = null;

        String objectReference = args[0];

        SetGet SetGetVar = null;
        short h = 0;

        try
        {
            current.begin();
            current.begin();
            current.begin();
        }
        catch (Exception e)
        {
            TestUtility.fail("Caught exception during begin: "+e);
            e.printStackTrace(System.err);
        }

        try
        {
            Services serv = new Services(myORB);

            SetGetVar = SetGetHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(objectReference)));
        }
        catch (Exception ex)
        {
            TestUtility.fail("Failed to bind to setget server: "+ex);
            ex.printStackTrace(System.err);
        }

        try
        {
            theControl = current.get_control();

            SetGetVar.set((short) 2, theControl);
            //	    SetGetVar.set((short) 2, theControl);

            theControl = null;

            System.out.println("Set value.");
        }
        catch (Exception ex1)
        {
            TestUtility.fail("Unexpected system exception during set: "+ex1);
            ex1.printStackTrace(System.err);
        }

        try
        {
            System.out.println("committing first nested action");

            current.commit(true);

            //	    SetGetVar.set((short) 4, current.get_control());

            System.out.println("committing second nested action");

            current.commit(true);
        }
        catch (Exception sysEx)
        {
            TestUtility.fail("Caught unexpected exception during commit: "+sysEx);
            sysEx.printStackTrace(System.err);
        }

        try
        {
            theControl = current.get_control();

            h = SetGetVar.get(theControl);

            theControl = null;

            System.out.println("Got value.");
        }
        catch (Exception ex2)
        {
            TestUtility.fail("Unexpected system exception during get: "+ex2);
            ex2.printStackTrace(System.err);
        }

        try
        {
            current.commit(true);

            System.out.println("committed top-level action");
        }
        catch (Exception ep)
        {
            TestUtility.fail("Caught commit exception for top-level action: "+ep);
            ep.printStackTrace(System.err);
        }

        myOA.destroy();
        myORB.shutdown();

        System.out.println("Passed");
    }
}