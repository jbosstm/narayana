/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jts.remote.servers;

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.Services;
import com.hp.mwtests.ts.jts.orbspecific.resources.setget_i;
import com.hp.mwtests.ts.jts.resources.TestUtility;
import com.hp.mwtests.ts.jts.utils.ServerORB;

public class SetGetServer
{
    public static void main(String[] args) throws Exception
    {
        ServerORB orb = new ServerORB();
        ORB myORB = orb.getORB();
        RootOA myOA = orb.getOA();

        String refFile = args[0];

        setget_i impl = new setget_i();
        Services serv = new Services(myORB);

        try
        {
            TestUtility.registerService(refFile, myORB.orb().object_to_string(impl.getReference()));

            System.out.println("Ready");
            myOA.run();
        }
        catch (Exception e)
        {
            TestUtility.fail("SetGetServer caught exception: "+e);
        }

        myOA.shutdownObject(impl);

        System.out.println("**Object server exiting**");
    }
}