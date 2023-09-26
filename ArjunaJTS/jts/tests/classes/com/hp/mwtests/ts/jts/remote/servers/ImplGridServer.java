/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jts.remote.servers;

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.Services;
import com.hp.mwtests.ts.jts.orbspecific.resources.trangrid_i;
import com.hp.mwtests.ts.jts.resources.TestUtility;
import com.hp.mwtests.ts.jts.utils.ServerORB;

public class ImplGridServer
{
    public static void main(String[] args) throws Exception
    {
        ServerORB orb = new ServerORB();
        ORB myORB = orb.getORB();
        RootOA myOA = orb.getOA();

        String refFile = args[0];

        trangrid_i gridI = new trangrid_i((short) 100, (short) 100);
        Services serv = new Services(myORB);

        try
        {
            TestUtility.registerService(refFile, myORB.orb().object_to_string(gridI.getReference()));

            System.out.println("Ready");

            myOA.run();
        }
        catch (Exception e)
        {
            TestUtility.fail("ImplGrid server caught exception: "+e);
        }

        myOA.shutdownObject(gridI);

        System.out.println("**ImplGrid server exiting**");
    }
}