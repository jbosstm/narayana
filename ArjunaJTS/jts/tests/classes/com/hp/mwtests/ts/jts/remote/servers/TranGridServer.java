/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jts.remote.servers;

import static org.junit.Assert.fail;

import com.hp.mwtests.ts.jts.utils.ServerORB;
import org.junit.Test;

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.Services;
import com.hp.mwtests.ts.jts.orbspecific.resources.trangrid_i;
import com.hp.mwtests.ts.jts.resources.TestUtility;

public class TranGridServer
{
    @Test
    public void test() throws Exception
    {
        ServerORB orb = new ServerORB();
        ORB myORB = orb.getORB();
        RootOA myOA = orb.getOA();

        String serverName = "TranGrid";
        String refFile = "/tmp/trangrid.ref";

        if (System.getProperty("os.name").startsWith("Windows"))
        {
            refFile = "C:\\temp\\trangrid.ref";
        }

        trangrid_i gridI = new trangrid_i((short) 100, (short) 100);
        Services serv = new Services(myORB);

        try
        {
            TestUtility.registerService(refFile, myORB.orb().object_to_string(gridI.getReference()));

            System.out.println("**TranGrid server started**");
            //assertReady();

            myOA.run();
        }
        catch (Exception e)
        {
            fail("TranGrid server caught exception: "+e);
        }

        myOA.shutdownObject(gridI);

        System.out.println("**TranGrid server exiting**");
    }
}