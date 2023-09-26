/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jts.remote.servers;

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.Services;
import com.hp.mwtests.ts.jts.TestModule.stackHelper;
import com.hp.mwtests.ts.jts.TestModule.stackPOATie;
import com.hp.mwtests.ts.jts.orbspecific.resources.StackImple;
import com.hp.mwtests.ts.jts.resources.TestUtility;
import com.hp.mwtests.ts.jts.utils.ServerORB;

public class StackServer
{
   public static void main(String[] args) throws Exception
    {
        ServerORB orb = new ServerORB();
        ORB myORB = orb.getORB();
        RootOA myOA = orb.getOA();

        String refFile = args[0];

        stackPOATie theStack = new stackPOATie (new StackImple());

        myOA.objectIsReady(theStack);

        Services serv = new Services(myORB);

        try
        {
            TestUtility.registerService(refFile, myORB.orb().object_to_string(stackHelper.narrow(myOA.corbaReference(theStack))));

            System.out.println("Ready");

            myOA.run();
        }
        catch (Exception e)
        {
            TestUtility.fail("StackServer caught exception: "+e);
        }

        myOA.shutdownObject(theStack);

        System.out.println("**StackServer exiting**");
    }
}