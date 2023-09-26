/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jts.remote.servers;

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.Services;
import com.hp.mwtests.ts.jts.TestModule.HammerHelper;
import com.hp.mwtests.ts.jts.TestModule.HammerPOATie;
import com.hp.mwtests.ts.jts.orbspecific.resources.HammerObject;
import com.hp.mwtests.ts.jts.resources.TestUtility;
import com.hp.mwtests.ts.jts.utils.ServerORB;


public class HammerServer
{
    public static void main(String[] args) throws Exception
    {
        ServerORB orb = new ServerORB();
        ORB myORB = orb.getORB();
        RootOA myOA = orb.getOA();

        String refFile = args[0];

        HammerPOATie theObject = new HammerPOATie(new HammerObject());

        myOA.objectIsReady(theObject);

        Services serv = new Services(myORB);

        try
        {
            TestUtility.registerService( refFile, myORB.orb().object_to_string(HammerHelper.narrow(myOA.corbaReference(theObject))) );

            System.out.println("\nIOR file: "+refFile);

            System.out.println("Ready");

            //assertReady();
            myOA.run();
        }
        catch (Exception e)
        {
            //fail("HammerServer caught exception: "+e);
            e.printStackTrace(System.err);
        }

        myOA.shutdownObject(theObject);

        System.out.println("**HammerServer exiting**");
    }
}