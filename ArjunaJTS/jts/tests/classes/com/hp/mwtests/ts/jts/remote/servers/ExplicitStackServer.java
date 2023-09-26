/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jts.remote.servers;

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.Services;
import com.hp.mwtests.ts.jts.TestModule.ExplicitStackPOATie;
import com.hp.mwtests.ts.jts.orbspecific.resources.ExplicitStackImple;
import com.hp.mwtests.ts.jts.resources.TestUtility;
import com.hp.mwtests.ts.jts.utils.ServerORB;

public class ExplicitStackServer
{
    public static void main(String[] args) throws Exception
    {
        ServerORB orb = new ServerORB();
        ORB myORB = orb.getORB();
        RootOA myOA = orb.getOA();

        String refFile = args[0];

        ExplicitStackPOATie theObject = new ExplicitStackPOATie (new ExplicitStackImple());

        myOA.objectIsReady(theObject);

        Services serv = new Services(myORB);

        try
        {
            TestUtility.registerService(refFile, myORB.orb().object_to_string(myOA.corbaReference(theObject)));

            System.out.println("Ready");

            myOA.run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        myOA.shutdownObject(theObject);

        System.out.println("**ExplicitStackServer exiting**");
    }
}