/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jts.remote.servers;

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.Services;
import com.hp.mwtests.ts.jts.orbspecific.resources.grid_i;
import com.hp.mwtests.ts.jts.resources.TestUtility;
import com.hp.mwtests.ts.jts.utils.ServerORB;

public class GridServer
{
    public static void main(String[] args) throws Exception
    {
        ServerORB orb = new ServerORB();
        ORB myORB = orb.getORB();
        RootOA myOA = orb.getOA();

        String gridReference = args[0];

        grid_i myGrid = new grid_i(100, 100);
        Services serv = new Services(myORB);

        try
        {
            TestUtility.registerService(gridReference, myORB.orb().object_to_string(myGrid.getReference()));

            System.out.println("Ready");

            myOA.run();
        }
        catch (Exception e)
        {
            TestUtility.fail("**GridServer caught exception: "+e);
        }

        myOA.shutdownObject(myGrid);

        System.out.println("**Grid server exiting**");
    }
}